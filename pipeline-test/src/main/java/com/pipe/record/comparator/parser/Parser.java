package com.pipe.record.comparator.parser;

import com.pipe.record.comparator.model.Record;
import org.apache.kafka.clients.consumer.ConsumerRecord;

public class Parser {

    public static Record parseFromKafka(ConsumerRecord<String, String> recordFromKafka) {
        return parseToRecord(recordFromKafka.value());
      }

    public static Record parseFromCage(String cageResponse) {
        return parseToRecord(cageResponse);
    }

    public static Record parseToRecord(String externalRecord) {
        ExternalRecordJsonParser externalRecordJsonParser = new ExternalRecordJsonParser(externalRecord);
        return new Record(externalRecordJsonParser.getRecording(), externalRecordJsonParser.getEvents(),externalRecordJsonParser.getStreams(),
                externalRecordJsonParser.getWebPage());
    }


}
