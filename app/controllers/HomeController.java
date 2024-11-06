package controllers;

import play.mvc.*;
import views.html.index;
import views.html.results;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class HomeController extends Controller {

    private final YouTubeService youTubeService;

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

            // Pass the Java List directly to the render method
            return ok(views.html.results.render(query, videos));
        });
    }
}
