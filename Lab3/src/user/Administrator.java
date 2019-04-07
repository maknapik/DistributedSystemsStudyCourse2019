package user;

import com.rabbitmq.client.*;
import converter.MessageConverter;
import model.Injury;
import model.message.ExaminationMessage;
import model.message.InfoMessage;
import model.message.Message;
import model.message.MessageType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalTime;
import java.util.concurrent.TimeoutException;

public class Administrator {

    public static void main (String[] args) throws IOException, TimeoutException {

        MessageConverter messageConverter = new MessageConverter();

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        String EXCHANGE_NAME = "hospitalaa";
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.TOPIC);

        String queueName = channel.queueDeclare().getQueue();
        channel.queueBind(queueName, EXCHANGE_NAME, "*");

        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                try {
                    Message message = messageConverter.BytesToMessageConverter(body);
                    String userName = properties.getReplyTo() != null ? properties.getReplyTo() : properties.getMessageId();

                    if (message.getMessageType() == MessageType.REQUEST) {
                        System.out.println(String.format("[%s] Message from doctor: %s to technicians - %s", LocalTime.now(), userName, message));
                    } else if (message.getMessageType() == MessageType.RESPONSE) {
                        System.out.println(String.format("[%s] Message from technician to doctor: %s - %s", LocalTime.now(), userName, message));
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        };

        channel.basicConsume(queueName, true, consumer);

        while (true) {
            String line = bufferedReader.readLine();

            InfoMessage message1 = new InfoMessage(line);

            if ("exit".equals(line)) {
                break;
            }

            // publish
            channel.basicPublish(EXCHANGE_NAME, "doctor", null, messageConverter.MessageToBytesConverter(message1));
            channel.basicPublish(EXCHANGE_NAME, "technician", null, messageConverter.MessageToBytesConverter(message1));
            System.out.println("Sent: " + line);
        }
    }

}
