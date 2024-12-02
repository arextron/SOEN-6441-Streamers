import akka.actor.testkit.typed.javadsl.TestKitJunitResource;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.actor.typed.ActorRef;
import org.junit.ClassRule;
import org.junit.Test;
import services.YouTubeService;
import static org.mockito.Mockito.mock;

public class SupervisorActorTest {

    @ClassRule
    public static final TestKitJunitResource testKit = new TestKitJunitResource();

    @Test
    public void testSupervisorCreatesChildActor() {
        YouTubeService mockService = mock(YouTubeService.class);
        TestProbe<ActorRef> probe = testKit.createTestProbe();

        ActorRef<ActorRef> supervisor = testKit.spawn(SupervisorActor.props(mockService));
        supervisor.tell(ChildActor.props(), probe.getRef());

        probe.expectMessageClass(ActorRef.class);
    }
}
