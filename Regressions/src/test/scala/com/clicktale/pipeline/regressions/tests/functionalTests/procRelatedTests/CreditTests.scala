package com.clicktale.pipeline.regressions.tests.functionalTests.procRelatedTests

import com.clicktale.pipeline.dataObjects.AuthResponse
import com.clicktale.pipeline.framework.dal.ConfigParser.conf
import com.clicktale.pipeline.framework.dal.{CoreAdministrationSqlManager, GeneralFunctions}
import com.clicktale.pipeline.framework.senders.SendSession._
import org.scalatest.WordSpecLike


class CreditTests extends WordSpecLike {

  // creditSpecificPid represents a PID made specially for QA purposes
  val PidForPendingCredit: Int =
    conf.getString(s"WebRecorder.Environments.${conf.getString("WebRecorder.Current.Environment")}.PidForPendingCreditTests").toInt
  val PidForPendingSingleCredit: Int =
    conf.getString(s"WebRecorder.Environments.${conf.getString("WebRecorder.Current.Environment")}.PidForPendingCreditTests").toInt
  val PidForRecordingCredit: Int =
    conf.getString(s"WebRecorder.Environments.${conf.getString("WebRecorder.Current.Environment")}.PidForRecordingCreditTests").toInt
  val PidForEnhancedCredit: Int =
    conf.getString(s"WebRecorder.Environments.${conf.getString("WebRecorder.Current.Environment")}.PidForEnhancedCreditTests").toInt
  val creditSpecificSubsId: Int =
    conf.getString(s"WebRecorder.Environments.${conf.getString("WebRecorder.Current.Environment")}.SubsIDForCreditTests").toInt

  val a = pushSessionParallely("/recFiles/rec.json", conf.getString("WebRecorder.Session.States.ETRPending"), PidForPendingCredit)
  val b = pushSessionForUID("/recFiles/rec.json", a.uid.toLong, optionalPid = PidForPendingCredit, subsId = creditSpecificSubsId, optionalTrackingState = a.userTrackingState)
  val c = pushSessionForUID("/recFiles/rec.json", a.uid.toLong, optionalPid = PidForPendingCredit, subsId = creditSpecificSubsId, optionalTrackingState = conf.getString("WebRecorder.Session.States.EnhancedRecording"), referrer = urlFromRuleForEnhancedSessions)
  val d = pushSessionForUID("/recFiles/erezTestEvent.json", a.uid.toLong, optionalPid = PidForPendingCredit, subsId = creditSpecificSubsId, optionalTrackingState = conf.getString("WebRecorder.Session.States.EnhancedRecording"), referrer = urlFromRuleForEnhancedSessions)

  val initialCreditAmount: creditStatus = creditStatus(
    GeneralFunctions.getCreditsByProject(PidForRecordingCredit, creditSpecificSubsId).RandomCreditAmount,
    GeneralFunctions.getCreditsByProject(PidForPendingCredit, creditSpecificSubsId).EventTriggerCreditAmount,
    GeneralFunctions.getCreditsByProject(PidForEnhancedCredit, creditSpecificSubsId).EnhancedCreditAmount)

  val urlFromRuleForEnhancedSessions:
    String = "http://www.test.com"

  // Pushing the ETR Pending Sessions
  val sessionPending1: AuthResponse =
    pushSessionParallely("/recFiles/rec.json", conf.getString("WebRecorder.Session.States.ETRPending"), PidForPendingCredit)
  pushSessionForUID("/recFiles/rec.json", sessionPending1.uid.toLong, optionalPid = PidForPendingCredit, subsId = creditSpecificSubsId, optionalTrackingState = sessionPending1.userTrackingState)
  pushSessionForUID("/recFiles/rec.json", sessionPending1.uid.toLong, optionalPid = PidForPendingCredit, subsId = creditSpecificSubsId, optionalTrackingState = sessionPending1.userTrackingState)

  val sessionPending2: AuthResponse =
    pushSessionParallely("/recFiles/rec.json", conf.getString("WebRecorder.Session.States.ETRPending"), PidForPendingCredit)
  pushSessionForUID("/recFiles/rec.json", sessionPending2.uid.toLong, optionalPid = PidForPendingCredit, subsId = creditSpecificSubsId, optionalTrackingState = sessionPending2.userTrackingState)
  pushSessionForUID("/recFiles/rec.json", sessionPending2.uid.toLong, optionalPid = PidForPendingCredit, subsId = creditSpecificSubsId, optionalTrackingState = sessionPending2.userTrackingState)


