// We certify that this submission is the original work of the members of the group and meets the Faculty's Expectations of Originality.
// Signed by- Aryan Awasthi, Harsukhvir Singh Grewal, Sharun Basnet
// 40278847, 40310953, 40272435

package controllers;

import actors.TagsActor;
import actors.UserActor;
import actors.WordStatsActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.stream.Materializer;
import akka.stream.OverflowStrategy;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.api.services.youtube.model.Channel;
import models.VideoResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.cache.SyncCacheApi;
import play.i18n.MessagesApi;
import play.libs.Json;
import play.libs.streams.ActorFlow;
import play.mvc.*;
import services.YouTubeService;
import views.html.index;
import views.html.results;
import views.html.videoDetails;

import javax.inject.Inject;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

/**
 * Controller class for handling YouTube video search, video details, and word statistics.
 */
public class HomeController extends Controller {

    private final YouTubeService youTubeService;
    private final SyncCacheApi cache;
    private final ActorSystem actorSystem;
    private final Materializer materializer;
    private final ActorRef tagsActor;
    private final MessagesApi messagesApi;

    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

    // Cache for storing search results
    final Map<String, List<VideoResult>> videoCache = new HashMap<>();

    /**
     * Constructor for HomeController.
     *
     * @param youTubeService Service to interact with YouTube API.
     * @param cache          The cache API to store search history and results.
     * @param actorSystem    The ActorSystem for creating actors.
     * @param materializer   The Materializer for Akka streams.
     * @param messagesApi    The MessagesApi for internationalization.
     */
    @Inject
    public HomeController(YouTubeService youTubeService, SyncCacheApi cache, ActorSystem actorSystem, Materializer materializer, MessagesApi messagesApi) {
        this(youTubeService, cache, actorSystem, materializer, null, messagesApi);
    }

    /**
     * Overloaded constructor to allow injection of a custom TagsActor for testing purposes.
     *
     * @param youTubeService Service to interact with YouTube API.
     * @param cache          The cache API to store search history and results.
     * @param actorSystem    The ActorSystem for creating actors.
     * @param materializer   The Materializer for Akka streams.
     * @param tagsActor      The TagsActor to use; if null, a new one is created.
     * @param messagesApi    The MessagesApi for internationalization.
     */
    public HomeController(YouTubeService youTubeService, SyncCacheApi cache, ActorSystem actorSystem, Materializer materializer, ActorRef tagsActor, MessagesApi messagesApi) {
        this.youTubeService = youTubeService;
        this.cache = cache;
        this.actorSystem = actorSystem;
        this.materializer = materializer;
        this.messagesApi = messagesApi;
        if (tagsActor != null) {
            this.tagsActor = tagsActor;
        } else {
            this.tagsActor = actorSystem.actorOf(TagsActor.props(youTubeService), "tagsActor");
        }
    }

    /**
     * Provides a WebSocket for real-time search updates.
     *
     * @return A WebSocket.
     */
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
        return ok(index.render("YT Lytics", request));
    }

    /**
     * Handles the search request, displays video results, and updates search history.
     *
     * @param query   The search query entered by the user.
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
        CompletionStage<Object> futureResult = akka.pattern.Patterns.ask(
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
        CompletionStage<Object> futureResult = akka.pattern.Patterns.ask(
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
        if (tag == null || tag.trim().isEmpty()) {
            logger.warn("Received a null or empty tag for search.");
            return CompletableFuture.completedFuture(badRequest("Tag must not be empty."));
        }

        if (tagsActor == null) {
            logger.error("TagsActor is not initialized.");
            return CompletableFuture.completedFuture(internalServerError("TagsActor not initialized."));
        }

        try {
            return akka.pattern.Patterns.ask(tagsActor, new TagsActor.SearchByTag(tag), Duration.ofSeconds(5))
                    .thenApply(response -> {
                        if (response instanceof List) {
                            @SuppressWarnings("unchecked")
                            List<VideoResult> videos = (List<VideoResult>) response;
                            if (videos.isEmpty()) {
                                logger.info("No results found for tag: {}", tag);
                                return notFound("No results found for the specified tag.");
                            } else {
                                logger.info("Search results found for tag: {}", tag);
                                return ok(Json.toJson(videos));
                            }
                        } else if (response instanceof TagsActor.ErrorMessage) {
                            TagsActor.ErrorMessage errorMessage = (TagsActor.ErrorMessage) response;
                            logger.error("Error occurred while searching by tag: {} - {}", tag, errorMessage.message);
                            return internalServerError(errorMessage.message);
                        } else {
                            logger.info("No results found for tag: {}", tag);
                            return notFound("No results found for the specified tag.");
                        }
                    })
                    .exceptionally(ex -> {
                        logger.error("Error occurred while searching by tag: {}", tag, ex);
                        return internalServerError("An error occurred while processing your request.");
                    });
        } catch (Exception e) {
            logger.error("Unexpected error in searchByTag method for tag: {}", tag, e);
            return CompletableFuture.completedFuture(internalServerError("An unexpected error occurred."));
        }
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
