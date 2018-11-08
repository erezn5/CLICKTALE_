package com.keywee.shorten;

import org.openqa.selenium.WebDriver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Properties;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

public class TestClassForService {

    private Properties prop;
    private WebDriver driver;
    private StringBuffer verificationErrors = new StringBuffer();
    private ArrayList<String> tabs2;
    private final Logger logger = LoggerFactory.getLogger(TestClassForService.class);
    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {

        FactoryHelper factoryHelper = new FactoryHelper();
        prop = factoryHelper.getPropFile();
    }

    @Test
    public void testShortExapndsToOriginalLink() throws Exception {

        String longURL = prop.getProperty("longUrl");
        String mode = prop.getProperty("MODE_UI");
        ConnectAndBrowse connectAndBrowse = new ConnectAndBrowse(longURL, mode);
        driver = connectAndBrowse.browseToUrlWithShortLink();
        tabs2 = new ArrayList<String>(driver.getWindowHandles());
//        String shorthenUrl = driver.getCurrentUrl();
        driver.switchTo().window(tabs2.get(1));
        String url = driver.getCurrentUrl();

        assertTrue(url.equals(longURL));
    }

    @Test
    public void testUpTo8AlphaNumericCharacters() throws Exception {
        int maxLength = Integer.parseInt(prop.getProperty("MAX_LENGTH"));
        String shortUrl = gettingShortLink();
        String trimmedShortUrl = shortUrl.split("com/")[1];
        if(trimmedShortUrl.length()<=maxLength){
            assertTrue(true);
        }else{
            assertTrue(false);
        }

    }

    private String gettingShortLink() throws Exception {
        String defaultUrl = prop.getProperty("defaultUrl");
        String mode = prop.getProperty("MODE_UI");
        ConnectAndBrowse connectAndBrowse = new ConnectAndBrowse(defaultUrl, mode);
        driver = connectAndBrowse.browseToUrlWithShortLink();
        String shortUrl = connectAndBrowse.returnShortLink(driver);
        return shortUrl;
    }

    @Test
    public void sameLinkWillProduceTheSameShortLink() throws Exception {
        String firstShortLink = gettingShortLink();
        System.out.println(firstShortLink);

        String secondShortLink = gettingShortLink();
        System.out.println(secondShortLink);
        assertTrue(firstShortLink.equals(secondShortLink));

    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        driver.quit();
        String verificationErrorString = verificationErrors.toString();
        if (!"".equals(verificationErrorString)) {
            fail(verificationErrorString);
        }
    }
}
