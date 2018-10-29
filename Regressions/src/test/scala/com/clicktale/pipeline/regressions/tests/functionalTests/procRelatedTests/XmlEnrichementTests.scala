package com.clicktale.pipeline.regressions.tests.functionalTests.procRelatedTests

import com.typesafe.config._
import com.clicktale.pipeline.dataObjects.{AuthResponse, CageArchivePackage}
import com.clicktale.pipeline.framework.senders.SendSession._
import com.clicktale.pipeline.framework.storage.GetFromS3._
import org.json4s._
import org.json4s.native.JsonMethods._
import org.scalatest.WordSpecLike
import com.clicktale.pipeline.framework.dal.ConfigParser.{conf, formats}

import scala.xml._


class XmlEnrichementTests extends WordSpecLike {

  "Xml Enrichement Tests" should {
    val authJson: AuthResponse =
      pushSession("/recFiles/recMobileScrollTapTest.json", conf.getString("WebRecorder.Session.States.Recording"))
    Thread.sleep(5000)
    val archivePackage1: CageArchivePackage =
      loadFromCage(conf.getString(s"WebRecorder.Environments.${conf.getString("WebRecorder.Current.Environment")}.SubsId").toInt, conf.getString(s"WebRecorder.Environments.${conf.getString("WebRecorder.Current.Environment")}.Pid").toInt, authJson.sid.toLong)

    val parsedJson: JValue = parse(archivePackage1.recording)
    val parsedXml: Elem = XML.loadString(archivePackage1.events)

    val xmlEnrichmentFlag: String = (parsedXml \\ "recording" \ "@enriched").toString
    val xmlTouchStartsCounter: Int = (parsedXml \\ "recording" \\ "touchstart").size
    val xmlTapCounter: Int = (parsedXml \\ "recording" \\ "tap").size
    val xmlScrollTapCounter: Int = (parsedXml \\ "recording" \\ "scrolltap").size
    val xmlDoubleTapCounter: Int = (parsedXml \\ "recording" \\ "doubletapzoom").size
    val xmlPinchTapCounter: Int = (parsedXml \\ "recording" \\ "pinchzoom").size


    "Recording Xml & Json Existence Taps Test" in {
      assert(parsedJson != null)
      assert(parsedXml != null)
      assert(xmlEnrichmentFlag == "true")
    }

    "TapCount Test" in {
      assert((parsedJson \ "TapCount").extract[Int] == 10, "issues with json " + authJson.sid)
      assert(xmlTapCounter == 10, "issues with json " + authJson.sid)
      assert(xmlTapCounter == (parsedJson \ "TapCount").extract[Int], "issues with json " + authJson.sid)
    }

    "DoubleTapZoom Test" in {
      assert(xmlDoubleTapCounter == 1, "issues with json " + authJson.sid)
    }

    "PinchZoom Test" in {
      assert(xmlPinchTapCounter == 1, "issues with json " + authJson.sid)
    }

    "ScrollTapZoom Test" in {
      assert(xmlScrollTapCounter == 17, "issues with json " + authJson.sid)
    }

    Thread.sleep(1000)
    println("Tests run:" + testNames)
  }
}
