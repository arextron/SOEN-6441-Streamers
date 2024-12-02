package actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.OneForOneStrategy;
import akka.actor.SupervisorStrategy;
import akka.actor.Props;
import akka.japi.pf.DeciderBuilder;
import services.YouTubeService;
import scala.concurrent.duration.Duration;

import javax.inject.Inject;

import static akka.actor.SupervisorStrategy.restart;

/**
 * Supervisor actor to manage child actors and handle failures.
 * Implements a "One-for-One" strategy to restart child actors upon failure.
 */
public class SupervisorActor extends AbstractActor {

    /**
     * Constructor for SupervisorActor.
     * Initializes the actor with a reference to the YouTubeService.
     *
     * @param youTubeService The service used for YouTube API interactions.
     */
    @Inject
    public SupervisorActor(YouTubeService youTubeService) {
    }

    /**
     * Defines the supervisor strategy for managing child actor failures.
     * Restarts child actors in case of exceptions and escalates unhandled cases.
     *
     * @return The supervisor strategy.
     */
    @Override
    public SupervisorStrategy supervisorStrategy() {
        return new OneForOneStrategy(
                10, // Maximum number of restarts within the time range
                Duration.create("1 minute"), // Time range for restart count
                DeciderBuilder.match(Exception.class, e -> restart()) // Restart on exceptions
                        .matchAny(o -> SupervisorStrategy.escalate()) // Escalate on unknown errors
                        .build()
        );
    }

    /**
     * Handles incoming messages and creates child actors when Props are provided.
     *
     * @return The Receive object defining the actor's behavior.
     */
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Props.class, props -> {
                    // Create a child actor with the provided Props
                    ActorRef child = getContext().actorOf(props);
                    // Notify the sender with the child actor reference
                    sender().tell(child, self());
                })
                .build();
    }
}
