package actors;

import akka.actor.AbstractActor;
import akka.actor.Props;
import models.VideoResult;
import services.YouTubeService;

import java.util.List;

/**
 * Actor to handle video details, tag-based searches, and viewing tags.
 */
public class TagsActor extends AbstractActor {

    private final YouTubeService youTubeService;

    public static Props props(YouTubeService youTubeService) {
        return Props.create(TagsActor.class, () -> new TagsActor(youTubeService));
    }

    public TagsActor(YouTubeService youTubeService) {
        this.youTubeService = youTubeService;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ViewVideoDetails.class, this::handleViewVideoDetails)
                .match(ViewTags.class, this::handleViewTags)
                .match(SearchByTag.class, this::handleSearchByTag)
                .build();
    }

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

    private void handleViewTags(ViewTags message) {
        try {
            List<VideoResult> videos = youTubeService.searchVideos(message.query);
            sender().tell(videos, self());
        } catch (Exception e) {
            sender().tell(new ErrorMessage("Error fetching videos by query"), self());
        }
    }

    private void handleSearchByTag(SearchByTag message) {
        try {
            List<VideoResult> videos = youTubeService.searchVideosByTag(message.tag);
            sender().tell(videos, self());
        } catch (Exception e) {
            sender().tell(new ErrorMessage("Error fetching videos by tag"), self());
        }
    }

    // Messages to be sent to the actor
    public static class ViewVideoDetails {
        public final String videoId;

        public ViewVideoDetails(String videoId) {
            this.videoId = videoId;
        }
    }

    public static class ViewTags {
        public final String query;

        public ViewTags(String query) {
            this.query = query;
        }
    }

    public static class SearchByTag {
        public final String tag;

        public SearchByTag(String tag) {
            this.tag = tag;
        }
    }

    // Error message for handling failures
    public static class ErrorMessage {
        public final String message;

        public ErrorMessage(String message) {
            this.message = message;
        }
    }
}
