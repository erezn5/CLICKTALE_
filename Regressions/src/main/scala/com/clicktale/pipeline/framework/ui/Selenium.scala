package com.clicktale.pipeline.framework.ui

import java.lang.System
import java.{lang, util}
import java.net.URL

import com.clicktale.pipeline.framework.dal.ConfigParser.conf
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.remote.{DesiredCapabilities, RemoteWebDriver}

object Selenium {
  val capabilities: DesiredCapabilities = DesiredCapabilities.chrome()
  //capabilities.setCapability("chrome.switches", "--ignore-certificate-errors --allow-failed-policy-fetch-for-test --allow-http-background-page --allow-insecure-localhost --allow-running-insecure-content ")
  capabilities.setCapability("chrome.switches", util.Arrays.asList("--ignore-certificate-errors"))
  System.setProperty(conf.getString("WebRecorder.Selenium.StandAlone.Driver"), System.getProperty("user.dir")+conf.getString("WebRecorder.Selenium.StandAlone.RelativeDriverPath"))


//*************************************************************
//Change only this section when switching between drivers!!!***
//*************************************************************
  //val driverType = "Grid"
  val driverType = "StandAlone"
//*************************************************************

  if(driverType=="Grid") {
    capabilities.setCapability("applicationName", "main")
  }

  var driver: RemoteWebDriver = if(driverType=="StandAlone") new ChromeDriver() else new RemoteWebDriver(new URL(conf.getString("WebRecorder.Selenium.Grid.Url")), capabilities)

  def ResetSessions(): Unit = {
    driver.quit()
    driver = if(driverType=="StandAlone") new ChromeDriver() else new RemoteWebDriver(new URL(conf.getString("WebRecorder.Selenium.Grid.Url")), capabilities)
  }
}

object GeneralFunctions {
  def GetTitle: String = {
    Selenium.driver.getTitle
  }
}