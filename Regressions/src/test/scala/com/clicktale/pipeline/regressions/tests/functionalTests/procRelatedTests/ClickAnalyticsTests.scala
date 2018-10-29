package com.clicktale.pipeline.regressions.tests.functionalTests.procRelatedTests

import com.clicktale.pipeline.dataObjects.AuthResponse
import com.clicktale.pipeline.framework.dal.ConfigParser.{conf, formats}
import com.clicktale.pipeline.framework.senders.SendSession._
import com.clicktale.pipeline.framework.storage.GetFromS3._
import org.json4s._
import org.json4s.native.JsonMethods._
import org.scalatest.{Tag, WordSpecLike}


class ClickAnalyticsTests extends WordSpecLike {

  val authResponse1: AuthResponse =
    pushSession("/recFiles/rec4up_8down_4up.json", conf.getString("WebRecorder.Session.States.Recording"))

  val jsonJvalueFromFirstAuth: JValue =
    parse(loadFromCage(conf.getString(s"WebRecorder.Environments.${conf.getString("WebRecorder.Current.Environment")}.SubsId").toInt,conf.getString(s"WebRecorder.Environments.${conf.getString("WebRecorder.Current.Environment")}.Pid").toInt,
        authResponse1.sid.toLong).recording)

  val authResponseForTwoLinks: AuthResponse =
    pushSession("/recFiles/rec2Links.json", conf.getString("WebRecorder.Session.States.Recording"))

  val jsonJvalueFromTwoLinks: JValue =
    parse(loadFromCage(conf.getString(s"WebRecorder.Environments.${conf.getString("WebRecorder.Current.Environment")}.SubsId").toInt, conf.getString(s"WebRecorder.Environments.${conf.getString("WebRecorder.Current.Environment")}.Pid").toInt,
      authResponseForTwoLinks.sid.toLong).recording)

  "Test Archive Existance" taggedAs Tag("BuildRegressions") in {
    assert(checkIfBucketExistsInS3(authResponse1), "Bucket does not exist!")
    assert(jsonJvalueFromFirstAuth != null, "Json is null!")
  }

  "Test Second Archive Existance" taggedAs Tag("BuildRegressions") in {
    assert(checkIfBucketExistsInS3(authResponseForTwoLinks), "Bucket does not exist!")
    assert(jsonJvalueFromTwoLinks != null, "Json is null!")
  }

  "Test Clicks Below Fold" taggedAs Tag("BuildRegressions") in {
    assert((jsonJvalueFromFirstAuth \ "ClicksBelowFold").extract[Int] == 8,
      "ClicksBelowFold not correct")
  }

  "Test Clicks On Link Elements - No Links" in {
    assert((jsonJvalueFromFirstAuth \ "ClicksOnLinkLikeElements").extract[Int] == 0,
      "ClicksOnLinkLikeElements not correct")
  }

  "Test Clicks On Non Click Like Elements - No Links" in {
    assert((jsonJvalueFromFirstAuth \ "ClicksOnNonLinkLikeElements").extract[Int] == 16,
      "ClicksOnNonLinkLikeElements not correct")
  }

  "Test Clicks On Link Like Elements" in {
    assert((jsonJvalueFromTwoLinks \ "ClicksOnLinkLikeElements").extract[Int] == 2,
      "ClicksOnLinkLikeElements not correct")
  }

  "Test Clicks On None Link Like Elements" in {
    assert((jsonJvalueFromTwoLinks \ "ClicksOnNonLinkLikeElements").extract[Int] == 2,
      "ClicksOnNonLinkLikeElements not correct")
  }
  Thread.sleep(1000)

}


