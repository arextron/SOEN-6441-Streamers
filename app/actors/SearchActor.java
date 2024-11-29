package actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import models.VideoResult;
import models.YouTubeService;
import play.libs.Json;
import scala.concurrent.duration.Duration;

import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Actor responsible for fetching new search results periodically.
 */
public class SearchActor extends AbstractActor {

    private final String query;
    private final ActorRef out;
    private final YouTubeService youTubeService;
    private final Set<String> sentVideoIds = new HashSet<>();

    public static Props props(String query, ActorRef out, YouTubeService youTubeService) {
        return Props.create(SearchActor.class, () -> new SearchActor(query, out, youTubeService));
    }

    public SearchActor(String query, ActorRef out, YouTubeService youTubeService) {
        this.query = query;
        this.out = out;
        this.youTubeService = youTubeService;

        // Fetch initial results
        fetchAndSendNewVideos();

        // Schedule periodic task to fetch new videos
        getContext().getSystem().scheduler().scheduleWithFixedDelay(
                Duration.create(30, TimeUnit.SECONDS),
                Duration.create(30, TimeUnit.SECONDS),
                getSelf(),
                new FetchNewVideos(),
                getContext().getSystem().dispatcher(),
                null
        );
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(FetchNewVideos.class, msg -> {
                    fetchAndSendNewVideos();
                })
                .build();
    }

    private void fetchAndSendNewVideos() {
        List<VideoResult> newVideos = youTubeService.searchVideos(query).stream()
                .filter(video -> !sentVideoIds.contains(video.getVideoId()))
                .collect(Collectors.toList());

        if (!newVideos.isEmpty()) {
            sentVideoIds.addAll(newVideos.stream()
                    .map(VideoResult::getVideoId)
                    .collect(Collectors.toSet()));

            // Send new videos to the client
            out.tell(Json.toJson(newVideos).toString(), self());
        }
    }

    static class FetchNewVideos { }
}
