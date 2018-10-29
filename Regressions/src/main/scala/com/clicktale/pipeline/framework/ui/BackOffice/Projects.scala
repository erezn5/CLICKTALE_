package com.clicktale.pipeline.framework.ui.BackOffice
import com.clicktale.pipeline.framework._


import com.clicktale.pipeline.framework.dal.CreditsSqlManager
import com.clicktale.pipeline.framework.ui.Selenium._
import org.openqa.selenium.By

object Projects {

  def ClickSearch() : Unit = {
    driver.findElement(By.xpath("//button[text() = 'Search']")).click()
  }

  def ClickDelete (pid:String): Unit = {
    driver.findElementByCssSelector("div.panel table.table tbody").findElement(By.xpath(s"//td[contains(text(), '$pid')]" )).findElement(By.xpath("..")).findElements(By.cssSelector("td a")).get(0).click()
  }

  def ClickEdit() : Unit = {
    driver.findElementsByCssSelector("div.panel table.table tbody tr td a").get(1).click()
  }

  def ClickEnhancedRules() : Unit = {
    driver.findElementsByCssSelector("div.panel table.table tbody tr td a").get(2).click()
  }

  def ClickCredits() : Unit = {
    driver.findElementsByCssSelector("div.panel table.table tbody tr td a").get(3).click()
  }

  def ClickBoost() : Unit = {
    driver.findElementsByCssSelector("div.panel table.table tbody tr td a").get(4).click()
  }

  def ClickCreditsState() : Unit = {
    driver.findElementsByCssSelector("div.panel table.table tbody tr td a").get(5).click()
  }

  def GetSuccessLine : String = {
    driver.findElementByCssSelector("div.alert-success").getText
  }

  def SetSubsID (id : String = ""): Unit = {
    driver.findElementById("Id").clear()
    driver.findElementById("Id").sendKeys(id)
  }

  def SetPID (id : String = ""): Unit = {
    driver.findElementById("ProjectId").clear()
    driver.findElementById("ProjectId").sendKeys(id)
  }

  def ProjectId (name : String = ""): Unit = {
    driver.findElementById("ProjectId").clear()
    driver.findElementById("ProjectId").sendKeys(name)
  }

  def CheckTableValuesByProjectId (projectId : String, count:Int = 1): Unit = {
    assert(driver.findElementsByCssSelector("div.panel table.table tbody tr").size() == count)
    for( i <- 0 until count)
      {
        val row = driver.findElementsByCssSelector("div.panel table.table tbody tr").get(i)
        val res = dal.GeneralFunctions.getProjectByIdAndSubsId(row.findElements(By.cssSelector("td")).get(6).getText.toInt,row.findElements(By.cssSelector("td")).get(7).getText.toInt)
        assert(row.findElements(By.cssSelector("td")).get(8).getText == res.Name)
        assert(row.findElements(By.cssSelector("td")).get(9).getText == res.QueueId.toString)
        assert(row.findElements(By.cssSelector("td")).get(14).getText == res.Ratio.toString)
        assert(row.findElements(By.cssSelector("td")).get(10).getText == CreditsSqlManager.getCreditsForSpecificPid(projectId.toInt).toString)
      }
  }

}



