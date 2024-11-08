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

    // Default constructor initializing YouTube instance with API key
    public YouTubeService(YouTube youtube) {
        this.youtube = youtube;
    }

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

    // Fetch channel profile information
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

    // Fetch latest videos for a channel
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

    // Search for videos based on a query
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

    // Get video details including tags
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

    // Search videos by tag
    public List<VideoResult> searchVideosByTag(String tag) {
        List<VideoResult> videoResults = new ArrayList<>();
        try {
            YouTube.Search.List search = youtube.search().list("snippet");
            search.setQ(tag); // Use the tag as the search query
            search.setType("video");
            search.setMaxResults(MAX_RESULTS);
            search.setKey(API_KEY);

            SearchListResponse response = search.execute();
            List<SearchResult> results = response.getItems();

            for (SearchResult result : results) {
                String title = result.getSnippet().getTitle();
                String description = result.getSnippet().getDescription();
                String videoId = result.getId().getVideoId();
                String channelId = result.getSnippet().getChannelId();
                String thumbnailUrl = result.getSnippet().getThumbnails().getDefault().getUrl();
                String channelTitle = result.getSnippet().getChannelTitle();
                List<String> tags = new ArrayList<>(); // Tags can be empty in search results

                videoResults.add(new VideoResult(title, description, videoId, channelId, thumbnailUrl, channelTitle, tags));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return videoResults;
    }
}
