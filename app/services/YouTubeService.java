package services;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.*;
import models.VideoResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for interacting with the YouTube API.
 */
public class YouTubeService {
    private static final String API_KEY = "AIzaSyCZaFSQMkl2nuD8tuCo43hPvoMua2e3VEY"; // Replace with your actual API key
    private static final String APPLICATION_NAME = "TubeLytics";
    private static final long MAX_RESULTS = 10;
    private final YouTube youtube;

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
                    return getVideoDetails(videoId);
                })
                .collect(Collectors.toList());
    }

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

    public VideoResult getVideoDetails(String videoId) {
        try {
            YouTube.Videos.List request = youtube.videos().list("snippet");
            request.setId(videoId);
            request.setKey(API_KEY);

            VideoListResponse response = request.execute();

            // Process the video details reactively
            return response.getItems().stream()
                    .findFirst()
                    .map(video -> new VideoResult(
                            video.getSnippet().getTitle(),
                            video.getSnippet().getDescription(),
                            videoId,
                            video.getSnippet().getChannelId(),
                            video.getSnippet().getThumbnails().getDefault().getUrl(),
                            video.getSnippet().getChannelTitle(),
                            video.getSnippet().getTags() != null ? video.getSnippet().getTags() : new ArrayList<>()
                    ))
                    .orElse(null); // Return null if no video is found
        } catch (IOException e) {
            e.printStackTrace();
            return null; // Handle exception
        }
    }


    public List<VideoResult> searchVideosByTag(String tag) {
        try {
            YouTube.Search.List search = youtube.search().list("snippet");
            search.setQ(tag);
            search.setType("video");
            search.setMaxResults(10L);
            search.setKey(API_KEY);

            SearchListResponse response = search.execute();
            List<SearchResult> results = response.getItems();

            // Stream through search results and map to VideoResult
            return results.stream()
                    .map(result -> getVideoDetails(result.getId().getVideoId()))
                    .filter(videoResult -> videoResult != null) // Filter out null values
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>(); // Return an empty list on error
        }
    }
}