package com.clicktale.pipeline.regressions.tests.end2EndTests
import com.clicktale.pipeline.regression.tools.PushSession_AM_

import com.clicktale.pipeline.framework.dal.ConfigParser.conf
import com.clicktale.pipeline.framework.helpers.StringManipulator
import com.clicktale.pipeline.framework.storage.AzureBlob
import com.clicktale.pipeline.framework.ui.Selenium._
import com.clicktale.pipeline.regressions.testHelpers.WhichTestsToRun
import com.microsoft.azure.storage.blob.CloudBlobDirectory
import org.openqa.selenium.By
import org.scalatest.WordSpecLike

class AssetsManagerBasicTests extends WordSpecLike with StringManipulator {

  final val projectId: String = conf.getString("WebRecorder.Environments.AwsCentral.AssetsManagerPid")
  final val subsId: String = conf.getString("WebRecorder.Environments.AwsCentral.AssetsManagerSubsId")
  final val testSiteUrlPrefix: String = conf.getString("WebRecorder.Environments.AwsCentral.TestSiteUrl")

  var UID = ""


  "Basic Assets Manager Flow" in {
    if (!WhichTestsToRun().shouldRunTest)
    AzureBlob.DeleteBlob("storage",projectId)
    PerformBasicSession
    PerformPlayback
    findBlobsInPlayerHtml
    var blobs = AzureBlob.ListBlobs("storage")
    var flag = false
    while(blobs.hasNext()){
      val value = blobs.next().asInstanceOf[CloudBlobDirectory].getPrefix()
      if (value.equals("6/"))
        flag=true
    }
    assert(flag)
//    AzureBlob.DeleteBlob("storage",projectId)
  }

  def PerformBasicSession(): Unit = {
    driver.navigate().to(testSiteUrlPrefix+"?ct_configName=assetsmanager")
    Thread.sleep(10000)
    driver.executeScript("ClickTaleStop();")
    UID = driver.executeScript("return ClickTaleGetUID();").toString
    Thread.sleep(60000)
  }

  def PerformPlayback = {
    driver.navigate().to("https://loginst.app.clicktale.com/Login.aspx")
    driver.findElementById("ctl00_MainContentPlaceHolder_UserName").sendKeys("nir.harel@clicktale.com")
    driver.findElementById("Password").sendKeys("Auto12345")
    driver.findElementById("ctl00_MainContentPlaceHolder_LoginButton").click()
    driver.navigate().to(driver.getCurrentUrl().split('?')(0)+"Search_2.aspx?PID=6&traffictype=Desktop")
    Thread.sleep(2000)
    driver.findElementByLinkText("Visitor IDs").click()
    Thread.sleep(5000)
    driver.findElementByName("frm_yui-gen16").findElement(By.cssSelector("textarea")).sendKeys(UID)
    driver.findElementById("yui-gen17-button").click()
    Thread.sleep(2000)
    driver.findElementById("search-do-search-button").click()
    Thread.sleep(5000)
    driver.navigate().to(driver.findElementById(s"play-user-Desktop-$projectId-$UID").getAttribute("href"))
    Thread.sleep(5000)
  }

  def findBlobsInPlayerHtml = {
    driver.switchTo().frame(driver.findElementByCssSelector(".PageDisplay"))
    val blobLink = driver.findElementByCssSelector("head link").getAttribute("href")
    assert(blobLink.equals(s"https://ctassetsmanager.blob.core.windows.net/storage/$projectId/CSS/http/stage37-p6.ctqatest.info/style.css"))
    driver.navigate().to(blobLink)
    assert(driver.findElementByCssSelector("pre").getText.contains("Theme Name: Delphic"),"Blob css was not found!")
  }

}
