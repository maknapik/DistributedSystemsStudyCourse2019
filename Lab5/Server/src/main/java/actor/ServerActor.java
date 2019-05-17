package actor;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.routing.RoundRobinPool;
import database.DatabaseService;
import model.SearchRequest;

public class ServerActor extends AbstractActor {

    private DatabaseService databaseService;

    public ServerActor(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(String.class, line -> {
            String[] parameters = line.split(" ");
            Command command = Command.valueOf(parameters[0].toUpperCase());

            switch (command) {
                case HEALTH:
                    System.out.println(String.format("Server's system path: %s", getSelf().path()));
            }
        }).match(SearchRequest.class, searchRequest -> {
            getContext().child(ServerActor.class.getSimpleName()).get().tell(searchRequest, getSender());
        }).matchAny(object -> {
            System.out.println("Any");
        }).build();
    }

    @Override
    public void preStart() {
        context().actorOf(Props.create(SearchActor.class, databaseService).withRouter(new RoundRobinPool(10)),
                ServerActor.class.getSimpleName());
    }

}
