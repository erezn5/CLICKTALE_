package com.clicktale.pipeline.framework.senders

import java.net.{HttpURLConnection, URL}
import java.util.Base64
import com.clicktale.pipeline.dataObjects.{AuthResponse, SessionStream}
import com.clicktale.pipeline.framework.dal.ConfigParser.{conf, formats}
import com.clicktale.pipeline.framework.helpers.UrlBuilder.{buildAuthUrl, buildStream}
import org.apache.commons.io.IOUtils
import org.apache.http.client.methods.{HttpGet, HttpPost}
import org.apache.http.entity.ByteArrayEntity
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.log4j.Logger
import org.joda.time.{DateTime, DateTimeZone}
import org.json4s._
import org.json4s.native.JsonMethods._
import scala.io.Source
import scala.util.{Failure, Success, Try}

object SendSession {
  def sendGetRequest(url: String,
                     ua: String = "",
                     domain: String = "",
                     referrer: String = ""): String = {
    //println(url)
    val httpGet =
      new HttpGet(url)
    // set the desired header values
    if (domain != "")
      httpGet.setHeader("Origin", conf.getString(s"WebRecorder.Environments.${conf.getString("WebRecorder.Current.Environment")}.Domains"))
    if (ua != "")
      httpGet.addHeader("User-Agent", ua)
    if (referrer != "")
      httpGet.addHeader("Referer", referrer)
    try {
      val client =
        HttpClientBuilder.create().build()
      val response =
        client.execute(httpGet)
      val result =
        String.join("", IOUtils.readLines(response.getEntity.getContent))
      println(result)
      client.close()
      result
    }
    catch {
      case e: Exception =>
        Thread.sleep(5000)
        val client =
          HttpClientBuilder.create().build()
        val response =
          client.execute(httpGet)
        val result =
          String.join("", IOUtils.readLines(response.getEntity.getContent))
        client.close()
        println(result)
        result
    }
  }

  def sendPostRequest(url: String,
                      body: String = "",
                      domain: String): String = {
    //println(url)
    val httpPost = new HttpPost(url)
    // set the desired header values
    if (domain != "") httpPost.setHeader("Origin", conf.getString(s"WebRecorder.Environments.${conf.getString("WebRecorder.Current.Environment")}.Domains"))
    val decoded = body.getBytes()
    val entity = new ByteArrayEntity(decoded)
    httpPost.setEntity(entity)
    // execute the request
    val client = HttpClientBuilder.create().build()
    val response = client.execute(httpPost)
    val result = IOUtils.readLines(response.getEntity.getContent).toString
    client.close()
    result
  }

  def sendPostRequest64(url: String,
                        body: String = "",
                        domain: String): String = {
    //println(url)
    val httpPost = new HttpPost(url)
    // set the desired header values
    if (domain != "") httpPost.setHeader("Origin", conf.getString(s"WebRecorder.Environments.${conf.getString("WebRecorder.Current.Environment")}.Domains"))
    val decoded = Base64.getDecoder.decode(body.getBytes())
    val entity = new ByteArrayEntity(decoded)
    httpPost.setEntity(entity)
    // execute the request
    val client = HttpClientBuilder.create().build()
    val response = client.execute(httpPost)
    val result = IOUtils.readLines(response.getEntity.getContent).toString
    client.close()
    result
  }

