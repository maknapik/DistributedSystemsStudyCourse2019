package actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;

public class ClientActor extends AbstractActor {

    public Receive createReceive() {
        return receiveBuilder()
                .match(String.class, line -> {
                    getContext().actorSelection("akka.tcp://ServerSystem@127.0.0.1:2552/user/Server").tell("health", getSelf());
                })
                .matchAny(object -> {})
                .build();
    }
}
