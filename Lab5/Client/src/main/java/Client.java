import actor.ClientActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.typesafe.config.ConfigFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Client {

    public static void main(String[] args) throws IOException {
        final ActorSystem actorSystem = ActorSystem.create("ClientSystem", ConfigFactory.load());
        final ActorRef client = actorSystem.actorOf(Props.create(ClientActor.class), "Client");

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            String line = br.readLine();
            if (line.equals("q")) {
                break;
            }
            client.tell(line, null);
        }
    }

}
