package com.pipe.pipeapp;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.testng.annotations.*;

public final class PusherParams {
    String recFile="";
    String authState = "";
    int optionalPid = 0;// 26955;
    Boolean optionalMobile = false;
    int subsId = 0;//223;
    String domain = "";
    Boolean retry = true;
    String referrer = "";
    String dnsName = "";//"https://wrlinqa-aws.clicktale.net/ctn_v2/";
    String ua = "";
    Boolean isProtocolV16 = false;
    int messageCounter = 0;
    int sessionStreamsDelayFromAuth = 0;
    String optionalSuffix = "";
   // final static public Properties prop = new Properties();


    //In order to run other env please change you testng.xml to the desired env in edit configurations

    public PusherParams(){

         InputStream input = null;
        try{

            recFile =  Configuration.prop.getProperty("suite.pusher.defaultParams.recFile");
            authState = Configuration.prop.getProperty("suite.pusher.defaultParams.authState");
            optionalPid = Integer.parseInt(Configuration.prop.getProperty("suite.pusher.defaultParams.optionalPid"));
            optionalMobile = Boolean.parseBoolean(Configuration.prop.getProperty("suite.pusher.defaultParams.optionalMobile"));
            subsId = Integer.parseInt(Configuration.prop.getProperty("suite.pusher.defaultParams.subsId"));
            domain = Configuration.prop.getProperty("suite.pusher.defaultParams.domain");
            retry = Boolean.parseBoolean(Configuration.prop.getProperty("suite.pusher.defaultParams.retry"));
            referrer = Configuration.prop.getProperty("suite.pusher.defaultParams.referrer");
            dnsName = Configuration.prop.getProperty("suite.pusher.defaultParams.dnsName");
            ua = Configuration.prop.getProperty("suite.pusher.defaultParams.ua");
            isProtocolV16 = Boolean.getBoolean(Configuration.prop.getProperty("suite.pusher.defaultParams.isProtocolV16"));
            messageCounter = Integer.parseInt(Configuration.prop.getProperty("suite.pusher.defaultParams.messageCounter"));
            sessionStreamsDelayFromAuth = Integer.parseInt(Configuration.prop.getProperty("suite.pusher.defaultParams.sessionStreamsDelayFromAuth"));
            optionalSuffix = Configuration.prop.getProperty("suite.pusher.defaultParams.optionalSuffix");
            System.out.println(domain);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


    }

    public String getAuthState(){
        return authState;
    }


}
