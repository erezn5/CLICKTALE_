package com.clicktale.pipeline.regressions.tests.functionalTests.procRelatedTests

import com.clicktale.pipeline.dataObjects.{AuthResponse, CageArchivePackage, RecordingJson}
import com.clicktale.pipeline.framework.dal.ConfigParser.conf
import com.clicktale.pipeline.framework.senders.SendSession._
import com.clicktale.pipeline.framework.storage.GetFromS3._
import org.scalatest.WordSpecLike

class ImpactRecorderTests extends WordSpecLike {

  // creditSpecificPid represents a PID made specially for QA purposes
  val IRPid: Int = 223975
//  val IRPid: Int = 223223
  val IRSubsId: Int = 223

  "Pending to Pending to Pending to Recording test" in {
    val sessionPending1: AuthResponse = pushSession("/recFiles/rec.json", conf.getString("WebRecorder.Session.States.ETRPending"), optionalPid = IRPid, subsId = IRSubsId)
    assert(sessionPending1.userTrackingState == conf.getString("WebRecorder.Session.States.ETRPending"),
      "User tracking state was not changed properly " + sessionPending1.sid)
    //println("bla" + checkIfBucketExistsInS3(sessionPending1))
    //assert(loadFromCage(IRSubsId,IRPid,sessionPending1.sid.toLong).json.length>100)
    //assert(!checkIfBucketExistsInS3(sessionPending1))
    println(sessionPending1.sid)

    val sessionPending2 = pushSessionForUID("/recFiles/rec.json", sessionPending1.uid.toLong, optionalTrackingState = conf.getString("WebRecorder.Session.States.ETRPending"), optionalPid = IRPid, subsId = IRSubsId)
    //assert(loadFromCage(IRSubsId,IRPid,sessionPending1.sid.toLong).json.length>100)
    //assert(!checkIfBucketExistsInS3(sessionPending2))
    assert(sessionPending2.userTrackingState == conf.getString("WebRecorder.Session.States.ETRPending"),
      "User tracking state was not changed properly " + sessionPending2.sid)
    println(sessionPending1.sid + " " + sessionPending2.sid)

    val sessionPending3 = pushSessionForUID("/recFiles/rec.json", sessionPending1.uid.toLong, optionalTrackingState = conf.getString("WebRecorder.Session.States.ETRPending"), optionalPid = IRPid, subsId = IRSubsId)
    assert(sessionPending3.userTrackingState == conf.getString("WebRecorder.Session.States.ETRPending"),"User tracking state was not changed properly " + sessionPending3.sid)
    println(sessionPending1.sid + " " + sessionPending2.sid + " " + sessionPending3.sid)

    val sessionPending4 = pushSessionForUID("/recFiles/erezTestEvent.json", sessionPending1.uid.toLong, optionalTrackingState = conf.getString("WebRecorder.Session.States.ETRPending"), optionalPid = IRPid, subsId = IRSubsId)
    println(sessionPending1.sid + " " + sessionPending2.sid + " " + sessionPending3.sid + " " + sessionPending4.sid)

    Thread.sleep(30000)
    assert(sessionPending4.userTrackingState == conf.getString("WebRecorder.Session.States.ETRPending"),
      "User tracking state was not changed properly " + sessionPending4.sid)
    //      assert(checkIfBucketExistsInS3(sessionPending1))
    //      assert(checkIfBucketExistsInS3(sessionPending2))
    //      assert(checkIfBucketExistsInS3(sessionPending3))
    //      assert(checkIfBucketExistsInS3(sessionPending4))
    assert(loadFromCage(IRSubsId,IRPid,sessionPending1.sid.toLong).recording.length>100)
    assert(loadFromCage(IRSubsId,IRPid,sessionPending2.sid.toLong).recording.length>100)
    assert(loadFromCage(IRSubsId,IRPid,sessionPending3.sid.toLong).recording.length>100)
    assert(loadFromCage(IRSubsId,IRPid,sessionPending4.sid.toLong).recording.length>100)

    assert(RecordingJson(loadFromCage(IRSubsId, IRPid, sessionPending4.sid.toLong).recording).SID == sessionPending4.sid.toLong,
      "S3 does not contain " + sessionPending4.sid)
    assert(getAuthForUID(sessionPending1.uid.toLong).userTrackingState == conf.getString("WebRecorder.Session.States.ETRecording"),
      "User tracking state was not changed properly " + sessionPending4.sid)
    val archivePackage = loadFromCage(IRSubsId, IRPid, sessionPending4.sid.toLong)
    assert(RecordingJson(archivePackage.recording).populationType.toInt == 1,"S3 does not contain " + sessionPending4.sid)
  }

