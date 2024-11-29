package actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import models.VideoResult;
import scala.concurrent.duration.Duration;
import services.YouTubeService;

import java.util.List;
import java.util.stream.Collectors;

public class UserActor extends AbstractActor {

    private final ActorRef out;
    private final YouTubeService youTubeService;

    public static Props props(ActorRef out, YouTubeService youTubeService) {
        return Props.create(UserActor.class, () -> new UserActor(out, youTubeService));
    }

    public UserActor(ActorRef out, YouTubeService youTubeService) {
        this.out = out;
        this.youTubeService = youTubeService;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(String.class, query -> {
                    // Initial fetch for the query
                    List<VideoResult> initialVideos = youTubeService.searchVideos(query).stream()
                            .limit(10)
                            .collect(Collectors.toList());
                    out.tell(play.libs.Json.toJson(initialVideos).toString(), self());

                    // Periodically fetch updates for the query
                    getContext().getSystem().scheduler().scheduleWithFixedDelay(
                            scala.concurrent.duration.Duration.create(1, "seconds"), // Start immediately
                            scala.concurrent.duration.Duration.create(60, "seconds"), // Update interval
                            () -> {
                                List<VideoResult> updatedVideos = youTubeService.searchVideos(query).stream()
                                        .limit(10)
                                        .collect(Collectors.toList());
                                out.tell(play.libs.Json.toJson(updatedVideos).toString(), self());
                            },
                            getContext().getSystem().dispatcher()
                    );
                })
                .build();
    }
}
