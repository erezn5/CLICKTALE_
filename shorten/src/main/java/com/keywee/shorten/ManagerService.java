package com.keywee.shorten;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.openqa.selenium.WebDriver;

import java.io.IOException;
import java.util.Properties;

public class ManagerService {

    public static void main(String[] args) {
        try {
            FactoryHelper factoryHelper = new FactoryHelper();
            Properties prop = factoryHelper.getPropFile();
            String toShorten = prop.getProperty("defaultUrl");
            String mode = prop.getProperty("MODE_UI");
            ConnectAndBrowse connectAndBrowse = new ConnectAndBrowse(toShorten, mode);
            WebDriver driver=connectAndBrowse.browseToUrlWithShortLink();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
