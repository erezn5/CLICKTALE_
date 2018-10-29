package com.clicktale.pipeline.framework.dal

import java.sql.ResultSet
import java.time.format.DateTimeFormatter

import com.clicktale.pipeline.dataObjects.{Credits, EnhancedRule, Project, Rule, Subscriber}
import org.joda.time.DateTime
import com.github.nscala_time.time.Imports._

import scala.collection.mutable.ListBuffer
import scalaj.http.Http

object GeneralFunctions {

  def createProject(Id:String, Ratio:Double=0.5, SubscriberId:String, Name:String, Type:String = "1", QueueId:Int = 33, Domains:String = "", LegacyProjectId:String = "", TTL:Int=60): Unit ={
    val res = CoreAdministrationSqlManager.Insert(s"INSERT INTO CoreAdministration.dbo.ProjectRecordingConfiguration (RecordingRatio, SubscriberId, ProjectId, ProjectName, ProcessingTypeId, QueueId, LegacyProjectId, TimeToLive, Domains, ImageFlagContentResponse, EnforceAllSensitive, CollectIpAddresses, AutoAddDomains, AllowPcRecording, AllowMobileRecording, AllowCustomTags, PublicId, IpBlockListJson, LastUpdatedDate, IsChangeMonitorEnabled, IsPersonalComputerAllowed, IsVisualEditorEnabled, IsAnyDomainAllowed, IsXhtmlCompliant, IsHooksEnabled, IsDefault, IsActive, IsAggregationOnCloudEnabled) VALUES (${Ratio.toFloat}, $SubscriberId, $Id, '$Name', $Type, $QueueId, $Id, $TTL, ${if(Domains=="") null else Domains}, 0, 0, 0, 0, 1, 1, 1, 'b080c6a4-b88d-4878-8fb0-53446f3e7f05', '', '${DateTime.now().toString()}', 1, 1, 1, 1, 0, 0, 0, 1, 1);")
  }

  def createCredit(project: Project, randomAmount:String, etrAmount:String, enhancedAmount:String): Unit ={
    val res = CoreRecordingsSqlManager.Insert(s"INSERT INTO CoreRecordings.dbo.ProjectCredits (SubscriberId, ProjectId, CreateDate, LastUpdate, RandomCreditAmount, EnhancedCreditAmount, EventTriggerCreditAmount) VALUES (${project.SubscriberId}, ${project.Id}, '${DateTime.now().toString()}', '${DateTime.now().toString()}', $randomAmount, $enhancedAmount, $etrAmount);")
  }

  def createIPBlockingRule(subsId:Int,projectId:Int,json:String):Unit = {
    val res = CoreAdministrationSqlManager.Insert(s"INSERT INTO CoreAdministration.dbo.ProjectIPBlockingConfigurations (SubscriberId, ProjectId, SerializedIPBlockingConfig) VALUES (${subsId}, ${projectId}, '${json}');")
  }

  def deleteIPBlockingRule(subsId:Int,projectId:Int):Unit = {
    val res = CoreAdministrationSqlManager.Insert(s"DELETE FROM CoreAdministration.dbo.ProjectIPBlockingConfigurations WHERE SubscriberId=$subsId And ProjectId=$projectId;")
  }

  def createCreditRule(project: Project): Unit ={
    val res = CoreRecordingsSqlManager.Insert(s"INSERT INTO CoreRecordings.dbo.ProjectCreditRules (SubscriberId, ProjectId, ProjectCreditAdditionTypeId, ProjectCreditRuleTypeId, EffectiveFrom, IntervalInHours, CreatedDate, IsEnabled, RandomCreditValue, EnhancedCreditValue, EventTriggerCreditValue) VALUES (${project.SubscriberId}, ${project.Id}, 1, 1, '${DateTime.now().toString()}', 24, '${DateTime.now().toString()}', 1, 1000, 1000, 1000);")
  }

  def createEnhancedRule(project: Project, url:String, isIgnore:Int = 1, ratio:Double = 1, ruleType:Int = 1, enabled:Int = 1): Unit ={
    val s1 = "\"Rule\""
    val s2 = "\"Enabled\""
    val res = CoreAdministrationSqlManager.Insert(s"INSERT INTO CoreAdministration.dbo.UrlRules (SubscriberId, ProjectId, Uri, IsIgnoreQString, Ratio, StartDate, EndDate, $s1, $s2) VALUES (${project.SubscriberId}, ${project.Id}, '${url}', ${isIgnore}, ${ratio} ,'${(DateTime.now() - 2.days).toString}', '${(DateTime.now()+ 2.days).toString()}', ${ruleType}, ${enabled});")
  }

