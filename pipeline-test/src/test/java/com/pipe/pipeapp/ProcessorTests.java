package com.pipe.pipeapp;

import com.pipe.record.comparator.parser.ExternalRecordJsonParser;
import com.rabbitmq.client.Channel;
import helpers.*;
import junit.framework.TestCase;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.testng.Assert;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

public class ProcessorTests extends TestCase {
    private static final Logger LOGGER = Logger.getLogger(ProcessorTests.class.getName());
    JsonActions jsonActions;
    ExternalRecordJsonParser externalRecordJsonParser;
    Amazon aws;
    PushSession pushSession;
    FileHandling fh = new FileHandling();
    ArrayList<String> files;
    String path = "C:\\Git\\Pipe-Regressions\\pipeline-test\\src\\main\\resources\\recFiles\\";
    File f = new File(path);

    JSONObject childHTML = new JSONObject();
    private org.json.JSONObject objectIdHTML;
    private org.json.JSONObject mainObjHTML = new org.json.JSONObject();
    RunHelperForTestsPreparationMethods runHelperForTestsPreparationMethods;// = new RunHelperForTestsPreparationMethods();
    int i = 0;
    int count = 0;
    String m_bucketName = "nv-q1-s3-proclin2-03";
    String bucketDotNet = "nv-q-recordings";
    String[] sidArray;



    @Ignore
    @Test
    public void testRewriteRules() throws InterruptedException, NoSuchAlgorithmException, ParseException, IOException {
        int subsId = Integer.parseInt(Configuration.prop.getProperty("suite.pusher.defaultParams.subsId"));
        int pid = Integer.parseInt(Configuration.prop.getProperty("suite.pusher.defaultParams.optionalPid"));
        this.runHelperForTestsPreparationMethods = new RunHelperForTestsPreparationMethods();
        HttpGetRequest cageResponse = new HttpGetRequest();
        ExternalRecordJsonParser externalRecordJsonParser;
//        pushSession = new PushSession();
//todo push same json file to a project without pii configurations in RDS
        String str="C:\\Git\\pipe-Regressions\\pipeline-test\\src\\main\\resources\\recFiles\\adidas_pii_test.json";
//        if ((this.aws.countNumberOfObjectsInBucket()) != 0) {
//            this.aws.deleteAllObjetsFromBucketS3();
//        }

        System.out.println("-----------------------------------------------------------------------------------------------------------------------------------");
        System.out.println("running test for the following json input file: " + str);
        System.out.println("-----------------------------------------------------------------------------------------------------------------------------------");

        this.runHelperForTestsPreparationMethods.runHelperPreparationWithoutDotNet(str);

        String sid = this.runHelperForTestsPreparationMethods.getSid();

        System.out.println("Cage url with relevant parameters: \n" + Configuration.prop.getProperty("suite.pusher.defaultParams.cagePipe") + "/" + Configuration.prop.getProperty("suite.pusher.defaultParams.subsId") + "/" + Configuration.prop.getProperty("suite.pusher.defaultParams.optionalPid") + "/" + sid);

        String pipeResponse = cageResponse.getRequest(Configuration.prop.getProperty("suite.pusher.defaultParams.cagePipe") + "/" + Configuration.prop.getProperty("suite.pusher.defaultParams.subsId") + "/" + Configuration.prop.getProperty("suite.pusher.defaultParams.optionalPid") + "/" + sid);

         externalRecordJsonParser = new ExternalRecordJsonParser(pipeResponse);
         externalRecordJsonParser = externalRecordJsonParser.getExternalRecordJsonParser();
         String recording = externalRecordJsonParser.getRecording();
         jsonActions = new JsonActions();
         JSONObject jsonObject = jsonActions.convertStringToJson(recording);
         String location = jsonObject.get("Loc").toString();
         if(!location.contains("pii")){
             Assert.assertTrue(true);
         }

    }

    public void printArray(String[] array) {
        for (int j = 0; j < array.length; j++) {
            System.out.println(array[j]);
        }
    }

    @Ignore
    @Test
    public void checkLaggingWithMissingMessageInTheMiddle() throws IOException, TimeoutException, ParseException, InterruptedException, NoSuchAlgorithmException {

        runHelperForTestsPreparationMethods = new RunHelperForTestsPreparationMethods();
        System.out.println("running test for the following json input file: recMissingMiddle.json");
        String inputFile = "C:\\Git\\pipe-Regressions\\pipeline-test\\src\\main\\resources\\not_working\\recMissingMiddle.json";
        String sid = runHelperForTestsPreparationMethods.runHelperPreparationWithoutDotNet(inputFile);

        String dotNetLaggingQueue = Configuration.prop.getProperty("suite.pusher.defaultParams.rabbitQueueLaggingDotNet");
        String pipeLaggingQueue = Configuration.prop.getProperty("suite.pusher.defaultParams.rabbitQueueLaggingPipe");

        RabbitAMQPTest rabbitAMQP = new RabbitAMQPTest();

        Channel channelDotNet = rabbitAMQP.connectToRabbitQueue(dotNetLaggingQueue);
        Channel channelPipe = rabbitAMQP.connectToRabbitQueue(pipeLaggingQueue);

        String sidRabbitMessage=rabbitAMQP.checkAvailability(dotNetLaggingQueue, channelDotNet);
        if(!sid.equals(sidRabbitMessage)){
            System.out.println("Not the right sid...");
        }
        Assert.assertEquals(sid,sidRabbitMessage);
        rabbitAMQP.checkAvailability(pipeLaggingQueue, channelPipe);

    }


}