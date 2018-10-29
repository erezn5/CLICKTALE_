package com.clicktale.pipeline.regressions.tests.functionalTests.procRelatedTests

import java.io.DataInputStream
import java.net.URL

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
import org.scalatest.{BeforeAndAfterAll, Tag, WordSpecLike}
import com.github.nscala_time.time.Imports._
import org.apache.commons.io.IOUtils
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.ByteArrayEntity
import org.apache.http.impl.client.HttpClientBuilder
import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import java.util.zip.{ZipEntry, ZipInputStream, ZipOutputStream}
import java.io.ByteArrayInputStream
import java.util

import com.clicktale.pipeline.storagemaster.logic.archives.binary.FullSessionBinarySerializer
import com.clicktale.pipeline.storagemaster.logic.archives.binary.FullSessionBinarySerializer.StringBinaryDeserialized

import scala.collection.mutable.ArrayBuffer
//import com.clicktale.pipeline.common.dal.storage.ArchiveEntry
//import com.gargoylesoftware.htmlunit.javascript.host.arrays.ArrayBuffer

import scala.xml.XML
import scalaj.http._

case class ArchiveEntry(name: String, data: Array[Byte])

class CageRelatedTests extends WordSpecLike with BeforeAndAfterAll {

  Vertica.connect("vcs01.clicktale.net")

  val optionalPid: Int = conf.getString("WebRecorder.Environments.AwsQaLin.Pid").toInt
  val to = DateTime.now().toString.split('T')(0)+" 00:00:00"
  val from = (DateTime.now() - 7.days).toString.split('T')(0)+" 00:00:00"
  var errList:util.ArrayList[String] = new util.ArrayList[String]()
  var exceptionList:util.ArrayList[String] = new util.ArrayList[String]()

  val subsId: Int = conf.getString("WebRecorder.Environments.AwsQaLin.SubsId").toInt

  lazy val recBigSessionDetails: AuthResponse =
    pushSessionParallely("/recFiles/rec.json", conf.getString("WebRecorder.Session.States.Recording"),optionalPid, optionalMobile=false,subsId)

  lazy val recSmallSessionDetails: AuthResponse =
    pushSessionParallely("/recFiles/rec.json", conf.getString("WebRecorder.Session.States.Recording"),optionalPid, optionalMobile=false,subsId)

  lazy val recPerfectSessionDetails: AuthResponse =
    pushSessionParallely("/recFiles/rec.json", conf.getString("WebRecorder.Session.States.Recording"),optionalPid, optionalMobile=false,subsId)

  lazy val archivePackage: CageArchivePackage =
    loadFromCage(subsId,optionalPid,recPerfectSessionDetails.sid.toLong)

  lazy val bigArchivePackage: CageArchivePackage =
    loadFromCage(subsId,optionalPid,recPerfectSessionDetails.sid.toLong)

  lazy val smallArchivePackage: CageArchivePackage =
    loadFromCage(subsId,optionalPid,recPerfectSessionDetails.sid.toLong)

  val cageInstanceIp: String =
    conf.getString(s"WebRecorder.Cage.${conf.getString("WebRecorder.Current.Environment")}.DNS")

  var parsedJsonCage: json4s.JValue = _

  val avroShouldBe: String =
    conf.getString("WebRecorder.Cage.Tests.AvroShouldBe")


