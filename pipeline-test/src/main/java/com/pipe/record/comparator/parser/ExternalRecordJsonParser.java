package com.pipe.record.comparator.parser;

import com.google.gson.Gson;

public class ExternalRecordJsonParser {

    String events;
    String streams;
    String webPage;
    String recording;
    String avro;
    String errorException;
    String rawData;


    Gson g;
    ExternalRecordJsonParser externalRecordJsonParser;

    public ExternalRecordJsonParser(String resposne){
        g = new Gson();
        externalRecordJsonParser = g.fromJson(resposne, ExternalRecordJsonParser.class);
        externalRecordJsonParser.getExternalRecordJsonParser();

    }

    public ExternalRecordJsonParser getExternalRecordJsonParser() {
        return externalRecordJsonParser;
    }

    public String getEvents() {
        return events;
    }

    public String getStreams() {
        return streams;
    }

    public String getWebPage() {
        return webPage;
    }

    public String getRecording() {
        return recording;
    }

    public String getAvro() {
        return avro;
    }

    public String getErrorException() {
        return errorException;
    }

    public String getRawData() {
        return rawData;
    }

    public Gson getG() {
        return g;
    }

}
