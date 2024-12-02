import akka.actor.testkit.typed.javadsl.TestKitJunitResource;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.actor.typed.ActorRef;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.ClassRule;
import org.junit.Test;
import services.YouTubeService;
import static org.mockito.Mockito.*;

public class ChannelProfileActorTest {

    @ClassRule
    public static final TestKitJunitResource testKit = new TestKitJunitResource();

    @Test
    public void testFetchChannelProfile() throws Exception {
        YouTubeService mockService = mock(YouTubeService.class);
        TestProbe<String> probe = testKit.createTestProbe();

        String channelId = "test-channel-id";

        when(mockService.getChannelProfile(channelId)).thenReturn(mock(Channel.class));
        when(mockService.getLast10Videos(channelId)).thenReturn(List.of());

        ActorRef<String> channelActor = testKit.spawn(ChannelProfileActor.props(channelId, probe.getRef(), mockService));

        channelActor.tell("fetchChannelProfile");

        probe.expectMessageClass(JsonNode.class);
    }
}