  "Check fullAvro post request with 5000 SIDs" in { pending
    val numOfRecords = 5000

    var ret:StringBinaryDeserialized = new StringBinaryDeserialized("","",7,null)
    val sids = Vertica.select(s"select SID from CT1Analytics.Recordings where SubscriberID=233448 and pid=53 and CreateDate>'$from' and CreateDate<'$to' LIMIT $numOfRecords")
    sids.foreach {

      entry =>
        File.AppendAllText("c:\\aaa\\ya.txt",entry+",")
    }
    Vertica.quit
    //val sids = ArrayBuffer[String]("1525734349488277")
    val httpPost = new HttpPost("http://ir-p1-elb-ext-district-cage-r-01-102719072.eu-west-1.elb.amazonaws.com/allevents/v1/1510860851920957")
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

    assert(archiveEntryArray.size==numOfRecords,"number of records is invalid: "+archiveEntryArray.size)
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
          i+=1
        } catch {
          case ex: Exception => {
            errList.add(entry.name);
            exceptionList.add(ex.toString)
          }
        }
    }

    if (errList.size()>0) {
      println("failed sessions:")
      for (i <- 0 to errList.size() - 1) {
        println("SID: " + errList.get(i) + " Reason: " + exceptionList.get(i))
      }
      println("\nFailure of "+((errList.size().toDouble/numOfRecords.toDouble)*100+"%"))
      assert(true==false,"Failed. Look up")
    }
    else
      println("no failed sessions")
  }

  "Check Recording" in {
    assert(recPerfectSessionDetails != null)
  }

//  "Check Avro returns" taggedAs Tag("NotInTeamCity") in {
//    val archiveAvro: String = loadAvroFromCage22(recPerfectSessionDetails.sid.toLong)
//    assert(archiveAvro != null)
//  }

  "Check Cage Url Existence" in {
    try {
      val cageResponse = tryHttpResponse(
        s"$cageInstanceIp${
          conf.getString("WebRecorder.Cage.UrlSuffix")}$subsId/$optionalPid/${
          recPerfectSessionDetails.sid}")
      parsedJsonCage = parse(cageResponse)
    }
    catch {
      case e: Exception => fail(e)
    }
  }

  "Check Cage is proper JSON" in {
    assert(parsedJsonCage != null)
  }

  "Check Large Package is processed" in {
    val cageResponse = tryHttpResponse(
      s"$cageInstanceIp${
        conf.getString("WebRecorder.Cage.UrlSuffix")}$subsId/$optionalPid/${
        recBigSessionDetails.sid}")
    val parsedBigJsonCage = parse(cageResponse)
    assert(parsedBigJsonCage != null)
    //assert(parsedBigJsonCage.children(0).toString().contains("recording"))
    assert(parsedBigJsonCage.children(0).toString().contains("json"))
    //assert(parsedBigJsonCage.children(1).toString().contains("recording"))
    assert(parsedBigJsonCage.children(1).toString().contains("json"))
    assert(parsedBigJsonCage.children(2).toString().contains("html"))
    assert(parsedBigJsonCage.children(3).toString().contains("ActiveTime"))
  }

