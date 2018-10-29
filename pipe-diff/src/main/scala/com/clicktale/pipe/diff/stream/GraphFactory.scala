package com.clicktale.pipe.diff.stream

import akka.actor.ActorSystem
import akka.kafka.scaladsl.Consumer
import akka.stream.scaladsl.{Flow, GraphDSL, Keep, RunnableGraph, Sink, Source}
import akka.stream._
import akka.{Done, NotUsed}
import com.clicktale.pipe.diff.conf.conf.{CageConf, FlowConf}
import com.clicktale.pipe.diff.model
import com.clicktale.pipe.diff.model._
import com.clicktale.pipe.diff.stream.Store.SinkElement
import com.clicktale.pipe.diff.stream.StreamFactory.{CageResult, SessionId}
import com.clicktale.pipe.utils.LazyLogger

import scala.concurrent.{ExecutionContextExecutor, Future}


object GraphFactory extends LazyLogger {

  def uniqueKafkaTopicConsumeDisplay(source: PDKafkaSource): RunnableGraph[(Consumer.Control, Future[PDKafkaRecord])] =
    source.via(
      Flow[PDKafkaRecord].map { rec =>
        println(rec.key())
        println(rec.value())
        rec
      }).toMat(Sink.last)(Keep.both)

  def uniqueAMQPConsumeDisplay(source: PDAMQPSource): RunnableGraph[(NotUsed, Future[AMQPInput])] =
    source.via(
      Flow[AMQPInput].map { rec =>
        println(new String(rec.bytes.toArray))
        rec
      }
    ).toMat(Sink.last)(Keep.both)


  def uniqueAMQPCountSessionKey(source: PDAMQPSource,
                                fc: FlowConf): RunnableGraph[(NotUsed, Future[(Int, SessionKey)])] = {
    val pid = ProjectId(fc.pid, fc.subid)
    val s: Source[(Int, SessionKey), NotUsed] = source.via(
      Flow[AMQPInput].map { rec =>
        import SessionKey._
        fromJsonAnalytics(new String(rec.bytes.toArray)) match {
          case lsk@Left(ex) =>
            error("Could not parse the json analytics !", ex)
            lsk
          case rsk@Right(sk) =>
            trace(s"parsed correctly ${sk.sessionId}")
            rsk
        }
      }).filter {
      case Right(SessionKey(`pid`, _)) =>
        trace(s"passing as correct project as $pid")
        true
      case _ => false
    }.collect {
      case Right(sk) =>
        trace(s"collecting $sk")
        (1, sk)
    }.reduce((a, b) => (a._1 + b._1, a._2))

    s.toMat(Sink.head)(Keep.both)
  }

  def amqp2SourceAlternateAndFilteredByProject(sourceA: PDAMQPSource,
                                               sourceB: PDAMQPSource,
                                               flowConf: FlowConf): RunnableGraph[(NotUsed, Future[Done])] = {
    val mixedSource: Graph[SourceShape[model.SourcedEl], NotUsed] =
      StreamFactory.createAmqpMergeSource(sourceA, sourceB, flowConf)

    val flowParseNFilter: Flow[model.SourcedEl, TaggedSessionKey, NotUsed] =
      StreamFactory.flowParseAndFilterAnalyticsByProject(flowConf)

    val resSink = Flow[TaggedSessionKey].map { tsk =>
      trace(s"Received TaggedSessionKey: $tsk")
      tsk
    }.toMat(Sink.ignore)(Keep.both)

    RunnableGraph.fromGraph(GraphDSL.create(resSink) { implicit b =>
      sink =>
        import GraphDSL.Implicits._
        val s = b.add(mixedSource)

        s.out ~> flowParseNFilter ~> sink

        ClosedShape
    })

  }

  def amqp2Cache(sourceA: PDAMQPSource,
                 sourceB: PDAMQPSource,
                 flowConf: FlowConf): RunnableGraph[(NotUsed, Future[Done])] = {
    val mixedSource: Graph[SourceShape[model.SourcedEl], NotUsed] =
      StreamFactory.createAmqpMergeSource(sourceA, sourceB, flowConf)

    val flowParseNFilter: Flow[model.SourcedEl, TaggedSessionKey, NotUsed] =
      StreamFactory.flowParseAndFilterAnalyticsByProject(flowConf)

    val flowCache: Flow[TaggedSessionKey, SessionKey, Map[SessionId, (SessionInCage, SessionInCage)]] =
      StreamFactory.flowSimpleCacheTaggedSessionKeys

    val resSink = Flow[SessionKey].map { sid =>
      trace(s"Received SessionId (for cage request): $sid")
      sid
    }.toMat(Sink.ignore)(Keep.both)

    RunnableGraph.fromGraph(GraphDSL.create(resSink) { implicit b =>
      sink =>
        import GraphDSL.Implicits._
        val s = b.add(mixedSource)

        s.out ~> flowParseNFilter ~> flowCache ~> sink

        ClosedShape
    })

  }


