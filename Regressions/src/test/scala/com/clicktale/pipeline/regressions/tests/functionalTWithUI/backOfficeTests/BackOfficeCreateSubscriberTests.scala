package com.clicktale.pipeline.regressions.tests.functionalTWithUI.backOfficeTests

import com.clicktale.pipeline.framework.ui._
import com.clicktale.pipeline.framework._
import com.clicktale.pipeline.framework.dal.GeneralFunctions
import com.clicktale.pipeline.framework.helpers.StringManipulator
import com.clicktale.pipeline.framework.ui.BackOffice.{CreateSubscriber, Home, Login}
import com.clicktale.pipeline.framework.ui.Selenium
import com.clicktale.pipeline.regressions.testHelpers.WhichTestsToRun
import org.scalatest.{Tag, WordSpecLike}

class BackOfficeCreateSubscriberTests extends WordSpecLike with StringManipulator {
  "BackOffice CreateSubscriber Tests" should {
    if (!WhichTestsToRun().shouldRunBackOfficeSubscribersTests) pending
    "Create Basic Subscriber" taggedAs Tag("NotInTeamCity") in {
      Selenium.ResetSessions()
      Home.GoTo()
      Login.login("test")
      BackOffice.Home.ClickMenuItem("Create Subscriber")
      val id = CreateSubscriber.SetID
      val name = BackOffice.CreateSubscriber.SetName
      assert(BackOffice.CreateSubscriber.ClickCreate.split('=')(1).trim == id)
      val res = GeneralFunctions.getSubscriberById(id)
      assert(res.Name == name)
      assert(res.TotalQuota == 0)
      val res1 = GeneralFunctions.getSubscribersByName(name)
      assert(res1.length == 1)
      assert(res1.head.Id == id)
      assert(res1.head.TotalQuota == 0)
      GeneralFunctions.deleteSubscriber(id)
    }

    "2 Subscribers with the same Id" taggedAs Tag("NotInTeamCity") in {
      Selenium.ResetSessions()
      BackOffice.Home.GoTo()
      BackOffice.Login.login("test")
      BackOffice.Home.ClickMenuItem("Create Subscriber")
      val id = BackOffice.CreateSubscriber.SetID
      val name = BackOffice.CreateSubscriber.SetName
      BackOffice.CreateSubscriber.ClickCreate
      BackOffice.Home.ClickMenuItem("Create Subscriber")
      BackOffice.CreateSubscriber.SetID(id)
      BackOffice.CreateSubscriber.SetName
      assert(!BackOffice.CreateSubscriber.ClickCreateNoAssert)
      val res = GeneralFunctions.getSubscriberById(id)
      assert(res != null)
      GeneralFunctions.deleteSubscriber(id)
    }

    "2 Subscribers with the same Name" taggedAs Tag("NotInTeamCity") in {
      Selenium.ResetSessions()
      BackOffice.Home.GoTo()
      BackOffice.Login.login("test")
      BackOffice.Home.ClickMenuItem("Create Subscriber")
      val id = BackOffice.CreateSubscriber.SetID
      val name = BackOffice.CreateSubscriber.SetName
      BackOffice.CreateSubscriber.ClickCreate
      BackOffice.Home.ClickMenuItem("Create Subscriber")
      val id2 = BackOffice.CreateSubscriber.SetID
      BackOffice.CreateSubscriber.SetName(name)
      assert(BackOffice.CreateSubscriber.ClickCreateNoAssert)
      val res = GeneralFunctions.getSubscribersByName(name)
      assert(res.length == 2)
      GeneralFunctions.deleteSubscriber(id)
      GeneralFunctions.deleteSubscriber(id2)
    }
  }
}
