package com.clicktale.pipeline.regressions.tests.functionalTWithUI.backOfficeTests

import com.clicktale.pipeline.framework.ui.BackOffice.Subscribers
import com.clicktale.pipeline.framework.dal.GeneralFunctions
import com.clicktale.pipeline.framework.dal.GeneralFunctions._
import com.clicktale.pipeline.framework.ui._
import com.clicktale.pipeline.framework.helpers.StringManipulator
import com.clicktale.pipeline.framework.ui
import com.clicktale.pipeline.framework.ui.BackOffice._
import com.clicktale.pipeline.framework.ui.Selenium
import com.clicktale.pipeline.regressions.testHelpers.WhichTestsToRun
import org.scalatest.{Tag, WordSpecLike}

class BackOfficeCreateCreditsTests extends WordSpecLike with StringManipulator {
  "BackOffice CreateCredits Tests" should {
    if (!WhichTestsToRun().shouldRunBackOfficeSubscribersTests) pending
    Selenium.ResetSessions()
    Home.GoTo()
    Login.login("test")
    ui.BackOffice.Home.ClickMenuItem("Create Subscriber")
    val id = ui.BackOffice.CreateSubscriber.SetID
    val name = ui.BackOffice.CreateSubscriber.SetName
    ui.BackOffice.CreateSubscriber.ClickCreate
    Subscribers.ClickNewProject()
    val pid = CreateProject.SetID
    val pname = ui.BackOffice.CreateProject.SetName
    val ratio = ui.BackOffice.CreateProject.SetRatio()
    val queue = ui.BackOffice.CreateProject.SetPartition()
    val ttl = ui.BackOffice.CreateProject.SetTTL()
    ui.BackOffice.CreateProject.ClickCreate

    "Create ETR Credit" taggedAs Tag("NotInTeamCity") in {
      Selenium.ResetSessions()
      ui.BackOffice.Home.GoTo()
      ui.BackOffice.Login.login("test")
      ui.BackOffice.Home.ClickMenuItem("Projects")
      Projects.SetPID(pid)
      ui.BackOffice.Projects.ClickSearch()
      ui.BackOffice.Projects.ClickCredits()
      ui.BackOffice.ProjectCredits.SetBucketType("EventTrigger")
      ui.BackOffice.ProjectCredits.SetCredits(10000)
      ui.BackOffice.ProjectCredits.SetRepeatEvery(2)
      val str = ui.BackOffice.ProjectCredits.ClickAdd
      assert(str.split('=')(2).trim.split('.')(0).trim == pid && str.split("Amount:")(1).trim.split(' ')(0).trim.toInt == 10000)
      Thread.sleep(1000)
      ui.BackOffice.ProjectCredits.CheckAllTables(id, pid)
    }

    "Create Random Credit" taggedAs Tag("NotInTeamCity") in {
      Selenium.ResetSessions()
      ui.BackOffice.Home.GoTo()
      ui.BackOffice.Login.login("test")
      ui.BackOffice.Home.ClickMenuItem("Projects")
      ui.BackOffice.Projects.SetPID(pid)
      ui.BackOffice.Projects.ClickSearch()
      ui.BackOffice.Projects.ClickCredits()
      ui.BackOffice.ProjectCredits.SetBucketType("Random")
      ui.BackOffice.ProjectCredits.SetCredits(10000)
      ui.BackOffice.ProjectCredits.SetRepeatEvery(2)
      val str = ui.BackOffice.ProjectCredits.ClickAdd
      assert(str.split('=')(2).trim.split('.')(0).trim == pid && str.split("Amount:")(1).trim.split(' ')(0).trim.toInt == 10000)
      Thread.sleep(1000)
      ui.BackOffice.ProjectCredits.CheckAllTables(id, pid)
    }

    "Create Enhanced Credit" taggedAs Tag("NotInTeamCity") in {
      Selenium.ResetSessions()
      ui.BackOffice.Home.GoTo()
      ui.BackOffice.Login.login("test")
      ui.BackOffice.Home.ClickMenuItem("Projects")
      ui.BackOffice.Projects.SetPID(pid)
      ui.BackOffice.Projects.ClickSearch()
      ui.BackOffice.Projects.ClickCredits()
      ui.BackOffice.ProjectCredits.SetBucketType("Enhanced")
      ui.BackOffice.ProjectCredits.SetCredits(10000)
      ui.BackOffice.ProjectCredits.SetRepeatEvery(2)
      val str = ui.BackOffice.ProjectCredits.ClickAdd
      assert(str.split('=')(2).trim.split('.')(0).trim == pid && str.split("Amount:")(1).trim.split(' ')(0).trim.toInt == 10000)
      Thread.sleep(1000)
      ui.BackOffice.ProjectCredits.CheckAllTables(id, pid)
      deleteProject(pid)
      deleteSubscriber(id)
    }
  }
}
