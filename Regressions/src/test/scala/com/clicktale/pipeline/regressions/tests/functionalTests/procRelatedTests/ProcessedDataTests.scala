package com.clicktale.pipeline.regressions.tests.functionalTests.procRelatedTests

import com.clicktale.pipeline.dataObjects.{AuthResponse, CageArchivePackage}
import com.clicktale.pipeline.framework.dal.ConfigParser.conf
import com.clicktale.pipeline.framework.senders.SendSession._
import com.clicktale.pipeline.framework.storage.GetFromS3._
import org.json4s
import org.json4s._
import org.json4s.native.JsonMethods._
import org.scalatest.{BeforeAndAfterAll, Tag, WordSpecLike}

import scalaj.http._

class ProcessedDataTests extends WordSpecLike with BeforeAndAfterAll {

  val recPerfectSessionDetails: AuthResponse =
    pushSession("/recFiles/recPerfect.json", conf.getString("WebRecorder.Session.States.Recording"),optionalPid = 26952, subsId = 422)

  val archivePackage: CageArchivePackage =
    loadFromCage(subsId = 422,pid = 26952,recPerfectSessionDetails.sid.toLong)

  val cageInstanceIp: String =
    conf.getString(s"WebRecorder.Cage.${conf.getString("WebRecorder.Current.Environment")}.DNS")

  var parsedJsonCage: json4s.JValue = _

  "Check XML Contains TrueSizeScrW and TrueSizeScrH" in {
    assert(archivePackage.events.contains("TrueSizeScrW")&&archivePackage.events.contains("TrueSizeScrH"))
  }

  "Check JSON Contains TrueSizeScrW and TrueSizeScrH" in {
    assert((archivePackage.recording.contains("TrueSizeScrW"))&&(archivePackage.recording.contains("TrueSizeScrH")))
  }

}

