package helpers;

import com.google.gson.*;
import org.json.JSONException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;


public class JsonActions {

    StringBuilder sb;
    org.json.JSONObject builderObject = new org.json.JSONObject();
    JSONArray jsonArray = new JSONArray();
    FileHandling fileHandling = new FileHandling();

    static private String jeToString(JsonElement je) {
        return je == null ? "null" : je.toString();
    }

    private JsonParser parser = new JsonParser();
    public String dotNetName = ".NET";//todo - change to Staging
    public String pipeName = "pipe";//todo - change to Prod


    //--------------------For Side by Side Tests Json values pipe vs processor .NET-------------------------------------
    public List<FieldCompare> deepCompare(String dotNet,
                                          String pipe) throws IOException {

        return this.parser == null ?
                new ArrayList<>() :
                deepCompare(
                        parser.parse(dotNet),
                        parser.parse(pipe)
                );
    }


    public List<FieldCompare> deepCompare(JsonElement dotNet,
                                          JsonElement pipe) {

        return deepCompare(
                "root",
                new ArrayList<>(),
                dotNet,
                pipe);
    }
    //--------------------------------------------------------------------------------------------
    public boolean compareJsonArray(JsonElement jsonElement1, JsonElement jsonElement2) {
        boolean isEqual = true;
        // Check whether both jsonElement are not null
        if (jsonElement1 != null && jsonElement2 != null) {

            // Check whether both jsonElement are objects
            if (jsonElement1.isJsonObject() && jsonElement2.isJsonObject()) {
                Set<Map.Entry<String, JsonElement>> ens1 = ((JsonObject) jsonElement1).entrySet();
                Set<Map.Entry<String, JsonElement>> ens2 = ((JsonObject) jsonElement2).entrySet();
                JsonObject json2obj = (JsonObject) jsonElement2;
                if (ens1 != null && ens2 != null && (ens2.size() == ens1.size())) {
                    // Iterate JSON Elements with Key values
                    for (Map.Entry<String, JsonElement> en : ens1) {
                        isEqual = isEqual && compareJsonArray(en.getValue(), json2obj.get(en.getKey()));
                    }
                } else {
                    return false;
                }
            }

            // Check whether both jsonElement are arrays
            else if (jsonElement1.isJsonArray() && jsonElement2.isJsonArray()) {
                JsonArray jarr1 = jsonElement1.getAsJsonArray();
                JsonArray jarr2 = jsonElement2.getAsJsonArray();

                if (jarr1.size() != jarr2.size()) {
                    return false;
                } else {
                    // Iterate JSON Array to JsonElement(plural)
                    for (JsonElement je1 : jarr1) {
                        boolean flag = false;
                        for(JsonElement je2 : jarr2){
                            flag = compareJsonArray(je1, je2);
                            if(flag){
                                jarr2.remove(je2);
                                break;
                            }
                        }
                        isEqual = isEqual && flag;
                    }
                }
            }

            // Check whether both jsonElement are null
            else if (jsonElement1.isJsonNull() && jsonElement2.isJsonNull()) {

                return true;
            }

            // Check whether both jsonElement are primitives
            else if (jsonElement1.isJsonPrimitive() && jsonElement2.isJsonPrimitive()) {
                if (jsonElement1.equals(jsonElement2)) {

                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else if (jsonElement1 == null && jsonElement2 == null) {
            return true;
        } else {
            return false;
        }
        System.out.println(isEqual);
        return isEqual;

    }

    //---------------------------------------------------------------------------------------------------
    private List<FieldCompare> deepCompare(String path,
                                           List<FieldCompare> acc,
                                           JsonElement dotNet,
                                           JsonElement pipe) {

        if (dotNet == null) {
            return acc;
        }

        if (dotNet.isJsonPrimitive()) {
            acc.add(compare(path, dotNet.getAsJsonPrimitive(), pipe));

            return acc;
        }
//        if(dotNet.isJsonArray() && pipe.isJsonArray()){
//            compareJsonArray(dotNet, pipe);
//        }
//        if(json1.isJsonArray() && json2.isJsonNull()){
//            acc.add(compare(path, json1.getAsJsonArray(),json2.getAsJsonNull()));
//        }
//        if(dotNet.isJsonArray() && pipe.isJsonArray()){
//            acc.add(compare(path,dotNet.getAsJsonArray(),pipe.getAsJsonArray()));
//        }
        if (dotNet.isJsonArray()) {
            acc.add(compare(path, dotNet.getAsJsonArray(), pipe));
            return acc;
        }

        //    for the moment the array is just like a base case
//        if (json1.isJsonNull()) {
//            acc.add(compare(path, json1.getAsJsonNull(), json2));
//            return acc;
//        }

        // json1 is a JsonObject but we don't know about json2
        if (pipe == null || !pipe.isJsonObject()) {
            acc.add(new FieldCompare(path, dotNet.toString(), jeToString(pipe), false));
            return acc;
        }

        // ...then it must be an JsonObject
        JsonObject jo2 = pipe.getAsJsonObject();
        return dotNet.getAsJsonObject().entrySet().stream().map(entry -> {
            String updatedPath = String.format("%s / %s", path, entry.getKey());
            return deepCompare(updatedPath, new ArrayList<>(), entry.getValue(), jo2.get(entry.getKey()));
        }).reduce(
                new ArrayList<>(),
                (fc1, fc2) -> {
                    fc1.addAll(fc2);
                    return fc1;
                });

    }

    private FieldCompare compare(String path,
                                 JsonNull jsonNull,
                                 JsonElement pipe) {
        return new FieldCompare(
                path,
                jsonNull.toString(),
                jeToString(pipe),
                pipe.isJsonNull());
    }


    private FieldCompare compare(String path,
                                 JsonPrimitive dotNet,
                                 JsonElement pipe) {
        return new FieldCompare(
                path,
                dotNet.toString(),
                jeToString(pipe),
                dotNet.equals(pipe));
    }

    private FieldCompare compare(String path, JsonArray dotNet, JsonArray pipe) {
        return new FieldCompare(path, dotNet.toString(), jeToString(pipe), dotNet.equals(pipe));
    }

    // TODO - identical to the primitive treatment but this will not always be
    private FieldCompare compare(String path,
                                 JsonArray dotNet,
                                 JsonElement pipe) {
        return new FieldCompare(
                path,
                dotNet.toString(),
                jeToString(pipe),
                dotNet.equals(pipe));
    }


    public class FieldCompare {

        public String dotNetName = ".NET";//todo - change to Staging
        public String pipeName = "pipe";//todo - change to Prod

        public FieldCompare(String jsonPath, String dotNet, String pipe, Boolean cmp) {
            this.jsonPath = jsonPath;
            this.dotNet = dotNet;
            this.pipe = pipe;
            this.cmp = cmp;
        }

        private String jsonPath = null;
        private String dotNet = null;
        private String pipe = null;
        private Boolean cmp = false;

        public String getJsonPath() {
            return jsonPath;
        }

        public String getDotNet() {
            return dotNet;
        }

        public String getPipe() {
            return pipe;
        }

        public Boolean getCmp() {
            return cmp;
        }

        @Override
        public String toString() {
//            return String.format(
//                    "[path; %s, .NET; %s, pipe; %s, comp; %s]",
//                    getJsonPath(), getDotNet(), getPipe(), getCmp().toString());
            return String.format(
                    "[path; %s,, " + this.dotNetName + "; %s,," + this.pipeName + "; %s,, comp; %s]",
                    getJsonPath(), getDotNet(), getPipe(), getCmp().toString());
//            return String.format(
//                    "[path; %s, " + this.dotNetName + "; %s," + this.pipeName + "; %s, comp; %s]",
//                    getJsonPath(), getDotNet(), getPipe(), getCmp().toString());
        }
    }
    //---------------------------------------------END------------------------------------------------------------------

    public String removeUnwantedJSONFieldsFROMRecordingAndReturnJsonAsString(String strJson) throws ParseException {
        JSONObject jsonObject = new JSONObject();
        jsonObject = removeJsonFields(strJson);
//        jsonObject = removeJsonFieldsDuplicator(strJson);
        strJson = jsonObject.toString();
        return strJson;
    }

    private JSONObject removeJsonFieldsDuplicator(String strJson) throws ParseException {
        //todo - insert vals into properties file and populate local string array with vals (using split) - constractor
        JSONObject jsonObject = convertStringToJson(strJson);
        jsonObject.remove("PID");//PIPELINE-423
        jsonObject.remove("cage_ip");
        jsonObject.remove("cage_version");
        jsonObject.remove("cage_time");
        jsonObject.remove("WebPageHash");
        jsonObject.remove("SubscriberId");//PIPELINE-423
        jsonObject.remove("ProcessingDateTimeUtc");//PIPELINE-423
        jsonObject.remove("ProcessingDateTimeZone");//PIPELINE-423
        jsonObject.remove("RecordingGUID");//PIPELINE-423
        jsonObject.remove("XMLSize");//PIPELINE-423
        jsonObject.remove("MachineName");//PIPELINE-423
        jsonObject.remove("IpAddress");//PIPELINE-423
        jsonObject.remove("ProcessVersion");//PIPELINE-423
        jsonObject.remove("ClientRecordingDateTimeZone");//PIPELINE-423
        jsonObject.remove("ClientRecordingDateTimeUtc");//PIPELINE-423
        jsonObject.remove("ClientDate");//PIPELINE-423
        jsonObject.remove("AuthenticationUnixTime");//PIPELINE-423
        jsonObject.remove("CreateDate");//PIPELINE-423
        jsonObject.remove("AuthenticationUnixTime");//PIPELINE-423
        jsonObject.remove("RecordingDate");//PIPELINE-423
        jsonObject.remove("RecordingDateJs");//PIPELINE-423
        jsonObject.remove("RecordingEndDate");//PIPELINE-423

        return jsonObject;


    }

    public String[] removeNULLElementsFromStringArray(String[] json) {

        return json = Arrays.stream(json).filter(s -> (s != null && s.length() > 0)).toArray(String[]::new);
    }

    public String removeUnwantedJSONFieldsFromXMLAndReturnJsonAsString(String strJson) throws ParseException {
        JSONObject jsonObject = new JSONObject();
        jsonObject = removeXmlFields(strJson);
//        jsonObject = removeXmlFieldsDuplicator(strJson);

        strJson = jsonObject.toString();
        return strJson;
    }

    public String readJsonContent(String filePath) throws IOException {
        sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new FileReader(filePath));

        try {
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append("\n");
                line = br.readLine();
            }

//            System.out.println(sb);
        } catch (IOException e1) {
            e1.printStackTrace();
        } finally {
            br.close();
        }

        return sb.toString();
    }

    public JSONObject loadJsonFromFile(String filePath) {
        JSONParser parser = new JSONParser();
        JSONObject jsonObject = new JSONObject();
        try {
            Object obj = parser.parse(new FileReader(filePath));
            jsonObject = (JSONObject) obj;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return jsonObject;
    }

    public JSONObject convertStringToJson(String recording) throws org.json.simple.parser.ParseException {
        JSONObject jsonObject = new JSONObject();
        JSONParser parser = new JSONParser();
        jsonObject = (JSONObject) parser.parse(recording);

        return jsonObject;

    }

    public JSONObject removeXmlFieldsDuplicator(String json) throws ParseException {
        JSONObject jsonObject = convertStringToJson(json);
        jsonObject = (JSONObject) jsonObject.get("recording");
        jsonObject.remove("procVersion");
        jsonObject.remove("rtime");
        jsonObject.remove("machine");
        jsonObject.remove("procIp");
        jsonObject.remove("pid");

        return jsonObject;

    }

    public JSONObject removeXmlFields(String json) throws ParseException {
        JSONObject jsonObject = convertStringToJson(json);
        jsonObject = (JSONObject) jsonObject.get("recording");
        // jsonObject.remove("pageContentSize");
        jsonObject.remove("messagesSize");
        jsonObject.remove("webPageHash");
//        jsonObject.remove("operatingSystem");//PIPELINE-505 V
        jsonObject.remove("pageContentSize");//Not a bug
//        jsonObject.remove("DeviceTypeID");//PIPELINE-472
//        jsonObject.remove("deviceType");//PIPELINE-506 V
        jsonObject.remove("rtime");
        jsonObject.remove("machine");
        jsonObject.remove("procVersion");
        jsonObject.remove("procIp");


        return jsonObject;

    }


    public JSONObject removeJsonFields(String json) throws ParseException {

        JSONObject jsonObject = convertStringToJson(json);
        jsonObject.remove("AuthenticationUnixTime");//PIPELINE-423
        jsonObject.remove("RecordingGUID");// PIPELINE-471
        jsonObject.remove("HTMLSize");
        jsonObject.remove("ClientRecordingDateTimeUtc");
        jsonObject.remove("ContentSize");
        jsonObject.remove("CreateDate");
        jsonObject.remove("MessagesSize");
        jsonObject.remove("RecordingDate");
        jsonObject.remove("RecordingDateJs");
        jsonObject.remove("IpAddress");
        jsonObject.remove("ProcessVersion");
        jsonObject.remove("XMLSize");
        jsonObject.remove("MachineName");
        jsonObject.remove("Tags");
        jsonObject.remove("avroCompressedSize");
        jsonObject.remove("cage_ip");
        jsonObject.remove("cage_time");
        jsonObject.remove("cage_version");
        jsonObject.remove("avroSize");
        jsonObject.remove("ProcessingDateTimeUtc");
        jsonObject.remove("OriginalSID");
        jsonObject.remove("WebPageHash");
        jsonObject.remove("WebpageHash");
        jsonObject.remove("DSRSize");
        jsonObject.remove("HtmlCharset");
        jsonObject.remove("ClientDate");


        return jsonObject;

    }


    public JSONArray parseJsonFromResourcesFolderAndReturnLiveSessionDataCollectionJsonArray(String filePath) throws IOException, ParseException {

        sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new FileReader(filePath));


        try {
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append("\n");
                line = br.readLine();
            }
            sb.toString();
//            System.out.println(sb);
        } catch (IOException e1) {
            e1.printStackTrace();
        } finally {
            br.close();
        }
        JSONParser js = new JSONParser();
        JSONObject json = (JSONObject) js.parse(sb.toString());
        jsonArray = (JSONArray) json.get("LiveSessionDataCollection");

        return jsonArray;
    }

