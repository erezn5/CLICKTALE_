package com.clicktale.pipeline.regressions.tests.functionalTWithUI.backOfficeTests

import com.clicktale.pipeline.framework.dal.GeneralFunctions
import com.clicktale.pipeline.framework.ui._
import com.clicktale.pipeline.framework.helpers.StringManipulator
import com.clicktale.pipeline.framework.ui.BackOffice._
import com.clicktale.pipeline.framework.ui.Selenium
import com.clicktale.pipeline.regressions.testHelpers.WhichTestsToRun
import org.scalatest.{Tag, WordSpecLike}

class BackOfficeEditSubscriberTests extends WordSpecLike with StringManipulator {
  "BackOffice Edit Subscribers Tests" should {
    if (!WhichTestsToRun().shouldRunBackOfficeSubscribersTests) pending
    Selenium.ResetSessions()
    Home.GoTo()
    Login.login("test")
    BackOffice.Home.ClickMenuItem("Create Subscriber")
    val id = CreateSubscriber.SetID
    var name = BackOffice.CreateSubscriber.SetName
    var quota = "0"
    BackOffice.CreateSubscriber.ClickCreate

    "Edit Subscriber" taggedAs Tag("NotInTeamCity") in {
      Selenium.ResetSessions()
      BackOffice.Home.GoTo()
      BackOffice.Login.login("test")
      BackOffice.Home.ClickMenuItem("Subscribers")
      Subscribers.SetID(id)
      BackOffice.Subscribers.ClickSearch()
      BackOffice.Subscribers.ClickEdit()
      name = EditSubscriber.SetName
      quota = BackOffice.EditSubscriber.SetQuota
      BackOffice.EditSubscriber.ClickSave()
      assert(BackOffice.Subscribers.GetSuccessLine == s"Successfully updated subscriber with id = $id")
      BackOffice.EditSubscriber.CheckTableValuesById(id, name, quota)
      GeneralFunctions.deleteSubscriber(id)
    }
  }
}
