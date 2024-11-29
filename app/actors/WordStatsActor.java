package actors;

import akka.actor.AbstractActor;
import akka.actor.Props;
import com.fasterxml.jackson.databind.JsonNode;
import play.libs.Json;
import models.VideoResult;
import services.YouTubeService;

import java.util.*;
import java.util.stream.Collectors;

public class WordStatsActor extends AbstractActor {

    private final YouTubeService youTubeService;

    // Constructor to inject YouTubeService
    public WordStatsActor(YouTubeService youTubeService) {
        this.youTubeService = youTubeService;
    }

    public static Props props(YouTubeService youTubeService) {
        return Props.create(WordStatsActor.class, () -> new WordStatsActor(youTubeService));
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(String.class, this::computeWordStats)
                .build();
    }

    private void computeWordStats(String query) {
        try {
            List<VideoResult> videos = youTubeService.searchVideos(query);

            if (videos.isEmpty()) {
                getSender().tell(
                        Json.newObject().put("message", "No word frequency data available for \"" + query + "\"").toString(),
                        getSelf()
                );
                return;
            }

            List<VideoResult> latestVideos = videos.stream()
                    .filter(video -> video.getDescription() != null && !video.getDescription().isEmpty())
                    .limit(50)
                    .collect(Collectors.toList());

            Map<String, Long> wordFrequency = latestVideos.stream()
                    .flatMap(video -> Arrays.stream(video.getDescription().split("\\W+")))
                    .map(String::toLowerCase)
                    .filter(word -> !word.isEmpty())
                    .collect(Collectors.groupingBy(
                            word -> word,
                            Collectors.counting() // Ensures values are stored as Long
                    ));

            Map<String, Long> sortedWordFrequency = wordFrequency.entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .limit(100)
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue,
                            (e1, e2) -> e1,
                            LinkedHashMap::new
                    ));

            getSender().tell(Json.toJson(sortedWordFrequency).toString(), getSelf());
        } catch (Exception e) {
            getSender().tell(
                    Json.newObject().put("error", "An error occurred while processing your request.").toString(),
                    getSelf()
            );
        }
    }
}
