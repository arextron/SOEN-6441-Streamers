package controllers;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class YouTubeServiceTest {
    private YouTube youtubeMock;
    private YouTubeService youtubeService;

    @Before
    public void setUp() {
        youtubeMock = mock(YouTube.class);
        youtubeService = new YouTubeService(youtubeMock);
    }
    @Test
    public void testYouTubeService_DefaultConstructor() {
        YouTubeService service = new YouTubeService();
        assertNotNull(service);
    }
    @Test
    public void testSearchVideos_ValidQuery() throws IOException {
        String query = "java programming";

        // Mock the YouTube API response
        YouTube.Search searchMock = mock(YouTube.Search.class);
        YouTube.Search.List searchListMock = mock(YouTube.Search.List.class);
        SearchListResponse responseMock = mock(SearchListResponse.class);

        SearchResultSnippet snippet = new SearchResultSnippet();
        snippet.setTitle("Java Tutorial");
        snippet.setDescription("Learn Java Programming");
        snippet.setChannelId("channel123");
        Thumbnail thumbnail = new Thumbnail();
        thumbnail.setUrl("http://example.com/thumb.jpg");
        ThumbnailDetails thumbnails = new ThumbnailDetails();
        thumbnails.setDefault(thumbnail);
        snippet.setThumbnails(thumbnails);

        ResourceId resourceId = new ResourceId();
        resourceId.setVideoId("video123");

        SearchResult searchResult = new SearchResult();
        searchResult.setSnippet(snippet);
        searchResult.setId(resourceId);

        when(youtubeMock.search()).thenReturn(searchMock);
        when(searchMock.list("snippet")).thenReturn(searchListMock);
        when(searchListMock.setQ(query)).thenReturn(searchListMock);
        when(searchListMock.setMaxResults(anyLong())).thenReturn(searchListMock);
        when(searchListMock.setType("video")).thenReturn(searchListMock);
        when(searchListMock.setKey(anyString())).thenReturn(searchListMock);
        when(searchListMock.execute()).thenReturn(responseMock);
        when(responseMock.getItems()).thenReturn(Arrays.asList(searchResult));

        List<VideoResult> results = youtubeService.searchVideos(query);

        // Verify the results
        assertEquals(1, results.size());
        VideoResult video = results.get(0);
        assertEquals("Java Tutorial", video.getTitle());
        assertEquals("Learn Java Programming", video.getDescription());
        assertEquals("https://www.youtube.com/watch?v=video123", video.getVideoUrl());
        assertEquals("https://www.youtube.com/channel/channel123", video.getChannelUrl());
        assertEquals("http://example.com/thumb.jpg", video.getThumbnailUrl());
    }

    @Test
    public void testSearchVideos_IOException() throws IOException {
        String query = "test";

        // Mock to throw IOException
        YouTube.Search searchMock = mock(YouTube.Search.class);
        YouTube.Search.List searchListMock = mock(YouTube.Search.List.class);

        when(youtubeMock.search()).thenReturn(searchMock);
        when(searchMock.list("snippet")).thenReturn(searchListMock);
        when(searchListMock.setQ(query)).thenReturn(searchListMock);
        when(searchListMock.setMaxResults(anyLong())).thenReturn(searchListMock);
        when(searchListMock.setType("video")).thenReturn(searchListMock);
        when(searchListMock.setKey(anyString())).thenReturn(searchListMock);
        when(searchListMock.execute()).thenThrow(new IOException("Simulated IOException"));

        List<VideoResult> results = youtubeService.searchVideos(query);

        // Verify that an empty list is returned on exception
        assertTrue(results.isEmpty());
    }
}
