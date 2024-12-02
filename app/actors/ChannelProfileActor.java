package actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.api.services.youtube.model.Channel;
import models.VideoResult;
import services.YouTubeService;
import play.libs.Json;

import java.util.List;

/**
 *
 * @author Harsukhvir Singh Grewal
 *
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
     * @param channelId The ID of the YouTube channel.
     * @param out       The actor reference for sending data to the client.
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

    /**
     * Creates the message handling behavior for the actor.
     *
     * @return The Receive object defining the message handling behavior.
     */
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .matchEquals("fetchChannelProfile", msg -> {
                    try {
                        // Fetch channel profile data
                        Channel channel = youTubeService.getChannelProfile(channelId);
                        List<VideoResult> videos = youTubeService.getLast10Videos(channelId);

                        // Combine profile and videos into a single JSON response
                        ObjectNode response = Json.newObject();
                        response.set("channel", Json.toJson(channel));
                        response.set("videos", Json.toJson(videos));

                        out.tell(response, self());
                    } catch (Exception e) {
                        out.tell(Json.newObject().put("error", e.getMessage()), self());
                    }
                })
                .build();
    }

    /**
     * Helper class to encapsulate channel profile data.
     * Used for sending channel information and the latest videos to the client.
     */
    public static class ChannelProfileData {
        public Channel channel;
        public List<VideoResult> latestVideos;

        /**
         * Constructor for ChannelProfileData.
         *
         * @param channel      The channel information.
         * @param latestVideos The list of the latest videos for the channel.
         */
        public ChannelProfileData(Channel channel, List<VideoResult> latestVideos) {
            this.channel = channel;
            this.latestVideos = latestVideos;
        }
    }
}
