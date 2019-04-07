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
import java.util.Arrays;
import java.util.List;

public class Technician {

    public static void main(String[] argv) throws Exception {

        MessageConverter messageConverter = new MessageConverter();

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));

        System.out.println("Enter serviced injuries, Technician: ");
        String line = bufferedReader.readLine();
        String[] properties = line.split(" ");

        List<Injury> servicedInjuries = Arrays.asList(Injury.fromString(properties[0]), Injury.fromString(properties[1]));

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        String EXCHANGE_NAME = "hospitalaa";
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.TOPIC);

        String queueName = channel.queueDeclare().getQueue();
        channel.queueBind(queueName, EXCHANGE_NAME, "technician");

        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                try {
                    try {
                        Thread.sleep(1000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    Message receivedMessage = messageConverter.BytesToMessageConverter(body);

                    if (receivedMessage.getMessageType() == MessageType.REQUEST && servicedInjuries.contains(((ExaminationMessage) receivedMessage).getInjury())) {
                        System.out.println("Received: " + receivedMessage);

                        ExaminationMessage message = new ExaminationMessage(MessageType.RESPONSE, ((ExaminationMessage) receivedMessage).getSurname(),
                                ((ExaminationMessage) receivedMessage).getInjury());

                        channel.basicPublish(EXCHANGE_NAME, "doctor", new AMQP.BasicProperties.Builder().messageId(properties.getReplyTo()).build(),
                                messageConverter.MessageToBytesConverter(message));

                        System.out.println("Sent: " + message);
                    } else if (receivedMessage.getMessageType() == MessageType.INFO) {
                        System.out.println("Received: " + receivedMessage);
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        };

        channel.basicConsume(queueName, true, consumer);
    }

}