//  "Check 6-event Avro is created" in {
//    val cageResponse = tryHttpResponse(
//      s"$cageInstanceIp/validatePartialEvent/${
//        recSmallSessionDetails.sid}")
//    assert(cageResponse.contains("Version 2"))
//  }

  "Check 100-event Avro is created" in {
    val cageResponse = tryHttpResponse(
      s"$cageInstanceIp/validateFullEvent/${
        recSmallSessionDetails.sid}")
    assert(cageResponse.contains("Version 7"))
  }

  "Check Avro to XML" in {
    val cageResponse = tryHttpResponse(
      s"$cageInstanceIp/avro/${
        recSmallSessionDetails.sid}")
    try {
      val x = XML.load(new URL(s"$cageInstanceIp/avro/${
        recSmallSessionDetails.sid}"))
      assert(cageResponse.contains("<recording"))
    }
    catch {
      case e: Exception => fail(e)
    }
  }

  "Check subsid=0 is not processed in cage" in {
    val cageResponse = tryHttpResponse(
      s"$cageInstanceIp${
        conf.getString("WebRecorder.Cage.UrlSuffix")}0/$optionalPid/${
        recPerfectSessionDetails.sid}")
    assert(cageResponse.contains("The request contains bad syntax or cannot be fulfilled."))
  }

  "Check pid=0 is not processed in cage" in {
    val cageResponse = tryHttpResponse(
      s"$cageInstanceIp${
        conf.getString("WebRecorder.Cage.UrlSuffix")}$subsId/0/${
        recPerfectSessionDetails.sid}")
    assert(cageResponse.contains("The request contains bad syntax or cannot be fulfilled."))
  }

  "Check sid=0 is not processed in cage" in {
    val cageResponse = tryHttpResponse(
      s"$cageInstanceIp${
        conf.getString("WebRecorder.Cage.UrlSuffix")}$subsId/$optionalPid/0")
    assert(cageResponse.contains("The request contains bad syntax or cannot be fulfilled."))
  }

  "Check cage read api for recording" in {
    val cageResponse = tryHttpResponse(
      s"$cageInstanceIp/recording/recording/v1/$subsId/$optionalPid/${
        recPerfectSessionDetails.sid}")
    assert(!cageResponse.contains(" ")&&cageResponse.length>0)
  }

  "Check cage read api for events" in {
    val cageResponse = tryHttpResponse(
      s"$cageInstanceIp/recording/events/v1/$subsId/$optionalPid/${
        recPerfectSessionDetails.sid}")
    assert(!cageResponse.contains(" ")&&cageResponse.length>0)
  }

  "Check cage read api for streams" in {
    val cageResponse = tryHttpResponse(
      s"$cageInstanceIp/recording/streams/v1/$subsId/$optionalPid/${
        recPerfectSessionDetails.sid}")
    assert(!cageResponse.contains(" ")&&cageResponse.length>0)
  }

  "Check cage read api for webPage" in {
    val cageResponse = tryHttpResponse(
      s"$cageInstanceIp/recording/webPage/v1/$subsId/$optionalPid/${
        recPerfectSessionDetails.sid}")
    assert(!cageResponse.contains(" ")&&cageResponse.length>0)
  }

  "Check cage read api for recording is encoded xml" in {
    val cageResponse = tryHttpResponse(
      s"$cageInstanceIp${
        conf.getString("WebRecorder.Cage.UrlSuffix")}$subsId/$optionalPid/${
        recSmallSessionDetails.sid}")
    assert(cageResponse.toString().split("&amp").size>500)
  }

  "Check cage read api for performance" in {
    //only for storagemaster
    val cageResponse = tryHttpResponse(
      s"$cageInstanceIp/performance")
    assert(cageResponse.contains("kafkaRecordingsConsumedMessagesCount")&&cageResponse.length>0)
  }

  "Check cage read api for statistics" in {
    //only for storagemaster
    val cageResponse = tryHttpResponse(
      s"$cageInstanceIp/statistics")
    assert(cageResponse.contains("26952")&&cageResponse.length>0)
  }

  "Check cage read api for sessions" in {
    //only for storagemaster
    val cageResponse = tryHttpResponse(
      s"$cageInstanceIp/sessions")
    assert(cageResponse.contains(recPerfectSessionDetails.sid)&&cageResponse.contains(recPerfectSessionDetails.uid)&&cageResponse.contains(recPerfectSessionDetails.pid)&&cageResponse.length>0)
  }

  "Check cage read api for configuration" in {
    //only for storagemaster
    val cageResponse = tryHttpResponse(
      s"$cageInstanceIp/serviceconfig")
    assert(cageResponse.contains("StreamPublisherRegistrationConfigurationCollection")&&cageResponse.length>0)
  }

  "Check cage read api for binstorage webPage" in {
    val cageResponse = tryHttpResponse(
      s"$cageInstanceIp/api/webpage/v1/$subsId/$optionalPid/${
        recPerfectSessionDetails.sid}")
    assert(cageResponse.contains("<!doctype html>"))
    assert(cageResponse.indexOf("<?xml")==0)
    assert(cageResponse.contains("<load"))
  }

  "Check cage read api for binstorage playback" in {
    val cageResponse = tryHttpResponse(
      s"$cageInstanceIp/api/playback/v1/$subsId/$optionalPid/${
        recPerfectSessionDetails.sid}")
    assert(cageResponse.indexOf("<?xml")==0)
    assert(cageResponse.contains("<load"))
    assert(cageResponse.contains("<!doctype html>"))
  }
  /**
    * Tries to connect n times via HTTP request
    *
    * @param url        the url to try
    * @param numOfTries optional field symbolizing the number of tries to connect.
    * @return the string of the response
    */
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

