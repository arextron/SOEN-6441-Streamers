package actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import models.VideoResult;
import models.YouTubeService;

import java.util.List;

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
                    // Fetch the latest 10 videos
                    List<VideoResult> latestVideos = youTubeService.searchVideos(query).subList(0, 10);
                    out.tell(play.libs.Json.toJson(latestVideos).toString(), self());

                    // Simulate new results arriving over time
                    getContext().getSystem().scheduler().scheduleWithFixedDelay(
                            scala.concurrent.duration.Duration.create(10, "seconds"),
                            scala.concurrent.duration.Duration.create(10, "seconds"),
                            () -> {
                                List<VideoResult> newVideos = youTubeService.searchVideos(query).subList(0, 10);
                                out.tell(play.libs.Json.toJson(newVideos).toString(), self());
                            },
                            getContext().getSystem().dispatcher()
                    );
                })
                .build();
    }
}
