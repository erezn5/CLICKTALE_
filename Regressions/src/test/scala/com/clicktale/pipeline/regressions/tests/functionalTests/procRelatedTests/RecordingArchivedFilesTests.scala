package com.clicktale.pipeline.regressions.tests.functionalTests.procRelatedTests

import com.clicktale.pipeline.dataObjects.{CageArchivePackage, PusherParams, RecordingJson}
import com.clicktale.pipeline.framework.helpers.JsonFunctions._
import com.clicktale.pipeline.framework.helpers.StringExtensions._
import com.clicktale.pipeline.framework.helpers.StringManipulator
import com.clicktale.pipeline.regressions.testHelpers.RunningTestsAssistance._
import com.clicktale.pipeline.regressions.testHelpers.{ExpectedRecFilePaths, RunningTestsAssistance, TestData}
import org.json4s._
import org.json4s.native.JsonMethods._
import org.scalatest.WordSpecLike

import scala.util.{Failure, Success, Try}
import scala.xml._

class RecordingArchivedFilesTests extends WordSpecLike with StringManipulator {

  val testArguments: Array[TestData] =
    Array(
      TestData(testName = "RecordingArchivedFilesTests - Double Streams Recording Test",
        sessionParams = PusherParams(recFile = "/recFiles/japan.json",messageCounter = 4)),
      TestData(testName = "RecordingArchivedFilesTests - Change Monitor Perfect Recording Encrypted Test",
        sessionParams = PusherParams(recFile = "/recFiles/recPerfect.json", optionalPid = 26952, subsId = 1001,messageCounter = 127)),
      TestData(testName = "RecordingArchivedFilesTests - Change Monitor Perfect Recording subsid 422 Test",
        sessionParams = PusherParams(recFile = "/recFiles/recPerfect.json", optionalPid = 26952, subsId = 422,messageCounter = 127)),
      TestData(testName = "RecordingArchivedFilesTests - Change Monitor Perfect Recording NOT Encrypted Test",
        sessionParams = PusherParams(recFile = "/recFiles/recPerfect.json", optionalPid = 223098, subsId = 223,messageCounter = 127))
    )

    testArguments(0).expectedRecFilePaths.pathToDsr = "/OutputTestFiles/recJapanUTF8/recJapanUTF8Dsr.dsr"
    testArguments(0).expectedRecFilePaths.pathToXml = "/OutputTestFiles/recJapanUTF8/recJapanUTF8Xml.xml"
    testArguments(0).expectedRecFilePaths.pathToJson = "/OutputTestFiles/recJapanUTF8/recJapanUTF8Json.json"
    testArguments(0).expectedRecFilePaths.pathToHtml = "/OutputTestFiles/recJapanUTF8/recJapanUTF8Html.html"

    testArguments(1).expectedRecFilePaths.pathToDsr = "/OutputTestFiles/recPerfectOutputFiles/recPerfectExpectedDsr.dsr"
    testArguments(1).expectedRecFilePaths.pathToXml = "/OutputTestFiles/recPerfectOutputFiles/recPerfectExpectedXml.xml"
    testArguments(1).expectedRecFilePaths.pathToJson = "/OutputTestFiles/recPerfectOutputFiles/recPerfectExpectedJson.json"
    testArguments(1).expectedRecFilePaths.pathToHtml = "/OutputTestFiles/recPerfectOutputFiles/recPerfectExpectedHtml.html"

    testArguments(2).expectedRecFilePaths.pathToDsr = "/OutputTestFiles/recPerfectOutputFiles/recPerfectExpectedDsr.dsr"
    testArguments(2).expectedRecFilePaths.pathToXml = "/OutputTestFiles/recPerfectOutputFiles/recPerfectExpectedXml.xml"
    testArguments(2).expectedRecFilePaths.pathToJson = "/OutputTestFiles/recPerfectOutputFiles/recPerfectExpectedJson.json"
    testArguments(2).expectedRecFilePaths.pathToHtml = "/OutputTestFiles/recPerfectOutputFiles/recPerfectExpectedHtml.html"

    testArguments(3).expectedRecFilePaths.pathToDsr = "/OutputTestFiles/recPerfectOutputFiles/recPerfectExpectedDsr.dsr"
    testArguments(3).expectedRecFilePaths.pathToXml = "/OutputTestFiles/recPerfectOutputFiles/recPerfectExpectedXml.xml"
    testArguments(3).expectedRecFilePaths.pathToJson = "/OutputTestFiles/recPerfectOutputFiles/recPerfectExpectedJson.json"
    testArguments(3).expectedRecFilePaths.pathToHtml = "/OutputTestFiles/recPerfectOutputFiles/recPerfectExpectedHtml.html"

    testArguments.takeRight(4).foreach(x => runTests(x))