  "Pending to Recording test" in {
    for(a <- 1 to 2){
//      val sessionIDPending = pushSession("/recFiles/recEvent.json", conf.getString("WebRecorder.Session.States.ETRPending"),optionalPid = IRPid, subsId = IRSubsId)
val sessionIDPending = pushSession("/recFiles/erezTestEvent.json", conf.getString("WebRecorder.Session.States.ETRPending"),optionalPid = IRPid, subsId = IRSubsId)
      Thread.sleep(2000)
      val archivePackage2: CageArchivePackage =
        loadFromCage(IRSubsId, IRPid, sessionIDPending.sid.toLong)
      val testSession = getAuthForUID(sessionIDPending.uid.toLong)
      assert(testSession.userTrackingState == conf.getString("WebRecorder.Session.States.ETRecording"),"User tracking state was not changed properly " + RecordingJson(archivePackage2.recording).SID)
      assert(RecordingJson(archivePackage2.recording).SID == sessionIDPending.sid.toLong,"S3 does not contain " + RecordingJson(archivePackage2.recording).SID)
//      assert(RecordingJson(archivePackage2.analytics).populationType.toInt == 1,"S3 does not contain " + sessionIDPending.sid)
//      assert(RecordingJson(archivePackage2.recording).populationType == 1, "issues with json " + RecordingJson(archivePackage2.recording).SID)
      assert(RecordingJson(archivePackage2.recording).populationType.toInt == 1)

    }}


