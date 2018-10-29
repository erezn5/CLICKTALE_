package com.clicktale.pipeline.framework.ui.BackOffice

import java.lang.Exception

import com.clicktale.pipeline.framework.dal.CoreRecordingsSqlManager
import com.clicktale.pipeline.framework.ui.Selenium._
import org.openqa.selenium.support.ui.Select
import org.openqa.selenium.{By, WebElement}

import scala.util.control.Exception


object CreateEnhancedRule {

  def SetUrl (url : String = "http://www.clicktale.com"): String = {
    try {
      driver.findElementById("datepicker").clear()
      driver.findElementById("datepicker").sendKeys(url)
    }
    catch {
      case unknown: Throwable => {
        driver.findElementById("Uri").clear()
        driver.findElementById("Uri").sendKeys(url)
      }
    }
    url
  }

  def SetRule (name:String): Unit = {
    val dropdown = new Select(driver.findElementById("ProjectCreditRuleType"))
    dropdown.selectByVisibleText(name)
  }

  def SetIgnore (name:String): Unit = {
    val dropdown = new Select(driver.findElementById("IsIgnore"))
    dropdown.selectByVisibleText(name)
  }

  def SetRatio (ratio:Double = 0.5): Double = {
    driver.findElementById("Ratio").clear()
    driver.findElementById("Ratio").sendKeys(ratio.toString)
    ratio
  }

  def ClickAdd : String = {
    driver.findElementByCssSelector("button.btn-primary").click()
    assert(Projects.GetSuccessLine.contains("Successfully"))
    Projects.GetSuccessLine
  }

  def ClickEditRule (id:String): Unit = {
    driver.findElementByXPath(s"//td[contains(text(), '$id')]" ).findElement(By.xpath("../..")).findElements(By.cssSelector("td a")).get(1).click()
  }

  def SetEndDate : Unit = {
    driver.findElementByName("EndDate").sendKeys("12/12/3020")
  }

  def CheckAllTables(subsId:String, pid:String): Unit = {
    val enhancedCreditRules:java.util.List[WebElement] = driver.findElementByXPath("//h4[contains(text(), 'Enhanced Credit Rules')]" ).findElement(By.xpath("../..")).findElements(By.cssSelector("tbody tr"))
    val dbEnhancedCreditRules = CoreRecordingsSqlManager.Select(s"select * from CoreAdministration.dbo.UrlRules where ProjectId = $pid and SubscriberId = $subsId order by 1 desc")

    var i=0
    while(dbEnhancedCreditRules.next()){
      assert(dbEnhancedCreditRules.getInt("RuleId")==enhancedCreditRules.get(i).findElements(By.cssSelector("td")).get(2).getText.toInt)
      assert(dbEnhancedCreditRules.getString("Uri")==enhancedCreditRules.get(i).findElements(By.cssSelector("td")).get(5).getText)
      assert(dbEnhancedCreditRules.getString("Ratio")==enhancedCreditRules.get(i).findElements(By.cssSelector("td")).get(6).getText)
      i+=1
    }
    assert(enhancedCreditRules.size()==i)

  }

}



