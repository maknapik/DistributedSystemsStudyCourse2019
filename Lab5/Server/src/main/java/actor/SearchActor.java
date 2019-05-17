package actor;

import akka.actor.AbstractActor;
import akka.japi.pf.ReceiveBuilder;
import database.DatabaseService;
import model.SearchRequest;
import model.SearchResponse;

public class SearchActor extends AbstractActor {

    private DatabaseService databaseService;

    public SearchActor(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    @Override
    public Receive createReceive() {
        return new ReceiveBuilder()
                .match(SearchRequest.class, searchRequest -> {
                    double price = databaseService.findPosition(searchRequest.getTitle());

                    SearchResponse searchResponse = new SearchResponse();
                    searchResponse.setPrice(price);

                    getSender().tell(searchResponse, getSelf());
                })
                .build();
    }

}
