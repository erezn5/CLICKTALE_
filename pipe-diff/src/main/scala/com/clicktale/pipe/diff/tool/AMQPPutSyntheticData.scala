package com.clicktale.pipe.diff.tool

import akka.Done
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.alpakka.amqp.scaladsl.AmqpSink
import akka.stream.alpakka.amqp.{AmqpCachedConnectionProvider, AmqpSinkSettings, AmqpUriConnectionProvider, OutgoingMessage}
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.util.ByteString
import com.clicktale.pipe.utils.LazyLogger

import scala.concurrent.Future
import scala.util.Random

object AMQPPutSyntheticData extends LazyLogger {

  def main(args: Array[String]): Unit = {

    val num = 10
    val ratio = 0.3
    val secToExecute = 7

    //     analyticsGenerator(123456, 987654, 10, 0.3) foreach println
    val sidFix = 987654
    val pidFix = 123456
    val g = Source(analyticsGenerator(pidFix, sidFix, num, ratio)).via {
      Flow[(String, (Int, Int, Int, Long))].map {
        case (msg, (pid, sid, uid, ssid)) =>
          val om = outMsg(msg, sid, pid, uid, ssid)
          info("sending another messsage")
          om
      }
    }.to(createAmqpSink(amqp))

    implicit val system: ActorSystem = ActorSystem("AmqpPutter")
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    val m = g.run()

    Thread.sleep(secToExecute * 1000)
    system.terminate()
  }

  val amqp = SimpleAMQPTargetConf(AMQPConnParam("172.22.7.11",
    5672,
    "pipeline",
    "eb72BrfGU6JA6Ne23UIg"),
    "#",
    "pdt0.2.pipe.diff.exchange")

  trait AMQPTargetConf {
    def host: String

    def port: Int

    def username: String

    def password: String

    def routingKey: String

    def exchangeName: String
  }

  case class AMQPConnParam(host: String,
                           port: Int,
                           username: String,
                           password: String)

  case class SimpleAMQPTargetConf(conn: AMQPConnParam,
                                  routingKey: String,
                                  exchangeName: String) extends AMQPTargetConf {
    override def host: String = conn.host

    override def port: Int = conn.port

    override def username: String = conn.username

    override def password: String = conn.password
  }

  private def buildUri(typeQueue: String,
                       host: String,
                       port: Int,
                       username: String,
                       password: String) = {
    val uri = s"amqp://$username:$password@$host:$port"
    info(s"AMQP connection for the $typeQueue is set with: $uri")
    uri
  }

  private def createAmqpSink(conf: AMQPTargetConf): Sink[OutgoingMessage, Future[Done]] = {
    val connectionProvider =
      AmqpCachedConnectionProvider(
        AmqpUriConnectionProvider(
          buildUri("Source", conf.host, conf.port, conf.username, conf.password)))

    val settings = AmqpSinkSettings(connectionProvider,
      Some(conf.exchangeName),
      Some(conf.routingKey))

    AmqpSink(settings)
  }


  def routingKey(sid: Int, pid: Int, uid: Int, ssid: Long) = s"$sid.$pid.$uid.$ssid.${Random.nextInt(9)}"

  def outMsg(msg: String, sid: Int, pid: Int, uid: Int, ssid: Long) =
    OutgoingMessage(ByteString(msg.getBytes),
      immediate = false,
      mandatory = false,
      routingKey = Some(routingKey(sid, pid, uid, ssid)))

