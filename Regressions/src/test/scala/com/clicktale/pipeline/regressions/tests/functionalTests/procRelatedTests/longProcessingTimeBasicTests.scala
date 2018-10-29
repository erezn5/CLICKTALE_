package com.clicktale.pipeline.regressions.tests.functionalTests.procRelatedTests

import com.clicktale.pipeline.dataObjects.{CageArchivePackage, PusherParams, RecordingJson}
import com.clicktale.pipeline.framework.dal.ConfigParser.conf
import com.clicktale.pipeline.regressions.testHelpers.RunningTestsAssistance._
import com.clicktale.pipeline.regressions.testHelpers.TestData
import org.scalatest.WordSpecLike

import scala.util.{Failure, Success, Try}
import scala.xml._

class longProcessingTimeBasicTests extends WordSpecLike {

  val projectIdForEnhancedSessions:
    Int = 22302230
  val subsIdForEnhancedSessions:
    Int = 223
  val recordingStateForEnhancedSessions:
    String = conf.getString("WebRecorder.Session.States.EnhancedRecording")
  val recordingFileForEnhancedSessions:
    String = "/recFiles/recPerfect.json"
  val urlFromRuleForEnhancedSessions:
    String = "https://www.test.com"

  val longTermTestsArguments: Array[TestData] =
    Array(
      TestData(testName = "Session processing -> Missing message in the middle test",
        sessionParams = PusherParams(recFile = "/recFiles/recMissingMiddle.json"),
        laggingWaitingTime = 300000, expectedPopulationType = null),
      TestData(testName = "Session processing -> Missing last message test",
        sessionParams = PusherParams(recFile = "/recFiles/recEventMissingLastMessage.json"),
        laggingWaitingTime = 300000, expectedPopulationType = null)
    )

  longTermTestsArguments.foreach(x => {
    x.testName = "Random "+x.testName
    runTests(x)
  })

  longTermTestsArguments.foreach(x => {
    x.sessionParams.optionalPid = projectIdForEnhancedSessions
    x.sessionParams.subsId = subsIdForEnhancedSessions
    x.sessionParams.authState = recordingStateForEnhancedSessions
    x.sessionParams.referrer = urlFromRuleForEnhancedSessions
    x.testName = "Enhanced " + x.testName
    x.expectedPopulationType = "2"
    runTests(x)
  })
  def runTests(testData: TestData): Unit = {
    testData.testName should {
      Try(pushAndGetArchivePackage(testData.sessionParams, laggingWaitingTime = testData.laggingWaitingTime)) match {
        case Success(x) => recordingBasicArchiveTests(x,
          desiredPopulationType = testData.expectedPopulationType)
        case Failure(x) => "Archived package existence test" in {
          assert(1 == 2, x)
        }
      }
    }
  }

  def recordingBasicArchiveTests(x: (Long, CageArchivePackage),
                                 desiredPopulationType: String = null): Unit = {

    val archivePackage: CageArchivePackage = x._2
    val sid: Long = x._1

    "Check session archivedPackage exists" in {
      assert(archivePackage != null)
      assert(archivePackage.recording != null)
      assert(archivePackage.webPage != null)
      assert(archivePackage.events != null)
      assert(archivePackage.streams != null)
      assert(archivePackage.avro != null)
    }

    val recXml: Elem =
      XML.loadString(archivePackage.events)

    val recDsr: Elem =
      Try(XML.loadString(archivePackage.streams)) match {
        case Success(dsr) if archivePackage.streams != null => dsr
        case Failure(_) if archivePackage.streams != null => <empty></empty>
        case _ => null
      }

    "Check Cage JSON to include all the archived data types" in {
      assert(archivePackage != null)
      assert(archivePackage.recording != null)
      assert(archivePackage.webPage != null)
      assert(archivePackage.events != null)
      assert(archivePackage.streams != null)
      assert(archivePackage.avro != null)
    }

    "populationType test" in {
      assert(RecordingJson(archivePackage.recording).populationType == desiredPopulationType,"S3 does not contain " + sid)
    }

    "Basic rec zipped folder test - Json" in {
      val recJson =
        RecordingJson(archivePackage.recording)

      assert(recJson != null, "Json is null!")
      assert(recJson.SID == sid, "S3 does not contain " + sid)
      assert(recJson.country == "Israel", "issues with json " + sid)
      assert(!recJson.recorderIsMobile, "Recorder is mobile instead of not")
      assert(recJson.deviceTypeID == 1, "DeviceTypeID is not 1")
    }

    "Basic rec zipped folder test - xml" in {
      assert(recXml != null)
      assert(archivePackage.events contains "<?xml version=", "issues with xml " + sid)
    }

    "Basic rec zipped folder test - dsr" in {
      assert(recDsr != null)
    }

    "Basic rec zipped folder test - html" in {
      val recHtml = archivePackage.webPage

      assert(recHtml != null)
      assert(recHtml contains "<html", "issues with dsr " + sid)
      assert(recHtml contains "<head", "issues with dsr " + sid)
      assert(recHtml contains "<body", "issues with dsr " + sid)
    }

    "Dsr should be different from XML Test" in {
      assert(recDsr != recXml)
    }

  }

  println("Tests run:" + testNames + "\n")

}
