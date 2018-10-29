package com.pipe.pipeapp;

import com.pipe.record.comparator.parser.ExternalRecordJsonParser;
import helpers.*;
import junit.framework.TestCase;
import org.json.simple.parser.ParseException;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.logging.Logger;

public class DuplicatorTest extends TestCase {
    private static final Logger LOGGER = Logger.getLogger(DuplicatorTest.class.getName());
    PushSession pushSession = new PushSession();
    JsonActions jsonActions = new JsonActions();
    boolean isLast;

    @Test
    public void gettingSIDFromFileAndCompare() throws IOException, ParseException, URISyntaxException, InterruptedException {
        long startTime = System.nanoTime();
        jsonActions = new JsonActions();
        String recordingStaging ="";
        String eventsStaging ="";
        String streamsStaging ="";
        Compare compare = new Compare();
        String recordingProd ="";
        String eventsProd ="";
        String streamsProd ="";

        String cageProd = Configuration.prop.getProperty("suite.pusher.defaultParams.cageProd");
        String cageStaging = Configuration.prop.getProperty("suite.pusher.defaultParams.cageStaging");

        String[] array = jsonActions.parseSIDFileFromPathAndReturnStringArray("C:\\Users\\erez.naim\\Documents\\Aharon_Shemesh\\link4.txt");

        String subIdProd = "233206";
        String pidProd = "9898";
        String subIdStaging = "69694";
        String pidStaging = "84";

        HttpGetRequest cageResponse = new HttpGetRequest();
        ExternalRecordJsonParser[] cageArchivePackagesArray;
        pushSession = new PushSession();
        for(int i=0;i<array.length;i++){
            String sid = array[i];
            if(sid.equals("1789943826301098")){
               System.out.println("hello");
            }
            //todo time stamp response for cage staging
            String responseDotNetStaging = cageResponse.getRequest(cageStaging + "/" + subIdStaging + "/" + pidStaging + "/" + sid);
            //todo time stamp response for cage prod
            String responseDotNetProd = cageResponse.getRequest(cageProd + "/" + subIdProd + "/" + pidProd + "/" + sid);
            cageArchivePackagesArray = getCageProducts(responseDotNetStaging,responseDotNetProd);

            System.out.println(cageStaging + "/" + subIdStaging + "/" + pidStaging + "/" + sid);
            System.out.println(cageProd + "/" + subIdProd + "/" + pidProd + "/" + sid);

            recordingStaging=cageArchivePackagesArray[0].getRecording();
            eventsStaging=cageArchivePackagesArray[0].getEvents();
            streamsStaging=cageArchivePackagesArray[0].getStreams();


            recordingProd=cageArchivePackagesArray[1].getRecording();
            eventsProd=cageArchivePackagesArray[1].getEvents();
            streamsProd=cageArchivePackagesArray[1].getStreams();


            pushSession.saveSessionsArchiveForDuplicatorTest(Long.parseLong(sid),recordingStaging,recordingProd,eventsStaging,eventsProd,streamsStaging,streamsProd);
            if (i == (array.length) - 1) {
                isLast = true;
            }

            compare.compareTests(recordingStaging,recordingProd, eventsStaging, eventsProd,streamsStaging, streamsProd , isLast,array[i] );
            Thread.sleep(1000);
        }

        long endTime   = System.nanoTime();
        System.out.println(endTime-startTime);

    }



    public ExternalRecordJsonParser[] getCageProducts(String responseStaging, String responseProd){

        ExternalRecordJsonParser[] cageArchivePackgeArray = new ExternalRecordJsonParser[2];
        ExternalRecordJsonParser externalRecordJsonParserStaging = new ExternalRecordJsonParser(responseStaging);
        ExternalRecordJsonParser externalRecordJsonParserProd = new ExternalRecordJsonParser(responseProd);

        externalRecordJsonParserStaging = externalRecordJsonParserStaging.getExternalRecordJsonParser();
        externalRecordJsonParserProd = externalRecordJsonParserProd.getExternalRecordJsonParser();

        cageArchivePackgeArray[0] = externalRecordJsonParserStaging;
        cageArchivePackgeArray[1] = externalRecordJsonParserProd;

        return cageArchivePackgeArray;
    }
}