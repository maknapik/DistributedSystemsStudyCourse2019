import command.CommandParser;
import model.DistributedMap;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Main {

    public static void main(String[] args) throws Exception {
        System.setProperty("java.net.preferIPv4Stack","true");

        DistributedMap distributedMap = new DistributedMap(args[0], args[1]);

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        String command;

        while(!CommandParser.parseCommand(distributedMap, bufferedReader.readLine())) {
        }

        bufferedReader.close();
    }
}
