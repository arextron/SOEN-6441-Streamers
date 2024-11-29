package actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import models.VideoResult;
import services.YouTubeService;
import play.libs.Json;

import java.util.List;

/**
 * Actor for handling tag-related tasks.
 */
public class TagsActor extends AbstractActor {

    private final String tagOrVideoId;
    private final ActorRef out;
    private final YouTubeService youTubeService;
    private final boolean isTagSearch;

    public static Props props(String tagOrVideoId, ActorRef out, YouTubeService youTubeService, boolean isTagSearch) {
        return Props.create(TagsActor.class, () -> new TagsActor(tagOrVideoId, out, youTubeService, isTagSearch));
    }

    public TagsActor(String tagOrVideoId, ActorRef out, YouTubeService youTubeService, boolean isTagSearch) {
        this.tagOrVideoId = tagOrVideoId;
        this.out = out;
        this.youTubeService = youTubeService;
        this.isTagSearch = isTagSearch;

        if (isTagSearch) {
            searchByTag();
        } else {
            viewTags();
        }
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .build();
    }

    private void searchByTag() {
        List<VideoResult> videos = youTubeService.searchVideosByTag(tagOrVideoId);
        out.tell(Json.toJson(videos).toString(), self());
    }

    private void viewTags() {
        VideoResult video = youTubeService.getVideoDetails(tagOrVideoId);
        if (video != null) {
            out.tell(Json.toJson(video.getTags()).toString(), self());
        } else {
            out.tell(Json.newObject().put("error", "Video not found").toString(), self());
        }
    }
}
