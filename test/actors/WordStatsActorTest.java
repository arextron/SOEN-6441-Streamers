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

import static org.junit.Assert.*;
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
    @Test
    public void testFilterVideosWithEmptyDescriptions() {
        new TestKit(system) {{
            YouTubeService mockYouTubeService = mock(YouTubeService.class);
            List<VideoResult> videoResults = Arrays.asList(
                    new VideoResult("Title1", "", "videoId1", "channelId1", "channelTitle1", "thumbnailUrl", Collections.emptyList()),
                    new VideoResult("Title2", "Valid description", "videoId2", "channelId2", "channelTitle2", "thumbnailUrl", Collections.emptyList())
            );

            when(mockYouTubeService.searchVideos("filter_empty")).thenReturn(videoResults);

            ActorRef wordStatsActor = system.actorOf(WordStatsActor.props(mockYouTubeService));
            wordStatsActor.tell("filter_empty", getRef());

            String response = expectMsgClass(String.class);
            JsonNode jsonResponse = Json.parse(response);

            assertEquals(1, jsonResponse.get("valid").asLong());
            assertEquals(1, jsonResponse.get("description").asLong());
            verify(mockYouTubeService, times(1)).searchVideos("filter_empty");
        }};
    }
    @Test
    public void testLimitToFiftyVideos() {
        new TestKit(system) {{
            YouTubeService mockYouTubeService = mock(YouTubeService.class);
            List<VideoResult> videoResults = IntStream.range(0, 100)
                    .mapToObj(i -> new VideoResult(
                            "Title" + i,
                            "Description " + i,
                            "videoId" + i,
                            "channelId" + i,
                            "channelTitle " + i,
                            "thumbnailUrl",
                            Collections.emptyList()
                    ))
                    .collect(Collectors.toList());

            when(mockYouTubeService.searchVideos("limit_50")).thenReturn(videoResults);

            ActorRef wordStatsActor = system.actorOf(WordStatsActor.props(mockYouTubeService));
            wordStatsActor.tell("limit_50", getRef());

            String response = expectMsgClass(String.class);
            JsonNode jsonResponse = Json.parse(response);

            assertTrue(jsonResponse.size() <= 50);
            verify(mockYouTubeService, times(1)).searchVideos("limit_50");
        }};
    }
    @Test
    public void testSplitDescriptionsIntoWords() {
        new TestKit(system) {{
            YouTubeService mockYouTubeService = mock(YouTubeService.class);
            List<VideoResult> videoResults = Collections.singletonList(
                    new VideoResult(
                            "Title1",
                            "Word1, Word2; Word3!?",
                            "videoId1",
                            "channelId1",
                            "channelTitle1",
                            "thumbnailUrl",
                            Collections.emptyList()
                    )
            );

            when(mockYouTubeService.searchVideos("split_words")).thenReturn(videoResults);

            ActorRef wordStatsActor = system.actorOf(WordStatsActor.props(mockYouTubeService));
            wordStatsActor.tell("split_words", getRef());

            String response = expectMsgClass(String.class);
            JsonNode jsonResponse = Json.parse(response);

            assertEquals(1, jsonResponse.get("word1").asLong());
            assertEquals(1, jsonResponse.get("word2").asLong());
            assertEquals(1, jsonResponse.get("word3").asLong());
            verify(mockYouTubeService, times(1)).searchVideos("split_words");
        }};
    }
    @Test
    public void testExceptionHandling() {
        new TestKit(system) {{
            YouTubeService mockYouTubeService = mock(YouTubeService.class);
            when(mockYouTubeService.searchVideos("exception")).thenThrow(new RuntimeException("Test exception"));

            ActorRef wordStatsActor = system.actorOf(WordStatsActor.props(mockYouTubeService));
            wordStatsActor.tell("exception", getRef());

            String response = expectMsgClass(String.class);
            JsonNode jsonResponse = Json.parse(response);

            assertEquals("An error occurred while processing your request.", jsonResponse.get("error").asText());
            verify(mockYouTubeService, times(1)).searchVideos("exception");
        }};
    }
    @Test
    public void testFilterEmptyWords() {
        new TestKit(system) {{
            YouTubeService mockYouTubeService = mock(YouTubeService.class);
            List<VideoResult> videoResults = Collections.singletonList(
                    new VideoResult(
                            "Title1",
                            "   !! !",
                            "videoId1",
                            "channelId1",
                            "channelTitle1",
                            "thumbnailUrl",
                            Collections.emptyList()
                    )
            );

            when(mockYouTubeService.searchVideos("empty_words")).thenReturn(videoResults);

            ActorRef wordStatsActor = system.actorOf(WordStatsActor.props(mockYouTubeService));
            wordStatsActor.tell("empty_words", getRef());

            String response = expectMsgClass(String.class);
            JsonNode jsonResponse = Json.parse(response);

            assertTrue(jsonResponse.isEmpty());
            verify(mockYouTubeService, times(1)).searchVideos("empty_words");
        }};
    }
    @Test
    public void testSortingWithTiedFrequencies() {
        new TestKit(system) {{
            YouTubeService mockYouTubeService = mock(YouTubeService.class);
            List<VideoResult> videoResults = Collections.singletonList(
                    new VideoResult(
                            "Title1",
                            "word word tie tie",
                            "videoId1",
                            "channelId1",
                            "channelTitle1",
                            "thumbnailUrl",
                            Collections.emptyList()
                    )
            );

            when(mockYouTubeService.searchVideos("ties")).thenReturn(videoResults);

            ActorRef wordStatsActor = system.actorOf(WordStatsActor.props(mockYouTubeService));
            wordStatsActor.tell("ties", getRef());

            String response = expectMsgClass(String.class);
            JsonNode jsonResponse = Json.parse(response);

            assertEquals(2, jsonResponse.get("word").asLong());
            assertEquals(2, jsonResponse.get("tie").asLong());
            verify(mockYouTubeService, times(1)).searchVideos("ties");
        }};
    }
    @Test
    public void testHandleRuntimeException() {
        new TestKit(system) {{
            YouTubeService mockYouTubeService = mock(YouTubeService.class);
            when(mockYouTubeService.searchVideos("runtime_exception"))
                    .thenThrow(new RuntimeException("Simulated runtime exception"));

            ActorRef wordStatsActor = system.actorOf(WordStatsActor.props(mockYouTubeService));
            wordStatsActor.tell("runtime_exception", getRef());

            String response = expectMsgClass(String.class);
            JsonNode jsonResponse = Json.parse(response);

            assertEquals("An error occurred while processing your request.", jsonResponse.get("error").asText());
            verify(mockYouTubeService, times(1)).searchVideos("runtime_exception");
        }};
    }
    @Test
    public void testDuplicateWordFrequencyResolution() {
        new TestKit(system) {{
            YouTubeService mockYouTubeService = mock(YouTubeService.class);
            List<VideoResult> videoResults = Arrays.asList(
                    new VideoResult("Title1", "duplicate duplicate", "videoId1", "channelId1", "channelTitle1", "thumbnailUrl", Collections.emptyList()),
                    new VideoResult("Title2", "duplicate word", "videoId2", "channelId2", "channelTitle2", "thumbnailUrl", Collections.emptyList())
            );

            when(mockYouTubeService.searchVideos("duplicate_merge")).thenReturn(videoResults);

            ActorRef wordStatsActor = system.actorOf(WordStatsActor.props(mockYouTubeService));
            wordStatsActor.tell("duplicate_merge", getRef());

            String response = expectMsgClass(String.class);
            JsonNode jsonResponse = Json.parse(response);

            assertEquals(3, jsonResponse.get("duplicate").asLong());
            assertEquals(1, jsonResponse.get("word").asLong());
            verify(mockYouTubeService, times(1)).searchVideos("duplicate_merge");
        }};
    }
    @Test
    public void testFilterEmptyWordsAndValidWords() {
        new TestKit(system) {{
            YouTubeService mockYouTubeService = mock(YouTubeService.class);
            List<VideoResult> videoResults = Collections.singletonList(
                    new VideoResult("Title1", " valid  ", "videoId1", "channelId1", "channelTitle1", "thumbnailUrl", Collections.emptyList())
            );

            when(mockYouTubeService.searchVideos("empty_and_valid")).thenReturn(videoResults);

            ActorRef wordStatsActor = system.actorOf(WordStatsActor.props(mockYouTubeService));
            wordStatsActor.tell("empty_and_valid", getRef());

            String response = expectMsgClass(String.class);
            JsonNode jsonResponse = Json.parse(response);

            assertEquals(1, jsonResponse.get("valid").asLong());
            assertFalse(jsonResponse.has(""));
            verify(mockYouTubeService, times(1)).searchVideos("empty_and_valid");
        }};
    }











}
