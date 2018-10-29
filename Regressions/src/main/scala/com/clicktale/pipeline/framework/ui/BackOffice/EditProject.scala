package com.clicktale.pipeline.framework.ui.BackOffice

import com.clicktale.pipeline.framework.dal.ConfigParser.conf
import com.clicktale.pipeline.framework.ui.Selenium._
import org.joda.time.DateTime
import org.openqa.selenium.support.ui.Select


object EditProject {

  def SetID (id : String): Unit = {
    driver.findElementById("ProjectId").clear()
    driver.findElementById("ProjectId").sendKeys(id)
  }

  def SetID : String = {
    val r = scala.util.Random
    val random = r.nextInt(10000).toString +DateTime.now.toString("dd")
    driver.findElementById("ProjectId").clear()
    driver.findElementById("ProjectId").sendKeys(random)
    random
  }

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

  def SetRatio (ratio : Double = 0.7): Double = {
    driver.findElementById("RecordingRatio").clear()
    driver.findElementById("RecordingRatio").sendKeys(ratio.toString)
    ratio
  }

  def SetPartition (par : Int = 33): Int = {
    driver.findElementById("QueueId").clear()
    driver.findElementById("QueueId").sendKeys(par.toString)
    par
  }

  def SetTTL (ttl : Int = 100): Int = {
    driver.findElementById("TimeToLive").clear()
    driver.findElementById("TimeToLive").sendKeys(ttl.toString)
    ttl
  }

  def ClickCreate : String = {
    driver.findElementByCssSelector("input.btn-primary").click()
    assert(Projects.GetSuccessLine.contains("Project successfully created"))
    Projects.GetSuccessLine
  }

  def ClickCreateNoAssert : Boolean = {
    driver.findElementByCssSelector("input.btn-primary").click()
    try {
      Projects.GetSuccessLine.contains("Project successfully created")
    } catch {
      case unknown: Throwable => return false
    }
    true
  }

  def SetProjectType (projectType : Int): Unit = {
    val dropdown = new Select(driver.findElementById("ProcessingTypeId"))
    dropdown.selectByValue(projectType.toString)
  }

}



