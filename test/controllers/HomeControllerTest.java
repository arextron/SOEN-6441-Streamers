package controllers;

import akka.actor.ActorSystem;
import akka.stream.Materializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.api.services.youtube.model.*;
import models.VideoResult;
import org.junit.Before;
import org.junit.Test;
import play.cache.SyncCacheApi;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.WebSocket;
import play.test.Helpers;
import play.test.WithApplication;
import services.YouTubeService;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static play.mvc.Http.Status.*;
import static play.test.Helpers.*;

public class HomeControllerTest extends WithApplication {

    private HomeController controller;
    private YouTubeService mockYouTubeService;
    private SyncCacheApi mockCache;
    private ActorSystem actorSystem;
    private Materializer materializer;

    @Before
    public void setUp() {
        // Set up the mocks and the controller
        mockYouTubeService = mock(YouTubeService.class);
        mockCache = mock(SyncCacheApi.class);
        actorSystem = ActorSystem.create();
        materializer = Materializer.matFromSystem(actorSystem);

        controller = new HomeController(mockYouTubeService, mockCache, actorSystem, materializer);
    }
/*
@Test
public void testIndex() {
    // Create a fake request with necessary attributes
    Map<String, String> sessionData = new HashMap<>();
    sessionData.put("sessionId", "testSessionId"); // Add any session attributes if needed

    // If the view uses flash messages
    Map<String, String> flashData = new HashMap<>();
    flashData.put("success", "Test flash message");

    Http.Request request = Helpers.fakeRequest()
            .session(sessionData)
            .flash(flashData)
            .build();

    // If the index method uses other services, ensure they are mocked and set up properly
    // For example, if index method uses data from YouTubeService:
    // when(mockYouTubeService.someMethod()).thenReturn(someData);

    Result result = controller.index(request);
    assertEquals(OK, result.status());
    String content = contentAsString(result);
    assertTrue(content.contains("TubeLytics"));
    assertTrue(content.contains("Test flash message"));
}

*/





    @Test
    public void testSearchWithValidQuery() throws Exception {
        String query = "test query";
        Map<String, String> sessionData = new HashMap<>();
        sessionData.put("sessionId", "sessionId");
        Http.RequestBuilder requestBuilder = Helpers.fakeRequest().session(sessionData);

        // Mocking YouTubeService to return sample data
        List<VideoResult> mockVideos = Arrays.asList(
                new VideoResult("Title1", "Description1", "VideoId1", "ChannelId1",
                        "ThumbnailUrl1", "ChannelTitle1", Arrays.asList("Tag1", "Tag2")),
                new VideoResult("Title2", "Description2", "VideoId2", "ChannelId2",
                        "ThumbnailUrl2", "ChannelTitle2", Arrays.asList("Tag3", "Tag4"))
        );
        when(mockYouTubeService.searchVideos(query)).thenReturn(mockVideos);

        // Mocking cache
        when(mockCache.getOptional(anyString())).thenReturn(Optional.empty());

        CompletionStage<Result> resultStage = controller.search(query, requestBuilder.build());
        Result result = resultStage.toCompletableFuture().get();

        assertEquals(OK, result.status());
        // Further assertions can be made on the content
        String content = contentAsString(result);
        assertTrue(content.contains("Title1"));
        assertTrue(content.contains("Title2"));
    }


    @Test
    public void testSearchWithException() throws Exception {
        String query = "test query";
        Map<String, String> sessionData = new HashMap<>();
        sessionData.put("sessionId", "sessionId");
        Http.RequestBuilder requestBuilder = Helpers.fakeRequest().session(sessionData);

        // Mocking YouTubeService to throw an exception
        when(mockYouTubeService.searchVideos(query)).thenThrow(new RuntimeException("Simulated Exception"));

        CompletionStage<Result> resultStage = controller.search(query, requestBuilder.build());
        Result result = resultStage.toCompletableFuture().get();

        assertEquals(INTERNAL_SERVER_ERROR, result.status());
        String content = contentAsString(result);
        assertTrue(content.contains("An error occurred while processing your search."));
    }


