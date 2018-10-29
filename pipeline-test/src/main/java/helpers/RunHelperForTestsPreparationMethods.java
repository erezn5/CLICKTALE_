package helpers;

import com.pipe.pipeapp.Configuration;
import com.pipe.pipeapp.HttpGetRequest;
import com.pipe.pipeapp.PushSession;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import org.json.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeoutException;

public class RunHelperForTestsPreparationMethods {

    Amazon aws;
    CageArchivePackage cageArchivePackage;
    PushSession pushSession = new PushSession();
    JsonActions jsonActions = new JsonActions();
    String sid;
    String[] list = null;
    boolean isLast;
    String[] json;
    String jsonDotNetPathFile;
    JSONArray jsonObjectsFromDotNet = new JSONArray();

    int soferSessions=0;
    private static final Logger logger = LoggerFactory.getLogger(RunHelperForTestsPreparationMethods.class);

    public void runHelperPreparationFromBothCages(String str, String[] list) throws SAXException, InterruptedException, ParseException, IOException, SQLException, ParserConfigurationException {

        this.cageArchivePackage = this.pushSession.testPushSessionBothCages(str);

        JSONObject jsonObjectDotNet = this.jsonActions.convertStringToJson(this.cageArchivePackage.getRecording());

        this.sid = jsonObjectDotNet.get("SID").toString();

    }
    public String[] runHelperPreparation(String str, String[] list) throws SAXException, InterruptedException, ParseException, IOException, SQLException, ParserConfigurationException, NoSuchAlgorithmException {

        this.aws = new Amazon(Configuration.prop.getProperty("suite.pusher.defaultParams.processorBucketName"));
        FileHandling fh = new FileHandling();
        if ((this.aws.countNumberOfObjectsInBucket()) != 0) {
            this.aws.deleteAllObjetsFromBucketS3();
        }


        //Getting cage response
        this.cageArchivePackage = this.pushSession.testPushSession(str);


        //Getting sid from cage response
        JSONObject jsonObjectDotNet = this.jsonActions.convertStringToJson(this.cageArchivePackage.getRecording());
        this.sid = jsonObjectDotNet.get("SID").toString();

        //getting all files in pipe folder (xml, dsr,html,json)
        int counter=0;
        String filePath = aws.downloadAnObjectAndReturnFolderPath();
        int i=0;

        if(filePath.equals("")){
            int counterLoop=0;
            for(;i<5;i++) {

                filePath = aws.downloadAnObjectAndReturnFolderPath();
                logger.info("Trying getting all files from pipe folder...attempt: " + i + "...");
                Thread.sleep(5000);
                if (!filePath.equals("")) {
                    break;
                }
            }
            if(i==5){

                //Getting cage response
                cageArchivePackage = pushSession.testPushSession(str);

                //Getting sid from cage response
                jsonObjectDotNet = new JSONObject();
                jsonObjectDotNet = jsonActions.convertStringToJson(cageArchivePackage.getRecording());
                this.sid = jsonObjectDotNet.get("SID").toString();

                //getting all files in pipe folder (xml, dsr,html,json)
                filePath = aws.downloadAnObjectAndReturnFolderPath();

                for( i=0;i<5;i++){
                    filePath = aws.downloadAnObjectAndReturnFolderPath();
                    logger.info("Trying getting all files from pipe folder...attempt: " + i + "...");
                    Thread.sleep(5000);
                    if(!filePath.equals("")) {
                        break;
                    }
                }

            }

            if(i==5)
                Assert.assertTrue(false, "Test Failed... Could not push session");

        }

        this.list = fh.filesListFromFolder(filePath + "\\");

        //getting name of folder using md5
        String md5 = fh.calcMD5AndReturnMD5Date(sid);

        logger.info("Waiting 10 seconds for second run...");
        Thread.sleep(10000);
        logger.info("Returning list with pipe files for SID: " + this.sid );
        return this.list;

    }

    public String getSid(){
        return this.sid;
    }

    public void runHelperPreparationWithoutPipe(String str) throws SAXException, InterruptedException, ParseException, IOException, SQLException, ParserConfigurationException {

        this.cageArchivePackage = this.pushSession.testPushSession(str);
        Thread.sleep(5000);
        //Getting sid from cage response
        JSONObject jsonObjectDotNet = this.jsonActions.convertStringToJson(this.cageArchivePackage.getRecording());
        jsonObjectsFromDotNet.put(jsonObjectDotNet);
        this.sid = jsonObjectDotNet.get("SID").toString();
        logger.info("-------------------------------------------------------------------------------------------------------------------------");
        logger.info("Recording result for sid: " + this.sid + " is: " + jsonObjectDotNet.toString());
        logger.info("-------------------------------------------------------------------------------------------------------------------------");
//        this.sidArray[this.soferSessions]=sid;
        this.soferSessions++;

        logger.info("Session ID:" + sid);
    }

    public JSONArray getJsonObjectsFromDotNet() {
        return jsonObjectsFromDotNet;
    }

    public String runHelperPreparationWithoutDotNet(String str) throws ParseException, InterruptedException, IOException, NoSuchAlgorithmException {
        this.aws = new Amazon(Configuration.prop.getProperty("suite.pusher.defaultParams.processorBucketName"));
//        FileHandling fh = new FileHandling();
//        if ((this.aws.countNumberOfObjectsInBucket()) != 0) {
//            this.aws.deleteAllObjetsFromBucketS3();
//        }
        this.sid=pushSession.pushSessionWithoutCageForDotNet(str);

        Thread.sleep(5000);

        //getting all files in pipe folder (xml, dsr,html,json)
//     //   String key = fh.calcMD5AndReturnMD5Date(sid);
//        String filePath = aws.downloadAnObjectAndReturnFolderPath();
//
//        list = fh.filesListFromFolder(filePath);

//        this.sidArray[this.soferSessions]=sid;
        this.soferSessions++;

        logger.info("Session ID:" + this.sid);
        return  this.sid;
    }

}
