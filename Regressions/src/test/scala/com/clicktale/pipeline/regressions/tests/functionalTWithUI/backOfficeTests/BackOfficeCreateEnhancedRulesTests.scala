package com.clicktale.pipeline.regressions.tests.functionalTWithUI.backOfficeTests

import com.clicktale.pipeline.framework.ui.BackOffice.Subscribers
import com.clicktale.pipeline.framework.dal.GeneralFunctions
import com.clicktale.pipeline.framework.ui.{BackOffice, _}
import com.clicktale.pipeline.framework.helpers.StringManipulator
import com.clicktale.pipeline.framework.ui
import com.clicktale.pipeline.framework.ui.BackOffice._
import com.clicktale.pipeline.framework.ui.Selenium
import com.clicktale.pipeline.regressions.testHelpers.WhichTestsToRun
import org.scalatest.{Tag, WordSpecLike}

class BackOfficeCreateEnhancedRulesTests extends WordSpecLike with StringManipulator {
  "BackOffice CreateEnhancedRules Tests" should {
    if (!WhichTestsToRun().shouldRunBackOfficeSubscribersTests) pending
    Selenium.ResetSessions()
    Home.GoTo()
    ui.BackOffice.Login.login("test")
    ui.BackOffice.Home.ClickMenuItem("Create Subscriber")
    val idFull = ui.BackOffice.CreateSubscriber.SetID
    val nameFull = ui.BackOffice.CreateSubscriber.SetName
    ui.BackOffice.CreateSubscriber.ClickCreate
    Subscribers.ClickNewProject()
    val pidFull = CreateProject.SetID
    val pnameFull = ui.BackOffice.CreateProject.SetName
    val ratioFull = ui.BackOffice.CreateProject.SetRatio(0.1)
    val queueFull = ui.BackOffice.CreateProject.SetPartition()
    val ttlFull = ui.BackOffice.CreateProject.SetTTL()
    ui.BackOffice.CreateProject.ClickCreate
    ui.BackOffice.Home.ClickMenuItem("Create Subscriber")
    val idETR = ui.BackOffice.CreateSubscriber.SetID
    val nameETR = ui.BackOffice.CreateSubscriber.SetName
    ui.BackOffice.CreateSubscriber.ClickCreate
    ui.BackOffice.Subscribers.ClickNewProject()
    val pidETR = ui.BackOffice.CreateProject.SetID
    val pnameETR = ui.BackOffice.CreateProject.SetName
    val ratioETR = ui.BackOffice.CreateProject.SetRatio(0.1)
    val queueETR = ui.BackOffice.CreateProject.SetPartition()
    val ttlETR = ui.BackOffice.CreateProject.SetTTL()
    ui.BackOffice.CreateProject.ClickCreate

    "Create Basic Enhanced Rule from ETR Project" taggedAs Tag("NotInTeamCity") in {
      Selenium.ResetSessions()
      ui.BackOffice.Home.GoTo()
      ui.BackOffice.Login.login("test")
      ui.BackOffice.Home.ClickMenuItem("Projects")
      Projects.SetPID(pidETR)
      ui.BackOffice.Projects.ClickSearch()
      ui.BackOffice.Projects.ClickEnhancedRules()
      val url = CreateEnhancedRule.SetUrl()
      ui.BackOffice.CreateEnhancedRule.SetIgnore("No")
      val ratio = ui.BackOffice.CreateEnhancedRule.SetRatio()
      ui.BackOffice.CreateEnhancedRule.ClickAdd
      val urlRules = GeneralFunctions.getEnhancedRulesByProject(pidETR.toInt, idETR.toInt)
      assert(urlRules.length == 1)
      assert(urlRules.head.enabled)
      assert(!urlRules.head.ignore)
      assert(urlRules.head.ratio == ratio)
      assert(urlRules.head.uri == url)
      GeneralFunctions.deleteRule(urlRules.head.id.toString)
    }

    "Create Basic Enhanced Rule from Full Project" taggedAs Tag("NotInTeamCity") in {
      Selenium.ResetSessions()
      ui.BackOffice.Home.GoTo()
      ui.BackOffice.Login.login("test")
      ui.BackOffice.Home.ClickMenuItem("Projects")
      ui.BackOffice.Projects.SetPID(pidFull)
      ui.BackOffice.Projects.ClickSearch()
      ui.BackOffice.Projects.ClickEnhancedRules()
      val url = ui.BackOffice.CreateEnhancedRule.SetUrl()
      ui.BackOffice.CreateEnhancedRule.SetIgnore("No")
      val ratio = ui.BackOffice.CreateEnhancedRule.SetRatio()
      ui.BackOffice.CreateEnhancedRule.ClickAdd
      val urlRules = GeneralFunctions.getEnhancedRulesByProject(pidFull.toInt, idFull.toInt)
      assert(urlRules.length == 1)
      assert(urlRules.head.enabled)
      assert(!urlRules.head.ignore)
      assert(urlRules.head.ratio == ratio)
      assert(urlRules.head.uri == url)
      GeneralFunctions.deleteRule(urlRules.head.id.toString)
    }

    "Create Enhanced Rule from Full Project - Ignore" taggedAs Tag("NotInTeamCity") in {
      Selenium.ResetSessions()
      ui.BackOffice.Home.GoTo()
      ui.BackOffice.Login.login("test")
      ui.BackOffice.Home.ClickMenuItem("Projects")
      ui.BackOffice.Projects.SetPID(pidFull)
      ui.BackOffice.Projects.ClickSearch()
      ui.BackOffice.Projects.ClickEnhancedRules()
      val url = ui.BackOffice.CreateEnhancedRule.SetUrl()
      ui.BackOffice.CreateEnhancedRule.SetIgnore("Yes")
      val ratio = ui.BackOffice.CreateEnhancedRule.SetRatio()
      ui.BackOffice.CreateEnhancedRule.ClickAdd
      val urlRules = GeneralFunctions.getEnhancedRulesByProject(pidFull.toInt, idFull.toInt)
      assert(urlRules.length == 1)
      assert(urlRules.head.enabled)
      assert(urlRules.head.ignore)
      assert(urlRules.head.ratio == ratio)
      assert(urlRules.head.uri == url)
      GeneralFunctions.deleteRule(urlRules.head.id.toString)
    }

    "Create Enhanced Rule from ETR Project - Ratio 1" taggedAs Tag("NotInTeamCity") in {
      Selenium.ResetSessions()
      ui.BackOffice.Home.GoTo()
      ui.BackOffice.Login.login("test")
      ui.BackOffice.Home.ClickMenuItem("Projects")
      ui.BackOffice.Projects.SetPID(pidETR)
      ui.BackOffice.Projects.ClickSearch()
      ui.BackOffice.Projects.ClickEnhancedRules()
      val url = ui.BackOffice.CreateEnhancedRule.SetUrl()
      ui.BackOffice.CreateEnhancedRule.SetIgnore("No")
      val ratio = ui.BackOffice.CreateEnhancedRule.SetRatio(1)
      ui.BackOffice.CreateEnhancedRule.ClickAdd
      val urlRules = GeneralFunctions.getEnhancedRulesByProject(pidETR.toInt, idETR.toInt)
      assert(urlRules.length == 1)
      assert(urlRules.head.enabled)
      assert(!urlRules.head.ignore)
      assert(urlRules.head.ratio == 1)
      assert(urlRules.head.uri == url)
      GeneralFunctions.deleteRule(urlRules.head.id.toString)
      GeneralFunctions.deleteProject(pidFull)
      GeneralFunctions.deleteSubscriber(idFull)
      GeneralFunctions.deleteProject(pidETR)
      GeneralFunctions.deleteSubscriber(idETR)
    }

    "Create Basic Enhanced Rule from Full Project" taggedAs Tag("NotInTeamCity") in {
      Selenium.ResetSessions
      ui.BackOffice.Home.GoTo
      ui.BackOffice.Login.login("test")
      ui.BackOffice.Home.ClickMenuItem("Projects")
      ui.BackOffice.Projects.SetPID(pidFull)
      ui.BackOffice.Projects.ClickSearch
      ui.BackOffice.Projects.ClickEnhancedRules
      val url = ui.BackOffice.CreateEnhancedRule.SetUrl()
      ui.BackOffice.CreateEnhancedRule.SetIgnore("No")
      val ratio = ui.BackOffice.CreateEnhancedRule.SetRatio()
      BackOffice.CreateEnhancedRule.SetEndDate
      ui.BackOffice.CreateEnhancedRule.ClickAdd
      val urlRules = GeneralFunctions.getEnhancedRulesByProject(pidFull.toInt, idFull.toInt)
      assert(urlRules.length == 1)
      assert(urlRules(0).enabled == true)
      assert(urlRules(0).ignore == false)
      assert(urlRules(0).ratio == ratio)
      assert(urlRules(0).uri == url)
      GeneralFunctions.deleteRule(urlRules(0).id.toString)
    }

    "Create Enhanced Rule from Full Project - Ignore" taggedAs Tag("NotInTeamCity") in {
      Selenium.ResetSessions
      ui.BackOffice.Home.GoTo
      ui.BackOffice.Login.login("test")
      ui.BackOffice.Home.ClickMenuItem("Projects")
      ui.BackOffice.Projects.SetPID(pidFull)
      ui.BackOffice.Projects.ClickSearch
      ui.BackOffice.Projects.ClickEnhancedRules
      val url = ui.BackOffice.CreateEnhancedRule.SetUrl()
      ui.BackOffice.CreateEnhancedRule.SetIgnore("Yes")
      val ratio = ui.BackOffice.CreateEnhancedRule.SetRatio()
      BackOffice.CreateEnhancedRule.SetEndDate
      ui.BackOffice.CreateEnhancedRule.ClickAdd
      val urlRules = GeneralFunctions.getEnhancedRulesByProject(pidFull.toInt, idFull.toInt)
      assert(urlRules.length == 1)
      assert(urlRules(0).enabled == true)
      assert(urlRules(0).ignore == true)
      assert(urlRules(0).ratio == ratio)
      assert(urlRules(0).uri == url)
      GeneralFunctions.deleteRule(urlRules(0).id.toString)
    }

    "Create Enhanced Rule from ETR Project - Ratio 1" taggedAs Tag("NotInTeamCity") in {
      Selenium.ResetSessions
      ui.BackOffice.Home.GoTo
      ui.BackOffice.Login.login("test")
      ui.BackOffice.Home.ClickMenuItem("Projects")
      ui.BackOffice.Projects.SetPID(pidETR)
      ui.BackOffice.Projects.ClickSearch
      ui.BackOffice.Projects.ClickEnhancedRules
      val url = ui.BackOffice.CreateEnhancedRule.SetUrl()
      ui.BackOffice.CreateEnhancedRule.SetIgnore("No")
      val ratio = ui.BackOffice.CreateEnhancedRule.SetRatio(1)
      BackOffice.CreateEnhancedRule.SetEndDate
      ui.BackOffice.CreateEnhancedRule.ClickAdd
      val urlRules = GeneralFunctions.getEnhancedRulesByProject(pidETR.toInt, idETR.toInt)
      assert(urlRules.length == 1)
      assert(urlRules(0).enabled == true)
      assert(urlRules(0).ignore == false)
      assert(urlRules(0).ratio == 1)
      assert(urlRules(0).uri == url)
      GeneralFunctions.deleteRule(urlRules(0).id.toString)
      GeneralFunctions.deleteProject(pidFull)
      GeneralFunctions.deleteSubscriber(idFull)
      GeneralFunctions.deleteProject(pidETR)
      GeneralFunctions.deleteSubscriber(idETR)
    }
  }
}
