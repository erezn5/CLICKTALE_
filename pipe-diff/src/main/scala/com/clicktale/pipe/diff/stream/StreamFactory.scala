package com.clicktale.pipe.diff.stream

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.{Http, HttpExt}
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.stream.alpakka.amqp.scaladsl.AmqpSource
import akka.stream.alpakka.amqp.{AmqpUriConnectionProvider, NamedQueueSourceSettings}
import akka.stream.scaladsl.{Flow, GraphDSL, Keep, Merge, Source}
import akka.stream.{ActorMaterializer, Graph, SourceShape}
import com.clicktale.pipe.diff.conf.PipeDiffConfModel.{AMQPSourceSetting, PipeDiffAMQPCageConf, PipeDiffConfKafka}
import com.clicktale.pipe.diff.conf.conf._
import com.clicktale.pipe.diff.model._
import com.clicktale.pipe.diff.model.compare.CompareModel
import com.clicktale.pipe.diff.util.QueryHelper
import com.clicktale.pipe.utils.LazyLogger
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.{Deserializer, StringDeserializer}

import scala.concurrent.{ExecutionContextExecutor, Future}


object StreamFactory extends LazyLogger {

  def kafkaSourceAAutoAssigned(pipeDiffConf: PipeDiffConfKafka): PDKafkaSource = autoAssignedKafkaSource(
    consumerSettings(pipeDiffConf.kafkaSettingA,
      new StringDeserializer(),
      new StringDeserializer),
    pipeDiffConf.topicA
  )

  def kafkaSourceBAutoAssigned(pipeDiffConf: PipeDiffConfKafka): PDKafkaSource = autoAssignedKafkaSource(
    consumerSettings(pipeDiffConf.kafkaSettingB,
      new StringDeserializer(),
      new StringDeserializer),
    pipeDiffConf.topicB
  )

  def amqpSourceA(pipeDiffAMQPCageConf: PipeDiffAMQPCageConf): PDAMQPSource =
    createAmqpSource(pipeDiffAMQPCageConf.amqpA)

  def amqpSourceB(pipeDiffAMQPCageConf: PipeDiffAMQPCageConf): PDAMQPSource =
    createAmqpSource(pipeDiffAMQPCageConf.amqpB)


  def autoAssignedKafkaSource[K, V](consumerSettings: ConsumerSettings[K, V],
                                    topic: Topic): Source[ConsumerRecord[K, V], Consumer.Control] =
    Consumer.plainSource(consumerSettings, Subscriptions.topics(Set(topic.topic)))


  def consumerSettings[K, V](kafkaConsumerSetting: KafkaConsumerSetting,
                             keyDeserializer: Deserializer[K],
                             valDeserializer: Deserializer[V]): ConsumerSettings[K, V] =
    ConsumerSettings(kafkaConsumerSetting.tuning, keyDeserializer, valDeserializer).
      withBootstrapServers(kafkaConsumerSetting.boostrapServer).
      withGroupId(kafkaConsumerSetting.groupId)

  def alternateKafkaSource[K, V](ks1: KafkaSource[K, V], ks2: KafkaSource[K, V]): Nothing = ???

  def buildUri(typeQueue: String,
               host: String,
               port: Int,
               username: String,
               password: String): String = {
    val uri = s"amqp://$username:$password@$host:$port"
    info(s"AMQP connection for the $typeQueue is set with: $uri")
    uri
  }

  def createAmqpSource(conf: AMQPSourceConf): Source[AMQPInput, NotUsed] = {
    val connectionProvider = AmqpUriConnectionProvider(
      buildUri("Source", conf.host, conf.port, conf.username, conf.password))

    val settings = AMQPSourceSetting(
      NamedQueueSourceSettings(connectionProvider, conf.queueName),
      conf.prefetchCount)

    info(s"Create Source with settings: [$settings]")

    AmqpSource.atMostOnceSource(
      NamedQueueSourceSettings(connectionProvider, conf.queueName),
      conf.prefetchCount)
  }


  //TODO - Use simple merge operator (because it prevent from not processing the missing msg from rabbit from one of the processor)

  def createAmqpMergeSource(sourceA: PDAMQPSource,
                            sourceB: PDAMQPSource,
                            flowConf: FlowConf): Graph[SourceShape[SourcedEl], NotUsed] = {
    GraphDSL.create() { implicit b =>
      import GraphDSL.Implicits._

      // the zip solution seems to have a defeft in case of missing message from AMQP so not in use until better comprehension
      //        val flatten = Flow[List[OUT]].mapConcat(identity)
      //        val zipSource = b.add(ZipWith[IN, IN, List[OUT]](
      //          (a, b) => List(SourcedEl(0, a), SourcedEl(1, b))))
      //        val flattBuilt = b.add(flatten)
      //        zipSource.out ~> flattBuilt
      val sa = sourceA.map(SourcedEl(0, _))
      val sb = sourceB.map(SourcedEl(1, _))
      val mergeSource = b.add(Merge[SourcedEl](2))

      sa ~> mergeSource
      sb ~> mergeSource

      SourceShape(mergeSource.out)
    }
  }

