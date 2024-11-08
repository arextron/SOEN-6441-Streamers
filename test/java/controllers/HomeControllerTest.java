package controllers;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import play.mvc.Result;
import play.twirl.api.Content;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static org.junit.Assert.*;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.*;
import static org.mockito.Mockito.*;

public class HomeControllerTest {

    private YouTubeService youTubeServiceMock;
    private HomeController homeController;

    @Before
    public void setUp() {
        // Mock the YouTubeService
        youTubeServiceMock = Mockito.mock(YouTubeService.class);
        // Inject the mocked service into HomeController
        homeController = new HomeController(youTubeServiceMock);
    }


    /**
     * Test the index() method to ensure it returns the correct status and content.
     */
    @Test
    public void testIndex() {
        Result result = homeController.index();
        assertEquals(OK, result.status());
        assertEquals("text/html", result.contentType().orElse(""));
        String content = contentAsString(result);
        assertTrue(content.contains("Welcome to TubeLytics"));
    }

    /**
     * Test the search() method with a null query to ensure it handles invalid input.
     */
    @Test
    public void testSearch_NullQuery() {
        CompletionStage<Result> resultStage = homeController.search(null);
        Result result = resultStage.toCompletableFuture().join();
        assertEquals(OK, result.status());
        String content = contentAsString(result);
        assertEquals("Please provide a search query.", content);
    }

    /**
     * Test the search() method with an empty query string.
     */
    @Test
    public void testSearch_EmptyQuery() {
        CompletionStage<Result> resultStage = homeController.search("");
        Result result = resultStage.toCompletableFuture().join();
        assertEquals(OK, result.status());
        String content = contentAsString(result);
        assertEquals("Please provide a search query.", content);
    }

    /**
     * Test the search() method with a valid query and verify it processes and renders the results correctly.
     */
    @Test
    public void testSearch_ValidQuery() {
        String query = "java programming";
        List<VideoResult> mockVideos = Arrays.asList(
                new VideoResult("Java Tutorial", "Learn Java", "vid1", "chan1", "thumb1"),
                new VideoResult("Advanced Java", "Deep dive into Java", "vid2", "chan2", "thumb2")
        );
        when(youTubeServiceMock.searchVideos(query)).thenReturn(mockVideos);

        CompletionStage<Result> resultStage = homeController.search(query);
        Result result = resultStage.toCompletableFuture().join();
        assertEquals(OK, result.status());
        String content = contentAsString(result);

        // Verify that the content contains the expected video titles and descriptions
        assertTrue(content.contains("Java Tutorial"));
        assertTrue(content.contains("Learn Java"));
        assertTrue(content.contains("Advanced Java"));
        assertTrue(content.contains("Deep dive into Java"));
    }

    /**
     * Test the search() method to ensure it filters out videos with empty descriptions.
     */
    @Test
    public void testSearch_VideosWithEmptyDescriptions() {
        String query = "test";
        List<VideoResult> mockVideos = Arrays.asList(
                new VideoResult("Video1", "", "vid1", "chan1", "thumb1"),
                new VideoResult("Video2", "Description2", "vid2", "chan2", "thumb2")
        );
        when(youTubeServiceMock.searchVideos(query)).thenReturn(mockVideos);

        CompletionStage<Result> resultStage = homeController.search(query);
        Result result = resultStage.toCompletableFuture().join();
        assertEquals(OK, result.status());
        String content = contentAsString(result);

        // Verify that videos with empty descriptions are filtered out
        assertFalse(content.contains("Video1"));
        assertTrue(content.contains("Video2"));
        assertTrue(content.contains("Description2"));
    }

    /**
     * Test the search() method to ensure it limits the results to 10 videos.
     */
    @Test
    public void testSearch_LimitsToTenVideos() {
        String query = "limit test";
        // Create a list of 15 mock videos
        List<VideoResult> mockVideos = new ArrayList<>();
        for (int i = 1; i <= 15; i++) {
            mockVideos.add(new VideoResult("Video" + i, "Description" + i, "vid" + i, "chan" + i, "thumb" + i));
        }
        when(youTubeServiceMock.searchVideos(query)).thenReturn(mockVideos);

        CompletionStage<Result> resultStage = homeController.search(query);
        Result result = resultStage.toCompletableFuture().join();
        String content = contentAsString(result);

        // Verify that only 10 videos are displayed
        for (int i = 1; i <= 10; i++) {
            assertTrue(content.contains("Video" + i));
            assertTrue(content.contains("Description" + i));
        }
        // Videos beyond the 10th should not be displayed
        assertFalse(content.contains("Video11"));
    }

