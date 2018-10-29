package helpers;

import com.google.gson.Gson;

public class RecordingParametersParser {

    int XMLSize;
    int DSRSize;
    int HTMLSize;

    RecordingParametersParser recordingParametersParser;
    Gson g;

    public int getXMLSize() {
        return XMLSize;
    }

    public int getDSRSize() {
        return DSRSize;
    }

    public int getHTMLSize() {
        return HTMLSize;
    }

    public RecordingParametersParser(String recordingString){
        g = new Gson();
        recordingParametersParser=g.fromJson(recordingString, RecordingParametersParser.class);
        recordingParametersParser=getRecordingParametersParser();
    }

    public RecordingParametersParser getRecordingParametersParser() {
        return recordingParametersParser;
    }
}
