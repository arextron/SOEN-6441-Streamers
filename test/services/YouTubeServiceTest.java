//We certify that this submission is the original work of the members of the group and meets the Faculty's Expectations of Originality.
//Signed by- Aryan Awasthi, Harsukhvir Singh Grewal, Sharun Basnet
// 40278847, 40310953, 40272435

package services;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.*;
import models.VideoResult;
import services.YouTubeService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import java.io.IOException;
import java.math.BigInteger;
import java.util.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;


@RunWith(MockitoJUnitRunner.class)

/**
 * This class represents YouTubeServiceTest.
 */

public class YouTubeServiceTest {

    @Mock
    private YouTube youtube;

    @Mock
    private YouTube.Search youtubeSearch;

    @Mock
    private YouTube.Search.List searchList;

    @Mock
    private YouTube.Videos youtubeVideos;

    @Mock
    private YouTube.Videos.List videosList;

    @Mock
    private YouTube.Channels youtubeChannels;

    @Mock
    private YouTube.Channels.List channelsList;

    @InjectMocks
    private YouTubeService youTubeService;

    @Before

/**
 * This method represents setUp.
 *
 * @return [Description of return value]
 */
    public void setUp() {

        youTubeService = new YouTubeService(youtube);
    }
    @Test

/**
 * This method represents testSearchVideos.
 *
 * @return [Description of return value]
 */
    public void testSearchVideos() throws IOException {
        String query = "test query";
        SearchListResponse searchListResponse = new SearchListResponse();
        SearchResult searchResult = new SearchResult();
        ResourceId resourceId = new ResourceId();
        resourceId.setVideoId("videoId123");
        searchResult.setId(resourceId);
        searchResult.setSnippet(new SearchResultSnippet());
        searchListResponse.setItems(Collections.singletonList(searchResult));

        when(youtube.search()).thenReturn(youtubeSearch);
        when(youtubeSearch.list("snippet")).thenReturn(searchList);
        when(searchList.setQ(query)).thenReturn(searchList);
        when(searchList.setMaxResults(anyLong())).thenReturn(searchList);
        when(searchList.setType("video")).thenReturn(searchList);
        when(searchList.setKey(anyString())).thenReturn(searchList);
        when(searchList.execute()).thenReturn(searchListResponse);


        VideoResult videoResult = new VideoResult(
                "Test Title",
                "Test Description",
                "videoId123",
                "channelId123",
                "http://thumbnail.url",
                "Channel Title",
                Arrays.asList("tag1", "tag2")
        );
        YouTubeService spyService = spy(youTubeService);
        doReturn(videoResult).when(spyService).getVideoDetails("videoId123");
        List<VideoResult> results = spyService.searchVideos(query);
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(videoResult, results.get(0));
    }
    @Test

/**
 * This method represents testSearchVideos_VideoDetailNull.
 *
 * @return [Description of return value]
 */
    public void testSearchVideos_VideoDetailNull() throws IOException {

        String query = "test query";
        SearchListResponse searchListResponse = new SearchListResponse();
        SearchResult searchResult = new SearchResult();
        ResourceId resourceId = new ResourceId();
        resourceId.setVideoId("videoId123");
        searchResult.setId(resourceId);
        searchResult.setSnippet(new SearchResultSnippet());
        searchListResponse.setItems(Collections.singletonList(searchResult));
        when(youtube.search()).thenReturn(youtubeSearch);
        when(youtubeSearch.list("snippet")).thenReturn(searchList);
        when(searchList.setQ(query)).thenReturn(searchList);
        when(searchList.setMaxResults(anyLong())).thenReturn(searchList);
        when(searchList.setType("video")).thenReturn(searchList);
        when(searchList.setKey(anyString())).thenReturn(searchList);
        when(searchList.execute()).thenReturn(searchListResponse);

        YouTubeService spyService = spy(youTubeService);
        doReturn(null).when(spyService).getVideoDetails("videoId123");
        List<VideoResult> results = spyService.searchVideos(query);
        assertNotNull(results);
        assertTrue(results.isEmpty()); // Expect an empty list since videoDetail is null
    }
    @Test

/**
 * This method represents testGetVideoDetails.
 *
 * @return [Description of return value]
 */
    public void testGetVideoDetails() throws IOException {

        String videoId = "videoId123";

        VideoListResponse videoListResponse = new VideoListResponse();
        Video video = new Video();
        VideoSnippet snippet = new VideoSnippet();
        snippet.setTitle("Test Title");
        snippet.setDescription("Test Description");
        snippet.setChannelId("channelId123");
        snippet.setChannelTitle("Channel Title");
        snippet.setTags(Arrays.asList("tag1", "tag2"));

        Thumbnail thumbnail = new Thumbnail();
        thumbnail.setUrl("http://thumbnail.url");
        ThumbnailDetails thumbnailDetails = new ThumbnailDetails();
        thumbnailDetails.setDefault(thumbnail);
        snippet.setThumbnails(thumbnailDetails);

        video.setSnippet(snippet);
        videoListResponse.setItems(Collections.singletonList(video));

        when(youtube.videos()).thenReturn(youtubeVideos);
        when(youtubeVideos.list("snippet")).thenReturn(videosList);
        when(videosList.setId(videoId)).thenReturn(videosList);
        when(videosList.setKey(anyString())).thenReturn(videosList);
        when(videosList.execute()).thenReturn(videoListResponse);

        VideoResult result = youTubeService.getVideoDetails(videoId);

        assertNotNull(result);
        assertEquals("Test Title", result.getTitle());
        assertEquals("Test Description", result.getDescription());
        assertEquals(videoId, result.getVideoId());
        assertEquals("channelId123", result.getChannelId());
        assertEquals("http://thumbnail.url", result.getThumbnailUrl());
        assertEquals("Channel Title", result.getChannelTitle());
        assertEquals(Arrays.asList("tag1", "tag2"), result.getTags());
    }
    @Test

/**
 * This method represents testGetVideoDetails_NullTags.
 *
 * @return [Description of return value]
 */
    public void testGetVideoDetails_NullTags() throws IOException {

        String videoId = "videoId123";
        VideoListResponse videoListResponse = new VideoListResponse();
        Video video = new Video();

        VideoSnippet snippet = new VideoSnippet();
        snippet.setTitle("Test Title");
        snippet.setDescription("Test Description");
        snippet.setChannelId("channelId123");
        snippet.setChannelTitle("Channel Title");
        snippet.setTags(null); // Simulate tags being null


        Thumbnail thumbnail = new Thumbnail();
        thumbnail.setUrl("http://thumbnail.url");
        ThumbnailDetails thumbnailDetails = new ThumbnailDetails();
        thumbnailDetails.setDefault(thumbnail);
        snippet.setThumbnails(thumbnailDetails);

        video.setSnippet(snippet);
        videoListResponse.setItems(Collections.singletonList(video));
        when(youtube.videos()).thenReturn(youtubeVideos);
        when(youtubeVideos.list("snippet")).thenReturn(videosList);
        when(videosList.setId(videoId)).thenReturn(videosList);
        when(videosList.setKey(anyString())).thenReturn(videosList);
        when(videosList.execute()).thenReturn(videoListResponse);

        VideoResult result = youTubeService.getVideoDetails(videoId);
        assertNotNull(result);
        assertEquals("Test Title", result.getTitle());
        assertEquals("Test Description", result.getDescription());
        assertEquals(videoId, result.getVideoId());
        assertEquals("channelId123", result.getChannelId());
        assertEquals("http://thumbnail.url", result.getThumbnailUrl());
        assertEquals("Channel Title", result.getChannelTitle());
        assertNotNull(result.getTags());
        assertTrue(result.getTags().isEmpty()); // Expect an empty list when tags are null
    }

