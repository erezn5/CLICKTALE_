package com.pipe.record.comparator.compare;

import com.google.gson.*;
import helpers.FileHandling;
import org.json.simple.JSONArray;
//import org.json.simple.JSONArray;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RecordComparator {

    StringBuilder sb;
    org.json.JSONObject builderObject = new org.json.JSONObject();
    JSONArray jsonArray = new JSONArray();
//    JsonObject builderObject = new JsonObject();
//    JsonArray jsonArray = new JsonArray();
    FileHandling fileHandling = new FileHandling();

    static private String jeToString(JsonElement je) {
        return je == null ? "null" : je.toString();
    }

    private JsonParser parser = new JsonParser();
    public String dotNetName = ".NET";//todo - change to Staging
    public String pipeName = "pipe";//todo - change to Prod


    //--------------------For Side by Side Tests Json values pipe vs processor .NET-------------------------------------
    public List<RecordComparator.FieldCompare> deepCompare(String dotNet,
                                                      String pipe) throws IOException {

        return this.parser == null ?
                new ArrayList<>() :
                deepCompare(
                        parser.parse(dotNet),
                        parser.parse(pipe)
                );
    }


    public List<RecordComparator.FieldCompare> deepCompare(JsonElement dotNet,
                                                      JsonElement pipe) {

        return deepCompare(
                "root",
                new ArrayList<>(),
                dotNet,
                pipe);
    }

    private List<RecordComparator.FieldCompare> deepCompare(String path,
                                                       List<RecordComparator.FieldCompare> acc,
                                                       JsonElement dotNet,
                                                       JsonElement pipe) {
        if (path.contains("tapCount")) {
            System.out.println("stop");
        }
        if (dotNet == null) {
            return acc;
        }

        if (dotNet.isJsonPrimitive()) {
            acc.add(compare(path, dotNet.getAsJsonPrimitive(), pipe));

            return acc;
        }
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
            acc.add(new RecordComparator.FieldCompare(path, dotNet.toString(), jeToString(pipe), false));
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

    private RecordComparator.FieldCompare compare(String path,
                                             JsonNull jsonNull,
                                             JsonElement pipe) {
        return new RecordComparator.FieldCompare(
                path,
                jsonNull.toString(),
                jeToString(pipe),
                pipe.isJsonNull());
    }


    private RecordComparator.FieldCompare compare(String path,
                                             JsonPrimitive dotNet,
                                             JsonElement pipe) {
        return new RecordComparator.FieldCompare(
                path,
                dotNet.toString(),
                jeToString(pipe),
                dotNet.equals(pipe));
    }

    private RecordComparator.FieldCompare compare(String path, JsonArray dotNet, JsonArray pipe) {
        return new RecordComparator.FieldCompare(path, dotNet.toString(), jeToString(pipe), dotNet.equals(pipe));
    }

    // TODO - identical to the primitive treatment but this will not always be
    private RecordComparator.FieldCompare compare(String path,
                                             JsonArray dotNet,
                                             JsonElement pipe) {
        return new RecordComparator.FieldCompare(
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
        }
    }


}
