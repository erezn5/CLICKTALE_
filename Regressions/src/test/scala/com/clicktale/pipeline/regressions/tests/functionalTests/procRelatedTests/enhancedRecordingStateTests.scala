package com.clicktale.pipeline.regressions.tests.functionalTests.procRelatedTests

import com.aerospike.client._
import com.aerospike.client.policy.{ClientPolicy, WritePolicy}
import com.clicktale.pipeline.dataObjects.{AuthResponse, CageArchivePackage, RecordingJson}
import com.clicktale.pipeline.framework.dal.ConfigParser.conf
import com.clicktale.pipeline.framework.senders.SendSession._
import com.clicktale.pipeline.framework.storage.GetFromS3._
import org.scalatest.WordSpecLike

class enhancedRecordingStateTests extends WordSpecLike {

  var runTest:
    Boolean = true
  var json:
    Option[AuthResponse] = None
  var archivedContent:
    Option[CageArchivePackage] = None


  val projectId:
    Int = 26959
  val subsId:
    Int = 223
  val recordingState:
    String = conf.getString("WebRecorder.Session.States.EnhancedRecording")
  val recordingFile:
    String = "/recFiles/recPerfect.json"
  val urlFromRule = "http://www.test.com"


  "An enhanced URL session (When no Random nor ETR credits)" should {
    try {
      json = Some(pushSessionParallely("/recFiles/recPerfect.json",recordingState, optionalPid = projectId, subsId = subsId, referrer = urlFromRule))
      println(json.get.sid.toLong)
      archivedContent = Some(loadFromCage(subsId, projectId,json.get.sid.toLong))
    } catch {
      case _: Throwable =>
        runTest = false
        info(s"Quota Management enhanced tests will NOT RUN!!! No archivePackage")
    }

    if (!runTest) {
      info("Tests Will not be run! Pending due to initiating issues")
      throw new IllegalArgumentException("CouldNotRunTest")
      pending
    }
    println(this.getClass.getName)
    val recJson: RecordingJson =
      RecordingJson(archivedContent.get.recording)

    "Should be saved & processed" in {
      assert(checkIfBucketExistsInS3(json.get))
      assert(RecordingJson(archivedContent.get.recording).populationType.toInt == 2,"S3 does not contain " + json.get.sid)
    }

    "Should return population type = 2 in archived JSON" in {
      assert(RecordingJson(archivedContent.get.recording).populationType.toInt == 2,"S3 does not contain " + json.get.sid)
    }
  }

  "An enhanced URL session also" should {
    if (!runTest) {
      info("Tests Will not be run! Pending due to initiating issues")
      throw new IllegalArgumentException("CouldNotRunTest")
      pending
    }
    "Should not change status when @ETSINGLE@ event" in{
      val json2 =
        pushSessionParallely("/recFiles/recSingleEvent.json",recordingState, optionalPid = projectId, subsId = subsId, referrer = urlFromRule)
      val archivedContent2 =
        loadFromCage(subsId, projectId,json2.sid.toLong)
      val testSession =
        getAuthForUID(json2.uid.toLong,optionalPid = projectId)
      assert(testSession.userTrackingState == recordingState,"User tracking state was not changed properly " + testSession.sid)
      assert(RecordingJson(archivedContent2.recording).SID == json2.uid.toLong,"S3 does not contain " + json2.uid.toLong)
      assert(RecordingJson(archivedContent2.recording).populationType.toInt == 2,"S3 does not contain " + json2.uid.toLong)
    }

    "Should not change status when @ET@ event" in{
      val json2 =
        pushSessionParallely("/recFiles/recEvent.json",recordingState, optionalPid = projectId, subsId = subsId, referrer = urlFromRule)
      val archivedContent2 =
        loadFromCage(subsId, projectId, json2.sid.toLong)
      val testSession =
        getAuthForUID(json2.uid.toLong,optionalPid = projectId)
      assert(testSession.userTrackingState != "Recording","User tracking state was not changed properly " + testSession.sid)
      assert(testSession.userTrackingState != "EventTriggeredRecording","User tracking state was not changed properly " + testSession.sid)
      assert(RecordingJson(archivedContent2.recording).SID == json2.uid.toLong,"S3 does not contain " + json2.uid.toLong)
      assert(RecordingJson(archivedContent2.recording).populationType.toInt == 2,"S3 does not contain " + json2.uid.toLong)
    }

  }
  println("Tests run:" + testNames)

}