  def getAuth(authState: String,
              optionalPid: Int = -1,
              optionalMobile: Boolean = false,
              optionalUa: String = "",
              subsId: Int = -1,
              domain: String = conf.getString(s"WebRecorder.Environments.${conf.getString("WebRecorder.Current.Environment")}.Domains"),
              retry: Boolean = true,
              referrer: String = "",
              dnsName: String = "",
             optionalSuffix:String=""): AuthResponse = {
    var authResponse: AuthResponse = null
    for (a <- 1 to 20) {
      authResponse =
        if (dnsName == "") {
          AuthResponse(sendGetRequest(buildAuthUrl(optionalMobile = optionalMobile, pid = optionalPid, subsId = subsId,authSuffix = optionalSuffix), ua = optionalUa, domain = domain, referrer = referrer))
        }
        else {
          AuthResponse(sendGetRequest(buildAuthUrl(optionalMobile = optionalMobile, pid = optionalPid, subsId = subsId, dnsName = dnsName, authSuffix = optionalSuffix), ua = optionalUa, domain = domain, referrer = referrer))
        }
      if (authResponse.userTrackingState == authState)
        return authResponse
      else if (!retry)
        throw new Exception(s"State is not as requested, no retry, Instead got:  $authResponse")
    }
    throw new Exception(s"Failed to get authState - $authState\n Instead got:  $authResponse")
  }

  def getAuthForUID(UID: Long,
                    optionalPid: Int = -1,
                    subsId: Int = -1,
                    domain: String = conf.getString(s"WebRecorder.Environments.${conf.getString("WebRecorder.Current.Environment")}.Domains"),
                    optionalTrackingState: String = "None",
                    retry: Boolean = true,
                    referrer: String = "",
                    dnsName: String = ""): AuthResponse = {
    var authResponse: AuthResponse = null
    for (a <- 1 to 20) {
      authResponse =
        if (dnsName == "") {
          AuthResponse(sendGetRequest(buildAuthUrl(uid = UID, pid = optionalPid, subsId = subsId), domain = domain, referrer = referrer))
        }
        else {
          AuthResponse(sendGetRequest(buildAuthUrl(uid = UID, pid = optionalPid, subsId = subsId, dnsName = dnsName), domain = domain, referrer = referrer))
        }
      if ((authResponse.userTrackingState == optionalTrackingState) || (optionalTrackingState == "None")) {
        return authResponse
      }
      else if (!retry) {
        throw new Exception(s"State is not as requested, no retry, Instead got:  $authResponse")
      }
    }
    authResponse
  }

  def sendStream2(sid: String,
                  pid: Int,
                  subsId: Int,
                  sessionMessage: JObject,
                  domain: String,
                  dnsName: String = ""): String = {
    val isPostMessage: Boolean =
      conf.getString("WebRecorder.Session.ExcludedDataFlagTypes").contains("," + (sessionMessage \ "DataFlagType").extract[String] + ",")
    println((sessionMessage \ "DataFlagType").extract[String])
    val sessionStream = buildStream(sid = sid.toString, streamData = SessionStream(sessionMessage),
      optionalPid = pid, subsId = subsId, dnsName = dnsName)
    if (!isPostMessage)
      sendGetRequest(sessionStream + "&" +
        (sessionMessage \ "Data").extract[String] +
        conf.getString("WebRecorder.Session.RequestAddition"), domain = domain)
    else if ((sessionMessage \ "DataFlagType").extract[String] == "104" || (sessionMessage \ "DataFlagType").extract[String] == "105")
      sendPostRequest64(sessionStream, (sessionMessage \ "Data").extract[String], domain = domain)
    else
      sendPostRequest(sessionStream, (sessionMessage \ "Data").extract[String], domain = domain)
  }

