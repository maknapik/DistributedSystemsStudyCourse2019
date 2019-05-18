package actor;

import akka.NotUsed;
import akka.actor.AbstractActor;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;
import akka.stream.ThrottleMode;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import database.DatabaseService;
import model.ReadRequest;
import model.ReadResponse;
import scala.concurrent.duration.Duration;

import java.util.LinkedList;
import java.util.List;

public class ReadActor extends AbstractActor {

    private DatabaseService databaseService;

    public ReadActor(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ReadRequest.class, readRequest -> {
                    List<ReadResponse> responses = readPosition(readRequest.getTitle());

                    final Source<ReadResponse, NotUsed> source = Source.from(responses);
                    final Materializer materializer = ActorMaterializer.create(getContext().getSystem());
                    final Sink<ReadResponse, NotUsed> sink = Sink.actorRef(getSender(), new ReadResponse());

                    source.throttle(1, Duration.create(1, "seconds"), 1, ThrottleMode.shaping())
                            .runWith(sink, materializer);
                })
                .build();
    }

    private List<ReadResponse> readPosition(String title) throws InterruptedException {
        List<ReadResponse> responses = new LinkedList<>();

        for (String line : databaseService.readPosition(title)) {
            ReadResponse readResponse = new ReadResponse();
            readResponse.setPayload(line);

            responses.add(readResponse);
        }

        return responses;
    }

}