package com.clicktale.pipeline.framework.helpers

import java.io.{File, PrintWriter}

import com.clicktale.pipeline.dataObjects.SessionStream
import com.clicktale.pipeline.framework.dal.ConfigParser.conf


object UrlBuilder {


  def retry[T](times: Int)(fn: => T): Option[T] =
    (1 to times).view flatMap (n => try Some(fn) catch {
      case e: Exception => None
    }) headOption

  def defaultSettingsPID(): Int =
    conf.getString(s"WebRecorder.Environments.${conf.getString("WebRecorder.Current.Environment")}.Pid").toInt

  def defaultSettingsProtocol(): String =
    conf.getString("WebRecorder.Protocol")

  def defaultSettingsDnsName(): String =
    conf.getString(s"WebRecorder.Environments.${conf.getString("WebRecorder.Current.Environment")}.BaseUrl")

  def buildAuthUrl(optionalMobile: Boolean = false,
                   pid: Int = -1,
                   subsId: Int = -1,
                   uid: Long = -1,
                   authSuffix: String = "",
                   dnsName: String = ""): String = {

    val authUrl =
      (if (dnsName == "") defaultSettingsDnsName() else dnsName) +
      (if (pid == -1) "auth?pid=" + defaultSettingsPID() else "auth?pid=" + pid) +
      (if(subsId == -1) "" else s"&subsid=${subsId.toString}") +
      (if (uid == (-1)) "" else s"&uid=${uid.toString}") +
      (if (optionalMobile) s"&m=1" else "") +
      authSuffix

    println(authUrl)
    authUrl
  }

  def buildStreamUrl(dnsName: String): String = {
    dnsName + "wr/?"
  }

  def buildStream(sid: String,
                  streamData: SessionStream,
                  optionalPid: Int = -1,
                  subsId : Int = -1,
                  dnsName: String = "",
                  isV16Protocol: Boolean = false,
                  optionalUrlSuffix:String = ""): String = {

    val finalDnsName: String =
      if (dnsName == ""){defaultSettingsDnsName()}
      else dnsName

    val streamDataFlag: String =
      if (isV16Protocol) (streamData.dataFlagType + 256).toString
      else streamData.dataFlagType.toString

    val pId = if (optionalPid == -1) defaultSettingsPID() else optionalPid
    val streamUrl = buildStreamUrl(finalDnsName) + sid + "&" + pId.toString + "&" + defaultSettingsProtocol() + "&" +
      streamData.messageNumber.toString + "&" + streamData.streamId.toString + "&" +
      streamData.streamMessageId.toString + "&" + streamDataFlag +
      (if(subsId==(-1)) "" else "&subsid="+subsId.toString) +
      optionalUrlSuffix
    streamUrl
  }

  def buildStreamV16(sid: String,
                     streamData: SessionStream,
                     subsId: Int = -1,
                     dnsName: String = ""): String = {

    val finalDnsName: String =
      if (dnsName == ""){defaultSettingsDnsName()}
      else dnsName

    val stream =
      buildStreamUrl(finalDnsName) + sid + "&" + defaultSettingsPID().toString + "&" + "10" + "&" +
        streamData.messageNumber.toString + "&" + streamData.streamId.toString + "&" +
        streamData.streamMessageId.toString + "&" + (streamData.dataFlagType + 256).toString +
        (if(subsId==(-1)) "" else "&subsid="+subsId.toString)
    stream
  }


  def writeToFile(filename: String, content: String): Unit = {
    val writer = new PrintWriter(new File(filename))
    writer.write(content)
    writer.close()
  }
}