  def getCreditRulesByProjectId(pid:Int):ResultSet = {
    CoreRecordingsSqlManager.Select(s"select * from CoreRecordings.dbo.ProjectCreditRules where ProjectId = $pid")
  }

  def getSubscribersByName(name : String): List[Subscriber] = {

    val res = CoreAdministrationSqlManager.Select(s"SELECT * FROM CoreAdministration.dbo.Subscribers where Name = '$name'")
    var ret = new ListBuffer[Subscriber]()
    while(res.next())
    {
      ret += new Subscriber(res.getString("SubscriberId"),res.getString("Name"),res.getInt("TotalQuota"),getProjectsBySubsId(res.getString("SubscriberId")))
    }
    ret.toList
  }

  def deleteSubscriber(id : String): Unit = {
    try {
      CoreAdministrationSqlManager.Delete(s"DELETE FROM CoreAdministration.dbo.Subscribers where SubscriberId = '$id'")
    } catch {
      case unknown: Throwable => println("Could not delete from DB")
    }
  }

  def deleteProject(id : String): Unit = {
    try {
      CoreAdministrationSqlManager.Delete(s"DELETE FROM CoreAdministration.dbo.ProjectRecordingConfiguration where ProjectId = '$id'")
    } catch {
      case unknown: Throwable => println("Could not delete from DB")
    }
  }

  def deleteProjectRange(lowId : String, highId : String): Unit = {
    try {
      CoreAdministrationSqlManager.Delete(s"DELETE FROM CoreAdministration.dbo.ProjectRecordingConfiguration where ProjectId > $lowId and ProjectId < $highId ")
    } catch {
      case unknown: Throwable => println("Could not delete from DB")
    }
  }

  def deleteCreditRuleRange(lowId : String, highId : String): Unit = {
    try {
      CoreRecordingsSqlManager.Delete(s"DELETE FROM CoreRecordings.dbo.ProjectCreditRules where ProjectId > $lowId and ProjectId < $highId ")
    } catch {
      case unknown: Throwable => println("Could not delete from DB")
    }
  }

  def deleteEnhancedRuleRange(lowId : String, highId : String): Unit = {
    try {
      CoreRecordingsSqlManager.Delete(s"DELETE FROM CoreAdministration.dbo.UrlRules where ProjectId > $lowId and ProjectId < $highId ")
    } catch {
      case unknown: Throwable => println("Could not delete from DB")
    }
  }

  def deleteCreditRange(lowId : String, highId : String): Unit = {
    try {
      CoreRecordingsSqlManager.Delete(s"DELETE FROM CoreRecordings.dbo.ProjectCredits where ProjectId > $lowId and ProjectId < $highId ")
    } catch {
      case unknown: Throwable => println("Could not delete from DB")
    }
  }

  def deleteRule(id : String): Unit = {
    try {
      CoreAdministrationSqlManager.Delete(s"DELETE FROM CoreAdministration.dbo.UrlRules where RuleId = '$id'")
    } catch {
      case unknown: Throwable => println("Could not delete from DB")
    }
  }

  def updateRule(id:String, attrName:String, attrVal:String, attrType:String): Unit ={
    if (attrType!="String" && attrType!="DateTime")
      CoreAdministrationSqlManager.Update(s"UPDATE CoreAdministration.dbo.UrlRules SET $attrName=$attrVal WHERE RuleId='$id';")
    else
      CoreAdministrationSqlManager.Update(s"UPDATE CoreAdministration.dbo.UrlRules SET $attrName='$attrVal' WHERE RuleId='$id';")
  }

  def updateProject(id:String, attrName:String, attrVal:String, attrType:String): Unit ={
    if (attrType!="String" && attrType!="DateTime")
      CoreAdministrationSqlManager.Update(s"UPDATE CoreAdministration.dbo.ProjectRecordingConfiguration SET $attrName=$attrVal WHERE ProjectId='$id';")
    else
      CoreAdministrationSqlManager.Update(s"UPDATE CoreAdministration.dbo.ProjectRecordingConfiguration SET $attrName='$attrVal' WHERE ProjectId='$id';")
  }

