package actors;

import akka.actor.AbstractActor;
import akka.actor.Props;
import com.fasterxml.jackson.databind.JsonNode;
import play.libs.Json;
import models.VideoResult;
import services.YouTubeService;

import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * @author Aryan Awasthi
 *
 * Actor responsible for computing word-level statistics from video descriptions.
 * Fetches videos based on a query, processes their descriptions, and calculates word frequencies.
 */
public class WordStatsActor extends AbstractActor {

    private final YouTubeService youTubeService;

    /**
     * Constructor for WordStatsActor.
     * Injects the YouTubeService to enable API interactions.
     *
     * @param youTubeService The YouTubeService instance for interacting with the YouTube API.
     */
    public WordStatsActor(YouTubeService youTubeService) {
        this.youTubeService = youTubeService;
    }

    /**
     * Creates Props for the WordStatsActor, used for instantiation.
     *
     * @param youTubeService The YouTubeService instance for interacting with the YouTube API.
     * @return Props for creating the actor.
     */
    public static Props props(YouTubeService youTubeService) {
        return Props.create(WordStatsActor.class, () -> new WordStatsActor(youTubeService));
    }

    /**
     * Defines the actor's message handling behavior.
     * Processes search queries to compute word frequency statistics.
     *
     * @return The Receive object specifying how to handle incoming messages.
     */
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(String.class, this::computeWordStats)
                .build();
    }

    /**
     * Computes word frequency statistics from the descriptions of the latest videos for a given query.
     *
     * @param query The search query provided by the user.
     */
    private void computeWordStats(String query) {
        try {
            // Fetch videos for the query
            List<VideoResult> videos = youTubeService.searchVideos(query);

            if (videos.isEmpty()) {
                getSender().tell(
                        Json.newObject().put("message", "No word frequency data available for \"" + query + "\"").toString(),
                        getSelf()
                );
                return;
            }

            // Filter and limit videos to the latest 50 with non-empty descriptions
            List<VideoResult> latestVideos = videos.stream()
                    .filter(video -> video.getDescription() != null && !video.getDescription().isEmpty())
                    .limit(50)
                    .collect(Collectors.toList());

            // Compute word frequencies from video descriptions
            Map<String, Long> wordFrequency = latestVideos.stream()
                    .flatMap(video -> Arrays.stream(video.getDescription().split("\\W+")))
                    .map(String::toLowerCase)
                    .filter(word -> !word.isEmpty())
                    .collect(Collectors.groupingBy(
                            word -> word,
                            Collectors.counting()
                    ));

            // Sort the word frequencies in descending order and limit to top 100 words
            Map<String, Long> sortedWordFrequency = wordFrequency.entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .limit(100)
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue,
                            (e1, e2) -> e1,
                            LinkedHashMap::new
                    ));

            // Send the sorted word frequencies to the sender
            getSender().tell(Json.toJson(sortedWordFrequency).toString(), getSelf());
        } catch (Exception e) {
            // Handle errors during processing
            getSender().tell(
                    Json.newObject().put("error", "An error occurred while processing your request.").toString(),
                    getSelf()
            );
        }
    }
}
