import akka.actor.testkit.typed.javadsl.TestKitJunitResource;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.actor.typed.ActorRef;
import org.junit.ClassRule;
import org.junit.Test;
import services.YouTubeService;
import static org.mockito.Mockito.*;

public class TagsActorTest {

    @ClassRule
    public static final TestKitJunitResource testKit = new TestKitJunitResource();

    @Test
    public void testHandleViewVideoDetails() throws Exception {
        YouTubeService mockService = mock(YouTubeService.class);
        TestProbe<Object> probe = testKit.createTestProbe();

        when(mockService.getVideoDetails("videoId")).thenReturn(mock(VideoResult.class));

        ActorRef<Object> tagsActor = testKit.spawn(TagsActor.props(mockService));
        tagsActor.tell(new TagsActor.ViewVideoDetails("videoId"), probe.getRef());

        probe.expectMessageClass(VideoResult.class);
    }
}
