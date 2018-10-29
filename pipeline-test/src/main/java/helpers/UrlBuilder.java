package helpers;
import com.pipe.pipeapp.Configuration;
import com.pipe.pipeapp.SessionStream;
import org.testng.annotations.Optional;

import java.text.ParseException;

public class UrlBuilder {

    StringBuilder authUrl = new StringBuilder();
    public StringBuilder buildAuthUrl(String baseAuthUrl,int pid, int subsId,String msgSize) {

        if (baseAuthUrl.equals("")) {
            authUrl.append(defaultSettingAuthUrlName());

        } else {
            authUrl.append(baseAuthUrl);

        }

        if(pid==-1) {
            authUrl.append("?pid=" + defaultSettingPID() + "");
        }else{
            authUrl.append("?pid="+pid);
        }

        authUrl.append("&as=1");

     //   authUrl.append("&skiprnd=1");

        if(subsId == -1){
            authUrl.append("&subsid=" + defaultSettiongsubsId());

        }else{
            authUrl.append("&subsid=" + (Integer.toString(subsId)+""));
        }

        authUrl.append("&msgsize="+defaultSettingMsgSize());

        System.out.println(authUrl);
        return authUrl;
    }

    private String defaultSettiongsubsId(){
        return Configuration.prop.getProperty("suite.pusher.defaultParams.subsId");
    }
    private String defaultSettingMsgSize(){
        return Configuration.prop.getProperty("suite.pusher.defaultParams.msgSize");
    }

    private String defaultSettingPID() {
        return Configuration.prop.getProperty("suite.pusher.defaultParams.optionalPid");
    }

    private String defaultSettingAuthUrlName () {
        return Configuration.prop.getProperty("suite.pusher.defaultParams.baseAuth");
    }

    private String defaultSettingDnsName(){
        return Configuration.prop.getProperty("suite.pusher.defaultParams.dnsName");
    }

    public StringBuilder buildStreamUrl(StringBuilder dnsName){
        return dnsName.append("wr/?");
    }

//    public String buildStreamV16(String sid, SessionStream streamData, int subsId, String dnsName) {
//        StringBuilder finalDnsName = new StringBuilder();
//
//        if (dnsName == "") {
//            finalDnsName.append(defaultSettingDnsName());
//        } else {
//            finalDnsName.append(dnsName);
//        }
//        String stream = buildStreamUrl(finalDnsName) + sid + "&" + defaultSettingPID() + "&" + "10" + "&" + Integer.toString(streamData.messageNumber) + "&" + Integer.toString(streamData.streamId) + "&" +
//                Integer.toString(streamData.streamMessageId) + "&" + Integer.toString(streamData.dataFlagType + 256);
//
//        if (subsId == -1) {
//        }
//
//    }

     public String buildStream(String sid, SessionStream streamData, int pid, int subsId, String dnsName){


        if(dnsName.equals("")){
            dnsName=defaultSettingDnsName();
        }

        if(pid == -1){
           pid=Integer.parseInt(defaultSettingPID());
        }

        if(subsId == -1)
            subsId=Integer.parseInt(defaultSettiongsubsId());

        String streamUrl = dnsName + "wr/?" + sid + "&" + Integer.toString(pid) + "&f&" + Integer.toString(streamData.getMessageNumber()) +
                "&" + Integer.toString(streamData.getStreamId()) + "&" + Integer.toString(streamData.getStreamMessageId()) + "&" + Integer.toString(streamData.getDataFlagType()) +
                "&subsid=" + Integer.toString(subsId);

        return streamUrl;

    }
}