  def pushSessionParallely(recFile: String,
                           authState: String,
                           optionalPid: Int = -1,
                           optionalMobile: Boolean = false,
                           subsId: Int = -1,
                           domain: String = conf.getString(s"WebRecorder.Environments.${conf.getString("WebRecorder.Current.Environment")}.Domains"),
                           retry: Boolean = true,
                           referrer: String = "",
                           dnsName: String = "",
                           ua: String = "",
                           sessionStreamsDelayFromAuth: Int = 0): AuthResponse = {
    val authorizationResponse =
      getAuth(authState, optionalPid = optionalPid, optionalMobile = optionalMobile, subsId = subsId, domain = domain, retry = retry, referrer = referrer, dnsName = dnsName, optionalUa = ua)
    if (sessionStreamsDelayFromAuth > 0) DateTime.now(DateTimeZone.UTC)
    val recStreams: JValue =
      parse(Source.fromInputStream(getClass.getResourceAsStream(recFile)).getLines.mkString)
    var lastMessage: Option[JObject] = None
    val parsedRecStreams: Seq[JObject] = (recStreams \ "LiveSessionDataCollection").extract[Seq[JObject]]
    var count: Int = 0
    Thread.sleep(sessionStreamsDelayFromAuth)


    parsedRecStreams.par.foreach(
      x => {
        val messageType = (x \ "DataFlagType").extract[Int]
        if ((messageType != 1) && (messageType != 9) && (messageType != 257) && (messageType != 265)) {
          sendStream2(authorizationResponse.sid, pid = optionalPid, subsId = subsId, sessionMessage = x, domain = domain, dnsName = dnsName)
          if (sessionStreamsDelayFromAuth > 0) println(DateTime.now(DateTimeZone.UTC))
          count += 1
        }
        else {
          lastMessage = Some(x)
          count += 1
        }
      })
    Try(sendStream2(authorizationResponse.sid, pid = optionalPid, subsId = subsId, sessionMessage = lastMessage.get, domain = domain, dnsName = dnsName)) match {
      case Success(x) => //println(s"last message for sid-${authorizationResponse.sid} was sent")
      case Failure(e) => println(s"no last message for sid-${authorizationResponse.sid}")
    }
    val logger = Logger.getLogger(SendSession.getClass.getName)
    logger.info(s"\n \n ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ \n \n For SessionId: ${authorizationResponse.sid} \n \n The total amount of streams in session recording Json was: ***** ${parsedRecStreams.size} *****. \n The total amount of pushed streams was: ***** $count ***** \n \n ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~")
    authorizationResponse
  }

  def pushSession(recFile: String,
                  authState: String,
                  optionalPid: Int = -1,
                  optionalMobile: Boolean = false,
                  subsId: Int = -1,
                  domain: String = conf.getString(s"WebRecorder.Environments.${conf.getString("WebRecorder.Current.Environment")}.Domains"),
                  retry: Boolean = true,
                  referrer: String = "",
                  dnsName: String = "",
                  eventStream: Int = 5,
                  isV16Protocol: Boolean = false,
                  optionalSuffix: String = ""): AuthResponse  = {
    val authorizationResponse = getAuth(authState, optionalPid = optionalPid, optionalMobile = optionalMobile, subsId = subsId, domain = domain, retry = retry, referrer = referrer,dnsName = dnsName, optionalSuffix=optionalSuffix)
    val fileContents =
      Source.fromInputStream(getClass.getResourceAsStream(recFile)).
        getLines.mkString
    val recStreams: JValue =
      parse(Source.fromInputStream(getClass.getResourceAsStream(recFile)).getLines.mkString)
    var lastMessage: Option[JObject] = None
    val parsedRecStreams: Seq[JObject] = (recStreams \ "LiveSessionDataCollection").extract[Seq[JObject]]
    var count: Int = 0
    Thread.sleep(1000)


    parsedRecStreams.foreach(
      x => {
        val messageType = (x \ "DataFlagType").extract[Int]
        if ((messageType != 1) && (messageType != 9) && (messageType != 257) && (messageType != 265)) {
          sendStream2(authorizationResponse.sid, pid = optionalPid, subsId = subsId, sessionMessage = x, domain = domain, dnsName = dnsName)
          if (1000 > 0) println(DateTime.now(DateTimeZone.UTC))
          count += 1
        }
        else {
          lastMessage = Some(x)
          count += 1
        }
      })
    Try(sendStream2(authorizationResponse.sid, pid = optionalPid, subsId = subsId, sessionMessage = lastMessage.get, domain = domain, dnsName = dnsName)) match {
      case Success(x) => //println(s"last message for sid-${authorizationResponse.sid} was sent")
      case Failure(e) => println(s"no last message for sid-${authorizationResponse.sid}")
    }
    val logger = Logger.getLogger(SendSession.getClass.getName)
    logger.info(s"\n \n ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ \n \n For SessionId: ${authorizationResponse.sid} \n \n The total amount of streams in session recording Json was: ***** ${parsedRecStreams.size} *****. \n The total amount of pushed streams was: ***** $count ***** \n \n ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~")
    authorizationResponse
  }

