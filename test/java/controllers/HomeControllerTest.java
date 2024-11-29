//We certify that this submission is the original work of the members of the group and meets the Faculty's Expectations of Originality.
//Signed by- Aryan Awasthi, Harsukhvir Singh Grewal, Sharun Basnet
// 40278847, 40310953, 40272435
package controllers;
import models.VideoResult;
import services.YouTubeService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import play.cache.SyncCacheApi;
import play.mvc.Http;
import play.mvc.Result;

import java.math.BigInteger;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.CompletionStage;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static play.mvc.Http.Status.*;
import static play.test.Helpers.*;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.ChannelSnippet;
import com.google.api.services.youtube.model.ChannelStatistics;

@RunWith(MockitoJUnitRunner.class)

/**
 * This class represents HomeControllerTest.
 */
public class HomeControllerTest {

    @Mock
    private YouTubeService youTubeService;

    @Mock
    private SyncCacheApi cache;

    @InjectMocks
    private HomeController homeController;

    @Before

/**
 * This method represents setUp.
 *
 * @return [Description of return value]
 */
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        // Initialize the controller with mocked dependencies
        homeController = new HomeController(youTubeService, cache);
    }

    @Test
/**
 * This method represents testIndex.
 *
 * @return [Description of return value]
 */
    public void testIndex() {
        Http.Request request = fakeRequest().build();
        Result result = homeController.index(request);
        assertEquals(OK, result.status());
        String content = contentAsString(result);
        assertTrue(content.contains("Welcome to TubeLytics"));
    }

    @Test

/**
 * This method represents testSearch_NullQuery.
 *
 * @return [Description of return value]
 */
    public void testSearch_NullQuery() {
        Http.Request request = fakeRequest().build();

        CompletionStage<Result> resultStage = homeController.search(null, request);
        Result result = resultStage.toCompletableFuture().join();

        assertEquals(OK, result.status());
        String content = contentAsString(result);
        assertEquals("Please provide a search query.", content);
    }

    @Test

/**
 * This method represents testSearch_EmptyQuery.
 *
 * @return [Description of return value]
 */
    public void testSearch_EmptyQuery() {
        Http.Request request = fakeRequest().build();

        CompletionStage<Result> resultStage = homeController.search("", request);
        Result result = resultStage.toCompletableFuture().join();

        assertEquals(OK, result.status());
        String content = contentAsString(result);
        assertEquals("Please provide a search query.", content);
    }

    @Test

/**
 * This method represents testSearch_ValidQuery_NotInCache.
 *
 * @return [Description of return value]
 */
    public void testSearch_ValidQuery_NotInCache() throws Exception {
        Http.Request request = fakeRequest().build();

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

        CompletionStage<Result> resultStage = homeController.search("test query", request);
        Result result = resultStage.toCompletableFuture().join();
        assertEquals(OK, result.status());
        String content = contentAsString(result);
        assertTrue(content.contains("Test Title"));
        assertTrue(content.contains("Displaying results for \"test query\""));
    }
    @Test

/**
 * This method represents testSearch_ValidQuery_InCache.
 *
 * @return [Description of return value]
 */
    public void testSearch_ValidQuery_InCache() throws Exception {
        Http.Request request = fakeRequest().build();

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
        Field videoCacheField = HomeController.class.getDeclaredField("videoCache");
        videoCacheField.setAccessible(true);
        Map<String, List<VideoResult>> videoCache = (Map<String, List<VideoResult>>) videoCacheField.get(homeController);
        videoCache.put("test query", videoResults);

        CompletionStage<Result> resultStage = homeController.search("test query", request);
        Result result = resultStage.toCompletableFuture().join();
        assertEquals(OK, result.status());
        String content = contentAsString(result);
        assertTrue(content.contains("Test Title"));
        assertTrue(content.contains("Displaying results for \"test query\""));
    }

    @Test

/**
 * This method represents testShowVideoDetails.
 *
 * @return [Description of return value]
 */
    public void testShowVideoDetails() {

        String videoId = "videoId123";
        VideoResult videoResult = new VideoResult(
                "Test Title",
                "Test Description",
                videoId,
                "channelId123",
                "http://thumbnail.url",
                "Channel Title",
                Arrays.asList("tag1", "tag2")
        );

        when(youTubeService.getVideoDetails(videoId)).thenReturn(videoResult);
        CompletionStage<Result> resultStage = homeController.showVideoDetails(videoId);

        Result result = resultStage.toCompletableFuture().join();

        assertEquals(OK, result.status());

        String content = contentAsString(result);

        assertTrue(content.contains("Test Title"));
        assertTrue(content.contains("Channel Title"));
        assertTrue(content.contains("tag1"));
        assertTrue(content.contains("tag2"));
    }

    @Test

