package models;

import java.util.List;
import java.util.ArrayList;

/**
 * Model representing a video result.
 */
public class VideoResult {

    private String title;
    private String description;
    private String videoId;
    private String channelId;
    private String thumbnailUrl;
    private String channelTitle;
    private List<String> tags;

    public VideoResult(String title, String description, String videoId, String channelId, String thumbnailUrl, String channelTitle, List<String> tags) {
        this.title = title;
        this.description = description;
        this.videoId = videoId;
        this.channelId = channelId;
        this.thumbnailUrl = thumbnailUrl;
        this.channelTitle = channelTitle;
        this.tags = tags != null ? tags : new ArrayList<>();
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getVideoUrl() {
        return "https://www.youtube.com/watch?v=" + videoId;
    }

    public String getChannelUrl() {
        return "https://www.youtube.com/channel/" + channelId;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public String getChannelTitle() {
        return channelTitle;
    }

    public String getChannelId() {
        return channelId;
    }

    public List<String> getTags() {
        return tags;
    }

    public String getVideoId() {
        return videoId;
    }
}
