package com.clicktale.pipeline.regressions.tests.end2EndTests

import java.io.{File, FileWriter}
import java.lang.{System, _}

import com.clicktale.pipeline.dataObjects.{Project, Subscriber, _}
import com.clicktale.pipeline.framework.dal.ConfigParser.conf
import com.clicktale.pipeline.framework.dal.GeneralFunctions
import com.clicktale.pipeline.framework.helpers.{CsvReader, StringManipulator}
import com.clicktale.pipeline.framework.senders.SendSession._
import com.clicktale.pipeline.framework.storage.GetFromS3.loadFromCage
import com.clicktale.pipeline.regressions.testHelpers.WhichTestsToRun
import org.apache.commons.io.FileUtils
import org.scalatest.{Tag, WordSpecLike}

import scala.xml.{Elem, XML}

class QuotaManagementBasicTests extends WordSpecLike with StringManipulator {

  val retryFailed: Boolean = false //*** Change this to run only retries ***
  val testFile: String = if(retryFailed) "BasicEtrRandomEnhancedResults.csv" else System.getProperty("user.dir")+"\\lib\\BasicETR_Random_Enhanced.csv"
  val resultsFile: String = if(retryFailed) "BasicEtrRandomEnhancedResultsRetry.csv" else "BasicEtrRandomEnhancedResults.csv"

  var dynamicSession:AuthResponse = _ //was null
  var subscriber1: Subscriber = GeneralFunctions.getSubscriberById(conf.getString(s"WebRecorder.Environments.${conf.getString("WebRecorder.Current.Environment")}.QuotaSubsId1"))
  var subscriber2: Subscriber = GeneralFunctions.getSubscriberById(conf.getString(s"WebRecorder.Environments.${conf.getString("WebRecorder.Current.Environment")}.QuotaSubsId2"))

  GeneralFunctions.deleteProjectRange("666000","668000")
  GeneralFunctions.deleteCreditRuleRange("666000","668000")
  GeneralFunctions.deleteCreditRange("666000","668000")
  GeneralFunctions.deleteEnhancedRuleRange("666000","668000")

