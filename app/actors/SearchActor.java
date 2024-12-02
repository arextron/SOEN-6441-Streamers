package actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import models.VideoResult;
import services.YouTubeService;
import play.libs.Json;
import scala.concurrent.duration.Duration;

import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Actor responsible for fetching new search results periodically.
 * This actor fetches and sends new video search results based on a query to the client
 * while filtering out duplicates.
 */
public class SearchActor extends AbstractActor {

    private final String query;
    private final ActorRef out;
    private final YouTubeService youTubeService;
    private final Set<String> sentVideoIds = new HashSet<>();

    /**
     * Creates Props for the SearchActor, used for instantiation.
     *
     * @param query The search query.
     * @param out The actor reference for sending data to the client.
     * @param youTubeService The YouTubeService instance for interacting with the YouTube API.
     * @return Props for creating the actor.
     */
    public static Props props(String query, ActorRef out, YouTubeService youTubeService) {
        return Props.create(SearchActor.class, () -> new SearchActor(query, out, youTubeService));
    }

    /**
     * Constructor for SearchActor.
     * Initializes the actor, fetches initial search results, and schedules periodic updates.
     *
     * @param query The search query.
     * @param out The actor reference for sending data to the client.
     * @param youTubeService The YouTubeService instance for interacting with the YouTube API.
     */
    public SearchActor(String query, ActorRef out, YouTubeService youTubeService) {
        this.query = query;
        this.out = out;
        this.youTubeService = youTubeService;

        // Fetch initial results
        fetchAndSendNewVideos();

        // Schedule periodic task to fetch new videos
        getContext().getSystem().scheduler().scheduleWithFixedDelay(
                Duration.create(30, TimeUnit.SECONDS),
                Duration.create(30, TimeUnit.SECONDS),
                getSelf(),
                new FetchNewVideos(),
                getContext().getSystem().dispatcher(),
                null
        );
    }

    /**
     * Creates the message handling behavior for the actor.
     *
     * @return The Receive object defining the message handling behavior.
     */
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(FetchNewVideos.class, msg -> {
                    fetchAndSendNewVideos();
                })
                .build();
    }

    /**
     * Fetches new videos based on the search query and sends them to the client.
     * Filters out videos that have already been sent to the client.
     */
    private void fetchAndSendNewVideos() {
        List<VideoResult> newVideos = youTubeService.searchVideos(query).stream()
                .filter(video -> !sentVideoIds.contains(video.getVideoId()))
                .collect(Collectors.toList());

        if (!newVideos.isEmpty()) {
            sentVideoIds.addAll(newVideos.stream()
                    .map(VideoResult::getVideoId)
                    .collect(Collectors.toSet()));

            // Send new videos to the client
            out.tell(Json.toJson(newVideos).toString(), self());
        }
    }

    /**
     * Message class for triggering a new fetch of videos.
     */
    static class FetchNewVideos { }
}
