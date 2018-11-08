package com.keywee.shorten;

import org.openqa.selenium.By;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/*************************************************************************/
/**params:
 * m_toShortenURL - url string we need to shorten
 * m_baseUrl - url which we use in order to use service to shorten links
 * driver - webdriver for using selenium*/
/*************************************************************************/

public class ConnectAndBrowse {
    WebDriver driver;
    private String m_baseUrl = "https://tinyurl.com/";
    private String m_toShortenURL;
    private ArrayList<String> tabs2;
    private final Logger logger = LoggerFactory.getLogger(ConnectAndBrowse.class);
    public ConnectAndBrowse( String i_toShortenURL, String mode ) throws MalformedURLException {
        setUp(i_toShortenURL, mode);
    }
    public String getOperatingSystem() {
        String os = System.getProperty("os.name");
        return os;
    }
    private void setUp(String i_toShortenURL, String mode) throws MalformedURLException {

        String os = getOperatingSystem();
        logger.info("System OS is: " + os);
        logger.info("Setting the correct os WebDriver driver...");
        if(mode.equals("false"))
            driver=returnSuitableDriver(os);
        else
            driver=returnSuitableUIDriver(os);
        m_toShortenURL = i_toShortenURL;
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
    }

    private WebDriver returnSuitableUIDriver(String os) {
        if(os.contains("Windows"))
            System.setProperty("webdriver.chrome.driver","./src/main/resources/drivers/chromedriver.exe");
        else
            System.setProperty("webdriver.chrome.driver","./src/main/resources/drivers/chromedriver");
        driver = new ChromeDriver();
        return driver;
    }

    private WebDriver returnSuitableDriver(String os) {

        DesiredCapabilities caps = new DesiredCapabilities();
        caps.setJavascriptEnabled(true);
        caps.setCapability("takesScreenshot", true);
        //caps.setCapability(PhantomJSDriverService.PHANTOMJS_CLI_ARGS,new String[] {"–web-security=no", "–ignore-ssl-errors=yes"});
        if(os.contains("Windows")){
            caps.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY,"./src/main/resources/drivers/phantomjs.exe");
//            System.setProperty("phantomjs.binary.path", "./src/main/resources/drivers/phantomjs.exe");
        }else{
            caps.setCapability(
                    PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY,
                    "./src/main/resources/drivers/phantomjs");
        }
        driver = new PhantomJSDriver(caps);
        logger.info("Current OS is: " + os);
        return driver;
    }

    public WebDriver browseToUrlWithShortLink() throws Exception {
        logger.info("Getting the base url for SUT");
        driver.get(m_baseUrl);
        driver.findElement(By.id("url")).click();
        driver.findElement(By.id("url")).clear();
        logger.info("Going to shorten the long link: " + m_toShortenURL);
        driver.findElement(By.id("url")).sendKeys(m_toShortenURL);
        driver.findElement(By.id("submit")).click();
        driver.findElement(By.linkText("Open in new window")).click();
//        String shortstr = returnShortLink(driver);
//        System.out.println(shortstr);
        logger.info("Returning driver from " + getClass().getName());
        return driver;
    }

    public String returnShortLink(WebDriver driver) {

        String data = driver.findElement(By.xpath("//*[@id=\"contentcontainer\"]/div[2]/b")).getText();
        return data;
    }


}
