package actor;

import akka.actor.AbstractActor;
import akka.japi.pf.ReceiveBuilder;
import database.DatabaseService;
import model.OrderRequest;
import model.OrderResponse;

public class OrderActor extends AbstractActor {

    private DatabaseService databaseService;

    public OrderActor(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    @Override
    public AbstractActor.Receive createReceive() {
        return new ReceiveBuilder()
                .match(OrderRequest.class, orderRequest -> {
                    OrderResponse orderResponse = new OrderResponse();

                    if (databaseService.saveOrder(orderRequest.getTitle())) {
                        orderResponse.setPayload("Order saved");
                    } else {
                        orderResponse.setPayload("Order not saved");
                    }

                    getSender().tell(orderResponse, getSelf());
                })
                .build();
    }

}