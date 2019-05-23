package actor;

import akka.actor.AbstractActor;
import akka.actor.OneForOneStrategy;
import akka.actor.Props;
import akka.actor.SupervisorStrategy;
import akka.japi.pf.DeciderBuilder;
import akka.routing.RoundRobinPool;
import database.DatabaseService;
import model.OrderRequest;
import model.ReadRequest;
import model.SearchRequest;
import scala.concurrent.duration.Duration;

import java.io.FileNotFoundException;

import static akka.actor.SupervisorStrategy.restart;
import static akka.actor.SupervisorStrategy.resume;

public class ServerActor extends AbstractActor {

    private static final SupervisorStrategy supervisorStrategy = new OneForOneStrategy(10,
            Duration.create("60 " + "seconds"),
            DeciderBuilder
                    .match(FileNotFoundException.class, e -> resume())
                    .matchAny(e -> restart())
                    .build());

    private DatabaseService databaseService;

    public ServerActor(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(SearchRequest.class, searchRequest -> {
            getContext().child(SearchActor.class.getSimpleName()).get().tell(searchRequest, getSender());
        }).match(OrderRequest.class, orderRequest -> {
            getContext().child(OrderActor.class.getSimpleName()).get().tell(orderRequest, getSender());
        }).match(ReadRequest.class, readRequest -> {
            getContext().child(ReadActor.class.getSimpleName()).get().tell(readRequest, getSender());
        }).matchAny(object -> {
            System.out.println("Unknown request");
        }).build();
    }

    @Override
    public void preStart() {
        context().actorOf(Props.create(SearchActor.class, new DatabaseService()).withRouter(new RoundRobinPool(10)),
                SearchActor.class.getSimpleName());
        context().actorOf(Props.create(OrderActor.class, databaseService).withRouter(new RoundRobinPool(10)),
                OrderActor.class.getSimpleName());
        context().actorOf(Props.create(ReadActor.class, new DatabaseService()).withRouter(new RoundRobinPool(10)),
                ReadActor.class.getSimpleName());
    }

    @Override
    public SupervisorStrategy supervisorStrategy() {
        return supervisorStrategy;
    }

}
