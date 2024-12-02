package actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import models.VideoResult;
import scala.concurrent.duration.Duration;
import services.YouTubeService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Actor responsible for managing user-specific operations.
 * Handles search queries and sends video results periodically to the client.
 */
public class UserActor extends AbstractActor {

    private final ActorRef out;
    private final YouTubeService youTubeService;
    private final Map<String, List<VideoResult>> searchHistory; // Map to store query and results

    /**
     * Creates Props for the UserActor, used for instantiation.
     *
     * @param out The actor reference for sending data to the client.
     * @param youTubeService The YouTubeService instance for interacting with the YouTube API.
     * @return Props for creating the actor.
     */
    public static Props props(ActorRef out, YouTubeService youTubeService) {
        return Props.create(UserActor.class, () -> new UserActor(out, youTubeService));
    }

    /**
     * Constructor for UserActor.
     * Initializes the actor with the output actor reference and YouTube service instance.
     *
     * @param out The actor reference for sending data to the client.
     * @param youTubeService The service used to interact with the YouTube API.
     */
    public UserActor(ActorRef out, YouTubeService youTubeService) {
        this.out = out;
        this.youTubeService = youTubeService;
        this.searchHistory = new HashMap<>(); // Initialize search history
    }

    /**
     * Defines the actor's message handling behavior.
     * Handles search queries and fetches video results periodically.
     *
     * @return The Receive object specifying how to handle incoming messages.
     */
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(String.class, query -> {
<<<<<<< HEAD
                    // Check if query exists in search history
                    List<VideoResult> initialVideos = searchHistory.getOrDefault(query, null);

                    if (initialVideos == null) {
                        // Fetch and store results if query is not in history
                        initialVideos = youTubeService.searchVideos(query).stream()
                                .limit(10)
                                .collect(Collectors.toList());
                        searchHistory.put(query, initialVideos); // Store in history
                    }

                    // Send the results to the client
=======
                    // Initial fetch for the query
                    List<VideoResult> initialVideos = youTubeService.searchVideos(query).stream()
                            .limit(10)
                            .collect(Collectors.toList());
                    // Send initial results to the client
>>>>>>> 157217f3cd104039914b96a1dc94064e04f3b3ea
                    out.tell(play.libs.Json.toJson(initialVideos).toString(), self());

                    // Periodically fetch updates for the query
                    getContext().getSystem().scheduler().scheduleWithFixedDelay(
                            Duration.create(1, "seconds"), // Start immediately
                            Duration.create(60, "seconds"), // Update interval
                            () -> {
                                List<VideoResult> updatedVideos = youTubeService.searchVideos(query).stream()
                                        .limit(10)
                                        .collect(Collectors.toList());
<<<<<<< HEAD
                                searchHistory.put(query, updatedVideos); // Update history
=======
                                // Send updated results to the client
>>>>>>> 157217f3cd104039914b96a1dc94064e04f3b3ea
                                out.tell(play.libs.Json.toJson(updatedVideos).toString(), self());
                            },
                            getContext().getSystem().dispatcher()
                    );
                })
                .matchEquals("getHistory", msg -> {
                    // Return the entire search history to the client
                    out.tell(play.libs.Json.toJson(searchHistory).toString(), self());
                })
                .build();
    }
}
