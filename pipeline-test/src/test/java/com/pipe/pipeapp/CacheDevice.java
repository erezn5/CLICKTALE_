package com.pipe.pipeapp;

import helpers.Compare;
import helpers.JsonActions;
import junit.framework.TestCase;
import org.json.simple.parser.ParseException;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.logging.Logger;

public class CacheDevice extends TestCase {
    private static final Logger LOGGER = Logger.getLogger(CacheDevice.class.getName());
    PushSession pushSession = new PushSession();
    JsonActions jsonActions = new JsonActions();
    boolean isLast;

    @Test
    public void gettingCacheDeviceJsonFilesAndCompare() throws IOException, ParseException, URISyntaxException, InterruptedException {
        CompareSideBySide compareSideBySide = new CompareSideBySide();
        jsonActions = new JsonActions();
        Compare compare = new Compare();
        pushSession = new PushSession();
        System.out.println("Getting cache device from staging environment...");
        String deciveCacheStaging=jsonActions.readJsonContent("C:\\Users\\erez.naim\\Documents\\DeviceCacheStaging.json");
        System.out.println("Done!");
        System.out.println("Getting cache device from production environment...");
        String deviceCacheProduction = jsonActions.readJsonContent("C:\\Users\\erez.naim\\Documents\\DeviceCacheProduction.json");
        System.out.println("Done!");
        compareSideBySide.compareJsonFilesTest(deciveCacheStaging,deviceCacheProduction,true);

    }


}