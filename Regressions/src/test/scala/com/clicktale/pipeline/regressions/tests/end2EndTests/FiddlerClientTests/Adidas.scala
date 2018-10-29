package com.clicktale.pipeline.regressions.tests.end2EndTests.FiddlerClientTests

import com.clicktale.pipeline.framework.dal.ConfigParser.conf
import com.clicktale.pipeline.framework.helpers.{CsvReader, StringManipulator}
import com.clicktale.pipeline.framework.storage.AzureBlob
import com.clicktale.pipeline.framework.ui.Selenium._
import com.clicktale.pipeline.regressions.testHelpers.WhichTestsToRun
import com.microsoft.azure.storage.blob.CloudBlobDirectory
import org.openqa.selenium.{By, WebElement}
import org.scalatest.WordSpecLike

class Adidas extends WordSpecLike with StringManipulator {

  final val projectId: String = "452";
  final val subsId: String = "233197"
  final val testProjectId: String = "2";
  final val testSubsId: String = "69313"
  final val clientUrl = "http://www.adidas.es"

  "Basic Adidas Assets Manager Flow" in {
    if (!WhichTestsToRun().shouldRunTest)
      AzureBlob.DeleteBlob("storage", testProjectId)
    val uid = PerformBasicSession(clientUrl)
    PerformPlayback(uid)
    findBlobsInPlayerHtml("head link", s"https://ctsacss01.blob.core.windows.net/storage/$testProjectId/CSS/http/www.adidas.es", "pre", "Adidas")
    var blobs = AzureBlob.ListBlobs("storage")
    var flag = false
    while (blobs.hasNext()) {
      val value = blobs.next().asInstanceOf[CloudBlobDirectory].getPrefix()
      if (value.equals("2/"))
        flag = true
    }
    assert(flag)
    //    AzureBlob.DeleteBlob("storage",projectId)
  }

  def PerformBasicSession(url: String): String = {
    driver.navigate().to(url)
    Thread.sleep(10000)
    driver.executeScript("ClickTaleStop();")
    Thread.sleep(60000)
    return driver.executeScript("return ClickTaleGetUID();").toString
  }

  def PerformPlayback(uid: String) = {
    driver.navigate().to("https://loginst.app.clicktale.com/Login.aspx")
    driver.findElementById("ctl00_MainContentPlaceHolder_UserName").sendKeys("nir.harel@clicktale.com")
    driver.findElementById("Password").sendKeys("Auto12345")
    driver.findElementById("ctl00_MainContentPlaceHolder_LoginButton").click()
    driver.navigate().to(driver.getCurrentUrl().split('?')(0) + s"Search_2.aspx?PID=$testProjectId&traffictype=Desktop")
    Thread.sleep(2000)
    driver.findElementByLinkText("Visitor IDs").click()
    Thread.sleep(5000)
    driver.findElementByName("frm_yui-gen16").findElement(By.cssSelector("textarea")).sendKeys(uid)
    driver.findElementById("yui-gen17-button").click()
    Thread.sleep(2000)
    driver.findElementById("search-do-search-button").click()
    Thread.sleep(5000)
    driver.navigate().to(driver.findElementById(s"play-user-Desktop-$testProjectId-$uid").getAttribute("href"))
    Thread.sleep(5000)
  }

  def findBlobsInPlayerHtml(blobSelector: String, blobSampleLink: String, blobAssetContentSelector: String, blobAssetContent: String) = {
    driver.switchTo().frame(driver.findElementByCssSelector(".PageDisplay"))
    var flag = false
    var link = ""
    val elements: java.util.List[WebElement] = driver.findElementsByCssSelector(blobSelector)
    for (i <- 0 to elements.size() - 1) {
      if (elements.get(i).getAttribute("href").contains(blobSampleLink)) {
        flag = true
        link = elements.get(i).getAttribute("href")
        scala.util.control.Breaks.break()
      }
      assert(flag)
      driver.navigate().to(link)
      assert(driver.findElementByCssSelector(blobAssetContentSelector).getText.contains(blobAssetContent), "Blob css was not found!")
    }
  }
}
