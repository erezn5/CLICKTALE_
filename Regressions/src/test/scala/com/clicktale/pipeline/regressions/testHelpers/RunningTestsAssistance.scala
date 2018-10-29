package com.clicktale.pipeline.regressions.testHelpers

import com.clicktale.pipeline.dataObjects.{CageArchivePackage, PusherParams}
import com.clicktale.pipeline.framework.dal.ConfigParser.{conf, formats}
import com.clicktale.pipeline.framework.helpers.StringExtensions._
import com.clicktale.pipeline.framework.senders.SendSession.{pushSession, pushSessionParallely}
import com.clicktale.pipeline.framework.storage.GetFromS3.{checkIfBucketExistsInS3, loadFromCage}
import org.json4s.{JValue, _}
import com.clicktale.pipeline.framework.senders._

import scala.util.{Failure, Success, Try}
import scala.xml.{Elem, XML}

object RunningTestsAssistance {
  def pushAndGetArchivePackage(sessionParams: PusherParams, laggingWaitingTime: Int = -1): (Long,CageArchivePackage) = {
    val authResponseOfStream =
      pushSession(sessionParams.recFile,
        sessionParams.authState,
        optionalPid = sessionParams.optionalPid,
        subsId = sessionParams.subsId,
        optionalMobile = sessionParams.optionalMobile,
        domain = sessionParams.domain,
        referrer = sessionParams.referrer,
        dnsName = sessionParams.dnsName
	//optionalUrlSuffix = sessionParams.optionalSuffix
	)

    if (laggingWaitingTime != -1){
      Thread.sleep(laggingWaitingTime)
    }
    val archivePackage: CageArchivePackage =
      loadFromCage(sessionParams.subsId, sessionParams.optionalPid, authResponseOfStream.sid.toLong)
    assert(checkIfBucketExistsInS3(authResponseOfStream), "Bucket does not exist for Double Streamed recording")
    (authResponseOfStream.sid.toLong,archivePackage)
  }

  def pushAndGetArchivePackageNonParalel(sessionParams: PusherParams, laggingWaitingTime: Int = -1): (Long,CageArchivePackage) = {
    val authResponseOfStream =
      pushSession(sessionParams.recFile,
        sessionParams.authState,
        optionalPid = sessionParams.optionalPid,
        subsId = sessionParams.subsId,
        optionalMobile = sessionParams.optionalMobile,
        domain = sessionParams.domain,
        referrer = sessionParams.referrer)//,
        //optionalUrlSuffix = sessionParams.optionalSuffix)

    if (laggingWaitingTime != -1){
      Thread.sleep(laggingWaitingTime)
    }
    val archivePackage: CageArchivePackage =
      loadFromCage(sessionParams.subsId, sessionParams.optionalPid, authResponseOfStream.sid.toLong)
    assert(checkIfBucketExistsInS3(authResponseOfStream), "Bucket does not exist for Double Streamed recording")
    (authResponseOfStream.sid.toLong,archivePackage)
  }

  def compareBetweenTwoXmlFiles(sessionArchivedXmlFile: String, expectedSessionXmlFile: String, isOnlyCompareRecordingChild: Boolean = false): Unit ={
    val xmlFromTest = sessionArchivedXmlFile.escapeChars.exctratedXml
    val xmlStreamSource = scala.io.Source.fromInputStream(getClass.getResourceAsStream(expectedSessionXmlFile))
    val xmlFromResource = xmlStreamSource.mkString.escapeChars.exctratedXml
    if (!isOnlyCompareRecordingChild) assert(xmlFromTest == xmlFromResource , xmlFromTest.child.diff(xmlFromResource.child).mkString(", "))
    else assert(xmlFromTest.child == xmlFromResource.child , xmlFromTest.child.diff(xmlFromResource.child).mkString(", "))
  }

  def assertIndividualFields(lst : List[(String, JValue)]) : Unit = {
    // Static values are taken from recPageTabCtEnable.json
    /*
    lst match {
      case Nil => Nil
      case ("XMLSize", x) :: rest => assert(x.extract[Int] > 0, "XMLSize value error"); assertIndividualFields(rest)
      case ("HTMLSize", x) :: rest => assert(x.extract[Int] == 27308, "HTMLSize value error"); assertIndividualFields(rest)
      case ("WRRecordedCounter", x) :: rest => assert(x.extract[Int] == 19, "WRRecordedCounter value error"); assertIndividualFields(rest)
      case ("WRGlobalCounter", x) :: rest => assert(x.extract[Int] == 45, "WRGlobalCounter value error"); assertIndividualFields(rest)
      case ("WebPageHash", x) :: rest => assert(x.extract[String] == conf.getString(s"WebRecorder.Environments.${conf.getString("WebRecorder.Current.Environment")}.WebPageHash"), "WebPageHash value error"); assertIndividualFields(rest)
      //case ("Version", x) :: rest => assert(x.extract[Int] == 15, "Version value error"); assertIndividualFields(rest)
      case ("CountryCode", x) :: rest => assert(x.extract[String] == "IL", "CountryCode value error"); assertIndividualFields(rest)
      case _ => assertIndividualFields(lst.tail)
    }*/
  }

}

