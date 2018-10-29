package com.clicktale.pipeline.framework.ui.BackOffice

import com.clicktale.pipeline.framework.dal.CoreRecordingsSqlManager
import com.clicktale.pipeline.framework.ui.Selenium._
import org.openqa.selenium.support.ui.Select
import org.openqa.selenium.{By, WebElement}

object ProjectCredits {

  def SetBucketType (name : String): Unit = {
    val dropdown = new Select(driver.findElementById("ProjectCreditType"))
    dropdown.selectByVisibleText(name)
  }

  def SetType (name : String): Unit = {
    val dropdown = new Select(driver.findElementById("ProjectCreditRuleType"))
    dropdown.selectByVisibleText(name)
  }

  def SetCredits (num:Int = 1000): Unit = {
    driver.findElementById("AddCreditsAmount").clear()
    driver.findElementById("AddCreditsAmount").sendKeys(num.toString)
  }

  def SetDailyCTSDate (date : String): AnyRef = {
    driver.executeScript(s"document.getElementById('datepicker').setAttribute('value', '$date')")
  }

  def SetDailyCTSTime (time : String): Unit = {
    val dropdown = new Select(driver.findElementById("AddCreditsStartDateHours"))
    dropdown.selectByVisibleText(time)
  }

  def SetRepeatEvery (num : Int): Unit = {
    driver.findElementById("AddCreditsInterval").clear()
    driver.findElementById("AddCreditsInterval").sendKeys(num.toString)
  }

  def getId : Int = {
    driver.findElementById("RuleId").getText.toInt
  }

  def ClickAdd : String= {
    driver.findElementByClassName("btn-primary").click()
    assert(ProjectCredits.GetSuccessLine.contains("Successfully added"))
    ProjectCredits.GetSuccessLine
  }

  def ClickAddNoAssertion : String= {
    driver.findElementByClassName("btn-primary").click()
    ProjectCredits.GetSuccessLine
  }

  def GetSuccessLine: String = {
    driver.findElementByCssSelector("div.alert-success").getText
  }

  def EditCredits(ruleId:String): Unit = {
    driver.findElementByXPath(s"//td[contains(text(), '$ruleId')]" ).findElement(By.xpath("../..")).findElements(By.cssSelector("td a")).get(1).click()
  }

