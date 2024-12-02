package actors;

import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.javadsl.TestKit;
import models.VideoResult;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import services.YouTubeService;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

public class TagsActorTest {

    static ActorSystem system;
    static YouTubeService mockYouTubeService;

    @BeforeClass
    public static void setup() {
        system = ActorSystem.create();
        mockYouTubeService = mock(YouTubeService.class);
    }

    @AfterClass
    public static void teardown() {
        TestKit.shutdownActorSystem(system);
        system = null;
    }

    @Test
    public void testHandleViewVideoDetails_Success() {
        new TestKit(system) {{
            String videoId = "testVideoId";
            VideoResult mockVideo = new VideoResult("Title", "Description", "Channel", "URL", "Date", "Thumbnail", new ArrayList<>());

            when(mockYouTubeService.getVideoDetails(videoId)).thenReturn(mockVideo);

            Props props = TagsActor.props(mockYouTubeService);
            final var tagsActor = system.actorOf(props);

            tagsActor.tell(new TagsActor.ViewVideoDetails(videoId), getRef());

            expectMsg(mockVideo);
            verify(mockYouTubeService, times(1)).getVideoDetails(videoId);
        }};
    }

    @Test
    public void testHandleViewVideoDetails_VideoNotFound() {
        new TestKit(system) {{
            String videoId = "nonexistentVideoId";

            when(mockYouTubeService.getVideoDetails(videoId)).thenReturn(null);

            Props props = TagsActor.props(mockYouTubeService);
            final var tagsActor = system.actorOf(props);

            tagsActor.tell(new TagsActor.ViewVideoDetails(videoId), getRef());

            TagsActor.ErrorMessage error = expectMsgClass(TagsActor.ErrorMessage.class);
            assert error.message.equals("Video not found");
            verify(mockYouTubeService, times(1)).getVideoDetails(videoId);
        }};
    }

    @Test
    public void testHandleViewVideoDetails_Exception() {
        new TestKit(system) {{
            String videoId = "exceptionVideoId";

            when(mockYouTubeService.getVideoDetails(videoId)).thenThrow(new RuntimeException("Service Error"));

            Props props = TagsActor.props(mockYouTubeService);
            final var tagsActor = system.actorOf(props);

            tagsActor.tell(new TagsActor.ViewVideoDetails(videoId), getRef());

            TagsActor.ErrorMessage error = expectMsgClass(TagsActor.ErrorMessage.class);
            assert error.message.equals("Error fetching video details");
            verify(mockYouTubeService, times(1)).getVideoDetails(videoId);
        }};
    }
    @Test
    public void testHandleViewTags_Success() {
        new TestKit(system) {{
            // Arrange
            String query = "testQuery";
            List<VideoResult> mockVideos = new ArrayList<>();
            mockVideos.add(new VideoResult("Title1", "Description1", "Channel1", "URL1", "Date1", "Thumbnail1", new ArrayList<>()));
            mockVideos.add(new VideoResult("Title2", "Description2", "Channel2", "URL2", "Date2", "Thumbnail2", new ArrayList<>()));

            // Mock the YouTubeService behavior
            when(mockYouTubeService.searchVideos(query)).thenReturn(mockVideos);

            // Create the TagsActor
            Props props = TagsActor.props(mockYouTubeService);
            final var tagsActor = system.actorOf(props);

            // Act
            tagsActor.tell(new TagsActor.ViewTags(query), getRef());

            // Assert
            expectMsg(mockVideos);
            verify(mockYouTubeService, times(1)).searchVideos(query);
        }};
    }


    @Test
    public void testHandleViewTags_Exception() {
        new TestKit(system) {{
            String query = "exceptionQuery";

            when(mockYouTubeService.searchVideos(query)).thenThrow(new RuntimeException("Service Error"));

            Props props = TagsActor.props(mockYouTubeService);
            final var tagsActor = system.actorOf(props);

            tagsActor.tell(new TagsActor.ViewTags(query), getRef());

            TagsActor.ErrorMessage error = expectMsgClass(TagsActor.ErrorMessage.class);
            assert error.message.equals("Error fetching videos by query");
            verify(mockYouTubeService, times(1)).searchVideos(query);
        }};
    }

    @Test
    public void testHandleSearchByTag_Success() {
        new TestKit(system) {{
            String tag = "testTag";
            List<VideoResult> mockVideos = new ArrayList<>();
            mockVideos.add(new VideoResult("Title1", "Description1", "Channel1", "URL1", "Date1", "Thumbnail1", new ArrayList<>()));
            mockVideos.add(new VideoResult("Title2", "Description2", "Channel2", "URL2", "Date2", "Thumbnail2", new ArrayList<>()));

            when(mockYouTubeService.searchVideosByTag(tag)).thenReturn(mockVideos);

            Props props = TagsActor.props(mockYouTubeService);
            final var tagsActor = system.actorOf(props);

            tagsActor.tell(new TagsActor.SearchByTag(tag), getRef());

            expectMsg(mockVideos);
            verify(mockYouTubeService, times(1)).searchVideosByTag(tag);
        }};
    }

    @Test
    public void testHandleSearchByTag_Exception() {
        new TestKit(system) {{
            String tag = "exceptionTag";

            when(mockYouTubeService.searchVideosByTag(tag)).thenThrow(new RuntimeException("Service Error"));

            Props props = TagsActor.props(mockYouTubeService);
            final var tagsActor = system.actorOf(props);

            tagsActor.tell(new TagsActor.SearchByTag(tag), getRef());

            TagsActor.ErrorMessage error = expectMsgClass(TagsActor.ErrorMessage.class);
            assert error.message.equals("Error fetching videos by tag");
            verify(mockYouTubeService, times(1)).searchVideosByTag(tag);
        }};
    }
}
