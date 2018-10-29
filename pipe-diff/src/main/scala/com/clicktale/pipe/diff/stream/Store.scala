package com.clicktale.pipe.diff.stream

import java.nio.file.{Files, Paths}

import akka.Done
import akka.stream.scaladsl.Sink
import com.clicktale.pipe.diff.conf.conf.FlowConf
import com.clicktale.pipe.diff.model._
import com.clicktale.pipe.diff.model.compare.CompareModel.Compare
import com.clicktale.pipe.diff.stream.Store.SinkElement
import com.clicktale.pipe.diff.stream.StreamFactory.SessionId
import com.clicktale.pipe.utils.LazyLogger

import scala.concurrent.Future
import scala.util.Try

object Store {
  type SinkElement = (SessionKey, CompareResult)
}

class Store extends LazyLogger {

  def sink(fc: FlowConf): Sink[SinkElement, Future[Done]] =
    Sink.foreach[SinkElement] { case (sk, cr) =>
      Try(Files.write(
        Paths.get(fc.resultDirectory, fmt(sk)),
        fmtResult(cr).getBytes)).fold(
        th => error("Could not writ file", th),
        p => info(s"wrote file to [$p]")
      )
    }

  def printMissing(cacheElmts: Iterable[(SessionId, CacheEl)],
                   fc: FlowConf): Unit = {
    info(s"Check cache for missings...")
    type TSK = TaggedSessionKey
    val subscriberId = fc.subid
    val projectId = fc.pid

    val streamOnEl: Stream[TSK] = {

      def source(cacheEl: CacheEl) = cacheEl match {
        case (true, false) => 0
        case (false, true) => 1
        case _ => throw new UnsupportedOperationException(s"Impossible case !!! Check the cache implementation.")
      }

      def soe(tsk: TSK, it: Iterable[(SessionId, CacheEl)]): Stream[TSK] =
        if (it.isEmpty) tsk #:: Stream.empty[TSK]
        else tsk #:: soe(TaggedSessionKey(source(it.head._2), projectId, subscriberId, it.head._1), it.tail)

      if (cacheElmts.isEmpty) Stream.empty[TSK]
      else soe(TaggedSessionKey(source(cacheElmts.head._2), projectId, subscriberId, cacheElmts.head._1),
        cacheElmts.tail)
    }

    streamOnEl.foreach { tsk =>
      Try(Files.write(
        Paths.get(fc.resultDirectory, fmt(tsk.sessionKey)),
        fmtResult(ContentMissing(tsk.source.toString)).getBytes
      )).fold(
        th => error("Could not writ file", th),
        p => info(s"wrote file to [$p]")
      )
    }
  }

  private def fmt(sessionKey: SessionKey) =
    sessionKey match {
      case SessionKey(ProjectId(pid, subId), sessionId) =>
        s"$subId-$pid-$sessionId"
    }

  private def fmtResult(compareResult: CompareResult): String = compareResult match {
    case CompareSucceed =>
      """ |status: identical
      """.stripMargin
    case ContentMissing(failedSource) =>
      s""" |status: missing
         |source: missing (in $failedSource)
        """.stripMargin
    case FailedInDiffPhase(cause) =>
      s""" |status: diff failed
         |cause: $cause
       """.stripMargin
    case ContentDiffers(fields) =>
      s""" |status: differ
         |diff by json patch (operation, path, value, msg)
       """.stripMargin +
        fields.map { case Compare(op, path, value, msg) => s"$op\t\t$path\t\t$value\t\t$msg" }
          .mkString(System.lineSeparator())

  }
}
