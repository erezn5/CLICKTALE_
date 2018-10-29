package com.clicktale.pipe.diff

import com.clicktale.pipe.diff.model.compare.CompareModel.Compare
import com.clicktale.pipe.diff.stream.AMQPInput
import com.clicktale.pipe.utils.LazyLogger
import io.circe

package object model extends LazyLogger {

  // model how we are going to keep track of received sessions
  type SessionInCage = Boolean
  type CacheEl = (SessionInCage, SessionInCage)
  type CacheUpdate = Option[Long]


  // TODO - consider a replacement for the 0/1 being the source A and B by for instance chars
  // tagged source of sessions
  case class SourcedEl(source: Int, el: AMQPInput)

  case class TaggedSessionKey(source: Int, sessionKey: SessionKey)

  //TODO - Question (Aryeh) parse the key VS parse JSON
  object TaggedSessionKey {
    def apply(source: Int, pid: Int, subid: Int, sessionId: Long): TaggedSessionKey =
      TaggedSessionKey(source, SessionKey(ProjectId(pid, subid), sessionId))

    def from(sourcedEl: SourcedEl): Either[circe.Error, TaggedSessionKey] = {
      val SourcedEl(tag, el) = sourcedEl
      import SessionKey._
      fromJsonAnalytics(new String(el.bytes.toArray)) match {
        case lsk@Left(ex) =>
          error("Could not parse the json analytics !", ex)
          Left(ex)
        case Right(sk) =>
          info(s"parsed correctly source from: [$tag], sid: [${sk.sessionId}]")
          Right(TaggedSessionKey(tag, sk))
      }
    }
  }

  case class ProjectId(pid: Int, subid: Int)

  case class SessionKey(pid: ProjectId, sessionId: Long)

  object SessionKey {

    def apply(projectId: Int, subscriberId: Int, sessionId: Long): SessionKey =
      SessionKey(ProjectId(projectId, subscriberId), sessionId)

    //    import cats.syntax.either._
    //    import io.circe._
    import io.circe.parser._

    def fromJsonAnalytics(jsonStr: String): Either[circe.Error, SessionKey] = {
      parse(jsonStr).flatMap { json =>
        val cursor = json.hcursor
        for {
          pid <- cursor.downField("PID").as[Int]
          subid <- cursor.downField("SubscriberId").as[Int]
          sid <- cursor.downField("SID").as[Long]
        } yield SessionKey(ProjectId(pid, subid), sid)
      }
    }


  }

  sealed trait CompareResult {
    def isSucceed: Boolean
  }

  case object CompareSucceed extends CompareResult {
    override def isSucceed: Boolean = true
  }

  case class ContentDiffers(fields: List[Compare]) extends CompareResult {
    override def isSucceed: Boolean = false
  }

  case class ContentMissing(failedSource: String) extends CompareResult {
    override def isSucceed: Boolean = false
  }

  case class FailedInDiffPhase(cause: String) extends CompareResult {
    override def isSucceed: Boolean = false
  }

}
