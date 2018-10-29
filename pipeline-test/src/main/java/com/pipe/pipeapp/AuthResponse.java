package com.pipe.pipeapp;

import com.amazonaws.services.dynamodbv2.xspec.S;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import jdk.nashorn.internal.parser.JSONParser;
import org.json.simple.JSONObject;
import org.json.*;
import org.json.simple.JSONObject;

import java.util.List;

public class AuthResponse {
    String ip;
    boolean authorized;
    String sid;
    String uid;
    double ratio;
    boolean skipRecording;
    String rejectReason;
    String userTrackingState;

    Gson g;
    AuthResponse ap;

    public AuthResponse(String i_ip,boolean i_authorized,int i_sid, String i_uid, double i_ratio,boolean i_skipRecording, String i_rejectReason,int i_pid,String i_userTrackingState) {
        ip = ip;
        authorized = i_authorized;
        sid = Integer.toString(i_sid);
        uid = i_uid;
        ratio = i_ratio;
        skipRecording = i_skipRecording;
        rejectReason = i_rejectReason;
        userTrackingState = i_userTrackingState;

    }

    public AuthResponse(String response){
       g = new Gson();
     //  ap = new AuthResponse(response);
       ap=g.fromJson(response, AuthResponse.class);
       ap=getAuthResponseObject();
//       System.out.println("ip: " +  ap.ip + "\nauthorized: " + ap.authorized + "\nsid: " + ap.sid + "\nuid: " + ap.uid + "\nratio: " + ap.ratio + "\nskipRecording: " + ap.skipRecording + "\nrejectReason: " + ap.rejectReason + "\nuserTrackingState: " + ap.userTrackingState + "\npid: " + ap.pid);

    }

    public AuthResponse getAuthResponseObject() {
        return ap;
    }

    @Override
    public String toString(){
        return
                ("ip: " + ip + "\n" +
                        "authorized: " +authorized + "\n" +
                        "sid: " + sid + "\n" +
                        "uid: " + uid + "\n" +
                        "ratio: " + ratio + "\n" +
                        "skipRecording: " + skipRecording + "\n" +
                        "rejectReason: " + rejectReason + "\n" +
                        "userTrackingState: " + userTrackingState + "\n");
    }

}

