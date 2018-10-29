package com.pipe.pipeapp;

import com.pipe.record.comparator.model.FactoryHelper;
import com.pipe.record.comparator.parser.ExternalRecordJsonParser;
import com.rabbitmq.client.ConnectionFactory;
import helpers.*;
import junit.framework.TestCase;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.testng.annotations.Ignore;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Performance extends TestCase {
    PushSession pushSession = new PushSession();
    ExternalRecordJsonParser externalRecordJsonParser;
    Amazon aws;
    JsonActions jsonActions = new JsonActions();
    FileHandling fh = new FileHandling();
    String pipe;
    String dotNet;
    List<String> results = new ArrayList<String>();
    final int MILISECONDS_TO_HOURS = 60*60*60*1000;
    String[] m_recordingStrArray;
    ArrayList<String> files;
    String path = "C:\\Git\\Pipe-Regressions\\pipeline-test\\src\\main\\resources\\recFiles\\";
    File f = new File(path);
    String m_bucketName = "nv-q1-s3-proclin2-03";
    String bucketDotNet = "nv-q-recordings";

    @Ignore
    @Test
    public void performanceTest() throws InterruptedException, ParseException, IOException, NoSuchAlgorithmException {
        addingAllFilesToList();
//        Amazon awsDotNet = new Amazon(bucketDotNet);
//        Amazon awsPipe = new Amazon(m_bucketName);
        JSONObject jsonObjectDotNet = new JSONObject();
        JSONObject jsonObjectPipe = new JSONObject();


        System.out.println("number of session to be run are: " + this.files.size());
        for(int i=0;i<this.files.size();i++){
            performanceOfCreationOfAnObject(this.files.get(i));
            System.out.println("----------------------------------------------------------------------------------------------------------------------------------");
            System.out.println("running test for the following json input file: " + this.files.get(i));
            System.out.println("----------------------------------------------------------------------------------------------------------------------------------");
        }
    }
//-------------------------------------------------------------------------------------------------------------------------------------------------------
    public JSONObject[] setUpBeforeCheckTimeZoneOrPerformance(Amazon awsDotNet, Amazon awsPipe,JSONObject jsonObjectDotNet, JSONObject jsonObjectPipe, String str) throws InterruptedException, ParseException, IOException {

        JSONObject jsonObjectArray[] = new JSONObject[2];
        m_recordingStrArray = new String[2];

       // awsDotNet.deleteAllObjetsFromBucketS3();
     //   awsPipe.deleteAllObjetsFromBucketS3();

        jsonObjectDotNet = new JSONObject();
        jsonObjectPipe = new JSONObject();


        //runHelperForTestsPreparationMethods = new RunHelperForTestsPreparationMethods();

        //this.files = new ArrayList<String>();
        //String str = "C:\\Git\\pipe-Regressions\\pipeline-test\\src\\main\\resources\\recFiles\\ASU-146.json";
        m_recordingStrArray=pushSession.returnBothCageArchivePackageAfterPushSession(str);
        jsonObjectDotNet=jsonActions.convertStringToJson(m_recordingStrArray[0]);
        jsonObjectPipe=jsonActions.convertStringToJson(m_recordingStrArray[1]);

        jsonObjectArray[0] = jsonObjectDotNet;
        jsonObjectArray[1]=jsonObjectPipe;

        return jsonObjectArray;


    }
//-------------------------------------------------------------------------------------------------------------------------------------------------------

    public void performanceOfCreationOfAnObject(String str) throws ParseException, IOException, InterruptedException, NoSuchAlgorithmException {
        //-------------------------------------------------------------------------------------------------------------------------

        Amazon awsDotNet = new Amazon(bucketDotNet);
        Amazon awsPipe = new Amazon(m_bucketName);
        JSONObject jsonObjectDotNet = new JSONObject();
        JSONObject jsonObjectPipe = new JSONObject();

        JSONObject[] dataJsonObjectArray=setUpBeforeCheckTimeZoneOrPerformance(awsDotNet,awsPipe,jsonObjectDotNet,jsonObjectPipe,str);

        String sid = dataJsonObjectArray[0].get("SID").toString();
        String SubscriberId = dataJsonObjectArray[0].get("SubscriberId").toString();
        String pid = dataJsonObjectArray[0].get("PID").toString();

        String RecordingDateDotNet =  dataJsonObjectArray[0].get("RecordingDate").toString();//epoch time conversion -seems ok
        String RecordingDatePipe=dataJsonObjectArray[1].get("RecordingDate").toString();//epoch time conversion - seems ok
        String recordingDateStringDotNet=convert(Long.parseLong(RecordingDateDotNet.toString()));
        System.out.println(recordingDateStringDotNet);
        recordingDateStringDotNet = recordingDateStringDotNet.split(":")[2];

        String recordingDateStringPipe=convert(Long.parseLong(RecordingDatePipe));
        System.out.println(recordingDateStringPipe);
        recordingDateStringPipe = recordingDateStringPipe.split(":")[2];

        String datePipe = awsPipe.getObjectCreation(sid, SubscriberId, pid);
        datePipe=parseTime(datePipe);
        System.out.println(datePipe);
        datePipe = datePipe.split(":")[2];
        String dateDotNet = awsDotNet.getObjectCreation(sid, SubscriberId, pid);
        dateDotNet = parseTime(dateDotNet);
        System.out.println(dateDotNet);
        dateDotNet = dateDotNet.split(":")[2];

        System.out.println("PIPE: " + (Integer.parseInt(datePipe)-Integer.parseInt(recordingDateStringPipe)) + " seconds");
        System.out.println(".NET: " + (Integer.parseInt(dateDotNet)-Integer.parseInt(recordingDateStringDotNet))+ " seconds");

//-------------------------------------------------------------------------------------------------------------------------
    }

    @Ignore
    @Test
    public void checkTimeZoneValues() throws InterruptedException, ParseException, IOException, java.text.ParseException {

        Amazon awsDotNet = new Amazon(bucketDotNet);
        Amazon awsPipe = new Amazon(m_bucketName);
        JSONObject jsonObjectDotNet = new JSONObject();
        JSONObject jsonObjectPipe = new JSONObject();
        String str = "C:\\Git\\pipe-Regressions\\pipeline-test\\src\\main\\resources\\recFiles\\ASU-146.json";

        JSONObject[] dataJsonObjectArray = setUpBeforeCheckTimeZoneOrPerformance(awsDotNet,awsPipe,jsonObjectDotNet,jsonObjectPipe, str);

        long authenticationUnixTimeDotNet = Long.parseLong(dataJsonObjectArray[0].get("AuthenticationUnixTime").toString());
        long authenticationUnixTimePipe = Long.parseLong(dataJsonObjectArray[1].get("AuthenticationUnixTime").toString());
        String dotNet = convert(authenticationUnixTimeDotNet);
        String pipe = convert(authenticationUnixTimePipe);
        checkDiffrences(dotNet,pipe,"AuthenticationUnixTime");

        String ClientRecordingDateTimeZoneDotNet = dataJsonObjectArray[0].get("ClientRecordingDateTimeZone").toString();
        String ClientRecordingDateTimeZonePipe = dataJsonObjectArray[1].get("ClientRecordingDateTimeZone").toString();
        ClientRecordingDateTimeZoneDotNet = parseTime(ClientRecordingDateTimeZoneDotNet);
        ClientRecordingDateTimeZonePipe = parseTime(ClientRecordingDateTimeZonePipe);
        checkDiffrences(ClientRecordingDateTimeZoneDotNet,ClientRecordingDateTimeZonePipe,"ClientRecordingDateTimeZone" );

        String ClientRecordingDateTimeUtcDotNet = dataJsonObjectArray[0].get("ClientRecordingDateTimeUtc").toString();
        String ClientRecordingDateTimeUtcPipe =dataJsonObjectArray[1].get("ClientRecordingDateTimeUtc").toString();
        ClientRecordingDateTimeUtcDotNet = parseTime(ClientRecordingDateTimeUtcDotNet);
        ClientRecordingDateTimeUtcPipe = parseTime(ClientRecordingDateTimeUtcPipe);
        checkDiffrences(ClientRecordingDateTimeUtcDotNet,ClientRecordingDateTimeUtcPipe, "ClientRecordingDateTimeUtc");

        String CreateDateDotNet =  dataJsonObjectArray[0].get("CreateDate").toString();
        String CreateDatePipe=dataJsonObjectArray[1].get("CreateDate").toString();
        CreateDateDotNet = parseTime(CreateDateDotNet);
        CreateDatePipe = parseTime(CreateDatePipe);
        checkDiffrences(CreateDateDotNet,CreateDatePipe,"CreateDate");

        String ProcessingDateTimeUtcDotNet =  dataJsonObjectArray[0].get("ProcessingDateTimeUtc").toString();
        String ProcessingDateTimeUtcPipe=dataJsonObjectArray[1].get("ProcessingDateTimeUtc").toString();
        ProcessingDateTimeUtcDotNet = parseTime(ProcessingDateTimeUtcDotNet);
        ProcessingDateTimeUtcPipe  = parseTime(ProcessingDateTimeUtcPipe);
        checkDiffrences(ProcessingDateTimeUtcDotNet,ProcessingDateTimeUtcPipe,"ProcessingDateTimeUtc" );

        String processingDateTimeZoneDotNet =  dataJsonObjectArray[0].get("ProcessingDateTimeZone").toString();
        String processingDateTimeZonePipe=dataJsonObjectArray[1].get("ProcessingDateTimeZone").toString();
        processingDateTimeZoneDotNet = parseTime(processingDateTimeZoneDotNet);
        processingDateTimeZonePipe = parseTime(processingDateTimeZonePipe);
        checkDiffrences(processingDateTimeZoneDotNet, processingDateTimeZonePipe,"ProcessingDateTimeZone");

        String RecordingDateDotNet =  dataJsonObjectArray[0].get("RecordingDate").toString();//epoch time conversion -seems ok
        String RecordingDatePipe=dataJsonObjectArray[1].get("RecordingDate").toString();//epoch time conversion - seems ok
        dotNet = convert(Long.parseLong(RecordingDateDotNet));
        pipe = convert(Long.parseLong(RecordingDatePipe));
        checkDiffrences(dotNet,pipe,"RecordingDate");

        String RecordingDateJsDotNet =  dataJsonObjectArray[0].get("RecordingDateJs").toString();//epoch time conversion -seems ok
        String RecordingDateJsPipe=dataJsonObjectArray[1].get("RecordingDateJs").toString();//epoch time conversion - seems ok
        dotNet = convert(Long.parseLong(RecordingDateJsDotNet));
        pipe = convert(Long.parseLong(RecordingDateJsPipe));
        checkDiffrences(dotNet,pipe,"RecordingDateJs");

        String cage_timeDotNet =  dataJsonObjectArray[0].get("cage_time").toString();//epoch time conversion -seems ok
        String cage_timePipe=dataJsonObjectArray[1].get("cage_time").toString();//epoch time conversion - seems ok
        dotNet = convert(Long.parseLong(cage_timeDotNet));
        pipe = convert(Long.parseLong(cage_timePipe));
        checkDiffrences(dotNet,pipe,"cage_time");

        String RecordingEndDateDotNet =  dataJsonObjectArray[0].get("RecordingEndDate").toString();//epoch time conversion - check
        String RecordingEndDatePipe=dataJsonObjectArray[1].get("RecordingEndDate").toString();//epoch time conversion - check
        dotNet = convert(Long.parseLong(RecordingEndDateDotNet));
        pipe = convert(Long.parseLong(RecordingEndDatePipe));
        checkDiffrences(dotNet,pipe,"RecordingEndDate");

        String ClientDateDotNet =  dataJsonObjectArray[0].get("ClientDate").toString();//epoch time conversion - check
        String ClientDatePipe=dataJsonObjectArray[1].get("ClientDate").toString();//epoch time conversion - check
        dotNet = convert(Long.parseLong(ClientDateDotNet));
        pipe = convert(Long.parseLong(ClientDatePipe));
        checkDiffrences(dotNet,pipe,"ClientDate");

    }

    public String convert(long miliSeconds)
    {
        int hrs = (int) TimeUnit.MILLISECONDS.toHours(miliSeconds) % 24;
        int min = (int) TimeUnit.MILLISECONDS.toMinutes(miliSeconds) % 60;
        int sec = (int) TimeUnit.MILLISECONDS.toSeconds(miliSeconds) % 60;
        return String.format("%02d:%02d:%02d", hrs, min, sec);
    }

    private String parseTime(String time) {
        if(time.contains(" ")) {
            time = time.split(" ")[1];
            time = time.substring(0, time.indexOf('.'));
        }else{
            if(time.contains("T")){
                time = time.split("T")[1];
                time = time.substring(0, time.indexOf('.'));
            }
        }
        return time;
    }

    private boolean checkDiffrences(String dotNetTime, String pipeTime, String name) throws java.text.ParseException {

        boolean flag = true;

        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        Date date1 = formatter.parse(dotNetTime);
        Date date2 = formatter.parse(pipeTime);
        long diff = (date2.getTime() - date1.getTime());
        diff = (diff/1000) /60;
        if(Math.abs(diff)>3){
            System.out.println(name + " is bugged, difference is " + Math.abs(diff) + " minutes");
            flag=false;
        }else{
            System.out.println(name + " is good, difference is " + Math.abs(diff) + " minutes");
        }

        return flag;

    }


    private void addingAllFilesToList() {

        this.files = new ArrayList<String>();
        if (f.isDirectory()) {
            String[] listOfFiles = f.list();
            Pattern p = Pattern.compile("^(.*?)\\.json$");
            for (String file : listOfFiles) {
                Matcher m = p.matcher(file);
                if (m.matches()) {
                    this.files.add(this.path + m.group(1) + ".json");
                }

            }
        }
//        files.add("src\\main\\resources\\recFiles\\HD-TESTS.json");


    }

    public void printArray(String[] array) {
        for (int j = 0; j < array.length; j++) {
            System.out.println(array[j]);
        }
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
@Test
    public void performanceTestForProcessorLinuxTime(){
        Properties props = new Properties();
        FactoryHelper factoryHelper = new FactoryHelper(props);
        //ConnectionFactory factory =  factoryHelper.fillPropertiesForRabbitConsumerAndConnectForUnitTesting();

    //    FactoryHelper factoryHelper = new FactoryHelper(prop);
    }


}