package command;

public enum CommandType {

    CONTAINS_KEY("contains"),
    GET("get"),
    PUT("put"),
    REMOVE("remove"),
    EXIT("exit");

    private String name;

    CommandType(String name) {
        this.name = name;
    }

    public static CommandType getbyName(String name) {
        for(CommandType commandType : CommandType.values()) {
            if(commandType.name.equals(name.toLowerCase())) {
                return commandType;
            }
        }

        return CommandType.EXIT;
    }

}
