package com.clicktale.pipeline.regressions.tests.functionalTests.procRelatedTests

import com.clicktale.pipeline.dataObjects.RecordingJson
import com.clicktale.pipeline.framework.dal.ConfigParser.conf
import com.clicktale.pipeline.framework.senders.SendSession._
import com.clicktale.pipeline.framework.storage.GetFromS3._
import org.json4s._
import org.scalatest.WordSpecLike


class ProtocolV16Tests extends WordSpecLike {

  implicit val formats = DefaultFormats

  "V16 protocol test when rec status is recording" in {
    val sessionID1 = pushSessionV16("/recFiles/rec.json", conf.getString("WebRecorder.Session.States.Recording"),"10", 5)
    val archivePackage1 = loadFromCage(conf.getString(s"WebRecorder.Environments.${conf.getString("WebRecorder.Current.Environment")}.SubsId").toInt, conf.getString(s"WebRecorder.Environments.${conf.getString("WebRecorder.Current.Environment")}.Pid").toInt,sessionID1.sid.toLong)
    val recJson1 = RecordingJson(archivePackage1.recording)
    assert(recJson1.SID == sessionID1.sid.toLong,"S3 does not contain " + sessionID1.sid)
  }

  "V16protocol test when rec status is etpending" in {
    val sessionID2 = pushSessionV16("/recFiles/recEvent.json", conf.getString("WebRecorder.Session.States.ETRPending"), "10", 5, optionalPid = 223224)
    val archivePackage2 = loadFromCage(223, 223224,sessionID2.sid.toLong)
    val recJson2 = RecordingJson(archivePackage2.recording)
    assert(recJson2.SID == sessionID2.sid.toLong,"S3 does not contain " + sessionID2.sid)
  }

  Thread.sleep(10000)
  println("Tests run:" + testNames)

}
