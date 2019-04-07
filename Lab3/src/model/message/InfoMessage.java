package model.message;

public class InfoMessage extends Message {

    private String message;

    public InfoMessage(String message) {
        super(MessageType.INFO);

        this.message = message;
    }

    @Override
    public String toString() {
        return super.toString() + message;
    }

}
