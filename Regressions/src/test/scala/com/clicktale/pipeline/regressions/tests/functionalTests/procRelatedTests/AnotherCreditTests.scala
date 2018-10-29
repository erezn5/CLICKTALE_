package com.clicktale.pipeline.regressions.tests.functionalTests.procRelatedTests

import com.clicktale.pipeline.dataObjects.AuthResponse
import com.clicktale.pipeline.framework.dal.ConfigParser.conf
import com.clicktale.pipeline.framework.dal._
import com.clicktale.pipeline.dataObjects.Credits
import com.clicktale.pipeline.framework.dal.GeneralFunctions
import com.clicktale.pipeline.framework.senders.SendSession._
import org.scalatest.WordSpecLike


class AnotherCreditTests extends WordSpecLike {

  // creditSpecificPid represents a PID made specially for QA purposes
  val PidForPendingCredit: Int =
    1234567
  val PidForRecordingCredit: Int =
    5678901
  val creditSpecificSubsId: Int =
    567

  println(PidForPendingCredit,PidForRecordingCredit,creditSpecificSubsId)

  val initialRandomCreditsAmount: Credits =
    GeneralFunctions.getCreditsByProject(PidForRecordingCredit,creditSpecificSubsId)
  val initialPendingCreditsAmount: Credits =
    GeneralFunctions.getCreditsByProject(PidForPendingCredit,creditSpecificSubsId)

  // Pushing the ETR Pending Sessions
  val sessionPending1: AuthResponse =
    pushSession("/recFiles/rec.json", conf.getString("WebRecorder.Session.States.ETRPending"), optionalPid = PidForPendingCredit, subsId = creditSpecificSubsId)
  pushSessionForUID("/recFiles/rec.json", sessionPending1.uid.toLong, optionalPid = PidForPendingCredit, subsId = creditSpecificSubsId, optionalTrackingState = sessionPending1.userTrackingState)
  pushSessionForUID("/recFiles/rec.json", sessionPending1.uid.toLong, optionalPid = PidForPendingCredit, subsId = creditSpecificSubsId, optionalTrackingState = sessionPending1.userTrackingState)

  // Pushing the Random Recording Session
  pushSession("/recFiles/rec.json", conf.getString("WebRecorder.Session.States.Recording"), optionalPid = PidForRecordingCredit, subsId = creditSpecificSubsId)

  Thread.sleep(60000)

  val midTestRandomCreditsAmount: Credits =
    GeneralFunctions.getCreditsByProject(PidForRecordingCredit,creditSpecificSubsId)
  val midTestPendingCreditsAmount: Credits =
    GeneralFunctions.getCreditsByProject(PidForPendingCredit,creditSpecificSubsId)

  // Pushing the ETR Event in Session
  pushSessionForUID("/recFiles/recEvent.json", sessionPending1.uid.toLong, optionalPid = PidForPendingCredit, subsId = creditSpecificSubsId, optionalTrackingState = sessionPending1.userTrackingState)

  Thread.sleep(60000)

  val finalRandomCreditsAmount: Credits =
    GeneralFunctions.getCreditsByProject(PidForRecordingCredit,creditSpecificSubsId)
  val finalPendingCreditsAmount: Credits =
    GeneralFunctions.getCreditsByProject(PidForPendingCredit,creditSpecificSubsId)

  "Project Credit Reduction Basic Test" in {
    assert(initialRandomCreditsAmount.RandomCreditAmount - finalRandomCreditsAmount.RandomCreditAmount == 1, "Amount of credits did not reduce for PID creditSpecificPid")
    assert(initialRandomCreditsAmount.RandomCreditAmount - midTestRandomCreditsAmount.RandomCreditAmount == 1, "Amount of credits did not reduce for PID creditSpecificPid")
  }

  "Project Credit Reduction ETR Test" in {
    assert(initialPendingCreditsAmount.EventTriggerCreditAmount - finalPendingCreditsAmount.EventTriggerCreditAmount == 4, "Amount of credits did not reduce for PID creditSpecificPid")
    assert(initialPendingCreditsAmount.EventTriggerCreditAmount - midTestPendingCreditsAmount.EventTriggerCreditAmount == 0, "Amount of credits did not reduce for PID creditSpecificPid")
  }

}
