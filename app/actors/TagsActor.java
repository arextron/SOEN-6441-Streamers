package actors;

import akka.actor.AbstractActor;
import akka.actor.Props;
import models.VideoResult;
import services.YouTubeService;

import java.util.List;

/**
 *
 * @author Sharun Basnet
 *
 * Actor to handle video details, tag-based searches, and viewing tags.
 * Communicates with the YouTubeService to fetch video information and tags.
 */
public class TagsActor extends AbstractActor {

    private final YouTubeService youTubeService;

    /**
     * Creates Props for the TagsActor, used for instantiation.
     *
     * @param youTubeService The service used to interact with the YouTube API.
     * @return Props for creating the actor.
     */
    public static Props props(YouTubeService youTubeService) {
        return Props.create(TagsActor.class, () -> new TagsActor(youTubeService));
    }

    /**
     * Constructor for TagsActor.
     *
     * @param youTubeService The service used to interact with the YouTube API.
     */
    public TagsActor(YouTubeService youTubeService) {
        this.youTubeService = youTubeService;
    }

    /**
     * Defines the actor's message handling behavior.
     *
     * @return The Receive object specifying how to handle incoming messages.
     */
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ViewVideoDetails.class, this::handleViewVideoDetails)
                .match(ViewTags.class, this::handleViewTags)
                .match(SearchByTag.class, this::handleSearchByTag)
                .build();
    }

    /**
     * Handles the ViewVideoDetails message to fetch details for a specific video.
     *
     * @param message The ViewVideoDetails message containing the video ID.
     */
    private void handleViewVideoDetails(ViewVideoDetails message) {
        try {
            VideoResult video = youTubeService.getVideoDetails(message.videoId);
            if (video == null) {
                sender().tell(new ErrorMessage("Video not found"), self());
            } else {
                sender().tell(video, self());
            }
        } catch (Exception e) {
            sender().tell(new ErrorMessage("Error fetching video details"), self());
        }
    }

    /**
     * Handles the ViewTags message to fetch videos based on a query.
     *
     * @param message The ViewTags message containing the query string.
     */
    private void handleViewTags(ViewTags message) {
        try {
            List<VideoResult> videos = youTubeService.searchVideos(message.query);
            sender().tell(videos, self());
        } catch (Exception e) {
            sender().tell(new ErrorMessage("Error fetching videos by query"), self());
        }
    }

    /**
     * Handles the SearchByTag message to fetch videos associated with a specific tag.
     *
     * @param message The SearchByTag message containing the tag.
     */
    private void handleSearchByTag(SearchByTag message) {
        try {
            List<VideoResult> videos = youTubeService.searchVideosByTag(message.tag);
            sender().tell(videos, self());
        } catch (Exception e) {
            sender().tell(new ErrorMessage("Error fetching videos by tag"), self());
        }
    }

    /**
     * Message class to request video details by video ID.
     */
    public static class ViewVideoDetails {
        public final String videoId;

        /**
         * Constructor for ViewVideoDetails.
         *
         * @param videoId The ID of the video.
         */
        public ViewVideoDetails(String videoId) {
            this.videoId = videoId;
        }
    }

    /**
     * Message class to request videos by a query.
     */
    public static class ViewTags {
        public final String query;

        /**
         * Constructor for ViewTags.
         *
         * @param query The search query string.
         */
        public ViewTags(String query) {
            this.query = query;
        }
    }

    /**
     * Message class to request videos by a specific tag.
     */
    public static class SearchByTag {
        public final String tag;

        /**
         * Constructor for SearchByTag.
         *
         * @param tag The tag to search videos for.
         */
        public SearchByTag(String tag) {
            this.tag = tag;
        }
    }

    /**
     * Message class to represent errors during video or tag operations.
     */
    public static class ErrorMessage {
        public final String message;

        /**
         * Constructor for ErrorMessage.
         *
         * @param message The error message.
         */
        public ErrorMessage(String message) {
            this.message = message;
        }
    }
}
