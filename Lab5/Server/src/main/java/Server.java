import actor.ServerActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.typesafe.config.ConfigFactory;

public class Server {

    public static void main(String[] args) {
        final ActorSystem actorSystem = ActorSystem.create("ServerSystem", ConfigFactory.load());
        final ActorRef server = actorSystem.actorOf(Props.create(ServerActor.class), "Server");

        server.tell("health", ActorRef.noSender());
    }

}