    @Test
    public void testShowVideoDetails() throws Exception {
        String videoId = "VideoId1";

        // Mocking YouTubeService to return a sample VideoResult
        VideoResult mockVideo = new VideoResult("Title1", "Description1", videoId, "ChannelId1",
                "ThumbnailUrl1", "ChannelTitle1", Arrays.asList("Tag1", "Tag2"));
        when(mockYouTubeService.getVideoDetails(videoId)).thenReturn(mockVideo);

        CompletionStage<Result> resultStage = controller.showVideoDetails(videoId);
        Result result = resultStage.toCompletableFuture().get();

        assertEquals(OK, result.status());
        String content = contentAsString(result);
        assertTrue(content.contains("Title1"));
        assertTrue(content.contains("Description1"));
    }

    @Test
    public void testShowVideoDetailsWithException() throws Exception {
        String videoId = "VideoId1";

        // Mocking YouTubeService to throw an exception
        when(mockYouTubeService.getVideoDetails(videoId)).thenThrow(new RuntimeException("Simulated Exception"));

        CompletionStage<Result> resultStage = controller.showVideoDetails(videoId);
        Result result = resultStage.toCompletableFuture().get();

        assertEquals(INTERNAL_SERVER_ERROR, result.status());
        String content = contentAsString(result);
        assertTrue(content.contains("Failed to fetch video details."));
    }

    @Test
    public void testViewTags() throws Exception {
        String query = "test query";

        // Mocking YouTubeService to return sample data
        List<VideoResult> mockVideos = Arrays.asList(
                new VideoResult("Title1", "Description1", "VideoId1", "ChannelId1",
                        "ThumbnailUrl1", "ChannelTitle1", Arrays.asList("Tag1", "Tag2")),
                new VideoResult("Title2", "Description2", "VideoId2", "ChannelId2",
                        "ThumbnailUrl2", "ChannelTitle2", Arrays.asList("Tag3", "Tag4"))
        );
        when(mockYouTubeService.searchVideos(query)).thenReturn(mockVideos);

        CompletionStage<Result> resultStage = controller.viewTags(query);
        Result result = resultStage.toCompletableFuture().get();

        assertEquals(OK, result.status());
        String content = contentAsString(result);
        assertTrue(content.contains("Tag1"));
        assertTrue(content.contains("Tag3"));
    }

    @Test
    public void testViewTagsWithException() throws Exception {
        String query = "test query";

        // Mocking YouTubeService to throw an exception
        when(mockYouTubeService.searchVideos(query)).thenThrow(new RuntimeException("Simulated Exception"));

        CompletionStage<Result> resultStage = controller.viewTags(query);
        Result result = resultStage.toCompletableFuture().get();

        assertEquals(INTERNAL_SERVER_ERROR, result.status());
        String content = contentAsString(result);
        assertTrue(content.contains("Failed to fetch videos for the query."));
    }

    @Test
    public void testSearchByTag() throws Exception {
        String tag = "Tag1";

        // Mocking YouTubeService to return sample data
        List<VideoResult> mockVideos = Arrays.asList(
                new VideoResult("Title1", "Description1", "VideoId1", "ChannelId1",
                        "ThumbnailUrl1", "ChannelTitle1", Arrays.asList(tag, "Tag2")),
                new VideoResult("Title2", "Description2", "VideoId2", "ChannelId2",
                        "ThumbnailUrl2", "ChannelTitle2", Arrays.asList(tag, "Tag3"))
        );
        when(mockYouTubeService.searchVideosByTag(tag)).thenReturn(mockVideos);

        CompletionStage<Result> resultStage = controller.searchByTag(tag);
        Result result = resultStage.toCompletableFuture().get();

        assertEquals(OK, result.status());
        String content = contentAsString(result);
        assertTrue(content.contains("Title1"));
        assertTrue(content.contains("Title2"));
    }

    @Test
    public void testSearchByTagWithException() throws Exception {
        String tag = "Tag1";

        // Mocking YouTubeService to throw an exception
        when(mockYouTubeService.searchVideosByTag(tag)).thenThrow(new RuntimeException("Simulated Exception"));

        CompletionStage<Result> resultStage = controller.searchByTag(tag);
        Result result = resultStage.toCompletableFuture().get();

        assertEquals(INTERNAL_SERVER_ERROR, result.status());
        String content = contentAsString(result);
        assertTrue(content.contains("Failed to fetch videos for the tag."));
    }

