package com.pipe.pipeapp;

import com.aerospike.client.AerospikeException;
import com.pipe.record.comparator.model.Record;
import com.pipe.record.comparator.parser.ExternalRecordJsonParser;
import helpers.*;
import junit.framework.TestCase;
import org.json.simple.parser.ParseException;
import org.testng.Assert;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

import java.io.IOException;

import java.net.URISyntaxException;
import java.util.logging.Logger;

public class ConnectionTests extends TestCase {
    private static final Logger LOGGER = Logger.getLogger(ConnectionTests.class.getName());
    PushSession pushSession = new PushSession();
          Amazon aws;
@Ignore
    @Test
    public void connectToAeroSpike() throws InterruptedException, IOException, ParseException {
        pushSession = new PushSession();
        String str = "C:\\Git\\Pipe-Regressions\\pipeline-test\\src\\main\\resources\\recFiles\\ADIDAS-675.json";
        String status="";
        aws = new Amazon();
        status=aws.checkPipeInstanceStatus();

        aws.stopPipe();
        System.out.println(" Stopping pipe instance please wait...");
        Thread.sleep(30000);
        status = aws.checkPipeInstanceStatus();
        if(!(status.equals("stopped"))){
            Thread.sleep(30000);
        }else{
            System.out.println(" Pipe instnace status is: "+status);
        }
        aws.stopAeroSpikes();
        Thread.sleep(30000);
        aws.checkAeroSpikesStatus();
        aws.startPipe();
        Thread.sleep(30000);
        status = aws.checkPipeInstanceStatus();
        if(!(status.equals("stopped"))){
            Thread.sleep(30000);
        }else{
            System.out.println(" Pipe instnace status is: "+status);
        }
        String sid = pushSession.pushSessionWithoutCageForDotNet(str);

        if(sid==null || sid.equals("0")){
            aws.startAeroSpikes();
            Thread.sleep(30000);
            aws.checkAeroSpikesStatus();
            sid = pushSession.pushSessionWithoutCageForDotNet(str);

            if(sid!=null||!(sid.equals("0"))){
                Assert.assertTrue(true);
            }
        }

    }
@Ignore
    @Test
    public void connectToKafka() throws InterruptedException, IOException, ParseException {
        pushSession = new PushSession();
        String str = "C:\\Git\\Pipe-Regressions\\pipeline-test\\src\\main\\resources\\recFiles\\ADIDAS-675.json";
        String status="";
        aws = new Amazon();
        status=aws.checkPipeInstanceStatus();

        aws.stopPipe();
        System.out.println(" Stopping pipe instance please wait...");
        Thread.sleep(30000);
        status = aws.checkPipeInstanceStatus();
        if(!(status.equals("stopped"))){
            Thread.sleep(30000);
        }else{
            System.out.println(" Pipe instnace status is: "+status);
        }
        aws.stopKafka();
        Thread.sleep(30000);
        aws.checkKafkaStatus();
        aws.startPipe();
        Thread.sleep(30000);
        status = aws.checkPipeInstanceStatus();
        if(!(status.equals("stopped"))){
            Thread.sleep(30000);
        }else{
            System.out.println(" Pipe instnace status is: "+status);
        }

        String sid = pushSession.pushSessionWithoutCageForDotNet(str);

        if(sid==null || (sid.equals("0"))){
            aws.startKafka();
            Thread.sleep(30000);
            aws.checkKafkaStatus();
            sid = pushSession.pushSessionWithoutCageForDotNet(str);
            if(sid!=null||!(sid.equals("0"))){
                Assert.assertTrue(true);
            }
        }

    }
@Ignore
    @Test
    public void connectToRabbit() throws InterruptedException, IOException, ParseException {
        pushSession = new PushSession();
        String str = "C:\\Git\\Pipe-Regressions\\pipeline-test\\src\\main\\resources\\recFiles\\ADIDAS-675.json";

        String status="";
        aws = new Amazon();
        status=aws.checkPipeInstanceStatus();

        aws.stopPipe();
        System.out.println(" Stopping pipe instance please wait...");
        Thread.sleep(30000);
        status = aws.checkPipeInstanceStatus();
        if(!(status.equals("stopped"))){
            Thread.sleep(30000);
        }else{
            System.out.println(" Pipe instnace status is: "+status);
        }
        aws.stopRabbit();
        Thread.sleep(30000);
        aws.checkKafkaStatus();
        aws.startPipe();
        Thread.sleep(30000);
        status = aws.checkPipeInstanceStatus();
        if(!(status.equals("stopped"))){
            Thread.sleep(30000);
        }else{
            System.out.println(" Pipe instnace status is: "+status);
        }

        String sid = pushSession.pushSessionWithoutCageForDotNet(str);

        if(sid==null||(sid.equals("0"))) {
            aws.startRabbit();
            Thread.sleep(30000);
            aws.checkRabbitStatus();
            sid = pushSession.pushSessionWithoutCageForDotNet(str);

            if (pushSession.cageArchivePackage != null) {
                Assert.assertTrue(true);
            }
        }
    }
//
//    @Test
//    public void recordTest() throws IOException, URISyntaxException {
//    FileHandling fileHandling = new FileHandling();
//        String filePath = "src\\main\\resources\\recFiles\\cageStrResponse.txt";
//        String str = fileHandling.getFileContentFromPath(filePath);
//        ExternalRecordJsonParser externalRecordJsonParser = new ExternalRecordJsonParser(str);
//        externalRecordJsonParser = externalRecordJsonParser.getExternalRecordJsonParser();
//        Record record = new Record(externalRecordJsonParser.getRecording(), externalRecordJsonParser.getEvents(), externalRecordJsonParser.getStreams(), externalRecordJsonParser.getWebPage());
//
//    }

    @Test
    public void connectionToAS() throws AerospikeException {
    AeroSpikeMethods aeroSpikeMethods = new AeroSpikeMethods();

    }

}