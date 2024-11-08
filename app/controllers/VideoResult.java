package controllers;

import java.util.List;
import java.util.ArrayList; // Import ArrayList for tags

public class VideoResult {
    private String title;
    private String description;
    private String videoId;
    private String channelId;
    private String thumbnailUrl;
    private String channelTitle; // New field for channel title
    private List<String> tags;   // New field for tags

    // Updated constructor to include channelTitle and tags
    public VideoResult(String title, String description, String videoId, String channelId, String thumbnailUrl, String channelTitle, List<String> tags) {
        this.title = title;
        this.description = description;
        this.videoId = videoId;
        this.channelId = channelId;
        this.thumbnailUrl = thumbnailUrl;
        this.channelTitle = channelTitle;
        this.tags = tags != null ? tags : new ArrayList<>(); // Initialize tags if null
    }

    // Getter for title
    public String getTitle() {
        return title;
    }

    // Getter for description
    public String getDescription() {
        return description;
    }

    // Method to get the video URL
    public String getVideoUrl() {
        return "https://www.youtube.com/watch?v=" + videoId;
    }

    // Method to get the channel URL
    public String getChannelUrl() {
        return "https://www.youtube.com/channel/" + channelId;
    }

    // Getter for thumbnail URL
    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    // Getter for channel title
    public String getChannelTitle() {
        return channelTitle;
    }
    public String getChannelId(){return channelId;}
    // Getter for tags
    public List<String> getTags() {
        return tags;
    }

    public String getVideoId() { return videoId;}
}
