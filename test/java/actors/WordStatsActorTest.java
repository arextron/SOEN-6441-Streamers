package java.actors;

import actors.WordStatsActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.javadsl.TestKit;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import play.libs.Json;
import services.YouTubeService;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class WordStatsActorTest {

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
    public void testComputeWordStats() {
        new TestKit(system) {{
            YouTubeService mockYouTubeService = Mockito.mock(YouTubeService.class);
            when(mockYouTubeService.searchVideos(anyString())).thenReturn(Collections.emptyList());

            ActorRef wordStatsActor = system.actorOf(WordStatsActor.props(mockYouTubeService));

            String query = "testQuery";
            wordStatsActor.tell(query, getRef());

            String expectedMessage = Json.newObject().put("message", "No word frequency data available for \"testQuery\"").toString();
            expectMsg(expectedMessage);
        }};
    }

    @Test
    public void testErrorDuringProcessing() {
        new TestKit(system) {{
            YouTubeService mockYouTubeService = Mockito.mock(YouTubeService.class);
            when(mockYouTubeService.searchVideos(anyString())).thenThrow(new RuntimeException("API error"));

            ActorRef wordStatsActor = system.actorOf(WordStatsActor.props(mockYouTubeService));

            String query = "testQuery";
            wordStatsActor.tell(query, getRef());

            String expectedError = Json.newObject().put("error", "An error occurred while processing your request.").toString();
            expectMsg(expectedError);
        }};
    }

    @Test
    public void testWordStatsComputation() {
        new TestKit(system) {{
            YouTubeService mockYouTubeService = Mockito.mock(YouTubeService.class);
            Map<String, Long> wordFrequency = new HashMap<>();
            wordFrequency.put("test", 5L);
            wordFrequency.put("query", 3L);

            when(mockYouTubeService.searchVideos(anyString())).thenReturn(Collections.emptyList());

            ActorRef wordStatsActor = system.actorOf(WordStatsActor.props(mockYouTubeService));

            String query = "testQuery";
            wordStatsActor.tell(query, getRef());

            Map<String, Long> expectedFrequency = new HashMap<>();
            expectedFrequency.put("test", 5L);
            expectedFrequency.put("query", 3L);

            expectMsg(Json.toJson(expectedFrequency).toString());
        }};
    }
}
