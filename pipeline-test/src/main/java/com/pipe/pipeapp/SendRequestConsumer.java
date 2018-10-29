package com.pipe.pipeapp;

import helpers.UrlBuilder;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.IOUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.function.Consumer;

public class SendRequestConsumer implements Consumer<JSONObject> {
    private int count = 0;
    public JSONObject lastMessage;
    private AuthResponse m_ap;
    private String subsId = Configuration.prop.getProperty("suite.pusher.defaultParams.subsId");
    private String domain = Configuration.prop.getProperty("suite.pusher.defaultParams.domain");
    private String dnsName = Configuration.prop.getProperty("suite.pusher.defaultParams.dnsName");
    private String pid = Configuration.prop.getProperty("suite.pusher.defaultParams.optionalPid");
    private UrlBuilder ub = new UrlBuilder();
    private SessionStream s;
    private int size;
    private static final Logger logger = LoggerFactory.getLogger(SendRequestConsumer.class);

    public SendRequestConsumer(){
        ;
    }

    public SendRequestConsumer(AuthResponse i_ap, JSONArray i_jsonArray) {
        m_ap = i_ap;
        Integer.parseInt(subsId);
        size = i_jsonArray.size();

    }

//    @Override
//    public ConsumerNew<JSONObject> andThen(ConsumerNew<? super JSONObject> after) {
//        return null;
//    }

    @Override
    public void accept(JSONObject json) {

        // int messageType = (int) json.get("DataFlagType");
        int intMessageType = Integer.parseInt(json.get("DataFlagType").toString());
        int messageNumber = Integer.parseInt(json.get("MessageNumber").toString());
        long longSid = Long.parseLong(m_ap.sid);
        int intSubsId = Integer.parseInt(subsId);
        int intPid = Integer.parseInt(pid);
       // logger.info("message type: " + intMessageType + ",\n");// + " message number: " + messageNumber +",\n");

        List<Integer> mtl = Arrays.asList(new Integer[]{1, 9, 257, 265});

        if (!(mtl.contains(intMessageType))) {

            try {
                logger.info(intMessageType + ",");
                sendStream(longSid, Integer.parseInt(pid), intSubsId, json, domain, dnsName, intMessageType, messageNumber);
                count++;
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //         count++;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            lastMessage = json;

        }

   //     logger.info("Number: " + count);
    }


    public String sendStream(long sid, int pid, int subsId, JSONObject sessionMessage, String domain, String dnsName, int sendStreammessageType, int messageNumber) throws IOException, InterruptedException {

        //TODO add get request method in order to handle future get request , please see scala code for reference (sendStream2 in SendSession object)

        SessionStream s = new SessionStream(sessionMessage.toString());

        String sessionStream = ub.buildStream(m_ap.sid, s, -1, subsId, dnsName);
        //TODO insert optional values for isProtocol16 and optionalUrlSuffix -> ub.buildStream(

        String res = sendPostRequest(sessionStream, s.getS().Data, domain, sendStreammessageType, messageNumber);



        return res;

    }

    String sendPostRequest(String url, String body, String domain, int messageType, int messageNumber) throws IOException, InterruptedException {
        // logger.info("url to send post request is: " + url);
        HttpClient client = HttpClientBuilder.create().build();
        byte[] decoded;
        HttpPost httpPost = new HttpPost(url);
        //set the desired header values
        if (!domain.equals(""))
            httpPost.setHeader("Origin", Configuration.prop.getProperty("suite.pusher.defaultParams.domain"));
//        if(messageType == 104){
//            decoded = Base64.getDecoder().decode(body.getBytes());
//        }else{
//            decoded = body.getBytes();
//        }
//
//        if(messageType == 105){
//            Thread.sleep(2000);
//            decoded = Base64.getDecoder().decode(body.getBytes());
//        }else{
//            decoded = body.getBytes();
//        }

       // Thread.sleep(5000);
        if ((messageType == 104) || (messageType == 105)) {
             //   Thread.sleep(5000);
            if(messageType == 105 ){
                Thread.sleep(4000);
            }
            decoded = Base64.getDecoder().decode(body.getBytes());
        } else {
            decoded = body.getBytes();
        }

        logger.info("message type: " + messageType + ",\n");
        ByteArrayEntity byteArrayEntity = new ByteArrayEntity(decoded);
        httpPost.setEntity(byteArrayEntity);
        //execute the request

        HttpResponse response = client.execute(httpPost);
        HttpEntity entity = response.getEntity();
        String responseInStr = EntityUtils.toString(entity);

        return responseInStr;

    }

}
