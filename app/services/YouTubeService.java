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
 * Provides methods for fetching video details, channel profiles, and search results.
 */
public class YouTubeService {
<<<<<<< HEAD
    private static final String API_KEY = "AIzaSyBTIpHIbq_TpejUrJI55qM8qKasiLJYRxo"; // Replace with your actual API key
=======

    private static final String API_KEY = "AIzaSyAe7aBw3usdV4b3GaCvO23BBkXog7ro-aU"; // Replace with your actual API key
>>>>>>> 157217f3cd104039914b96a1dc94064e04f3b3ea
    private static final String APPLICATION_NAME = "TubeLytics";
    private static final long MAX_RESULTS = 10;
    private final YouTube youtube;

    /**
     * Initializes the YouTube API client.
     * Configures the YouTube client with default settings and application name.
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
     * Fetches the last 10 videos of a given channel.
     *
     * @param channelId The ID of the YouTube channel.
     * @return A list of VideoResult representing the last 10 videos.
     * @throws IOException if an error occurs during API communication.
     */
    public List<VideoResult> getLast10Videos(String channelId) throws IOException {
        YouTube.Search.List search = youtube.search().list("id,snippet");
        search.setChannelId(channelId);
        search.setType("video");
        search.setMaxResults(MAX_RESULTS);
        search.setOrder("date");
        search.setKey(API_KEY);

        SearchListResponse response = search.execute();
        List<SearchResult> searchResults = response.getItems();

        return searchResults.stream()
                .map(result -> new VideoResult(
                        result.getSnippet().getTitle(),
                        result.getSnippet().getDescription(),
                        result.getId().getVideoId(),
                        channelId,
                        result.getSnippet().getThumbnails().getDefault().getUrl(),
                        result.getSnippet().getChannelTitle(),
                        null // Tags are not fetched in this API call; pass null or an empty list
                ))
                .collect(Collectors.toList());
    }

    /**
     * Fetches the profile of a given channel.
     *
     * @param channelId The ID of the channel to fetch.
     * @return The Channel object containing profile details.
     * @throws IOException if an error occurs during API communication.
     */
    public Channel getChannelProfile(String channelId) throws IOException {
        YouTube.Channels.List request = youtube.channels().list("snippet,statistics");
        request.setId(channelId);
        request.setKey(API_KEY);

        ChannelListResponse response = request.execute();
        List<Channel> channels = response.getItems();

        if (channels.isEmpty()) {
            throw new IOException("No channel found with ID: " + channelId);
        }

        return channels.get(0); // Return the first channel (should only be one for the given ID)
    }

    /**
     * Fetches the latest videos of a given channel, limited by the specified count.
     *
     * @param channelId The ID of the channel.
     * @param limit The maximum number of videos to fetch.
     * @return A list of VideoResult for the latest videos.
     * @throws IOException if an error occurs during API communication.
     */
    public List<VideoResult> getLatestVideosByChannel(String channelId, int limit) throws IOException {
        YouTube.Search.List request = youtube.search().list("snippet");
        request.setChannelId(channelId);
        request.setMaxResults((long) limit);
        request.setOrder("date");
        request.setKey(API_KEY);

        List<SearchResult> searchResults = request.execute().getItems();

        return searchResults.stream()
                .map(result -> getVideoDetails(result.getId().getVideoId()))
                .collect(Collectors.toList());
    }

    /**
     * Searches for videos based on a query string.
     *
     * @param query The search query string.
     * @return A list of VideoResult matching the search query.
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

    /**
     * Fetches detailed information for a specific video.
     *
     * @param videoId The ID of the video.
     * @return A VideoResult containing detailed information about the video, or null if not found.
     */
    public VideoResult getVideoDetails(String videoId) {
        try {
            YouTube.Videos.List request = youtube.videos().list("snippet");
            request.setId(videoId);
            request.setKey(API_KEY);

            VideoListResponse response = request.execute();

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

    /**
     * Searches for videos based on a specific tag.
     *
     * @param tag The tag to search for.
     * @return A list of VideoResult associated with the specified tag.
     */
    public List<VideoResult> searchVideosByTag(String tag) {
        try {
            YouTube.Search.List search = youtube.search().list("snippet");
            search.setQ(tag);
            search.setType("video");
            search.setMaxResults(10L);
            search.setKey(API_KEY);

            SearchListResponse response = search.execute();
            List<SearchResult> results = response.getItems();

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
