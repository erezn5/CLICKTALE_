package com.pipe.record.comparator.model;

import helpers.CageArchivePackage;
import helpers.JsonActions;
import org.json.simple.JSONObject;

public class Record {

    String recording, events, streams, webPage;

    public Record(String recording, String events, String streams,String webPage) {

        this.recording = recording;
        this.events = events;
        this.streams = streams;
        this.webPage = webPage;
    }

    public String getRecording() {
        return recording;
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


}
