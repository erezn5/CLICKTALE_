package com.clicktale.pipeline.regressions.tests.ProdTests

import java.io.DataInputStream
import java.net.URL

import org.joda.time.DateTime
import System.IO.File
import com.clicktale.pipeline.framework.dal.{CoreAdministrationSqlManager, Vertica}
import System.Tuple
import com.clicktale.pipeline.dataObjects.{AuthResponse, CageArchivePackage, SessionStream}
import com.clicktale.pipeline.framework.dal.ConfigParser.conf
import com.clicktale.pipeline.framework.helpers.UrlBuilder.buildStream
import com.clicktale.pipeline.framework.senders.SendSession._
import com.clicktale.pipeline.framework.storage.GetFromS3._
import com.clicktale.pipeline.framework.storage
import com.clicktale.pipeline.storagemaster
import org.joda.time.DateTime
import org.json4s
import org.json4s._
import org.json4s.native.JsonMethods._
import org.scalatest._
import com.github.nscala_time.time.Imports._
import org.apache.commons.io.IOUtils
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.ByteArrayEntity
import org.apache.http.impl.client.HttpClientBuilder
import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import java.util.zip.{ZipEntry, ZipInputStream, ZipOutputStream}
import java.io.ByteArrayInputStream
import java.sql.DriverManager
import java.util

import com.clicktale.pipeline.regressions.tests.functionalTests.procRelatedTests.ArchiveEntry
import com.clicktale.pipeline.storagemaster.logic.archives.binary.FullSessionBinarySerializer
import com.clicktale.pipeline.storagemaster.logic.archives.binary.FullSessionBinarySerializer.StringBinaryDeserialized
import org.scalatest.tagobjects._

import scala.collection.mutable.ArrayBuffer
//import com.clicktale.pipeline.common.dal.storage.ArchiveEntry
//import com.gargoylesoftware.htmlunit.javascript.host.arrays.ArrayBuffer

import scala.xml.XML
import scalaj.http._
import com.clicktale.pipeline.framework.dal.GeneralFunctions._

