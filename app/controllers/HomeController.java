package controllers;

import play.mvc.*;
import views.html.index;
import views.html.results;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

public class HomeController extends Controller {

    private final YouTubeService youTubeService;
    private final LinkedList<Map.Entry<String, List<VideoResult>>> searchHistory = new LinkedList<>();

    @Inject
    public HomeController(YouTubeService youTubeService) {
        this.youTubeService = youTubeService;
    }

    // Render the homepage with the search box
    public Result index() {
        return ok(index.render());
    }

    // Handle search request and display video results
    public CompletionStage<Result> search(String query) {
        if (query == null || query.isEmpty()) {
            return CompletableFuture.completedFuture(ok("Please provide a search query."));
        }

        return CompletableFuture.supplyAsync(() -> {
            List<VideoResult> videos = youTubeService.searchVideos(query);

            // Use Java Streams to process the videos list:
            // - Filter videos with non-empty descriptions
            // - Limit the list to the first 10 videos
            List<VideoResult> processedVideos = videos.stream()
                    .filter(video -> !video.getDescription().isEmpty()) // Filter out videos with empty descriptions
                    .limit(10) // Limit to the first 10 videos
                    .collect(Collectors.toList());

            // Add the processed video list with the search query to searchHistory
            searchHistory.addFirst(new AbstractMap.SimpleEntry<>(query, processedVideos));

            // Keep only the latest 10 search queries in history
            if (searchHistory.size() > 10) {
                searchHistory.removeLast();
            }
            // Render the results page with the processed search history
            return ok(results.render(searchHistory));
        });
    }
    public CompletionStage<Result> wordStats(String query) {
        return CompletableFuture.supplyAsync(() -> {
            List<VideoResult> videos = youTubeService.searchVideos(query);

            // Process word frequencies using Java Streams
            Map<String, Long> wordFrequency = videos.stream()
                    .flatMap(video -> Arrays.stream(video.getDescription().split("\\W+"))) // Split by non-word characters
                    .map(String::toLowerCase) // Normalize words to lowercase
                    .filter(word -> !word.isEmpty()) // Remove empty words
                    .collect(Collectors.groupingBy(word -> word, Collectors.counting())); // Count occurrences

            // Sort words by frequency in descending order and limit to top 50
            Map<String, Long> sortedWordFrequency = wordFrequency.entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .limit(50)
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue,
                            (e1, e2) -> e1,
                            LinkedHashMap::new
                    ));
            return ok(views.html.wordStats.render(query, sortedWordFrequency));
        });
    }
}