    @Test
    public void testWordStats() throws Exception {
        String query = "test query";

        // Mocking YouTubeService to return sample data
        List<VideoResult> mockVideos = Arrays.asList(
                new VideoResult("Title1", "Description one two three", "VideoId1",
                        "ChannelId1", "ThumbnailUrl1", "ChannelTitle1", null),
                new VideoResult("Title2", "Description two three four", "VideoId2",
                        "ChannelId2", "ThumbnailUrl2", "ChannelTitle2", null)
        );
        when(mockYouTubeService.searchVideos(query)).thenReturn(mockVideos);

        CompletionStage<Result> resultStage = controller.wordStats(query);
        Result result = resultStage.toCompletableFuture().get();

        assertEquals(OK, result.status());
        String content = contentAsString(result);
        // Check that word frequencies are displayed
        assertTrue(content.contains("one"));
        assertTrue(content.contains("two"));
        assertTrue(content.contains("three"));
        assertTrue(content.contains("four"));
    }

    @Test
    public void testWordStatsWithException() throws Exception {
        String query = "test query";

        // Mocking YouTubeService to throw an exception
        when(mockYouTubeService.searchVideos(query)).thenThrow(new RuntimeException("Simulated Exception"));

        CompletionStage<Result> resultStage = controller.wordStats(query);
        Result result = resultStage.toCompletableFuture().get();

        // Assuming your controller handles exceptions and returns an error message
        assertEquals(INTERNAL_SERVER_ERROR, result.status());
        String content = contentAsString(result);
        assertTrue(content.contains("An error occurred while processing your request."));
    }
    @Test
    public void testChannelProfile() throws Exception {
        String channelId = "ChannelId1";

        // Mocking YouTubeService to return sample data
        ChannelSnippet snippet = new ChannelSnippet();
        snippet.setTitle("ChannelTitle1");
        snippet.setDescription("ChannelDescription1");

        // Create ThumbnailDetails and set a default thumbnail
        ThumbnailDetails thumbnails = new ThumbnailDetails();
        Thumbnail defaultThumbnail = new Thumbnail();
        defaultThumbnail.setUrl("http://example.com/thumbnail.jpg");
        thumbnails.setDefault(defaultThumbnail);
        snippet.setThumbnails(thumbnails); // Provide the ThumbnailDetails object as an argument

        // Set other necessary fields in the snippet if required by your view

        // Set statistics if used in the view
        ChannelStatistics statistics = new ChannelStatistics();
        statistics.setSubscriberCount(BigInteger.valueOf(1000L));
        statistics.setVideoCount(BigInteger.valueOf(50L));
        // Set other necessary fields in statistics

        Channel mockChannel = new Channel();
        mockChannel.setId(channelId);
        mockChannel.setSnippet(snippet);
        mockChannel.setStatistics(statistics);
        // Set other necessary fields in the channel

        when(mockYouTubeService.getChannelProfile(channelId)).thenReturn(mockChannel);

        List<VideoResult> mockVideos = Arrays.asList(
                new VideoResult("Title1", "Description1", "VideoId1", channelId,
                        "ThumbnailUrl1", "ChannelTitle1", null),
                new VideoResult("Title2", "Description2", "VideoId2", channelId,
                        "ThumbnailUrl2", "ChannelTitle1", null)
        );
        when(mockYouTubeService.getLast10Videos(channelId)).thenReturn(mockVideos);

        CompletionStage<Result> resultStage = controller.channelProfile(channelId);
        Result result = resultStage.toCompletableFuture().get();

        assertEquals(OK, result.status());
        String content = contentAsString(result);
        assertTrue(content.contains("ChannelTitle1"));
        assertTrue(content.contains("Title1"));
        assertTrue(content.contains("Title2"));
    }



    @Test
    public void testChannelProfileWithException() throws Exception {
        String channelId = "ChannelId1";

        // Mocking YouTubeService to throw an exception
        when(mockYouTubeService.getChannelProfile(channelId)).thenThrow(new RuntimeException("Simulated Exception"));

        CompletionStage<Result> resultStage = controller.channelProfile(channelId);
        Result result = resultStage.toCompletableFuture().get();

        assertEquals(INTERNAL_SERVER_ERROR, result.status());
        String content = contentAsString(result);
        assertTrue(content.contains("Error fetching channel profile: Simulated Exception"));
    }

    @Test
    public void testSearchWebSocket() {
        // Testing WebSocket connection
        // Note: Testing WebSockets can be complex; this is a simplified example
        WebSocket webSocket = controller.searchWebSocket();
        assertNotNull(webSocket);
        // Further testing would involve setting up a WebSocket client and server
    }
}
