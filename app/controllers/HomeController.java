package controllers;

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

public class HomeController extends Controller {

    private final YouTubeService youTubeService;
    private final SyncCacheApi cache;
    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);
    // Cache for storing search results
    private final Map<String, List<VideoResult>> videoCache = new HashMap<>();

    private final LinkedList<Map.Entry<String, List<VideoResult>>> searchHistory = new LinkedList<>();

    @Inject
    public HomeController(YouTubeService youTubeService, SyncCacheApi cache) {
        this.youTubeService = youTubeService;
        this.cache = cache;
    }

    // Render the homepage with the search box
    public Result index(Http.Request request) {
        return ok(index.render());
    }

    // Handle search request and display video results with session-specific history
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
            searchHistory.addFirst(new AbstractMap.SimpleEntry<>(query, cachedVideos));
            if (searchHistory.size() > 10) {
                searchHistory.removeLast();
            }
            return CompletableFuture.completedFuture(ok(results.render(searchHistory)));
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
            LinkedList<Map.Entry<String, List<VideoResult>>> searchHistory = cache.getOptional(cacheKey)
                    .map(obj -> (LinkedList<Map.Entry<String, List<VideoResult>>>) obj)
                    .orElseGet(LinkedList::new);

            // Add to session-specific search history
            // Store the result in the cache
            videoCache.put(query, processedVideos);

            // Add to search history
            searchHistory.addFirst(new AbstractMap.SimpleEntry<>(query, processedVideos));
            if (searchHistory.size() > 10) {
                searchHistory.removeLast();
            }

            // Save the updated history in cache
            cache.set(cacheKey, searchHistory);

            return ok(results.render(searchHistory)).addingToSession(request, "sessionId", sessionId);
        });
    }


    // Show video details, including tags
    public CompletionStage<Result> showVideoDetails(String videoId) {
        return CompletableFuture.supplyAsync(() -> {
            VideoResult video = youTubeService.getVideoDetails(videoId);
            if (video == null) {
                return notFound("Video not found");
            }
            return ok(videoDetails.render(video));
        });
    }



    // Search videos by tag
    public CompletionStage<Result> searchByTag(String tag) {
        return CompletableFuture.supplyAsync(() -> {
            List<VideoResult> videos = youTubeService.searchVideosByTag(tag);
            return ok(searchResults.render(tag, videos));
        });
    }

    // Generate word statistics for a query
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

    // Channel profile page with latest videos
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
