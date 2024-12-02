import actors.WordStatsActor;
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
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

public class WordStatsActorTest {

    private static ActorSystem system;
    private static YouTubeService mockYouTubeService;

    @BeforeClass
    public static void setup() {
        system = ActorSystem.create();
        mockYouTubeService = Mockito.mock(YouTubeService.class);
    }

    @AfterClass
    public static void teardown() {
        TestKit.shutdownActorSystem(system);
        system = null;
    }

    @Test
    public void testComputeWordStats_NoVideos() {
        new TestKit(system) {{
            when(mockYouTubeService.searchVideos("query")).thenReturn(Collections.emptyList());

            final ActorRef wordStatsActor = system.actorOf(WordStatsActor.props(mockYouTubeService));
            wordStatsActor.tell("query", getRef());

            String expectedMessage = Json.newObject()
                    .put("message", "No word frequency data available for \"query\"").toString();
            expectMsg(expectedMessage);

            verify(mockYouTubeService, times(1)).searchVideos("query");
        }};
    }

    @Test
    public void testComputeWordStats_WithVideos() {
        new TestKit(system) {{
            VideoResult video1 = new VideoResult("Title1", "This is a sample description.");
            VideoResult video2 = new VideoResult("Title2", "Another description with sample words.");

            List<VideoResult> mockResults = Arrays.asList(video1, video2);
            when(mockYouTubeService.searchVideos("query")).thenReturn(mockResults);

            final ActorRef wordStatsActor = system.actorOf(WordStatsActor.props(mockYouTubeService));
            wordStatsActor.tell("query", getRef());

            String response = expectMsgClass(String.class);
            JsonNode responseJson = Json.parse(response);

            assert responseJson.has("sample");
            assert responseJson.get("sample").asLong() == 2;

            verify(mockYouTubeService, times(1)).searchVideos("query");
        }};
    }
}
