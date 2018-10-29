package com.pipe.record.comparator.model;

import com.pipe.record.comparator.compare.RecordComparator;
import helpers.FileHandling;
import org.json.JSONException;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class CompareResults {

    public List<RecordComparator.FieldCompare> deepCompare;
    public String dotNetName = ".NET";
    public String pipeName = "pipe";
    FileHandling fileHandling = new FileHandling();

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
//                    ezer = string.split("pipe; ");
//                    resPipe = ezer[1].split(", comp")[0];
                    ezer = string.split(this.pipeName + "; ");
                    resPipe = ezer[1].split(", comp")[0];
                }


                list = new JSONObject();
                if (resPipe == "") {

                    string = string.replaceAll("[\\[\\]]", "");
                    if (string.indexOf("{") > 0 && string.split("[\\{]")[1].split("[\\}]")[0].contains(",")) {
                        string = string.replace(string.split("[\\{]")[1].split("[\\}]")[0], string.split("[\\{]")[1].split("[\\}]")[0].replaceAll(",", "."));
                    }

                    if (string.contains("\"https://doda.jp/DodaFront/View/JobSearchList.action?pr=27&oc=01L,02L,07L,08L,09L&so=50&ss=1&op=17,43,5,43,5,47,56&pic=1&ds=0&tp=1&bf=1&mpsc_sid=10&page=0&oldestDayWdtno=0\"")) {
                        string = string.replace("path; root / referrer, .NET; \"https://doda.jp/DodaFront/View/JobSearchList.action?pr=27&oc=01L%2c02L%2c07L%2c08L%2c09L&so=50&ss=1&op=17%2c43%2c5%2c43%2c5%2c47%2c56&pic=1&ds=0&tp=1&bf=1&mpsc_sid=10&page=0&oldestDayWdtno=0\",pipe; \"https://doda.jp/DodaFront/View/JobSearchList.action?pr=27&oc=01L,02L,07L,08L,09L&so=50&ss=1&op=17,43,5,43,5,47,56&pic=1&ds=0&tp=1&bf=1&mpsc_sid=10&page=0&oldestDayWdtno=0\", comp; false",
                                "path; root / referrer, .NET; \"https://doda.jp/DodaFront/View/JobSearchList.action?pr=27&oc=01L%2c02L%2c07L%2c08L%2c09L&so=50&ss=1&op=17%2c43%2c5%2c43%2c5%2c47%2c56&pic=1&ds=0&tp=1&bf=1&mpsc_sid=10&page=0&oldestDayWdtno=0\",pipe; \"https://doda.jp/DodaFront/View/JobSearchList.action?pr=27&oc=01L.02L.07L.08L.09L&so=50&ss=1&op=17.43.5.43.5.47.56&pic=1&ds=0&tp=1&bf=1&mpsc_sid=10&page=0&oldestDayWdtno=0\", comp; false");
                    }

                    if (string.contains("HTML page is missing, Missing data")) {
                        string = string.replace("path; root / Description, " + this.dotNetName + "; \"No load, HTML page is missing, Missing data (might affect playback)\"," + this.pipeName + "; \"No load,HTML page is missing,Missing data (might affect playback)\", comp; false"
                                , "path; root / Description, " + this.dotNetName + "; \"No load HTML page is missing Missing data (might affect playback)\"," + this.pipeName + "; \"No load HTML page is missing Missing data (might affect playback)\", comp; false");

                        string = string.replace("path; root / Description, " + this.dotNetName + "; \"HTML page is missing, Missing data (might affect playback)\"," + this.pipeName + "; \"\", comp; false"
                                , "path; root / Description, " + this.dotNetName + "; \"HTML page is missing Missing data (might affect playback)\"," + this.pipeName + "; \"\", comp; false");
                        string = string.replace("path; root / Description, " + this.dotNetName + "; \"HTML page is missing, Missing data (might affect playback)\"," + this.pipeName + "; \"Missing data (might affect playback)\", comp; false",
                                "path; root / Description, " + this.dotNetName + "; \"HTML page is missing Missing data (might affect playback)\"," + this.pipeName + "; \"Missing data (might affect playback)\", comp; false");
                    }

                    if (string.contains("No load, HTML page is missing")) {
                        string = string.replaceAll("path; root / Description, " + this.dotNetName + "; \"No load, HTML page is missing\", " + this.pipeName + "; \"No load,HTML page is missing\", comp; false"
                                , "path; root / Description, " + this.dotNetName + "; \"No load HTML page is missing\", " + this.pipeName + "; \"No load HTML page is missing\", comp; false");

                    }
                    if (string.contains("ct=enable,debug")) {
                        string = string.replace("ct=enable,debug", "ct=enable.debug");
                    }

                    String[] json1 = string.split(",");
                    list = new JSONObject();
                    for (int i = 0; i < json1.length; i++) {
                        String[] object = json1[i].split(";");
                        list.put(object[0], object[1]);
                    }

                } else {
//                    list.put("path", string.split(",")[0].split(";")[1].trim());
//                    list.put(".NET", resPath);
//                    list.put("pipe", resPipe);
//                    list.put("comp", string.split("comp; ")[1].split("[\\]]")[0].trim());
                    list.put("path", string.split(",")[0].split(";")[1].trim());
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
                System.out.println("Output files can be found under: C:\\temp\\ArchivedFilesFromPusher\\test\\JsonResults\\" + fileName);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }


    }
}
