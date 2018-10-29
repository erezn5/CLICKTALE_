package com.clicktale.pipeline.framework.ui.BackOffice

import com.clicktale.pipeline.framework.dal.ConfigParser.conf
import com.clicktale.pipeline.framework.ui.Selenium._
import org.openqa.selenium.By

object Home {

  def GoTo(): Unit = {
    driver.navigate().to(conf.getString(s"WebRecorder.BackOffice.${conf.getString("WebRecorder.Current.Environment")}.Address"))
  }

  def GetUserDisplayName: String = {
    driver.findElementByCssSelector("ul.navbar-right li.navbar-text").getText.split(' ')(1).split('!')(0).trim
  }

  def Logout(): Unit = {
    driver.findElementByCssSelector("ul.navbar-right li a").click()
    assert(Login.GetSignoutMessage == "You have successfully signed out.")
  }

  def ClickMenuItem (text : String): Unit = {
    driver.findElement(By.linkText(text)).click()
  }
}