  def updateProjectsEncryptionBySubscriber(id:String): Unit ={

      CoreAdministrationSqlManager.Update(s"update CoreAdministration.dbo.ProjectRecordingConfiguration set EncryptedProjectKey = EncryptByPassPhrase('NoEncryptionPhraseHere!', 'NoEncryptionPhraseHere!') where SubscriberId = $id")

  }

  def updateSubscriber(id:String, attrName:String, attrVal:String, attrType:String): Unit ={
    if (attrType!="String" && attrType!="DateTime")
      CoreAdministrationSqlManager.Update(s"UPDATE CoreAdministration.dbo.Subscribers SET $attrName=$attrVal WHERE SubscriberId='$id';")
    else
      CoreAdministrationSqlManager.Update(s"UPDATE CoreAdministration.dbo.Subscribers SET $attrName='$attrVal' WHERE SubscriberId='$id';")
  }

  def getProjectsById(id:String): List[Project] ={
    val res = CoreAdministrationSqlManager.Select(s"SELECT * FROM CoreAdministration.dbo.ProjectRecordingConfiguration where ProjectId = '$id'")
    var ret = new ListBuffer[Project]
    while(res.next())
    {
      ret += new Project(res.getString("ProjectId"),res.getDouble("RecordingRatio"),res.getString("SubscriberId"),res.getString("ProjectName"),res.getString("ProcessingTypeId"),res.getInt("QueueId"),res.getString("Domains"),res.getString("LegacyProjectId"),res.getInt("TimeToLive"),getRulesByProject(id))
    }
    ret.toList
  }

  def getProjectLine(id:String) : ResultSet= {
    CoreAdministrationSqlManager.Select(s"SELECT * FROM CoreAdministration.dbo.ProjectRecordingConfiguration where ProjectId = '$id'")
  }

  def getCreditRuleLine(id:String) : ResultSet= {
    CoreAdministrationSqlManager.Select(s"SELECT * FROM CoreAdministration.dbo.ProjectRecordingConfiguration where ProjectId = '$id'")
  }

  def getProjectsByName(name:String): List[Project] ={
    val res = CoreAdministrationSqlManager.Select(s"SELECT * FROM CoreAdministration.dbo.ProjectRecordingConfiguration where Name = '$name'")
    var ret = new ListBuffer[Project]
    while(res.next())
    {
      ret += new Project(res.getString("ProjectId"),res.getDouble("RecordingRatio"),res.getString("SubscriberId"),res.getString("ProjectName"),res.getString("ProcessingTypeId"),res.getInt("QueueId"),res.getString("Domains"),res.getString("LegacyProjectId"),res.getInt("TimeToLive"),getRulesByProject(res.getString("ProjectId")))
    }
    ret.toList
  }

  def getProjectsCount:Int = {
    val res = CoreAdministrationSqlManager.Select(s"SELECT count(*) FROM CoreAdministration.dbo.ProjectRecordingConfiguration")
    res.next()
    res.getInt(1)
  }

  def getProjectsBySubsId(id:String): List[Project] ={
    val res = CoreAdministrationSqlManager.Select(s"SELECT * FROM CoreAdministration.dbo.ProjectRecordingConfiguration where SubscriberId = '$id'")
    var ret = new ListBuffer[Project]
    while(res.next())
    {
      ret += new Project(res.getString("ProjectId"),res.getDouble("RecordingRatio"),res.getString("SubscriberId"),res.getString("ProjectName"),res.getString("ProcessingTypeId"),res.getInt("QueueId"),res.getString("Domains"),res.getString("LegacyProjectId"),res.getInt("TimeToLive"),getRulesByProject(id))
    }
    ret.toList
  }

  def getRuleById(id:String): Rule ={
    val res = CoreAdministrationSqlManager.Select(s"SELECT * FROM CoreAdministration.dbo.UrlRules where RuleId = '$id'")
    res.next()
    new Rule(res.getString("RuleId"),res.getString("ProjectId"),res.getDouble("RecordingRatio"),res.getString("Uri"),res.getInt("Rule"),res.getInt("Enabled"))
  }

  def getRulesByProject(id:String): List[Rule] ={
    val res = CoreAdministrationSqlManager.Select(s"SELECT * FROM CoreAdministration.dbo.UrlRules where ProjectId = '$id'")
    var ret = new ListBuffer[Rule]
    while(res.next())
    {
      ret += new Rule(res.getString("RuleId"),res.getString("ProjectId"),res.getDouble("Ratio"),res.getString("Uri"),res.getInt("Rule"),res.getInt("Enabled"))
    }
    ret.toList
  }