/**
 * This method represents testSearchByTag.
 *
 * @return [Description of return value]
 */
    public void testSearchByTag() {

        String tag = "testTag";
        VideoResult videoResult = new VideoResult(
                "Test Title",
                "Test Description",
                "videoId123",
                "channelId123",
                "http://thumbnail.url",
                "Channel Title",
                Arrays.asList(tag)
        );

        List<VideoResult> videoResults = Arrays.asList(videoResult);

        when(youTubeService.searchVideosByTag(tag)).thenReturn(videoResults);
        CompletionStage<Result> resultStage = homeController.searchByTag(tag);
        Result result = resultStage.toCompletableFuture().join();
        assertEquals(OK, result.status());
        String content = contentAsString(result);
        assertTrue(content.contains("Search Results for Tag: \"testTag\""));
        assertTrue(content.contains("Test Title"));
    }
    @Test

/**
 * This method represents testWordStats_NullQuery.
 *
 * @return [Description of return value]
 */
    public void testWordStats_NullQuery() {
        CompletionStage<Result> resultStage = homeController.wordStats(null);
        Result result = resultStage.toCompletableFuture().join();
        assertEquals(BAD_REQUEST, result.status());
        String content = contentAsString(result);
        assertEquals("Please provide a search query.", content);
    }

    @Test

/**
 * This method represents testWordStats_EmptyQuery.
 *
 * @return [Description of return value]
 */
    public void testWordStats_EmptyQuery() {

        CompletionStage<Result> resultStage = homeController.wordStats("   ");
        Result result = resultStage.toCompletableFuture().join();
        assertEquals(BAD_REQUEST, result.status());
        String content = contentAsString(result);
        assertEquals("Please provide a search query.", content);
    }

    @Test

/**
 * This method represents testWordStats_ValidQuery.
 *
 * @return [Description of return value]
 */
    public void testWordStats_ValidQuery() {

        String query = "test query";

        VideoResult video1 = new VideoResult(
                "Test Title 1",
                "hello world",
                "videoId1",
                "channelId1",
                "http://thumbnail1.url",
                "Channel Title 1",
                Arrays.asList("tag1")
        );

        VideoResult video2 = new VideoResult(
                "Test Title 2",
                "hello again",
                "videoId2",
                "channelId2",
                "http://thumbnail2.url",
                "Channel Title 2",
                Arrays.asList("tag2")
        );

        List<VideoResult> videoResults = Arrays.asList(video1, video2);
        when(youTubeService.searchVideos(query)).thenReturn(videoResults);
        CompletionStage<Result> resultStage = homeController.wordStats(query);
        Result result = resultStage.toCompletableFuture().join();
        assertEquals(OK, result.status());
        String content = contentAsString(result);

        assertTrue(content.contains("hello"));
        assertTrue(content.contains("2")); // "hello" appears twice
        assertTrue(content.contains("world"));
        assertTrue(content.contains("1")); // "world" appears once
        assertTrue(content.contains("again"));
    }

    @Test

/**
 * This method represents testWordStats_NoVideosFound.
 *
 * @return [Description of return value]
 */
    public void testWordStats_NoVideosFound() {
        String query = "test query";
        when(youTubeService.searchVideos(query)).thenReturn(Collections.emptyList());
        CompletionStage<Result> resultStage = homeController.wordStats(query);
        Result result = resultStage.toCompletableFuture().join();
        assertEquals(OK, result.status());
        String content = contentAsString(result);
        assertEquals("No word frequency data available for \"test query\".", content);
    }

    @Test

/**
 * This method represents testWordStats_Exception.
 *
 * @return [Description of return value]
 */
    public void testWordStats_Exception() {
        String query = "test query";
        when(youTubeService.searchVideos(query)).thenThrow(new RuntimeException("Simulated exception"));
        CompletionStage<Result> resultStage = homeController.wordStats(query);
        Result result = resultStage.toCompletableFuture().join();
        assertEquals(INTERNAL_SERVER_ERROR, result.status());
        String content = contentAsString(result);
        assertEquals("An error occurred while processing your request.", content);
    }

    @Test

