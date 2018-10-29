package com.pipe.pipeapp;

import com.aerospike.client.AerospikeClient;
import com.aerospike.client.AerospikeException;
import com.aerospike.client.Key;
import com.aerospike.client.ScanCallback;
import com.aerospike.client.policy.ScanPolicy;
import com.aerospike.client.query.Statement;
import helpers.Amazon;
import junit.framework.TestCase;
import org.json.simple.parser.ParseException;
import org.testng.Assert;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;

public class SetUpConnectionClass extends TestCase {
    private static final Logger LOGGER = Logger.getLogger(SetUpConnectionClass.class.getName());
    static String psCommandStart = null;
    static String psCommandStop = null;
    Amazon aws;
    @Ignore

    @Test
    public void connectToASUsingClient() throws AerospikeException {

        AerospikeClient client = new AerospikeClient("172.22.3.126", 3000);
        ScanPolicy scanPolicy = new ScanPolicy();
     //   client.scanAll(scanPolicy, namespace, set, new ScanCallback()
        Statement stmt = new Statement();
        Key key = new Key("pipeline", "DeviceData", "putgetkey");
        stmt.setNamespace("pipeline.DeviceData");


    }
//@Ignore
    @Test
    public void connectToAeroSpike() throws InterruptedException, IOException, ParseException {
//        pushSession = new PushSession();
//        String str = "C:\\Git\\Pipe-Regressions\\pipeline-test\\src\\main\\resources\\recFiles\\ADIDAS-675.json";
        String status="";
        aws = new Amazon();
        aws.startAeroSpikes();
        Thread.sleep(30000);
        status=aws.checkInstanceStatus(Configuration.prop.getProperty("suite.pusher.defaultParams.instanceAero01"));


        if((status.equals("running"))){
            System.out.println("AeroSpikes are up! ");
            startProcessorDotNetService();
            aws.startProcessorDotNet();
        }

    }
//    @Ignore
@Test
    public void startProcessorDotNetService(){
        String currentServerHostname = "172.22.6.26";
        String currentServerUser = "Administrator";
        String currentServerPass="dyAf&E47p?s";
        String commandToStart="start ProcessorService";
        String commandToStop="stop ProcessorService";
        String psCommand ="C:\\Users\\erez.naim\\Downloads\\PSTools\\psservice \\\\"+ currentServerHostname + " -u " + currentServerUser + " -p \"dyAf&E47p?s\"";
        psCommandStart = psCommand + " " + commandToStart;
        psCommandStop = psCommand + " " + commandToStop;
        serviceStart();
    //    serviceStop();
    }
//    @Ignore
    @Test
    public void startKafka() throws InterruptedException {
        String status = "";
        aws = new Amazon();
        aws.startKafka();
        Thread.sleep(30000);
        status = aws.checkKafkaStatus();
    }

    private void serviceStop() {
        String[] cmd = new String[5];
        cmd[0]="cmd.exe";
        cmd[1]="/C";
        cmd[2]=psCommandStop;
        cmd[3]="";
        cmd[4]="";
        // Run remote command
        File f = new File(getCurrentWorkingDirectory() + "\\lib");
        try
        {
            Process run = Runtime.getRuntime().exec(cmd,null,f);

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void serviceStart() {
        String[] command = {
                "cmd",
        };

        Process p;

        try{
            p=Runtime.getRuntime().exec(command);
            PrintWriter stdin = new PrintWriter(p.getOutputStream());
            stdin.println(psCommandStart);

            stdin.close();
            p.waitFor();


        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private static String getCurrentWorkingDirectory() {
        String currentDir = System.getProperty("user.dir");
        return currentDir;
    }

}