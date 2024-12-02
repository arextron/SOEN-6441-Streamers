package controllers;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.stream.Materializer;
import akka.testkit.javadsl.TestKit;
import models.VideoResult;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import play.cache.SyncCacheApi;
import play.mvc.Http;
import play.mvc.Result;
import play.test.WithApplication;
import services.YouTubeService;

import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.contentAsString;

public class HomeControllerTest extends WithApplication {

    @Mock
    private YouTubeService youTubeService;

    @Mock
    private SyncCacheApi cache;

    @Mock
    private Materializer materializer;

    private static ActorSystem system;

    private HomeController homeController;

    @BeforeClass
    public static void setupClass() {
        system = ActorSystem.create("TestSystem");
    }

    @AfterClass
    public static void teardownClass() {
        TestKit.shutdownActorSystem(system);
        system = null;
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        homeController = new HomeController(youTubeService, cache, system, materializer);
    }

    @Test
    public void testIndex() {
        Http.Request request = mock(Http.Request.class);
        Result result = homeController.index(request);
        assertEquals(OK, result.status());
    }

    @Test
    public void testSearch_NullQuery() throws Exception {
        Http.Request request = mock(Http.Request.class);
        when(request.session()).thenReturn(new Http.Session(new HashMap<>()));

        CompletionStage<Result> resultStage = homeController.search(null, request);
        Result result = resultStage.toCompletableFuture().get();

        assertEquals(OK, result.status());
        assertEquals("Please provide a search query.", contentAsString(result));
    }

    @Test
    public void testSearch_EmptyQuery() throws Exception {
        Http.Request request = mock(Http.Request.class);
        when(request.session()).thenReturn(new Http.Session(new HashMap<>()));

        CompletionStage<Result> resultStage = homeController.search("", request);
        Result result = resultStage.toCompletableFuture().get();

        assertEquals(OK, result.status());
        assertEquals("Please provide a search query.", contentAsString(result));
    }

    // ... rest of your test methods with necessary adjustments ...

    @Test
    public void testShowVideoDetails_Success() throws Exception {
        String videoId = "testVideoId";

        // Mock youTubeService.getVideoDetails
        VideoResult videoResult = new VideoResult("Title", "Description", videoId, "channelId", "thumbUrl", "Channel", Arrays.asList("tag1", "tag2"));
        when(youTubeService.getVideoDetails(videoId)).thenReturn(videoResult);

        // Mock Patterns.ask to return a completed future with videoResult
        CompletionStage<Object> future = CompletableFuture.completedFuture(videoResult);
        ActorRef tagsActor = system.actorOf(Props.create(TestActor.class, future));

        // Use dependency injection or reflection to set the actor (if possible)
        // If not, you can adjust your controller to accept an ActorRef for testing purposes

        // Call the method
        CompletionStage<Result> resultStage = homeController.showVideoDetails(videoId);
        Result result = resultStage.toCompletableFuture().get();

        assertEquals(OK, result.status());
        // Additional assertions can be made
    }

    // ... similar adjustments for other methods ...

    // TestActor class to simulate actor responses
    public static class TestActor extends akka.actor.AbstractActor {
        private final CompletionStage<Object> response;

        public TestActor(CompletionStage<Object> response) {
            this.response = response;
        }

        @Override
        public Receive createReceive() {
            return receiveBuilder()
                    .matchAny(msg -> {
                        // Simulate the actor responding with the predefined future
                        response.thenAccept(result -> getSender().tell(result, getSelf()));
                    })
                    .build();
        }
    }
}
