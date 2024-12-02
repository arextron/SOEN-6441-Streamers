package models;

import java.util.List;
import java.util.ArrayList;

/**
 * Model representing a video result.
 * Encapsulates information about a video fetched from the YouTube API, including metadata and related URLs.
 */
public class VideoResult {

    private String title;
    private String description;
    private String videoId;
    private String channelId;
    private String thumbnailUrl;
    private String channelTitle;
    private List<String> tags;

    /**
     * Constructor for VideoResult.
     *
     * @param title The title of the video.
     * @param description The description of the video.
     * @param videoId The unique identifier for the video.
     * @param channelId The unique identifier for the video's channel.
     * @param thumbnailUrl The URL for the video's thumbnail image.
     * @param channelTitle The title of the channel hosting the video.
     * @param tags A list of tags associated with the video.
     */
    public VideoResult(String title, String description, String videoId, String channelId, String thumbnailUrl, String channelTitle, List<String> tags) {
        this.title = title;
        this.description = description;
        this.videoId = videoId;
        this.channelId = channelId;
        this.thumbnailUrl = thumbnailUrl;
        this.channelTitle = channelTitle;
        this.tags = tags != null ? tags : new ArrayList<>();
    }

    /**
     * Gets the title of the video.
     *
     * @return The video title.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Gets the description of the video.
     *
     * @return The video description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets the URL for watching the video on YouTube.
     *
     * @return The video URL.
     */
    public String getVideoUrl() {
        return "https://www.youtube.com/watch?v=" + videoId;
    }

    /**
     * Gets the URL for the channel hosting the video.
     *
     * @return The channel URL.
     */
    public String getChannelUrl() {
        return "https://www.youtube.com/channel/" + channelId;
    }

    /**
     * Gets the URL for the video's thumbnail image.
     *
     * @return The thumbnail URL.
     */
    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    /**
     * Gets the title of the channel hosting the video.
     *
     * @return The channel title.
     */
    public String getChannelTitle() {
        return channelTitle;
    }

    /**
     * Gets the unique identifier for the channel hosting the video.
     *
     * @return The channel ID.
     */
    public String getChannelId() {
        return channelId;
    }

    /**
     * Gets the list of tags associated with the video.
     *
     * @return A list of video tags.
     */
    public List<String> getTags() {
        return tags;
    }

    /**
     * Gets the unique identifier for the video.
     *
     * @return The video ID.
     */
    public String getVideoId() {
        return videoId;
    }
}
