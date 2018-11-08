package com.keywee.shorten;

import java.io.IOException;
import java.util.Properties;

public class FactoryHelper {
    final static String propertiesFile = "/application.properties";
    private static Properties prop;

    public FactoryHelper() throws IOException {
        prop = new Properties();
        prop.load((ManagerService.class.getResourceAsStream(propertiesFile)));
    }

    public Properties getPropFile(){
        return prop;
    }
}