  // Pushing the Random Recording Session
//  pushSessionParallely("/recFiles/rec.json", conf.getString("WebRecorder.Session.States.Recording"), optionalPid = PidForRecordingCredit, subsId = creditSpecificSubsId)
  pushSessionParallely("/recFiles/rec.json", conf.getString("WebRecorder.Session.States.Recording"), optionalPid = PidForRecordingCredit, subsId = creditSpecificSubsId)
  // Pushing the Enhanced Recording Session
  pushSessionParallely("/recFiles/rec.json", conf.getString("WebRecorder.Session.States.EnhancedRecording"), optionalPid = PidForEnhancedCredit, subsId = creditSpecificSubsId, referrer = urlFromRuleForEnhancedSessions)

  Thread.sleep(60000)

  val midTestCreditAmount: creditStatus = creditStatus(
    GeneralFunctions.getCreditsByProject(PidForRecordingCredit, creditSpecificSubsId).RandomCreditAmount,
    GeneralFunctions.getCreditsByProject(PidForPendingCredit, creditSpecificSubsId).EventTriggerCreditAmount,
    GeneralFunctions.getCreditsByProject(PidForEnhancedCredit, creditSpecificSubsId).EnhancedCreditAmount)

  // Pushing the ETR Event in Session
//  pushSessionForUID("/recFiles/recEvent.json", sessionPending1.uid.toLong, optionalPid = PidForPendingCredit, subsId = creditSpecificSubsId, optionalTrackingState = sessionPending1.userTrackingState)
  pushSessionForUID("/recFiles/erezTestEvent.json", sessionPending1.uid.toLong, optionalPid = PidForPendingCredit, subsId = creditSpecificSubsId, optionalTrackingState = sessionPending1.userTrackingState)

  pushSessionForUID("/recFiles/recETSingle.json", sessionPending2.uid.toLong, optionalPid = PidForPendingCredit, subsId = creditSpecificSubsId, optionalTrackingState = sessionPending2.userTrackingState)
  Thread.sleep(60000)

  val finalCreditAmount: creditStatus = creditStatus(
    GeneralFunctions.getCreditsByProject(PidForRecordingCredit, creditSpecificSubsId).RandomCreditAmount,
    GeneralFunctions.getCreditsByProject(PidForPendingCredit, creditSpecificSubsId).EventTriggerCreditAmount,
    GeneralFunctions.getCreditsByProject(PidForEnhancedCredit, creditSpecificSubsId).EnhancedCreditAmount)

  println("http://172.22.0.117:8080/recording/v1/"+"/"+creditSpecificSubsId+"/"+PidForPendingCredit+"/"+this.sessionPending1.sid)
  println("http://172.22.0.117:8080/recording/v1/"+"/"+creditSpecificSubsId+"/"+PidForPendingSingleCredit+"/"+this.sessionPending1.sid)
  println("http://172.22.0.117:8080/recording/v1/"+creditSpecificSubsId+"/"+PidForRecordingCredit+"/"+this.sessionPending1.sid)

  println("http://172.22.0.117:8080/recording/v1/"+"/"+creditSpecificSubsId+"/"+PidForPendingCredit+"/"+this.sessionPending2.sid)
  println("http://172.22.0.117:8080/recording/v1/"+"/"+creditSpecificSubsId+"/"+PidForPendingSingleCredit+"/"+this.sessionPending2.sid)
  println("http://172.22.0.117:8080/recording/v1/"+creditSpecificSubsId+"/"+PidForRecordingCredit+"/"+this.sessionPending2.sid)


  "Project Credit Reduction Random Test" in {
    assert(initialCreditAmount.randomCredits - finalCreditAmount.randomCredits == 1, "Amount of credits did not reduce for PID creditSpecificPid")
    assert(initialCreditAmount.randomCredits - midTestCreditAmount.randomCredits == 1, "Amount of credits did not reduce for PID creditSpecificPid")
  }

  "Project Credit Reduction ETR Test" in {
    assert(initialCreditAmount.pendingCredits - finalCreditAmount.pendingCredits == 4, "Amount of credits did not reduce for PID creditSpecificPid")
    assert(initialCreditAmount.pendingCredits - midTestCreditAmount.pendingCredits == 0, "Amount of credits did not reduce for PID creditSpecificPid")
  }

  "Project Credit Reduction Enhanced Test" in {
    assert(initialCreditAmount.enhancedCredits - finalCreditAmount.enhancedCredits == 1, "Amount of credits did not reduce for PID creditSpecificPid")
    assert(initialCreditAmount.enhancedCredits - midTestCreditAmount.enhancedCredits == 1, "Amount of credits did not reduce for PID creditSpecificPid")
  }

  "rds connection" in {
     print(CoreAdministrationSqlManager.url)
  }

}

case class creditStatus(randomCredits: Long, pendingCredits: Long, enhancedCredits: Long)