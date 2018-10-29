package com.clicktale.pipeline.framework.storage

import java.io.{BufferedInputStream, ByteArrayOutputStream, InputStream}
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Calendar

import cats.instances.byte
import com.amazonaws.regions.Regions
import com.clicktale.pipeline.common.dal.storage.AmazonS3StorageProvider
import com.google.gson.Gson
import com.clicktale.pipeline.dataObjects.{AuthResponse, CageArchivePackage}
import com.clicktale.pipeline.framework.dal.ConfigParser.conf
import com.clicktale.pipeline.framework.storage.AvroDeserializer.VersionedRecording
import oracle.net.aso.i

import scala.io.{BufferedSource, Source}
import scala.util.{Failure, Success, Try}

object GetFromS3 {

  private def md5(s: String): String = {
    var res=toHex(MessageDigest.getInstance("MD5").digest(s.getBytes("UTF-8")))
    res = res.substring(0,Math.min(res.length,4))
    return res
  }

  private def toHex(bytes: Array[Byte]): String = bytes.map( "%02x".format(_) ).mkString("")

  def checkIfBucketExistsInS3(sessionDetails: AuthResponse): Boolean = {

    val amazonS3StorageProvider = AmazonS3StorageProvider(
      conf.getString(s"WebRecorder.S3.${conf.getString("WebRecorder.Current.Environment")}.AccessKey"),
      conf.getString(s"WebRecorder.S3.${conf.getString("WebRecorder.Current.Environment")}.SecretKey"),
      Regions.US_EAST_1.getName
    )
    println("\n***** Searching archive package for SID : " + sessionDetails.sid.toString+ " in S3\n")
    Thread.sleep(1000)
    val formatter =
      new SimpleDateFormat(md5(sessionDetails.sid) + "-" + conf.getString(s"WebRecorder.S3.${conf.getString("WebRecorder.Current.Environment")}.DateFormat"))
    amazonS3StorageProvider.s3Client.doesObjectExist(conf.getString(s"WebRecorder.S3.${conf.getString("WebRecorder.Current.Environment")}.ObjectPrefix") + formatter.format(Calendar.getInstance().getTime), sessionDetails.sid.toString + conf.getString(s"WebRecorder.S3.${conf.getString("WebRecorder.Current.Environment")}.ObjectExtension"))
  }

  def loadFromCage(subsId:Int, pid: Int, sessionId: Long, numOfRetries: Int = 10): CageArchivePackage = {

    println(s"${conf.getString(s"WebRecorder.Cage.${conf.getString("WebRecorder.Current.Environment")}.DNS")}${conf.getString("WebRecorder.Cage.UrlSuffix")}$subsId/$pid/$sessionId")
    println(s"${conf.getString(s"WebRecorder.Cage.${conf.getString("WebRecorder.Current.Environment")}.DNSPipe")}${conf.getString("WebRecorder.Cage.UrlSuffix")}$subsId/$pid/$sessionId")
    println(s"http://nv-p1-elb-ext-central-cage-r-01-498798595.us-east-1.elb.amazonaws.com/recording/v1/$subsId/$pid/$sessionId")
    Thread.sleep(1000)
    for (a <- 1 until numOfRetries) {
      val request = Source.fromURL(
//        s"http://nv-p1-elb-ext-central-cage-r-01-498798595.us-east-1.elb.amazonaws.com/recording/v1/$subsId/$pid/$sessionId")
        s"${conf.getString(s"WebRecorder.Cage.${conf.getString("WebRecorder.Current.Environment")}.DNS")}${conf.getString("WebRecorder.Cage.UrlSuffix")}$subsId/$pid/$sessionId")
//        s"${conf.getString(s"WebRecorder.Cage.${conf.getString("WebRecorder.Current.Environment")}.DNSPipe")}${conf.getString("WebRecorder.Cage.UrlSuffix")}$subsId/$pid/$sessionId")
      Try(new Gson().fromJson(request.mkString, classOf[CageArchivePackage])) match {
        case Success(x) => return x
        case Failure(x) =>
          println(s"Error loading from cage ${request.mkString}")
          Thread.sleep(30000)
      }
    }
    throw new Exception("Failed to find bucket for session ID: " + sessionId)
  }

  def loadFromCageNoFail(subsId:Int, pid: Int, sessionId: Long, numOfRetries: Int = 10): CageArchivePackage = {
    println(s"${conf.getString(s"WebRecorder.Cage.${conf.getString("WebRecorder.Current.Environment")}.DNS")}${conf.getString("WebRecorder.Cage.UrlSuffix")}$subsId/$pid/$sessionId")
    Thread.sleep(1000)
    for (a <- 1 until numOfRetries) {
      val request = Source.fromURL(
        s"${conf.getString(s"WebRecorder.Cage.${conf.getString("WebRecorder.Current.Environment")}.DNS")}${conf.getString("WebRecorder.Cage.UrlSuffix")}$subsId/$pid/$sessionId")
      Try(new Gson().fromJson(request.mkString, classOf[CageArchivePackage])) match {
        case Success(x) => return x
        case Failure(x) =>
          println(s"Error loading from cage ${request.mkString}")
          Thread.sleep(30000)
      }
    }
    return null
  }

  def loadAvroFromCage(sessionId: Long): Map[Long, VersionedRecording] = {
    println(s"${conf.getString(s"WebRecorder.Cage.${conf.getString("WebRecorder.Current.Environment")}.DNS")}/events/v1/$sessionId")
    Thread.sleep(1000)

    val request: BufferedSource = Source.fromURL(
      s"${conf.getString(s"WebRecorder.Cage.${conf.getString("WebRecorder.Current.Environment")}.DNS")}/events/v1/$sessionId")

    AvroDeserializer.unzipRecordings(request)
  }



  def loadAvroFromCage22(sessionId: Long): String = {
    println(s"${conf.getString(s"WebRecorder.Cage.${conf.getString("WebRecorder.Current.Environment")}.DNS")}/events/v1/$sessionId")
    Thread.sleep(1000)
    for (a <- 1 until 10) {
      val request = Source.fromURL(
        s"${conf.getString(s"WebRecorder.Cage.${conf.getString("WebRecorder.Current.Environment")}.DNS")}/events/v1/$sessionId")
      Try(request.mkString) match {
        case Success(x) => return x
        case Failure(x) =>
          println(s"Error loading Avro from cage ${request.mkString}")
          Thread.sleep(30000)
      }
    }
    throw new Exception("Failed to find bucket for session ID" + sessionId)
  }
}
