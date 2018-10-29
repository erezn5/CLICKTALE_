package com.pipe.pipeapp;

import com.pipe.pipeapp.Production.CageResponseProduction;
import com.pipe.record.comparator.model.FactoryHelper;
import helpers.*;
import junit.framework.TestCase;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.text.ParseException;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import static org.apache.http.nio.client.methods.HttpAsyncMethods.createConsumer;

public class CageProd extends TestCase {

        CageArchivePackage cageArchivePackage;
        JSONObject jsonObject = null;
@Ignore
        @Test
        void cageProd() throws ParseException, ParserConfigurationException, IOException, InterruptedException, ExecutionException, org.json.simple.parser.ParseException {

                IdentifyProvider identifyProvider = new IdentifyProvider();
                long sessionId = identifyProvider.generateSessionId();

                CageResponseProduction cageResponseProduction = new CageResponseProduction();
                String res = cageResponseProduction.getCageResponse();
                cageArchivePackage = new CageArchivePackage(res);
                cageArchivePackage = cageArchivePackage.getCageArchivePackageObject();

                String recording = cageArchivePackage.getRecording();
                jsonObject = convertStringToJson(recording);

                String result = changeValues(jsonObject, res, sessionId);



        }

        public String changeValues(JSONObject jsonObject, String res, long sessionId) {

//        long oldSid =  Long.parseLong(jsonObject.get("SID").toString());
//        int oldPid = Integer.parseInt(jsonObject.get("PID").toString());
//        int oldSubscriberId =  Integer.parseInt(jsonObject.get("SubscriberId").toString());
//        long oldUid =  Long.parseLong(jsonObject.get("UID").toString());

                String oldSid = jsonObject.get("SID").toString();
                String oldPid = jsonObject.get("PID").toString();
                String oldSubscriberId = jsonObject.get("SubscriberId").toString();
                String oldUid = jsonObject.get("UID").toString();

                res=res.replaceAll(oldSid, Long.toString(sessionId));
                res=res.replaceAll(oldPid, Configuration.prop.getProperty("suite.pusher.defaultParams.optionalPid"));
                res=res.replaceAll(oldSubscriberId, Configuration.prop.getProperty("suite.pusher.defaultParams.subsId"));
                res=res.replaceAll(oldUid, Long.toString(sessionId));
                res=res.replaceAll("nv-p1-s3-assets-01", "nv-q-s3-assets-01");

                return res;
        }

        public JSONObject convertStringToJson(String recording) throws org.json.simple.parser.ParseException {
                JSONObject jsonObject = new JSONObject();
                JSONParser parser = new JSONParser();
                jsonObject = (JSONObject) parser.parse(recording);

                return jsonObject;

        }
        @Ignore
        @Test
        public void kafkaConsumerTest(){

                final Consumer<String, String>consumer = createKafkaConsumer();
                final int giveUp = 100;   int noRecordsCount = 0;

                while(true){
                        final ConsumerRecords<String, String> consumerRecords =
                                consumer.poll(100);

                        if (consumerRecords.count()==0) {
                                noRecordsCount++;
                                if (noRecordsCount > giveUp) break;
                                else continue;
                        }
                        consumerRecords.forEach(record -> {
                                System.out.printf("Consumer Record:(%d, %s, %d, %d)\n",
                                        record.key(), record.value(),
                                        record.partition(), record.offset());
                        });
                        consumer.commitAsync();
                }
                consumer.close();
                System.out.println("DONE");

        }

        private Consumer<String,String> createKafkaConsumer() {
                Properties props = new Properties();
                String brokers = Configuration.prop.getProperty("suite.pusher.defaultParams.brokers");
                props.put("bootstrap.servers", brokers);
                props.put("group.id", "CageConsumerGroupIO");
                props.put("enable.auto.commit", "true");
                props.put("auto.commit.interval.ms", "1000");
                props.put("session.timeout.ms", "30000");
                props.put("max.poll.records", "500");
                props.put("key.deserializer",
                        "org.apache.kafka.common.serialization.StringDeserializer");
                props.put("value.deserializer",
                        "org.apache.kafka.common.serialization.StringDeserializer");
                String topic = Configuration.prop.getProperty("suite.pusher.defaultParams.pipeTopic");
                final Consumer<String, String> consumer = new KafkaConsumer<String, String>(props);
                consumer.subscribe(Collections.singletonList(topic));

                return consumer;
        }

//        @Test
//        public void kafka(String res,long sessionId) throws ExecutionException, InterruptedException {
//            IdentifyProvider identifyProvider = new IdentifyProvider();
//            long sid = identifyProvider.generateSessionId();
//            KafkaClient kafkaClient = new KafkaClient("422", "26952", "message", sid);
//            kafkaClient.kafkaprocess();
//        }
    @Test
    public void kafka() throws ExecutionException, InterruptedException {
//        IdentifyProvider identifyProvider = new IdentifyProvider();
////        long sid = identifyProvider.generateSessionId();
////        KafkaClient kafkaClient = new KafkaClient("26952", "422", "message", sid);
////        kafkaClient.kafkaprocess();
        Producer<String, String> producer = ProducerCreator.createProducer();


        ProducerRecord<String, String> record = new ProducerRecord<String, String>(IKafkaConstants.TOPIC_NAME, "This is record ");
        RecordMetadata metadata = producer.send(record).get();

//        for (int index = 0; index < IKafkaConstants.MESSAGE_COUNT; index++) {
//            ProducerRecord<String, String> record = new ProducerRecord<String, String>(IKafkaConstants.TOPIC_NAME,
//                    "This is record " + index);
//            try {
//                RecordMetadata metadata = producer.send(record).get();
//                System.out.println("Record sent with key " + index + " to partition " + metadata.partition()
//                        + " with offset " + metadata.offset());
//            }
//            catch (ExecutionException e) {
//                System.out.println("Error in sending record");
//                System.out.println(e);
//            }
//            catch (InterruptedException e) {
//                System.out.println("Error in sending record");
//                System.out.println(e);
//            }
//        }
    }
}
