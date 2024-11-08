package controllers;

public class VideoResult {
    private String title;
    private String description;
    private String videoId;
    private String channelId;
    private String thumbnailUrl;

    public VideoResult(String title, String description, String videoId, String channelId, String thumbnailUrl) {
        this.title = title;
        this.description = description;
        this.videoId = videoId;
        this.channelId = channelId;
        this.thumbnailUrl = thumbnailUrl;
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
}