  "Basic ETR/Random/Enhanced" taggedAs Tag("NotInTeamCity") in {
    //if (!WhichTestsToRun().shouldRunQuotaManagementBasicTests) pending
    val r = scala.util.Random
    FileUtils.deleteQuietly(new File(resultsFile))
    val fw = new FileWriter(resultsFile, true)
    var retStr = "Test Cases Failed:\n"
    var index = 1
    CsvReader.GetAllLines(testFile).foreach(line => {
      if (index==3 && !line.contains("bug")) {
        GeneralFunctions.createProject(Id = (666000 + index).toString, Ratio = line.split(',')(5).toDouble, SubscriberId = conf.getString(s"WebRecorder.Environments.${conf.getString("WebRecorder.Current.Environment")}.QuotaSubsId${if (line.split(',')(0).contains("Random")) 1 else 2}"), Name = conf.getString("WebRecorder.TestParams.BackOffice.Subscriber.Name") + " " + r.nextInt(10000).toString, Type = if (line.split(',')(0).contains("Random")) "1" else "2", LegacyProjectId = (66600 + index).toString)
        var selectedProject: Project = GeneralFunctions.getProjectsById((666000 + index).toString).head
        GeneralFunctions.createCreditRule(selectedProject)

        if (line.split(',')(4) != "New") {
          if (line.split(',')(4) == "Recording") {
            GeneralFunctions.createCredit(selectedProject,"1000","0","0")
            GeneralFunctions.setRatioToProject(selectedProject.Id.toInt, 1)
          }

          if (line.split(',')(4) == "ETRecording") {
            GeneralFunctions.createCredit(selectedProject,"0","1000","0")
            GeneralFunctions.setRatioToProject(selectedProject.Id.toInt, 1)
          }

          if (line.split(',')(4) == "EnhancedRecording") {
            GeneralFunctions.createCredit(selectedProject,"0","0","1000")
            GeneralFunctions.setRatioToProject(selectedProject.Id.toInt, 1)
            GeneralFunctions.setRatioToProject(selectedProject.Id.toInt, 1)
          }
        }
        else {
          GeneralFunctions.createCredit(selectedProject,"0","0","0")
        }

        if (line.split(',')(0).contains("Enhanced")) {
          GeneralFunctions.createEnhancedRule(selectedProject, "http://www.test.com")
        }
      }
      else if (index==1)
      {
        fw.write(line + "\n")
      }
      index+=1
    })
    val maxIndex = index

    GeneralFunctions.updateProjectsEncryptionBySubscriber("666")
    GeneralFunctions.updateProjectsEncryptionBySubscriber("667")
    Thread.sleep(60000)

    var sessionArr = new Array[AuthResponse](maxIndex)
    index = 1
    CsvReader.GetAllLines(testFile).foreach(line => {
      if (index==3 && !line.contains("bug")) {
        var selectedProject =GeneralFunctions.getProjectsById((666000 + index).toString).head
        if (line.split(',')(4) != "New") {

          val initialState = line.split(',')(4)
          if (initialState != "ETRecording") {
            var preSession = getAuth(conf.getString(s"WebRecorder.Session.States.$initialState"), optionalPid = selectedProject.Id.toInt,referrer = "http://www.test.com")
            sessionArr(index) = preSession
          }
          else {
            var preSession = pushSession("/recFiles/recEvent.json", conf.getString("WebRecorder.Session.States.ETRPending"), optionalPid = selectedProject.Id.toInt,referrer = "http://www.test.com")
            var testSession = getAuthForUID(preSession.uid.toLong, optionalPid = selectedProject.Id.toInt, optionalTrackingState = conf.getString("WebRecorder.Session.States.ETRecording"), referrer = "http://www.test.com")
            sessionArr(index) = testSession
            if (testSession.userTrackingState != conf.getString("WebRecorder.Session.States.ETRecording"))
              {
                testSession = getAuthForUID(preSession.uid.toLong, optionalPid = selectedProject.Id.toInt,optionalTrackingState = conf.getString("WebRecorder.Session.States.ETRecording"),referrer = "http://www.test.com")
                sessionArr(index) = testSession
              }
            assert(testSession.userTrackingState == conf.getString("WebRecorder.Session.States.ETRecording"), "User tracking state was not changed properly ")
          }
        }
        if (line.split(',')(1) == "Yes")
          GeneralFunctions.setCreditsToProject(selectedProject.Id.toInt, "Random", 1000)
        else
          GeneralFunctions.setCreditsToProject(selectedProject.Id.toInt, "Random", 0)

        if (line.split(',')(2) == "Yes")
          GeneralFunctions.setCreditsToProject(selectedProject.Id.toInt, "EventTrigger", 1000)
        else
          GeneralFunctions.setCreditsToProject(selectedProject.Id.toInt, "EventTrigger", 0)

        if (line.split(',')(3) == "Yes")
          GeneralFunctions.setCreditsToProject(selectedProject.Id.toInt, "Enhanced", 1000)
        else
          GeneralFunctions.setCreditsToProject(selectedProject.Id.toInt, "Enhanced", 0)

        GeneralFunctions.setRatioToProject(selectedProject.Id.toInt, line.split(',')(5).toDouble)
      }
      index+=1
    })
    Thread.sleep(60000)

    index = 1
    CsvReader.GetAllLines(testFile).foreach(line => {
      if (index==3 && !line.contains("bug")){
          try {
            var selectedProject = GeneralFunctions.getProjectsById((666000 + index).toString).head
                       //val testSession = getAuthForUID(sessionArr(index).uid.toLong)
            if(line.split(',')(5).toDouble > 0 && line.split(',')(5).toDouble < 1)
              dynamicSession = if (line.split(',')(4) == "New") pushSession("/recFiles/recEvent.json", conf.getString(s"WebRecorder.Session.States.${line.split(',')(9)}"), optionalPid = selectedProject.Id.toInt,referrer = "http://www.test.com") else pushSessionForUID("/recFiles/recEvent.json", sessionArr(index).uid.toLong,optionalTrackingState = conf.getString(s"WebRecorder.Session.States.${line.split(',')(9)}"), optionalPid = selectedProject.Id.toInt, subsId = selectedProject.SubscriberId.toInt,referrer = "http://www.test.com")
            else
              dynamicSession = if (line.split(',')(4) == "New") pushSession("/recFiles/recEvent.json", conf.getString(s"WebRecorder.Session.States.${line.split(',')(9)}"), optionalPid = selectedProject.Id.toInt,retry = false,referrer = "http://www.test.com") else pushSessionForUID("/recFiles/recEvent.json", sessionArr(index).uid.toLong,optionalTrackingState = conf.getString(s"WebRecorder.Session.States.${line.split(',')(9)}"), optionalPid = selectedProject.Id.toInt, subsId = selectedProject.SubscriberId.toInt,retry = false,referrer = "http://www.test.com")
            assert(dynamicSession.userTrackingState == conf.getString(s"WebRecorder.Session.States.${line.split(',')(9)}"))
            assert(dynamicSession.authorized == (line.split(',')(7) == "Pass"),s"authorized is not ${line.split(',')(7) == "Pass"}")
            if (dynamicSession.userTrackingState != "NotRecording")
              assert(dynamicSession.ratio == line.split(',')(5).toFloat,s"ratio is not ${line.split(',')(5).toFloat}")
            assert(dynamicSession.rejectReason==line.split(',')(8).replace('_',','),s"rejectReason is not ${line.split(',')(8).replace('_',',')}")

            if (line.split(',')(9)=="Recording" || line.split(',')(9)=="ETRecording" || line.split(',')(9)=="EnhancedRecording") {
              val archivePackage: CageArchivePackage = loadFromCage(selectedProject.SubscriberId.toInt,selectedProject.Id.toInt, dynamicSession.sid.toLong,2)
              val recJson: RecordingJson = RecordingJson(archivePackage.recording)
              val recXml: Elem = XML.loadString(archivePackage.events)
              //val recDsr: Elem = XML.loadString(archivePackage.streams)
              assert(recJson.SID == dynamicSession.sid.toLong, "S3 does not contain " + dynamicSession.sid)
              val popType = if(line.split(',')(9)=="Recording") null else if(line.split(',')(9)=="ETRecording") 1 else 2
              if (popType==null)
                assert(recJson.populationType==popType)
              else
                assert((recJson.populationType==popType.toString) || recJson.populationType==Some(popType).get,s"Population Type is not $popType")
              assert(recXml != null)
              //assert(recDsr != null)
            }
          }
          catch {
            case e: Exception =>
              retStr += (index.toString + ":\n" + e.getMessage + "\n")
              fw.write(line+","+index.toString+": "+ e.getMessage.replace(',','_').replace('\n','_') + "\n")
          }
        }
      index+=1
    })

    Thread.sleep(60000)
    index = 1
    CsvReader.GetAllLines(testFile).foreach(line => {
      if (index==3 && !line.contains("bug")) {
        try {
          var selectedProject = GeneralFunctions.getProjectsById((666000 + index).toString).head
          val credits = GeneralFunctions.getCreditsByProject(selectedProject.Id.toInt,selectedProject.SubscriberId.toInt)
          if (line.split(',')(11)=="None") {
            if (line.split(',')(9) == "Recording")
              assert(credits.RandomCreditAmount == 1000)
            if (line.split(',')(9) == "ETRecording")
              assert(credits.EventTriggerCreditAmount == 1000)
            if (line.split(',')(9) == "EnhancedRecording")
              assert(credits.EnhancedCreditAmount == 1000)
          }
          else{
            if (line.split(',')(11) == "Random")
              assert(credits.RandomCreditAmount > 0 && credits.RandomCreditAmount < 1000)
            if (line.split(',')(11) == "ETR")
              assert(credits.EventTriggerCreditAmount > 0 && credits.EventTriggerCreditAmount < 1000)
            if (line.split(',')(11) == "Enhanced")
              assert(credits.EnhancedCreditAmount > 0 && credits.EnhancedCreditAmount < 1000)
          }
        }
        catch {
          case e: Exception =>
            retStr += (index.toString + ":\n" + e.getMessage + "\n")
            fw.write(line+","+index.toString+": "+ e.getMessage.replace(',','_').replace('\n','_') + "\n")

        }
      }
    })

    fw.close()
    if (retStr.length>20)
      throw new Exception(retStr)
    else {
      GeneralFunctions.deleteProjectRange("666000","668000")
      GeneralFunctions.deleteCreditRuleRange("666000","668000")
      GeneralFunctions.deleteCreditRange("666000","668000")
      GeneralFunctions.deleteEnhancedRuleRange("666000","668000")
    }
  }
}