  def runTests(testData: TestData): Unit = {
    testData.testName should {
      Try(pushAndGetArchivePackage(testData.sessionParams)) match {
        case Success(x) => recordingArchivedFilesContentTest(x,
          testData.expectedRecFilePaths,
          testData.sessionParams.optionalPid, testData.sessionParams.messageCounter)
        case Failure(x) => "Archived package existence test" in {
          assert(1 == 2, x)}
      }
    }
  }

  def recordingArchivedFilesContentTest(testedSessionData: (Long, CageArchivePackage), expectedOutputFiles: ExpectedRecFilePaths, sessionPid:Int, sessionMessageCount:Int): Unit = {

    val archivedPackage = testedSessionData._2
    val sessionId = testedSessionData._1

    "Simple rec to S3 test" in {
      assert(RecordingJson(archivedPackage.recording).SID == sessionId, "S3 does not contain " + sessionId)
    }

    "Compare the HTML file found in archivePackage to the expected resources HTML" in {
      val expectedHtml: String =
        scala.io.Source.fromInputStream(getClass.getResourceAsStream(expectedOutputFiles.pathToHtml)).getLines.mkString
      val archivedHtml: String =
        deleteLineEndings(archivedPackage.webPage)
      assert(expectedHtml == archivedHtml, "ArchiveHtml and expectedHtml are not equal")
    }

    "Compare the DSR file found in archivePackage to the expected resources DSR" in {
      RunningTestsAssistance.compareBetweenTwoXmlFiles(archivedPackage.streams, expectedOutputFiles.pathToDsr)
    }

    "Compare the XML file found in archivePackage to the expected resources XML" in {
      RunningTestsAssistance.compareBetweenTwoXmlFiles(archivedPackage.events, expectedOutputFiles.pathToXml, isOnlyCompareRecordingChild = true)
    }

    "Compare Json particular fields" in {
      val jsonFile = archivedPackage.recording
      assert(jsonFile != null)
      val json2 = parse(jsonFile)
      val json3 = RecordingJson(jsonFile)
      val tuplesList = getTupleListFromJObject(json2)
      RunningTestsAssistance.assertIndividualFields(tuplesList)
      assert(json3.PID == sessionPid,"Wrong PID in Json")
    }

    "Check XML Scheme In DSR File" in {
      val xmlStringArray = archivedPackage.streams
      try {
        archivedPackage.streams.escapeChars.exctratedXml
      }
      catch {
        case x: Throwable => fail(x)
      }
    }

    "Check XML Scheme In XML File (CANNOT BE EMPTY)" in {
      val xmlStringArray = archivedPackage.events
      try {
        xml.XML.loadString(xmlStringArray)
      }
      catch {
        case x: Throwable => fail(x)
      }
    }
    val expectedJson: String = scala.io.Source.fromInputStream(getClass.getResourceAsStream(expectedOutputFiles.pathToJson)).
      getLines.mkString
    val expectedRecordingJson = RecordingJson(expectedJson)
    val archivedJson = archivedPackage.recording
    val archivedRecordingJson = RecordingJson(archivedJson)

    "Testing expected fields content in recording Json - Location" in {
      assert(expectedRecordingJson.country == archivedRecordingJson.country, "issues with json " + sessionId)
      assert(expectedRecordingJson.city == archivedRecordingJson.city, "issues with json " + sessionId)
      assert(expectedRecordingJson.location == archivedRecordingJson.location, "issues with json " + sessionId)
      assert(expectedRecordingJson.countryCode == archivedRecordingJson.countryCode, "issues with json " + sessionId)
    }

    "Testing expected fields content in recording Json - Misc" in {


     // assert(expectedRecordingJson.webpageHash sameElements archivedRecordingJson.webpageHash, "issues with json " + sessionId)

      assert(expectedRecordingJson.WRRecordedCounter == archivedRecordingJson.WRRecordedCounter, "issues with json " + sessionId)
      assert(expectedRecordingJson.messageCount == archivedRecordingJson.messageCount, "issues with json " + sessionId)
      assert(expectedRecordingJson.countNonClickTaleJsErrors == archivedRecordingJson.countNonClickTaleJsErrors, "issues with json " + sessionId)
      assert(expectedRecordingJson.timeUnload == archivedRecordingJson.timeUnload, "issues with json " + sessionId)

      assert(expectedRecordingJson.timeDOMLoad == archivedRecordingJson.timeDOMLoad, "issues with json " + sessionId)
      assert(expectedRecordingJson.Tags sameElements archivedRecordingJson.Tags, "issues with json " + sessionId)
      assert(expectedRecordingJson.sizeWndIniW == archivedRecordingJson.sizeWndIniW, "issues with json " + sessionId)
      assert(expectedRecordingJson.referrer == archivedRecordingJson.referrer, "issues with json " + sessionId)
      assert(expectedRecordingJson.physicalDisplayWidth == archivedRecordingJson.physicalDisplayWidth, "issues with json " + sessionId)

      assert(expectedRecordingJson.description == archivedRecordingJson.description, "issues with json " + sessionId)
      assert(expectedRecordingJson.exposure == archivedRecordingJson.exposure, "issues with json " + sessionId)

    }

    "Testing expected fields content in recording Json - messageCount" in {
      assert(archivedRecordingJson.messageCount == sessionMessageCount, "issues with json " + sessionId)
    }

    "Zipped folder JSON fields content test - authTime (Symantec bug)" in {
      assert(archivedRecordingJson.authenticationUnixTime == archivedRecordingJson.clientDate, "issues with json " + sessionId)
      assert(archivedRecordingJson.authenticationUnixTime == archivedRecordingJson.recordingDate, "issues with json " + sessionId)
      assert(archivedRecordingJson.authenticationUnixTime == archivedRecordingJson.recordingDateJs, "issues with json " + sessionId)
    }

    "Zipped folder JSON fields content test - Click Analytics" in {
      assert(expectedRecordingJson.timeToFirstClick == archivedRecordingJson.timeToFirstClick, "issues with json " + sessionId)
      assert(expectedRecordingJson.tapCount == archivedRecordingJson.tapCount, "issues with json " + sessionId)
      assert(expectedRecordingJson.clicksBelowFold == archivedRecordingJson.clicksBelowFold, "issues with json " + sessionId)
      assert(expectedRecordingJson.zoomCount == archivedRecordingJson.zoomCount, "issues with json " + sessionId)
      assert(expectedRecordingJson.countMouseDown == archivedRecordingJson.countMouseDown, "issues with json " + sessionId)
    }

    "Zipped folder JSON fields content test - device Type" in {
      assert(expectedRecordingJson.device == archivedRecordingJson.device, "issues with json " + sessionId)
      assert(expectedRecordingJson.deviceTypeID == archivedRecordingJson.deviceTypeID, "issues with json " + sessionId)
      assert(expectedRecordingJson.isTouch == archivedRecordingJson.isTouch, "issues with json " + sessionId)
    }

    "Zipped folder JSON fields content test - XML & HTML Size" in {
      assert(expectedRecordingJson.HTMLSize == archivedRecordingJson.HTMLSize, "issues with json " + sessionId)
      assert(expectedRecordingJson.XMLSize >= archivedRecordingJson.XMLSize-5, "issues with json " + sessionId)
      assert(expectedRecordingJson.XMLSize > 0, "issues with json " + sessionId)
      assert(archivedRecordingJson.XMLSize >= 0, "issues with json " + sessionId)
    }

    val xmlFile = archivedPackage.events
    val parsedXml = XML.loadString(xmlFile)
    val parsedExpectedXml = XML.loadString(scala.io.Source.fromInputStream(getClass.getResourceAsStream(expectedOutputFiles.pathToXml)).
      getLines.mkString)

    "Simple rec zipped folder test - xml" in {
      assert(xmlFile contains "<?xml version=", "issues with xml " + sessionId)
    }

    "Testing if unix recording time in Json equals rtime attribute in xml" in {
      assert(archivedRecordingJson.authenticationUnixTime == (parsedXml \\ "recording" \ "@rtime").toString.toLong, "issues with json " + sessionId)
    }

    "Testing if exposure field in Json equals exposure attribute in xml" in {
      assert(archivedRecordingJson.exposure.toString == (parsedXml \\ "recording" \ "@exposure").toString, "issues with json " + sessionId)
    }

    "Testing if location fields in Json equals location attributes in xml" in {
      assert(expectedRecordingJson.country == (parsedXml \\ "recording" \ "@country").toString, "issues with json " + sessionId)
      assert(expectedRecordingJson.countryCode == (parsedXml \\ "recording" \ "@countryCode").toString, "issues with json " + sessionId)
    }

    "Testing if userAgent in Json equals userAgent attribute in xml" in {
      assert(archivedRecordingJson.userAgent.toString == (parsedXml \\ "recording" \ "@userAgent").toString, "issues with json " + sessionId)
    }
    "Testing if referrer in Json equals referrer attribute in xml" in {
      if (archivedRecordingJson.referrer != null) assert(archivedRecordingJson.referrer == (parsedXml \\ "recording" \ "@referrer").toString, "issues with json " + sessionId)
      else assert((parsedXml \\ "recording" \ "@referrer").toString == "", "issues with json " + sessionId)
    }

    "Testing if webpageHash in Json equals webpageHash attribute in xml" in {
      assert((parsedExpectedXml \\ "recording" \ "@webPageHash").toString == (parsedXml \\ "recording" \ "@webPageHash").toString, "issues with json " + sessionId)
    }

    "Testing expected fields content in recording XML - messageCount" in {
      assert((parsedExpectedXml \\ "recording" \ "@messagesCount").toString.toInt == archivedRecordingJson.messageCount, "issues with json " + sessionId)
    }



  }

  println(testNames)
}
