package com.clicktale.pipeline.regressions.tests.functionalTests.procRelatedTests

import com.clicktale.pipeline.dataObjects.{AuthResponse, CageArchivePackage, RecordingJson}
import com.clicktale.pipeline.framework.dal.ConfigParser.conf
import com.clicktale.pipeline.framework.dal.GeneralFunctions
import com.clicktale.pipeline.framework.dal.{CreditsSqlManager, GeneralFunctions}
import com.clicktale.pipeline.framework.senders.SendSession._
import com.clicktale.pipeline.framework.storage.GetFromS3._
import org.scalatest.WordSpecLike

class ProcDuplicatePidTests extends WordSpecLike {

  CreditsSqlManager.connect()

  // creditSpecificPid represents a PID made specially for QA purposes
  val creditSpecificPid: Int =
    conf.getString(s"WebRecorder.Environments.${conf.getString("WebRecorder.Current.Environment")}.PidForDupIDTests").toInt
  val creditSpecificSubsId1: Int =
    conf.getString(s"WebRecorder.Environments.${conf.getString("WebRecorder.Current.Environment")}.SubsId").toInt
  val creditSpecificSubsId2: Int =
    conf.getString(s"WebRecorder.Environments.${conf.getString("WebRecorder.Current.Environment")}.SubsId2").toInt
  val desiredRecState: String =
    conf.getString("WebRecorder.Session.States.Recording")
  val numberOfSessions: Int = 2

  testCreditsDifference("rec", 5000)
  testCreditsDifference("recMissingMiddle", 180000)
  testCreditsDifference("recMissingEndMessage", 180000)

  println("Tests run:" + testNames)

  def testCreditsDifference(recFile: String,
                            waitingTime: Int,
                            isPending: Boolean = false): Unit ={
    "Testing the deduction of the right amount of credits from the right project for recording type: " + recFile in{
      if (isPending) pending

      val initCreditState: List[Long] =
        List(GeneralFunctions.getCreditsByProject(creditSpecificPid,creditSpecificSubsId1).RandomCreditAmount, GeneralFunctions.getCreditsByProject(creditSpecificPid,creditSpecificSubsId2).RandomCreditAmount)

      val sids1: Array[Long] =
        new Array[Long](numberOfSessions+1)
      val sids2: Array[Long] =
        new Array[Long](numberOfSessions+1)

      for(i <- 1 to numberOfSessions){
        sids1(i) =
          pushSessionParallely("/recFiles/" + recFile + ".json", desiredRecState, optionalPid = creditSpecificPid, subsId = creditSpecificSubsId1)
            .sid.toLong
        sids2(i) =
          pushSessionParallely("/recFiles/" + recFile + ".json", desiredRecState, optionalPid = creditSpecificPid, subsId = creditSpecificSubsId2)
            .sid.toLong
      }

      Thread.sleep(waitingTime*2)

      List.range(1,numberOfSessions+1).par.foreach(i => {
        val recJson1: RecordingJson =
          RecordingJson(loadFromCage(creditSpecificSubsId1, creditSpecificPid,sids1(i)).recording)
        val recJson2: RecordingJson =
          RecordingJson(loadFromCage(creditSpecificSubsId2, creditSpecificPid,sids2(i)).recording)
        assert(recJson1.subscriberId==creditSpecificSubsId1)
        assert(recJson2.subscriberId==creditSpecificSubsId2)
        assert(recJson1.PID==recJson2.PID)
      })

      val finalCreditState: List[Long] =
        List(GeneralFunctions.getCreditsByProject(creditSpecificPid,creditSpecificSubsId1).RandomCreditAmount, GeneralFunctions.getCreditsByProject(creditSpecificPid,creditSpecificSubsId2).RandomCreditAmount)

      assert(initCreditState.head - finalCreditState.head == numberOfSessions)
      assert(initCreditState(1) - finalCreditState(1) == numberOfSessions)
  }}

}
