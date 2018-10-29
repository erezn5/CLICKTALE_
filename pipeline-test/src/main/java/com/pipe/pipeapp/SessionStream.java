package com.pipe.pipeapp;

import com.google.gson.Gson;
import org.json.simple.JSONObject;

public class SessionStream  {
    //Variables have been declared with capital letter at the beginning in order to get the variables from Gson object
    String CreateDate;
    String Data;
    int DataFlags;
    int DataFlagType;
    long LiveSessionId;
    int MessageNumber;
    int StreamId;
    int ProjectId; int StreamMessageId;  Gson g;
    SessionStream s;
    String hello;
    public int getStreamMessageId() {
        return StreamMessageId;
    }


    public int getMessageNumber() {
        return s.MessageNumber;
    }

    public int getDataFlags() {
        return s.DataFlags;
    }

    public int getDataFlagType() {
        return s.DataFlagType;
    }

    public int getStreamId() {
        return s.StreamId;
    }

    public SessionStream(int i_streamId,int i_messageNumber,long i_liveSessionId,int i_dataFlags,
                         int i_projectId,int i_dataFlagType, String i_data,String i_createDate,
                         int i_streamMessageId){
        CreateDate = i_createDate;
        DataFlags = i_dataFlags;
        DataFlagType = i_dataFlagType;
        LiveSessionId = i_liveSessionId;
        MessageNumber = i_messageNumber;
        StreamId = i_streamId;
        StreamMessageId = i_streamMessageId;
        ProjectId = i_projectId;
    }

    public SessionStream(String data){

        g = new Gson();
        s = g.fromJson(data,SessionStream.class);
        s = getS();

    }


    public SessionStream getS() {
        return s;
    }

}


