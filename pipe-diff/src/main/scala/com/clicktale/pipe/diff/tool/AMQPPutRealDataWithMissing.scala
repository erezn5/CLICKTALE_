package com.clicktale.pipe.diff.tool

import java.net.URI

import akka.Done
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.RawHeader
import akka.stream.alpakka.amqp.scaladsl.AmqpSink
import akka.stream.alpakka.amqp.{AmqpCachedConnectionProvider, AmqpSinkSettings, AmqpUriConnectionProvider, OutgoingMessage}
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, Keep, Sink, Source}
import akka.stream.{ActorMaterializer, SinkShape}
import akka.util.ByteString
import com.clicktale.pipe.diff.conf.PipeDiffConfModel.BasicCageConf
import com.clicktale.pipe.diff.model.SessionKey
import com.clicktale.pipe.utils.LazyLogger

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.matching.UnanchoredRegex
import scala.util.{Failure, Random, Success, Try}

object AMQPPutRealDataWithMissing extends LazyLogger {

  private val cageConf = BasicCageConf(
    "datareader.clicktale.net",
    8080,
    ("ct-api-key", "TheUnforgiven"))

  private def urlLog(lines: Int) = s"/log/$lines"

  private def urlJson(subscriberId: Int,
                      projectId: Int,
                      sessionId: Long) = s"/$subscriberId/$projectId/$sessionId"

  def main(args: Array[String]): Unit = {
    val idSinkMissing = 0
    val numMissing = 1
    val projectId = 74
    val subscriberId = 233542
    buildMsgFromRespAndSendAmqp(subscriberId, projectId, idSinkMissing, numMissing)
  }

  def buildMsgFromRespAndSendAmqp(subscriberId: Int,
                                  projectId: Int,
                                  idSinkMissing: Int, // maybe 0/1
                                  numMissing: Int): Unit = {

    val uri = Uri.from(scheme = "http", host = cageConf.host, path = urlLog(2000))
    trace(s"the requested uri is [$uri]")
    val headers = List(RawHeader(cageConf.apikey._1, cageConf.apikey._2))
    trace(s"put headers [$headers]")

    implicit val system: ActorSystem = ActorSystem()
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher


    val extractSession = queryContentAsText(_: HttpResponse).
      map(extractStructuredSessions).
      map(logMapSession).
      map(buildJsonMessageFromMap).
      map(
        Random.shuffle(_).map { case ((subid, pid, sid), msg) =>
          outMsg(msg, subid.toInt, pid.toInt, Random.nextInt(), sid.toLong)
        })

    val insertAmqp = Source.
      fromFuture(query(uri, headers, extractSession)).
      mapConcat(it => identity(it.toList)).
      toMat(
        amqpSinkWithMissing(subscriberId, projectId, idSinkMissing, numMissing)(amqp0, amqp1)
      )(Keep.both)

    insertAmqp.run()._2.onComplete {
      case Success(_) => system.terminate()
      case Failure(ex) => error("Error occurs", ex)
        system.terminate()
    }
  }

