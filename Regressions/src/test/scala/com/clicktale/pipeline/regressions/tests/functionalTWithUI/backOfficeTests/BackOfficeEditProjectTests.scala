package com.clicktale.pipeline.regressions.tests.functionalTWithUI.backOfficeTests

import com.clicktale.pipeline.framework
import com.clicktale.pipeline.framework.dal.GeneralFunctions
import org.scalatest.{Tag, WordSpecLike}
import com.clicktale.pipeline.framework.helpers.StringManipulator
import com.clicktale.pipeline.framework.ui.BackOffice._
import com.clicktale.pipeline.framework.ui.Selenium
import com.clicktale.pipeline.regressions.testHelpers.WhichTestsToRun

class BackOfficeEditProjectTests extends WordSpecLike with StringManipulator {
  "BackOffice Edit Project Tests" should {
    if (!WhichTestsToRun().shouldRunBackOfficeSubscribersTests) pending
    Selenium.ResetSessions()
    Home.GoTo()
    Login.login("test")
    framework.ui.BackOffice.Home.ClickMenuItem("Create Subscriber")
    val id = framework.ui.BackOffice.CreateSubscriber.SetID
    val name = framework.ui.BackOffice.CreateSubscriber.SetName
    framework.ui.BackOffice.CreateSubscriber.ClickCreate
    Subscribers.ClickNewProject()
    val pid = CreateProject.SetID
    val pname = framework.ui.BackOffice.CreateProject.SetName
    val ratio = framework.ui.BackOffice.CreateProject.SetRatio()
    val queue = framework.ui.BackOffice.CreateProject.SetPartition()
    val ttl = framework.ui.BackOffice.CreateProject.SetTTL()
    framework.ui.BackOffice.CreateProject.ClickCreate

    "Edit ProjectName" taggedAs Tag("NotInTeamCity") in {
      val lineBefore = framework.dal.GeneralFunctions.getProjectLine(pid)
      Selenium.ResetSessions()
      framework.ui.BackOffice.Home.GoTo()
      framework.ui.BackOffice.Login.login("test")
      framework.ui.BackOffice.Home.ClickMenuItem("Projects")
      Projects.SetPID(pid)
      framework.ui.BackOffice.Projects.ClickSearch()
      framework.ui.BackOffice.Projects.ClickEdit()
      val newVal = framework.ui.BackOffice.EditProject.SetName
      framework.ui.BackOffice.EditProject.ClickCreateNoAssert
      val lineAfter = GeneralFunctions.getProjectLine(pid)
      GeneralFunctions.CompareLines(lineBefore, lineAfter, "ProjectName", newVal)
    }

    "Edit Ratio" taggedAs Tag("NotInTeamCity") in {
      val lineBefore = GeneralFunctions.getProjectLine(pid)
      framework.ui.BackOffice.Projects.ClickEdit()
      val newVal = framework.ui.BackOffice.EditProject.SetRatio(0.1)
      framework.ui.BackOffice.EditProject.ClickCreateNoAssert
      val lineAfter = GeneralFunctions.getProjectLine(pid)
      GeneralFunctions.CompareLines(lineBefore, lineAfter, "RecordingRatio", newVal.toString)
    }

    "Edit Partition" taggedAs Tag("NotInTeamCity") in {
      val lineBefore = GeneralFunctions.getProjectLine(pid)
      framework.ui.BackOffice.Projects.ClickEdit()
      val newVal = framework.ui.BackOffice.EditProject.SetPartition(30)
      framework.ui.BackOffice.EditProject.ClickCreateNoAssert
      val lineAfter = GeneralFunctions.getProjectLine(pid)
      GeneralFunctions.CompareLines(lineBefore, lineAfter, "QueueId", newVal.toString)
    }

    "Edit ProjectType" taggedAs Tag("NotInTeamCity") in {
      val lineBefore = GeneralFunctions.getProjectLine(pid)
      framework.ui.BackOffice.Projects.ClickEdit()
      framework.ui.BackOffice.EditProject.SetProjectType(1)
      framework.ui.BackOffice.EditProject.ClickCreateNoAssert
      val lineAfter = GeneralFunctions.getProjectLine(pid)
      GeneralFunctions.CompareLines(lineBefore, lineAfter, "ProcessingTypeId", "1")
    }

    "Edit TTL" taggedAs Tag("NotInTeamCity") in {
      val lineBefore = GeneralFunctions.getProjectLine(pid)
      framework.ui.BackOffice.Projects.ClickEdit()
      val newVal = framework.ui.BackOffice.EditProject.SetTTL(200)
      framework.ui.BackOffice.EditProject.ClickCreateNoAssert
      val lineAfter = GeneralFunctions.getProjectLine(pid)
      GeneralFunctions.CompareLines(lineBefore, lineAfter, "TimeToLive", newVal.toString)
    }

    "Edit All" taggedAs Tag("NotInTeamCity") in {
      val lineBefore = GeneralFunctions.getProjectLine(pid)
      framework.ui.BackOffice.Projects.ClickEdit()
      val newValName = framework.ui.BackOffice.EditProject.SetName
      val newValRatio = framework.ui.BackOffice.EditProject.SetRatio(0.2)
      val newValPartition = framework.ui.BackOffice.EditProject.SetPartition(22)
      framework.ui.BackOffice.EditProject.SetProjectType(2)
      val newValTTL = framework.ui.BackOffice.EditProject.SetTTL(300)
      framework.ui.BackOffice.EditProject.ClickCreateNoAssert
      val lineAfter = GeneralFunctions.getProjectLine(pid)
      lineAfter.next()
      assert(lineAfter.getString("ProjectName") == newValName)
      assert(lineAfter.getString("RecordingRatio") == newValRatio.toString)
      assert(lineAfter.getString("QueueId") == newValPartition.toString)
      assert(lineAfter.getString("ProcessingTypeId") == "2")
      assert(lineAfter.getString("TimeToLive") == newValTTL.toString)
      GeneralFunctions.deleteProject(pid)
      GeneralFunctions.deleteSubscriber(id)
    }
  }
}
