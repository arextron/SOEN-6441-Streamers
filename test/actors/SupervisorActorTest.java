package actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.javadsl.TestKit;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import services.YouTubeService;

import static org.mockito.Mockito.mock;
import static org.junit.Assert.assertNotNull;

public class SupervisorActorTest {

    static ActorSystem system;

    @BeforeClass
    public static void setup() {
        system = ActorSystem.create("SupervisorActorTestSystem");
    }

    @AfterClass
    public static void teardown() {
        TestKit.shutdownActorSystem(system);
        system = null;
    }

    @Test
    public void testCreateChildActor_Success() {
        new TestKit(system) {{
            // Mock the YouTubeService
            YouTubeService mockYouTubeService = mock(YouTubeService.class);

            // Create the SupervisorActor
            ActorRef supervisor = system.actorOf(Props.create(SupervisorActor.class, mockYouTubeService));

            // Create a dummy Props to create a child actor
            Props childProps = Props.create(DummyActor.class);

            // Send the Props to the supervisor to create a child actor
            supervisor.tell(childProps, getRef());

            // Expect the child actor reference as a reply
            ActorRef childActorRef = expectMsgClass(ActorRef.class);

            // Assert the child actor is created successfully
            assertNotNull("Child actor should not be null", childActorRef);
        }};
    }

    // DummyActor for testing purposes
    public static class DummyActor extends AbstractActor {
        @Override
        public Receive createReceive() {
            return receiveBuilder().build();
        }
    }
}
