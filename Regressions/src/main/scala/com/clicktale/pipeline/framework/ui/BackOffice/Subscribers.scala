package com.clicktale.pipeline.framework.ui.BackOffice

import com.clicktale.pipeline.framework.ui.Selenium._
import com.clicktale.pipeline.framework._
import org.openqa.selenium.By

object Subscribers {

  def ClickSearch() : Unit = {
    driver.findElement(By.xpath("//button[text() = 'Search']")).click()
  }

  def ClickEdit() : Unit = {
    driver.findElementsByCssSelector("div.panel table.table tbody tr td a").get(0).click()
  }

  def ClickProjects() : Unit = {
    driver.findElementsByCssSelector("div.panel table.table tbody tr td a").get(1).click()
  }

  def ClickNewProject() : Unit = {
    driver.findElementsByCssSelector("div.panel table.table tbody tr td a").get(2).click()
  }

  def GetSuccessLine : String = {
    Thread.sleep(1000)
    driver.findElementByCssSelector("div.alert-success").getText
  }

  def SetID (id : String = ""): Unit = {
    driver.findElementById("Id").clear()
    driver.findElementById("Id").sendKeys(id)
  }

  def SetName (name : String = ""): Unit = {
    driver.findElementById("Name").clear()
    driver.findElementById("Name").sendKeys(name)
  }

  def CheckTableValueById (id : String): Unit = {
    val res = dal.GeneralFunctions.getSubscriberById(id)
    assert(driver.findElementsByCssSelector("div.panel table.table tbody tr").size()==1)
    assert(driver.findElementsByCssSelector("div.panel table.table tbody tr td").get(3).getText==res.Id)
    assert(driver.findElementsByCssSelector("div.panel table.table tbody tr td").get(4).getText==res.Name)
  }

  def CheckTableValueByName (name : String): Unit = {
    val res = dal.GeneralFunctions.getSubscribersByName(name)
    var i=0
    while (i<res.length){
      assert(driver.findElementsByCssSelector("div.panel table.table tbody tr").get(i).findElements(By.cssSelector("td")).get(3).getText==res(i).Id)
      assert(driver.findElementsByCssSelector("div.panel table.table tbody tr").get(i).findElements(By.cssSelector("td")).get(4).getText==res(i).Name)
      i = i+1
    }
    assert(i==driver.findElementsByCssSelector("div.panel table.table tbody tr").size())
  }

}



