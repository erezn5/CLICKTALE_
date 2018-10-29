package com.clicktale.pipeline.framework.ui.BackOffice

import com.clicktale.pipeline.framework.dal.ConfigParser.conf
import com.clicktale.pipeline.framework.ui.Selenium._
import org.joda.time.DateTime
import org.openqa.selenium.By

import scala.util.{Failure, Success, Try}

object CreateSubscriber {

  def SetID (id : String): Unit = {
    driver.findElementById("Id").clear()
    driver.findElementById("Id").sendKeys(id)
  }

  def SetID : String = {
    val r = scala.util.Random
    val random = r.nextInt(10000).toString +DateTime.now.toString("dd")
    driver.findElementById("Id").clear()
    driver.findElementById("Id").sendKeys(random)
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

  def ClickCreate : String = {
    driver.findElement(By.xpath("//button[text() = 'Create']")).click()
    assert(Subscribers.GetSuccessLine.contains("Subscriber successfully created"))
    Subscribers.GetSuccessLine
  }

  def ClickCreateNoAssert : Boolean = {
    driver.findElement(By.xpath("//button[text() = 'Create']")).click()

    Try(Subscribers.GetSuccessLine.contains("Subscriber successfully created")) match{
      case Success(x) => true
      case Failure(x) => false
    }
  }

}