  def amqp2Cage(sourceA: PDAMQPSource,
                sourceB: PDAMQPSource,
                flowConf: FlowConf,
                cageConfA: CageConf,
                cageConfB: CageConf)(implicit actorSystem: ActorSystem,
                                     actorMaterializer: ActorMaterializer,
                                     ec: ExecutionContextExecutor): RunnableGraph[(NotUsed, Future[Done])] = {
    val mixedSource: Graph[SourceShape[model.SourcedEl], NotUsed] =
      StreamFactory.createAmqpMergeSource(sourceA, sourceB, flowConf)

    val flowParseNFilter: Flow[model.SourcedEl, TaggedSessionKey, NotUsed] =
      StreamFactory.flowParseAndFilterAnalyticsByProject(flowConf)

    val flowCache: Flow[TaggedSessionKey, SessionKey, Map[SessionId, (SessionInCage, SessionInCage)]] =
      StreamFactory.flowSimpleCacheTaggedSessionKeys

    val flowCage: Flow[SessionKey, CageResult, NotUsed] =
      StreamFactory.flowRequestCage(cageConfA, cageConfB, flowConf)

    val resSink = Flow[CageResult].map {
      case (sk, Left(err)) => debug(s"Received error instead of Jsons.")
      case (sk, _) => debug(s"Received two JSON to compare: ${sk.sessionId}")
    }.toMat(Sink.ignore)(Keep.both)

    RunnableGraph.fromGraph(GraphDSL.create(resSink) { implicit b =>
      sink =>
        import GraphDSL.Implicits._
        val s = b.add(mixedSource)

        s.out ~> flowParseNFilter ~> flowCache ~> flowCage ~> sink

        ClosedShape
    })

  }

  def fullPipeDiff(sourceA: PDAMQPSource,
                   sourceB: PDAMQPSource,
                   flowConf: FlowConf,
                   cageConfA: CageConf,
                   cageConfB: CageConf,
                   store: Store)(implicit actorSystem: ActorSystem,
                                 actorMaterializer: ActorMaterializer,
                                 ec: ExecutionContextExecutor):
  RunnableGraph[((UniqueKillSwitch, Map[SessionId, (SessionInCage, SessionInCage)]), Future[Done])] = {
    info(s"building graph: FullPipeDiff")

    val mixedSource: Graph[SourceShape[model.SourcedEl], NotUsed] =
      StreamFactory.createAmqpMergeSource(sourceA, sourceB, flowConf)
    info(s"building graph: FullPipeDiff (Source)")

    val flowParseNFilter: Flow[model.SourcedEl, TaggedSessionKey, NotUsed] =
      StreamFactory.flowParseAndFilterAnalyticsByProject(flowConf)

    val flowCache: Flow[TaggedSessionKey, SessionKey, Map[SessionId, (SessionInCage, SessionInCage)]] =
      StreamFactory.flowSimpleCacheTaggedSessionKeys

    val flowCage: Flow[SessionKey, CageResult, NotUsed] =
      StreamFactory.flowRequestCage(cageConfA, cageConfB, flowConf)

    val flowCompare = StreamFactory.flowCompare()

    info(s"building graph: FullPipeDiff (Flows)")

    val resSink = Flow[SinkElement].viaMat(KillSwitches.single)(Keep.right).
      toMat(store.sink(flowConf))(Keep.both)

    info(s"building graph: FullPipeDiff (Sink)")

    RunnableGraph.fromGraph(
      GraphDSL.create(resSink, flowCache)
      ((m1, m2) => ((m1._1, m2), m1._2)) { implicit b =>
        (sink, fcache) =>
          import GraphDSL.Implicits._
          val s = b.add(mixedSource)

          s.out ~> flowParseNFilter ~> fcache ~> flowCage ~> flowCompare ~> sink

          ClosedShape
      })
  }
}
