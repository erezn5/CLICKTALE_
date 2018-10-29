package com.clicktale.pipeline.regressions.tests.ProdTests

import java.io.ByteArrayInputStream
import java.sql.DriverManager
import java.util
import java.util.zip.ZipInputStream

import System.IO.File
import com.clicktale.pipeline.framework.dal.ConfigParser.conf
import com.clicktale.pipeline.framework.dal.{CoreAdministrationSqlManager, Vertica}
import com.clicktale.pipeline.regressions.tests.functionalTests.procRelatedTests.ArchiveEntry
import com.clicktale.pipeline.storagemaster.logic.archives.binary.FullSessionBinarySerializer
import com.clicktale.pipeline.storagemaster.logic.archives.binary.FullSessionBinarySerializer.StringBinaryDeserialized
import org.apache.commons.io.IOUtils
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.ByteArrayEntity
import org.apache.http.impl.client.HttpClientBuilder
import org.joda.time.DateTime
import org.scalatest._
//import com.clicktale.pipeline.common.dal.storage.ArchiveEntry
//import com.gargoylesoftware.htmlunit.javascript.host.arrays.ArrayBuffer

import com.clicktale.pipeline.framework.dal.GeneralFunctions._

class TempRetentionStabilityTest extends WordSpecLike with BeforeAndAfterAll {
  val amountOfIterations = 2
  "Check retention Prod US" taggedAs (Tag.apply("US")) in {
    var failFlag = false
    CoreAdministrationSqlManager.driver = conf.getString(s"WebRecorder.Sql.ProdUS.CoreAdministration.Driver")
    CoreAdministrationSqlManager.url = s"jdbc:sqlserver://${conf.getString(s"WebRecorder.Sql.ProdUS.CoreAdministration.DataSource")}\\${conf.getString(s"WebRecorder.Sql.ProdUS.CoreAdministration.DataTable")}:${conf.getString(s"WebRecorder.Sql.ProdUS.CoreAdministration.Port")};user=${conf.getString(s"WebRecorder.Sql.ProdUS.CoreAdministration.UserId")};password=${conf.getString(s"WebRecorder.Sql.ProdUS.CoreAdministration.Password")}"
    CoreAdministrationSqlManager.connection = DriverManager.getConnection(CoreAdministrationSqlManager.url)
    //val aaaaa = "119929,26;119948,59;119948,310;120216,13;122434,14747;122434,14747;122434,14909;122434,14909;122434,16423;122434,16423;122434,16585;122434,16585;139469,9706;158700,11110;158700,13094;158700,13977;158700,14119;158700,14230;158700,4242;158700,14470;158700,14478;158700,14501;193223,9956;193518,13250;224631,20;232738,280;232806,16439;232806,16530;232806,16667;232806,16740;232806,16741;232897,201;232897,221;232897,226;232897,227;232897,264;232958,38784;232958,8918;232958,38918;232959,32381;232959,32393;232959,32465;233022,53331;233022,53331;233026,53342;233026,53456;233026,53457;233026,53517;233061,53470;233061,53512;233062,80;233062,81;233062,81;233062,82;233062,82;233062,83;233062,83;23306,84;233062,84;233062,85;233062,85;233087,70;233136,441;233136,442;233136,443;233136,451;233136,465;233144,53438;233184,98;233188,32467;233197,452;233197,457;233206,9898;233206,9946;233211,33;233240,44;233261,59;233261,81;233265,69;23267,70;233275,77;233283,7;233289,78;233291,79;233291,1090;233328,82;233328,82;233328,1089;233328,1089;233347,5;233395,5;233396,1001;233396,1002;233396,1018;233396,1019;233396,1020;233396,1031;233409,5;233429,1098;233447,32526".split(';')
val aaaaa = "120216,13;232649,323;233031,119;233062,81;233062,82;233062,84;233093,428;233167,17".split(';')
    val res1 = CoreAdministrationSqlManager.Select("SELECT [SubscriberId],[ProjectId],[TimeToLive],[ProjectName],[QueueId] FROM [CoreAdministration].[dbo].[ProjectRecordingConfiguration] WHERE IsPublishFullAvro = 1 and EncryptedProjectKey is not NULL and QueueId!= 0 and IsActive is not NULL")
    //if (File.Exists("failures_US.csv")) File.Delete("failures_US.csv")
    //File.AppendAllText("failures_US.csv","SubsId,Pid,Sid,Reason\n")
    if (File.Exists("temp_summ.csv")) File.Delete("temp_summ.csv")
    File.AppendAllText("temp_summ.csv",s"SubsId,Pid,project name,queue id,retention,retention date,first success,${DateTime.now().toString.split('.')(0).replace("T", " ")}\n")
    aaaaa.foreach {
      ent =>
        try {
          val res1 = CoreAdministrationSqlManager.Select(s"SELECT [SubscriberId],[ProjectId],[TimeToLive],[ProjectName],[QueueId] FROM [CoreAdministration].[dbo].[ProjectRecordingConfiguration] WHERE SubscriberId = ${ent.split(',')(0)} and ProjectId = ${ent.split(',')(1)}")
          res1.next()
          val subsId = res1.getString("SubscriberId")
          val pid = res1.getString("ProjectId")
          var retention = Math.max(res1.getString("TimeToLive").toInt, 3)
          var projectName = res1.getString("ProjectName")
          var queueId = res1.getString("QueueId")


          var winningDate = ""
          var trend = ""
          for (a <- 1 to (retention - 1)) {
            val to = DateTime.now().minusDays(a - 1).toString.split('.')(0).replace("T", " ")
            val from = DateTime.now().minusDays(a).toString.split('.')(0).replace("T", " ")
            var errList: util.ArrayList[String] = new util.ArrayList[String]()
            var exceptionList: util.ArrayList[String] = new util.ArrayList[String]()

            Vertica.connect("slvc1.clicktale.net")
            var numOfRecords: Int = 50

            var ret: StringBinaryDeserialized = new StringBinaryDeserialized("", "", 7, null)
            val sids = Vertica.select(s"select SID from CT1Analytics.Recordings where CreateDate>'$from' and CreateDate<'$to' and SID > 11108608519209 and SubscriberID=$subsId and pid=$pid LIMIT $numOfRecords")
            Vertica.quit
            //val sids = ArrayBuffer[String]("1525734349488277")
            numOfRecords = sids.size
            if (numOfRecords == 0) {
              trend += "fail,"
              if (winningDate == "")
                winningDate = to
            }
            else {
              val httpPost = new HttpPost("http://cage.clicktale.net/allevents/v1/1510860851920957")
              val decoded = (sids.mkString(",")).getBytes()
              val entity = new ByteArrayEntity(decoded)
              httpPost.setEntity(entity)
              // execute the request
              val client = HttpClientBuilder.create().build()
              val response = client.execute(httpPost)
              val result = IOUtils.toByteArray(response.getEntity.getContent)
              //File.AppendAllText("c:\\aaa\\out111.txt",result)
              client.close()

              val bytesInputStream = new ByteArrayInputStream(result)
              val zipInputStream = new ZipInputStream(bytesInputStream)
              val archiveEntryArray = Stream
                .continually(zipInputStream.getNextEntry)
                .takeWhile(_ != null)
                .map(zipEntry => ArchiveEntry(zipEntry.getName, IOUtils.toByteArray(zipInputStream)))
                .toList

              val fullSessionBinarySerializer = new FullSessionBinarySerializer()
              if (archiveEntryArray.size < (numOfRecords - (numOfRecords / 10))) {
                failFlag = true
                //File.AppendAllText("failures_US.csv",s"$subsId,$pid,,not enough records came back from cage\n")
                println("number of records for pid=" + pid + ", subsid=" + subsId + " is invalid: " + archiveEntryArray.size + "out of " + numOfRecords + "for retention of " + retention)
                trend += "fail,"
                if (winningDate == "")
                  winningDate = to
              }
              else {
                var i: Int = 0
                archiveEntryArray.foreach {
                  entry =>
                    try {
                      ret = fullSessionBinarySerializer.convertBytesToString(entry.data)
                    } catch {
                      case ex: Exception => {
                        failFlag = true
                        errList.add(entry.name);
                        exceptionList.add(ex.toString)
                      }
                    }
                    i += 1
                }
                if (errList.size() > (numOfRecords / 20)) {
                  trend += "fail,"
                  if (winningDate == "")
                    winningDate = to
                  //println("failed sessions for subsId="+subsId+", pid="+pid+" retention of "+retention+":")
                  //println("Failure of " + ((errList.size().toDouble / numOfRecords.toDouble) * 100 + "% of " + numOfRecords + " sessions\n"))
                  //File.AppendAllText("summary_US.csv",s"$subsId,$pid,${retention-3},${archiveEntryArray.size},${errList.size()},${(errList.size().toDouble/archiveEntryArray.size.toDouble)*100}\n")
                }
                else {
                  //println(s"no failed sessions. ran ${archiveEntryArray.size} sessions in subsId="+subsId+", pid="+pid+" retention of "+retention + "\n")
                  //File.AppendAllText("summary_US.csv",s"$subsId,$pid,${retention-3},${archiveEntryArray.size},${errList.size()},${if (archiveEntryArray.size==0) 0 else (errList.size().toDouble/archiveEntryArray.size.toDouble)*100}\n")
                  trend += "success,"
                }
              }
            }
            println(a)
          }
          println("*****"+pid+"*****")
          File.AppendAllText("temp_summ.csv", s"$subsId,$pid,$projectName,$queueId,$retention,${DateTime.now().minusDays(retention).toString.split('.')(0).replace("T", " ")},${winningDate},$trend\n")
        }
        catch {
          case ex: Exception => {
            File.AppendAllText("temp_summ.csv", s"encountered exception\n")

          }
        }
    }
    if (failFlag)
      assert(true==false,"\nFailed! see above.")
  }

