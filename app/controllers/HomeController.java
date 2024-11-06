package controllers;

import play.mvc.*;
import views.html.index;
import views.html.results;


import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

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

            // Add the new search query and its results at the start of the list
            searchHistory.addFirst(new AbstractMap.SimpleEntry<>(query, videos));

            // Keep only the latest 10 search queries
            if (searchHistory.size() > 10) {
                searchHistory.removeLast();
            }

            // Render the results page with the search history
            return ok(results.render(searchHistory));
        });
    }
}
