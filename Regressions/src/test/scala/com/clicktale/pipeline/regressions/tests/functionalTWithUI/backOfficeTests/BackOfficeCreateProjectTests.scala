package com.clicktale.pipeline.regressions.tests.functionalTWithUI.backOfficeTests

import com.clicktale.pipeline.framework.dal.GeneralFunctions
import com.clicktale.pipeline.framework.dal.ConfigParser.conf
import com.clicktale.pipeline.framework.ui._
import com.clicktale.pipeline.framework.helpers.StringManipulator
import com.clicktale.pipeline.framework.ui.BackOffice._
import com.clicktale.pipeline.framework.ui.Selenium
import com.clicktale.pipeline.regressions.testHelpers.WhichTestsToRun
import org.scalatest.{Tag, WordSpecLike}

class BackOfficeCreateProjectTests extends WordSpecLike with StringManipulator {
  "BackOffice CreateProject Tests" should {
    if (!WhichTestsToRun().shouldRunBackOfficeSubscribersTests) pending
    Selenium.ResetSessions()
    Home.GoTo()
    Login.login("test")
    BackOffice.Home.ClickMenuItem("Create Subscriber")
    val id = CreateSubscriber.SetID
    val name = BackOffice.CreateSubscriber.SetName
    BackOffice.CreateSubscriber.ClickCreate

    "Create Basic ETR Project" taggedAs Tag("NotInTeamCity") in {
      Selenium.ResetSessions()
      BackOffice.Home.GoTo()
      BackOffice.Login.login("test")
      BackOffice.Home.ClickMenuItem("Subscribers")
      Subscribers.SetID(id)
      BackOffice.Subscribers.ClickSearch()
      BackOffice.Subscribers.ClickNewProject()
      val pid = CreateProject.SetID
      val pname = BackOffice.CreateProject.SetName
      val ratio = BackOffice.CreateProject.SetRatio()
      val queue = BackOffice.CreateProject.SetPartition()
      val ttl = BackOffice.CreateProject.SetTTL()
      val c = BackOffice.CreateProject.ClickCreate
      assert(c.split('=')(1).trim.split(' ')(0).trim == pid && c.split('=').last.trim == id)
      val res = GeneralFunctions.getProjectsById(pid)
      assert(res.head.SubscriberId == id)
      assert(res.head.Id == pid.toString)
      assert(res.head.Name == pname)
      assert(res.head.QueueId.toString == queue.toString)
      assert(res.head.Ratio.toString == ratio.toString)
      assert(res.head.Type == conf.getString("WebRecorder.BackOffice.DBValues.Project.ProcessingTypeId.ETR"))
      assert(res.head.LegacyProjectId == pid.toString)
      assert(res.head.TTL.toString == ttl.toString)
      assert(res.length == 1)
      GeneralFunctions.deleteProject(pid)
    }

    "Create Basic Full Project" taggedAs Tag("NotInTeamCity") in {
      Selenium.ResetSessions()
      BackOffice.Home.GoTo()
      BackOffice.Login.login("test")
      BackOffice.Home.ClickMenuItem("Subscribers")
      BackOffice.Subscribers.SetID(id)
      BackOffice.Subscribers.ClickSearch()
      BackOffice.Subscribers.ClickNewProject()
      val pid = BackOffice.CreateProject.SetID
      val pname = BackOffice.CreateProject.SetName
      val ratio = BackOffice.CreateProject.SetRatio()
      val queue = BackOffice.CreateProject.SetPartition()
      val ttl = BackOffice.CreateProject.SetTTL()
      BackOffice.CreateProject.SetProjectType(1)
      val c = BackOffice.CreateProject.ClickCreate
      assert(c.split('=')(1).trim.split(' ')(0).trim == pid && c.split('=').last.trim == id)
      val res = GeneralFunctions.getProjectsById(pid)
      assert(res.head.SubscriberId == id)
      assert(res.head.Id == pid.toString)
      assert(res.head.Name == pname)
      assert(res.head.QueueId.toString == queue.toString)
      assert(res.head.Ratio.toString == ratio.toString)
      assert(res.head.Type == conf.getString("WebRecorder.BackOffice.DBValues.Project.ProcessingTypeId.Full"))
      assert(res.head.LegacyProjectId == pid.toString)
      assert(res.head.TTL.toString == ttl.toString)
      assert(res.length == 1)
      GeneralFunctions.deleteProject(pid)
    }

    "2 Projects with the same Id" taggedAs Tag("NotInTeamCity") in {
      Selenium.ResetSessions()
      BackOffice.Home.GoTo()
      BackOffice.Login.login("test")
      BackOffice.Home.ClickMenuItem("Create Subscriber")
      val id2 = BackOffice.CreateSubscriber.SetID
      val name2 = BackOffice.CreateSubscriber.SetName
      BackOffice.CreateSubscriber.ClickCreate
      BackOffice.Home.GoTo()
      BackOffice.Login.login("test")
      BackOffice.Home.ClickMenuItem("Subscribers")
      BackOffice.Subscribers.SetID(id)
      BackOffice.Subscribers.ClickSearch()
      BackOffice.Subscribers.ClickNewProject()
      val pid = BackOffice.CreateProject.SetID
      val pname = BackOffice.CreateProject.SetName
      val ratio = BackOffice.CreateProject.SetRatio()
      val queue = BackOffice.CreateProject.SetPartition()
      val ttl = BackOffice.CreateProject.SetTTL()
      val c = BackOffice.CreateProject.ClickCreate
      assert(c.split('=')(1).trim.split(' ')(0).trim == pid && c.split('=').last.trim == id)
      BackOffice.Home.ClickMenuItem("Subscribers")
      BackOffice.Subscribers.SetID(id2)
      BackOffice.Subscribers.ClickSearch()
      BackOffice.Subscribers.ClickNewProject()
      BackOffice.CreateProject.SetID(pid)
      val pname2 = BackOffice.CreateProject.SetName
      val ratio2 = BackOffice.CreateProject.SetRatio()
      val queue2 = BackOffice.CreateProject.SetPartition()
      val ttl2 = BackOffice.CreateProject.SetTTL()
      val c2 = BackOffice.CreateProject.ClickCreate
      assert(c2.split('=')(1).trim.split(' ')(0).trim == pid && c2.split('=').last.trim == id2)
      val res = GeneralFunctions.getProjectsById(pid)
      val idx = if (res.head.SubscriberId == id) 0 else 1
      assert(res(idx).SubscriberId == id)
      assert(res(idx).Id == pid.toString)
      assert(res(idx).Name == pname)
      assert(res(idx).QueueId.toString == queue.toString)
      assert(res(idx).Ratio.toString == ratio.toString)
      assert(res(idx).Type == conf.getString("WebRecorder.BackOffice.DBValues.Project.ProcessingTypeId.ETR"))
      assert(res(idx).LegacyProjectId == pid.toString)
      assert(res(idx).TTL.toString == ttl.toString)
      assert(res(1 - idx).SubscriberId == id2)
      assert(res(1 - idx).Id == pid.toString)
      assert(res(1 - idx).Name == pname2)
      assert(res(1 - idx).QueueId.toString == queue2.toString)
      assert(res(1 - idx).Ratio.toString == ratio2.toString)
      assert(res(1 - idx).Type == conf.getString("WebRecorder.BackOffice.DBValues.Project.ProcessingTypeId.ETR"))
      assert(res(1 - idx).LegacyProjectId == pid.toString)
      assert(res(1 - idx).TTL.toString == ttl2.toString)
      assert(res.length == 2)
      GeneralFunctions.deleteProject(pid)
      GeneralFunctions.deleteSubscriber(id)
      GeneralFunctions.deleteSubscriber(id2)
    }

  }
}