  "Check retention Prod EU" taggedAs (Tag.apply("EU")) in {
    var failFlag = false
    CoreAdministrationSqlManager.driver = conf.getString(s"WebRecorder.Sql.ProdEU.CoreAdministration.Driver")
    CoreAdministrationSqlManager.url = s"jdbc:sqlserver://${conf.getString(s"WebRecorder.Sql.ProdEU.CoreAdministration.DataSource")}\\${conf.getString(s"WebRecorder.Sql.ProdEU.CoreAdministration.DataTable")}:${conf.getString(s"WebRecorder.Sql.ProdEU.CoreAdministration.Port")};user=${conf.getString(s"WebRecorder.Sql.ProdEU.CoreAdministration.UserId")};password=${conf.getString(s"WebRecorder.Sql.ProdEU.CoreAdministration.Password")}"
    CoreAdministrationSqlManager.connection = DriverManager.getConnection(CoreAdministrationSqlManager.url)
    //val aaaaa = "119929,26;119948,59;119948,310;120216,13;122434,14747;122434,14747;122434,14909;122434,14909;122434,16423;122434,16423;122434,16585;122434,16585;139469,9706;158700,11110;158700,13094;158700,13977;158700,14119;158700,14230;158700,4242;158700,14470;158700,14478;158700,14501;193223,9956;193518,13250;224631,20;232738,280;232806,16439;232806,16530;232806,16667;232806,16740;232806,16741;232897,201;232897,221;232897,226;232897,227;232897,264;232958,38784;232958,8918;232958,38918;232959,32381;232959,32393;232959,32465;233022,53331;233022,53331;233026,53342;233026,53456;233026,53457;233026,53517;233061,53470;233061,53512;233062,80;233062,81;233062,81;233062,82;233062,82;233062,83;233062,83;23306,84;233062,84;233062,85;233062,85;233087,70;233136,441;233136,442;233136,443;233136,451;233136,465;233144,53438;233184,98;233188,32467;233197,452;233197,457;233206,9898;233206,9946;233211,33;233240,44;233261,59;233261,81;233265,69;23267,70;233275,77;233283,7;233289,78;233291,79;233291,1090;233328,82;233328,82;233328,1089;233328,1089;233347,5;233395,5;233396,1001;233396,1002;233396,1018;233396,1019;233396,1020;233396,1031;233409,5;233429,1098;233447,32526".split(';')
    val aaaaa = "224891,13;232601,294;233140,56;233177,64;233177,86;233193,68;233251,28;233372,42;233386,5;233448,53".split(';')
    val res1 = CoreAdministrationSqlManager.Select("SELECT [SubscriberId],[ProjectId],[TimeToLive],[ProjectName],[QueueId] FROM [CoreAdministration].[dbo].[ProjectRecordingConfiguration] WHERE IsPublishFullAvro = 1 and EncryptedProjectKey is not NULL and QueueId!= 0 and IsActive is not NULL")
    //if (File.Exists("failures_US.csv")) File.Delete("failures_US.csv")
    //File.AppendAllText("failures_US.csv","SubsId,Pid,Sid,Reason\n")
    if (File.Exists("temp_summ_eu.csv")) File.Delete("temp_summ_eu.csv")
    File.AppendAllText("temp_summ_eu.csv",s"SubsId,Pid,project name,queue id,retention,retention date,first success,${DateTime.now().toString.split('.')(0).replace("T", " ")}\n")
    aaaaa.foreach {
      ent =>
        try {
          val res1 = CoreAdministrationSqlManager.Select(s"SELECT [SubscriberId],[ProjectId],[TimeToLive],[ProjectName],[QueueId] FROM [CoreAdministration].[dbo].[ProjectRecordingConfiguration] WHERE SubscriberId = ${ent.split(',')(0)} and ProjectId = ${ent.split(',')(1)}")
          res1.next()
          val subsId = res1.getString("SubscriberId")
          val pid = res1.getString("ProjectId")
          var retention = Math.max(res1.getString("TimeToLive").toInt, 3)
          var projectName = res1.getString("ProjectName")
          var queueId = res1.getString("QueueId")


          var winningDate = ""
          var trend = ""
          for (a <- 1 to (retention - 1)) {
            val to = DateTime.now().minusDays(a - 1).toString.split('.')(0).replace("T", " ")
            val from = DateTime.now().minusDays(a).toString.split('.')(0).replace("T", " ")
            var errList: util.ArrayList[String] = new util.ArrayList[String]()
            var exceptionList: util.ArrayList[String] = new util.ArrayList[String]()

            Vertica.connect("amvc1.clicktale.net")
            var numOfRecords: Int = 50

            var ret: StringBinaryDeserialized = new StringBinaryDeserialized("", "", 7, null)
            val sids = Vertica.select(s"select SID from CT1Analytics.Recordings where CreateDate>'$from' and CreateDate<'$to' and SID > 11108608519209 and SubscriberID=$subsId and pid=$pid LIMIT $numOfRecords")
            Vertica.quit
            //val sids = ArrayBuffer[String]("1525734349488277")
            numOfRecords = sids.size
            if (numOfRecords == 0) {
              trend += "fail,"
              if (winningDate == "")
                winningDate = to
            }
            else {
              val httpPost = new HttpPost("http://cage-eu.clicktale.net/allevents/v1/1510860851920957")
              val decoded = (sids.mkString(",")).getBytes()
              val entity = new ByteArrayEntity(decoded)
              httpPost.setEntity(entity)
              // execute the request
              val client = HttpClientBuilder.create().build()
              val response = client.execute(httpPost)
              val result = IOUtils.toByteArray(response.getEntity.getContent)
              //File.AppendAllText("c:\\aaa\\out111.txt",result)
              client.close()

              val bytesInputStream = new ByteArrayInputStream(result)
              val zipInputStream = new ZipInputStream(bytesInputStream)
              val archiveEntryArray = Stream
                .continually(zipInputStream.getNextEntry)
                .takeWhile(_ != null)
                .map(zipEntry => ArchiveEntry(zipEntry.getName, IOUtils.toByteArray(zipInputStream)))
                .toList

              val fullSessionBinarySerializer = new FullSessionBinarySerializer()
              if (archiveEntryArray.size < (numOfRecords - (numOfRecords / 10))) {
                failFlag = true
                //File.AppendAllText("failures_US.csv",s"$subsId,$pid,,not enough records came back from cage\n")
                println("number of records for pid=" + pid + ", subsid=" + subsId + " is invalid: " + archiveEntryArray.size + "out of " + numOfRecords + "for retention of " + retention)
                trend += "fail,"
                if (winningDate == "")
                  winningDate = to
              }
              else {
                var i: Int = 0
                archiveEntryArray.foreach {
                  entry =>
                    try {
                      ret = fullSessionBinarySerializer.convertBytesToString(entry.data)
                    } catch {
                      case ex: Exception => {
                        failFlag = true
                        errList.add(entry.name);
                        exceptionList.add(ex.toString)
                      }
                    }
                    i += 1
                }
                if (errList.size() > (numOfRecords / 20)) {
                  trend += "fail,"
                  if (winningDate == "")
                    winningDate = to
                  //println("failed sessions for subsId="+subsId+", pid="+pid+" retention of "+retention+":")
                  //println("Failure of " + ((errList.size().toDouble / numOfRecords.toDouble) * 100 + "% of " + numOfRecords + " sessions\n"))
                  //File.AppendAllText("summary_US.csv",s"$subsId,$pid,${retention-3},${archiveEntryArray.size},${errList.size()},${(errList.size().toDouble/archiveEntryArray.size.toDouble)*100}\n")
                }
                else {
                  //println(s"no failed sessions. ran ${archiveEntryArray.size} sessions in subsId="+subsId+", pid="+pid+" retention of "+retention + "\n")
                  //File.AppendAllText("summary_US.csv",s"$subsId,$pid,${retention-3},${archiveEntryArray.size},${errList.size()},${if (archiveEntryArray.size==0) 0 else (errList.size().toDouble/archiveEntryArray.size.toDouble)*100}\n")
                  trend += "success,"
                }
              }
            }
            println(a)
          }
          println("*****"+pid+"*****")
          File.AppendAllText("temp_summ_eu.csv", s"$subsId,$pid,$projectName,$queueId,$retention,${DateTime.now().minusDays(retention).toString.split('.')(0).replace("T", " ")},${winningDate},$trend\n")
        }
        catch {
          case ex: Exception => {
            File.AppendAllText("temp_summ_eu.csv", s"encountered exception\n")

          }
        }
    }
    if (failFlag)
      assert(true==false,"\nFailed! see above.")
  }

}
