package com.clicktale.pipeline.regressions.tests.functionalTWithUI.backOfficeTests

import com.clicktale.pipeline.framework.dal.GeneralFunctions
import com.clicktale.pipeline.framework.dal.CreditsSqlManager
import com.clicktale.pipeline.framework.helpers.StringManipulator
import com.clicktale.pipeline.framework.ui
import com.clicktale.pipeline.framework.ui.BackOffice._
import com.clicktale.pipeline.framework.ui.Selenium
import com.clicktale.pipeline.regressions.testHelpers.WhichTestsToRun
import org.scalatest.{Tag, WordSpecLike}

class BackOfficeProjectsTests extends WordSpecLike with StringManipulator {
  "BackOffice Projects Tests" should {
    //if (!WhichTestsToRun().shouldRunBackOfficeSubscribersTests) pending
    CreditsSqlManager.connect()
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

    "Search For Existing Project By id" taggedAs Tag("NotInTeamCity") in {
      Selenium.ResetSessions()
      ui.BackOffice.Home.GoTo()
      ui.BackOffice.Login.login("test")
      ui.BackOffice.Home.ClickMenuItem("Projects")
      Projects.SetPID(pid)
      ui.BackOffice.Projects.ClickSearch()
      ui.BackOffice.Projects.CheckTableValuesByProjectId(pid)
    }

    "Search For Existing Project By SubsId" taggedAs Tag("NotInTeamCity") in {
      ui.BackOffice.Home.GoTo()
      ui.BackOffice.Home.ClickMenuItem("Projects")
      ui.BackOffice.Projects.SetSubsID(id)
      ui.BackOffice.Projects.ClickSearch()
      ui.BackOffice.Projects.CheckTableValuesByProjectId(pid)
    }

    "Search For Non-Existing Project id" taggedAs Tag("NotInTeamCity") in {
      ui.BackOffice.Home.GoTo()
      ui.BackOffice.Home.ClickMenuItem("Projects")
      ui.BackOffice.Projects.SetPID("111999")
      ui.BackOffice.Projects.ClickSearch()
      ui.BackOffice.Projects.CheckTableValuesByProjectId(pid, 0)
      GeneralFunctions.deleteProject(pid)
    }
  }
}
