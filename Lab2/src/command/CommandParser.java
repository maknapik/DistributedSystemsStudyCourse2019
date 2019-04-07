package command;

import model.DistributedMap;

public class CommandParser {

    public static void parseReceivedCommand(DistributedMap distributedMap, Command command) throws Exception {
        switch(command.getCommandType()) {
            case PUT:
                PutCommand putCommand = (PutCommand) command;
                distributedMap.putLocal(putCommand.getKey(), putCommand.getValue());
                break;
            case REMOVE:
                distributedMap.removeLocal(command.getKey());
                break;
        }
    }

    public static boolean parseCommand(DistributedMap distributedMap, String commandLine) throws Exception {
        Command command = convertLine(commandLine);

        return executeCommand(distributedMap, command);
    }

    private static boolean executeCommand(DistributedMap distributedMap, Command command) throws Exception {
        switch(command.getCommandType()) {
            case PUT:
                PutCommand putCommand = (PutCommand) command;
                distributedMap.put(putCommand.getKey(), putCommand.getValue());
                return false;
            case REMOVE:
                distributedMap.remove(command.getKey());
                return false;
            case GET:
                System.out.println(String.format("Value under the key: %s - %d", command.getKey(), distributedMap.get(command.getKey())));
                return false;
            case CONTAINS_KEY:
                System.out.println(String.format("Map contains key: %s - %s", command.getKey(), distributedMap.containsKey(command.getKey())));
                return false;
            case EXIT:
                System.out.println("Exiting");
                return true;
        }

        return true;
    }

    private static Command convertLine(String commandLine) {
        String[] arguments = commandLine.split("\\s+");

        CommandType commandType;
        String key;

        commandType = CommandType.getbyName(arguments[0]);
        key = arguments[1];

        if(arguments.length > 2) {
            Integer value = Integer.parseInt(arguments[2]);

            return new PutCommand(commandType, key, value);
        }

        return new StandardCommand(commandType, key);
    }
}
