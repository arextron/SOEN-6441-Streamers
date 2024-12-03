package java.actors;

import actors.UserActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.javadsl.TestKit;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import services.YouTubeService;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

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
    public void testInitialQueryFetch() {
        new TestKit(system) {{
            YouTubeService mockYouTubeService = Mockito.mock(YouTubeService.class);
            when(mockYouTubeService.searchVideos(anyString())).thenReturn(Collections.emptyList());

            ActorRef userActor = system.actorOf(UserActor.props(getRef(), mockYouTubeService));

            String query = "testQuery";
            userActor.tell(query, getRef());

            expectMsg("[]"); // Expect empty JSON array as string
        }};
    }

    @Test
    public void testPeriodicUpdates() {
        new TestKit(system) {{
            YouTubeService mockYouTubeService = Mockito.mock(YouTubeService.class);
            when(mockYouTubeService.searchVideos(anyString())).thenReturn(Collections.emptyList());

            ActorRef userActor = system.actorOf(UserActor.props(getRef(), mockYouTubeService));

            String query = "testQuery";
            userActor.tell(query, getRef());

            // Wait for periodic updates (simulate waiting time if needed)
            expectNoMessage(); // As updates are mocked as empty
        }};
    }
}
