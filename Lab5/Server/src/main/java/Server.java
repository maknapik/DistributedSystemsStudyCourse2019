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
        actorSystem.actorOf(Props.create(ServerActor.class, databaseService), "Server");
    }

    public static void main(String[] args) {
        Server server = new Server(new DatabaseService());

        server.run();
    }

}
