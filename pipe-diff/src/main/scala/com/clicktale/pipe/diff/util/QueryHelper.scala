package com.clicktale.pipe.diff.util

import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import akka.util.ByteString
import com.clicktale.pipe.utils.LazyLogger

import scala.concurrent.{ExecutionContextExecutor, Future}

object QueryHelper extends LazyLogger {

  def query[R](uri: Uri, headers: List[HttpHeader], mapTo: HttpResponse => Future[R])
              (implicit ec: ExecutionContextExecutor, http: HttpExt): Future[R] =
    http.singleRequest(HttpRequest(uri = uri, headers = headers)).flatMap(mapTo)

  def queryContentAsText(resp: HttpResponse)
                        (implicit materializer: ActorMaterializer): Future[ByteString] = {

    resp match {
      case HttpResponse(StatusCodes.OK, _, entity, _) =>
        entity.dataBytes.runFold(ByteString(""))(_ ++ _)
      case resp@HttpResponse(code, _, _, _) =>
        error("Request failed, response code: " + code)
        resp.discardEntityBytes()
        throw new Exception("Request failed, response code: " + code)
    }

  }
}
