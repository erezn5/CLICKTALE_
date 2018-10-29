package com.clicktale.pipeline.regressions.tests.functionalTWithUI.backOfficeTests

import com.clicktale.pipeline.framework.ui._
import org.scalatest.{Tag, WordSpecLike}
import com.clicktale.pipeline.framework.dal.GeneralFunctions
import com.clicktale.pipeline.framework.helpers.StringManipulator
import com.clicktale.pipeline.framework.ui.BackOffice._
import com.clicktale.pipeline.framework.ui.Selenium
import com.clicktale.pipeline.regressions.testHelpers.WhichTestsToRun

class BackOfficeEditCreditsTests extends WordSpecLike with StringManipulator {
  "BackOffice EditCredits Tests" should {
    if (!WhichTestsToRun().shouldRunBackOfficeSubscribersTests) pending
    Selenium.ResetSessions()
    Home.GoTo()
    Login.login("test")
    BackOffice.Home.ClickMenuItem("Create Subscriber")
    val id = BackOffice.CreateSubscriber.SetID
    val name = BackOffice.CreateSubscriber.SetName
    BackOffice.CreateSubscriber.ClickCreate
    Subscribers.ClickNewProject()
    val pid = CreateProject.SetID
    val pname = BackOffice.CreateProject.SetName
    val ratio = BackOffice.CreateProject.SetRatio()
    val queue = BackOffice.CreateProject.SetPartition()
    val ttl = BackOffice.CreateProject.SetTTL()
    BackOffice.CreateProject.ClickCreate
    Projects.ClickCredits()
    BackOffice.ProjectCredits.SetBucketType("EventTrigger")
    BackOffice.ProjectCredits.SetCredits()
    BackOffice.ProjectCredits.SetRepeatEvery(2)
    BackOffice.ProjectCredits.ClickAdd
    val creditRule = GeneralFunctions.getCreditRulesByProjectId(pid.toInt)
    val creditRuleBefore = creditRule
    creditRule.next()
    val ruleId = creditRule.getInt("ProjectCreditRuleId")

    "Edit Credit Amount" taggedAs Tag("NotInTeamCity") in {
      Selenium.ResetSessions()
      BackOffice.Home.GoTo()
      BackOffice.Login.login("test")
      BackOffice.Home.ClickMenuItem("Projects")
      BackOffice.Projects.SetPID(pid)
      BackOffice.Projects.ClickSearch()
      BackOffice.Projects.ClickCredits()
      BackOffice.ProjectCredits.EditCredits(ruleId.toString)
      BackOffice.ProjectCredits.SetCredits(10000)
      BackOffice.ProjectCredits.ClickAddNoAssertion
      BackOffice.ProjectCredits.CheckAllTables(id, pid)
      val creditRuleAfter = GeneralFunctions.getCreditRulesByProjectId(pid.toInt)
      GeneralFunctions.CompareLines(creditRuleBefore, creditRuleAfter, "RandomCreditValue", "10000")
    }

    "Edit Credit Repetition" taggedAs Tag("NotInTeamCity") in {
      BackOffice.ProjectCredits.EditCredits(ruleId.toString)
      BackOffice.ProjectCredits.SetRepeatEvery(3)
      BackOffice.ProjectCredits.ClickAddNoAssertion
      BackOffice.ProjectCredits.CheckAllTables(id, pid)
      val creditRuleAfter = GeneralFunctions.getCreditRulesByProjectId(pid.toInt)
      GeneralFunctions.CompareLines(creditRuleBefore, creditRuleAfter, "IntervalInHours", "72")
      GeneralFunctions.deleteProject(pid)
      GeneralFunctions.deleteSubscriber(id)
    }
  }
}