    /**
     * Test the search() method to ensure it maintains a search history of up to 10 entries.
     */
    @Test
    public void testSearch_SearchHistoryLimit() {
        List<VideoResult> mockVideos = Arrays.asList(
                new VideoResult("Title", "Description", "vid", "chan", "thumb")
        );
        when(youTubeServiceMock.searchVideos(anyString())).thenReturn(mockVideos);

        // Perform 12 searches
        for (int i = 1; i <= 12; i++) {
            homeController.search("query" + i).toCompletableFuture().join();
        }

        // Access the search history via reflection (since it's private)
        try {
            java.lang.reflect.Field field = HomeController.class.getDeclaredField("searchHistory");
            field.setAccessible(true);
            LinkedList<Map.Entry<String, List<VideoResult>>> searchHistory =
                    (LinkedList<Map.Entry<String, List<VideoResult>>>) field.get(homeController);

            // Verify that only the last 10 searches are kept
            assertEquals(10, searchHistory.size());
            for (int i = 0; i < 10; i++) {
                String expectedQuery = "query" + (12 - i);
                assertEquals(expectedQuery, searchHistory.get(i).getKey());
            }
        } catch (Exception e) {
            fail("Failed to access search history: " + e.getMessage());
        }
    }
    @Test
    public void testWordStats_ValidQueryWithResults() {
        String query = "java programming";
        List<VideoResult> mockVideos = Arrays.asList(
                new VideoResult("Title1", "Java is a programming language.", "vid1", "chan1", "thumb1"),
                new VideoResult("Title2", "Programming in Java.", "vid2", "chan2", "thumb2")
        );

        when(youTubeServiceMock.searchVideos(query)).thenReturn(mockVideos);

        CompletionStage<Result> resultStage = homeController.wordStats(query);
        Result result = resultStage.toCompletableFuture().join();

        assertEquals(OK, result.status());
        String content = contentAsString(result);
        assertTrue(content.contains("Word Frequency Statistics for \"" + query + "\""));
        assertTrue(content.contains("java"));
        assertTrue(content.contains("programming"));
    }
    @Test
    public void testWordStats_NoResults() {
        String query = "unknown query";

        when(youTubeServiceMock.searchVideos(query)).thenReturn(Collections.emptyList());

        CompletionStage<Result> resultStage = homeController.wordStats(query);
        Result result = resultStage.toCompletableFuture().join();

        assertEquals(OK, result.status());
        String content = contentAsString(result);
        assertTrue(content.contains("No word frequency data available for \"" + query + "\"."));
    }
    @Test
    public void testWordStats_NullOrEmptyQuery() {
        CompletionStage<Result> resultStage = homeController.wordStats("");
        Result result = resultStage.toCompletableFuture().join();

        assertEquals(BAD_REQUEST, result.status());
        String content = contentAsString(result);
        assertTrue(content.contains("Please provide a search query."));
    }

    @Test
    public void testGetSearchHistory() {
        // Perform a search to populate the history
        String query = "java";
        List<VideoResult> mockVideos = Arrays.asList(
                new VideoResult("Java Tutorial", "Learn Java", "vid1", "chan1", "thumb1")
        );
        when(youTubeServiceMock.searchVideos(query)).thenReturn(mockVideos);
        homeController.search(query).toCompletableFuture().join();

        // Test getSearchHistory()
        LinkedList<Map.Entry<String, List<VideoResult>>> history = homeController.getSearchHistory();
        assertNotNull(history);
        assertEquals(1, history.size());
        assertEquals(query, history.getFirst().getKey());
    }
    @Test
    public void testWordStats_ValidQuery() {
        String query = "java programming";
        List<VideoResult> mockVideos = Arrays.asList(
                new VideoResult("Title1", "Java is a programming language.", "vid1", "chan1", "thumb1"),
                new VideoResult("Title2", "Programming in Java.", "vid2", "chan2", "thumb2")
        );

        when(youTubeServiceMock.searchVideos(query)).thenReturn(mockVideos);

        CompletionStage<Result> resultStage = homeController.wordStats(query);
        Result result = resultStage.toCompletableFuture().join();

        assertEquals(OK, result.status());
        String content = contentAsString(result);
        assertTrue(content.contains("Word Frequency Statistics for \"" + query + "\""));
        assertTrue(content.contains("java"));
        assertTrue(content.contains("programming"));
    }
    @Test
    public void testWordStats_Exception() {
        String query = "exception";

        when(youTubeServiceMock.searchVideos(query)).thenThrow(new RuntimeException("Simulated exception"));

        CompletionStage<Result> resultStage = homeController.wordStats(query);
        Result result = resultStage.toCompletableFuture().join();

        assertEquals(INTERNAL_SERVER_ERROR, result.status());
        String content = contentAsString(result);
        assertTrue(content.contains("An error occurred while processing your request."));
    }




}
