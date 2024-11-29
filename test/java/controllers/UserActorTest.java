package java.controllers;

import actors.UserActor;
import akka.actor.ActorRef;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UserActorTest extends TestKit {

    static ActorSystem system;

    @BeforeClass
    public static void setup() {
        system = ActorSystem.create();
    }

    @AfterClass
    public static void teardown() {
        TestKit.shutdownActorSystem(system);
        system = null;
    }

    public UserActorTest() {
        super(system);
    }

    @Test
    public void testHandleSearchQuery() {
        final TestProbe probe = new TestProbe(system);
        YouTubeService mockService = mock(YouTubeService.class);
        when(mockService.searchVideos(anyString())).thenReturn(new ArrayList<>());

        final ActorRef userActor = system.actorOf(UserActor.props(mockService));

        userActor.tell("test query", probe.ref());

        // Verify that the actor sends the expected messages
        probe.expectMsgClass(String.class);
    }
}
