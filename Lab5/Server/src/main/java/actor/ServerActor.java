package actor;

import akka.actor.AbstractActor;

public class ServerActor extends AbstractActor {

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(String.class, line -> {
                    String[] parameters = line.split(" ");
                    Command command = Command.valueOf(parameters[0].toUpperCase());

                    switch (command) {
                        case HEALTH:
                            System.out.println(String.format("Server's system path: %s", getSelf().path()));
                    }
                })
                .matchAny(object -> {})
                .build();
    }

    @Override
    public void preStart() {

    }

}