  def getSubscriberById(id:String): Subscriber ={
    val res = CoreAdministrationSqlManager.Select(s"SELECT * FROM CoreAdministration.dbo.Subscribers where SubscriberId = '$id'")
    res.next()
    new Subscriber(res.getString("SubscriberId"),res.getString("Name"),res.getInt("TotalQuota"),getProjectsBySubsId(id))
  }

  def getCreditsByProject(pid:Int, subsId:Int): Credits ={
    val res = CoreRecordingsSqlManager.Select(s"select * from CoreRecordings.dbo.ProjectCredits where ProjectId = $pid and SubscriberId = $subsId")
    res.next()
    Credits(res.getInt("SubscriberId"), res.getInt("ProjectId"), res.getLong("RandomCreditAmount"), res.getLong("EnhancedCreditAmount"), res.getLong("EventTriggerCreditAmount"))
  }

  def getEnhancedRulesByProject(pid:Int, subsId:Int): List[EnhancedRule] ={
    val res = CoreAdministrationSqlManager.Select(s"select * from CoreAdministration.dbo.UrlRules where ProjectId = $pid and SubscriberId = $subsId")
    var ret = new ListBuffer[EnhancedRule]
    while(res.next())
    {
      ret += new EnhancedRule(res.getInt("RuleId"),res.getInt("SubscriberId"),res.getLong("ProjectId"),res.getString("Uri"),res.getBoolean("IsIgnoreQString"),res.getDouble("Ratio"),res.getBoolean("Enabled"))
    }
    ret.toList
  }

  def getRulesCount(pid:Int, subsId:Int, enabledStatus:Int): Int ={
    val res = CoreAdministrationSqlManager.Select(s"select count (*) from [CoreAdministration].[dbo].[UrlRules] where ProjectId = $pid and SubscriberId = $subsId and enabled = $enabledStatus")
    res.next()
    res.getInt("")
  }

  def setCreditsToProject(id:Int, queueName:String, amount:Int): Unit =
  {
    CoreRecordingsSqlManager.Update(s"""UPDATE CoreRecordings.dbo.ProjectCredits SET ${queueName}CreditAmount=$amount WHERE ProjectId=$id;""")
  }

  def setRatioToProject(id:Int, amount:Double): Unit ={
    CoreAdministrationSqlManager.Update(s"UPDATE CoreAdministration.dbo.ProjectRecordingConfiguration SET RecordingRatio=$amount WHERE ProjectId='$id';")
  }

  def getProjectByIdAndSubsId(subsId:Int, projectId:Int): Project =
  {
    val res = CoreAdministrationSqlManager.Select(s"SELECT * FROM CoreAdministration.dbo.ProjectRecordingConfiguration where SubscriberId = '$subsId' and ProjectId = '$projectId'")
    res.next()
    new Project(res.getString("ProjectId"),res.getDouble("RecordingRatio"),res.getString("SubscriberId"),res.getString("ProjectName"),res.getString("ProcessingTypeId"),res.getInt("QueueId"),res.getString("Domains"),res.getString("LegacyProjectId"),res.getInt("TimeToLive"),getRulesByProject(projectId.toString))
  }

  def CompareLines(lineBefore:ResultSet, lineAfter:ResultSet, attName:String, attVal:String) = {
      val beforeMD = lineBefore.getMetaData
      while(lineBefore.next()) {
      lineAfter.next()
      var i = 1
      while (i<=beforeMD.getColumnCount){
        if (beforeMD.getColumnLabel(i)==attName || beforeMD.getColumnLabel(i)=="LastUpdatedDate"){
          assert(lineBefore.getString(i)!=lineAfter.getString(i))
          if (beforeMD.getColumnLabel(i)==attName)
            assert(lineAfter.getString(attName)==attVal)
        }
        else
          assert(lineBefore.getString(i)==lineAfter.getString(i))
        i+=1
      }
    }
  }
  def tryHttpResponse(url: String, numOfTries: Int = 10): String = {
    val tries = 10
    val sleep =10000
    if (numOfTries < tries) {
      Thread.sleep(sleep)
    }
    try {
      Http(url).asString.body
    }
    catch {
      case e: Exception if numOfTries > 1 => tryHttpResponse(url, numOfTries - 1)
      case x: Throwable => throw x
    }
  }

}



