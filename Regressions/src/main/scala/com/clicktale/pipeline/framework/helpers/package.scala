package com.clicktale.pipeline.framework

import java.io.DataInputStream
import java.util.zip.ZipInputStream

import org.apache.commons.codec.digest.DigestUtils
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.ByteArrayEntity
import org.apache.http.impl.client.HttpClientBuilder
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import com.clicktale.pipeline.framework.dal.ConfigParser.conf

abstract class PathStrategies {
  final val epoch = new DateTime(2015,1,1,0,0)

  def getPath(sessionId: Long, subscriberId: Int, projectId: Int): String

  def toShortDateFormat(sessionId: Long): String = {
    val date = epoch.plus(sessionId >> 14)
    val format = DateTimeFormat.forPattern("yyMMdd")
    format.print(date)
  }
}

object DatePathStrategy extends PathStrategies {

  override def getPath(sessionId: Long, subscriberId: Int, projectId: Int): String = {
    val date = toShortDateFormat(sessionId)
    val path = s"d-$date/$sessionId.zip"
    path
  }
}

object MD5PathStrategy extends PathStrategies {
  override def getPath(sessionId: Long, subscriberId: Int, projectId: Int): String = {
    val md5Prefix = DigestUtils.md5Hex(sessionId.toString).toUpperCase.replace("/","").substring(0, 4)
    val date = toShortDateFormat(sessionId)
    val path = s"${md5Prefix}-${date}/${subscriberId}_${projectId}_${sessionId}.zip"
    path
  }
}

object Avro {

  def get(sid:String,eventsType:String):ZipInputStream = {
    val httpPost = new HttpPost(s"${conf.getString(s"WebRecorder.Cage.${conf.getString("WebRecorder.Current.Environment")}.DNS")}/$eventsType/v1/1")
    val entity = new ByteArrayEntity(s"$sid,$sid".getBytes())
    httpPost.setEntity(entity)
    val client = HttpClientBuilder.create().build()
    val response = client.execute(httpPost)
    val zipStream = new ZipInputStream(response.getEntity.getContent)
    return zipStream
  }

  def getVersionForSid(sid:String,eventsType:String) {
    val httpPost = new HttpPost(s"${conf.getString(s"WebRecorder.Cage.${conf.getString("WebRecorder.Current.Environment")}.DNS")}/$eventsType/v1/1")
    val entity = new ByteArrayEntity(s"$sid,$sid".getBytes())
    httpPost.setEntity(entity)
    val client = HttpClientBuilder.create().build()
    val response = client.execute(httpPost)
    val zipStream = new ZipInputStream(response.getEntity.getContent)
    Stream.continually(zipStream.getNextEntry).takeWhile(_ != null).foreach {
      entry =>
        val dataInputStream = new DataInputStream(zipStream)
        val ver = dataInputStream.readShort().toString()
        client.close()
        return ver
    }
  }
}