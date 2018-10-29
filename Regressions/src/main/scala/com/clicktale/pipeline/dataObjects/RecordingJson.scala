package com.clicktale.pipeline.dataObjects

import org.json4s.{DefaultFormats, FieldSerializer}
import org.json4s.JsonAST.JField
import org.json4s.native.Serialization
import com.clicktale.pipeline.processor.logic.calculation.JavaScriptErrorDetails

case class RecordingJson(activeTime: Int,
                         authenticationUnixTime: Long,
                         bodyCheck: Option[Long],
                         campaign: String,
                         city: String,
                         clicksBelowFold: Int,
                         clicksOnLinkLikeElements: Int,
                         clicksOnNonLinkLikeElements: Int,
                         clientDate: Long,
                         clientHeightFinal: Int,
                         clientHeightInitial: Int,
                         clientRecordingDateTimeUtc: String,
                         clientRecordingDateTimeZone: String,
                         clientWidthFinal: Int,
                         clientWidthInitial: Int,
                         content: String,
                         contentSize: Int,
                         contentSourceID: Int,
                         countJSError: Int,
                         countMouseDown: Int,
                         countMouseMove: Int,
                         countNonClickTaleJsErrors: Int,
                         country: String,
                         countryCode: String,
                          countScrollX: Int,
                          countScrollY: Int,
                          description: String,
                          device: String,
                          deviceTypeID: Int,
                          domain: String,
                          exposure: Int,
                          formCount: Int,
                          htmlCharset: String,
                          htmlCheck: Option[Long],
                          XMLSize: Int,
                         DSRSize: Int,
                         HTMLSize: Int,
                          isTouch: Boolean,
                          JsErrors: Array[JavaScriptErrorDetails],
                          languageCode: String,
                          length: Long,
                          location: String,
                          maxScrollPercent: Int,
                          medium: String,
                          messageCount: Int,
                          messagesSize: Int,
                          navigator: String,
                          operatingSystem: String,
                          originalSID: Long,
                          physicalDisplayHeight: Option[Int],
                          physicalDisplayWidth: Option[Int],
                          PID: Int,
                          platform: String,
                          populationType: String,
                          previousSubmitIndication: Option[Boolean],
                          processingDateTimeUtc: String,
                          processingDateTimeZone: String,
                          recorderIsMobile: Boolean,
                          recordingDate: Long,
                          recordingDateJs: Long,
                          recordingEndDate: Long,
                          referrer: String,
                         regionCode: String,
                         region: String,
                          SID: Long,
                         sizeScrollH: Int,
                         sizeScrollW: Int,
                         sizeWndEndH: Int,
                         sizeWndEndW: Int,
                         sizeWndIniH: Int,
                         sizeWndIniW: Int,
                          submitCount: Int,
                          subscriberId: Int,
                          Tags: Array[TagMetadata],
                          tapCount: Int,
                          term: String,
                          timeDOMLoad: Long,
                          timeExitPause: String,
                          timeInit: Long,
                          timeLoad: Long,
                          timeStart: Long,
                          timeToFirstClick: String,
                          timeUnload: Option[Long],
                          timeZone: Int,
                          UID: Long,
                          userAgent: String,
                          version: Option[Int],
                          versionMinor: Option[Int],
//                          webpageHash: Array[Byte],

                          WRAttemptedCounter: Long,
                          WRCheckedCounter: Long,
                          WRGlobalCounter: Long,
                          WRRecordedCounter: Long,
                          zoomCount: Int
                           )
  case class TagMetadata(tagName: String, tagTypeId: Int)
  object RecordingJson {
    def apply(jsonString : String): RecordingJson = {
      implicit val formats = DefaultFormats.preservingEmptyValues + FieldSerializer[TagMetadata](deserializer = {
        case JField(name, x) => JField(name.head.toLower + name.substring(1), x)
      }) + FieldSerializer[RecordingJson](
        { case (name, x) => Some(name.head.toUpper + name.substring(1), x) }, //Serialize
        { // Deserialization:
          case p@JField(name, x) if name.length > 1 && Character.isUpperCase(name.charAt(1)) => p // Field names like IP, HTMLSize stay the same
          case JField(name, x) => JField(name.head.toLower + name.substring(1), x)
        })
      Serialization.read[RecordingJson](jsonString)
    }
}
/*
object TagMetadata{
  def apply(tagJson: String): TagMetadata =  {
    implicit val formats = org.json4s.DefaultFormats
    println(tagJson)
    parse(tagJson).extract[TagMetadata]
  }
}
 */