/**
 * This method represents testChannelProfile.
 *
 * @return [Description of return value]
 */
    public void testChannelProfile() throws IOException {
        String channelId = "channelId123";

        // Create a mocked Channel object
        Channel channel = new Channel();

        // Set up the snippet
        ChannelSnippet snippet = new ChannelSnippet();
        snippet.setTitle("Test Channel");
        snippet.setDescription("Test Channel Description");
        channel.setSnippet(snippet);

        // Set up the statistics
        ChannelStatistics statistics = new ChannelStatistics();
        statistics.setSubscriberCount(BigInteger.valueOf(1000L));
        statistics.setVideoCount(BigInteger.valueOf(50L));
        statistics.setViewCount(BigInteger.valueOf(100000L));
        channel.setStatistics(statistics);

        // Mock latest videos
        VideoResult videoResult = new VideoResult(
                "Test Video",
                "Test Description",
                "videoId123",
                channelId,
                "http://thumbnail.url",
                "Channel Title",
                Arrays.asList("tag1", "tag2")
        );

        List<VideoResult> latestVideos = Arrays.asList(videoResult);
        when(youTubeService.getChannelProfile(channelId)).thenReturn(channel);
        when(youTubeService.getLatestVideosByChannel(channelId, 10)).thenReturn(latestVideos);
        CompletionStage<Result> resultStage = homeController.channelProfile(channelId);
        Result result = resultStage.toCompletableFuture().join();

        assertEquals(OK, result.status());
        String content = contentAsString(result);

        assertTrue(content.contains("Test Channel"));
        assertTrue(content.contains("Test Channel Description"));
        assertTrue(content.contains("Subscribers:"));
        assertTrue(content.contains("1000"));
        assertTrue(content.contains("Videos:"));
        assertTrue(content.contains("50"));
        assertTrue(content.contains("Views:"));
        assertTrue(content.contains("100000"));
        assertTrue(content.contains("Test Video"));
    }

    @Test

/**
 * This method represents testChannelProfile_Exception.
 *
 * @return [Description of return value]
 */
    public void testChannelProfile_Exception() throws IOException {

        String channelId = "channelId123";
        when(youTubeService.getChannelProfile(channelId)).thenThrow(new IOException("Simulated exception"));
        CompletionStage<Result> resultStage = homeController.channelProfile(channelId);
        Result result = resultStage.toCompletableFuture().join();
        assertEquals(INTERNAL_SERVER_ERROR, result.status());
        String content = contentAsString(result);
        assertEquals("Unable to fetch channel information", content);
    }

    @Test
/**
 * This method represents testShowVideoDetails_VideoNotFound.
 *
 * @return [Description of return value]
 */
    public void testShowVideoDetails_VideoNotFound() {
        String videoId = "nonExistentVideoId";
        when(youTubeService.getVideoDetails(videoId)).thenReturn(null);
        CompletionStage<Result> resultStage = homeController.showVideoDetails(videoId);
        Result result = resultStage.toCompletableFuture().join();

        assertEquals(NOT_FOUND, result.status());
        String content = contentAsString(result);
        assertEquals("Video not found", content);
    }

    @Test
/**
 * This method represents testSearch_CacheMiss.
 *
 * @return [Description of return value]
 */
    public void testSearch_CacheMiss() throws Exception {
        Http.Request request = fakeRequest().build();
        when(cache.getOptional(anyString())).thenReturn(Optional.empty());
        when(youTubeService.searchVideos(anyString())).thenReturn(Collections.emptyList());
        CompletionStage<Result> resultStage = homeController.search("new query", request);
        Result result = resultStage.toCompletableFuture().join();
        assertEquals(OK, result.status());
        verify(cache).set(anyString(), any());
        assertTrue(contentAsString(result).contains("Displaying results for \"new query\""));
    }

    @Test

/**
 * This method represents testSearch_SessionIdCreation.
 *
 * @return [Description of return value]
 */
    public void testSearch_SessionIdCreation() {

        Http.Request request = fakeRequest().session("sessionId", "").build();
        when(youTubeService.searchVideos(anyString())).thenReturn(Collections.emptyList());
        CompletionStage<Result> resultStage = homeController.search("test query", request);
        Result result = resultStage.toCompletableFuture().join();
        assertEquals(OK, result.status());
        String sessionId = result.session().get("sessionId").orElse(null);
        assertNotNull(sessionId);
    }

    @Test

/**
 * This method represents testSearch_EmptyDescriptionFilter.
 *
 * @return [Description of return value]
 */
    public void testSearch_EmptyDescriptionFilter() throws Exception {
        Http.Request request = fakeRequest().build();
        VideoResult videoWithEmptyDescription = new VideoResult(
                "Title with Empty Description", "", "videoId123", "channelId123", "http://thumbnail.url", "Channel Title", Arrays.asList("tag")
        );
        when(youTubeService.searchVideos(anyString())).thenReturn(Arrays.asList(videoWithEmptyDescription));

        CompletionStage<Result> resultStage = homeController.search("test query", request);
        Result result = resultStage.toCompletableFuture().join();

        assertEquals(OK, result.status());
        assertFalse(contentAsString(result).contains("Title with Empty Description"));
    }

    @Test