  def flowParseAndFilterAnalyticsByProject(fc: FlowConf): Flow[SourcedEl, TaggedSessionKey, NotUsed] = {

    val pid = ProjectId(fc.pid, fc.subid)

    Flow[SourcedEl].map { el =>
      val res = TaggedSessionKey.from(el)
      res match {
        case l@Left(err) => error(s"Error when parsing message from AMQP", err)
          l
        case r@Right(_) => r
      }
    }.filter {
      case Right(TaggedSessionKey(source, SessionKey(`pid`, _))) =>
        info(s"passing source [$source], project [$pid]")
        true
      case other =>
        trace(s"Error receive [$other]")
        false
    }.collect {
      case Right(tsk) =>
        trace(s"collecting $tsk")
        tsk
    }
  }

  type SessionId = Long

  def flowSimpleCacheTaggedSessionKeys: Flow[TaggedSessionKey, SessionKey, Map[SessionId, CacheEl]] = {
    var cache = Map.empty[SessionId, CacheEl]

    def cacheEl(tsk: TaggedSessionKey) = if (tsk.source == 0) (true, false) else (false, true)

    def tupleOr(t1: CacheEl, t2: CacheEl): CacheEl = (t1._1 || t2._1, t1._2 || t2._2)

    def tupleAnd(t: CacheEl): SessionInCage = t._1 && t._2

    Flow[TaggedSessionKey].map { tsk =>
      val cel = cacheEl(tsk)
      val doOnCache = if (tupleAnd(tupleOr(cel, cache.getOrElse(tsk.sessionKey.sessionId, (false, false))))) {
        cache -= tsk.sessionKey.sessionId
        info(s"remove key ${tsk.sessionKey.sessionId} of cache")
        Some(tsk.sessionKey)
      } else {
        cache += (tsk.sessionKey.sessionId -> cel)
        info(s"cache: add source [${tsk.source}.], key [${tsk.sessionKey.sessionId}]")
        None
      }
      trace(s"""State of cache (size ${cache.size}) key: [${cache.keys.mkString(",")}]""")
      info(s"""State of cache (size ${cache.size})""")
      doOnCache
    }.filter {
      case Some(_) => true
      case None => false
    }.collect { case Some(sk) => sk }.
      mapMaterializedValue(_ => cache)
  }

  // Always give the session key (for file name) then either a cause of failure either both the JSON
  type CageResult = (SessionKey, Either[String, (String, String)])

  // TODO - parallelism in configuration
  def flowRequestCage(cageConfA: CageConf,
                      cageConfB: CageConf,
                      flowConf: FlowConf)(
                       implicit actorSystem: ActorSystem,
                       actorMaterializer: ActorMaterializer,
                       ec: ExecutionContextExecutor): Flow[SessionKey, CageResult, NotUsed] = {
    def urlJson(subscriberId: Int,
                projectId: Int,
                sessionId: Long) = s"/recording/analytics/v2/$subscriberId/$projectId/$sessionId"

    implicit val http: HttpExt = Http()

    val queryA = (sid: Long) => QueryHelper.query(
      Uri.from(scheme = "http", host = cageConfA.host, port = cageConfA.port,
        path = urlJson(flowConf.subid, flowConf.pid, sid)),
      List(RawHeader(cageConfA.apikey._1, cageConfA.apikey._2)),
      QueryHelper.queryContentAsText)

    val queryB = (sid: Long) => QueryHelper.query(
      Uri.from(scheme = "http", host = cageConfB.host, port = cageConfB.port,
        path = urlJson(flowConf.subid, flowConf.pid, sid)),
      List(RawHeader(cageConfB.apikey._1, cageConfB.apikey._2)),
      QueryHelper.queryContentAsText)

    Flow[SessionKey].
      mapAsyncUnordered(1) { sk =>
        queryA(sk.sessionId).map(_.decodeString("utf-8")).
          zip(queryB(sk.sessionId).map(_.decodeString("utf-8"))).
          map(Some(_)).
          map { case Some((a, b)) => (sk, Right(a, b)) }.
          recoverWith { case th =>
            error(s"One of the request to Cages A/B (json) failed for [${sk.sessionId}]", th)
            Future.successful((sk, Left("Fail requesting JSON")))
          }
      }
  }


  def flowCompare(): Flow[CageResult, (SessionKey, CompareResult), NotUsed] = {

    val compareJson = CompareModel.CompareJson.compare(_: String, _: String)
    Flow[CageResult].map { case (sk, cr) =>
      cr.fold(cause => (sk, FailedInDiffPhase(cause)),
        t => {
          val (json1, json2) = t
          compareJson(json1, json2).fold(th => {
            error("Failed comparing json", th)
            (sk, FailedInDiffPhase("Failed in the json comparison"))
          },
            lc => (sk, if (lc.isEmpty) {
              trace(s"Compare yield identical for [${sk.sessionId}]")
              CompareSucceed
            } else {
              trace(s"Compare yield differences for [${sk.sessionId}]")
              ContentDiffers(lc)
            }))
        })
    }
  }
}
