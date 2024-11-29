package actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import com.google.api.services.youtube.model.Channel;
import models.VideoResult;
import models.YouTubeService;
import play.libs.Json;

import java.util.List;

/**
 * Actor for handling channel profile tasks.
 */
public class ChannelProfileActor extends AbstractActor {

    private final String channelId;
    private final ActorRef out;
    private final YouTubeService youTubeService;

    public static Props props(String channelId, ActorRef out, YouTubeService youTubeService) {
        return Props.create(ChannelProfileActor.class, () -> new ChannelProfileActor(channelId, out, youTubeService));
    }

    public ChannelProfileActor(String channelId, ActorRef out, YouTubeService youTubeService) {
        this.channelId = channelId;
        this.out = out;
        this.youTubeService = youTubeService;

        // Fetch channel profile and send to client
        try {
            Channel channel = youTubeService.getChannelProfile(channelId);
            List<VideoResult> latestVideos = youTubeService.getLatestVideosByChannel(channelId, 10);

            ChannelProfileData data = new ChannelProfileData(channel, latestVideos);
            out.tell(Json.toJson(data).toString(), self());
        } catch (Exception e) {
            out.tell(Json.newObject().put("error", "Unable to fetch channel information").toString(), self());
        }
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .build();
    }

    // Helper class to encapsulate channel data
    public static class ChannelProfileData {
        public Channel channel;
        public List<VideoResult> latestVideos;

        public ChannelProfileData(Channel channel, List<VideoResult> latestVideos) {
            this.channel = channel;
            this.latestVideos = latestVideos;
        }
    }
}
