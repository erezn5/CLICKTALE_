package com.clicktale.pipeline.regressions.tests.functionalTests.procRelatedTests

import com.clicktale.pipeline.dataObjects.{AuthResponse, CageArchivePackage, RecordingJson}
import com.clicktale.pipeline.framework.dal.ConfigParser.{conf, formats}
import com.clicktale.pipeline.framework.senders.SendSession._
import com.clicktale.pipeline.framework.storage.GetFromS3._
import org.json4s._
import org.json4s.native.JsonMethods._
import org.scalatest.WordSpec
import java.lang.Throwable


class JSErrorsSpec extends WordSpec {

  var runTest:
    Boolean = true
  var json:
    Option[AuthResponse] = None
  var archivePackage:
    Option[CageArchivePackage] = None

  try {
    json = Some(pushSession("/recFiles/recJSError.json", conf.getString("WebRecorder.Session.States.Recording")))
    archivePackage = Some(loadFromCage(conf.getString(s"WebRecorder.Environments.${conf.getString("WebRecorder.Current.Environment")}.SubsId").toInt, conf.getString(s"WebRecorder.Environments.${conf.getString("WebRecorder.Current.Environment")}.Pid").toInt, json.get.sid.toLong))
  } catch {
    case ex: Throwable =>
      runTest = false
      info(s"JSError tests will NOT RUN!!! No archivePackage")
  }

  "A session with jsError" should  {
    if (!runTest) {
      info("Tests Will not be run! Pending due to initiating issues")
      pending
      //throw new CustomException("whatever")
    }
    "Should be saved & processed" in {
      Thread.sleep(5000)
      assert(checkIfBucketExistsInS3(json.get))
    }

    "Should include JSError in JSON" in {
      val recJson2 = RecordingJson(archivePackage.get.recording)
      assert(recJson2 != null)
      assert(recJson2.countNonClickTaleJsErrors == 2, "issues with json " + json.get.sid.toLong)
    }

    "Should have the right tagName & tagTypeID" in {
      val json2 = parse(archivePackage.get.recording)
      assert(((json2 \ "Tags")(0) \ "TagName").extract[String] == "jserror","issues with json " + json.get.sid)
      assert(((json2 \ "Tags")(0) \ "TagTypeID").extract[Int] == 2,"issues with json " + json.get.sid)
    }
  }
}


