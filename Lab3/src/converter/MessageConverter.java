package converter;

import model.message.Message;

import java.io.*;

public class MessageConverter {

    public byte[] MessageToBytesConverter(Message message) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);

        objectOutputStream.writeObject(message);
        objectOutputStream.flush();

        return byteArrayOutputStream.toByteArray();
    }

    public Message BytesToMessageConverter(byte[] bytes) throws IOException, ClassNotFoundException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);

        return (Message) objectInputStream.readObject();
    }

}