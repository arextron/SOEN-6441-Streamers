package controllers;

import actors.TagsActor;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.stream.Materializer;
import akka.testkit.javadsl.TestKit;
import models.VideoResult;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import play.cache.SyncCacheApi;
import play.i18n.MessagesApi;
import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;
import play.test.WithApplication;
import services.YouTubeService;
import play.libs.Json;


import java.io.IOException;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.CompletionStage;
import static play.test.Helpers.*;


import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static play.mvc.Http.Status.*;
import static play.test.Helpers.*;

/**
 * Test class for HomeController.
 */
public class HomeControllerTest extends WithApplication {

    @Mock
    private YouTubeService youTubeService;

    @Mock
    private SyncCacheApi cache;

    private Materializer materializer;

    private static ActorSystem system;

    private HomeController homeController;

    private TestKit tagsActorProbe;

    private MessagesApi messagesApi;

    @BeforeClass
    public static void setupClass() {
        system = ActorSystem.create("TestSystem");
    }

    @AfterClass
    public static void teardownClass() {
        TestKit.shutdownActorSystem(system);
        system = null;
    }

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        tagsActorProbe = new TestKit(system);
        materializer = app.injector().instanceOf(Materializer.class);
        messagesApi = app.injector().instanceOf(MessagesApi.class);
        homeController = new HomeController(youTubeService, cache, system, materializer, tagsActorProbe.getRef(), messagesApi);
    }

    @Test
    public void testIndex() {
        Http.RequestBuilder requestBuilder = Helpers.fakeRequest();
        Http.Request request = requestBuilder.build();

        Result result = homeController.index(request);
        assertEquals(OK, result.status());
        String content = contentAsString(result);
        assertTrue(content.contains("Welcome to YT Lytics"));
    }

    @Test
    public void testSearch_NullQuery() throws Exception {
        Http.Request request = fakeRequest().build();
        CompletionStage<Result> resultStage = homeController.search(null, request);
        Result result = resultStage.toCompletableFuture().get();

        assertEquals(OK, result.status());
        assertEquals("Please provide a search query.", contentAsString(result));
    }

    @Test
    public void testSearch_EmptyQuery() throws Exception {
        Http.Request request = fakeRequest().build();
        CompletionStage<Result> resultStage = homeController.search("", request);
        Result result = resultStage.toCompletableFuture().get();

        assertEquals(OK, result.status());
        assertEquals("Please provide a search query.", contentAsString(result));
    }

    @Test
    public void testSearch_QueryInCache() throws Exception {
        Http.Request request = fakeRequest().session("sessionId", "testSessionId").build();

        VideoResult videoResult = new VideoResult(
                "Test Title",
                "Test Description",
                "videoId123",
                "channelId123",
                "http://thumbnail.url",
                "Channel Title",
                Arrays.asList("tag1", "tag2")
        );

        List<VideoResult> videoResults = Arrays.asList(videoResult);

        // Access the private videoCache field
        homeController.videoCache.put("test query", videoResults);

        when(cache.getOptional("searchHistory_testSessionId")).thenReturn(Optional.of(new LinkedList<>()));

        CompletionStage<Result> resultStage = homeController.search("test query", request);
        Result result = resultStage.toCompletableFuture().get();

        assertEquals(OK, result.status());
        String content = contentAsString(result);
        assertTrue(content.contains("Test Title"));
    }

    @Test
    public void testSearch_QueryNotInCache() throws Exception {
        Http.Request request = fakeRequest().session("sessionId", "testSessionId").build();

        VideoResult videoResult = new VideoResult(
                "Test Title",
                "Test Description",
                "videoId123",
                "channelId123",
                "http://thumbnail.url",
                "Channel Title",
                Arrays.asList("tag1", "tag2")
        );

        List<VideoResult> videoResults = Arrays.asList(videoResult);

        when(youTubeService.searchVideos("test query")).thenReturn(videoResults);
        when(cache.getOptional("searchHistory_testSessionId")).thenReturn(Optional.of(new LinkedList<>()));

        CompletionStage<Result> resultStage = homeController.search("test query", request);
        Result result = resultStage.toCompletableFuture().get();

        assertEquals(OK, result.status());
        String content = contentAsString(result);
        assertTrue(content.contains("Test Title"));
    }

    @Test
    public void testSearch_Exception() throws Exception {
        Http.Request request = fakeRequest().session("sessionId", "testSessionId").build();

        when(youTubeService.searchVideos("test query")).thenThrow(new RuntimeException("Simulated exception"));

        CompletionStage<Result> resultStage = homeController.search("test query", request);
        try {
            resultStage.toCompletableFuture().get();
            fail("Expected exception not thrown");
        } catch (Exception e) {
            // Expected exception
            assertTrue(e.getCause().getMessage().contains("Simulated exception"));
        }
    }

    @Test
    public void testShowVideoDetails_Success() throws Exception {
        String videoId = "testVideoId";
        VideoResult videoResult = new VideoResult("Title", "Description", videoId, "channelId", "thumbUrl", "Channel", Arrays.asList("tag1", "tag2"));

        // Start the test
        CompletionStage<Result> resultStage = homeController.showVideoDetails(videoId);

        // The tagsActor should have received a ViewVideoDetails message
        TagsActor.ViewVideoDetails message = tagsActorProbe.expectMsgClass(TagsActor.ViewVideoDetails.class);
        assertEquals(videoId, message.videoId);

        // Have the probe respond with the videoResult
        tagsActorProbe.reply(videoResult);

        // Get the result
        Result result = resultStage.toCompletableFuture().get();

        assertEquals(OK, result.status());
        String content = contentAsString(result);
        assertTrue(content.contains("Title"));
        assertTrue(content.contains("Description"));
    }

    @Test
    public void testShowVideoDetails_ErrorResponse() throws Exception {
        String videoId = "testVideoId";

        // Start the test
        CompletionStage<Result> resultStage = homeController.showVideoDetails(videoId);

        // The tagsActor should have received a ViewVideoDetails message
        TagsActor.ViewVideoDetails message = tagsActorProbe.expectMsgClass(TagsActor.ViewVideoDetails.class);
        assertEquals(videoId, message.videoId);

        // Have the probe respond with an ErrorMessage
        TagsActor.ErrorMessage errorMessage = new TagsActor.ErrorMessage("Video not found");
        tagsActorProbe.reply(errorMessage);

        // Get the result
        Result result = resultStage.toCompletableFuture().get();

        assertEquals(INTERNAL_SERVER_ERROR, result.status());
        String content = contentAsString(result);
        assertEquals("Failed to fetch video details.", content);
    }

    @Test
    public void testViewTags_Success() throws Exception {
        String query = "test query";
        List<VideoResult> videoResults = Arrays.asList(
                new VideoResult("Title1", "Description1", "videoId1", "channelId1", "thumbUrl1", "Channel1", Arrays.asList("tag1", "tag2")),
                new VideoResult("Title2", "Description2", "videoId2", "channelId2", "thumbUrl2", "Channel2", Arrays.asList("tag3", "tag4"))
        );

        // Start the test
        CompletionStage<Result> resultStage = homeController.viewTags(query);

        // The tagsActor should have received a ViewTags message
        TagsActor.ViewTags message = tagsActorProbe.expectMsgClass(TagsActor.ViewTags.class);
        assertEquals(query, message.query);

        // Have the probe respond with the videoResults
        tagsActorProbe.reply(videoResults);

        // Get the result
        Result result = resultStage.toCompletableFuture().get();

        assertEquals(OK, result.status());
        String content = contentAsString(result);
        assertTrue(content.contains("Title1"));
        assertTrue(content.contains("Title2"));
    }

    @Test
    public void testViewTags_ErrorResponse() throws Exception {
        String query = "test query";

        // Start the test
        CompletionStage<Result> resultStage = homeController.viewTags(query);

        // The tagsActor should have received a ViewTags message
        TagsActor.ViewTags message = tagsActorProbe.expectMsgClass(TagsActor.ViewTags.class);
        assertEquals(query, message.query);

        // Have the probe respond with an ErrorMessage
        TagsActor.ErrorMessage errorMessage = new TagsActor.ErrorMessage("Error fetching videos");
        tagsActorProbe.reply(errorMessage);

        // Get the result
        Result result = resultStage.toCompletableFuture().get();

        assertEquals(INTERNAL_SERVER_ERROR, result.status());
        String content = contentAsString(result);
        assertEquals("Failed to fetch videos for the query.", content);
    }

    @Test
    public void testSearchByTag_Success() throws Exception {
        String tag = "testTag";
        List<VideoResult> videoResults = Arrays.asList(
                new VideoResult("Title1", "Description1", "videoId1", "channelId1", "thumbUrl1", "Channel1", Arrays.asList("tag1", "tag2")),
                new VideoResult("Title2", "Description2", "videoId2", "channelId2", "thumbUrl2", "Channel2", Arrays.asList("tag3", "tag4"))
        );

        // Start the test
        CompletionStage<Result> resultStage = homeController.searchByTag(tag);

        // The tagsActor should have received a SearchByTag message
        TagsActor.SearchByTag message = tagsActorProbe.expectMsgClass(TagsActor.SearchByTag.class);
        assertEquals(tag, message.tag);

        // Have the probe respond with the videoResults
        tagsActorProbe.reply(videoResults);

        // Get the result
        Result result = resultStage.toCompletableFuture().get();

        assertEquals(OK, result.status());

        // Parse the JSON response
        String content = contentAsString(result);
        assertTrue(content.contains("Title1"));
        assertTrue(content.contains("Title2"));
    }

    @Test
    public void testSearchByTag_NoResultsFound() throws Exception {
        String tag = "testTag";

        // Start the test
        CompletionStage<Result> resultStage = homeController.searchByTag(tag);

        // The tagsActor should have received a SearchByTag message
        TagsActor.SearchByTag message = tagsActorProbe.expectMsgClass(TagsActor.SearchByTag.class);
        assertEquals(tag, message.tag);

        // Respond with an empty list
        tagsActorProbe.reply(new ArrayList<VideoResult>());

        // Get the result
        Result result = resultStage.toCompletableFuture().get();

        assertEquals(NOT_FOUND, result.status());
        String content = contentAsString(result);
        assertEquals("No results found for the specified tag.", content);
    }

    @Test
    public void testSearchByTag_ErrorResponse() throws Exception {
        String tag = "testTag";

        // Start the test
        CompletionStage<Result> resultStage = homeController.searchByTag(tag);

        // The tagsActor should have received a SearchByTag message
        TagsActor.SearchByTag message = tagsActorProbe.expectMsgClass(TagsActor.SearchByTag.class);
        assertEquals(tag, message.tag);

        // Respond with ErrorMessage
        TagsActor.ErrorMessage errorMessage = new TagsActor.ErrorMessage("Error fetching videos by tag");
        tagsActorProbe.reply(errorMessage);

        // Get the result
        Result result = resultStage.toCompletableFuture().get();

        assertEquals(INTERNAL_SERVER_ERROR, result.status());
        String content = contentAsString(result);
        assertEquals("Error fetching videos by tag", content);
    }

    @Test
    public void testWordStats_Success() throws Exception {
        String query = "test query";
        Map<String, Long> wordFrequency = new LinkedHashMap<>();
        wordFrequency.put("hello", 2L);
        wordFrequency.put("world", 1L);

        // Create a TestProbe to act as the wordStatsActor
        TestKit wordStatsActorProbe = new TestKit(system);

        // Mock the actorSystem to return our TestProbe when actorOf is called
        ActorSystem mockActorSystem = spy(system);
        doReturn(wordStatsActorProbe.getRef()).when(mockActorSystem).actorOf(any(Props.class));

        // Create a new HomeController with the mocked actorSystem
        HomeController homeControllerWithMockedActorSystem = new HomeController(youTubeService, cache, mockActorSystem, materializer, tagsActorProbe.getRef(), messagesApi);

        // Start the test
        CompletionStage<Result> resultStage = homeControllerWithMockedActorSystem.wordStats(query);

        // The wordStatsActor should have received the query
        String receivedQuery = wordStatsActorProbe.expectMsgClass(String.class);
        assertEquals(query, receivedQuery);

        // Have the probe respond with a JSON string
        String jsonResponse = Json.toJson(wordFrequency).toString();
        wordStatsActorProbe.reply(jsonResponse);

        // Get the result
        Result result = resultStage.toCompletableFuture().get();

        assertEquals(OK, result.status());
        String content = contentAsString(result);
        assertTrue(content.contains("hello"));
        assertTrue(content.contains("2"));
        assertTrue(content.contains("world"));
        assertTrue(content.contains("1"));
    }

    // ... Additional tests for wordStats (ErrorResponse, MessageResponse, UnexpectedResponse) can be included similarly

    @Test
    public void testChannelProfile_Success() throws Exception {
        String channelId = "testChannelId";
        com.google.api.services.youtube.model.Channel channel = new com.google.api.services.youtube.model.Channel();
        channel.setId(channelId);
        com.google.api.services.youtube.model.ChannelSnippet snippet = new com.google.api.services.youtube.model.ChannelSnippet();
        snippet.setTitle("Test Channel");
        snippet.setDescription("Test Description");
        snippet.setPublishedAt(new com.google.api.client.util.DateTime("2021-01-01T00:00:00Z"));
        // Set thumbnails
        com.google.api.services.youtube.model.ThumbnailDetails thumbnails = new com.google.api.services.youtube.model.ThumbnailDetails();
        com.google.api.services.youtube.model.Thumbnail defaultThumbnail = new com.google.api.services.youtube.model.Thumbnail();
        defaultThumbnail.setUrl("http://example.com/thumbnail.jpg");
        thumbnails.setDefault(defaultThumbnail);
        snippet.setThumbnails(thumbnails);
        channel.setSnippet(snippet);

        // Set statistics
        com.google.api.services.youtube.model.ChannelStatistics statistics = new com.google.api.services.youtube.model.ChannelStatistics();
        statistics.setSubscriberCount(BigInteger.valueOf(1000L));
        statistics.setViewCount(BigInteger.valueOf(50000L));
        statistics.setVideoCount(BigInteger.valueOf(100L));
        channel.setStatistics(statistics);

        // Create VideoResult with public getters
        VideoResult video = new VideoResult("Video1", "Description1", "videoId1", channelId, "http://example.com/video-thumbnail.jpg", "Channel1", Arrays.asList("tag1", "tag2"));

        List<VideoResult> videos = Arrays.asList(video);

        when(youTubeService.getChannelProfile(channelId)).thenReturn(channel);
        when(youTubeService.getLast10Videos(channelId)).thenReturn(videos);

        CompletionStage<Result> resultStage = homeController.channelProfile(channelId);
        Result result = resultStage.toCompletableFuture().get();

        assertEquals(OK, result.status());
        String content = contentAsString(result);
        assertTrue(content.contains("Test Channel"));
        assertTrue(content.contains("Video1"));
    }

    @Test
    public void testChannelProfile_Exception() throws Exception {
        String channelId = "testChannelId";

        when(youTubeService.getChannelProfile(channelId)).thenThrow(new IOException("Simulated exception"));

        CompletionStage<Result> resultStage = homeController.channelProfile(channelId);
        Result result = resultStage.toCompletableFuture().get();

        assertEquals(INTERNAL_SERVER_ERROR, result.status());
        String content = contentAsString(result);
        assertTrue(content.contains("Error fetching channel profile: Simulated exception"));
    }

    @Test
    public void testSearchWebSocket() {
        assertNotNull(homeController.searchWebSocket());
    }
}
