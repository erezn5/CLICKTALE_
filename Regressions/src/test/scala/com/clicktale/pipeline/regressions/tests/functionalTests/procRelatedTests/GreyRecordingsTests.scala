package com.clicktale.pipeline.regressions.tests.functionalTests.procRelatedTests

import com.typesafe.config._
import com.clicktale.pipeline.dataObjects.{AuthResponse, CageArchivePackage, RecordingJson}
import com.clicktale.pipeline.framework.senders.SendSession._
import com.clicktale.pipeline.framework.storage.GetFromS3._
import org.json4s._
import org.json4s.native.JsonMethods._
import org.scalatest.WordSpecLike
import com.clicktale.pipeline.framework.dal.ConfigParser.{conf, formats}


class GreyRecordingsTests extends WordSpecLike {

  val json: AuthResponse =
    pushSessionParallely("/recFiles/TDBANK.json", conf.getString("WebRecorder.Session.States.Recording"))

  Thread.sleep(120000)

  val archivePackage: CageArchivePackage =
    loadFromCage(conf.getString(s"WebRecorder.Environments.${conf.getString("WebRecorder.Current.Environment")}.SubsId").toInt, conf.getString(s"WebRecorder.Environments.${conf.getString("WebRecorder.Current.Environment")}.Pid").toInt, json.sid.toLong)

  "Recording with missing upload stream to S3" in {
    assert(RecordingJson(archivePackage.recording).SID == json.sid.toLong,
      "S3 does not contain " + json.sid)
  }

  "Recording with missing upload stream - Json" in {
    val jsonFile = archivePackage.recording
    val json2 = parse(jsonFile)
    assert(jsonFile != null)
    assert((json2 \ "Country").extract[String] == "Israel",
      "issues with json " + json.sid)
    assert((json2 \ "HTMLSize").extract[String].toInt == 0,
      "issues with json " + json.sid)
    assert((json2 \ "Description").extract[String] == "HTML page is missing",
      "issues with json " + json.sid)
  }

  println("Tests run:" + testNames)

}
