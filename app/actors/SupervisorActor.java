package actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.OneForOneStrategy;
import akka.actor.SupervisorStrategy;
import akka.actor.Props;
import akka.japi.pf.DeciderBuilder;
import models.YouTubeService;
import scala.concurrent.duration.Duration;

import javax.inject.Inject;

import static akka.actor.SupervisorStrategy.restart;

/**
 * Supervisor actor to manage child actors and handle failures.
 */
public class SupervisorActor extends AbstractActor {

    private final YouTubeService youTubeService;

    @Inject
    public SupervisorActor(YouTubeService youTubeService) {
        this.youTubeService = youTubeService;
    }

    @Override
    public SupervisorStrategy supervisorStrategy() {
        return new OneForOneStrategy(
                10,
                Duration.create("1 minute"),
                DeciderBuilder.match(Exception.class, e -> restart())
                        .matchAny(o -> SupervisorStrategy.escalate())
                        .build()
        );
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Props.class, props -> {
                    ActorRef child = getContext().actorOf(props);
                    sender().tell(child, self());
                })
                .build();
    }
}
