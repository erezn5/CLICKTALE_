package com.pipe.pipeapp;
import helpers.CageArchivePackage;
import helpers.FileHandling;
import helpers.JsonActions;
import helpers.UrlBuilder;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Base64;
import java.util.List;




public class PushSession {

    UrlBuilder ub;
    HttpGetRequest m_httpGetAuthRequest = null;
    StringBuilder m_url;
    AuthResponse m_ap = null;
    CageArchivePackage cageArchivePackage = null;
    CageArchivePackage cageArchivePackageNET = null;
    CageArchivePackage cageArchivePackagePipe = null;
    String m_result = "";
    StringBuilder sb = new StringBuilder();
    JSONArray jsonArray = new JSONArray();
    int sessionStreamsDealayFromAuth = 0;
    String filePath;
    JSONObject jsonObject = null;
    String dotNetName = "_.NET";
    String pipeName ="_PIPE";
    JsonActions jsonActions;
    FileHandling fh = null;
    List<List<PusherParams>> pushArguments = null;
    private static final Logger logger = LoggerFactory.getLogger(PushSession.class);

    public CageArchivePackage testPushSession(String filePath) throws IOException, ParseException, InterruptedException, ParserConfigurationException, SAXException, SQLException {
        this.m_url=new StringBuilder();
        this.ub = new UrlBuilder();
        this.jsonActions = new JsonActions();
        boolean isMobile = false;
        int subsId = Integer.parseInt(Configuration.prop.getProperty("suite.pusher.defaultParams.subsId"));
        int pid = Integer.parseInt(Configuration.prop.getProperty("suite.pusher.defaultParams.optionalPid"));
        String domain = Configuration.prop.getProperty("suite.pushe" +
                "r.defaultParams.domain");
        String dnsName = Configuration.prop.getProperty("suite.pusher.defaultParams.dnsName");
        //Create folder to save all the sessions
        String archiveFilesFolder = "C:\\temp\\ArchivedFilesFromPusher\\test\\"; //TODO change to realtive path
        this.fh = new FileHandling(archiveFilesFolder);
        this.fh.createFolder();
        //build the auth url for get request
        logger.info("Building auth url for the get request...");
        this.m_url = ub.buildAuthUrl("", -1, -1, "");
        //send the auth request itself
        logger.info("Send the auth request itself...");
        if(filePath.equals("C:\\Git\\pipe-Regressions\\pipeline-test\\src\\main\\resources\\recFiles\\reciPhone6.json")){
            isMobile = true;
        }
        if (checkRecordingState(isMobile) == true) {
            logger.info("Auth user tracking state is: " + m_ap.userTrackingState);
        } else {
            logger.info("Auth user tracking state is: " + m_ap.userTrackingState);

        }

        logger.info("Returning the parsed auth response...");


        logger.info(String.valueOf(DateTime.now(DateTimeZone.UTC)));

        try {
            Thread.sleep(sessionStreamsDealayFromAuth);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //parsing json text file from resources folder
        logger.info("Parsing json text file from resources folder...");
        jsonArray = jsonActions.parseJsonFromResourcesFolderAndReturnLiveSessionDataCollectionJsonArray(filePath);


        //Send parallel requests with Json details
        logger.info("Send parallel requests...");
        JSONObject lastMessage = null;
        //      --------------------------------------------------------------------------------------
        SendRequestConsumer src = new SendRequestConsumer(m_ap, jsonArray);
        jsonArray.parallelStream().forEach(src);
        //      --------------------------------------------------------------------------------------

        int messageType = Integer.parseInt(src.lastMessage.get("DataFlagType").toString());
        int messageNumber = Integer.parseInt(src.lastMessage.get("MessageNumber").toString());
        //Send last message request in order to ignite
        src.sendStream(Long.parseLong(m_ap.sid), pid, subsId, src.lastMessage, domain, dnsName, messageType, messageNumber);
        //
        logger.info("\n ------------------------------------------------------  FINISHED PUSHING ------------------------------------------------------ \n sid = " + m_ap.sid + "\n pid =" + pid + " & subsid = " + subsId);
        String check = Configuration.prop.getProperty("suite.pusher.defaultParams.cageDotNet") + "/" + Configuration.prop.getProperty("suite.pusher.defaultParams.subsId") + "/" + Configuration.prop.getProperty("suite.pusher.defaultParams.optionalPid") + "/" + m_ap.sid;
        logger.info("Cage url with relevant parameters: \n" + Configuration.prop.getProperty("suite.pusher.defaultParams.cageDotNet") + "/" + Configuration.prop.getProperty("suite.pusher.defaultParams.subsId") + "/" + Configuration.prop.getProperty("suite.pusher.defaultParams.optionalPid") + "/" + m_ap.sid);
        logger.info("\nUID: " + m_ap.uid);

        //Getting .NET response from cage
        HttpGetRequest cageResponse = new HttpGetRequest(new StringBuilder(Configuration.prop.getProperty("suite.pusher.defaultParams.cageDotNet")));


        if(filePath.equals("C:\\Git\\pipe-Regressions\\pipeline-test\\src\\main\\resources\\recFiles\\recBigData.json")||filePath.equals("C:\\Git\\pipe-Regressions\\pipeline-test\\src\\main\\resources\\recFiles\\1730211840426349_BigXML.json")){
            Thread.sleep(7000);
        }else{

            Thread.sleep(2000);
        }

        String response = cageResponse.getRequest(Configuration.prop.getProperty("suite.pusher.defaultParams.cageDotNet") + "/" + Configuration.prop.getProperty("suite.pusher.defaultParams.subsId") + "/" + Configuration.prop.getProperty("suite.pusher.defaultParams.optionalPid") + "/" + m_ap.sid);
        cageArchivePackage = new CageArchivePackage(response);
        cageArchivePackage = cageArchivePackage.getCageArchivePackageObject();



        saveSessionArchiveToDisk(subsId, pid, Long.parseLong(m_ap.sid), true, archiveFilesFolder, filePath);

        return cageArchivePackage;

      //  String webPageHash = cageArchivePackage.getEvents().split("webPageHash=\"")[1].split("\" version")[0];

    }

    public String[] returnBothCageArchivePackageAfterPushSession(String filePath) throws InterruptedException, IOException, ParseException {

        CageArchivePackage[] cageArchivePackagesArray = new CageArchivePackage[2];

        this.m_url=new StringBuilder();
        this.ub = new UrlBuilder();
        this.jsonActions = new JsonActions();
        int subsId = Integer.parseInt(Configuration.prop.getProperty("suite.pusher.defaultParams.subsId"));
        int pid = Integer.parseInt(Configuration.prop.getProperty("suite.pusher.defaultParams.optionalPid"));
        String domain = Configuration.prop.getProperty("suite.pusher.defaultParams.domain");
        String dnsName = Configuration.prop.getProperty("suite.pusher.defaultParams.dnsName");
        logger.info("Building auth url for the get request...");
        this.m_url = ub.buildAuthUrl("", -1, -1, "");
        //send the auth request itself
        logger.info("Send the auth request itself...");
        boolean isMobile = false;
        if (checkRecordingState(isMobile) == true) {
            logger.info("Auth user tracking state is: " + m_ap.userTrackingState);
        } else {
            logger.info("Auth user tracking state is: " + m_ap.userTrackingState);

        }

        logger.info("Returning the parsed auth response...");


        logger.info(String.valueOf(DateTime.now(DateTimeZone.UTC)));


        //parsing json text file from resources folder
        logger.info("Parsing json text file from resources folder...");
        jsonArray = jsonActions.parseJsonFromResourcesFolderAndReturnLiveSessionDataCollectionJsonArray(filePath);


        //Send parallel requests with Json details
        logger.info("Send parallel requests...");
        JSONObject lastMessage = null;
        //      --------------------------------------------------------------------------------------
        SendRequestConsumer src = new SendRequestConsumer(m_ap, jsonArray);
        jsonArray.parallelStream().forEach(src);
        //      --------------------------------------------------------------------------------------

        int messageType = Integer.parseInt(src.lastMessage.get("DataFlagType").toString());
        int messageNumber = Integer.parseInt(src.lastMessage.get("MessageNumber").toString());
        //Send last message request in order to ignite
        src.sendStream(Long.parseLong(m_ap.sid), pid, subsId, src.lastMessage, domain, dnsName, messageType, messageNumber);
        //
        logger.info("\n ------------------------------------------------------  FINISHED PUSHING ------------------------------------------------------ \n sid = " + m_ap.sid + "\n pid =" + pid + " & subsid = " + subsId);
        String check = Configuration.prop.getProperty("suite.pusher.defaultParams.cageDotNet") + "/" + Configuration.prop.getProperty("suite.pusher.defaultParams.subsId") + "/" + Configuration.prop.getProperty("suite.pusher.defaultParams.optionalPid") + "/" + m_ap.sid;
        logger.info("Cage url with relevant parameters: \n" + Configuration.prop.getProperty("suite.pusher.defaultParams.cageDotNet") + "/" + Configuration.prop.getProperty("suite.pusher.defaultParams.subsId") + "/" + Configuration.prop.getProperty("suite.pusher.defaultParams.optionalPid") + "/" + m_ap.sid);
        logger.info("Cage url with relevant parameters: \n" + Configuration.prop.getProperty("suite.pusher.defaultParams.cagePipe") + "/" + Configuration.prop.getProperty("suite.pusher.defaultParams.subsId") + "/" + Configuration.prop.getProperty("suite.pusher.defaultParams.optionalPid") + "/" + m_ap.sid);
        logger.info("\nUID: " + m_ap.uid);

        Thread.sleep(3000);

        //Getting .NET response from cage
        HttpGetRequest cageResponse = new HttpGetRequest(new StringBuilder(Configuration.prop.getProperty("suite.pusher.defaultParams.cageDotNet")));
        //Getting pipe response from cage
        HttpGetRequest cageResponsePipe = new HttpGetRequest(new StringBuilder(Configuration.prop.getProperty("suite.pusher.defaultParams.cagePipe")));

        String responseNET = cageResponse.getRequest(Configuration.prop.getProperty("suite.pusher.defaultParams.cageDotNet") + "/" + Configuration.prop.getProperty("suite.pusher.defaultParams.subsId") + "/" + Configuration.prop.getProperty("suite.pusher.defaultParams.optionalPid") + "/" + m_ap.sid);
        cageArchivePackagesArray[0] = new CageArchivePackage(responseNET);
        cageArchivePackagesArray[0] = cageArchivePackagesArray[0].getCageArchivePackageObject();
        Thread.sleep(2000);
        String responsePipe = cageResponsePipe.getRequest(Configuration.prop.getProperty("suite.pusher.defaultParams.cagePipe")+ "/" + Configuration.prop.getProperty("suite.pusher.defaultParams.subsId") + "/" + Configuration.prop.getProperty("suite.pusher.defaultParams.optionalPid") + "/" + m_ap.sid);
        cageArchivePackagesArray[1]= new CageArchivePackage(responsePipe);
        cageArchivePackagesArray[1] = cageArchivePackagesArray[1].getCageArchivePackageObject();
        String[] recordingArray = new String[2];
        recordingArray[0] = cageArchivePackagesArray[0].getRecording();
        recordingArray[1] = cageArchivePackagesArray[1].getRecording();


        return recordingArray;


    }

    public CageArchivePackage testPushSessionBothCages(String filePath) throws IOException, ParseException, InterruptedException, ParserConfigurationException, SAXException, SQLException {
        this.m_url=new StringBuilder();
        this.ub = new UrlBuilder();
        this.jsonActions = new JsonActions();
        boolean isMobile = false;
        int subsId = Integer.parseInt(Configuration.prop.getProperty("suite.pusher.defaultParams.subsId"));
        int pid = Integer.parseInt(Configuration.prop.getProperty("suite.pusher.defaultParams.optionalPid"));
        String domain = Configuration.prop.getProperty("suite.pusher.defaultParams.domain");
        String dnsName = Configuration.prop.getProperty("suite.pusher.defaultParams.dnsName");
        //Create folder to save all the sessions
        String archiveFilesFolder = "C:\\temp\\ArchivedFilesFromPusher\\test\\"; //TODO change to realtive path
        this.fh = new FileHandling(archiveFilesFolder);
        this.fh.createFolder();
        //build the auth url for get request
        logger.info("Building auth url for the get request...");
        this.m_url = ub.buildAuthUrl("", -1, -1, "");
        //send the auth request itself
        logger.info("Send the auth request itself...");
        if(filePath.equals("C:\\Git\\pipe-Regressions\\pipeline-test\\src\\main\\resources\\recFiles\\reciPhone6.json")){
            isMobile = true;
        }
        if (checkRecordingState(isMobile) == true) {
            logger.info("Auth user tracking state is: " + m_ap.userTrackingState);
        } else {
            logger.info("Auth user tracking state is: " + m_ap.userTrackingState);

        }

        logger.info("Returning the parsed auth response...");


        logger.info(String.valueOf(DateTime.now(DateTimeZone.UTC)));

        try {
            Thread.sleep(sessionStreamsDealayFromAuth);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //parsing json text file from resources folder
        logger.info("Parsing json text file from resources folder...");
        jsonArray = jsonActions.parseJsonFromResourcesFolderAndReturnLiveSessionDataCollectionJsonArray(filePath);


        //Send parallel requests with Json details
        logger.info("Send parallel requests...");
        JSONObject lastMessage = null;
        //      --------------------------------------------------------------------------------------
        SendRequestConsumer src = new SendRequestConsumer(m_ap, jsonArray);
        jsonArray.parallelStream().forEach(src);
        //      --------------------------------------------------------------------------------------

        int messageType = Integer.parseInt(src.lastMessage.get("DataFlagType").toString());
        int messageNumber = Integer.parseInt(src.lastMessage.get("MessageNumber").toString());
        //Send last message request in order to ignite
        src.sendStream(Long.parseLong(m_ap.sid), pid, subsId, src.lastMessage, domain, dnsName, messageType, messageNumber);
        //
        logger.info("\n ------------------------------------------------------  FINISHED PUSHING ------------------------------------------------------ \n sid = " + m_ap.sid + "\n pid =" + pid + " & subsid = " + subsId);
        String check = Configuration.prop.getProperty("suite.pusher.defaultParams.cageDotNet") + "/" + Configuration.prop.getProperty("suite.pusher.defaultParams.subsId") + "/" + Configuration.prop.getProperty("suite.pusher.defaultParams.optionalPid") + "/" + m_ap.sid;
        logger.info("Cage url with relevant parameters: \n" + Configuration.prop.getProperty("suite.pusher.defaultParams.cageDotNet") + "/" + Configuration.prop.getProperty("suite.pusher.defaultParams.subsId") + "/" + Configuration.prop.getProperty("suite.pusher.defaultParams.optionalPid") + "/" + m_ap.sid);
        logger.info("Cage url with relevant parameters: \n" + Configuration.prop.getProperty("suite.pusher.defaultParams.cagePipe") + "/" + Configuration.prop.getProperty("suite.pusher.defaultParams.subsId") + "/" + Configuration.prop.getProperty("suite.pusher.defaultParams.optionalPid") + "/" + m_ap.sid);
        logger.info("\nUID: " + m_ap.uid);
        //Getting .NET response from cage
        HttpGetRequest cageResponse = new HttpGetRequest(new StringBuilder(Configuration.prop.getProperty("suite.pusher.defaultParams.cageDotNet")));
        //Getting pipe response from cage
        HttpGetRequest cageResponsePipe = new HttpGetRequest(new StringBuilder(Configuration.prop.getProperty("suite.pusher.defaultParams.cagePipe")));
        if(filePath.equals("C:\\Git\\pipe-Regressions\\pipeline-test\\src\\main\\resources\\recFiles\\recBigData.json")||filePath.equals("C:\\Git\\pipe-Regressions\\pipeline-test\\src\\main\\resources\\recFiles\\1730211840426349_BigXML.json")){
            Thread.sleep(7000);
        }else{

            Thread.sleep(2000);
        }

        String responseNET = cageResponse.getRequest(Configuration.prop.getProperty("suite.pusher.defaultParams.cageDotNet") + "/" + Configuration.prop.getProperty("suite.pusher.defaultParams.subsId") + "/" + Configuration.prop.getProperty("suite.pusher.defaultParams.optionalPid") + "/" + m_ap.sid);
        cageArchivePackageNET = new CageArchivePackage(responseNET);
        cageArchivePackageNET = cageArchivePackageNET.getCageArchivePackageObject();
        Thread.sleep(2000);
        String responsePipe = cageResponsePipe.getRequest(Configuration.prop.getProperty("suite.pusher.defaultParams.cagePipe")+ "/" + Configuration.prop.getProperty("suite.pusher.defaultParams.subsId") + "/" + Configuration.prop.getProperty("suite.pusher.defaultParams.optionalPid") + "/" + m_ap.sid);
        cageArchivePackagePipe = new CageArchivePackage(responsePipe);
        cageArchivePackagePipe = cageArchivePackagePipe.getCageArchivePackageObject();

        saveSessionArchiveToDiskBothCages(subsId,pid,Long.parseLong(m_ap.sid),true,archiveFilesFolder,filePath);

        return cageArchivePackageNET;

        //  String webPageHash = cageArchivePackage.getEvents().split("webPageHash=\"")[1].split("\" version")[0];

    }
    public String pushSessionWithoutCageForDotNet(String filePath) throws IOException, ParseException, InterruptedException {
        this.m_url=new StringBuilder();
        this.ub = new UrlBuilder();
        this.jsonActions = new JsonActions();
        boolean isMobile = false;
        int subsId = Integer.parseInt(Configuration.prop.getProperty("suite.pusher.defaultParams.subsId"));
        int pid = Integer.parseInt(Configuration.prop.getProperty("suite.pusher.defaultParams.optionalPid"));
        String domain = Configuration.prop.getProperty("suite.pusher.defaultParams.domain");
        String dnsName = Configuration.prop.getProperty("suite.pusher.defaultParams.dnsName");
        //Create folder to save all the sessions
        String archiveFilesFolder = "C:\\temp\\ArchivedFilesFromPusher\\test\\"; //TODO change to realtive path
        this.fh = new FileHandling(archiveFilesFolder);
        this.fh.createFolder();
        //build the auth url for get request
        logger.info("Building auth url for the get request...");
        this.m_url = ub.buildAuthUrl("", -1, -1, "");
        //send the auth request itself
        logger.info("Send the auth request itself...");
        if(filePath.equals("C:\\Git\\Pipe-Regressions\\pipeline-test\\src\\main\\resources\\recFiles\\reciPhone6.json")){
            isMobile = true;
        }

       if (checkRecordingState(isMobile) == true) {
           logger.info("Auth user tracking state is: " + m_ap.userTrackingState);
        } else {
           logger.info("Auth user tracking state is: " + m_ap.userTrackingState);

        }

        logger.info("Returning the parsed auth response...");


        logger.info(String.valueOf(DateTime.now(DateTimeZone.UTC)));

        try {
            Thread.sleep(sessionStreamsDealayFromAuth);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //parsing json text file from resources folder
        logger.info("Parsing json text file from resources folder...");
        try {
            this.jsonArray = this.jsonActions.parseJsonFromResourcesFolderAndReturnLiveSessionDataCollectionJsonArray(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }


        //Send parallel requests with Json details
        logger.info("Send parallel requests...");
        JSONObject lastMessage = null;
        //      --------------------------------------------------------------------------------------
        SendRequestConsumer src = new SendRequestConsumer(m_ap, jsonArray);
        jsonArray.parallelStream().forEach(src);
        //      --------------------------------------------------------------------------------------

        int messageType = Integer.parseInt(src.lastMessage.get("DataFlagType").toString());
        int messageNumber = Integer.parseInt(src.lastMessage.get("MessageNumber").toString());
        //Send last message request in order to ignite
        src.sendStream(Long.parseLong(m_ap.sid), pid, subsId, src.lastMessage, domain, dnsName, messageType, messageNumber);

        HttpGetRequest cageResponsePipe = new HttpGetRequest(new StringBuilder(Configuration.prop.getProperty("suite.pusher.defaultParams.cagePipe")));
        String responsePipe = cageResponsePipe.getRequest(Configuration.prop.getProperty("suite.pusher.defaultParams.cagePipe")+ "/" + Configuration.prop.getProperty("suite.pusher.defaultParams.subsId") + "/" + Configuration.prop.getProperty("suite.pusher.defaultParams.optionalPid") + "/" + m_ap.sid);
        cageArchivePackagePipe = new CageArchivePackage(responsePipe);
        cageArchivePackagePipe = cageArchivePackagePipe.getCageArchivePackageObject();
        this.cageArchivePackage = cageArchivePackagePipe;

        saveSessionArchiveToDisk(subsId,pid,Long.parseLong(m_ap.sid),true,archiveFilesFolder,"C:\\Git\\pipeline-test\\src\\main\\resources\\recFiles\\rec.json");
        logger.info("\n ------------------------------------------------------  FINISHED PUSHING ------------------------------------------------------ \n sid = " + m_ap.sid + "\n pid =" + pid + " & subsid = " + subsId);
        return this.m_ap.sid;
    }

    private void printCageAssetes(String recording, String streams, String events, String webPage) {

        logger.info("-------------------------------------------------------------------------------------");
        logger.info("recording: " + cageArchivePackage.getRecording());
        logger.info("-------------------------------------------------------------------------------------");
        logger.info("streams: " + cageArchivePackage.getStreams());
        logger.info("-------------------------------------------------------------------------------------");
        logger.info("Events: " + cageArchivePackage.getEvents());
        logger.info("-------------------------------------------------------------------------------------");
        logger.info("webpage: " + cageArchivePackage.getWebPage());
        logger.info("webPageHash " + cageArchivePackage.getWebPageHash());
    }


    public boolean checkRecordingState(boolean isMobile) {

        m_httpGetAuthRequest = new HttpGetRequest(this.m_url);
        this.m_result = null;

        boolean flag = true;
        int i;
        for (i = 0; i < 10; i++) {

            m_result = m_httpGetAuthRequest.sendAuthRequest(isMobile);

            m_ap = new AuthResponse(m_result);
            m_ap = m_ap.getAuthResponseObject();
            if (!(m_ap.userTrackingState.equals("Recording"))) {
                continue;

            } else {
                if (m_ap.userTrackingState.equals("Recording")) {
                    logger.info("Recording state was achieved after: " + i + " tries.");
                    break;
                }
            }

            flag = false;

        }


        logger.info(m_result);
        return flag;
    }


    public String sendPostRequest(String url, String data, String domain, int dataFlagType) throws IOException {
        String res = "";
        HttpClient client = HttpClientBuilder.create().build();
        byte[] decoded;
        HttpPost httpPost = new HttpPost(url);
        //set the desired header values
        if (!domain.equals(""))
            httpPost.setHeader("Origin", Configuration.prop.getProperty("suite.pusher.defaultParams.domain"));

        if ((dataFlagType == 104) || (dataFlagType == 105)) {
            decoded = Base64.getDecoder().decode(data.getBytes());
        } else {
            decoded = data.getBytes();
        }

        ByteArrayEntity byteArrayEntity = new ByteArrayEntity(decoded);
        httpPost.setEntity(byteArrayEntity);
        //execute the request

        HttpResponse response = client.execute(httpPost);
        HttpEntity entity = response.getEntity();
        String responseInStr = EntityUtils.toString(entity);

        return responseInStr;

    }

    public void saveSessionsArchiveForDuplicatorTest(Long sid,String recordingStaging, String recordingProd, String eventsStaging, String eventProd,
                                                     String streamStaging, String streamProd){
        String subDirectory = "C:\\temp\\ArchivedFilesFromPusher\\test\\";
        String filePathStaging = subDirectory.concat(Long.toString(sid)).concat("_Staging");
        String filePathProd = subDirectory.concat(Long.toString(sid)).concat("_Prod");

        fh = new FileHandling(filePathStaging);
        fh.createFolder();
        fh.writeToFile(filePathStaging.concat("\\Staging_"+sid.toString()+".json"),recordingStaging);
        fh.writeToFile(filePathStaging.concat("\\Staging_"+sid+".xml"),eventsStaging);
        fh.writeToFile(filePathStaging.concat("\\Staging_"+sid+".dsr"),streamStaging);

        fh = new FileHandling(filePathProd);
        fh.createFolder();
        fh.writeToFile(filePathProd.concat("\\Prod_"+sid+".json"),recordingProd);
        fh.writeToFile(filePathProd.concat("\\Prod_"+sid+".xml"),eventProd);
        fh.writeToFile(filePathProd.concat("\\Prod_"+sid+".dsr"),streamProd);


    }

    public void saveSessionArchiveToDiskBothCages(int subsId, int pid, long sid, boolean isQa, String subDirectory, String inputFile) throws IOException {
        String[] ArrayDotNet = {cageArchivePackageNET.getRecording(),cageArchivePackageNET.getEvents(),cageArchivePackageNET.getStreams(),cageArchivePackageNET.getWebPage()};
        String[] ArrayPipe = {cageArchivePackagePipe.getRecording(),cageArchivePackagePipe.getEvents(),cageArchivePackagePipe.getStreams(),cageArchivePackagePipe.getWebPage()};

        String filePathDotNet = subDirectory.concat(Long.toString(sid)).concat(this.dotNetName);
        String filePathPipe = subDirectory.concat(Long.toString(sid)).concat(this.pipeName);
        fh = new FileHandling(filePathDotNet);
        fh.createFolder();

        fh.writeToFile(filePathDotNet.concat("\\dot-net_"+m_ap.sid+".json"), ArrayDotNet[0]);
        fh.writeToFile(filePathDotNet.concat("\\dot-net_"+m_ap.sid+".xml"), ArrayDotNet[1]);
        fh.writeToFile(filePathDotNet.concat("\\dot-net_"+m_ap.sid+".dsr"), ArrayDotNet[2]);
        fh.writeToFile(filePathDotNet.concat("\\dot-net_"+m_ap.sid+".html"), ArrayDotNet[3]);
        fh.copyFromSourceToDst(inputFile,"\\"+filePathDotNet);

        fh = new FileHandling(filePathPipe);
        fh.createFolder();

        fh.writeToFile(filePathPipe.concat("\\pipe_"+m_ap.sid+".json"), ArrayPipe[0]);
        fh.writeToFile(filePathPipe.concat("\\pipe_"+m_ap.sid+".xml"), ArrayPipe[1]);
        fh.writeToFile(filePathPipe.concat("\\pipe_"+m_ap.sid+".dsr"), ArrayPipe[2]);
        fh.writeToFile(filePathPipe.concat("\\pipe_"+m_ap.sid+".html"), ArrayPipe[3]);
//        fh.copyFromSourceToDst(inputFile,"\\"+filePathDotNet);

    }

    public void saveSessionArchiveToDisk(int subsId, int pid, long sid, boolean isQa, String subDirectory, String inputFile) throws IOException {
        //   absoulte
//        String[] Array = {cageArchivePackage.getRecording(), cageArchivePackage.getEvents(), cageArchivePackage.getStreams(), cageArchivePackage.getWebPage()};
        String[] Array = {cageArchivePackage.getRecording(), cageArchivePackage.getEvents(), cageArchivePackage.getStreams(), cageArchivePackage.getWebPage()};
        String filePath = subDirectory.concat(Long.toString(sid));

        fh = new FileHandling(filePath);
        fh.createFolder();


        fh.writeToFile(filePath.concat("\\dot-net.json"), Array[0]);
        fh.writeToFile(filePath.concat("\\dot-net.xml"), Array[1]);
        fh.writeToFile(filePath.concat("\\dot-net.dsr"), Array[2]);
        fh.writeToFile(filePath.concat("\\dot-net.html"), cageArchivePackage.getWebPage());
        fh.copyFromSourceToDst(inputFile,"\\"+filePath);

    }

    public void saveFullDataSessions(String recording, String recordingPipe, String events, String eventsPipe, String streams, String streamsPipe) throws ParseException {
        String filePath = "C:\\temp\\ArchivedFilesFromPusher\\test\\";//todo - relative path (properties files)
        String previous = "previous";

        JSONObject jsonObject = new JSONObject();
        jsonActions = new JsonActions();
        jsonObject=jsonActions.convertStringToJson(recording);
        String sid = jsonObject.get("SID").toString();
        String filePathPrevious = filePath.concat(sid+"_"+previous);

        fh = new FileHandling(filePathPrevious);
        fh.createFolder();

        fh.writeToFile(filePathPrevious.concat("\\"+previous+"_"+sid+".json"),recording);
        fh.writeToFile(filePathPrevious.concat("\\"+previous+"_"+sid+".xml"),events);
        fh.writeToFile(filePathPrevious.concat("\\"+previous+"_"+sid+".dsr"),streams);

        String current = "current";
        String filePathCurrent = filePath.concat(sid+"_"+current);
        fh = new FileHandling(filePathCurrent);
        fh.createFolder();

        fh.writeToFile(filePathCurrent.concat("\\"+current+"_"+sid+".json"),recordingPipe);
        fh.writeToFile(filePathCurrent.concat("\\"+current+"_"+sid+".xml"),eventsPipe);
        fh.writeToFile(filePathCurrent.concat("\\"+current+"_"+sid+".dsr"),streamsPipe);



    }
}
