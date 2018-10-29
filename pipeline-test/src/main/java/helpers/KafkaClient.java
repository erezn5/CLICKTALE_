package helpers;

import com.pipe.pipeapp.Configuration;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

public class KafkaClient {
    private Producer<String, String> m_kafkaProducers;
    private int maxKafkaMessageSizeInMb = 8;
    private String kafkaCompressionCodec = "lz4";
    private String maxKafkaAcks = "1";
    private int maxKafkaRetries;
    private int kafkaBatchSize = 16384;
    private int kafkaBufferMemory = 33554432;
    private int kafkaConnectionsMaxIdleMs = 540000;
    private int kafkaLingerMs = 0;
    private int kafkaMaxBlocks = 100;
    private int kafkaMaxRequestSize = 1048576;
    private int kafkaRequestTimeoutMs = 30000;
    private int kafkaMaxInFlightRequestsPerConnection = 5;
    private int kafkaBatchNumMessages = 10000;
    private int kafkeQueueBufferingMaxMs = 10;
    private int kafkaFetchWaitMaxMs = 10;
    private int kafkaSocketBlockingMaxMs = 10;
    private boolean kafkaSocketNagleDisable = false;


    String m_projectId;
    String m_subscriberId;
    String m_content;
    Map<String, String> m_kafkaRawParams;
//    String brokers = Configuration.prop.getProperty("suite.pusher.defaultParams.kafkaBrokers");
    String brokers = "172.22.0.65:9092";

    String groupId = Configuration.prop.getProperty("suite.pusher.defaultParams.groupid");
//    String kafkaPrefix = Configuration.prop.getProperty("suite.pusher.defaultParams.kafkaTopicPrefix");
    String kafkaPrefix = "proc_";
    long m_sessionId;

    public KafkaClient(String i_projectId, String i_subscriberId,String i_content,long i_sessionId/*, Map<String, String> i_kafkaRawParams*/ ){
        m_projectId = i_projectId;
        m_subscriberId = i_subscriberId;
        m_content = i_content;
        m_sessionId = i_sessionId;

      //  System.getenv()

    }

    /**
     * <url href="http://cage.clicktale.net/recording/v1/233078/225/1464879842145684">example message</url>
     */

    public void kafkaprocess(){

        Properties props = new Properties();
//        props.put("retries",5);
//        props.put("acks","all");
//        props.put("batch.size",Configuration.prop.getProperty("suite.pusher.deafultParams.bufferSize"));
        props.put("bootstrap.servers", brokers);
//        props.put("group.id", groupId);
//        props.put("enable.auto.commit", "true");
//        props.put("auto.commit.interval.ms", "1000");
//        props.put("session.timeout.ms", "30000");
        props.put("key.serializer", org.apache.kafka.common.serialization.StringSerializer.class.getName());
        props.put("value.serializer", org.apache.kafka.common.serialization.StringSerializer.class.getName());


        Producer producer = new KafkaProducer<>(props);

//        for (int i = 0; i < 100; i++) {
//            producer.send(new ProducerRecord(getTopicName(m_projectId, m_subscriberId), Integer.toString(i), Integer.toString(i)));
            producer.send(new ProducerRecord(getTopicName(m_projectId, m_subscriberId), m_content,Long.toString(m_sessionId)));
            System.out.println("Message sent successfully");
//        }

        producer.close();

    }


    public String getTopicName(String projectId, String subscriberId) {
        String str = kafkaPrefix  + subscriberId + "_"+ projectId;

        System.out.println(str);
        return str;
    }
}