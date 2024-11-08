package controllers;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.ChannelListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.SearchListResponse;

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
                    null // No need for an HttpRequestInitializer
            ).setApplicationName(APPLICATION_NAME).build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize YouTube client", e);
        }
    }

    // Fetch channel profile information
    public Channel getChannelProfile(String channelId) throws IOException {
        YouTube.Channels.List request = youtube.channels().list("snippet,statistics");
        request.setId(channelId);
        request.setKey(API_KEY); // Set the API key

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
        request.setKey(API_KEY); // Set the API key

        List<SearchResult> searchResults = request.execute().getItems();

        // Log each video's title and medium resolution thumbnail URL to check for duplicates
        for (SearchResult result : searchResults) {
            System.out.println("Video Title: " + result.getSnippet().getTitle());
            System.out.println("Thumbnail URL: " + result.getSnippet().getThumbnails().getMedium().getUrl());
        }

        return searchResults.stream()
                .map(result -> new VideoResult(
                        result.getSnippet().getTitle(),
                        result.getSnippet().getDescription(),
                        result.getId().getVideoId(),
                        channelId,
                        result.getSnippet().getThumbnails().getMedium().getUrl() // Use medium resolution thumbnail
                ))
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
            search.setKey(API_KEY); // Ensure API key is set

            SearchListResponse response = search.execute();
            List<SearchResult> results = response.getItems();

            for (SearchResult result : results) {
                String title = result.getSnippet().getTitle();
                String description = result.getSnippet().getDescription();
                String videoId = result.getId().getVideoId();
                String channelId = result.getSnippet().getChannelId();
                String thumbnailUrl = result.getSnippet().getThumbnails().getMedium().getUrl(); // Use medium resolution

                videoResults.add(new VideoResult(title, description, videoId, channelId, thumbnailUrl));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return videoResults;
    }
}