class RetentionStabilityTest extends WordSpecLike with BeforeAndAfterAll {
  val amountOfIterations = 2
  "Check retention Prod US" taggedAs (Tag.apply("US")) in {
    var failFlag = false
    CoreAdministrationSqlManager.driver = conf.getString(s"WebRecorder.Sql.ProdUS.CoreAdministration.Driver")
    CoreAdministrationSqlManager.url = s"jdbc:sqlserver://${conf.getString(s"WebRecorder.Sql.ProdUS.CoreAdministration.DataSource")}\\${conf.getString(s"WebRecorder.Sql.ProdUS.CoreAdministration.DataTable")}:${conf.getString(s"WebRecorder.Sql.ProdUS.CoreAdministration.Port")};user=${conf.getString(s"WebRecorder.Sql.ProdUS.CoreAdministration.UserId")};password=${conf.getString(s"WebRecorder.Sql.ProdUS.CoreAdministration.Password")}"
    CoreAdministrationSqlManager.connection = DriverManager.getConnection(CoreAdministrationSqlManager.url)
    val res = CoreAdministrationSqlManager.Select("SELECT [SubscriberId],[ProjectId],[TimeToLive], [ProjectName], [QueueId] FROM [CoreAdministration].[dbo].[ProjectRecordingConfiguration] WHERE IsPublishFullAvro = 1 and EncryptedProjectKey is not NULL and QueueId!= 0")
    if (File.Exists("failures_US.csv")) File.Delete("failures_US.csv")
    File.AppendAllText("failures_US.csv","SubsId,Pid,Sid,Reason\n")
    if (File.Exists("summary_US.csv")) File.Delete("failures_US.csv")
    File.AppendAllText("summary_US.csv","SubsId,Pid,Project Name,Queue Id,days back,Tested,Failed,Fail percentage\n")
    while(res.next()){
      val subsId = res.getString("SubscriberId")
      val pid = res.getString("ProjectId")
      val projectName = res.getString("ProjectName")
      val queueId = res.getString("QueueId")
      var retention = Math.max(res.getString("TimeToLive").toInt,3)

      for (a <- 1 to amountOfIterations) {
        val to = DateTime.now().minusDays(retention.toInt -6).toString.split('.')(0).replace("T", " ")
        val from = DateTime.now().minusDays(retention.toInt -5).toString.split('.')(0).replace("T", " ")
        var errList: util.ArrayList[String] = new util.ArrayList[String]()
        var exceptionList: util.ArrayList[String] = new util.ArrayList[String]()

        Vertica.connect("slvc1.clicktale.net")
        var numOfRecords: Int = 500

        var ret: StringBinaryDeserialized = new StringBinaryDeserialized("", "", 7, null)
        val sids = Vertica.select(s"select SID from CT1Analytics.Recordings where CreateDate>'$from' and CreateDate<'$to' and SID > 11108608519209 and SubscriberID=$subsId and pid=$pid LIMIT $numOfRecords")
        Vertica.quit
        //val sids = ArrayBuffer[String]("1525734349488277")
        numOfRecords = sids.size
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
          File.AppendAllText("failures_US.csv",s"$subsId,$pid,,not enough records came back from cage\n")
          println("number of records for pid="+pid+", subsid="+subsId+" is invalid: " + archiveEntryArray.size + "out of "+numOfRecords + "for retention of "+ retention )
          File.AppendAllText("summary_US.csv",s"$subsId,$pid,$projectName,$queueId,${retention-3},${archiveEntryArray.size},,${(archiveEntryArray.size.toDouble/numOfRecords.toDouble)*100}\n")
       }
        else {
          var i: Int = 0
          archiveEntryArray.foreach {
            entry =>
              try {
                ret = fullSessionBinarySerializer.convertBytesToString(entry.data)
                if (i % 1000 == 0) {
                  val cageResponse = tryHttpResponse(s"http://cage.clicktale.net/recording/v1/$subsId/$pid/${entry.name}")
                  //val parsed = parse(cageResponse)
                  assert(cageResponse.contains("recording"))
                }
              } catch {
                case ex: Exception => {
                  failFlag = true
                  errList.add(entry.name);
                  exceptionList.add(ex.toString)
                }
              }
              i += 1
          }
          if (errList.size() > (numOfRecords / 10)) {
            println("failed sessions for subsId="+subsId+", pid="+pid+" retention of "+retention+":")
            for (i <- 0 to errList.size() - 1) {
              println("SID: " + errList.get(i) + " Reason: " + exceptionList.get(i))
              if (exceptionList.get(i).contains("java.io.EOFException"))
                File.AppendAllText("failures_US.csv",s"$subsId,$pid,${errList.get(i)},session was not found in Aerospike\n")
              else if (exceptionList.get(i).contains("There was an internal server error"))
                File.AppendAllText("failures_US.csv",s"$subsId,$pid,${errList.get(i)},session was not found in S3\n")
              else if (exceptionList.get(i).contains("java.lang.ArrayIndexOutOfBoundsException"))
                File.AppendAllText("failures_US.csv",s"$subsId,$pid,${errList.get(i)},avro is corrupted\n")
              else
                File.AppendAllText("failures_US.csv",s"$subsId,$pid,${errList.get(i)},${exceptionList.get(i)}\n")
            }
            println("Failure of " + ((errList.size().toDouble / numOfRecords.toDouble) * 100 + "% of " + numOfRecords + " sessions\n"))
            File.AppendAllText("summary_US.csv",s"$subsId,$pid,$projectName,$queueId,${retention-3},${archiveEntryArray.size},${errList.size()},${(errList.size().toDouble/archiveEntryArray.size.toDouble)*100}\n")
          }
          else{
            println(s"no failed sessions. ran ${archiveEntryArray.size} sessions in subsId="+subsId+", pid="+pid+" retention of "+retention + "\n")
            File.AppendAllText("summary_US.csv",s"$subsId,$pid,$projectName,$queueId,${retention-3},${archiveEntryArray.size},${errList.size()},${if (archiveEntryArray.size==0) 0 else (errList.size().toDouble/archiveEntryArray.size.toDouble)*100}\n")
          }
                    }
        retention = (retention - (retention / 3))
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
    val res = CoreAdministrationSqlManager.Select("SELECT [SubscriberId],[ProjectId],[TimeToLive] FROM [CoreAdministration].[dbo].[ProjectRecordingConfiguration] WHERE IsPublishFullAvro = 1 and EncryptedProjectKey is not NULL and QueueId!= 0")
    if (File.Exists("failures_EU.csv")) File.Delete("failures_EU.csv")
    File.AppendAllText("failures_EU.csv","SubsId,Pid,Sid,Reason\n")
    if (File.Exists("summary_EU.csv")) File.Delete("failures_EU.csv")
    File.AppendAllText("summary_EU.csv","SubsId,Pid,days back,Tested,Failed,Fail percentage\n")
    while(res.next()){
      val subsId = res.getString("SubscriberId")
      val pid = res.getString("ProjectId")
      var retention = Math.max(res.getString("TimeToLive").toInt,3)

      for (a <- 1 to amountOfIterations) {
        val to = DateTime.now().minusDays(retention.toInt -3).toString.split('.')(0).replace("T", " ")
        val from = DateTime.now().minusDays(retention.toInt -2).toString.split('.')(0).replace("T", " ")
        var errList: util.ArrayList[String] = new util.ArrayList[String]()
        var exceptionList: util.ArrayList[String] = new util.ArrayList[String]()

        Vertica.connect("amvc1.clicktale.net")
        var numOfRecords: Int = 50

        var ret: StringBinaryDeserialized = new StringBinaryDeserialized("", "", 7, null)
        val sids = Vertica.select(s"select SID from CT1Analytics.Recordings where CreateDate>'$from' and CreateDate<'$to' and SID > 11108608519209 and SubscriberID=$subsId and pid=$pid LIMIT $numOfRecords")
        Vertica.quit
        //val sids = ArrayBuffer[String]("1525734349488277")
        numOfRecords = sids.size
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
          File.AppendAllText("failures_EU.csv",s"$subsId,$pid,,not enough records came back from cage\n")
          println("number of records for pid="+pid+", subsid="+subsId+" is invalid: " + archiveEntryArray.size + "out of "+numOfRecords + "for retention of "+ retention )
          File.AppendAllText("summary_EU.csv",s"$subsId,$pid,${retention-3},${archiveEntryArray.size},,${(archiveEntryArray.size.toDouble/numOfRecords.toDouble)*100}\n")
        }
        else {
          var i: Int = 0
          archiveEntryArray.foreach {
            entry =>
              try {
                ret = fullSessionBinarySerializer.convertBytesToString(entry.data)
                if (i % 10 == 0) {
                  val cageResponse = tryHttpResponse(s"http://cage-eu.clicktale.net/recording/v1/$subsId/$pid/${entry.name}")
                  //val parsed = parse(cageResponse)
                  assert(cageResponse.contains("recording"))
                }
              } catch {
                case ex: Exception => {
                  failFlag = true
                  errList.add(entry.name);
                  exceptionList.add(ex.toString)
                }
              }
              i += 1
          }
          if (errList.size() > (numOfRecords / 10)) {
            println("failed sessions for subsId="+subsId+", pid="+pid+" retention of "+retention+":")
            for (i <- 0 to errList.size() - 1) {
              println("SID: " + errList.get(i) + " Reason: " + exceptionList.get(i))
              if (exceptionList.get(i).contains("java.io.EOFException"))
                File.AppendAllText("failures_EU.csv",s"$subsId,$pid,${errList.get(i)},session was not found in Aerospike\n")
              else if (exceptionList.get(i).contains("There was an internal server error"))
                File.AppendAllText("failures_EU.csv",s"$subsId,$pid,${errList.get(i)},session was not found in S3\n")
              else if (exceptionList.get(i).contains("java.lang.ArrayIndexOutOfBoundsException"))
                File.AppendAllText("failures_EU.csv",s"$subsId,$pid,${errList.get(i)},avro is corrupted\n")
              else
                File.AppendAllText("failures_EU.csv",s"$subsId,$pid,${errList.get(i)},${exceptionList.get(i)}\n")
            }
            println("Failure of " + ((errList.size().toDouble / numOfRecords.toDouble) * 100 + "% of " + numOfRecords + " sessions\n"))
            File.AppendAllText("summary_EU.csv",s"$subsId,$pid,${retention-3},${archiveEntryArray.size},${errList.size()},${(errList.size().toDouble/archiveEntryArray.size.toDouble)*100}\n")
          }
          else
          {
            println(s"no failed sessions. ran ${archiveEntryArray.size} sessions in subsId="+subsId+", pid="+pid+" retention of "+retention + "\n")
            File.AppendAllText("summary_EU.csv",s"$subsId,$pid,${retention-3},${archiveEntryArray.size},${errList.size()},${if (archiveEntryArray.size==0) 0 else (errList.size().toDouble/archiveEntryArray.size.toDouble)*100}\n")
          }
        }
                retention = (retention - (retention / 3))
      }
    }
    if (failFlag)
      assert(true==false,"\nFailed! see above.")
  }

