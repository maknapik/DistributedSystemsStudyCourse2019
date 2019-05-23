package actor;

import akka.actor.AbstractActor;
import model.*;

public class ClientActor extends AbstractActor {

    private static final String SERVER_REF = "akka.tcp://ServerSystem@127.0.0.1:2552/user/Server";

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(String.class, line -> {
                    String[] parameters = line.split(" ");

                    try {
                        Command command = Command.valueOf(parameters[0].toUpperCase());

                        switch (command) {
                            case PRICE:
                                SearchRequest searchRequest = new SearchRequest();
                                searchRequest.setTitle(parameters[1]);

                                getContext()
                                        .actorSelection(SERVER_REF)
                                        .tell(searchRequest, getSelf());
                                break;
                            case ORDER:
                                OrderRequest orderRequest = new OrderRequest();
                                orderRequest.setTitle(parameters[1]);

                                getContext()
                                        .actorSelection(SERVER_REF)
                                        .tell(orderRequest, getSelf());
                                break;
                            case READ:
                                ReadRequest readRequest = new ReadRequest();
                                readRequest.setTitle(parameters[1]);

                                getContext()
                                        .actorSelection(SERVER_REF)
                                        .tell(readRequest, getSelf());
                                break;
                        }
                    } catch (IllegalArgumentException e) {
                        System.out.println("Unknown command");
                    } catch (ArrayIndexOutOfBoundsException e) {
                        System.out.println("Lack of parameters");
                    }

                })
                .match(SearchResponse.class, searchResponse -> {
                    if (searchResponse.getPrice() < 0.0) {
                        System.out.println("Position not found");
                    } else {
                        System.out.println("Price: " + searchResponse.getPrice());
                    }
                })
                .match(OrderResponse.class, searchResponse -> {
                    System.out.println(searchResponse.getPayload());
                })
                .match(ReadResponse.class, readResponse -> {
                    System.out.println(readResponse.getPayload());
                })
                .matchAny(object -> {
                })
                .build();
    }

}