  def justLogResponse(): Unit = {

    val uri = Uri.from(scheme = "http", host = cageConf.host, path = urlLog(2000))
    trace(s"the requested uri is [$uri]")
    val headers = List(RawHeader(cageConf.apikey._1, cageConf.apikey._2))
    trace(s"put headers [$headers]")

    implicit val system: ActorSystem = ActorSystem()
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher

    val extractSession = queryContentAsText(_: HttpResponse).
      map(extractStructuredSessions).
      map(logMapSession).
      map(buildJsonMessageFromMap).
      map(
        Random.shuffle(_).map { case ((subid, pid, sid), msg) =>
          outMsg(msg, subid.toInt, pid.toInt, Random.nextInt(), sid.toLong)
        })

    val ft = query(uri, headers, extractSession)
    trace("created the future for query ...")

    val insertAmqp = Source.fromFuture(ft).collect { case it =>
      debug(s"Size of messages list: [${it.size}]")
      it
    }.toMat(Sink.ignore)(Keep.both)

    insertAmqp.run()._2 onComplete {
      case Success(_) => system.terminate()
      case Failure(ex) =>
        error("Error occurs", ex)
        system.terminate()
    }
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  /// Building/Send message part
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  type SubidPidSid = (Int, Int, Long)

  def buildJsonMessageFromMap(m: Map[String, Traversable[SubidPidSid]]): Iterable[(SubidPidSid, String)] = {

    def toJson(t: SubidPidSid) =
      s"""{"SubscriberId" :${t._1},  "PID" : ${t._2}, "SID" : ${t._3} }"""

    val flatVal = m.values.flatten
    flatVal.zip(flatVal).map { case (t1, t2) => (t1, toJson(t2)) }
  }


  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  /// Query part
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  def query[R](uri: Uri,
               headers: List[HttpHeader],
               mapTo: HttpResponse => Future[R])(implicit actorSystem: ActorSystem,
                                                 ec: ExecutionContextExecutor): Future[R] =
    Http(actorSystem).singleRequest(
      HttpRequest(uri = uri, headers = headers)).flatMap(mapTo)

  def queryContentAsText(resp: HttpResponse)(
    implicit materializer: ActorMaterializer,
    ec: ExecutionContextExecutor): Future[ByteString] = {

    resp match {
      case HttpResponse(StatusCodes.OK, headers, entity, _) =>
        entity.dataBytes.runFold(ByteString(""))(_ ++ _)
      case resp@HttpResponse(code, _, _, _) =>
        error("Request failed, response code: " + code)
        resp.discardEntityBytes()
        throw new Exception("Request failed, response code: " + code)
    }

  }

  def extractStructuredSessions(bs: ByteString): Map[String, Traversable[SubidPidSid]] = {

    trace(s"size of the receive bs [${bs.size}] bs is : [$bs]")
    val parser: String => Option[(Int, Int, Long)] = parseUrlFromLog(_: String, """\[(http.*?)\]""".r.unanchored)

    bs.decodeString("utf-8").split(System.lineSeparator()).
      filter(_.contains("http")).
      map(parser).
      flatten.
      groupBy { case (subid, pid, _) => s"$subid.$pid" }
      .map { case (k, mb) => k -> mb.toSet }
  }

  def parseUrlFromLog(l: String, extractRegEx: UnanchoredRegex): Option[SubidPidSid] = {
    trace(s"New line [$l]")
    // [^\]]
    //    val sqBrackReg: UnanchoredRegex =       """\[(http.*?)\]""".r.unanchored
    //    val sqBrackReg = """\[(http.*?)\]""".r.unanchored

    l match {
      case extractRegEx(ct) =>
        trace(s"found the pattern of url in the line [$ct]")

        new URI(ct).getPath.split("/").reverse.take(3).toList match {
          case sid :: pid :: subid :: Nil =>
            Try(Some((subid.toInt, pid.toInt, sid.toLong))).fold(
              _ => Option.empty[SubidPidSid],
              t => {
                debug(s"found ids [$t]")
                t
              })
          case _ => Option.empty[SubidPidSid]
        }
      case _ =>
        trace(s"Not found pattern of url")
        Option.empty[SubidPidSid]
    }
  }

  def logMapSession(m: Map[String, Traversable[SubidPidSid]]): Map[String, Traversable[SubidPidSid]] = {
    m.foreach { case (k, it) =>
      debug(s"""Key [$k] => [${it.mkString(" | ")}]""")
    }
    m
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  /// Flow part
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  val amqp0: AMQPTargetConf = SimpleAMQPTargetConf(AMQPConnParam("172.22.7.11",
    5672,
    "pipeline",
    "eb72BrfGU6JA6Ne23UIg"),
    "#",
    "pdt0.2.pipe.diff.test.ex1")

  val amqp1: AMQPTargetConf = SimpleAMQPTargetConf(AMQPConnParam("172.22.7.11",
    5672,
    "pipeline",
    "eb72BrfGU6JA6Ne23UIg"),
    "#",
    "pdt0.2.pipe.diff.test.ex2")

  def amqpSinkWithMissing(subscriberId: Int,
                          projectId: Int,
                          idSinkMissing: Int, // maybe 0/1
                          numMissing: Int)
                         (s0: AMQPTargetConf, s1: AMQPTargetConf): Sink[OutgoingMessage, Future[(Done, Done)]] = {
    var countMissing = 0

    val induceMissing = Flow[OutgoingMessage].filter { om =>
      val sk = om.routingKey.fold(throw new Exception(s"Should provide routing key and find None"))(revRoutingKey)
      if (sk.pid.pid == projectId && sk.pid.subid == subscriberId && countMissing < numMissing) {
        countMissing += 1
        false
      } else true
    }

    val (sinkMissing, sinkNoMissing) = idSinkMissing match {
      case 0 => (createAmqpSink(s0), createAmqpSink(s1))
      case 1 => (createAmqpSink(s1), createAmqpSink(s0))
      case _ => throw new Exception(s"Unkown source [$idSinkMissing] to create sink (possible: 0/1)")
    }

    Sink.fromGraph(GraphDSL.create(sinkMissing, sinkNoMissing)
    ((fst, snd) => fst.zip(snd)) {
      implicit builder =>
        (sinkMissingMat, sinkNoMissingMat) =>
          import GraphDSL.Implicits._
          val broadcast = builder.add(Broadcast[OutgoingMessage](2))

          broadcast ~> sinkNoMissingMat
          broadcast ~> induceMissing ~> sinkMissingMat

          SinkShape(broadcast.in)
    })
  }

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

  def revRoutingKey(rk: String): SessionKey = rk.split(".") match {
    case Array(sid, pid, uid, ssid, _) =>
      SessionKey(pid.toInt, sid.toInt, ssid.toLong)
    case _ => throw new Exception("Routing key badly formatted !!!")
  }

  def outMsg(msg: String, subid: Int, pid: Int, uid: Int, ssid: Long): OutgoingMessage =
    OutgoingMessage(ByteString(msg.getBytes),
      immediate = false,
      mandatory = false,
      routingKey = Some(routingKey(subid, pid, uid, ssid)))

}
