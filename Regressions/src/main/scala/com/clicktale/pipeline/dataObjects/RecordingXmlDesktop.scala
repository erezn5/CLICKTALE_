package com.clicktale.pipeline.dataObjects

import com.typesafe.config.ConfigFactory

import scala.collection.JavaConversions._
import scala.xml.{Elem, Node, XML}

/**
  * Created by Idan Midroni on 02/11/2016.
  */
class RecordingXmlDesktop(
                           val ip: String,
                           val userAgent: String,
                           val contentSource: String,
                           val fetchFrom: String,
                           val location: String,
                           val t: String,
                           val navigator: String,
                           val platform: String,
                           val scrw: String,
                           val scrh: String,
                           val scrx: String,
                           val scry: String,
                           val scrDepth: String,
                           val itime: String,
                           val timeZone: String,
                           val w: String,
                           val h: String,
                           val cw: String,
                           val ch: String,
                           val eventMask: String,
                           val cookieFlag: String,
                           val languageCode: String,
                           val uid: String,
                           val uid2: String,
                           val referrer: String,
                           val country: String,
                           val countryCode: String,
                           val webPageHash: String,
                           val version: String,
                           val length: String,
                           val activeTime: String,
                           val rtime: String,
                           val errorCount: String,
                           val messagesCount: String,
                           val messagesSize: String,
                           val pid: String,
                           val sid: String,
                           val tapCount: String,
                           val originalLocation: String,
                           val enriched: String,
                           val sw: String,
                           val sh: String,
                           val pageContentSize: String,
                           val recorderVersionMajor: String,
                           val recorderVersionMinor: String,
                           val historyLength: String,
                           val isClassifiedInThisSession: String,
                           val lh: String,
                           val lw: String,
                           val orientation: String,
                           val isRecorderMobile: String,
                           val exposure: String,
                           val deviceType: String,
                           val operatingSystem: String,
                           val devicePixelRatio: String,
                           val globalPageviewCounter: String,
                           val attemptedPageviewCounter: String,
                           val checkedPageviewCounter: String,
                           val recordedPageviewCounter: String,
                           val clicksOnLinkLikeElements: String,
                           val clicksOnNonLinkLikeElements: String,
                           val clicksBelowFold: String,
                           val timeToFirstClick: String
                         ) {
  /**
    * Checks that every single member of both objects are equal
    * @param other the other object
    * @return equality boolean
    */
  override def equals(other: Any): Boolean = {
    if (other.isInstanceOf[RecordingXmlDesktop]) {
      import RecordingXmlDesktop.conf
      val classMemberList = conf.getStringList("WebRecorder.DesktopXmlAttributes")
      classMemberList.count(member =>
        this.getClass.getDeclaredMethod(member).invoke(this)
          .asInstanceOf[String] != other.getClass.getDeclaredMethod(member)
          .invoke(other).asInstanceOf[String]) == 0
    }
    else false
  }
}

object RecordingXmlDesktop {
  val conf = ConfigFactory.load

  def apply(xmlString: String): RecordingXmlDesktop = {
    val xmlElem: Elem = XML.loadString(xmlString)
    val recordingTag = (xmlElem \\ "recording").head
    val valArray = conf.getStringList("WebRecorder.DesktopXmlAttributes")
      .map(attribute => extractStringFromOption(recordingTag.attribute(attribute)))
    val construct = classOf[RecordingXmlDesktop].getConstructors.head
    construct.newInstance(valArray: _*).asInstanceOf[RecordingXmlDesktop]
  }

  def extractStringFromOption: Option[Seq[Node]] => String = {
    case Some(x) => x.toString
    case None => ""
  }
}
