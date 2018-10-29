package com.clicktale.pipe.diff.tool

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Keep, Sink, Source}

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}

object FailingFutureSource {

  def main(args: Array[String]): Unit = {

    implicit val system: ActorSystem = ActorSystem()
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher

    // create a failing future source
    val g = Source.fromFuture(Future {
      1 / 0
    }).map(_.toString).toMat(Sink.ignore)(Keep.both)

    g.run()._2 onComplete {
      case Success(value) => println(value)
        system.terminate()
      case Failure(ex) =>
        println(ex)
        system.terminate()
    }
  }

}
