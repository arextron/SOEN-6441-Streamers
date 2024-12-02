package java.actors;

import actors.TagsActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.javadsl.TestKit;
import models.VideoResult;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import services.YouTubeService;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class TagsActorTest {

    private ActorSystem system;

    @BeforeEach
    public void setup() {
        system = ActorSystem.create("TestSystem");
    }

    @AfterEach
    public void teardown() {
        TestKit.shutdownActorSystem(system);
    }

    @Test
    public void testViewVideoDetails() {
        new TestKit(system) {{
            // Mock the YouTubeService
            YouTubeService mockYouTubeService = mock(YouTubeService.class);

            // Mock video result
            VideoResult mockVideo = new VideoResult(
                    "videoId",
                    "Video Title",
                    "Description",
                    "thumbnailUrl",
                    "channelId",
                    "channelTitle",
                    Arrays.asList("tag1", "tag2")
            );
            when(mockYouTubeService.getVideoDetails("videoId")).thenReturn(mockVideo);

            // Create the actor
            ActorRef actor = system.actorOf(TagsActor.props(mockYouTubeService));

            // Send ViewVideoDetails message
            TagsActor.ViewVideoDetails request = new TagsActor.ViewVideoDetails("videoId");
            actor.tell(request, getTestActor());

            // Expect the video result
            VideoResult result = expectMsgClass(VideoResult.class);
            assertEquals("videoId", result.getVideoId());
            assertEquals("Video Title", result.getTitle());
        }};
    }

    @Test
    public void testViewTags() {
        new TestKit(system) {{
            // Mock the YouTubeService
            YouTubeService mockYouTubeService = mock(YouTubeService.class);

            // Mock video results
            List<VideoResult> mockVideos = Arrays.asList(
                    new VideoResult(
                            "id1",
                            "Title 1",
                            "Description 1",
                            "thumbnail1",
                            "channelId1",
                            "channelTitle1",
                            Arrays.asList("tag1", "tag2")
                    ),
                    new VideoResult(
                            "id2",
                            "Title 2",
                            "Description 2",
                            "thumbnail2",
                            "channelId2",
                            "channelTitle2",
                            Arrays.asList("tag3", "tag4")
                    )
            );
            when(mockYouTubeService.searchVideos("testQuery")).thenReturn(mockVideos);

            // Create the actor
            ActorRef actor = system.actorOf(TagsActor.props(mockYouTubeService));

            // Send ViewTags message
            TagsActor.ViewTags request = new TagsActor.ViewTags("testQuery");
            actor.tell(request, getTestActor());

            // Expect the video list
            List<?> result = expectMsgClass(List.class);
            assertEquals(2, result.size());
        }};
    }

    @Test
    public void testSearchByTag() {
        new TestKit(system) {{
            // Mock the YouTubeService
            YouTubeService mockYouTubeService = mock(YouTubeService.class);

            // Mock video results
            List<VideoResult> mockVideos = Collections.singletonList(
                    new VideoResult(
                            "id1",
                            "Title 1",
                            "Description 1",
                            "thumbnail1",
                            "channelId1",
                            "channelTitle1",
                            Arrays.asList("tag1", "tag2")
                    )
            );
            when(mockYouTubeService.searchVideosByTag("testTag")).thenReturn(mockVideos);

            // Create the actor
            ActorRef actor = system.actorOf(TagsActor.props(mockYouTubeService));

            // Send SearchByTag message
            TagsActor.SearchByTag request = new TagsActor.SearchByTag("testTag");
            actor.tell(request, getTestActor());

            // Expect the video list
            List<?> result = expectMsgClass(List.class);
            assertEquals(1, result.size());
        }};
    }

    @Test
    public void testErrorHandlingInViewVideoDetails() {
        new TestKit(system) {{
            // Mock the YouTubeService
            YouTubeService mockYouTubeService = mock(YouTubeService.class);

            // Simulate an exception
            when(mockYouTubeService.getVideoDetails("videoId")).thenThrow(new RuntimeException("Simulated error"));

            // Create the actor
            ActorRef actor = system.actorOf(TagsActor.props(mockYouTubeService));

            // Send ViewVideoDetails message
            TagsActor.ViewVideoDetails request = new TagsActor.ViewVideoDetails("videoId");
            actor.tell(request, getTestActor());

            // Expect an error message
            TagsActor.ErrorMessage error = expectMsgClass(TagsActor.ErrorMessage.class);
            assertEquals("Error fetching video details", error.message);
        }};
    }
}