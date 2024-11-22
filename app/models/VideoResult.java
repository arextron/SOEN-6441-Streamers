
package models;
//We certify that this submission is the original work of the members of the group and meets the Faculty's Expectations of Originality.
//Signed by- Aryan Awasthi, Harsukhvir Singh Grewal, Sharun Basnet
// 40278847, 40310953, 40272435


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

    /**
     * Constructor to initialize a VideoResult object with all fields.
     *
     * @param title The title of the video.
     * @param description The description of the video.
     * @param videoId The unique ID of the video.
     * @param channelId The ID of the channel the video belongs to.
     * @param thumbnailUrl The URL of the video's thumbnail image.
     * @param channelTitle The title of the channel.
     * @param tags The tags associated with the video.
     */
    public VideoResult(String title, String description, String videoId, String channelId, String thumbnailUrl, String channelTitle, List<String> tags) {
        this.title = title;
        this.description = description;
        this.videoId = videoId;
        this.channelId = channelId;
        this.thumbnailUrl = thumbnailUrl;
        this.channelTitle = channelTitle;
        this.tags = tags != null ? tags : new ArrayList<>(); // Initialize tags if null
    }

    /**
     * Gets the title of the video.
     *
     * @return The title of the video.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Gets the description of the video.
     *
     * @return The description of the video.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Generates the video URL using the video's ID.
     *
     * @return The URL of the video.
     */
    public String getVideoUrl() {
        return "https://www.youtube.com/watch?v=" + videoId;
    }

    /**
     * Generates the channel URL using the channel's ID.
     *
     * @return The URL of the channel.
     */
    public String getChannelUrl() {
        return "https://www.youtube.com/channel/" + channelId;
    }

    /**
     * Gets the URL of the video's thumbnail.
     *
     * @return The URL of the video's thumbnail.
     */
    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    /**
     * Gets the title of the channel.
     *
     * @return The title of the channel.
     */
    public String getChannelTitle() {
        return channelTitle;
    }

    /**
     * Gets the unique ID of the channel.
     *
     * @return The channel ID.
     */
    public String getChannelId() {
        return channelId;
    }

    /**
     * Gets the list of tags associated with the video.
     *
     * @return The list of tags.
     */
    public List<String> getTags() {
        return tags;
    }

    /**
     * Gets the unique ID of the video.
     *
     * @return The video ID.
     */
    public String getVideoId() {
        return videoId;
    }
}
