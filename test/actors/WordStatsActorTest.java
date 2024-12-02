import akka.actor.testkit.typed.javadsl.TestKitJunitResource;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.actor.typed.ActorRef;
import org.junit.ClassRule;
import org.junit.Test;
import services.YouTubeService;
import static org.mockito.Mockito.*;

public class WordStatsActorTest {

    @ClassRule
    public static final TestKitJunitResource testKit = new TestKitJunitResource();

    @Test
    public void testComputeWordStats() throws Exception {
        YouTubeService mockService = mock(YouTubeService.class);
        TestProbe<Object> probe = testKit.createTestProbe();

        when(mockService.searchVideos("query")).thenReturn(List.of());

        ActorRef<Object> wordStatsActor = testKit.spawn(WordStatsActor.props(mockService));
        wordStatsActor.tell("query", probe.getRef());

        probe.expectMessageClass(String.class);
    }
}
