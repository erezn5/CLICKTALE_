package com.clicktale.pipeline.regressions.tests.functionalTests.procRelatedTests

import java.util

import com.clicktale.pipeline.dataObjects.{CageArchivePackage, PusherParams, RecordingJson}
import com.clicktale.pipeline.framework.dal.ConfigParser.conf
import com.clicktale.pipeline.framework.dal.AerospikeManager
import com.clicktale.pipeline.framework.senders.SendSession.pushSession
import com.clicktale.pipeline.framework.storage.GetFromS3.loadFromCage
import com.clicktale.pipeline.regressions.testHelpers.RunningTestsAssistance._
import com.clicktale.pipeline.regressions.testHelpers.TestData
import org.scalatest.WordSpecLike


import scala.collection.mutable
import scala.util.{Failure, Success, Try}
import scala.xml._

class BufferSizeTests extends WordSpecLike {

  val testArguments: Array[TestData] =
    Array(
      TestData(testName = "Buffer Size 40000 expected 3", sessionParams = PusherParams(recFile = "/recFiles/rec.json", optionalSuffix = "&msgsize=40")),
      TestData(testName = "Buffer Size 30000 expected 4", sessionParams = PusherParams(recFile = "/recFiles/rec.json", optionalSuffix = "&msgsize=30")),
      TestData(testName = "Buffer Size 20000 expected 6", sessionParams = PusherParams(recFile = "/recFiles/rec.json", optionalSuffix = "&msgsize=20")),
      TestData(testName = "Buffer Size 60000 expected 2", sessionParams = PusherParams(recFile = "/recFiles/rec.json", optionalSuffix = "&msgsize=60")),
      TestData(testName = "Buffer Size 120000 expected 1", sessionParams = PusherParams(recFile = "/recFiles/rec.json", optionalSuffix = "&msgsize=120"))
      //TestData(testName = "Buffer Size 10000 expected 12", sessionParams = PusherParams(recFile = "/recFiles/rec.json", optionalSuffix = "&msgsize=12"))
    )

  testArguments.foreach(x => runTests(x))

  def runTests(testData: TestData): Unit = {
    testData.testName should {
      val authResponse = pushSession(testData.sessionParams.recFile, conf.getString("WebRecorder.Session.States.Recording"), optionalPid = 26952, subsId = 422,optionalSuffix = testData.sessionParams.optionalSuffix)
      Thread.sleep(8000)
      val archivePackage: CageArchivePackage = loadFromCage(subsId = 422, pid = 26952, authResponse.sid.toLong)
      recordingBufferTests((authResponse.sid.toLong, archivePackage), testData.testName)
    }
  }

  def recordingBufferTests(x: (Long, CageArchivePackage), testName: String): Unit = {
    val archivePackage: CageArchivePackage = x._2
    val sid: Long = x._1

    val recXml: Elem =
      XML.loadString(archivePackage.events)

    val recDsr: Elem =
      Try(XML.loadString(archivePackage.streams)) match {
        case Success(dsr) if archivePackage.streams != null => dsr
        case Failure(_) if archivePackage.streams != null => <empty></empty>
        case _ => null
      }
    "Check Package is ok" in {
      assert(archivePackage != null)
      assert(archivePackage.recording != null)
      assert(archivePackage.webPage != null)
      assert(archivePackage.events != null)
      assert(archivePackage.streams != null)
    }

    "Check Aerospike for " + testName.split("expected ")(1) + " messages in Bin when " + testName.split("expected ")(0) in {
      val res = AerospikeManager.getMessagesFromAerospike(sid)
      assert(res.bins.get("maxMsgPerRec") == testName.split("expected ")(1).toInt)

    }


  }

  println("Tests run:" + testNames + "\n")

}
