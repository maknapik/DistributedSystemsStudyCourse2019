package command;

public class PutCommand extends StandardCommand {

    private Integer value;

    public PutCommand(CommandType commandType, String key, Integer value) {
        super(commandType, key);

        this.value = value;
    }

    Integer getValue() {
        return value;
    }

    @Override
    public String toString() {
        return super.toString() + String.format(" - value: %d", value);
    }
}
