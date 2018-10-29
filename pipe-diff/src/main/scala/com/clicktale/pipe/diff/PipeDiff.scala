package com.clicktale.pipe.diff

import akka.Done
import akka.actor.CoordinatedShutdown._
import akka.actor.{ActorSystem, CoordinatedShutdown}
import akka.stream.scaladsl.RunnableGraph
import akka.stream.{ActorMaterializer, KillSwitch, UniqueKillSwitch}
import com.clicktale.pipe.diff.conf.PipeDiffConfModel.{PipeDiffAMQPCageConf, PipeDiffConfKafka}
import com.clicktale.pipe.diff.conf.conf.FlowConf
import com.clicktale.pipe.diff.model.SessionInCage
import com.clicktale.pipe.diff.stream.StreamFactory.SessionId
import com.clicktale.pipe.diff.stream.{GraphFactory, Store, StreamFactory}
import com.clicktale.pipe.utils.LazyLogger
import com.typesafe.config.ConfigFactory

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success, Try}

object PipeDiff extends LazyLogger {


  def main(args: Array[String]): Unit = {
    import com.clicktale.pipe.diff.conf.PipeDiffConfModel._

    implicit val system: ActorSystem = ActorSystem("PipeDiff")
    val cs: CoordinatedShutdown = CoordinatedShutdown(system)

    // TODO - add try on the config load
    val conf = ConfigFactory.load()
    val flowc: Try[FlowConf] = logConf(loadFlowConf(conf.getConfig(s"$confPrefix.flow")))
    val pdck: Try[PipeDiffConfKafka] = loadPipeDiffConfKafka(confPrefix, conf)
    val pdcc: Try[PipeDiffAMQPCageConf] = loadPipeDiffAMQPCageConf(conf.getConfig(s"$confPrefix.amqp-cage"))

    flowc.fold(errFlowConf,
      { fc =>
        implicit val materializer: ActorMaterializer = ActorMaterializer()
        implicit val ec: ExecutionContextExecutor = system.dispatcher
        val store = new Store()
        debug(s"creating graph...")

        // !!!! for debug purpose... !!!!
        //        Try(createGraph(fc, pdck, pdcc, store).run()) match {
        //          case Failure(ex) => error(s"Exception occurs while running pipe-diff", ex)
        //          case Success((_ , _)) => ()
        //        }

        //        // working code for check missing befor killing stream
        //        Try(runPipeDiff(fc, pdcc, store).run()) match {
        //          case Failure(ex) => error(s"Exception occurs while running pipe-diff", ex)
        //          case Success(((killswith, cache), _)) => Runtime.getRuntime.addShutdownHook(
        //            new Thread(() => closeResource(killswith, cache, store, fc)))
        //          case Success(_) => throw new Exception("Should receive ((killswitch, cache), f: Future[Done])")
        //        }

        // also working maybe cleaner (see code review with Roman/Aryeh/Elie
        Try(runPipeDiff(fc, pdcc, store).run()) match {
          case Success(((killSwitch, cache), doneFuture)) =>
            cs.addTask(PhaseServiceUnbind, "ks-shutdown")(
              () => Future(closeResource(killSwitch, cache, store, fc)).map(_ => Done))
            doneFuture.onComplete { _ =>
              warn(s"stream futures completed, shutting down actor system")
              cs.run(unknownReason)
            }
          case Failure(ex) =>
            error("Exception occurred", ex)
            cs.run(unknownReason)
        }
      })
  }


  def runPipeDiff(fc: FlowConf,
                  pdcc: Try[PipeDiffAMQPCageConf],
                  store: Store)
                 (implicit actorSystem: ActorSystem,
                  actorMaterializer: ActorMaterializer,
                  ec: ExecutionContextExecutor):
  RunnableGraph[((UniqueKillSwitch, Map[SessionId, (SessionInCage, SessionInCage)]), Future[Done])] =
    pdcc.fold(errAmqpCage, c => {
      logConf(c)
      GraphFactory.fullPipeDiff(
        StreamFactory.amqpSourceA(c), StreamFactory.amqpSourceB(c),
        fc,
        c.cageA, c.cageB,
        store)
    })

  def closeResource(killSwitch: KillSwitch,
                    cache: Map[SessionId, (SessionInCage, SessionInCage)],
                    store: Store,
                    fc: FlowConf): Unit = {
    Try(store.printMissing(cache.toIterable, fc)).fold(
      th => error(s"Error occurs when emptying cache of missing.", th), _ => ())
    Try(killSwitch.shutdown()).fold(
      th => error(s"Error occurs when killing the stream", th), _ => ())
  }

  def errConf(ex: Throwable, msg: String): Nothing = {
    error(msg, ex)
    throw ex
  }

  val errFlowConf: Throwable => Nothing = errConf(_: Throwable, "Could not load/find flow conf")
  val errKafka: Throwable => Nothing = errConf(_: Throwable, "Could not load/find Kafka configuration")
  val errAmqpCage: Throwable => Nothing = errConf(_: Throwable, "Could not load/find AMQP-Cage configuration")

  def logConf[C](cf: C): C = {
    info(
      s"""
         |=============================================================================================================
         |$cf
         |=============================================================================================================
       """.stripMargin)
    cf
  }

  // !!! - this method is for debugging only (allows to call part of the pipeline in order to test them) --- !!!
  def createGraph(flowc: FlowConf,
                  pdck: Try[PipeDiffConfKafka],
                  pdcc: Try[PipeDiffAMQPCageConf],
                  store: Store)(
                   implicit actorSystem: ActorSystem,
                   actorMaterializer: ActorMaterializer,
                   ec: ExecutionContextExecutor): RunnableGraph[(_, Future[_])] = {

    import GraphFactory._
    import StreamFactory._

    val g: RunnableGraph[(_, Future[_])] = flowc.mode match {
      case "ConnectivityKafkaA" => pdck.fold(errKafka, c => {
        logConf(c)
        uniqueKafkaTopicConsumeDisplay(kafkaSourceAAutoAssigned(c))
      })

      case "ConnectivityKafkaB" => pdck.fold(errKafka, c => {
        logConf(pdck)
        uniqueKafkaTopicConsumeDisplay(kafkaSourceBAutoAssigned(c))
      })

      case "ConnectivityAmqpA" => pdcc.fold(errAmqpCage, c => {
        logConf(c)
        uniqueAMQPConsumeDisplay(amqpSourceA(c))
      })

      case "ConnectivityAmqpB" => pdcc.fold(errAmqpCage, c => {
        logConf(c)
        uniqueAMQPConsumeDisplay(amqpSourceB(c))
      })

      case "CountSessionKeyOnA" => pdcc.fold(errAmqpCage, c => {
        logConf(c)
        uniqueAMQPCountSessionKey(amqpSourceA(c), flowc)
      })

      case "2AMQPSFiltered" => pdcc.fold(errAmqpCage, c => {
        logConf(c)
        amqp2SourceAlternateAndFilteredByProject(amqpSourceA(c), amqpSourceB(c), flowc)
      })

      case "AMQP2Cache" => pdcc.fold(errAmqpCage, c => {
        logConf(c)
        amqp2Cache(amqpSourceA(c), amqpSourceB(c), flowc)
      })

      case "AMQP2Cage" => pdcc.fold(errAmqpCage, c => {
        logConf(c)
        amqp2Cage(amqpSourceA(c), amqpSourceB(c), flowc, c.cageA, c.cageB)
      })

      case "FullPipeDiff" => runPipeDiff(flowc, pdcc, store)

      case _ => throw new Exception("Could not identified flow !")
    }

    g
  }

}
