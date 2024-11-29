//We certify that this submission is the original work of the members of the group and meets the Faculty's Expectations of Originality.
//Signed by- Aryan Awasthi, Harsukhvir Singh Grewal, Sharun Basnet
// 40278847, 40310953, 40272435
package controllers;
import akka.stream.OverflowStrategy;
import play.libs.streams.ActorFlow;
import actors.UserActor;
import akka.actor.PoisonPill;
import akka.stream.OverflowStrategy;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import models.VideoResult;
import models.YouTubeService;
import play.mvc.*;
import views.html.index;
import views.html.results;
import views.html.videoDetails;
import views.html.searchResults;
import views.html.channelProfile;
import play.cache.SyncCacheApi;
import com.google.api.services.youtube.model.Channel;
import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.stream.Materializer;
import akka.stream.javadsl.Flow;
import play.mvc.WebSocket;
import javax.inject.Inject;
import javax.inject.Singleton;


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
        return CompletableFuture.supplyAsync(() -> {
            VideoResult video = youTubeService.getVideoDetails(videoId);
            if (video == null) {
                return notFound("Video not found");
            }
            return ok(videoDetails.render(video));
        });
    }
    /**
     * View tags for a search query and render a page with videos and their tags.
     *
     * @param query The search query.
     * @return A CompletionStage rendering videos related to the query.
     */
    public CompletionStage<Result> viewTags(String query) {
        return CompletableFuture.supplyAsync(() -> {
            List<VideoResult> videos = youTubeService.searchVideos(query);
            return ok(views.html.tagResults.render(query, videos));
        });
    }

    /**
     * Fetch videos related to a specific tag and render them.
     *
     * @param tag The tag to search for.
     * @return A CompletionStage rendering videos associated with the tag.
     */
    public CompletionStage<Result> searchByTag(String tag) {
        return CompletableFuture.supplyAsync(() -> {
            List<VideoResult> videos = youTubeService.searchVideosByTag(tag);
            return ok(views.html.tagResults.render(tag, videos));
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

        return CompletableFuture.supplyAsync(() -> {
            try {
                List<VideoResult> videos = youTubeService.searchVideos(query);

                if (videos.isEmpty()) {
                    return ok("No word frequency data available for \"" + query + "\".");
                }

                // Process only the first 50 videos (or fewer if not enough results are available)
                List<VideoResult> latestVideos = videos.stream()
                        .filter(video -> video.getDescription() != null && !video.getDescription().isEmpty())
                        .limit(50)
                        .collect(Collectors.toList());

                Map<String, Long> wordFrequency = latestVideos.stream()
                        .flatMap(video -> Arrays.stream(video.getDescription().split("\\W+")))
                        .map(String::toLowerCase)
                        .filter(word -> !word.isEmpty())
                        .collect(Collectors.groupingBy(word -> word, Collectors.counting()));

                Map<String, Long> sortedWordFrequency = wordFrequency.entrySet().stream()
                        .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                        .limit(100)
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                Map.Entry::getValue,
                                (e1, e2) -> e1,
                                LinkedHashMap::new
                        ));

                return ok(views.html.wordStats.render(query, sortedWordFrequency));
            } catch (Exception e) {
                logger.error("Error processing word statistics for query: " + query, e);
                return internalServerError("An error occurred while processing your request.");
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
                Channel channel = youTubeService.getChannelProfile(channelId);
                List<VideoResult> latestVideos = youTubeService.getLatestVideosByChannel(channelId, 10);
                return ok(channelProfile.render(channel, latestVideos));
            } catch (Exception e) {
                logger.error("Failed to fetch channel information for channel ID: " + channelId, e);
                return internalServerError("Unable to fetch channel information");
            }
        });
    }

}
