package helpers;

import com.pipe.pipeapp.Configuration;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class RabbitAMQPTest {
    String rabbitUserName = Configuration.prop.getProperty("suite.pusher.defaultParams.rabbitQueueUserName");
    String rabbitPassword = Configuration.prop.getProperty("suite.pusher.defaultParams.rabbitQueuePassword");
    String rabbitIp = Configuration.prop.getProperty("suite.pusher.defaultParams.rabbitIp");
    String rabbitPort = Configuration.prop.getProperty("suite.pusher.defaultParams.rabbitPort");
    public Channel connectToRabbitQueue(String queueName) throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();

        factory.setUsername(this.rabbitUserName);
        factory.setPassword(this.rabbitPassword);
        factory.setHost(this.rabbitIp);
        factory.setPort(Integer.parseInt(rabbitPort.toString()));
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        return channel;
    }

    public String checkAvailability(String queue, Channel channel) throws IOException, InterruptedException {

        boolean durable = true;
        channel.queueDeclare(queue, durable, false, false, null);
        System.out.println(" [*] Consumer : waiting for messages. To exit press CTRL + C");

        boolean autoAck = false;
        System.out.println("Queue name is: " + queue);
        QueueingConsumer consumer = new QueueingConsumer(channel);
        channel.basicConsume(queue, autoAck, consumer);
        int i=0;
        String sid="";
        while(i<10){

            QueueingConsumer.Delivery delivery = consumer.nextDelivery();
            String message = new String(delivery.getBody());


            if(message == null){
                Thread.sleep(100);
                i++;
            }else{
                System.out.println(" [X] Consumer : received '" + message + "'");
                sid = message.split("LiveSessionId")[1].split("MessageCreateDate")[0].split("\":")[1].split(",\"")[0];

                break;
            }
        }

        return sid;

    }
}