  "Pending to Pending to Pending to Recording and an additional one test" in {
    val sessionPending1: AuthResponse = pushSession("/recFiles/rec.json", conf.getString("WebRecorder.Session.States.ETRPending"), optionalPid = IRPid, subsId = IRSubsId)
    assert(sessionPending1.userTrackingState == conf.getString("WebRecorder.Session.States.ETRPending"),
      "User tracking state was not changed properly " + sessionPending1.sid)
  //  assert(!checkIfBucketExistsInS3(sessionPending1))
   // assert(loadFromCage(IRSubsId,IRPid,sessionPending1.sid.toLong).json.length>100)
    println(sessionPending1.sid)

    val sessionPending2 = pushSessionForUID("/recFiles/rec.json", sessionPending1.uid.toLong, optionalTrackingState = conf.getString("WebRecorder.Session.States.ETRPending"), optionalPid = IRPid, subsId = IRSubsId)
    //assert(!checkIfBucketExistsInS3(sessionPending2))
   //assert(loadFromCage(IRSubsId,IRPid,sessionPending1.sid.toLong).json.length>100)
    assert(sessionPending2.userTrackingState == conf.getString("WebRecorder.Session.States.ETRPending"),
      "User tracking state was not changed properly " + sessionPending2.sid)
    println(sessionPending1.sid + " " + sessionPending2.sid)

    val sessionPending3 = pushSessionForUID("/recFiles/rec.json", sessionPending1.uid.toLong, optionalTrackingState = conf.getString("WebRecorder.Session.States.ETRPending"), optionalPid = IRPid, subsId = IRSubsId)
    assert(sessionPending3.userTrackingState == conf.getString("WebRecorder.Session.States.ETRPending")
      ,"User tracking state was not changed properly " + sessionPending3.sid)
    println(sessionPending1.sid + " " + sessionPending2.sid + " " + sessionPending3.sid)

    val sessionPending4 = pushSessionForUID("/recFiles/recEvent.json", sessionPending1.uid.toLong, optionalTrackingState = conf.getString("WebRecorder.Session.States.ETRPending"), optionalPid = IRPid, subsId = IRSubsId)
    println(sessionPending1.sid + " " + sessionPending2.sid + " " + sessionPending3.sid + " " + sessionPending4.sid)

    Thread.sleep(30000)
    assert(sessionPending4.userTrackingState == conf.getString("WebRecorder.Session.States.ETRPending"),
      "User tracking state was not changed properly " + sessionPending4.sid)
//    assert(checkIfBucketExistsInS3(sessionPending1))
//    assert(checkIfBucketExistsInS3(sessionPending2))
//    assert(checkIfBucketExistsInS3(sessionPending3))
//    assert(checkIfBucketExistsInS3(sessionPending4))

    assert(loadFromCage(IRSubsId,IRPid,sessionPending1.sid.toLong).recording.length>100)
    assert(loadFromCage(IRSubsId,IRPid,sessionPending1.sid.toLong).recording.length>100)
    assert(loadFromCage(IRSubsId,IRPid,sessionPending1.sid.toLong).recording.length>100)
    assert(loadFromCage(IRSubsId,IRPid,sessionPending1.sid.toLong).recording.length>100)

    assert(RecordingJson(loadFromCage(IRSubsId, IRPid, sessionPending4.sid.toLong).recording).SID == sessionPending4.sid.toLong,
      "S3 does not contain " + sessionPending4.sid)
    assert(getAuthForUID(sessionPending1.uid.toLong).userTrackingState == conf.getString("WebRecorder.Session.States.ETRecording"),
      "User tracking state was not changed properly " + sessionPending4.sid)
    val archivePackage = loadFromCage(IRSubsId, IRPid, sessionPending4.sid.toLong)
    assert(RecordingJson(archivePackage.recording).populationType.toInt == 1,"S3 does not contain " + sessionPending4.sid)

    val sessionPending5 = pushSessionForUID("/recFiles/rec.json", sessionPending1.uid.toLong, optionalTrackingState = conf.getString("WebRecorder.Session.States.ETRecording"), optionalPid = IRPid, subsId = IRSubsId)
    //assert(checkIfBucketExistsInS3(sessionPending5))
    assert(loadFromCage(IRSubsId,IRPid,sessionPending5.sid.toLong).recording.length>100)
    val archivePackage5 = loadFromCage(IRSubsId, IRPid, sessionPending5.sid.toLong)
    assert(RecordingJson(archivePackage5.recording).populationType.toInt == 1,"S3 does not contain " + sessionPending5.sid)
  }

    "Pending to Pending with process (@ETSINGLE@) test" in { //TODO - Failed in Pipe
      val sessionID2 = pushSession("/recFiles/recSingleEvent.json", conf.getString("WebRecorder.Session.States.ETRPending"),optionalPid = IRPid, subsId = IRSubsId)
      val archivePackage2 = loadFromCage(IRSubsId, IRPid, sessionID2.sid.toLong)
      val testSession = getAuthForUID(sessionID2.uid.toLong,optionalPid = IRPid)
      assert(testSession.userTrackingState == conf.getString("WebRecorder.Session.States.ETRPending"),"User tracking state was not changed properly " + testSession.sid)
      assert(RecordingJson(archivePackage2.recording).SID == sessionID2.sid.toLong,"S3 does not contain " + sessionID2.sid)
      assert(RecordingJson(archivePackage2.recording).populationType.toInt == 2,"S3 does not contain " + sessionID2.sid)
    }

  Thread.sleep(1000)
  println("Tests run:" + testNames+"\n")

}
