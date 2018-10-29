package com.clicktale.pipe.diff.model

import java.nio.file.{Files, Paths}

import org.scalatest.FlatSpec

class ModelTest extends FlatSpec {

  "SessionKey" should "be parsed correctly from analytics json (1)" in {

    val jsonStr = new String(Files.readAllBytes(
      Paths.get(getClass.getClassLoader.getResource("./amqp-msg-1.json").getPath)))

    import SessionKey._
    assert(fromJsonAnalytics(jsonStr) ==
      Right(SessionKey(ProjectId(26952, 422), 1870786132934683L)))
  }


  it should "be parsed correctly from analytics json (2) " in {

    val jsonStr = """{"ActiveTime":20526,"AuthenticationUnixTime":1534405055243,"BodyCheck":null,"Campaign":null,"City":"Tiberias","CityID":null,"ClicksBelowFold":0,"ClicksOnLinkLikeElements":0,"ClicksOnNonLinkLikeElements":0,"ClientDate":1534405055243,"ClientDateTime":0,"ClientHeightFinal":615,"ClientHeightInitial":615,"ClientRecordingDateTimeUtc":"2018-08-16 07:37:35.243","ClientRecordingDateTimeZone":"2018-08-16 02:37:35.243","ClientWidthFinal":1263,"ClientWidthInitial":1263,"Content":null,"ContentSize":160576,"ContentSourceID":2,"CountJSError":0,"CountMouseDown":0,"CountMouseMove":1,"CountNonClickTaleJsErrors":0,"Country":"Israel","CountryCode":"IL","CountryID":null,"CountScrollX":0,"CountScrollY":0,"CreateDate":"2018-08-16T07:37:35.243","Description":"","Device":"","DeviceID":null,"DeviceTypeID":1,"Domain":"www.homedepot.com","DomainID":null,"ErrorCount":0,"Exposure":18,"FetchURL":"","FormCount":3,"FullRecordingData":"","HtmlCharset":"UTF-8","HtmlCheck":null,"HttpCharset":"","IP":null,"IsPhysicalSizeDefault":null,"IsTouch":false,"JsErrors":[],"LanguageCode":"en-US","LayoutViewportWidth":1263,"Length":20527,"Location":"http://www.homedepot.com/","LocationID":-1,"MaxScrollPercent":18,"Medium":null,"MessageCount":35,"MessagesSize":0,"Navigator":"Ch57","OperatingSystem":"other","OperatingSystemID":null,"OrientationChangeCount":0,"OriginalSID":0,"PhysicalDisplayHeight":null,"PhysicalDisplayWidth":null,"PID":578979578,"Platform":"Win32","PreviousSubmitIndication":null,"PopulationType":null,"ProcessingDateTimeUtc":"2018-08-16 07:37:41.564","ProcessingDateTimeZone":"2018-08-16 02:37:41.564","RecorderIsMobile":false,"RecordingDate":1534405055243,"RecordingGUID":"c0d56e31-6ecb-49ba-9ab0-e73a3433db29","RecordingDateJs":1534405055243,"RecordingEndDate":1534405075770,"Referrer":null,"ReferrerID":-1,"Region":"Northern District","RegionCode":"Z","RegionID":null,"SID":4977517240224097854,"SizeScrollH":3338,"SizeScrollW":1263,"SizeScrW":1280,"SizeScrH":720,"TrueSizeScrW":1280,"TrueSizeScrH":720,"SizeWndEndH":615,"SizeWndEndW":1280,"SizeWndIniH":615,"SizeWndIniW":1280,"Source":"Direct","SubmitCount":0,"SubscriberId":742520436,"Tags":[{"TagName":"Config: Release_20170503","TagTypeID":0},{"TagName":"Onload | homepage","TagTypeID":0},{"TagName":"Onload | pageType: homepage","TagTypeID":0},{"TagName":"Onload | guest","TagTypeID":0},{"TagName":"Onload | Site Section: homepage","TagTypeID":0},{"TagName":"Onload | THD_FORCE_LOC : 2","TagTypeID":0},{"TagName":"SSR: THD_FORCE_LOC null to 2","TagTypeID":0},{"TagName":"CEC Onload | User state: Guest","TagTypeID":0},{"TagName":"MCVID:85470864210153246070977938746591733077","TagTypeID":0},{"TagName":"UploadPage Timeout","TagTypeID":0}],"TapCount":0,"Term":null,"TimeDOMLoad":13310,"TimeExitPause":200,"TimeInit":19999,"TimeLoad":7593,"TimeStart":7609,"TimeToFirstClick":null,"TimeUnload":20527,"TimeWebPageFetch":0,"TimeZone":-420,"TrafficCampaignID":-1,"TrafficContentID":-1,"TrafficMediumID":-1,"TrafficSourceID":-1,"TrafficTermID":-1,"UID":1162585740,"UserAgent":"Apache-HttpClient/4.5.2 (Java/1.8.0_151)","UserAgentID":-1,"Version":16,"VersionMinor":3,"WebPageHash":"B1hlTZQATUNnzlhFrLn5LYGW32k=","WRAttemptedCounter":1,"WRCheckedCounter":1,"WRGlobalCounter":1,"WRRecordedCounter":1,"XMLSize":9268,"DSRSize":160378,"HTMLSize":160576,"ZoomCount":0,"MachineName":"WIN-D1NE1J4RB6A","IpAddress":"172.22.6.26","ProcessVersion":"1.0.1.134","ProcessingStartTimeUtc":"2018-08-16T07:37:38.1985978Z","ProcessingEndTimeUtc":"2018-08-16T07:37:41.6584906Z","ProcessingElapsedMilliseconds":3459, "cage_ip":"172.22.0.117", "cage_version":"2.2.3", "cage_time":1534405061636,"avroSize":2678,"avroCompressedSize":1659} """

    import SessionKey._

    assert(fromJsonAnalytics(jsonStr) ==
      Right(SessionKey(ProjectId(578979578, 742520436), 4977517240224097854L)))
  }

}
