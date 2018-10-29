package com.pipe.record.comparator.io;

import com.pipe.pipeapp.HttpGetRequest;
import com.pipe.record.comparator.model.FactoryHelper;
import com.pipe.record.comparator.model.Record;
import com.pipe.record.comparator.parser.Parser;
import com.pipe.record.comparator.parser.RecordSignatureParser;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import helpers.JsonActions;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

public class AMQPConsumer implements ConsumerNew {

    ConnectionFactory factory;
    FactoryHelper factoryHelper;
    String queueName;
    JsonActions jsonActions = new JsonActions();
    String cage="";
    HttpGetRequest cageResponse = new HttpGetRequest();

    public AMQPConsumer(String queueName, Properties prop, String cage) {
        this.queueName = queueName;
        factoryHelper = new FactoryHelper(prop);
        factory=factoryHelper.fillPropertiesForRabbitConsumerAndConnect();
        this.cage = cage;
    }

    @Override
    public Record consume() throws IOException, TimeoutException, ParseException {
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        String message="";
        boolean durable = true;

        channel.queueDeclare(this.queueName, durable, false, false, null);
        System.out.println(" [*] ConsumerNew : waiting for messages. To exit press CTRL + C");

        int prefetchCount = 1;
        channel.basicQos(prefetchCount);
        boolean autoAck = false;

        QueueingConsumer consumer = new QueueingConsumer(channel);
        channel.basicConsume(this.queueName, autoAck, consumer);
        while (true) {//  while (count < limit) {
            long tag = -1;
            //count++;
            try {
                QueueingConsumer.Delivery delivery = consumer.nextDelivery();
                tag = delivery.getEnvelope().getDeliveryTag();
                message = new String(delivery.getBody());
                //todo - think about getting only signautre of subid.pid.sid
                RecordSignatureParser record = new RecordSignatureParser(
                        jsonActions.convertStringToJson(message));
                try {
                    String path = this.cage + "/"
                            + record.subscriber + "/"
                            + record.project + "/"
                            + record.session;

                    System.out.println("Getting Cage response... ");
                    String response = cageResponse.getRequest(path);

                    return Parser.parseFromCage(response);
                }catch(Throwable e){
                    System.err.println("Failed to get cage results in " + record + ". " + e.getMessage());
                    e.printStackTrace();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

}
