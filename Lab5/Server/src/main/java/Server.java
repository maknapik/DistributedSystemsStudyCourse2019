import actor.ServerActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.typesafe.config.ConfigFactory;
import database.DatabaseService;

public class Server {

    private DatabaseService databaseService;

    private Server(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    private void run() {
        final ActorSystem actorSystem = ActorSystem.create("ServerSystem", ConfigFactory.load());
        final ActorRef server = actorSystem.actorOf(Props.create(ServerActor.class, databaseService), "Server");

        server.tell("health", ActorRef.noSender());
    }

    public static void main(String[] args) {
        Server server = new Server(new DatabaseService());

        server.run();
    }

}