/**
 * This method represents testChannelProfile_VideosIOException.
 *
 * @return [Description of return value]
 */
    public void testChannelProfile_VideosIOException() throws Exception {
        String channelId = "channelId123";
        Channel channel = new Channel();
        ChannelSnippet snippet = new ChannelSnippet();
        snippet.setTitle("Test Channel");
        channel.setSnippet(snippet);

        when(youTubeService.getChannelProfile(channelId)).thenReturn(channel);
        when(youTubeService.getLatestVideosByChannel(channelId, 10)).thenThrow(new IOException("Video fetch error"));
        CompletionStage<Result> resultStage = homeController.channelProfile(channelId);
        Result result = resultStage.toCompletableFuture().join();
        assertEquals(INTERNAL_SERVER_ERROR, result.status());
        assertTrue(contentAsString(result).contains("Unable to fetch channel information"));
    }

    @Test

/**
 * This method represents testWordStats_LambdaExpression.
 *
 * @return [Description of return value]
 */
    public void testWordStats_LambdaExpression() {
        String query = "test query";
        // Create mock video results with sample descriptions
        VideoResult video1 = new VideoResult(
                "Test Title 1",
                "hello world",
                "videoId1",
                "channelId1",
                "http://thumbnail1.url",
                "Channel Title 1",
                Arrays.asList("tag1")
        );

        VideoResult video2 = new VideoResult(
                "Test Title 2",
                "hello again",
                "videoId2",
                "channelId2",
                "http://thumbnail2.url",
                "Channel Title 2",
                Arrays.asList("tag2")
        );

        // List of video results
        List<VideoResult> videoResults = Arrays.asList(video1, video2);
        // Mock YouTubeService to return the video results
        when(youTubeService.searchVideos(query)).thenReturn(videoResults);

        // Call the wordStats method and get the result
        CompletionStage<Result> resultStage = homeController.wordStats(query);
        Result result = resultStage.toCompletableFuture().join();
        // Assert that the status is OK
        assertEquals(OK, result.status());
        // Get the content of the result
        String content = contentAsString(result);
        // Assert that the words and their frequencies appear in the content
        assertTrue(content.contains("hello"));
        assertTrue(content.contains("2")); // "hello" appears twice
        assertTrue(content.contains("world"));
        assertTrue(content.contains("1")); // "world" appears once
        assertTrue(content.contains("again"));
        assertTrue(content.contains("1")); // "again" appears once
    }
    @Test

/**
 * This method represents testWordStats_LambdaExpression_Extended.
 *
 * @return [Description of return value]
 */
    public void testWordStats_LambdaExpression_Extended() {
        String query = "test query extended";
        // Mock videos with different cases, punctuations, and some empty descriptions
        VideoResult video1 = new VideoResult(
                "Title 1", "Hello world!", "videoId1", "channelId1", "http://thumbnail1.url", "Channel Title 1", Arrays.asList("tag1")
        );
        VideoResult video2 = new VideoResult(
                "Title 2", "hello again, world", "videoId2", "channelId2", "http://thumbnail2.url", "Channel Title 2", Arrays.asList("tag2")
        );
        VideoResult video3 = new VideoResult(
                "Title 3", "", "videoId3", "channelId3", "http://thumbnail3.url", "Channel Title 3", Arrays.asList("tag3")
        );

        List<VideoResult> videoResults = Arrays.asList(video1, video2, video3);

        // Mock YouTubeService to return the video results
        when(youTubeService.searchVideos(query)).thenReturn(videoResults);
        // Call the wordStats method and get the result
        CompletionStage<Result> resultStage = homeController.wordStats(query);
        Result result = resultStage.toCompletableFuture().join();
        // Assert that the status is OK
        assertEquals(OK, result.status());
        // Get the content of the result
        String content = contentAsString(result);
        // Validate that words are counted case-insensitively, with punctuation handled
        assertTrue(content.contains("hello"));
        assertTrue(content.contains("2")); // "hello" appears twice across descriptions
        assertTrue(content.contains("world"));
        assertTrue(content.contains("2")); // "world" appears twice, despite punctuation
        assertTrue(content.contains("again"));
        assertTrue(content.contains("1")); // "again" appears once
        // Check that empty descriptions are ignored in word count
        assertFalse(content.contains("Title 3"));
    }
}