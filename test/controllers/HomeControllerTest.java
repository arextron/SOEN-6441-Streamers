package controllers;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.stream.Materializer;
import akka.testkit.javadsl.TestKit;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.ChannelSnippet;
import models.VideoResult;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import play.cache.SyncCacheApi;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.WebSocket;
import play.test.WithApplication;
import scala.concurrent.duration.Duration;
import services.YouTubeService;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static play.mvc.Http.Status.*;
import static play.test.Helpers.*;

public class HomeControllerTest extends WithApplication {

    private HomeController controller;
    private YouTubeService mockYouTubeService;
    private SyncCacheApi mockCache;
    private ActorSystem actorSystem;
    private Materializer materializer;
    private HttpExecutionContext ec;

    @BeforeClass
    public static void setupClass() {
        // Initialize resources needed before any tests are run
    }

    @AfterClass
    public static void tearDownClass() {
        // Clean up resources after all tests are completed
    }

    @Before
    public void setUp() {
        // Set up the mocks and the controller
        mockYouTubeService = mock(YouTubeService.class);
        mockCache = mock(SyncCacheApi.class);
        actorSystem = ActorSystem.create();
        materializer = Materializer.matFromSystem(actorSystem);
        ec = new HttpExecutionContext(actorSystem.dispatcher());

        controller = new HomeController(mockYouTubeService, mockCache, actorSystem, materializer);
    }
/*
    @Test
    public void testIndex() {
        Http.Request request = mock(Http.Request.class);
        when(request.session()).thenReturn(new Http.Session(new HashMap<>()));

        Result result = controller.index(request);
        assertEquals(OK, result.status());
        // Further assertions can be made on the content
        assertTrue(contentAsString(result).contains("TubeLytics"));
    }
*/

    @Test
    public void testSearchWithEmptyQuery() throws Exception {
        Http.Request request = mock(Http.Request.class);
        when(request.session()).thenReturn(new Http.Session(new HashMap<>()));

        CompletionStage<Result> resultStage = controller.search("", request);
        Result result = resultStage.toCompletableFuture().get();

        assertEquals(OK, result.status());
        assertEquals("Please provide a search query.", contentAsString(result));
    }

    @Test
    public void testSearchWithValidQuery() throws Exception {
        String query = "test query";
        Http.Request request = mock(Http.Request.class);
        when(request.session()).thenReturn(new Http.Session(new HashMap<>()));

        // Mocking YouTubeService to return sample data
        List<VideoResult> mockVideos = Arrays.asList(
                new VideoResult("Title1", "Description1", "VideoId1", "ChannelId1", "ThumbnailUrl1", "ChannelTitle1", Arrays.asList("Tag1", "Tag2")),
                new VideoResult("Title2", "Description2", "VideoId2", "ChannelId2", "ThumbnailUrl2", "ChannelTitle2", Arrays.asList("Tag3", "Tag4"))
        );
        when(mockYouTubeService.searchVideos(query)).thenReturn(mockVideos);

        // Mocking cache
        when(mockCache.getOptional(anyString())).thenReturn(Optional.empty());

        CompletionStage<Result> resultStage = controller.search(query, request);
        Result result = resultStage.toCompletableFuture().get();

        assertEquals(OK, result.status());
        // Further assertions can be made on the content
        String content = contentAsString(result);
        assertTrue(content.contains("Title1"));
        assertTrue(content.contains("Title2"));
    }

    @Test
    public void testShowVideoDetails() throws Exception {
        String videoId = "VideoId1";

        // Mocking YouTubeService to return a sample VideoResult
        VideoResult mockVideo = new VideoResult("Title1", "Description1", videoId, "ChannelId1", "ThumbnailUrl1", "ChannelTitle1", Arrays.asList("Tag1", "Tag2"));
        when(mockYouTubeService.getVideoDetails(videoId)).thenReturn(mockVideo);

        CompletionStage<Result> resultStage = controller.showVideoDetails(videoId);
        Result result = resultStage.toCompletableFuture().get();

        assertEquals(OK, result.status());
        String content = contentAsString(result);
        assertTrue(content.contains("Title1"));
        assertTrue(content.contains("Description1"));
    }

    @Test
    public void testViewTags() throws Exception {
        String query = "test query";

        // Mocking YouTubeService to return sample data
        List<VideoResult> mockVideos = Arrays.asList(
                new VideoResult("Title1", "Description1", "VideoId1", "ChannelId1", "ThumbnailUrl1", "ChannelTitle1", Arrays.asList("Tag1", "Tag2")),
                new VideoResult("Title2", "Description2", "VideoId2", "ChannelId2", "ThumbnailUrl2", "ChannelTitle2", Arrays.asList("Tag3", "Tag4"))
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
    public void testSearchByTag() throws Exception {
        String tag = "Tag1";

        // Mocking YouTubeService to return sample data
        List<VideoResult> mockVideos = Arrays.asList(
                new VideoResult("Title1", "Description1", "VideoId1", "ChannelId1", "ThumbnailUrl1", "ChannelTitle1", Arrays.asList(tag, "Tag2")),
                new VideoResult("Title2", "Description2", "VideoId2", "ChannelId2", "ThumbnailUrl2", "ChannelTitle2", Arrays.asList(tag, "Tag3"))
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
    public void testWordStats() throws Exception {
        String query = "test query";

        // Mocking YouTubeService to return sample data
        List<VideoResult> mockVideos = Arrays.asList(
                new VideoResult("Title1", "Description one two three", "VideoId1", "ChannelId1", "ThumbnailUrl1", "ChannelTitle1", null),
                new VideoResult("Title2", "Description two three four", "VideoId2", "ChannelId2", "ThumbnailUrl2", "ChannelTitle2", null)
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
    /*
    @Test
    public void testChannelProfile() throws Exception {
        String channelId = "ChannelId1";

        // Mocking YouTubeService to return sample data
        ChannelSnippet snippet = new ChannelSnippet();
        snippet.setTitle("ChannelTitle1");
        Channel mockChannel = new Channel();
        mockChannel.setId(channelId);
        mockChannel.setSnippet(snippet);
        when(mockYouTubeService.getChannelProfile(channelId)).thenReturn(mockChannel);

        List<VideoResult> mockVideos = Arrays.asList(
                new VideoResult("Title1", "Description1", "VideoId1", channelId, "ThumbnailUrl1", "ChannelTitle1", null),
                new VideoResult("Title2", "Description2", "VideoId2", channelId, "ThumbnailUrl2", "ChannelTitle1", null)
        );
        when(mockYouTubeService.getLast10Videos(channelId)).thenReturn(mockVideos);

        // Ensure that no exceptions are thrown during CompletableFuture execution
        CompletionStage<Result> resultStage = controller.channelProfile(channelId);
        Result result = resultStage.toCompletableFuture().get();

        assertEquals(OK, result.status());
        String content = contentAsString(result);
        assertTrue(content.contains("ChannelTitle1"));
        assertTrue(content.contains("Title1"));
        assertTrue(content.contains("Title2"));
    }
     */
    @Test
    public void testSearchWebSocket() {
        // Testing WebSocket connection
        // Note: Testing WebSockets can be complex; this is a simplified example
        WebSocket webSocket = controller.searchWebSocket();
        assertNotNull(webSocket);
        // Further testing would involve setting up a WebSocket client and server
    }
}