  "Check retention Prod Central" taggedAs (Tag.apply("Central")) in {
    var failFlag = false
    CoreAdministrationSqlManager.driver = conf.getString(s"WebRecorder.Sql.AwsCentral.CoreAdministration.Driver")
    CoreAdministrationSqlManager.url = s"jdbc:sqlserver://${conf.getString(s"WebRecorder.Sql.AwsCentral.CoreAdministration.DataSource")}\\${conf.getString(s"WebRecorder.Sql.AwsCentral.CoreAdministration.DataTable")}:${conf.getString(s"WebRecorder.Sql.AwsCentral.CoreAdministration.Port")};user=${conf.getString(s"WebRecorder.Sql.AwsCentral.CoreAdministration.UserId")};password=${conf.getString(s"WebRecorder.Sql.AwsCentral.CoreAdministration.Password")}"
    CoreAdministrationSqlManager.connection = DriverManager.getConnection(CoreAdministrationSqlManager.url)
    val res = CoreAdministrationSqlManager.Select("SELECT [SubscriberId],[ProjectId],[TimeToLive] FROM [CoreAdministration].[dbo].[ProjectRecordingConfiguration] WHERE IsPublishFullAvro = 1 and EncryptedProjectKey is not NULL and QueueId!= 0")
    if (File.Exists("failures_Central.csv")) File.Delete("failures_Central.csv")
    File.AppendAllText("failures_Central.csv","SubsId,Pid,Sid,Reason\n")
    if (File.Exists("summary_Central.csv")) File.Delete("failures_Central.csv")
    File.AppendAllText("summary_Central.csv","SubsId,Pid,days back,Tested,Failed,Fail percentage\n")
    while(res.next()){
      val subsId = res.getString("SubscriberId")
      val pid = res.getString("ProjectId")
      var retention = Math.max(res.getString("TimeToLive").toInt,3)

      for (a <- 1 to amountOfIterations) {
        val to = DateTime.now().minusDays(retention.toInt -3).toString.split('.')(0).replace("T", " ")
        val from = DateTime.now().minusDays(retention.toInt -2).toString.split('.')(0).replace("T", " ")
        var errList: util.ArrayList[String] = new util.ArrayList[String]()
        var exceptionList: util.ArrayList[String] = new util.ArrayList[String]()

        Vertica.connect("vcs01.clicktale.net")
        var numOfRecords: Int = 50

        var ret: StringBinaryDeserialized = new StringBinaryDeserialized("", "", 7, null)
        val sids = Vertica.select(s"select SID from CT1Analytics.Recordings where CreateDate>'$from' and CreateDate<'$to' and SID > 11108608519209 and SubscriberID=$subsId and pid=$pid LIMIT $numOfRecords")
        Vertica.quit
        //val sids = ArrayBuffer[String]("1525734349488277")
        numOfRecords = sids.size
        val httpPost = new HttpPost("http://nv-p1-elb-ext-central-cage-r-01-498798595.us-east-1.elb.amazonaws.com/allevents/v1/1510860851920957")
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
          File.AppendAllText("failures_Central.csv",s"$subsId,$pid,,not enough records came back from cage\n")
          println("number of records for pid="+pid+", subsid="+subsId+" is invalid: " + archiveEntryArray.size + "out of "+numOfRecords + "for retention of "+ retention )
          File.AppendAllText("summary_Central.csv",s"$subsId,$pid,${retention-3},${archiveEntryArray.size},,${(archiveEntryArray.size.toDouble/numOfRecords.toDouble)*100}\n")
        }
        else {
          var i: Int = 0
          archiveEntryArray.foreach {
            entry =>
              try {
                ret = fullSessionBinarySerializer.convertBytesToString(entry.data)
                if (i % 10 == 0) {
                  val cageResponse = tryHttpResponse(s"http://nv-p1-elb-ext-central-cage-r-01-498798595.us-east-1.elb.amazonaws.com/recording/v1/$subsId/$pid/${entry.name}")
                  //val parsed = parse(cageResponse)
                  assert(cageResponse.contains("recording"))
                }
              } catch {
                case ex: Exception => {
                  failFlag = true
                  errList.add(entry.name);
                  exceptionList.add(ex.toString)
                }
              }
              i += 1
          }
          if (errList.size() > (numOfRecords / 10)) {
            println("failed sessions for subsId="+subsId+", pid="+pid+" retention of "+retention+":")
            for (i <- 0 to errList.size() - 1) {
              println("SID: " + errList.get(i) + " Reason: " + exceptionList.get(i))
              if (exceptionList.get(i).contains("java.io.EOFException"))
                File.AppendAllText("failures_Central.csv",s"$subsId,$pid,${errList.get(i)},session was not found in Aerospike\n")
              else if (exceptionList.get(i).contains("There was an internal server error"))
                File.AppendAllText("failures_Central.csv",s"$subsId,$pid,${errList.get(i)},session was not found in S3\n")
              else if (exceptionList.get(i).contains("java.lang.ArrayIndexOutOfBoundsException"))
                File.AppendAllText("failures_Central.csv",s"$subsId,$pid,${errList.get(i)},avro is corrupted\n")
              else
                File.AppendAllText("failures_Central.csv",s"$subsId,$pid,${errList.get(i)},${exceptionList.get(i)}\n")
            }
            println("Failure of " + ((errList.size().toDouble / numOfRecords.toDouble) * 100 + "% of " + numOfRecords + " sessions\n"))
            File.AppendAllText("summary_Central.csv",s"$subsId,$pid,${retention-3},${archiveEntryArray.size},${errList.size()},${(errList.size().toDouble/archiveEntryArray.size.toDouble)*100}\n")

          }
          else
          {
            File.AppendAllText("summary_Central.csv",s"$subsId,$pid,${retention-3},${archiveEntryArray.size},${errList.size()},${if (archiveEntryArray.size==0) 0 else (errList.size().toDouble/archiveEntryArray.size.toDouble)*100}\n")
            println(s"no failed sessions. ran ${archiveEntryArray.size} sessions in subsId="+subsId+", pid="+pid+" retention of "+retention + "\n")

          }
        }
        retention = (retention - (retention / 3))
      }
    }
    if (failFlag)
      assert(true==false,"\nFailed! see above.")
  }
}
