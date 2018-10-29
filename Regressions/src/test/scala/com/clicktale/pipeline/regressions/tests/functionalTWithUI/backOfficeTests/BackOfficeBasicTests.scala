package com.clicktale.pipeline.regressions.tests.functionalTWithUI.backOfficeTests

import com.clicktale.pipeline.framework.dal.ConfigParser.{conf, formats}
import com.clicktale.pipeline.framework.ui._
import com.clicktale.pipeline.framework.helpers.StringManipulator
import com.clicktale.pipeline.framework.ui
import com.clicktale.pipeline.framework.ui.BackOffice.{Home, Login}
import com.clicktale.pipeline.framework.ui.Selenium
import com.clicktale.pipeline.regressions.testHelpers.WhichTestsToRun
import org.scalatest.{Tag, WordSpecLike}

class BackOfficeBasicTests extends WordSpecLike with StringManipulator {
  "BackOffice Basic Tests" should {
    //if (!WhichTestsToRun().shouldRunBackOfficeSubscribersTests) pending
    "Login Test" taggedAs Tag("NotInTeamCity") in {
      Selenium.ResetSessions()
      Home.GoTo()
      Login.login("test")
    }

    "Logout Test" taggedAs Tag("NotInTeamCity") in {
      ui.BackOffice.Home.Logout()
    }
  }
}
