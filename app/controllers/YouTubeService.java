//We certify that this submission is the original work of the members of the group and meets the Faculty's Expectations of Originality.
//Signed by- Aryan Awasthi, Harsukhvir Singh Grewal, Sharun Basnet
// 40278847, 40310953, 40272435
package controllers;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class YouTubeService {
    private static final String API_KEY = "AIzaSyDDresrMUXm0WOThwntrZDEt8pL3j4dOsA"; // Replace with your actual API key
    private static final String APPLICATION_NAME = "TubeLytics";
    private static final long MAX_RESULTS = 10;
    private final YouTube youtube;

    /**
     * Constructor that initializes the YouTube instance with the provided YouTube object.
     *
     * @param youtube The YouTube instance.
     */
    public YouTubeService(YouTube youtube) {
        this.youtube = youtube;
    }

    /**
     * Default constructor that initializes the YouTube client using API key and sets application name.
     * This constructor throws a runtime exception if YouTube client initialization fails.
     */
    public YouTubeService() {
        try {
            youtube = new YouTube.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    JacksonFactory.getDefaultInstance(),
                    null
            ).setApplicationName(APPLICATION_NAME).build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize YouTube client", e);
        }
    }

    /**
     * Fetches the profile information of a channel by its ID.
     *
     * @param channelId The ID of the YouTube channel.
     * @return The Channel object containing channel profile details.
     * @throws IOException If an I/O error occurs while fetching channel details.
     */
    public Channel getChannelProfile(String channelId) throws IOException {
        YouTube.Channels.List request = youtube.channels().list("snippet,statistics");
        request.setId(channelId);
        request.setKey(API_KEY);

        ChannelListResponse response = request.execute();
        if (response.getItems().isEmpty()) {
            throw new IOException("No channel found for ID: " + channelId);
        }
        return response.getItems().get(0);
    }

    /**
     * Fetches the latest videos for a given channel by its ID.
     *
     * @param channelId The ID of the YouTube channel.
     * @param limit     The maximum number of videos to return.
     * @return A list of VideoResult objects representing the latest videos.
     * @throws IOException If an I/O error occurs while fetching videos.
     */
    public List<VideoResult> getLatestVideosByChannel(String channelId, int limit) throws IOException {
        YouTube.Search.List request = youtube.search().list("snippet");
        request.setChannelId(channelId);
        request.setMaxResults((long) limit);
        request.setOrder("date");
        request.setKey(API_KEY);

        List<SearchResult> searchResults = request.execute().getItems();

        return searchResults.stream()
                .map(result -> {
                    String videoId = result.getId().getVideoId();
                    return getVideoDetails(videoId); // Fetch video details including tags
                })
                .collect(Collectors.toList());
    }

    /**
     * Searches for videos based on a query string and returns a list of video details.
     *
     * @param query The search query string.
     * @return A list of VideoResult objects for the videos found.
     */
    public List<VideoResult> searchVideos(String query) {
        List<VideoResult> videoResults = new ArrayList<>();
        try {
            YouTube.Search.List search = youtube.search().list("snippet");
            search.setQ(query);
            search.setMaxResults(MAX_RESULTS);
            search.setType("video");
            search.setKey(API_KEY);

            SearchListResponse response = search.execute();
            List<SearchResult> results = response.getItems();

            for (SearchResult result : results) {
                String videoId = result.getId().getVideoId();
                VideoResult videoDetail = getVideoDetails(videoId); // Fetch detailed info including tags
                if (videoDetail != null) {
                    videoResults.add(videoDetail);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return videoResults;
    }

    /**
     * Fetches detailed information about a video by its ID, including tags.
     *
     * @param videoId The ID of the YouTube video.
     * @return A VideoResult object containing the detailed video information, or null if not found.
     */
    public VideoResult getVideoDetails(String videoId) {
        try {
            YouTube.Videos.List request = youtube.videos().list("snippet");
            request.setId(videoId);
            request.setKey(API_KEY);

            VideoListResponse response = request.execute();
            if (response.getItems().isEmpty()) {
                return null; // Video not found
            }

            Video video = response.getItems().get(0);
            String title = video.getSnippet().getTitle();
            String description = video.getSnippet().getDescription();
            String channelId = video.getSnippet().getChannelId();
            String channelTitle = video.getSnippet().getChannelTitle();
            String thumbnailUrl = video.getSnippet().getThumbnails().getDefault().getUrl();
            List<String> tags = video.getSnippet().getTags() != null ? video.getSnippet().getTags() : new ArrayList<>();

            return new VideoResult(title, description, videoId, channelId, thumbnailUrl, channelTitle, tags);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<VideoResult> searchVideosByTag(String tag) {
        List<VideoResult> videoResults = new ArrayList<>();
        try {
            YouTube.Search.List search = youtube.search().list("snippet");
            search.setQ(tag);
            search.setType("video");
            search.setMaxResults(10L); // Fetch up to 10 results
            search.setKey(API_KEY);

            SearchListResponse response = search.execute();
            List<SearchResult> results = response.getItems();

            for (SearchResult result : results) {
                String videoId = result.getId().getVideoId();
                VideoResult videoDetail = getVideoDetails(videoId);
                if (videoDetail != null) {
                    videoResults.add(videoDetail);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return videoResults;
    }
}
