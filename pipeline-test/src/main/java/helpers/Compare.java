package helpers;

import com.pipe.pipeapp.HttpGetRequest;
import com.pipe.pipeapp.PushSession;
import org.json.XML;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Node;
import org.testng.Assert;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Compare {
    Properties m_prop;
    CageArchivePackage cageArchivePackage;
    CageArchivePackage cageArchivePackagePipe;

    String cageCurrent; //
    String cagePrevious; //

    String recording ="";
    String events ="";
    String streams ="";
    String webPage = "";

    String recordingPipe ="";
    String eventsPipe ="";
    String streamsPipe ="";
    String webPagePipe = "";

    String[] stringBuilderHTML;

    public String sessionId="";
    int subId;
    int pid;

    boolean isLast;
    JsonActions jsonActions = new JsonActions();
    ArrayList<String> m_arrayList;
    String[] xml;
    String[] json;
    String[] dsr;
    String[] html;

    JSONObject childHTML = new JSONObject();
    private org.json.JSONObject objectIdHTML;
    private org.json.JSONObject mainObjHTML = new org.json.JSONObject();
    private PushSession pushSession = new PushSession();
    public void doWork(String session){


    }

    public Compare(Properties i_prop,ArrayList<String> i_arrayList){

        m_prop = i_prop;
        cageCurrent = m_prop.getProperty("suite.pusher.defaultParams.cagePipe");
        cagePrevious = m_prop.getProperty("suite.pusher.defaultParams.cageDotNet");
        m_arrayList = i_arrayList;


    }
    public Compare(){;}

    public Compare(Properties prop){
        this.m_prop = prop;
        this.cageCurrent = m_prop.getProperty("suite.pusher.defaultParams.cagePipe");
        this.cagePrevious = m_prop.getProperty("suite.pusher.defaultParams.cageDotNet");
    }

    public class WorkingTask implements Runnable {
        private final RecordsToCompare recordsToCompare;
        public String sid = "";


        public WorkingTask(RecordsToCompare pair) {
          recordsToCompare = pair;
        }
        //todo - not runnable static function in a class just to compare 2 records
        @Override
        public void run() {

            /*** Converting message that came from rabbitAMQP to jsonObject in order to manipulate
             *  values came back from AeroSpike to send a parallel request to cage
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } */

          try {


            System.out.println("Compare two records: \n\tprevious: "
                + recordsToCompare.previous.getKey() + "\n\tcurrent: "
                + recordsToCompare.current.getKey() );

            HttpGetRequest cageResponse = new HttpGetRequest();

            System.out.println("Getting cage response for processor previous version...");
            String responseDotNet = null;
            try {
              String path = cagePrevious + "/"
                  + recordsToCompare.previous.subscriber + "/"
                  + recordsToCompare.previous.project + "/"
                  + recordsToCompare.previous.session;
              responseDotNet = cageResponse.getRequest(path);
              System.out.println(path);

            } catch (Throwable e) {
              System.err.println("Failed to get cage results for previous in " + recordsToCompare + ". " + e.getMessage());
              e.printStackTrace();
            }

            System.out.println("Getting cage response for processor current version...");
            String responsePipe = null;
            try {

//              if (recordsToCompare.previous.cageArchivePackage == null) {
//                  // get from cafe
//              }
//                if (recordsToCompare.current.cageArchivePackage == null) {
//                    // get from cafe
//                }

                // Thread.sleep(1000);
              String path = cageCurrent + "/"
                  + recordsToCompare.current.subscriber + "/"
                  + recordsToCompare.current.project + "/"
                  + recordsToCompare.current.session;
              responsePipe = cageResponse.getRequest(path);
              System.out.println(path);
            } catch (Throwable e) {
              System.err.println("Failed to get cage results for current in " + recordsToCompare + ". " + e.getMessage());
              e.printStackTrace();
            }
            this.sid = recordsToCompare.current.session;
            sessionId = sid;
            System.out.println("Start comparison for " + recordsToCompare);
            getCageProducts(responseDotNet, responsePipe);

            recording = cageArchivePackage.getRecording();
            events = cageArchivePackage.getEvents();
            streams = cageArchivePackage.getStreams();
            webPage = cageArchivePackage.getWebPage();

            recordingPipe = cageArchivePackagePipe.getRecording();
            eventsPipe = cageArchivePackagePipe.getEvents();
            streamsPipe = cageArchivePackagePipe.getStreams();
            webPagePipe = cageArchivePackagePipe.getWebPage();
            //pushSession.saveFullDataSessions(recording,recordingPipe,events,eventsPipe,streams,streamsPipe);
            try {
              compareTests();

            } catch (IOException e) {
              e.printStackTrace();
            } catch (ParseException e) {
              e.printStackTrace();
            } catch (URISyntaxException e) {
              e.printStackTrace();
            }
            //do work
          } catch (Throwable e) {
            System.err.println("Failed to compare "  + (recordsToCompare != null ? recordsToCompare : "empty pair") + ". " + e.getMessage());
            e.printStackTrace();
          }
        }
    }

    public void compareTests() throws IOException, ParseException, URISyntaxException {
        testCompareJsonPercentage(recording,recordingPipe);
//        testCompareXMLPercentage(events,eventsPipe);
//        testCompareDSRPercentage(streams, streamsPipe);
//        diffHTML(webPage, webPagePipe);
    }

    public void compareTests(String recordingStaging, String recordingProd, String eventsStaging, String eventsProd,
                             String streamsStaging, String streamsProd, boolean isLast, String sid) throws IOException, ParseException, URISyntaxException {
       // this.sid = sid;
        testCompareJsonPercentage(recordingStaging,recordingProd);
        testCompareXMLPercentage(eventsStaging,eventsProd);
        testCompareDSRPercentage(streamsStaging, streamsProd);

//        diffHTML(webPage, webPagePipe);
    }

  public void compareChunk(ArrayList<RecordsToCompare> chunk) throws ParseException, IOException, InterruptedException {
    // TODO PDT Please make refactoring for the thread usage, use task execution instead of the thread creation by new Thread
    ArrayList<Thread> threads = new ArrayList<>(chunk.size());
    for (RecordsToCompare pair : chunk) {
      long startTime = System.currentTimeMillis();
      Thread t = new Thread(new WorkingTask(pair));
      threads.add(t);
      t.start();
      long estimatedTime = System.currentTimeMillis() - startTime;
      System.out.println(estimatedTime);
    }
    for (Thread i : threads) {
      i.join();
    }
  }

    public void getCageProducts(String responseDotNet, String responsePipe){

        cageArchivePackage = new CageArchivePackage(responseDotNet);
        cageArchivePackage = cageArchivePackage.getCageArchivePackageObject();

        cageArchivePackagePipe = new CageArchivePackage(responsePipe);
        cageArchivePackagePipe = cageArchivePackagePipe.getCageArchivePackage();


    }

    public void testCompareDSRPercentage(String val1, String val2) throws ParseException, IOException, URISyntaxException {

        val1 = XML.toJSONObject(val1).toString();
        val2 = XML.toJSONObject(val2).toString();
        int countBad = 0;
        int countGood = 0;
        val1 = this.jsonActions.removeUnwantedJSONFieldsFROMRecordingAndReturnJsonAsString(val1);
        val2 = this.jsonActions.removeUnwantedJSONFieldsFROMRecordingAndReturnJsonAsString(val2);
        compareDSRFilesTest(val1, val2, isLast);
        List<JsonActions.FieldCompare> a = this.jsonActions.deepCompare(val1, val2);

        for(int i = 0; i < a.size(); ++i) {
            if (!((JsonActions.FieldCompare)a.get(i)).getDotNet().equals("null") || !((JsonActions.FieldCompare)a.get(i)).getPipe().equals("null")) {
                boolean status = ((JsonActions.FieldCompare)a.get(i)).getCmp();
                if (status) {
                    ++countGood;
                } else {
                    ++countBad;
                }
            }
        }

        float size = (float)(countBad + countGood);
        float percentage = (float)countGood * 100.0F / size;
        if (percentage == 100.0F) {
            System.out.println("Files are completely identical! ");
        }

        System.out.println("Number of good comparisons are(DSR / Metadata): " + countGood);
        System.out.println("Number of bad comparisons are (DSR / Metadata): " + countBad);
        System.out.println("Percantage of Success(DSR / Metadata): " + percentage + "%");
        Assert.assertTrue(percentage >= 95.0F, "Good: " + countGood + "\nBad: " + countBad);
    }

    public void testCompareXMLPercentage(String val1, String val2) throws IOException, URISyntaxException, ParseException {

        val1 = XML.toJSONObject(val1).toString();
        val2 = XML.toJSONObject(val2).toString();

        val1 = this.jsonActions.removeUnwantedJSONFieldsFromXMLAndReturnJsonAsString(val1);
        val2 = this.jsonActions.removeUnwantedJSONFieldsFromXMLAndReturnJsonAsString(val2);

        compareXmlFilesTest(val1, val2, isLast);
        List<JsonActions.FieldCompare> a = this.jsonActions.deepCompare(val1, val2);//todo make compareXMLFiles to return list 'a'

        int countBad = 0;
        int countGood = 0;

//        for(JsonActions.FieldCompare el : a){
//            if (!((JsonActions.FieldCompare)el.getDotNet().equals("null") || !((JsonActions.FieldCompare)a.get(i)).getPipe().equals("null")) {
//                //todo - change logical var name (dotNet - first, etc)
//                //todo - remove if and put it inside field compare class
//                boolean status = ((JsonActions.FieldCompare)a.get(i)).getCmp();
//                if (status) {
//                    ++countGood;
//                } else {
//                    ++countBad;
//                }
//        }
        for(int i = 0; i < a.size(); ++i) {
            if (!((JsonActions.FieldCompare)a.get(i)).getDotNet().equals("null") || !((JsonActions.FieldCompare)a.get(i)).getPipe().equals("null")) {

                boolean status = ((JsonActions.FieldCompare)a.get(i)).getCmp();
                if (status) {
                    ++countGood;
                } else {
                    ++countBad;
                }
            }
        }

        float size = (float)(countBad + countGood);
        float percentage = (float)countGood * 100.0F / size;
        System.out.println("Number of good comparisons are (XML / Events): " + countGood);
        System.out.println("Number of bad comparisons are (XML / Events): " + countBad);
        System.out.println("Percantage of Success (XML / Events): " + percentage + "%");
    }

    public void testCompareJsonPercentage(String val1, String val2) throws IOException, ParseException {

        int countBad = 0;
        int countGood = 0;
      //  val1 = this.jsonActions.removeUnwantedJSONFieldsFROMRecordingAndReturnJsonAsString(val1);
        val1 = this.jsonActions.removeUnwantedJSONFieldsFROMRecordingAndReturnJsonAsString(val1);
        val2 = this.jsonActions.removeUnwantedJSONFieldsFROMRecordingAndReturnJsonAsString(val2);
        this.compareJsonFilesTest(val1, val2, isLast);
        List<JsonActions.FieldCompare> a = this.jsonActions.deepCompare(val1, val2);

        for(int i = 0; i < a.size(); ++i) {
            if (!((JsonActions.FieldCompare)a.get(i)).getDotNet().equals("null") || !((JsonActions.FieldCompare)a.get(i)).getPipe().equals("null")) {
                boolean status = ((JsonActions.FieldCompare)a.get(i)).getCmp();
                if (status) {
                    ++countGood;
                } else {
                    ++countBad;
                }
            }
        }

        float size = (float)(countBad + countGood);
        float percentage = (float)countGood * 100.0F / size;
        if (percentage == 100.0F) {
            System.out.println("Files are completely identical! ");
        }

        System.out.println("Number of good comparisons are(Json / Metadata): " + countGood);
        System.out.println("Number of bad comparisons are (Json / Metadata): " + countBad);
        System.out.println("Percantage of Success(Json / Metadata): " + percentage + "%");
     //   Assert.assertTrue(percentage >= 95.0F, "Good: " + countGood + "\nBad: " + countBad);
    }

    public void compareDSRFilesTest(String dotNet, String pipe, boolean isLast) throws IOException, URISyntaxException, ParseException {
        List<JsonActions.FieldCompare> a = this.jsonActions.deepCompare(dotNet, pipe);
        dotNet = dotNet.toString();
        pipe = pipe.toString();

//        dotNet = XML.toJSONObject(dotNet).toString();
//        pipe = XML.toJSONObject(pipe).toString();
        this.dsr = new String[a.size()];
        this.dsr = new String[a.size()];
        int j = 0;

        for(int i = 0; i < a.size(); ++i) {
            boolean status = ((JsonActions.FieldCompare)a.get(i)).getCmp();
            if (!status && (!((JsonActions.FieldCompare)a.get(i)).getDotNet().equals("null") || !((JsonActions.FieldCompare)a.get(i)).getPipe().equals("null"))) {
                this.dsr[j] = ((JsonActions.FieldCompare)a.get(i)).toString();
                ++j;
            }
        }

        this.dsr = this.jsonActions.removeNULLElementsFromStringArray(this.dsr);
        this.jsonActions.buildJSONVer(this.dsr, this.sessionId, isLast, "DSR", "dsrResults.json");
    }

    public void compareXmlFilesTest(String dotNet, String pipe, boolean isLast) throws IOException, URISyntaxException, ParseException {
        List<JsonActions.FieldCompare> a = this.jsonActions.deepCompare(dotNet, pipe);
        this.xml = new String[a.size()];
        int j = 0;

        for(int i = 0; i < a.size(); ++i) {
            boolean status = ((JsonActions.FieldCompare)a.get(i)).getCmp();
            if (!status) {
                this.xml[j] = ((JsonActions.FieldCompare)a.get(i)).toString();
                ++j;
            }
        }

        this.xml = this.jsonActions.removeNULLElementsFromStringArray(this.xml);
        this.jsonActions.buildJSONVer(this.xml, this.sessionId, this.isLast, "XML", "eventsResults.json");
    }
    public void compareJsonFilesTest(String dotNet, String pipe, boolean isLast) throws IOException, ParseException {
        List<JsonActions.FieldCompare> a = this.jsonActions.deepCompare(dotNet, pipe);
        this.json = new String[a.size()];
        int j = 0;

        for(int i = 0; i < a.size(); ++i) {
            boolean status = ((JsonActions.FieldCompare)a.get(i)).getCmp();
            if (!status && (!((JsonActions.FieldCompare)a.get(i)).getDotNet().equals("null") || !((JsonActions.FieldCompare)a.get(i)).getPipe().equals("null"))) {
                this.json[j] = ((JsonActions.FieldCompare)a.get(i)).toString();
                ++j;
            }
        }



        this.json = this.jsonActions.removeNULLElementsFromStringArray(this.json);
        this.jsonActions.buildJSONVer(this.json, this.sessionId, this.isLast, "JSON", "jsonResults.json");
    }

    public void diffHTML(String val1, String val2) throws IOException, URISyntaxException {

        Document docNetDoc = Jsoup.parse(val1);
        Document pipeDoc = Jsoup.parse(val2);
        List<Node> dotNetNodes = docNetDoc.childNodes();
        List<Node> pipeDocNodes = pipeDoc.childNodes();
        int size = dotNetNodes.size();
        dotNetNodes.toString().replaceAll("\n", "");
        pipeDocNodes.toString().replaceAll("\n", "");
        int counter = 0;

        for(int i = 0; i < size; ++i) {
            this.diffHTMLRecursion((Node)dotNetNodes.get(i), (Node)pipeDocNodes.get(i), counter);
        }

        if (this.mainObjHTML == null) {
            System.out.println("Files are perfectly identical!");
        }

        System.out.println(this.mainObjHTML);
    }

    private void diffHTMLRecursion(Node node1, Node node2, int counter) {
        new org.json.JSONObject();
        new org.json.JSONObject();
        new JSONObject();
        String line1 = "";
        String line2 = "";
        if (node1.childNodes().size() != 0) {
            List<Node> dotNetNodes = node1.childNodes();
            List<Node> pipeDocNodes = node2.childNodes();
            int size = dotNetNodes.size();
            this.stringBuilderHTML = new String[size];
            line1 = dotNetNodes.toString().replaceAll("<br>", "");
            line2 = pipeDocNodes.toString().replaceAll("<br>", "");
            if (dotNetNodes.size() != pipeDocNodes.size()) {
                System.out.println(".NET: " + dotNetNodes.size() + "\npipe: " + pipeDocNodes.size());
            }

            if (dotNetNodes.size() == pipeDocNodes.size()) {
                for(int i = 0; i < dotNetNodes.size(); ++i) {
                    this.diffHTMLRecursion((Node)pipeDocNodes.get(i), (Node)dotNetNodes.get(i), counter);
                    line1 = ((Node)dotNetNodes.get(i)).toString().replaceAll("\n", "").replaceAll("\r", "").replaceAll(" ", "").replaceAll("<br>", "");
                    line2 = ((Node)pipeDocNodes.get(i)).toString().replaceAll("\n", "").replaceAll("\r", "").replaceAll(" ", "").replaceAll("<br>", "");
                    if (!line1.equals(line2)) {
                        ++counter;
                        this.appendJson(line1 + "pipe" + line2);
                    }
                }
            }

        }
    }

    public void appendJson(String str) {
        JSONObject list = new JSONObject();
        String[] str1 = str.split("pipe");
        list.put(".NET", str1[0]);
        list.put("PIPE", str1[1]);
        this.childHTML.put("HTML", list);
        this.objectIdHTML = new org.json.JSONObject();
        this.objectIdHTML.put(sessionId.toString(), this.childHTML);
        this.childHTML = new JSONObject();
        this.mainObjHTML.append("SID", this.objectIdHTML);
    }
}

