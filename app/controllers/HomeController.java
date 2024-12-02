//We certify that this submission is the original work of the members of the group and meets the Faculty's Expectations of Originality.
//Signed by- Aryan Awasthi, Harsukhvir Singh Grewal, Sharun Basnet
// 40278847, 40310953, 40272435
package controllers;
import actors.WordStatsActor;
import actors.TagsActor;
import akka.actor.ActorRef;
import akka.stream.OverflowStrategy;
import com.fasterxml.jackson.databind.JsonNode;
import play.libs.Json;
import play.libs.streams.ActorFlow;
import actors.UserActor;
import models.VideoResult;
import services.YouTubeService;
import play.mvc.*;
import views.html.index;
import views.html.results;
import views.html.videoDetails;
import play.cache.SyncCacheApi;
import com.google.api.services.youtube.model.Channel;
import javax.inject.Inject;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import akka.actor.ActorSystem;
import akka.stream.Materializer;
import play.mvc.WebSocket;
import akka.pattern.Patterns;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.List;

public class HomeController extends Controller {

    private final YouTubeService youTubeService;
    private final SyncCacheApi cache;
    private final ActorSystem actorSystem;
    private final Materializer materializer;
    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);
    // Cache for storing search results
    private final Map<String, List<VideoResult>> videoCache = new HashMap<>();

    private final LinkedList<Map.Entry<String, List<VideoResult>>> searchHistory = new LinkedList<>();

    /**
     * Constructor for HomeController.
     *
     * @param youTubeService Service to interact with YouTube API.
     * @param cache The cache API to store search history and results.
     */

    @Inject
    public HomeController(YouTubeService youTubeService, SyncCacheApi cache, ActorSystem actorSystem, Materializer materializer) {
        this.youTubeService = youTubeService;
        this.cache = cache;
        this.actorSystem = actorSystem;
        this.materializer = materializer;
    }

    public WebSocket searchWebSocket() {
        return WebSocket.Text.accept(request -> {
            return ActorFlow.actorRef(
                    out -> UserActor.props(out, youTubeService),
                    256, // buffer size
                    OverflowStrategy.dropHead(), // overflow strategy
                    actorSystem,
                    materializer
            );
        });
    }

    /**
     * Renders the homepage with a search box.
     *
     * @param request The HTTP request object.
     * @return The rendered homepage.
     */
    public Result index(Http.Request request) {
        return ok(index.render("TubeLytics", request));
    }

    /**
     * Handles the search request, displays video results and updates search history.
     *
     * @param query The search query entered by the user.
     * @param request The HTTP request object.
     * @return A CompletionStage that returns the rendered search results.
     */
    public CompletionStage<Result> search(String query, Http.Request request) {
        if (query == null || query.isEmpty()) {
            return CompletableFuture.completedFuture(ok("Please provide a search query."));
        }

        // Get or create a session ID
        String sessionId = request.session().getOptional("sessionId").orElseGet(() -> {
            String id = UUID.randomUUID().toString();
            request.session().adding("sessionId", id);
            return id;
        });
        String cacheKey = "searchHistory_" + sessionId;

        // Check cache for the query
        if (videoCache.containsKey(query)) {
            // Return cached result
            List<VideoResult> cachedVideos = videoCache.get(query);
            // Add to search history as usual
            LinkedList<Map.Entry<String, List<VideoResult>>> sessionSearchHistory = cache.getOptional(cacheKey)
                    .map(obj -> (LinkedList<Map.Entry<String, List<VideoResult>>>) obj)
                    .orElseGet(LinkedList::new);

            // Add to search history
            sessionSearchHistory.addFirst(new AbstractMap.SimpleEntry<>(query, cachedVideos));
            if (sessionSearchHistory.size() > 10) {
                sessionSearchHistory.removeLast();
            }

            // Save the updated history in cache
            cache.set(cacheKey, sessionSearchHistory);

            return CompletableFuture.completedFuture(ok(results.render(sessionSearchHistory, request)));
        }

        // If not in cache, fetch from YouTube API and store in cache
        return CompletableFuture.supplyAsync(() -> {
            List<VideoResult> videos = youTubeService.searchVideos(query);

            // Process videos and limit to first 10 with non-empty descriptions
            List<VideoResult> processedVideos = videos.stream()
                    .filter(video -> !video.getDescription().isEmpty())
                    .limit(10)
                    .collect(Collectors.toList());

            // Retrieve or initialize session-specific search history
            LinkedList<Map.Entry<String, List<VideoResult>>> sessionSearchHistory = cache.getOptional(cacheKey)
                    .map(obj -> (LinkedList<Map.Entry<String, List<VideoResult>>>) obj)
                    .orElseGet(LinkedList::new);

            // Store the result in the cache
            videoCache.put(query, processedVideos);

            // Add to search history
            sessionSearchHistory.addFirst(new AbstractMap.SimpleEntry<>(query, processedVideos));
            if (sessionSearchHistory.size() > 10) {
                sessionSearchHistory.removeLast();
            }

            // Save the updated history in cache
            cache.set(cacheKey, sessionSearchHistory);

            return ok(results.render(sessionSearchHistory, request)).addingToSession(request, "sessionId", sessionId);
        });
    }


    /**
     * Displays video details, including tags.
     *
     * @param videoId The ID of the video.
     * @return A CompletionStage that returns the rendered video details.
     */
    public CompletionStage<Result> showVideoDetails(String videoId) {
        ActorRef tagsActor = actorSystem.actorOf(TagsActor.props(youTubeService));

        CompletionStage<Object> futureResult = Patterns.ask(
                tagsActor,
                new TagsActor.ViewVideoDetails(videoId),
                Duration.ofSeconds(5)
        );

        return futureResult.thenApply(response -> {
            if (response instanceof VideoResult) {
                VideoResult video = (VideoResult) response;
                return ok(videoDetails.render(video));
            } else {
                return internalServerError("Failed to fetch video details.");
            }
        });
    }

    /**
     * View tags for a search query and render a page with videos and their tags.
     *
     * @param query The search query.
     * @return A CompletionStage rendering videos related to the query.
     */
    public CompletionStage<Result> viewTags(String query) {
        ActorRef tagsActor = actorSystem.actorOf(TagsActor.props(youTubeService));

        CompletionStage<Object> futureResult = Patterns.ask(
                tagsActor,
                new TagsActor.ViewTags(query),
                Duration.ofSeconds(5)
        );

        return futureResult.thenApply(response -> {
            if (response instanceof List) {
                @SuppressWarnings("unchecked")
                List<VideoResult> videos = (List<VideoResult>) response;
                return ok(views.html.tagResults.render(query, videos));
            } else {
                return internalServerError("Failed to fetch videos for the query.");
            }
        });
    }


    /**
     * Fetch videos related to a specific tag and render them.
     *
     * @param tag The tag to search for.
     * @return A CompletionStage rendering videos associated with the tag.
     */
    public CompletionStage<Result> searchByTag(String tag) {
        ActorRef tagsActor = actorSystem.actorOf(TagsActor.props(youTubeService));

        CompletionStage<Object> futureResult = Patterns.ask(
                tagsActor,
                new TagsActor.SearchByTag(tag),
                Duration.ofSeconds(5)
        );

        return futureResult.thenApply(response -> {
            if (response instanceof List) {
                @SuppressWarnings("unchecked")
                List<VideoResult> videos = (List<VideoResult>) response;
                return ok(views.html.tagResults.render(tag, videos));
            } else {
                return internalServerError("Failed to fetch videos for the tag.");
            }
        });
    }


    /**
     * Generates word statistics for a given search query.
     *
     * @param query The search query.
     * @return A CompletionStage that returns the rendered word statistics.
     */
    public CompletionStage<Result> wordStats(String query) {
        if (query == null || query.trim().isEmpty()) {
            return CompletableFuture.completedFuture(
                    Results.badRequest("Please provide a search query.")
            );
        }

        // Create the WordStatsActor
        ActorRef wordStatsActor = actorSystem.actorOf(WordStatsActor.props(youTubeService));

        // Ask the actor to compute word stats
        CompletionStage<Object> statsFuture = akka.pattern.Patterns.ask(
                wordStatsActor,
                query,
                Duration.ofSeconds(5) // Timeout for response
        );

        // Handle the actor's response
        return statsFuture.thenApply(response -> {
            if (response instanceof String) {
                JsonNode jsonResponse = Json.parse((String) response);
                if (jsonResponse.has("error")) {
                    return Results.internalServerError(jsonResponse);
                } else if (jsonResponse.has("message")) {
                    return ok(views.html.wordStats.render(query, new LinkedHashMap<>(), jsonResponse.get("message").asText()));
                } else {
                    // Deserialize the JSON into a Map<String, Long>
                    Map<String, Long> wordFrequency = new LinkedHashMap<>();
                    jsonResponse.fields().forEachRemaining(entry -> {
                        wordFrequency.put(entry.getKey(), entry.getValue().asLong()); // Convert explicitly to Long
                    });
                    return ok(views.html.wordStats.render(query, wordFrequency, null));
                }
            } else {
                return internalServerError("Unexpected response from WordStatsActor");
            }
        });
    }


    /**
     * Displays the channel profile page along with the latest videos.
     *
     * @param channelId The ID of the YouTube channel.
     * @return A CompletionStage that returns the rendered channel profile page.
     */
    /**
     * Renders the channel profile page.
     * @param channelId The ID of the channel to display.
     * @return A CompletionStage that renders the profile view.
     */
    public CompletionStage<Result> channelProfile(String channelId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Fetch channel profile and videos
                Channel channel = youTubeService.getChannelProfile(channelId);
                List<VideoResult> videos = youTubeService.getLast10Videos(channelId);

                // Render view with the fetched data
                return ok(views.html.channelProfile.render(channel, videos));
            } catch (Exception e) {
                return internalServerError("Error fetching channel profile: " + e.getMessage());
            }
        });
    }


}
