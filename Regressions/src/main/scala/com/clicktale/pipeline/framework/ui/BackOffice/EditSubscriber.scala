package com.clicktale.pipeline.framework.ui.BackOffice

import com.clicktale.pipeline.framework.dal
import com.clicktale.pipeline.framework.dal.ConfigParser.conf
import com.clicktale.pipeline.framework.ui.Selenium._
import org.openqa.selenium.By

object EditSubscriber {

  def SetName : String = {
    val r = scala.util.Random
    driver.findElementById("Name").clear()
    val random = conf.getString("WebRecorder.TestParams.BackOffice.Subscriber.Name") + " " + r.nextInt(10000).toString
    driver.findElementById("Name").sendKeys(random)
    random
  }

  def SetName (name : String): String = {
    driver.findElementById("Name").clear()
    driver.findElementById("Name").sendKeys(name)
    name
  }

  def SetQuota : String = {
    val r = scala.util.Random
    driver.findElementById("Quota").clear()
    val random = r.nextInt(10000).toString
    driver.findElementById("Quota").sendKeys(random)
    random
  }

  def SetQuota (quota : String): String = {
    driver.findElementById("Quota").clear()
    driver.findElementById("Quota").sendKeys(quota)
    quota
  }

  def ClickSave() : Unit = {
    driver.findElement(By.xpath("//button[text() = 'Save']")).click()
//    assert(Subscribers.GetSuccessLine.contains("Subscriber successfully created"))
//    Subscribers.GetSuccessLine
  }

  def CheckTableValuesById (id : String, name : String, quota : String): Unit = {
    val res = dal.GeneralFunctions.getSubscriberById(id)
    assert(name == res.Name)
    assert(quota == res.TotalQuota.toString)
  }

}