    @Test

/**
 * This method represents testGetVideoDetails_VideoNotFound.
 *
 * @return [Description of return value]
 */
    public void testGetVideoDetails_VideoNotFound() throws IOException {

        String videoId = "nonExistentVideoId";

        VideoListResponse videoListResponse = new VideoListResponse();
        videoListResponse.setItems(Collections.emptyList());

        when(youtube.videos()).thenReturn(youtubeVideos);
        when(youtubeVideos.list("snippet")).thenReturn(videosList);
        when(videosList.setId(videoId)).thenReturn(videosList);
        when(videosList.setKey(anyString())).thenReturn(videosList);
        when(videosList.execute()).thenReturn(videoListResponse);

        VideoResult result = youTubeService.getVideoDetails(videoId);
        assertNull(result);
    }
    @Test

/**
 * This method represents testSearchVideosByTag.
 *
 * @return [Description of return value]
 */
    public void testSearchVideosByTag() throws IOException {

        String tag = "testTag";
        SearchListResponse searchListResponse = new SearchListResponse();
        SearchResult searchResult = new SearchResult();

        SearchResultSnippet snippet = new SearchResultSnippet();
        snippet.setTitle("Test Title");
        snippet.setDescription("Test Description");
        snippet.setChannelId("channelId123");
        snippet.setChannelTitle("Channel Title");

        Thumbnail thumbnail = new Thumbnail();
        thumbnail.setUrl("http://thumbnail.url");
        ThumbnailDetails thumbnailDetails = new ThumbnailDetails();
        thumbnailDetails.setDefault(thumbnail);
        snippet.setThumbnails(thumbnailDetails);

        searchResult.setId(new ResourceId().setVideoId("videoId123"));
        searchResult.setSnippet(snippet);
        searchListResponse.setItems(Collections.singletonList(searchResult));

        when(youtube.search()).thenReturn(youtubeSearch);
        when(youtubeSearch.list("snippet")).thenReturn(searchList);
        when(searchList.setQ(tag)).thenReturn(searchList);
        when(searchList.setType("video")).thenReturn(searchList);
        when(searchList.setMaxResults(anyLong())).thenReturn(searchList);
        when(searchList.setKey(anyString())).thenReturn(searchList);
        when(searchList.execute()).thenReturn(searchListResponse);

        List<VideoResult> results = youTubeService.searchVideosByTag(tag);
        assertNotNull(results);
        assertEquals(1, results.size());
        VideoResult result = results.get(0);
        assertEquals("Test Title", result.getTitle());
        assertEquals("Test Description", result.getDescription());
        assertEquals("videoId123", result.getVideoId());
        assertEquals("channelId123", result.getChannelId());
        assertEquals("http://thumbnail.url", result.getThumbnailUrl());
        assertEquals("Channel Title", result.getChannelTitle());
        assertNotNull(result.getTags());
        assertTrue(result.getTags().isEmpty()); // Tags are empty in search results
    }

