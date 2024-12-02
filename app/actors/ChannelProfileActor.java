package actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.api.services.youtube.model.Channel;
import models.VideoResult;
import services.YouTubeService;
import play.libs.Json;

import java.io.IOException;
import java.util.List;

/**
 * Actor for handling channel profile tasks.
 * Responsible for fetching channel profile information and the latest videos for a channel.
 * Sends the data to the client through a WebSocket connection.
 */
public class ChannelProfileActor extends AbstractActor {

    private final String channelId;
    private final ActorRef out;
    private final YouTubeService youTubeService;

    /**
     * Creates Props for the actor, used for actor instantiation.
     *
     * @param channelId      The ID of the YouTube channel.
     * @param out            The actor reference for sending data to the client.
     * @param youTubeService The YouTubeService instance for fetching data from the YouTube API.
     * @return Props for creating the actor.
     */
    public static Props props(String channelId, ActorRef out, YouTubeService youTubeService) {
        return Props.create(ChannelProfileActor.class, () -> new ChannelProfileActor(channelId, out, youTubeService));
    }

    /**
     * Constructor for ChannelProfileActor.
     *
     * @param channelId      The ID of the YouTube channel.
     * @param out            The actor reference for sending data to the client.
     * @param youTubeService The YouTubeService instance for fetching data from the YouTube API.
     */
    public ChannelProfileActor(String channelId, ActorRef out, YouTubeService youTubeService) {
        this.channelId = channelId;
        this.out = out;
        this.youTubeService = youTubeService;

        // Immediately fetch the channel profile on actor creation
        fetchChannelProfile();
    }

    /**
     * Fetches the channel profile and latest videos, and sends the data to the client.
     */
    private void fetchChannelProfile() {
        try {
            Channel channel = youTubeService.getChannelProfile(channelId);
            List<VideoResult> latestVideos = youTubeService.getLatestVideosByChannel(channelId, 10);

            ObjectNode response = Json.newObject();
            response.set("channel", Json.toJson(channel));
            response.set("videos", Json.toJson(latestVideos));

            out.tell(response.toString(), self());
        } catch (IOException e) {
            sendErrorResponse("Unable to fetch channel information: " + e.getMessage());
        }
    }

    /**
     * Sends an error response to the client.
     *
     * @param message The error message to send.
     */
    private void sendErrorResponse(String message) {
        out.tell(Json.newObject().put("error", message).toString(), self());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .matchEquals("fetchChannelProfile", msg -> fetchChannelProfile())
                .build();
    }
}
