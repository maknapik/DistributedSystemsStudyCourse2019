package command;

import java.io.Serializable;

public class StandardCommand implements Command, Serializable {

    private CommandType commandType;
    private String key;

    public StandardCommand(CommandType commandType, String key) {
        this.commandType = commandType;
        this.key = key;
    }

    @Override
    public CommandType getCommandType() {
        return commandType;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String toString() {
        return String.format("Command type: %s - key: %s", commandType, key);
    }

}