    @Test

/**
 * This method represents testGetChannelProfile.
 *
 * @return [Description of return value]
 */
    public void testGetChannelProfile() throws IOException {
        String channelId = "channelId123";
        ChannelListResponse channelListResponse = new ChannelListResponse();
        Channel channel = new Channel();

        ChannelSnippet snippet = new ChannelSnippet();
        snippet.setTitle("Test Channel");
        snippet.setDescription("Test Channel Description");
        channel.setSnippet(snippet);

        ChannelStatistics statistics = new ChannelStatistics();
        statistics.setSubscriberCount(BigInteger.valueOf(1000L));
        statistics.setVideoCount(BigInteger.valueOf(50L));
        statistics.setViewCount(BigInteger.valueOf(100000L));
        channel.setStatistics(statistics);
        channelListResponse.setItems(Collections.singletonList(channel));

        when(youtube.channels()).thenReturn(youtubeChannels);
        when(youtubeChannels.list("snippet,statistics")).thenReturn(channelsList);
        when(channelsList.setId(channelId)).thenReturn(channelsList);
        when(channelsList.setKey(anyString())).thenReturn(channelsList);
        when(channelsList.execute()).thenReturn(channelListResponse);

        Channel result = youTubeService.getChannelProfile(channelId);
        assertNotNull(result);
        assertEquals("Test Channel", result.getSnippet().getTitle());
        assertEquals("Test Channel Description", result.getSnippet().getDescription());
        assertEquals(BigInteger.valueOf(1000L), result.getStatistics().getSubscriberCount());
        assertEquals(BigInteger.valueOf(50L), result.getStatistics().getVideoCount());
        assertEquals(BigInteger.valueOf(100000L), result.getStatistics().getViewCount());
    }
    @Test(expected = IOException.class)
/**
 * This method represents testGetChannelProfile_ChannelNotFound.
 *
 * @return [Description of return value]
 */
    public void testGetChannelProfile_ChannelNotFound() throws IOException {
        String channelId = "nonExistentChannelId";
        ChannelListResponse channelListResponse = new ChannelListResponse();
        channelListResponse.setItems(Collections.emptyList());
        when(youtube.channels()).thenReturn(youtubeChannels);
        when(youtubeChannels.list("snippet,statistics")).thenReturn(channelsList);
        when(channelsList.setId(channelId)).thenReturn(channelsList);
        when(channelsList.setKey(anyString())).thenReturn(channelsList);
        when(channelsList.execute()).thenReturn(channelListResponse);
        youTubeService.getChannelProfile(channelId);
    }
    @Test
/**
 * This method represents testGetLatestVideosByChannel.
 *
 * @return [Description of return value]
 */
    public void testGetLatestVideosByChannel() throws IOException {
        String channelId = "channelId123";
        int limit = 5;
        SearchListResponse searchListResponse = new SearchListResponse();
        List<SearchResult> searchResults = new ArrayList<>();

        for (int i = 0; i < limit; i++) {
            SearchResult searchResult = new SearchResult();
            searchResult.setId(new ResourceId().setVideoId("videoId" + i));
            searchResults.add(searchResult);
        }
        searchListResponse.setItems(searchResults);

        when(youtube.search()).thenReturn(youtubeSearch);
        when(youtubeSearch.list("snippet")).thenReturn(searchList);
        when(searchList.setChannelId(channelId)).thenReturn(searchList);
        when(searchList.setMaxResults((long) limit)).thenReturn(searchList);
        when(searchList.setOrder("date")).thenReturn(searchList);
        when(searchList.setKey(anyString())).thenReturn(searchList);
        when(searchList.execute()).thenReturn(searchListResponse);

        YouTubeService spyService = spy(youTubeService);
        for (int i = 0; i < limit; i++) {
            String videoId = "videoId" + i;
            VideoResult videoResult = new VideoResult(
                    "Title " + i,
                    "Description " + i,
                    videoId,
                    channelId,
                    "http://thumbnail.url/" + i,
                    "Channel Title",
                    Arrays.asList("tag1", "tag2")
            );
            doReturn(videoResult).when(spyService).getVideoDetails(videoId);
        }

        List<VideoResult> results = spyService.getLatestVideosByChannel(channelId, limit);
        assertNotNull(results);
        assertEquals(limit, results.size());
        for (int i = 0; i < limit; i++) {
            VideoResult result = results.get(i);
            assertEquals("Title " + i, result.getTitle());
            assertEquals("Description " + i, result.getDescription());
            assertEquals("videoId" + i, result.getVideoId());
            assertEquals("http://thumbnail.url/" + i, result.getThumbnailUrl());
        }
    }
    @Test
/**
 * This method represents testConstructor_Default.
 *
 * @return [Description of return value]
 */
    public void testConstructor_Default() {
        YouTubeService service = new YouTubeService();
        assertNotNull(service);
    }
    @Test(expected = RuntimeException.class)
/**
 * This method represents testConstructor_Exception.
 *
 * @return [Description of return value]
 */
    public void testConstructor_Exception() {
        try (MockedStatic<GoogleNetHttpTransport> mockedStatic = mockStatic(GoogleNetHttpTransport.class)) {
            mockedStatic.when(GoogleNetHttpTransport::newTrustedTransport).thenThrow(new Exception("Initialization failed"));
            new YouTubeService();
        }
    }
}