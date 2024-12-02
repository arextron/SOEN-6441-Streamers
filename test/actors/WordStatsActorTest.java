package actors;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.testkit.javadsl.TestKit;
import com.fasterxml.jackson.databind.JsonNode;
import models.VideoResult;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import play.libs.Json;
import services.YouTubeService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class WordStatsActorTest {

    public static ActorSystem system;

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
    public void testComputeWordStats_ValidQuery() {
        new TestKit(system) {{
            YouTubeService mockYouTubeService = mock(YouTubeService.class);
            List<VideoResult> videoResults = Arrays.asList(
                    new VideoResult("Title1", "Description with some words", "videoId1", "channelId1", "channelTitle1", "thumbnailUrl", Collections.emptyList()),
                    new VideoResult("Title2", "Another description with words", "videoId2", "channelId2", "channelTitle2", "thumbnailUrl", Collections.emptyList())
            );

            when(mockYouTubeService.searchVideos("test")).thenReturn(videoResults);

            ActorRef wordStatsActor = system.actorOf(WordStatsActor.props(mockYouTubeService));
            wordStatsActor.tell("test", getRef());

            String response = expectMsgClass(String.class);
            JsonNode jsonResponse = Json.parse(response);

            assertEquals(2, jsonResponse.get("with").asLong());
            assertEquals(2, jsonResponse.get("words").asLong());
            verify(mockYouTubeService, times(1)).searchVideos("test");
        }};
    }

    @Test
    public void testComputeWordStats_NoVideos() {
        new TestKit(system) {{
            YouTubeService mockYouTubeService = mock(YouTubeService.class);
            when(mockYouTubeService.searchVideos("empty")).thenReturn(Collections.emptyList());

            ActorRef wordStatsActor = system.actorOf(WordStatsActor.props(mockYouTubeService));
            wordStatsActor.tell("empty", getRef());

            String response = expectMsgClass(String.class);
            JsonNode jsonResponse = Json.parse(response);

            assertEquals("No word frequency data available for \"empty\"", jsonResponse.get("message").asText());
            verify(mockYouTubeService, times(1)).searchVideos("empty");
        }};
    }

    @Test
    public void testComputeWordStats_ExceptionHandling() {
        new TestKit(system) {{
            YouTubeService mockYouTubeService = mock(YouTubeService.class);
            when(mockYouTubeService.searchVideos("error")).thenThrow(new RuntimeException("Simulated exception"));

            ActorRef wordStatsActor = system.actorOf(WordStatsActor.props(mockYouTubeService));
            wordStatsActor.tell("error", getRef());

            String response = expectMsgClass(String.class);
            JsonNode jsonResponse = Json.parse(response);

            assertEquals("An error occurred while processing your request.", jsonResponse.get("error").asText());
            verify(mockYouTubeService, times(1)).searchVideos("error");
        }};
    }

    @Test
    public void testComputeWordStats_ExactlyFiftyVideos() {
        new TestKit(system) {{
            YouTubeService mockYouTubeService = mock(YouTubeService.class);
            List<VideoResult> videoResults = IntStream.range(0, 50)
                    .mapToObj(i -> new VideoResult(
                            "Title " + i,
                            "Description " + i,
                            "videoId" + i,
                            "channelId" + i,
                            "channelTitle " + i,
                            "thumbnailUrl",
                            Collections.emptyList()
                    ))
                    .collect(Collectors.toList());

            when(mockYouTubeService.searchVideos("query")).thenReturn(videoResults);

            ActorRef wordStatsActor = system.actorOf(WordStatsActor.props(mockYouTubeService));
            wordStatsActor.tell("query", getRef());

            String response = expectMsgClass(String.class);
            JsonNode jsonResponse = Json.parse(response);

            assertEquals(50, jsonResponse.size());
            verify(mockYouTubeService, times(1)).searchVideos("query");
        }};
    }

    @Test
    public void testComputeWordStats_SpecialCharacters() {
        new TestKit(system) {{
            YouTubeService mockYouTubeService = mock(YouTubeService.class);
            List<VideoResult> videoResults = Collections.singletonList(
                    new VideoResult(
                            "Title1",
                            "Special! Characters? like: these# are, filtered.",
                            "videoId1",
                            "channelId1",
                            "channelTitle1",
                            "thumbnailUrl",
                            Collections.emptyList()
                    )
            );

            when(mockYouTubeService.searchVideos("special")).thenReturn(videoResults);

            ActorRef wordStatsActor = system.actorOf(WordStatsActor.props(mockYouTubeService));
            wordStatsActor.tell("special", getRef());

            String response = expectMsgClass(String.class);
            JsonNode jsonResponse = Json.parse(response);

            assertEquals(1, jsonResponse.get("special").asLong());
            assertEquals(1, jsonResponse.get("characters").asLong());
            verify(mockYouTubeService, times(1)).searchVideos("special");
        }};
    }
    @Test
    public void testComputeWordStats_NullDescriptions() {
        new TestKit(system) {{
            YouTubeService mockYouTubeService = mock(YouTubeService.class);
            List<VideoResult> videoResults = Arrays.asList(
                    new VideoResult("Title1", null, "videoId1", "channelId1", "channelTitle1", "thumbnailUrl", Collections.emptyList()),
                    new VideoResult("Title2", "Valid description", "videoId2", "channelId2", "channelTitle2", "thumbnailUrl", Collections.emptyList())
            );

            when(mockYouTubeService.searchVideos("null_desc")).thenReturn(videoResults);

            ActorRef wordStatsActor = system.actorOf(WordStatsActor.props(mockYouTubeService));
            wordStatsActor.tell("null_desc", getRef());

            String response = expectMsgClass(String.class);
            JsonNode jsonResponse = Json.parse(response);

            assertEquals(1, jsonResponse.get("valid").asLong());
            assertEquals(1, jsonResponse.get("description").asLong());
            verify(mockYouTubeService, times(1)).searchVideos("null_desc");
        }};
    }
    @Test
    public void testComputeWordStats_EmptyQuery() {
        new TestKit(system) {{
            YouTubeService mockYouTubeService = mock(YouTubeService.class);
            when(mockYouTubeService.searchVideos("")).thenReturn(Collections.emptyList());

            ActorRef wordStatsActor = system.actorOf(WordStatsActor.props(mockYouTubeService));
            wordStatsActor.tell("", getRef());

            String response = expectMsgClass(String.class);
            JsonNode jsonResponse = Json.parse(response);

            assertEquals("No word frequency data available for \"\"", jsonResponse.get("message").asText());
            verify(mockYouTubeService, times(1)).searchVideos("");
        }};
    }



}
