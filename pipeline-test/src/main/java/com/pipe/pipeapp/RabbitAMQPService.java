package com.pipe.pipeapp;

import com.pipe.record.comparator.io.ConsumerNew;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import helpers.Consumer;
import helpers.JsonActions;
import helpers.RecordMapper;
import helpers.RecordSignature;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeoutException;


public class RabbitAMQPService implements Consumer {

    JsonActions jsonActions = new JsonActions();
    String queueName = "";
    private Executor executor;
    ConnectionFactory m_factory;
    String m_key;
    RecordMapper m_mapper;
    private int limit;
    private int count = 0;

    public RabbitAMQPService(String queueName, int limit, String i_key, RecordMapper i_mapper, ConnectionFactory factory) {

        this.m_factory = factory;
        this.m_mapper = i_mapper;
        this.m_key = i_key;
        this.queueName = queueName;
        this.limit = limit;
    }

    @Override
    public void consume() throws IOException, TimeoutException {

        Connection connection = m_factory.newConnection();
        Channel channel = connection.createChannel();

        boolean durable = true;

        channel.queueDeclare(this.queueName, durable, false, false, null);
        System.out.println(" [*] ConsumerNew : waiting for messages. To exit press CTRL + C");

        int prefetchCount = 1;
        channel.basicQos(prefetchCount);
        boolean autoAck = false;

        QueueingConsumer consumer = new QueueingConsumer(channel);
        channel.basicConsume(this.queueName, autoAck, consumer);
//        messageContainter = new ArrayList<>();
        while (count < limit) {
            // long tag = -1;
            count++;
            try {
                QueueingConsumer.Delivery delivery = consumer.nextDelivery();
                long tag = delivery.getEnvelope().getDeliveryTag();
//                if (tag > -1) {
                System.out.println(" [X] Consumer : ack message " + tag);
                channel.basicAck(tag, false);
//                }
                String message = new String(delivery.getBody());
                Thread.sleep(2000);
                RecordSignature record = new RecordSignature(
                        jsonActions.convertStringToJson(message));

                System.out.println(" [X] Consumer : received " + record.getKey() + " session.");
                m_mapper.addEntry(record, m_key);
                System.out.println(" [X] Consumer : going to the next message..");
            } catch (Throwable e) {
                System.err.println("Failed to create record for the comparison " + e.getMessage());
                e.printStackTrace();
//            } finally {
//                if (tag > -1) {
//                    System.out.println(" [X] Consumer : ack message " + tag);
//                    channel.basicAck(tag, false);
//                }
//            }
            }
            System.out.println(" [X] Consumer : finished consuming");
            m_mapper.report();
        }

    }
}