  def pushSessionForUID(
                         recFile: String,
                         UID: Long,
                         optionalPid: Int = -1,
                         subsId: Int = -1,
                         domain: String = conf.getString(s"WebRecorder.Environments.${conf.getString("WebRecorder.Current.Environment")}.Domains"),
                         optionalTrackingState: String = "None",
                         retry: Boolean = true,
                         referrer: String = "",
                         dnsName: String = ""): AuthResponse = {
    val authorizationResponse = getAuthForUID(UID, optionalPid = optionalPid, domain = domain, subsId = subsId, optionalTrackingState = optionalTrackingState, retry = retry, referrer = referrer, dnsName = dnsName)
    val fileContents =
      Source.fromInputStream(getClass.getResourceAsStream(recFile)).
        getLines.mkString
    val recStreams =
      parse(fileContents)
    for (recordStream <- (recStreams \ "LiveSessionDataCollection").extract[List[JObject]]) {
      //val streamData = (recStreams \ "LiveSessionDataCollection" \ "Data").extract[List[String]]
      val stream = SessionStream(recordStream)
      val sessionStream = {
        buildStream(authorizationResponse.sid.toString, stream, optionalPid = optionalPid, subsId = subsId, dnsName = dnsName)
      }
      if (!conf.getString("WebRecorder.Session.ExcludedDataFlagTypes").contains("," + (recordStream \ "DataFlagType").extract[String] + ","))
        sendGetRequest(sessionStream + "&" + (recordStream \ "Data").extract[String] + conf.getString("WebRecorder.Session.RequestAddition"), domain = domain)
      else
        sendPostRequest(sessionStream, (recordStream \ "Data").extract[String], domain = domain)
    }
    authorizationResponse
  }

  def pushSessionV16(recFile: String,
                     authState: String,
                     protocol: String,
                     eventStream: Int,
                     domain: String = conf.getString(s"WebRecorder.Environments.${conf.getString("WebRecorder.Current.Environment")}.Domains"),
                     referrer: String = "",
                     dnsName: String = "",
                     optionalPid: Int = -1): AuthResponse = {
    val authorizationResponse = getAuth(authState, domain = domain, referrer = referrer, dnsName = dnsName, optionalPid = optionalPid)
    val fileContents =
      Source.fromInputStream(getClass.getResourceAsStream(recFile)).
        getLines.mkString
    val recStreams =
      parse(fileContents)
    for (recordStream <- (recStreams \ "LiveSessionDataCollection").extract[List[JObject]]) {
      //val streamData = (recStreams \ "LiveSessionDataCollection" \ "Data").extract[List[String]]
      val stream = SessionStream(recordStream)
      val sessionStream = {
        if ((recordStream \ "MessageNumber").extract[String].toInt > eventStream)
          buildStream(authorizationResponse.sid.toString, stream, dnsName = dnsName, isV16Protocol = true, optionalPid = optionalPid)
        else
          buildStream(authorizationResponse.sid.toString, stream, dnsName = dnsName, optionalPid = optionalPid)
      }
      if (!(conf getString "WebRecorder.Session.ExcludedDataFlagTypes").contains("," + (recordStream \ "DataFlagType").extract[String] + ","))
        sendGetRequest(sessionStream + "&" + (recordStream \ "Data").extract[String] + conf.getString("WebRecorder.Session.RequestAddition"), domain = domain)
      else
        sendPostRequest(sessionStream, (recordStream \ "Data").extract[String], domain = domain)
    }
    authorizationResponse
  }
}
