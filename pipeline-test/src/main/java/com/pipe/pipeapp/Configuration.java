package com.pipe.pipeapp;


import org.junit.runners.Parameterized;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Parameters;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Configuration {

    public static Properties prop ;
    public static PusherParams pp;

    @BeforeSuite
    @Parameters({"config"})

    public void TestConfiguration(String config) {

        prop = new Properties();

        InputStream input = null;

        try {
            input = new FileInputStream(config);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        // input = getClass().getClassLoader().getResourceAsStream(config);
        try {
            prop.load(input);
      //      PusherParams pp = new PusherParams();
        } catch (IOException e) {
            e.printStackTrace();
        }
        pp = new PusherParams();

    }

}
