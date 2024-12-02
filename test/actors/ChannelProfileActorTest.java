
package actors;

import actors.ChannelProfileActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Status;
import akka.testkit.TestKit;
import akka.testkit.TestProbe;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.api.services.youtube.model.Channel;
import models.VideoResult;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import play.libs.Json;
import scala.concurrent.duration.Duration;
import services.YouTubeService;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class ChannelProfileActorTest {

    private ActorSystem system;
    private YouTubeService mockYouTubeService;

    @Before
    public void setUp() {
        system = ActorSystem.create("testActorSystem");
        mockYouTubeService = mock(YouTubeService.class);
    }

    @After
    public void tearDown() {
        TestKit.shutdownActorSystem(system, Duration.create(3, "seconds"), true);
    }

    @Test
    public void testFetchChannelProfile_Success() throws IOException {
        String channelId = "validChannelId";

        // Mocking data
        Channel mockChannel = mock(Channel.class);
        List<VideoResult> mockVideos = Arrays.asList(
                new VideoResult("Video 1", "Description 1", "videoId1", channelId, "thumbnailUrl1", "Channel Name", null),
                new VideoResult("Video 2", "Description 2", "videoId2", channelId, "thumbnailUrl2", "Channel Name", null)
        );

        when(mockYouTubeService.getChannelProfile(channelId)).thenReturn(mockChannel);
        when(mockYouTubeService.getLatestVideosByChannel(channelId, 10)).thenReturn(mockVideos);

        // Create TestProbe
        TestProbe probe = new TestProbe(system);

        // Create the actor
        ActorRef channelProfileActor = system.actorOf(ChannelProfileActor.props(channelId, probe.ref(), mockYouTubeService));

        // Expect a response
        String responseJson = probe.expectMsgClass(String.class);

        // Verify the response
        JsonNode response = Json.parse(responseJson);
        assertTrue(response.has("channel"));
        assertTrue(response.has("videos"));

        verify(mockYouTubeService, times(1)).getChannelProfile(channelId);
        verify(mockYouTubeService, times(1)).getLatestVideosByChannel(channelId, 10);
    }

    @Test
    public void testFetchChannelProfile_ChannelNotFound() throws IOException {
        String channelId = "invalidChannelId";

        // Simulate exception for channel not found
        when(mockYouTubeService.getChannelProfile(channelId)).thenThrow(new IOException("Channel not found"));

        // Create TestProbe
        TestProbe probe = new TestProbe(system);

        // Create the actor
        ActorRef channelProfileActor = system.actorOf(ChannelProfileActor.props(channelId, probe.ref(), mockYouTubeService));

        // Expect an error response
        String errorResponse = probe.expectMsgClass(String.class);
        JsonNode errorJson = Json.parse(errorResponse);
        assertTrue(errorJson.has("error"));
        assertTrue(errorJson.get("error").asText().contains("Unable to fetch channel information"));

        verify(mockYouTubeService, times(1)).getChannelProfile(channelId);
    }

    @Test
    public void testFetchChannelProfile_VideosFetchError() throws IOException {
        String channelId = "errorChannelId";

        // Simulate successful channel fetch but exception on videos fetch
        Channel mockChannel = mock(Channel.class);
        when(mockYouTubeService.getChannelProfile(channelId)).thenReturn(mockChannel);
        when(mockYouTubeService.getLatestVideosByChannel(channelId, 10)).thenThrow(new IOException("Video fetch error"));

        // Create TestProbe
        TestProbe probe = new TestProbe(system);

        // Create the actor
        ActorRef channelProfileActor = system.actorOf(ChannelProfileActor.props(channelId, probe.ref(), mockYouTubeService));

        // Expect an error response
        String errorResponse = probe.expectMsgClass(String.class);
        JsonNode errorJson = Json.parse(errorResponse);
        assertTrue(errorJson.has("error"));
        assertTrue(errorJson.get("error").asText().contains("Unable to fetch channel information"));

        verify(mockYouTubeService, times(1)).getChannelProfile(channelId);
        verify(mockYouTubeService, times(1)).getLatestVideosByChannel(channelId, 10);
    }

    @Test
    public void testConstructorWithImmediateFetch_Success() throws IOException {
        String channelId = "constructorChannel";

        // Mocking data
        Channel mockChannel = mock(Channel.class);
        List<VideoResult> mockVideos = Arrays.asList(
                new VideoResult("Video 1", "Description 1", "videoId1", channelId, "thumbnailUrl1", "Channel Name", null),
                new VideoResult("Video 2", "Description 2", "videoId2", channelId, "thumbnailUrl2", "Channel Name", null)
        );

        when(mockYouTubeService.getChannelProfile(channelId)).thenReturn(mockChannel);
        when(mockYouTubeService.getLatestVideosByChannel(channelId, 10)).thenReturn(mockVideos);

        // Create TestProbe
        TestProbe probe = new TestProbe(system);

        // Create the actor
        system.actorOf(ChannelProfileActor.props(channelId, probe.ref(), mockYouTubeService));

        // Expect the immediate constructor response
        String responseJson = probe.expectMsgClass(String.class);

        // Verify the response
        JsonNode response = Json.parse(responseJson);
        assertTrue(response.has("channel"));
        assertTrue(response.has("videos"));
    }

    @Test
    public void testFetchChannelProfile_ExceptionDuringChannelFetch() throws IOException {
        String channelId = "exceptionChannelId";

        // Simulate an exception during the channel profile fetch
        when(mockYouTubeService.getChannelProfile(channelId)).thenThrow(new IOException("Simulated exception during channel fetch"));

        // Create TestProbe to simulate the sender
        TestProbe probe = new TestProbe(system);

        // Create the actor
        ActorRef channelProfileActor = system.actorOf(ChannelProfileActor.props(channelId, probe.ref(), mockYouTubeService));

        // Expect an error response
        String errorResponse = probe.expectMsgClass(String.class);

        // Verify the error response content
        JsonNode errorJson = Json.parse(errorResponse);
        assertTrue(errorJson.has("error"));
        assertTrue(errorJson.get("error").asText().contains("Unable to fetch channel information"));

        // Verify that the exception was triggered and handled
        verify(mockYouTubeService, times(1)).getChannelProfile(channelId);
    }

    @Test
    public void testFetchChannelProfile_ExceptionDuringVideoFetch() throws IOException {
        String channelId = "exceptionChannelId";

        // Simulate successful channel fetch but exception during video fetch
        Channel mockChannel = mock(Channel.class);
        when(mockYouTubeService.getChannelProfile(channelId)).thenReturn(mockChannel);
        when(mockYouTubeService.getLatestVideosByChannel(channelId, 10)).thenThrow(new IOException("Simulated exception during video fetch"));

        // Create TestProbe to simulate the sender
        TestProbe probe = new TestProbe(system);

        // Create the actor
        ActorRef channelProfileActor = system.actorOf(ChannelProfileActor.props(channelId, probe.ref(), mockYouTubeService));

        // Expect an error response
        String errorResponse = probe.expectMsgClass(String.class);

        // Verify the error response content
        JsonNode errorJson = Json.parse(errorResponse);
        assertTrue(errorJson.has("error"));
        assertTrue(errorJson.get("error").asText().contains("Unable to fetch channel information"));

        // Verify that both methods were called
        verify(mockYouTubeService, times(1)).getChannelProfile(channelId);
        verify(mockYouTubeService, times(1)).getLatestVideosByChannel(channelId, 10);
    }


}
