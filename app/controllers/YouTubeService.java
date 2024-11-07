package controllers;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class YouTubeService {
    private static final String API_KEY = "AIzaSyDDresrMUXm0WOThwntrZDEt8pL3j4dOsA"; // Replace with your actual API key
    private static final String APPLICATION_NAME = "TubeLytics";
    private static final long MAX_RESULTS = 10;

    private final YouTube youtube;

    public YouTubeService() {
        // Initialize the YouTube instance
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

    // Search videos by query
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
                //String title = result.getSnippet().getTitle();
                //String description = result.getSnippet().getDescription();
                String videoId = result.getId().getVideoId();
                VideoResult videoDetail = getVideoDetails(videoId); // Fetch detailed info including tags
                if (videoDetail != null) {
                    videoResults.add(videoDetail);
                }
                /*String channelId = result.getSnippet().getChannelId();
                String thumbnailUrl = result.getSnippet().getThumbnails().getDefault().getUrl();
                String channelTitle = result.getSnippet().getChannelTitle(); // Retrieve the channel title
                List<String> tags = new ArrayList<>(); // Tags can be empty in search results


                videoResults.add(new VideoResult(title, description, videoId, channelId, thumbnailUrl, channelTitle, tags));*/
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
                String channelTitle = result.getSnippet().getChannelTitle(); // Retrieve the channel title
                List<String> tags = new ArrayList<>(); // Tags can be empty in search results

                videoResults.add(new VideoResult(title, description, videoId, channelId, thumbnailUrl, channelTitle, tags));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return videoResults;
    }
}