    public String[] parseSIDFileFromPathAndReturnStringArray(String filePathSID) throws ParseException, IOException {

        JSONArray jsonArraySID = new JSONArray();
        JSONParser parser = new JSONParser();
        Object object = parser.parse((new FileReader(filePathSID)));
        JSONObject jsonObject1 = (JSONObject) object;
        jsonArraySID = (JSONArray) jsonObject1.get("pageviewCollection");

        String[] array = new String[jsonArraySID.size()];

        for (int i = 0; i < jsonArraySID.size(); i++) {
            JSONObject jsonObject = (JSONObject) jsonArraySID.get(i);

            array[i] = jsonObject.get("SID").toString();
        }

        return array;

    }


    public JsonElement convertJSONArrayTOJsonArray(JSONArray arr) {
        String arrayToConvertString = arr.toString();
        JsonElement jsonElement = parser.parse(arrayToConvertString);
        return jsonElement;
    }

    public void buildJSONVer(String[] json, String sid, boolean isLast, String kind, String fileName) throws IOException {

//        for (int i = 0; i < json.length; i++) {
//            System.out.println(json[i]);
//        }

        org.json.JSONObject mainObj = new org.json.JSONObject();
        org.json.JSONObject child = new org.json.JSONObject();
        JSONObject list;
        JSONObject objectId;
        try {

            for (String string : json) {
                String resPipe = "";
                String resPath = "";
                if (string.split("[\\[]").length > 2) {
                    String[] ezer = string.split("[\\[]");
                    int i = 2;
                    Boolean flag = true;
                    resPath = "[";
                    while (i < ezer.length && flag) {
//                        if (ezer[i].contains("pipe;")) {
                        if (ezer[i].contains(this.pipeName + ";")) {
//                        if (ezer[i].contains("Prod;")) {
                            resPath += ezer[i].split(" ")[0];
                            flag = false;
                            break;
                        }

                        resPath += ezer[i] + '[';
                        i++;
                    }

                    ezer = string.split(this.pipeName + "; ");
//                    resPipe = ezer[1].split(", comp")[0];
                    resPipe = ezer[1].split(",, comp")[0];
                }


                list = new JSONObject();
                if (resPipe == "") {

                    string = string.replaceAll("[\\[\\]]", "");
                    if (string.indexOf("{") > 0 && string.split("[\\{]")[1].split("[\\}]")[0].contains(",")) {
                        string = string.replace(string.split("[\\{]")[1].split("[\\}]")[0], string.split("[\\{]")[1].split("[\\}]")[0].replaceAll(",", "."));
                    }

                    String[] json1 = string.split(",,");
                    list = new JSONObject();
                    for (int i = 0; i < json1.length; i++) {
                        String[] object = json1[i].split(";");
                        list.put(object[0], object[1]);
                    }

                } else {

//                    list.put("path", string.split(",")[0].split(";")[1].trim());
                    list.put("path", string.split(",,")[0].split(";")[1].trim());

                    list.put(this.dotNetName, resPath);
                    list.put(this.pipeName, resPipe);
                    list.put("comp", string.split("comp; ")[1].split("[\\]]")[0].trim());

                }

                child.append(kind, list);
            }

            objectId = new JSONObject();
            objectId.put(sid, child);
            mainObj.put("SID", objectId);
            System.out.println(mainObj);

            fileHandling.createFolder("C:\\temp\\ArchivedFilesFromPusher\\test\\JsonResults");

            if (!new File("C:\\temp\\ArchivedFilesFromPusher\\test\\JsonResults\\" + fileName).exists()) {
                fileHandling.writeToExistedFile("C:\\temp\\ArchivedFilesFromPusher\\test\\JsonResults\\" + fileName, "[");
            }
            if (!isLast)
                fileHandling.writeToExistedFile("C:\\temp\\ArchivedFilesFromPusher\\test\\JsonResults\\" + fileName, mainObj.toString() + ",");

            if (isLast) {
                fileHandling.writeToExistedFile("C:\\temp\\ArchivedFilesFromPusher\\test\\JsonResults\\" + fileName, mainObj.toString() + "]");
                removeEmptyObjectsFromJsonArray("C:\\temp\\ArchivedFilesFromPusher\\test\\JsonResults\\" + fileName);
                System.out.println("Output files can be found under: C:\\temp\\ArchivedFilesFromPusher\\test\\JsonResults\\" + fileName);//todo relative path
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    private void removeEmptyObjectsFromJsonArray(String fileName) throws IOException {
        String json = readJsonContent(fileName);
//        String[] removedNull = Arrays.stream(stringArray)
//                .filter(value ->
//                        value != null && value.length() > 0
//                )
//                .toArray(size -> new String[size]);
    }

}
