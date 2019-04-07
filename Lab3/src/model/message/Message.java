package model.message;

import java.io.Serializable;

public abstract class Message implements Serializable {

    private MessageType messageType;

    Message(MessageType messageType) {
        this.messageType = messageType;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    @Override
    public String toString() {
        return "[" + messageType + "] ";
    }
}
