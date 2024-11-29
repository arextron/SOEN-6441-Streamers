package actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import models.VideoResult;
import services.YouTubeService;
import play.libs.Json;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Actor for computing word statistics.
 */
public class WordStatsActor extends AbstractActor {

    private final String query;
    private final ActorRef out;
    private final YouTubeService youTubeService;

    public static Props props(String query, ActorRef out, YouTubeService youTubeService) {
        return Props.create(WordStatsActor.class, () -> new WordStatsActor(query, out, youTubeService));
    }

    public WordStatsActor(String query, ActorRef out, YouTubeService youTubeService) {
        this.query = query;
        this.out = out;
        this.youTubeService = youTubeService;

        computeWordStats();
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .build();
    }

    private void computeWordStats() {
        try {
            List<VideoResult> videos = youTubeService.searchVideos(query);

            if (videos.isEmpty()) {
                out.tell(Json.newObject().put("message", "No word frequency data available for \"" + query + "\"").toString(), self());
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
                    .collect(Collectors.groupingBy(word -> word, Collectors.counting()));

            Map<String, Long> sortedWordFrequency = wordFrequency.entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .limit(100)
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue,
                            (e1, e2) -> e1,
                            LinkedHashMap::new
                    ));

            out.tell(Json.toJson(sortedWordFrequency).toString(), self());
        } catch (Exception e) {
            out.tell(Json.newObject().put("error", "An error occurred while processing your request.").toString(), self());
        }
    }
}
