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

case class ArchiveEntry(name: String, data: Array[Byte])

class BasicPushTest extends WordSpecLike with BeforeAndAfterAll {

  val optionalPid: Int = conf.getString("WebRecorder.Environments.AwsQaLin.Pid").toInt
  val to = DateTime.now().minusHours(8).toString.split('.')(0).replace("T"," ")
  val from = DateTime.now().minusHours(9).toString.split('.')(0).replace("T"," ")
  var errList:util.ArrayList[String] = new util.ArrayList[String]()
  var exceptionList:util.ArrayList[String] = new util.ArrayList[String]()

//  val subsId: Int = conf.getString("WebRecorder.Environments.AwsQaLin.SubsId").toInt

//  val recBasicSessionDetails: AuthResponse =
//    pushSessionParallely("/recFiles/rec.json", conf.getString("WebRecorder.Session.States.Recording"),optionalPid, optionalMobile=false,subsId)
//
//  val archivePackage: CageArchivePackage =
//    loadFromCage(subsId,optionalPid,recBasicSessionDetails.sid.toLong)


  "Check fullAvro post request with 5000 SIDs Prod US" taggedAs(Tag.apply("US")) in {

    Vertica.connect("slvc1.clicktale.net")

    var numOfRecords:Int = 5000

    var ret:StringBinaryDeserialized = new StringBinaryDeserialized("","",7,null)
    val sids = Vertica.select(s"select SID from CT1Analytics.Recordings where CreateDate>'$from' and CreateDate<'$to' and SID > 11108608519209 LIMIT $numOfRecords")
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

    assert(archiveEntryArray.size>(numOfRecords - (numOfRecords/10)),"number of records is invalid: "+archiveEntryArray.size)
    var i:Int =0
    archiveEntryArray.foreach {

      entry =>
        try {
          ret = fullSessionBinarySerializer.convertBytesToString(entry.data)
          if (i%10 == 0)
          {
            val cageResponse = tryHttpResponse(s"http://cage.clicktale.net/avro/${entry.name}")
            //val parsed = parse(cageResponse)
            assert(cageResponse==ret.deserialized)
          }
        } catch {
          case ex: Exception => {
            errList.add(entry.name);
            exceptionList.add(ex.toString)
          }
        }
        i += 1
    }

    if (errList.size()>numOfRecords/20) {
      println("failed sessions:")
      for (i <- 0 to errList.size() - 1) {
        println("SID: " + errList.get(i) + " Reason: " + exceptionList.get(i))
      }
      println("\nFailure of "+((errList.size().toDouble/numOfRecords.toDouble)*100+"% of "+numOfRecords+" sessions"))
      assert(true==false,"Failed. Look up")
    }
    else
      println(s"no failed sessions. ran ${archiveEntryArray.size} sessions")
  }

  "Check fullAvro post request with 5000 SIDs Prod EU" taggedAs(Tag.apply("EU")) in {

    Vertica.connect("amvc1.clicktale.net")

    var numOfRecords:Int = 5000

    var ret:StringBinaryDeserialized = new StringBinaryDeserialized("","",7,null)
    val sids = Vertica.select(s"select SID from CT1Analytics.Recordings where CreateDate>'$from' and CreateDate<'$to' and SID > 11108608519209 LIMIT $numOfRecords")
    Vertica.quit
    numOfRecords = sids.size
    //val sids = ArrayBuffer[String]("1525734349488277")
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

    assert(archiveEntryArray.size>(numOfRecords - (numOfRecords/10)),"number of records is invalid: "+archiveEntryArray.size)
    var i:Int =0
    archiveEntryArray.foreach {

      entry =>
        try {
          ret = fullSessionBinarySerializer.convertBytesToString(entry.data)
          if (i%10 == 0)
          {
            val cageResponse = tryHttpResponse(s"http://cage-eu.clicktale.net/avro/${entry.name}")
            //val parsed = parse(cageResponse)
            assert(cageResponse==ret.deserialized)
          }
        } catch {
          case ex: Exception => {
            errList.add(entry.name);
            exceptionList.add(ex.toString)
          }
        }
        i += 1
    }

    if (errList.size()>numOfRecords/20) {
      println("failed sessions:")
      for (i <- 0 to errList.size() - 1) {
        println("SID: " + errList.get(i) + " Reason: " + exceptionList.get(i))
      }
      println("\nFailure of "+((errList.size().toDouble/numOfRecords.toDouble)*100+"% of "+numOfRecords+" sessions"))
      assert(true==false,"Failed. Look up")
    }
    else
      println(s"no failed sessions. ran ${archiveEntryArray.size} sessions")
  }

  "Check fullAvro post request with 5000 SIDs Central" taggedAs(Tag.apply("Central")) in {

    Vertica.connect("vcs01.clicktale.net")

    var numOfRecords:Int = 5000

    var ret:StringBinaryDeserialized = new StringBinaryDeserialized("","",7,null)
    val sids = Vertica.select(s"select SID from CT1Analytics.Recordings where CreateDate>'$from' and CreateDate<'$to' and SID > 11108608519209 LIMIT $numOfRecords")
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

    assert(archiveEntryArray.size>(numOfRecords - (numOfRecords/2)),"number of records is invalid: "+archiveEntryArray.size)
    var i:Int =0
    archiveEntryArray.foreach {

      entry =>
        try {
          ret = fullSessionBinarySerializer.convertBytesToString(entry.data)
          if (i%10 == 0)
          {
            val cageResponse = tryHttpResponse(s"http://nv-p1-elb-ext-central-cage-r-01-498798595.us-east-1.elb.amazonaws.com/avro/${entry.name}")
            //val parsed = parse(cageResponse)
            assert(cageResponse==ret.deserialized)
          }
        } catch {
          case ex: Exception => {
            errList.add(entry.name);
            exceptionList.add(ex.toString)
          }
        }
        i += 1
    }

    if (errList.size()>numOfRecords/20) {
      println("failed sessions:")
      for (i <- 0 to errList.size() - 1) {
        println("SID: " + errList.get(i) + " Reason: " + exceptionList.get(i))
      }
      println("\nFailure of "+((errList.size().toDouble/numOfRecords.toDouble)*100+"% of "+numOfRecords+" sessions"))
      assert(true==false,"Failed. Look up")
    }
    else
      println(s"no failed sessions. ran ${archiveEntryArray.size} sessions")
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
