package user;

import com.rabbitmq.client.*;
import converter.MessageConverter;
import model.Injury;
import model.message.ExaminationMessage;
import model.message.Message;
import model.message.MessageType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeoutException;

public class Doctor {

    public static void main(String[] args) throws IOException, TimeoutException {

        MessageConverter messageConverter = new MessageConverter();

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        String EXCHANGE_NAME = "hospitalaa";
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.TOPIC);

        String queueName = channel.queueDeclare().getQueue();
        channel.queueBind(queueName, EXCHANGE_NAME, "doctor");

        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                try {
                    Message message = messageConverter.BytesToMessageConverter(body);

                    if (queueName.equals(properties.getMessageId()) || message.getMessageType() == MessageType.INFO) {
                        System.out.println("Received: " + message);
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        };

        channel.basicConsume(queueName, true, consumer);

        while (true) {

            String line = bufferedReader.readLine();
            String[] messageProperties = line.split(" ");

            ExaminationMessage message = new ExaminationMessage(MessageType.REQUEST, messageProperties[0], Injury.fromString(messageProperties[1]));

            if ("exit".equals(line)) {
                break;
            }

            AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder().replyTo(queueName).build();

            channel.basicPublish(EXCHANGE_NAME, "technician", properties,
                    messageConverter.MessageToBytesConverter(message));

            System.out.println("Sent: " + message);
        }
    }

}