  def protoTypeAnalytics(uid: Int,
                         sid: Int,
                         pid: Int,
                         ssid: Long): String =
    s"""{"ActiveTime":20526,"AuthenticationUnixTime":1534405055243,"BodyCheck":null,"Campaign":null,"City":"Tiberias","CityID":null,"ClicksBelowFold":0,"ClicksOnLinkLikeElements":0,"ClicksOnNonLinkLikeElements":0,"ClientDate":1534405055243,"ClientDateTime":0,"ClientHeightFinal":615,"ClientHeightInitial":615,"ClientRecordingDateTimeUtc":"2018-08-16 07:37:35.243","ClientRecordingDateTimeZone":"2018-08-16 02:37:35.243","ClientWidthFinal":1263,"ClientWidthInitial":1263,"Content":null,"ContentSize":160576,"ContentSourceID":2,"CountJSError":0,"CountMouseDown":0,"CountMouseMove":1,"CountNonClickTaleJsErrors":0,"Country":"Israel","CountryCode":"IL","CountryID":null,"CountScrollX":0,"CountScrollY":0,"CreateDate":"2018-08-16T07:37:35.243","Description":"","Device":"","DeviceID":null,"DeviceTypeID":1,"Domain":"www.homedepot.com","DomainID":null,"ErrorCount":0,"Exposure":18,"FetchURL":"","FormCount":3,"FullRecordingData":"","HtmlCharset":"UTF-8","HtmlCheck":null,"HttpCharset":"","IP":null,"IsPhysicalSizeDefault":null,"IsTouch":false,"JsErrors":[],"LanguageCode":"en-US","LayoutViewportWidth":1263,"Length":20527,"Location":"http://www.homedepot.com/","LocationID":-1,"MaxScrollPercent":18,"Medium":null,"MessageCount":35,"MessagesSize":0,"Navigator":"Ch57","OperatingSystem":"other","OperatingSystemID":null,"OrientationChangeCount":0,"OriginalSID":0,"PhysicalDisplayHeight":null,"PhysicalDisplayWidth":null,"PID":$pid,"Platform":"Win32","PreviousSubmitIndication":null,"PopulationType":null,"ProcessingDateTimeUtc":"2018-08-16 07:37:41.564","ProcessingDateTimeZone":"2018-08-16 02:37:41.564","RecorderIsMobile":false,"RecordingDate":1534405055243,"RecordingGUID":"c0d56e31-6ecb-49ba-9ab0-e73a3433db29","RecordingDateJs":1534405055243,"RecordingEndDate":1534405075770,"Referrer":null,"ReferrerID":-1,"Region":"Northern District","RegionCode":"Z","RegionID":null,"SID":$ssid,"SizeScrollH":3338,"SizeScrollW":1263,"SizeScrW":1280,"SizeScrH":720,"TrueSizeScrW":1280,"TrueSizeScrH":720,"SizeWndEndH":615,"SizeWndEndW":1280,"SizeWndIniH":615,"SizeWndIniW":1280,"Source":"Direct","SubmitCount":0,"SubscriberId":$sid,"Tags":[{"TagName":"Config: Release_20170503","TagTypeID":0},{"TagName":"Onload | homepage","TagTypeID":0},{"TagName":"Onload | pageType: homepage","TagTypeID":0},{"TagName":"Onload | guest","TagTypeID":0},{"TagName":"Onload | Site Section: homepage","TagTypeID":0},{"TagName":"Onload | THD_FORCE_LOC : 2","TagTypeID":0},{"TagName":"SSR: THD_FORCE_LOC null to 2","TagTypeID":0},{"TagName":"CEC Onload | User state: Guest","TagTypeID":0},{"TagName":"MCVID:85470864210153246070977938746591733077","TagTypeID":0},{"TagName":"UploadPage Timeout","TagTypeID":0}],"TapCount":0,"Term":null,"TimeDOMLoad":13310,"TimeExitPause":200,"TimeInit":19999,"TimeLoad":7593,"TimeStart":7609,"TimeToFirstClick":null,"TimeUnload":20527,"TimeWebPageFetch":0,"TimeZone":-420,"TrafficCampaignID":-1,"TrafficContentID":-1,"TrafficMediumID":-1,"TrafficSourceID":-1,"TrafficTermID":-1,"UID":$uid,"UserAgent":"Apache-HttpClient/4.5.2 (Java/1.8.0_151)","UserAgentID":-1,"Version":16,"VersionMinor":3,"WebPageHash":"B1hlTZQATUNnzlhFrLn5LYGW32k=","WRAttemptedCounter":1,"WRCheckedCounter":1,"WRGlobalCounter":1,"WRRecordedCounter":1,"XMLSize":9268,"DSRSize":160378,"HTMLSize":160576,"ZoomCount":0,"MachineName":"WIN-D1NE1J4RB6A","IpAddress":"172.22.6.26","ProcessVersion":"1.0.1.134","ProcessingStartTimeUtc":"2018-08-16T07:37:38.1985978Z","ProcessingEndTimeUtc":"2018-08-16T07:37:41.6584906Z","ProcessingElapsedMilliseconds":3459, "cage_ip":"172.22.0.117", "cage_version":"2.2.3", "cage_time":1534405061636,"avroSize":2678,"avroCompressedSize":1659} """.stripMargin

  def analyticsGenerator(pid: Int,
                         sid: Int,
                         num: Int,
                         ratio: Double): List[(String, (Int, Int, Int, Long))] = {
    if (ratio > 1 || ratio < 0) throw new Exception("ratio not correctly define")
    if (num < 0) throw new Exception("number of analytics must be positive")

    val rl = () => Math.abs(Random.nextLong())
    val ri = () => Math.abs(Random.nextInt())

    val matchIds = Math.round(num * ratio)
    val notMatchIds = num - matchIds
    import Random._

    def semiRandMsg: (String, (Int, Int, Int, Long)) = {
      val uid = ri()
      val ssid = rl()
      (protoTypeAnalytics(uid, sid, pid, ssid), (pid, sid, uid, ssid))
    }

    def randMsg: (String, (Int, Int, Int, Long)) = {
      val uid = ri()
      val pid = ri()
      val sid = ri()
      val ssid = rl()
      (protoTypeAnalytics(uid, sid, pid, ssid), (pid, sid, uid, ssid))
    }

    shuffle(
      List.fill(matchIds.toInt) {
        semiRandMsg
      } :::
        List.fill(notMatchIds.toInt) {
          randMsg
        })
  }

}
