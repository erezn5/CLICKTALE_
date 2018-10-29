package com.pipe.record.comparator.parser;

import com.pipe.record.comparator.model.Record;
import helpers.JsonActions;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

public class RecordSignatureParser {

    Record record;
    public String subscriber;
    public String project;
    public String session;
    JSONObject jsonObject = new JSONObject();
    JsonActions jsonActions = new JsonActions();

    public RecordSignatureParser(JSONObject jsonObject) throws ParseException {
        this.record = record;
        jsonActions.convertStringToJson(record.getRecording());
        this.subscriber = jsonObject.get("SubscriberId").toString();
        this.session = jsonObject.get("SID").toString();
        this.project = jsonObject.get("PID").toString();

    }
    public String getKey() {
        StringBuilder buf = new StringBuilder();
        return buf.append(subscriber)
                .append(".").append(project)
                .append(".").append(session).toString();
    }
    public String toString() {
        return getKey();
    }
}

