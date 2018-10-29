package com.clicktale.pipeline.framework.ui.BackOffice

import com.clicktale.pipeline.framework.dal.ConfigParser.conf
import com.clicktale.pipeline.framework.ui.Selenium._

object Login {
  def login(user: String): Unit = {
    try{
      driver.findElementById("userNameInput").sendKeys(conf.getString(s"WebRecorder.BackOffice.${conf.getString("WebRecorder.Current.Environment")}.Users.${user}.User"))
      driver.findElementById("passwordInput").sendKeys(conf.getString(s"WebRecorder.BackOffice.${conf.getString("WebRecorder.Current.Environment")}.Users.${user}.Password"))
      driver.findElementById("submitButton").click()
      Thread.sleep(1000)
    }
    catch {
      case unknown: Throwable =>
        ResetSessions
        Home.GoTo
        driver.findElementById("userNameInput").sendKeys(conf.getString(s"WebRecorder.BackOffice.${conf.getString("WebRecorder.Current.Environment")}.Users.${user}.User"))
        driver.findElementById("passwordInput").sendKeys(conf.getString(s"WebRecorder.BackOffice.${conf.getString("WebRecorder.Current.Environment")}.Users.${user}.Password"))
        driver.findElementById("submitButton").click()
        Thread.sleep(1000)
    }
    assert(Home.GetUserDisplayName == conf.getString(s"WebRecorder.BackOffice.${conf.getString("WebRecorder.Current.Environment")}.Users.${user}.DisplayName"))
  }

  def GetSignoutMessage: String = {
    driver.findElementById("instruction").getText.trim
  }
}



