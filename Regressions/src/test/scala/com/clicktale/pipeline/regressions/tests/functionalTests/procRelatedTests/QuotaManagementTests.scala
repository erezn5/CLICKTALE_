package com.clicktale.pipeline.regressions.tests.functionalTests.procRelatedTests

import com.clicktale.pipeline.dataObjects.{AuthResponse, CageArchivePackage, RecordingJson}
import com.clicktale.pipeline.framework.dal.ConfigParser.conf
import com.clicktale.pipeline.framework.senders.SendSession._
import com.clicktale.pipeline.framework.storage.GetFromS3._
import com.clicktale.pipeline.framework.queue.RabbitMQ._
import org.json._
import org.scalatest.WordSpecLike

import scala.xml._

class QuotaManagementTests extends WordSpecLike {

  "Recording gets to RabbitMQ quota after processing test" in {
    clearQueue(conf.getString(s"WebRecorder.RabbitMQ.${conf.getString("WebRecorder.Current.Environment")}.TestQueueName"),conf.getString(s"WebRecorder.RabbitMQ.${conf.getString("WebRecorder.Current.Environment")}.exchangeName"),conf.getString(s"WebRecorder.RabbitMQ.${conf.getString("WebRecorder.Current.Environment")}.routingKey"))

    val recPid: Int = conf.getString(s"WebRecorder.Environments.${conf.getString("WebRecorder.Current.Environment")}.Pid").toInt
    val recSubsId: Int = conf.getString(s"WebRecorder.Environments.${conf.getString("WebRecorder.Current.Environment")}.SubsId").toInt

    val authJson: AuthResponse = pushSession("/recFiles/recPerfect.json", conf.getString("WebRecorder.Session.States.Recording"),optionalPid = recPid, subsId = recSubsId)
    val archivePackage: CageArchivePackage = loadFromCage(conf.getString(s"WebRecorder.Environments.${conf.getString("WebRecorder.Current.Environment")}.SubsId").toInt, conf.getString(s"WebRecorder.Environments.${conf.getString("WebRecorder.Current.Environment")}.Pid").toInt,authJson.sid.toLong)
    val recJson: RecordingJson = RecordingJson(archivePackage.recording)
    val message = getMessagesForQueue(conf.getString(s"WebRecorder.RabbitMQ.${conf.getString("WebRecorder.Current.Environment")}.TestQueueName"))
    val obj = new JSONObject(message)
    assert(obj.get("ProjectId").toString.toInt==recPid)
    assert(obj.get("SubscriberId").toString.toInt==recSubsId)
    assert(obj.get("TrackingStateType").toString.toInt==2)
  }

  println("Tests run:" + testNames)

}
