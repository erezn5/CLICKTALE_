package com.pipe.pipeapp;

import com.pipe.record.comparator.parser.ExternalRecordJsonParser;
import helpers.*;
import junit.framework.TestCase;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class CompareSideBySide extends TestCase {
    private static final Logger logger = LoggerFactory.getLogger(CompareSideBySide.class);
    //List<List<PusherParams>> pushArguments = null;
    PushSession pushSession = new PushSession();
    String filePath;
    ExternalRecordJsonParser externalRecordJsonParser;
    Amazon aws;
    JsonActions jsonActions = new JsonActions();
    FileHandling fh = new FileHandling();
    String pipe;
    String dotNet;
    String[] list = null;
    String filePathDotNetHtml;
    String filePathPipeHtml;
    String filePathDotNetXml;
    String filePathPipeXml;
    String filePathDotNetDsr;
    String filePathPipeDsr;
    String jsonDotNetPathFile;
    String jsonPipePathFile;
    List<String> results = new ArrayList<String>();
    String xml[];
    String json[];
    String dsr[];
    String html[];
    String stringBuilderHTML[];
    boolean isLast;
    ArrayList<String> files;
    String path = "src\\main\\resources\\recFiles\\";
    File f = new File(path);

    int counter = 0;
    JSONObject childHTML = new JSONObject();
    private org.json.JSONObject objectIdHTML;
    private org.json.JSONObject mainObjHTML = new org.json.JSONObject();
    RunHelperForTestsPreparationMethods runHelperForTestsPreparationMethods;// = new RunHelperForTestsPreparationMethods();
    int i = 0;
    int count = 0;
    String m_bucketName = "nv-q1-s3-proclin2-03";
    String bucketDotNet = "nv-q-recordings";
    String[] sidArray;
    private SoftAssert softAssert = new SoftAssert();
    boolean isMobile = false;
    String dotNetName = "_.NET";
    String pipeName = "_PIPE";

    @Test//(dependsOnMethods = {"testPipeWithoutProcDotNet", "testDotNetWithoutProcPipe"})
    public void compareTest() throws Exception {

        runHelperForTestsPreparationMethods = new RunHelperForTestsPreparationMethods();

        addingAllFilesToList();

        int size = this.files.size();
        logger.info("number of session to be run are: " + size);
        for (i = 0; i < size; i++) {
            String str = this.files.get(i);
            logger.info("----------------------------------------------------------------------------------------------------------------------------------");
            logger.info("running test for the following json input file: " + str);
            logger.info("----------------------------------------------------------------------------------------------------------------------------------");

//            this.list = this.runHelperForTestsPreparationMethods.runHelperPreparation(this.files.get(i), this.list); use this if you want to download pipe packages from S3
            this.runHelperForTestsPreparationMethods.runHelperPreparationFromBothCages(this.files.get(i), this.list);// use this to get packages both for .NET and pipe from their cage
            if (i == size - 1) {
                isLast = true;
            }

            runTestSuite();
        }

    }


    private void addingAllFilesToList() {

        this.files = new ArrayList<String>();
//        if (f.isDirectory()) {
//            String[] listOfFiles = f.list();
//            Pattern p = Pattern.compile("^(.*?)\\.json$");
//            for (String file : listOfFiles) {
//                Matcher m = p.matcher(file);
//                if (m.matches()) {
//                    this.files.add(this.path + m.group(1) + ".json");
//                }
//
//            }
//        }
        files.add("src\\main\\resources\\recFiles\\HD-TESTS.json");
//        files.add("src\\main\\resources\\recFiles\\escalation.json");
//        files.add("src\\main\\resources\\recFiles\\ASU-146.json");
//        files.add("src\\main\\resources\\recFiles\\rec.json");
//        files.add("src\\main\\resources\\recFiles\\adidas_mobile_iphone6.json");
//        files.add("src\\main\\resources\\recFiles\\reciPhone6.json");
//        files.add("src\\main\\resources\\recFiles\\1730211840426349_BigXML.json");
//        files.add("src\\main\\resources\\recFiles\\1730231154344565_1788_message.json");
//        files.add("src\\main\\resources\\recFiles\\recBigData.json");
//        files.add("src\\main\\resources\\recFiles\\HMDP-1611.json");
//        files.add("src\\main\\resources\\recFiles\\ADIDAS-675.json");
//        files.add("src\\main\\resources\\recFiles\\trueCarCheck.json");
//        files.add("src\\main\\resources\\temp\\test_09.json");
//        files.add("src\\main\\resources\\recFiles\\adidas_pii_test.json");
//        files.add("src\\main\\resources\\temp\\japanese.json");
//        files.add("src\\main\\resources\\recFiles\\fido_ROG-73.json");
//        files.add("src\\main\\resources\\recFiles\\BUG-252-input.json");
//        files.add("src\\main\\resources\\temp\\adidas_mobile_iphone8.json");
//        files.add("src\\main\\resources\\temp\\rec.json");
        files.add("src\\main\\resources\\recFiles\\sukot.json");
//        files.add("src\\main\\resources\\recFiles\\new_input_09.json");
//        files.add("src\\main\\resources\\recFiles\\moshe_mazouz_amex.json");

    }

    @Test
    public void runTestSuite() throws Exception {
        //  Stopwatch timer = new Stopwatch().start();

        long startTime = System.currentTimeMillis();

        testCompareJsonPercentage(isLast);
        testCompareXMLPercentage(isLast);
        testCompareDSRPercentage(isLast);
        //    diffHTML();

        long stopTime = System.currentTimeMillis();

        long elapsedTime = stopTime - startTime;

        logger.info(String.valueOf((long) elapsedTime));
//        compareHTMLFiles();
    }


    public void testCompareJsonPercentage(boolean isLast) throws IOException, ParseException {

        String sid = runHelperForTestsPreparationMethods.getSid();
        this.jsonDotNetPathFile = "C:\\temp\\ArchivedFilesFromPusher\\test\\" + sid + this.dotNetName + "\\dot-net_" + sid + ".json";
        this.jsonPipePathFile = "C:\\temp\\ArchivedFilesFromPusher\\test\\" + sid + this.pipeName + "\\pipe_" + sid + ".json";
        String val1 = jsonActions.readJsonContent(this.jsonDotNetPathFile);
        //String val2 = jsonActions.readJsonContent(list[2]); //if pipe is writing directly to amazon S3
        String val2 = jsonActions.readJsonContent(this.jsonPipePathFile);

        int countBad = 0;
        int countGood = 0;

        val1 = jsonActions.removeUnwantedJSONFieldsFROMRecordingAndReturnJsonAsString(val1);
        val2 = jsonActions.removeUnwantedJSONFieldsFROMRecordingAndReturnJsonAsString(val2);

        compareJsonFilesTest(val1, val2, isLast);

        List<JsonActions.FieldCompare> a = jsonActions.deepCompare(val1, val2);

        for (int i = 0; i < a.size(); i++) {
            if (a.get(i).getDotNet().equals("null") && a.get(i).getPipe().equals("null")) {
                continue;
            } else {
                boolean status = a.get(i).getCmp();

                if (status == true) {
                    countGood++;
                } else {
                    countBad++;
                }

            }
        }

        float size = countBad + countGood;
        float percentage = ((countGood * 100f) / size);
        if (percentage == 100) {
            System.out.println("Files are completely identical! " + jsonDotNetPathFile + jsonPipePathFile);
        }
        logger.info("Number of good comparisons are(Json / Metadata): " + countGood);
        logger.info("Number of bad comparisons are (Json / Metadata): " + countBad);
        logger.info("Percantage of Success(Json / Metadata): " + percentage + "%");
        errorChecker(percentage, countGood, countBad);
    }

    public void compareJsonFilesTest(String dotNet, String pipe, boolean isLast) throws IOException {

        List<JsonActions.FieldCompare> a = jsonActions.deepCompare(dotNet, pipe);

        this.json = new String[a.size()];
        int j = 0;
        for (int i = 0; i < a.size(); i++) {

            boolean status = a.get(i).getCmp();
            if (status == true) {
                continue;
            } else if ((a.get(i).getDotNet().equals("null")) && (a.get(i).getPipe().equals("null"))) {
                continue;
            } else {

                this.json[j] = a.get(i).toString();
                j++;

            }

        }

        this.json = jsonActions.removeNULLElementsFromStringArray(this.json);
        jsonActions.buildJSONVer(this.json, runHelperForTestsPreparationMethods.getSid(), this.isLast, "JSON", "jsonResults.json");
    }


    public void compareDSRFilesTest(String dotNet, String pipe, boolean isLast) throws IOException {

        List<JsonActions.FieldCompare> a = jsonActions.deepCompare(dotNet, pipe);


        pipe = org.json.XML.toJSONObject(pipe).toString();
        dotNet = org.json.XML.toJSONObject(dotNet).toString();


        this.dsr = new String[a.size()];

        dsr = new String[a.size()];
        int j = 0;
        for (int i = 0; i < a.size(); i++) {
            boolean status = a.get(i).getCmp();
            if (status == true) {
                continue;
            } else if ((a.get(i).getDotNet().equals("null")) && (a.get(i).getPipe().equals("null"))) {
                continue;
            } else {
                dsr[j] = a.get(i).toString();
                j++;


            }
        }

        this.dsr = jsonActions.removeNULLElementsFromStringArray(this.dsr);
        jsonActions.buildJSONVer(this.dsr, runHelperForTestsPreparationMethods.getSid(), isLast, "DSR", "dsrResults.json");

//        for (int i = 0; i < a.size(); i++) {
//            Assert.assertTrue(a.get(i).getCmp());
//        }

    }


    public void compareXmlFilesTest(String dotNet, String pipe, boolean isLast) throws IOException, URISyntaxException, ParseException {

        List<JsonActions.FieldCompare> a = jsonActions.deepCompare(dotNet, pipe);
        this.xml = new String[a.size()];
        int j = 0;
        for (int i = 0; i < a.size(); i++) {
            boolean status = a.get(i).getCmp();
            if (status == true) {
                continue;
            } else {
                this.xml[j] = a.get(i).toString();
                j++;

            }
        }
        this.xml = jsonActions.removeNULLElementsFromStringArray(this.xml);


        // jsonActions.buildXML(this.xml,this.sid,this.isLast);
        jsonActions.buildJSONVer(this.xml, runHelperForTestsPreparationMethods.getSid(), this.isLast, "XML", "eventsResults.json");
    }

    public void testCompareDSRPercentage(boolean isLast) throws ParseException, IOException, URISyntaxException {
        String sid = runHelperForTestsPreparationMethods.getSid();
        this.filePathDotNetDsr = "C:\\temp\\ArchivedFilesFromPusher\\test\\" + sid + this.dotNetName + "\\dot-net_" + sid + ".dsr";
        this.filePathPipeDsr = "C:\\temp\\ArchivedFilesFromPusher\\test\\" + sid + this.pipeName + "\\pipe_" + sid + ".dsr";

        String val1 = jsonActions.readJsonContent(this.filePathDotNetDsr);
//        String val2 = jsonActions.readJsonContent(list[0]);
        String val2 = jsonActions.readJsonContent(this.filePathPipeDsr);

        val1 = org.json.XML.toJSONObject(val1).toString();
        val2 = org.json.XML.toJSONObject(val2).toString();

        int countBad = 0;
        int countGood = 0;

        val1 = jsonActions.removeUnwantedJSONFieldsFROMRecordingAndReturnJsonAsString(val1);
        val2 = jsonActions.removeUnwantedJSONFieldsFROMRecordingAndReturnJsonAsString(val2);

        compareDSRFilesTest(val1, val2, isLast);

        List<JsonActions.FieldCompare> a = jsonActions.deepCompare(val1, val2);

        for (int i = 0; i < a.size(); i++) {
            if (a.get(i).getDotNet().equals("null") && a.get(i).getPipe().equals("null")) {
                continue;
            } else {
                boolean status = a.get(i).getCmp();

                if (status == true) {
                    countGood++;
                } else {
                    countBad++;
                }

            }
        }

        float size = countBad + countGood;
        float percentage = ((countGood * 100f) / size);
        if (percentage == 100) {
            System.out.println("Files are completely identical! " + filePathDotNetDsr + filePathPipeDsr);
        }
        logger.info("Number of good comparisons are(DSR / Metadata): " + countGood);
        logger.info("Number of bad comparisons are (DSR / Metadata): " + countBad);
        logger.info("Percantage of Success(DSR / Metadata): " + percentage + "%");
        errorChecker(percentage, countGood, countBad);
    }

    public void errorChecker(float percentage, int countGood, int countBad) {
        try {
            softAssert.assertTrue(percentage >= 95, "Good: " + countGood + "\nBad: " + countBad);
        } catch (Exception e) {
            logger.info(e.getMessage(), e);
        }
    }


    public void testCompareXMLPercentage(boolean isLast) throws IOException, URISyntaxException, ParseException {
        String sid = runHelperForTestsPreparationMethods.getSid();

        this.filePathDotNetXml = "C:\\temp\\ArchivedFilesFromPusher\\test\\" + sid + this.dotNetName + "\\dot-net_" + sid + ".xml";
        this.filePathPipeXml = "C:\\temp\\ArchivedFilesFromPusher\\test\\" + sid + this.pipeName + "\\pipe_" + sid + ".xml";

        String val1 = fh.getFileContentFromPath(this.filePathDotNetXml);
//        String val2 = fh.getFileContentFromPath(list[3]);
        String val2 = fh.getFileContentFromPath(this.filePathPipeXml);

        val1 = org.json.XML.toJSONObject(val1).toString();
        val2 = org.json.XML.toJSONObject(val2).toString();
        // compareXmlFilesTest(dotNet,pipe);
        val1 = jsonActions.removeUnwantedJSONFieldsFromXMLAndReturnJsonAsString(val1);
        val2 = jsonActions.removeUnwantedJSONFieldsFromXMLAndReturnJsonAsString(val2);
        compareXmlFilesTest(val1, val2, isLast);
        List<JsonActions.FieldCompare> a = jsonActions.deepCompare(val1, val2);
        //xml = new String[a.size()];

        int countBad = 0;
        int countGood = 0;

        for (int i = 0; i < a.size(); i++) {
            if (a.get(i).getDotNet().equals("null") && a.get(i).getPipe().equals("null")) {
                continue;
            } else {
                boolean status = a.get(i).getCmp();

                if (status == true) {
                    countGood++;
                } else {
                    countBad++;
                }
            }
        }

        float size = countBad + countGood;
        float percentage = ((countGood * 100f) / size);
        logger.info("Number of good comparisons are (XML / Events): " + countGood);
        logger.info("Number of bad comparisons are (XML / Events): " + countBad);
        logger.info("Percantage of Success (XML / Events): " + percentage + "%");
        errorChecker(percentage, countGood, countBad);
        // softAssert.assertTrue(percentage >= 95, "Good: " + countGood + "\nBad: " + countBad);
        //  Assert.assertTrue(percentage >= 95, "Good: " + countGood + "\nBad: " + countBad);


    }

    /**
     * ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
     **/
    public void diffHTML() throws IOException, URISyntaxException {
        this.filePathDotNetHtml = "C:\\temp\\ArchivedFilesFromPusher\\test\\" + runHelperForTestsPreparationMethods.getSid() + "\\dot-net.html";
        String dotNET = fh.getFileContentFromPath(this.filePathDotNetHtml);
        String pipe = fh.getFileContentFromPath(list[1]);

//        dotNET = dotNet.replace("\n", "");
//        pipe = pipe.replace("\n", "");

        Document docNetDoc;
        Document pipeDoc;

        docNetDoc = Jsoup.parse(dotNET);
        pipeDoc = Jsoup.parse(pipe);

        List<Node> dotNetNodes = docNetDoc.childNodes();
        List<Node> pipeDocNodes = pipeDoc.childNodes();
        int size = dotNetNodes.size();

        dotNetNodes.toString().replaceAll("\n", "");
        pipeDocNodes.toString().replaceAll("\n", "");
        //   Assert.assertTrue(dotNetNodes.size()==pipeDocNodes.size());
        int counter = 0;
        for (int i = 0; i < size; i++) {

            diffHTMLRecursion(dotNetNodes.get(i), pipeDocNodes.get(i), counter);
        }
        if (mainObjHTML == null) {
            System.out.println("Files are perfectly identical!");
        }
        System.out.println(mainObjHTML);


    }

    private void diffHTMLRecursion(Node node1, Node node2, int counter) {

        org.json.JSONObject mainObj = new org.json.JSONObject();
        org.json.JSONObject child = new org.json.JSONObject();
        JSONObject list = new JSONObject();
        JSONObject objectId;
        String line1 = "";
        String line2 = "";
        if (node1.childNodes().size() == 0) {
            return;
        }

        List<Node> dotNetNodes = node1.childNodes();
        List<Node> pipeDocNodes = node2.childNodes();

        int size = dotNetNodes.size();
        stringBuilderHTML = new String[size];

        line1 = dotNetNodes.toString().replaceAll("<br>", "");
        line2 = pipeDocNodes.toString().replaceAll("<br>", "");

//        if(line1.length()!=line2.length()){
        if (!(dotNetNodes.size() == pipeDocNodes.size())) {

            System.out.println(".NET: " + dotNetNodes.size() + "\npipe: " + pipeDocNodes.size());
        }


        if (dotNetNodes.size() == pipeDocNodes.size()) {
            for (int i = 0; i < dotNetNodes.size(); i++) {

                diffHTMLRecursion(pipeDocNodes.get(i), dotNetNodes.get(i), counter);
                line1 = (dotNetNodes.get(i).toString()).replaceAll("\n", "").replaceAll("\r", "").replaceAll(" ", "").replaceAll("<br>", "");
                line2 = (pipeDocNodes.get(i).toString()).replaceAll("\n", "").replaceAll("\r", "").replaceAll(" ", "").replaceAll("<br>", "");

                if (!line1.equals(line2)) {
                    counter++;
//                    this.html=
                    //    System.out.println(".Net: " + line1 + "\npipe: " + line2);
                    //     System.out.println();
                    //      jsonActions.buildJSONVer(line1,runHelperForTestsPreparationMethods.getSid(),isLast,"HTML","C:\\temp\\ArchivedFilesFromPusher\\test\\JsonResults\\htmlResults.json");
                    appendJson(line1 + "pipe" + line2);

                }

            }

        }

    }

    /**
     * ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
     **/

    public void appendJson(String str) {

        JSONObject list = new JSONObject();

        String str1[] = str.split("pipe");
        list.put(".NET", str1[0]);
        list.put("PIPE", str1[1]);
        childHTML.put("HTML", list);
        objectIdHTML = new org.json.JSONObject();
        objectIdHTML.put(runHelperForTestsPreparationMethods.getSid(), childHTML);
        childHTML = new JSONObject();
        mainObjHTML.append("SID", objectIdHTML);

        //   list.put(".NET",str);


    }

    @Ignore
    @Test(dependsOnMethods = {"compareTest"})
    public void createBundle() throws IOException, NoSuchAlgorithmException {

        File dirDotNet = new File("C:\\temp\\ArchivedFilesFromPusher\\test\\" + runHelperForTestsPreparationMethods.getSid());
        File dirPipe = new File("C:\\temp\\ArchivedFilesFromPusher\\test\\proclin2\\" + fh.calcMD5AndReturnMD5Date(runHelperForTestsPreparationMethods.getSid()) + "\\" +
                Configuration.prop.getProperty("suite.pusher.defaultParams.subsId") + "_" + Configuration.prop.getProperty("suite.pusher.defaultParams.optionalPid") + "_" + runHelperForTestsPreparationMethods.getSid());
        String Results = "C:\\temp\\ArchivedFilesFromPusher\\test\\JsonResults";
        // String zipFileName = "C:\\temp\\ArchivedFilesFromPusher\\test\\bundle_"+runHelperForTestsPreparationMethods.getSid()+".zip";
        String zipFileNameDotNet = "C:\\temp\\ArchivedFilesFromPusher\\test\\bundle_" + runHelperForTestsPreparationMethods.getSid() + "DotNet.zip";
        String zipFileNamePipe = "C:\\temp\\ArchivedFilesFromPusher\\test\\bundle_" + runHelperForTestsPreparationMethods.getSid() + "Pipe.zip";
        String zipFileNameResults = "C:\\temp\\ArchivedFilesFromPusher\\test\\bundle_" + runHelperForTestsPreparationMethods.getSid() + "Results.zip";

        fh.zip(dirDotNet.toString(), zipFileNameDotNet);
        fh.zip(dirPipe.toString(), zipFileNamePipe);
        fh.zip(Results, zipFileNameResults);

    }

}