package com.clicktale.pipeline.regressions.tests.functionalTWithUI.backOfficeTests

import com.clicktale.pipeline.framework.dal
import com.clicktale.pipeline.framework.ui._
import com.clicktale.pipeline.framework.dal.GeneralFunctions
import com.clicktale.pipeline.framework.helpers.StringManipulator
import com.clicktale.pipeline.framework.ui
import com.clicktale.pipeline.framework.ui.BackOffice.{CreateSubscriber, Home, Login, Subscribers}
import com.clicktale.pipeline.framework.ui.Selenium
import com.clicktale.pipeline.regressions.testHelpers.WhichTestsToRun
import org.scalatest.{Tag, WordSpecLike}

class BackOfficeSubscribersTests extends WordSpecLike with StringManipulator {

  "BackOffice Subscribers Tests" should {
    if (!WhichTestsToRun().shouldRunBackOfficeSubscribersTests) pending

    Selenium.ResetSessions()
    Home.GoTo()
    Login.login("test")
    ui.BackOffice.Home.ClickMenuItem("Create Subscriber")
    val id = CreateSubscriber.SetID
    val name = ui.BackOffice.CreateSubscriber.SetName
    ui.BackOffice.CreateSubscriber.ClickCreate

    "Search For Existing Subscriber id" in {
      Selenium.ResetSessions()
      ui.BackOffice.Home.GoTo()
      ui.BackOffice.Login.login("test")
      ui.BackOffice.Home.ClickMenuItem("Subscribers")
      Subscribers.SetID(id)
      ui.BackOffice.Subscribers.ClickSearch()
      ui.BackOffice.Subscribers.CheckTableValueById(id)
    }

    "Search For Existing Subscriber id + name" in {
      ui.BackOffice.Home.GoTo()
      ui.BackOffice.Home.ClickMenuItem("Subscribers")
      ui.BackOffice.Subscribers.SetID(id)
      ui.BackOffice.Subscribers.SetName(name)
      ui.BackOffice.Subscribers.ClickSearch()
      ui.BackOffice.Subscribers.CheckTableValueById(id)
    }

    "Search For Non-Existing Subscriber id" in {
      ui.BackOffice.Home.GoTo()
      ui.BackOffice.Home.ClickMenuItem("Subscribers")
      ui.BackOffice.Subscribers.SetID(id)
      ui.BackOffice.Subscribers.ClickSearch()
      ui.BackOffice.Subscribers.CheckTableValueById(id)
      GeneralFunctions.deleteSubscriber(id)
    }
  }
}
