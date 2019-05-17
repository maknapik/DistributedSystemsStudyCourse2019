package actor;

import akka.actor.AbstractActor;
import model.SearchRequest;
import model.SearchResponse;

public class ClientActor extends AbstractActor {

    public Receive createReceive() {
        return receiveBuilder()
                .match(String.class, line -> {
                    String[] parameters = line.split(" ");
                    Command command = Command.valueOf(parameters[0].toUpperCase());

                    switch (command) {
                        case PRICE:
                            SearchRequest searchRequest = new SearchRequest();
                            searchRequest.setTitle(parameters[1]);

                            getContext()
                                    .actorSelection("akka.tcp://ServerSystem@127.0.0.1:2552/user/Server")
                                    .tell(searchRequest, getSelf());
                    }

                })
                .match(SearchResponse.class, searchResponse -> {
                    if (searchResponse.getPrice() < 0.0) {
                        System.out.println("Position not found");
                    } else {
                        System.out.println("Price: " + searchResponse.getPrice());
                    }
                })
                .matchAny(object -> {
                })
                .build();
    }
}
