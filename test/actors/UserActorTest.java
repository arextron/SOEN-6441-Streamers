package actors;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.testkit.javadsl.TestKit;
import com.fasterxml.jackson.databind.JsonNode;
import models.VideoResult;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import play.libs.Json;
import services.YouTubeService;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;

public class UserActorTest {
    private static ActorSystem system;

    @BeforeClass
    public static void setup() {
        system = ActorSystem.create();
    }

    @AfterClass
    public static void teardown() {
        TestKit.shutdownActorSystem(system);
        system = null;
    }

    @Test
    public void testInitialFetch() {
        new TestKit(system) {{
            // Mock YouTubeService
            YouTubeService mockService = Mockito.mock(YouTubeService.class);
            List<VideoResult> mockResults = Arrays.asList(
                    new VideoResult("Title 1", "URL 1", "Channel 1", "Description 1", "Date 1", "Thumbnail 1", Arrays.asList("Tag1", "Tag2")),
                    new VideoResult("Title 2", "URL 2", "Channel 2", "Description 2", "Date 2", "Thumbnail 2", Arrays.asList("Tag3", "Tag4"))
            );
            when(mockService.searchVideos("testQuery")).thenReturn(mockResults);

            // Create the actor
            ActorRef userActor = system.actorOf(UserActor.props(getRef(), mockService));

            // Send a query
            userActor.tell("testQuery", getRef());

            // Expect initial video results
            String response = expectMsgClass(String.class);
            JsonNode jsonResponse = Json.parse(response);
            assert jsonResponse.size() == 2;
            assert jsonResponse.get(0).get("title").asText().equals("Title 1");
            assert jsonResponse.get(1).get("title").asText().equals("Title 2");
        }};
    }

    @Test
    public void testPeriodicFetch() {
        new TestKit(system) {{
            // Mock YouTubeService
            YouTubeService mockService = Mockito.mock(YouTubeService.class);
            List<VideoResult> initialResults = Arrays.asList(
                    new VideoResult("Title 1", "URL 1", "Channel 1", "Description 1", "Date 1", "Thumbnail 1", Arrays.asList("Tag1", "Tag2"))
            );
            List<VideoResult> updatedResults = Arrays.asList(
                    new VideoResult("Updated Title", "Updated URL", "Updated Channel", "Updated Description", "Updated Date", "Updated Thumbnail", Arrays.asList("Tag3", "Tag4"))
            );
            when(mockService.searchVideos("testQuery")).thenReturn(initialResults, updatedResults);

            // Create the actor
            ActorRef userActor = system.actorOf(UserActor.props(getRef(), mockService));

            // Send a query
            userActor.tell("testQuery", getRef());

            // Expect initial results
            String initialResponse = expectMsgClass(String.class);
            JsonNode initialJson = Json.parse(initialResponse);
            assert initialJson.size() == 1;
            assert initialJson.get(0).get("title").asText().equals("Title 1");

            // Wait for periodic update
            awaitAssert(() -> {
                String periodicResponse = expectMsgClass(String.class);
                JsonNode periodicJson = Json.parse(periodicResponse);
                assert periodicJson.size() == 1;
                assert periodicJson.get(0).get("title").asText().equals("Updated Title");
                return null;
            });
        }};
    }
}
