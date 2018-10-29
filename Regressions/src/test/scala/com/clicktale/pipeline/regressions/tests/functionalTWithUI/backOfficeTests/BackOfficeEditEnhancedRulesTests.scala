package com.clicktale.pipeline.regressions.tests.functionalTWithUI.backOfficeTests

import com.clicktale.pipeline.framework.dal.GeneralFunctions
import com.clicktale.pipeline.framework.ui.{BackOffice, _}
import com.clicktale.pipeline.framework.helpers.StringManipulator
import com.clicktale.pipeline.framework.ui.BackOffice._
import com.clicktale.pipeline.framework.ui.Selenium
import com.clicktale.pipeline.regressions.testHelpers.WhichTestsToRun
import org.scalatest.{Tag, WordSpecLike}

class BackOfficeEditEnhancedRulesTests extends WordSpecLike with StringManipulator {
  "BackOffice EditEnhanced Tests" should {
    if (!WhichTestsToRun().shouldRunBackOfficeSubscribersTests) pending
    Selenium.ResetSessions
    Home.GoTo
    BackOffice.Login.login("test")
    BackOffice.Home.ClickMenuItem("Create Subscriber")
    val idFull = BackOffice.CreateSubscriber.SetID
    val nameFull = BackOffice.CreateSubscriber.SetName
    BackOffice.CreateSubscriber.ClickCreate
    Subscribers.ClickNewProject
    val pidFull = CreateProject.SetID
    val pnameFull = BackOffice.CreateProject.SetName
    val ratioFull = BackOffice.CreateProject.SetRatio(0.1)
    val queueFull = BackOffice.CreateProject.SetPartition()
    val ttlFull = BackOffice.CreateProject.SetTTL()
    BackOffice.CreateProject.ClickCreate
    Projects.ClickEnhancedRules
    CreateEnhancedRule.SetUrl()
    BackOffice.CreateEnhancedRule.SetIgnore("No")
    BackOffice.CreateEnhancedRule.SetRatio()
    BackOffice.CreateEnhancedRule.SetEndDate
    BackOffice.CreateEnhancedRule.ClickAdd
    BackOffice.Home.ClickMenuItem("Create Subscriber")
    val idETR = BackOffice.CreateSubscriber.SetID
    val nameETR = BackOffice.CreateSubscriber.SetName
    BackOffice.CreateSubscriber.ClickCreate
    BackOffice.Subscribers.ClickNewProject
    val pidETR = BackOffice.CreateProject.SetID
    val pnameETR = BackOffice.CreateProject.SetName
    val ratioETR = BackOffice.CreateProject.SetRatio(0.1)
    val queueETR = BackOffice.CreateProject.SetPartition()
    val ttlETR = BackOffice.CreateProject.SetTTL()
    BackOffice.CreateProject.ClickCreate
    BackOffice.Projects.ClickEnhancedRules
    BackOffice.CreateEnhancedRule.SetUrl()
    BackOffice.CreateEnhancedRule.SetIgnore("No")
    BackOffice.CreateEnhancedRule.SetRatio()
    BackOffice.CreateEnhancedRule.SetEndDate
    BackOffice.CreateEnhancedRule.ClickAdd

    "Edit Url ETR" taggedAs Tag("NotInTeamCity") in {
      Selenium.ResetSessions
      BackOffice.Home.GoTo
      BackOffice.Login.login("test")
      BackOffice.Home.ClickMenuItem("Projects")
      BackOffice.Projects.SetPID(pidETR)
      BackOffice.Projects.ClickSearch
      BackOffice.Projects.ClickEnhancedRules
      val id = GeneralFunctions.getEnhancedRulesByProject(pidETR.toInt, idETR.toInt)(0).id
      BackOffice.CreateEnhancedRule.ClickEditRule(id.toString())
      BackOffice.CreateEnhancedRule.SetUrl("http://www.google.com")
      BackOffice.CreateEnhancedRule.SetEndDate
      BackOffice.CreateEnhancedRule.ClickAdd
      assert(GeneralFunctions.getEnhancedRulesByProject(pidETR.toInt, idETR.toInt)(0).uri == "http://www.google.com")
      BackOffice.CreateEnhancedRule.CheckAllTables(idETR, pidETR)
    }

    "Edit Url Full" taggedAs Tag("NotInTeamCity") in {
      Selenium.ResetSessions
      BackOffice.Home.GoTo
      BackOffice.Login.login("test")
      BackOffice.Home.ClickMenuItem("Projects")
      BackOffice.Projects.SetPID(pidFull)
      BackOffice.Projects.ClickSearch
      BackOffice.Projects.ClickEnhancedRules
      val id = GeneralFunctions.getEnhancedRulesByProject(pidFull.toInt, idFull.toInt)(0).id
      BackOffice.CreateEnhancedRule.ClickEditRule(id.toString())
      BackOffice.CreateEnhancedRule.SetUrl("http://www.google.com")
      BackOffice.CreateEnhancedRule.SetEndDate
      BackOffice.CreateEnhancedRule.ClickAdd
    }

      "Edit Url ETR" taggedAs Tag("NotInTeamCity") in {
        Selenium.ResetSessions()
        BackOffice.Home.GoTo()
        BackOffice.Login.login("test")
        BackOffice.Home.ClickMenuItem("Projects")
        BackOffice.Projects.SetPID(pidETR)
        BackOffice.Projects.ClickSearch()
        BackOffice.Projects.ClickEnhancedRules()
        val id = GeneralFunctions.getEnhancedRulesByProject(pidETR.toInt, idETR.toInt).head.id
        BackOffice.CreateEnhancedRule.ClickEditRule(id.toString)
        BackOffice.CreateEnhancedRule.SetUrl("http://www.google.com")
        BackOffice.CreateEnhancedRule.ClickAdd
        assert(GeneralFunctions.getEnhancedRulesByProject(pidETR.toInt, idETR.toInt).head.uri == "http://www.google.com")
        BackOffice.CreateEnhancedRule.CheckAllTables(idETR, pidETR)
      }

      "Edit Url Full" taggedAs Tag("NotInTeamCity") in {
        Selenium.ResetSessions()
        BackOffice.Home.GoTo()
        BackOffice.Login.login("test")
        BackOffice.Home.ClickMenuItem("Projects")
        BackOffice.Projects.SetPID(pidFull)
        BackOffice.Projects.ClickSearch()
        BackOffice.Projects.ClickEnhancedRules()
        val id = GeneralFunctions.getEnhancedRulesByProject(pidFull.toInt, idFull.toInt).head.id
        BackOffice.CreateEnhancedRule.ClickEditRule(id.toString)
        BackOffice.CreateEnhancedRule.SetUrl("http://www.google.com")
        BackOffice.CreateEnhancedRule.ClickAdd
        assert(GeneralFunctions.getEnhancedRulesByProject(pidFull.toInt, idFull.toInt).head.uri == "http://www.google.com")
        BackOffice.CreateEnhancedRule.CheckAllTables(idFull, pidFull)
      }

      "Edit Ratio ETR" taggedAs Tag("NotInTeamCity") in {
        Selenium.ResetSessions
        BackOffice.Home.GoTo
        BackOffice.Login.login("test")
        BackOffice.Home.ClickMenuItem("Projects")
        BackOffice.Projects.SetPID(pidETR)
        BackOffice.Projects.ClickSearch
        BackOffice.Projects.ClickEnhancedRules
        val id = GeneralFunctions.getEnhancedRulesByProject(pidETR.toInt, idETR.toInt)(0).id
        BackOffice.CreateEnhancedRule.ClickEditRule(id.toString())
        BackOffice.CreateEnhancedRule.SetRatio(0.2)
        BackOffice.CreateEnhancedRule.SetEndDate
        BackOffice.CreateEnhancedRule.ClickAdd
        assert(GeneralFunctions.getEnhancedRulesByProject(pidETR.toInt, idETR.toInt)(0).ratio == 0.2)
        BackOffice.CreateEnhancedRule.CheckAllTables(idETR, pidETR)
      }

      "Edit Ratio Full" taggedAs Tag("NotInTeamCity") in {
        Selenium.ResetSessions
        BackOffice.Home.GoTo
        BackOffice.Login.login("test")
        BackOffice.Home.ClickMenuItem("Projects")
        BackOffice.Projects.SetPID(pidFull)
        BackOffice.Projects.ClickSearch
        BackOffice.Projects.ClickEnhancedRules
        val id = GeneralFunctions.getEnhancedRulesByProject(pidFull.toInt, idFull.toInt)(0).id
        BackOffice.CreateEnhancedRule.ClickEditRule(id.toString())
        BackOffice.CreateEnhancedRule.SetRatio(0.2)
        BackOffice.CreateEnhancedRule.SetEndDate
        BackOffice.CreateEnhancedRule.ClickAdd
        assert(GeneralFunctions.getEnhancedRulesByProject(pidFull.toInt, idFull.toInt)(0).ratio == 0.2)
        BackOffice.CreateEnhancedRule.CheckAllTables(idFull, pidFull)
        GeneralFunctions.deleteRule(GeneralFunctions.getEnhancedRulesByProject(pidFull.toInt, idFull.toInt)(0).id.toString)
        GeneralFunctions.deleteRule(GeneralFunctions.getEnhancedRulesByProject(pidETR.toInt, idETR.toInt)(0).id.toString)
        GeneralFunctions.deleteProject(pidFull)
        GeneralFunctions.deleteSubscriber(idFull)
        GeneralFunctions.deleteProject(pidETR)
        GeneralFunctions.deleteSubscriber(idETR)
      }
    }
}
