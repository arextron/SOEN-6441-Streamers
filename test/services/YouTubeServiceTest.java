package services;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.*;
import models.VideoResult;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class YouTubeServiceTest {

    private YouTube youtubeMock;
    private YouTubeService youTubeService;

    @Before
    public void setUp() {
        // Mock the YouTube client
        youtubeMock = Mockito.mock(YouTube.class);
        // Instantiate YouTubeService with the mocked YouTube client
        youTubeService = new YouTubeService(youtubeMock);
    }
    @Test
    public void testSearchVideos() throws Exception {
        // Mock YouTube.Search and YouTube.Search.List
        YouTube.Search searchMock = mock(YouTube.Search.class);
        YouTube.Search.List searchListMock = mock(YouTube.Search.List.class);

        // Mock YouTube.Videos and YouTube.Videos.List
        YouTube.Videos videosMock = mock(YouTube.Videos.class);
        YouTube.Videos.List videosListMock = mock(YouTube.Videos.List.class);

        // Mock response for search
        SearchListResponse searchResponseMock = mock(SearchListResponse.class);
        SearchResult searchResultMock = mock(SearchResult.class);
        SearchResultSnippet snippetMock = mock(SearchResultSnippet.class);
        Thumbnail thumbnailMock = mock(Thumbnail.class);
        ThumbnailDetails thumbnailDetailsMock = mock(ThumbnailDetails.class);
        ResourceId idMock = mock(ResourceId.class);

        // Mock response for getVideoDetails
        VideoListResponse videoListResponseMock = mock(VideoListResponse.class);
        Video videoMock = mock(Video.class);
        VideoSnippet videoSnippetMock = mock(VideoSnippet.class);

        // Setup mocks for searchVideos
        when(youtubeMock.search()).thenReturn(searchMock);
        when(searchMock.list(anyString())).thenReturn(searchListMock);
        when(searchListMock.setQ(anyString())).thenReturn(searchListMock);
        when(searchListMock.setMaxResults(anyLong())).thenReturn(searchListMock);
        when(searchListMock.setType(anyString())).thenReturn(searchListMock);
        when(searchListMock.setKey(anyString())).thenReturn(searchListMock);
        when(searchListMock.execute()).thenReturn(searchResponseMock);

        when(searchResponseMock.getItems()).thenReturn(Arrays.asList(searchResultMock));
        when(searchResultMock.getSnippet()).thenReturn(snippetMock);
        when(searchResultMock.getId()).thenReturn(idMock);
        when(idMock.getVideoId()).thenReturn("videoId");
        when(snippetMock.getTitle()).thenReturn("Search Video Title");
        when(snippetMock.getDescription()).thenReturn("Search Video Description");
        when(snippetMock.getThumbnails()).thenReturn(thumbnailDetailsMock);
        when(thumbnailDetailsMock.getDefault()).thenReturn(thumbnailMock);
        when(thumbnailMock.getUrl()).thenReturn("Search Thumbnail URL");
        when(snippetMock.getChannelTitle()).thenReturn("Search Channel Title");
        when(snippetMock.getChannelId()).thenReturn("Search Channel ID");

        // Setup mocks for getVideoDetails
        when(youtubeMock.videos()).thenReturn(videosMock);
        when(videosMock.list(anyString())).thenReturn(videosListMock);
        when(videosListMock.setId(anyString())).thenReturn(videosListMock);
        when(videosListMock.setKey(anyString())).thenReturn(videosListMock);
        when(videosListMock.execute()).thenReturn(videoListResponseMock);

        when(videoListResponseMock.getItems()).thenReturn(Arrays.asList(videoMock));
        when(videoMock.getSnippet()).thenReturn(videoSnippetMock);
        when(videoSnippetMock.getTitle()).thenReturn("Detailed Video Title");
        when(videoSnippetMock.getDescription()).thenReturn("Detailed Video Description");
        when(videoSnippetMock.getChannelId()).thenReturn("Detailed Channel ID");
        when(videoSnippetMock.getChannelTitle()).thenReturn("Detailed Channel Title");
        when(videoSnippetMock.getThumbnails()).thenReturn(thumbnailDetailsMock);
        when(thumbnailDetailsMock.getDefault()).thenReturn(thumbnailMock);
        when(thumbnailMock.getUrl()).thenReturn("Detailed Thumbnail URL");
        when(videoSnippetMock.getTags()).thenReturn(Arrays.asList("tag1", "tag2"));

        // Call the method under test
        List<VideoResult> results = youTubeService.searchVideos("query");

        // Verify results
        assertNotNull(results);
        assertEquals(1, results.size());
        VideoResult result = results.get(0);

        // Assertions for the result
        assertEquals("Detailed Video Title", result.getTitle());
        assertEquals("Detailed Video Description", result.getDescription());
        assertEquals("videoId", result.getVideoId());
        assertEquals("Detailed Channel ID", result.getChannelId());
        assertEquals("Detailed Thumbnail URL", result.getThumbnailUrl());
        assertEquals("Detailed Channel Title", result.getChannelTitle());
        assertEquals(Arrays.asList("tag1", "tag2"), result.getTags());
    }
    @Test
    public void testGetVideoDetails_IOException() throws Exception {
        // Mock YouTube.Videos and YouTube.Videos.List
        YouTube.Videos videosMock = mock(YouTube.Videos.class);
        YouTube.Videos.List videosListMock = mock(YouTube.Videos.List.class);

        // Setup mocks
        when(youtubeMock.videos()).thenReturn(videosMock);
        when(videosMock.list(anyString())).thenReturn(videosListMock);
        when(videosListMock.setId(anyString())).thenReturn(videosListMock);
        when(videosListMock.setKey(anyString())).thenReturn(videosListMock);

        // Simulate IOException
        when(videosListMock.execute()).thenThrow(new IOException("Simulated IOException"));

        // Call the method under test
        VideoResult result = youTubeService.getVideoDetails("videoId");

        // Verify result is null
        assertNull(result);
    }

    @Test
    public void testSearchVideos_EmptyResults() throws Exception {
        // Mock YouTube.Search and YouTube.Search.List
        YouTube.Search searchMock = mock(YouTube.Search.class);
        YouTube.Search.List searchListMock = mock(YouTube.Search.List.class);

        // Setup mocks
        when(youtubeMock.search()).thenReturn(searchMock);
        when(searchMock.list(anyString())).thenReturn(searchListMock);
        when(searchListMock.setQ(anyString())).thenReturn(searchListMock);
        when(searchListMock.setMaxResults(anyLong())).thenReturn(searchListMock);
        when(searchListMock.setType(anyString())).thenReturn(searchListMock);
        when(searchListMock.setKey(anyString())).thenReturn(searchListMock);

        // Mock empty response
        SearchListResponse responseMock = mock(SearchListResponse.class);
        when(searchListMock.execute()).thenReturn(responseMock);
        when(responseMock.getItems()).thenReturn(Arrays.asList());

        // Call the method under test
        List<VideoResult> results = youTubeService.searchVideos("query");

        // Verify results
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }
    @Test
    public void testSearchVideos_IOException() throws Exception {
        // Mock YouTube.Search and YouTube.Search.List
        YouTube.Search searchMock = mock(YouTube.Search.class);
        YouTube.Search.List searchListMock = mock(YouTube.Search.List.class);

        // Setup mocks
        when(youtubeMock.search()).thenReturn(searchMock);
        when(searchMock.list(anyString())).thenReturn(searchListMock);
        when(searchListMock.setQ(anyString())).thenReturn(searchListMock);
        when(searchListMock.setMaxResults(anyLong())).thenReturn(searchListMock);
        when(searchListMock.setType(anyString())).thenReturn(searchListMock);
        when(searchListMock.setKey(anyString())).thenReturn(searchListMock);

        // Simulate IOException
        when(searchListMock.execute()).thenThrow(new IOException("Simulated IOException"));

        // Call the method under test
        List<VideoResult> results = youTubeService.searchVideos("query");

        // Verify results
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }
    @Test
    public void testSearchVideosByTag_EmptyResults() throws Exception {
        // Mock YouTube.Search and YouTube.Search.List
        YouTube.Search searchMock = mock(YouTube.Search.class);
        YouTube.Search.List searchListMock = mock(YouTube.Search.List.class);

        // Setup mocks
        when(youtubeMock.search()).thenReturn(searchMock);
        when(searchMock.list(anyString())).thenReturn(searchListMock);
        when(searchListMock.setQ(anyString())).thenReturn(searchListMock);
        when(searchListMock.setType(anyString())).thenReturn(searchListMock);
        when(searchListMock.setMaxResults(anyLong())).thenReturn(searchListMock);
        when(searchListMock.setKey(anyString())).thenReturn(searchListMock);

        // Mock empty response
        SearchListResponse responseMock = mock(SearchListResponse.class);
        when(searchListMock.execute()).thenReturn(responseMock);
        when(responseMock.getItems()).thenReturn(Arrays.asList());

        // Call the method under test
        List<VideoResult> results = youTubeService.searchVideosByTag("tag");

        // Verify results
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }
    @Test
    public void testSearchVideosByTag_NullVideoDetails() throws Exception {
        // Mock YouTube.Search and YouTube.Search.List
        YouTube.Search searchMock = mock(YouTube.Search.class);
        YouTube.Search.List searchListMock = mock(YouTube.Search.List.class);

        // Setup mocks
        when(youtubeMock.search()).thenReturn(searchMock);
        when(searchMock.list(anyString())).thenReturn(searchListMock);
        when(searchListMock.setQ(anyString())).thenReturn(searchListMock);
        when(searchListMock.setType(anyString())).thenReturn(searchListMock);
        when(searchListMock.setMaxResults(anyLong())).thenReturn(searchListMock);
        when(searchListMock.setKey(anyString())).thenReturn(searchListMock);

        // Mock response with a valid item
        SearchListResponse responseMock = mock(SearchListResponse.class);
        SearchResult searchResultMock = mock(SearchResult.class);
        ResourceId idMock = mock(ResourceId.class);
        when(searchResultMock.getId()).thenReturn(idMock);
        when(idMock.getVideoId()).thenReturn("videoId");
        when(responseMock.getItems()).thenReturn(Arrays.asList(searchResultMock));
        when(searchListMock.execute()).thenReturn(responseMock);

        // Mock getVideoDetails to return null
        YouTubeService spyService = spy(youTubeService);
        doReturn(null).when(spyService).getVideoDetails("videoId");

        // Call the method under test
        List<VideoResult> results = spyService.searchVideosByTag("tag");

        // Verify results
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }
    @Test
    public void testYouTubeServiceConstructor_Success() {
        try {
            // Simulate successful initialization
            YouTubeService service = new YouTubeService();
            assertNotNull(service); // Ensure the object is created successfully
        } catch (Exception e) {
            fail("Constructor should not throw an exception on successful initialization.");
        }
    }

    @Test
    public void testGetLatestVideosByChannel_ValidResponse() throws Exception {
        // Mock YouTube.Search and YouTube.Search.List
        YouTube.Search searchMock = mock(YouTube.Search.class);
        YouTube.Search.List searchListMock = mock(YouTube.Search.List.class);

        // Mock response
        SearchListResponse responseMock = mock(SearchListResponse.class);
        SearchResult searchResultMock = mock(SearchResult.class);
        ResourceId idMock = mock(ResourceId.class);

        // Mock response for getVideoDetails
        VideoResult videoResultMock = mock(VideoResult.class);

        // Setup mocks
        when(youtubeMock.search()).thenReturn(searchMock);
        when(searchMock.list(anyString())).thenReturn(searchListMock);
        when(searchListMock.setChannelId(anyString())).thenReturn(searchListMock);
        when(searchListMock.setMaxResults(anyLong())).thenReturn(searchListMock);
        when(searchListMock.setOrder(anyString())).thenReturn(searchListMock);
        when(searchListMock.setKey(anyString())).thenReturn(searchListMock);
        when(searchListMock.execute()).thenReturn(responseMock);

        when(responseMock.getItems()).thenReturn(Arrays.asList(searchResultMock));
        when(searchResultMock.getId()).thenReturn(idMock);
        when(idMock.getVideoId()).thenReturn("videoId");

        YouTubeService spyService = spy(youTubeService);
        doReturn(videoResultMock).when(spyService).getVideoDetails("videoId");

        // Call the method under test
        List<VideoResult> results = spyService.getLatestVideosByChannel("channelId", 5);

        // Verify results
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(videoResultMock, results.get(0));
    }
    @Test
    public void testGetLatestVideosByChannel_EmptyResults() throws Exception {
        // Mock YouTube.Search and YouTube.Search.List
        YouTube.Search searchMock = mock(YouTube.Search.class);
        YouTube.Search.List searchListMock = mock(YouTube.Search.List.class);

        // Mock response
        SearchListResponse responseMock = mock(SearchListResponse.class);

        // Setup mocks
        when(youtubeMock.search()).thenReturn(searchMock);
        when(searchMock.list(anyString())).thenReturn(searchListMock);
        when(searchListMock.setChannelId(anyString())).thenReturn(searchListMock);
        when(searchListMock.setMaxResults(anyLong())).thenReturn(searchListMock);
        when(searchListMock.setOrder(anyString())).thenReturn(searchListMock);
        when(searchListMock.setKey(anyString())).thenReturn(searchListMock);
        when(searchListMock.execute()).thenReturn(responseMock);

        when(responseMock.getItems()).thenReturn(Arrays.asList());

        // Call the method under test
        List<VideoResult> results = youTubeService.getLatestVideosByChannel("channelId", 5);

        // Verify results
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }


    @Test
    public void testSearchVideosByTag_IOException() throws Exception {
        // Mock YouTube.Search and YouTube.Search.List
        YouTube.Search searchMock = mock(YouTube.Search.class);
        YouTube.Search.List searchListMock = mock(YouTube.Search.List.class);

        // Setup mocks
        when(youtubeMock.search()).thenReturn(searchMock);
        when(searchMock.list(anyString())).thenReturn(searchListMock);
        when(searchListMock.setQ(anyString())).thenReturn(searchListMock);
        when(searchListMock.setType(anyString())).thenReturn(searchListMock);
        when(searchListMock.setMaxResults(anyLong())).thenReturn(searchListMock);
        when(searchListMock.setKey(anyString())).thenReturn(searchListMock);

        // Simulate IOException
        when(searchListMock.execute()).thenThrow(new IOException("Simulated IOException"));

        // Call the method under test
        List<VideoResult> results = youTubeService.searchVideosByTag("tag");

        // Verify results
        assertNotNull(results);  // Ensure the method returns a list
        assertTrue(results.isEmpty());  // The list should be empty due to the exception
    }


    @Test(expected = RuntimeException.class)
    public void testYouTubeServiceConstructor_Failure() throws Exception {
        // Mock GoogleNetHttpTransport and JacksonFactory
        try (MockedStatic<GoogleNetHttpTransport> mockedTransport = mockStatic(GoogleNetHttpTransport.class);
             MockedStatic<JacksonFactory> mockedFactory = mockStatic(JacksonFactory.class)) {

            // Simulate exception when creating new trusted transport
            mockedTransport.when(GoogleNetHttpTransport::newTrustedTransport)
                    .thenThrow(new IOException("Simulated Transport Exception"));

            // Call the constructor, which should throw RuntimeException
            new YouTubeService();
        }
    }


    @Test
    public void testGetVideoDetails() throws Exception {
        // Mock YouTube.Videos and YouTube.Videos.List
        YouTube.Videos videosMock = mock(YouTube.Videos.class);
        YouTube.Videos.List videosListMock = mock(YouTube.Videos.List.class);

        // Mock response
        VideoListResponse responseMock = mock(VideoListResponse.class);
        Video videoMock = mock(Video.class);
        VideoSnippet snippetMock = mock(VideoSnippet.class);
        Thumbnail thumbnailMock = mock(Thumbnail.class);
        ThumbnailDetails thumbnailDetailsMock = mock(ThumbnailDetails.class);

        // Setup mocks
        when(youtubeMock.videos()).thenReturn(videosMock);
        when(videosMock.list(anyString())).thenReturn(videosListMock);
        when(videosListMock.setId(anyString())).thenReturn(videosListMock);
        when(videosListMock.setKey(anyString())).thenReturn(videosListMock);
        when(videosListMock.execute()).thenReturn(responseMock);

        when(responseMock.getItems()).thenReturn(Arrays.asList(videoMock));
        when(videoMock.getSnippet()).thenReturn(snippetMock);
        when(snippetMock.getTitle()).thenReturn("Title");
        when(snippetMock.getDescription()).thenReturn("Description");
        when(snippetMock.getChannelId()).thenReturn("channelId");
        when(snippetMock.getChannelTitle()).thenReturn("Channel Title");
        when(snippetMock.getThumbnails()).thenReturn(thumbnailDetailsMock);
        when(thumbnailDetailsMock.getDefault()).thenReturn(thumbnailMock);
        when(thumbnailMock.getUrl()).thenReturn("thumbnailUrl");
        when(snippetMock.getTags()).thenReturn(Arrays.asList("tag1", "tag2"));

        // Call the method under test
        VideoResult result = youTubeService.getVideoDetails("videoId");

        // Verify results
        assertNotNull(result);
        assertEquals("Title", result.getTitle());
        assertEquals("Description", result.getDescription());
        assertEquals("videoId", result.getVideoId());
        assertEquals("channelId", result.getChannelId());
        assertEquals("thumbnailUrl", result.getThumbnailUrl());
        assertEquals("Channel Title", result.getChannelTitle());
        assertEquals(Arrays.asList("tag1", "tag2"), result.getTags());
    }

    @Test
    public void testGetChannelProfile() throws Exception {
        // Mock YouTube.Channels and YouTube.Channels.List
        YouTube.Channels channelsMock = mock(YouTube.Channels.class);
        YouTube.Channels.List channelsListMock = mock(YouTube.Channels.List.class);

        // Mock response
        ChannelListResponse responseMock = mock(ChannelListResponse.class);
        Channel channelMock = mock(Channel.class);

        // Setup mocks
        when(youtubeMock.channels()).thenReturn(channelsMock);
        when(channelsMock.list(anyString())).thenReturn(channelsListMock);
        when(channelsListMock.setId(anyString())).thenReturn(channelsListMock);
        when(channelsListMock.setKey(anyString())).thenReturn(channelsListMock);
        when(channelsListMock.execute()).thenReturn(responseMock);

        when(responseMock.getItems()).thenReturn(Arrays.asList(channelMock));

        // Call the method under test
        Channel result = youTubeService.getChannelProfile("channelId");

        // Verify results
        assertNotNull(result);
        // Additional assertions can be added based on your needs
    }

    @Test(expected = IOException.class)
    public void testGetChannelProfile_ChannelNotFound() throws Exception {
        // Mock YouTube.Channels and YouTube.Channels.List
        YouTube.Channels channelsMock = mock(YouTube.Channels.class);
        YouTube.Channels.List channelsListMock = mock(YouTube.Channels.List.class);

        // Mock response with no channels
        ChannelListResponse responseMock = mock(ChannelListResponse.class);

        // Setup mocks
        when(youtubeMock.channels()).thenReturn(channelsMock);
        when(channelsMock.list(anyString())).thenReturn(channelsListMock);
        when(channelsListMock.setId(anyString())).thenReturn(channelsListMock);
        when(channelsListMock.setKey(anyString())).thenReturn(channelsListMock);
        when(channelsListMock.execute()).thenReturn(responseMock);

        when(responseMock.getItems()).thenReturn(Arrays.asList());

        // Call the method under test, which should throw an IOException
        youTubeService.getChannelProfile("channelId");
    }

    @Test
    public void testSearchVideosByTag() throws Exception {
        // Mock YouTube.Search and YouTube.Search.List
        YouTube.Search searchMock = mock(YouTube.Search.class);
        YouTube.Search.List searchListMock = mock(YouTube.Search.List.class);

        // Mock response
        SearchListResponse responseMock = mock(SearchListResponse.class);
        SearchResult searchResultMock = mock(SearchResult.class);
        ResourceId idMock = mock(ResourceId.class); // Use ResourceId instead of Id

        // Setup mocks
        when(youtubeMock.search()).thenReturn(searchMock);
        when(searchMock.list(anyString())).thenReturn(searchListMock);
        when(searchListMock.setQ(anyString())).thenReturn(searchListMock);
        when(searchListMock.setType(anyString())).thenReturn(searchListMock);
        when(searchListMock.setMaxResults(anyLong())).thenReturn(searchListMock);
        when(searchListMock.setKey(anyString())).thenReturn(searchListMock);
        when(searchListMock.execute()).thenReturn(responseMock);

        when(responseMock.getItems()).thenReturn(Arrays.asList(searchResultMock));
        when(searchResultMock.getId()).thenReturn(idMock);
        when(idMock.getVideoId()).thenReturn("videoId");

        // Mock getVideoDetails to return a VideoResult
        VideoResult videoResultMock = mock(VideoResult.class);
        YouTubeService spyService = spy(youTubeService);
        doReturn(videoResultMock).when(spyService).getVideoDetails("videoId");

        // Call the method under test
        List<VideoResult> results = spyService.searchVideosByTag("tag");

        // Verify results
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(videoResultMock, results.get(0));
    }

    @Test
    public void testGetLast10Videos() throws Exception {
        // Mock YouTube.Search and YouTube.Search.List
        YouTube.Search searchMock = mock(YouTube.Search.class);
        YouTube.Search.List searchListMock = mock(YouTube.Search.List.class);

        // Mock response
        SearchListResponse responseMock = mock(SearchListResponse.class);
        SearchResult searchResultMock = mock(SearchResult.class);
        SearchResultSnippet snippetMock = mock(SearchResultSnippet.class);
        ThumbnailDetails thumbnailDetailsMock = mock(ThumbnailDetails.class);
        Thumbnail thumbnailMock = mock(Thumbnail.class);
        ResourceId idMock = mock(ResourceId.class); // Use ResourceId instead of Id

        // Setup mocks
        when(youtubeMock.search()).thenReturn(searchMock);
        when(searchMock.list(anyString())).thenReturn(searchListMock);
        when(searchListMock.setChannelId(anyString())).thenReturn(searchListMock);
        when(searchListMock.setType(anyString())).thenReturn(searchListMock);
        when(searchListMock.setMaxResults(anyLong())).thenReturn(searchListMock);
        when(searchListMock.setOrder(anyString())).thenReturn(searchListMock);
        when(searchListMock.setKey(anyString())).thenReturn(searchListMock);
        when(searchListMock.execute()).thenReturn(responseMock);

        when(responseMock.getItems()).thenReturn(Arrays.asList(searchResultMock));
        when(searchResultMock.getSnippet()).thenReturn(snippetMock);
        when(searchResultMock.getId()).thenReturn(idMock);
        when(idMock.getVideoId()).thenReturn("videoId");
        when(snippetMock.getTitle()).thenReturn("Title");
        when(snippetMock.getDescription()).thenReturn("Description");
        when(snippetMock.getThumbnails()).thenReturn(thumbnailDetailsMock);
        when(thumbnailDetailsMock.getDefault()).thenReturn(thumbnailMock);
        when(thumbnailMock.getUrl()).thenReturn("thumbnailUrl");
        when(snippetMock.getChannelTitle()).thenReturn("Channel Title");

        // Call the method under test
        List<VideoResult> results = youTubeService.getLast10Videos("channelId");

        // Verify results
        assertNotNull(results);
        assertEquals(1, results.size());
        VideoResult result = results.get(0);
        assertEquals("Title", result.getTitle());
        assertEquals("Description", result.getDescription());
        assertEquals("videoId", result.getVideoId());
        // Additional assertions can be added
    }

    // Add more tests as needed for other methods

}
