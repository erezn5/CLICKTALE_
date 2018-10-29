package com.clicktale.pipeline.dataObjects

import com.clicktale.pipeline.framework.dal.ConfigParser.conf

import scala.collection.JavaConversions._
import scala.xml.{Elem, Node, XML}


class RecordingXmlMobile(
                          val ip : String,
                          val userAgent : String,
                          val contentSource : String,
                          val fetchFrom : String,
                          val location : String,
                          val t : String,
                          val navigator : String,
                          val platform : String,
                          val scrw : String,
                          val scrh : String,
                          val scrx : String,
                          val scry : String,
                          val scrDepth : String,
                          val itime : String,
                          val timeZone : String,
                          val w : String,
                          val h : String,
                          val cw : String,
                          val ch : String,
                          val eventMask : String,
                          val cookieFlag : String,
                          val languageCode : String,
                          val uid : String,
                          val uid2 : String,
                          val referrer : String,
                          val country : String,
                          val countryCode : String,
                          val webPageHash : String,
                          val version : String,
                          val length : String,
                          val activeTime : String,
                          val rtime : String,
                          val errorCount : String,
                          val messagesCount : String,
                          val messagesSize : String,
                          val pid : String,
                          val sid : String,
                          val tapCount : String,
                          val originalLocation : String,
                          val enriched : String,
                          val sw : String,
                          val sh : String,
                          val pageContentSize : String,
                          val recorderVersionMajor : String,
                          val recorderVersionMinor : String,
                          val historyLength : String,
                          val isClassifiedInThisSession : String,
                          val lh : String,
                          val lw : String,
                          val orientation : String,
                          val isRecorderMobile : String,
                          val exposure : String,
                          val deviceType : String,
                          val device : String,
                          val operatingSystem : String,
                          val physicalDisplayHeight : String,
                          val physicalDisplayWidth : String,
                          val devicePixelRatio : String,
                          val globalPageviewCounter : String,
                          val attemptedPageviewCounter : String,
                          val checkedPageviewCounter : String,
                          val recordedPageviewCounter : String,
                          val clicksOnLinkLikeElements : String,
                          val clicksOnNonLinkLikeElements : String,
                          val clicksBelowFold : String,
                          val timeToFirstClick : String
                         ) {
  /**
    * Checks that every single member of both objects are equal
    * @param other the other object
    * @return equality boolean
    */
  override def equals(other: Any): Boolean = {
    if (other.isInstanceOf[RecordingXmlMobile]) {
      import RecordingXmlDesktop.conf
      val classMemberList = conf.getStringList("WebRecorder.MobileXmlAttributes")
      classMemberList.count(member =>
        this.getClass.getDeclaredMethod(member).invoke(this)
          .asInstanceOf[String] != other.getClass.getDeclaredMethod(member)
          .invoke(other).asInstanceOf[String]) == 0
    }
    else false
  }
}

object RecordingXmlMobile {
  def apply(xmlString : String): RecordingXmlMobile = {
    val xmlElem : Elem = XML.loadString(xmlString)
    val recordingTag = (xmlElem \\ "recording").head
    val valArray = conf.getStringList("WebRecorder.MobileXmlAttributes")
      .map(attribute => extractStringFromOption(recordingTag.attribute(attribute)))
    val cTor = classOf[RecordingXmlMobile].getConstructors.head
    cTor.newInstance(valArray : _*).asInstanceOf[RecordingXmlMobile]
  }

  def extractStringFromOption : Option[Seq[Node]] => String = {
    case Some(x) => x.toString
    case None => ""
  }
}
