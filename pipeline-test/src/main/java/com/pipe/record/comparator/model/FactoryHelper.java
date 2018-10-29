package com.pipe.record.comparator.model;

import com.pipe.pipeapp.Configuration;
import com.rabbitmq.client.ConnectionFactory;

import java.util.Properties;

public class FactoryHelper {

    Properties props;
    Properties m_prop;
    int limit;
    public FactoryHelper(){;}
    public FactoryHelper(Properties prop){
        props = new Properties();

        this.m_prop = prop;
    }
//    public Properties fillPropertiesForKafkaConsumer(){
//
//        String brokers = m_prop.getProperty("suite.pusher.defaultParams.brokers");
//
//        props.put("bootstrap.servers", brokers);
//        props.put("group.id", "CageConsumerGroupIO");
//        props.put("enable.auto.commit", "true");
//        props.put("auto.commit.interval.ms", "1000");
//        props.put("session.timeout.ms", "30000");
//        props.put("max.poll.records", "500");
//        props.put("key.deserializer",
//                "org.apache.kafka.common.serialization.StringDeserializer");
//        props.put("value.deserializer",
//                "org.apache.kafka.common.serialization.StringDeserializer");
//
//        return props;
//    }

    public ConnectionFactory fillPropertiesForRabbitConsumerAndConnect() {

        ConnectionFactory factory = new ConnectionFactory();
        String rabbitIp = m_prop.getProperty("suite.pusher.defaultParams.rabbitIp");
        String rabbitPort = m_prop.getProperty("suite.pusher.defaultParams.rabbitPort");
        String rabbitUserName = m_prop.getProperty("suite.pusher.defaultParams.rabbitQueueUserName");
        String rabbitPassword = m_prop.getProperty("suite.pusher.defaultParams.rabbitQueuePassword");
        // this.queueName = m_prop.getProperty("suite.pusher.defaultParams.rabbitQueue");
        this.limit = Integer.parseInt(m_prop.getProperty("suite.pusher.defaultParams.limit"));

        factory.setUsername(rabbitUserName);
        factory.setPassword(rabbitPassword);
        factory.setHost(rabbitIp);
        factory.setPort(Integer.parseInt(rabbitPort.toString()));

        return factory;

    }

//    public ConnectionFactory fillPropertiesForRabbitConsumerAndConnectForUnitTesting(){
//        ConnectionFactory factory = new ConnectionFactory();
//        this.rabbitIp = Configuration.prop.getProperty("suite.pusher.defaultParams.rabbitIp");
//        this.rabbitPort = Configuration.prop.getProperty("suite.pusher.defaultParams.rabbitPort");
//        this.rabbitUserName = Configuration.prop.getProperty("suite.pusher.defaultParams.rabbitQueueUserName");
//        this.rabbitPassword = Configuration.prop.getProperty("suite.pusher.defaultParams.rabbitQueuePassword");
//        // this.queueName = m_prop.getProperty("suite.pusher.defaultParams.rabbitQueue");
//        this.limit = Integer.parseInt(Configuration.prop.getProperty("suite.pusher.defaultParams.limit"));
//
//        factory.setUsername(rabbitUserName);
//        factory.setPassword(rabbitPassword);
//        factory.setHost(rabbitIp);
//        factory.setPort(Integer.parseInt(rabbitPort.toString()));
//
//        return factory;
//    }
}