  def CheckAllTables(subsId:String, pid:String): Unit = {
    val randomCreditValues:java.util.List[WebElement] = driver.findElementByXPath("//h4[contains(text(), 'Random Credit Rules')]" ).findElement(By.xpath("../..")).findElements(By.cssSelector("tbody tr"))
    val ETRCreditValues:java.util.List[WebElement] = driver.findElementByXPath("//h4[contains(text(), 'EventTrigger Credit Rules')]" ).findElement(By.xpath("../..")).findElements(By.cssSelector("tbody tr"))
    val enhancedCreditValues:java.util.List[WebElement] = driver.findElementByXPath("//h4[contains(text(), 'Enhanced Credit Rules')]" ).findElement(By.xpath("../..")).findElements(By.cssSelector("tbody tr"))
    val disabledRandomCreditValues:java.util.List[WebElement] = driver.findElementByXPath("//h4[contains(text(), 'Disabled Random Credit Rules')]" ).findElement(By.xpath("../..")).findElements(By.cssSelector("tbody tr"))
    val disabledETRCreditValues:java.util.List[WebElement] = driver.findElementByXPath("//h4[contains(text(), 'Disabled EventTrigger Credit Rules')]" ).findElement(By.xpath("../..")).findElements(By.cssSelector("tbody tr"))
    val disabledEnhancedCreditValues:java.util.List[WebElement] = driver.findElementByXPath("//h4[contains(text(), 'Disabled Enhanced Credit Rules')]" ).findElement(By.xpath("../..")).findElements(By.cssSelector("tbody tr"))

    val dbRandomCreditValues = CoreRecordingsSqlManager.Select(s"select * from CoreRecordings.dbo.ProjectCreditRules where ProjectId = $pid and SubscriberId = $subsId and IsEnabled = 1 and RandomCreditValue IS NOT NULL order by 1 desc")
    val dbETRCreditValues = CoreRecordingsSqlManager.Select(s"select * from CoreRecordings.dbo.ProjectCreditRules where ProjectId = $pid and SubscriberId = $subsId and IsEnabled = 1 and EventTriggerCreditValue IS NOT NULL order by 1 desc")
    val dbEnhancedCreditValues = CoreRecordingsSqlManager.Select(s"select * from CoreRecordings.dbo.ProjectCreditRules where ProjectId = $pid and SubscriberId = $subsId and IsEnabled = 1 and EnhancedCreditValue IS NOT NULL order by 1 desc")
    val dbDisabledRandomCreditValues = CoreRecordingsSqlManager.Select(s"select * from CoreRecordings.dbo.ProjectCreditRules where ProjectId = $pid and SubscriberId = $subsId and IsEnabled = 0 and RandomCreditValue IS NOT NULL order by 1 desc")
    val dbDisabledETRCreditValues = CoreRecordingsSqlManager.Select(s"select * from CoreRecordings.dbo.ProjectCreditRules where ProjectId = $pid and SubscriberId = $subsId and IsEnabled = 0 and EventTriggerCreditValue IS NOT NULL order by 1 desc")
    val dbDisabledEnhancedCreditValues = CoreRecordingsSqlManager.Select(s"select * from CoreRecordings.dbo.ProjectCreditRules where ProjectId = $pid and SubscriberId = $subsId and IsEnabled = 0 and EnhancedCreditValue IS NOT NULL order by 1 desc")

    var i=0
    while(dbRandomCreditValues.next()){
      assert(dbRandomCreditValues.getInt("ProjectCreditRuleId")==randomCreditValues.get(i).findElements(By.cssSelector("td")).get(2).getText.toInt)
      assert(dbRandomCreditValues.getInt("IntervalInHours")==randomCreditValues.get(i).findElements(By.cssSelector("td")).get(7).getText.toInt*24)
      assert(dbRandomCreditValues.getInt("RandomCreditValue")==randomCreditValues.get(i).findElements(By.cssSelector("td")).get(3).getText.replace(",","").toInt)
      i+=1
    }
    assert(randomCreditValues.size()==i)

    i=0
    while(dbETRCreditValues.next()){
      assert(dbETRCreditValues.getInt("ProjectCreditRuleId")==ETRCreditValues.get(i).findElements(By.cssSelector("td")).get(2).getText().toInt)
      assert(dbETRCreditValues.getInt("IntervalInHours")==ETRCreditValues.get(i).findElements(By.cssSelector("td")).get(7).getText().toInt*24)
      assert(dbETRCreditValues.getInt("EventTriggerCreditValue")==ETRCreditValues.get(i).findElements(By.cssSelector("td")).get(3).getText().replace(",","").toInt)
      i+=1
    }
    assert(ETRCreditValues.size()==i)

    i=0
    while(dbEnhancedCreditValues.next()){
      assert(dbEnhancedCreditValues.getInt("ProjectCreditRuleId")==enhancedCreditValues.get(i).findElements(By.cssSelector("td")).get(2).getText().toInt)
      assert(dbEnhancedCreditValues.getInt("IntervalInHours")==enhancedCreditValues.get(i).findElements(By.cssSelector("td")).get(7).getText().toInt*24)
      assert(dbEnhancedCreditValues.getInt("EnhancedCreditValue")==enhancedCreditValues.get(i).findElements(By.cssSelector("td")).get(3).getText().replace(",","").toInt)
      i+=1
    }
    assert(enhancedCreditValues.size()==i)

    i=0
    while(dbDisabledRandomCreditValues.next()){
      assert(dbDisabledRandomCreditValues.getInt("ProjectCreditRuleId")==disabledRandomCreditValues.get(i).findElements(By.cssSelector("td")).get(0).getText().toInt)
      assert(dbDisabledRandomCreditValues.getInt("IntervalInHours")==disabledRandomCreditValues.get(i).findElements(By.cssSelector("td")).get(5).getText().toInt*24)
      assert(dbDisabledRandomCreditValues.getInt("RandomCreditValue")==disabledRandomCreditValues.get(i).findElements(By.cssSelector("td")).get(1).getText().replace(",","").toInt)
      i+=1
    }
    assert(disabledRandomCreditValues.size()==i)

    i=0
    while(dbDisabledETRCreditValues.next()){
      assert(dbDisabledETRCreditValues.getInt("ProjectCreditRuleId")==disabledETRCreditValues.get(i).findElements(By.cssSelector("td")).get(0).getText.toInt)
      assert(dbDisabledETRCreditValues.getInt("IntervalInHours")==disabledETRCreditValues.get(i).findElements(By.cssSelector("td")).get(5).getText().toInt*24)
      assert(dbDisabledETRCreditValues.getInt("EventTriggerCreditValue")==disabledETRCreditValues.get(i).findElements(By.cssSelector("td")).get(1).getText().replace(",","").toInt)
      i+=1
    }
    assert(disabledETRCreditValues.size()==i)

    i=0
    while(dbDisabledEnhancedCreditValues.next()){
      assert(dbDisabledEnhancedCreditValues.getInt("ProjectCreditRuleId")==disabledEnhancedCreditValues.get(i).findElements(By.cssSelector("td")).get(0).getText().toInt)
      assert(dbDisabledEnhancedCreditValues.getInt("IntervalInHours")==disabledEnhancedCreditValues.get(i).findElements(By.cssSelector("td")).get(5).getText().toInt*24)
      assert(dbDisabledEnhancedCreditValues.getInt("RandomCreditValue")==disabledEnhancedCreditValues.get(i).findElements(By.cssSelector("td")).get(1).getText().replace(",","").toInt)
      i+=1
    }
    assert(disabledEnhancedCreditValues.size()==i)
  }

}



