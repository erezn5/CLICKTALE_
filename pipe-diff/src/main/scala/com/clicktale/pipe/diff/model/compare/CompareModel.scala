package com.clicktale.pipe.diff.model.compare

import java.io.File

import com.clicktale.pipe.diff.model.compare.CompareModel.Compare.Operation
import com.clicktale.pipe.utils.LazyLogger
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import com.flipkart.zjsonpatch.{DiffFlags, JsonDiff}
import io.circe._
import io.circe.parser._

import scala.io.Source
import scala.collection.mutable.ListBuffer
import scala.util.matching.Regex
import scala.util.{Failure, Success, Try}

object CompareModel extends LazyLogger {

  case class Compare(op: String, path: String, value: Option[Json], msg: String = "")

  object Compare {

    object Operation extends Enumeration {
      val add, remove, replace, copy, move, test = Value
    }

    implicit val decodeFoo: Decoder[Compare] =
      (c: HCursor) => for {
        op <- c.downField("op").as[String]
        path <- c.downField("path").as[String]
        value <- c.downField("value").as[Option[Json]]
      } yield {
        new Compare(op, path, value)
      }
  }

  object CompareJson {
    // compile here in order not to recompute (for all json comparison)
    val arrIdx: Regex =
      """/\d+/|/\d+$|/-/|/-$""".r

    val integer: Regex = """^\d+$""".r
    val excludeCSVPath = new File(getClass.getClassLoader.getResource("excludeJsonFields.csv").getPath)
    // TODO - pass the name of the csv file or the configuration as a parameter of the function
    def readBlackListFile(): Iterator[String] =
      Source.
        fromFile(excludeCSVPath).
        getLines()


    def compare(jsonStr1: String, jsonStr2: String): Either[Exception, List[Compare]] = {
      import Compare._
      val flags = DiffFlags.dontNormalizeOpIntoMoveAndCopy().clone()
      for {
        json1 <- parseJson(jsonStr1)
        //        _ <- Right(println(json1))
        json2 <- parseJson(jsonStr2)
        //        _ <- Right(println(json2))
        patch1 <- parse(JsonDiff.asJson(json1, json2, flags).toString)
        //        _ <- Right(println(patch1))
        // TODO - this is necessary only if the array field contain replace operation (optimization!!! ROMANNNNNN)
        patch2 <- parse(JsonDiff.asJson(json2, json1, flags).toString)
        //        _ <- Right(println(patch2))
        lc1 <- patch1.as[List[Compare]]
        lc2 <- patch2.as[List[Compare]]
      } yield compare(filterFieldBlackList(lc1), filterFieldBlackList(lc2))
    }

    def filterFieldBlackList(l: List[Compare]): List[Compare] = {
      val fieldBlackList = readBlackListFile().toSet
      l.filter( cmp => ! fieldBlackList.contains(cmp.path))
    }


    def compare(patch12: List[Compare], partch21: List[Compare]): List[Compare] =
    //separate the patch on array path from patch without array path
      patch12.filter(!arrayPath(_)) ::: compareArrays(patch12, partch21)

    // this is private because it is strongly based on the fact that path are from arrays
    private def compareArrays(patch12: List[Compare], patch21: List[Compare]): List[Compare] = {
      import Compare._

      val (withAddRemove, withoutAddRemove) = keyCompare(patch12).partition { case (k, lc) =>
        lc.exists(c => Operation.withName(c.op) == Operation.add || Operation.withName(c.op) == Operation.remove)
      }

      compareArrayAddRemoveOp(withAddRemove) ::: compareArrayReplaceOp(withoutAddRemove, patch21)
    }

    /**
      *
      * @param withoutAddRemove key the path without the last index present
      *                         ( = all compare result that have path in a given array)
      * @param patch            a json-patch (see zjsonpatch lib) patch as a List of compare
      * @return synthetic result on "replace" json patch operation of each arrray (replace due to difference in order
      *         vs "real" replace)
      */
    private def compareArrayReplaceOp(withoutAddRemove: Map[String, List[Compare]],
                                      patch: List[Compare]): List[Compare] = {
      val kc21 = keyCompare(patch)
      val lcReplaceOnly = withoutAddRemove.
        map { case (k, lc) => (lc, kc21.getOrElse(k, List())) }.
        filter { case (_, l21) => l21.nonEmpty }

      // compare list compare of array path from the same json array
      lcReplaceOnly.flatMap { case (l1, l2) =>

        val ml2 = ListBuffer(l2: _*)

        l1.foldLeft(ml2) { case (ml, c) =>
          val idx = ml.indexOf(c)
          if (idx >= 0) ml.remove(idx)
          ml
        }

        if (ml2.nonEmpty) Some(l1.head.copy(msg = "array compare yield replace with at least one discrepancy."))
        else None

      }.toList
    }

    /**
      *
      * @param withAddRemove key the path without the last index present
      *                      ( = all compare result that have path in a given array)
      * @return 1) the synthetic compare with detection of "replace" json patch operation as equivalent
      *         to a pair of "add/remove" operation
      *         2) the "real" add/remove operation
      */
    private def compareArrayAddRemoveOp(withAddRemove: Map[String, List[Compare]]): List[Compare] = {
      // array that containt "remove/add"
      withAddRemove.flatMap { case (k, lc) =>
        val sameValOp = lc.groupBy(_.value)
        val (addRemovePair, others) = sameValOp.partition { lcso =>
          if (lcso._2.size == 2 &&
            lcso._2.exists(c => Operation.withName(c.op) == Operation.add) &&
            lcso._2.exists(c => Operation.withName(c.op) == Operation.remove)) true
          else false
        }
        if (others.isEmpty) None
        //Compare("replace", k, None, "identical, the order is different")
        else Some(Compare("add/remove", k, None, "array compare yield add/remove in patch."))
      }.toList
    }

    private def keyCompare(lc: List[Compare]) = lc.filter(arrayPath).
      map(c => c.copy(path = buildKey(c.path))).
      groupBy(_.path)

    def arrayPath(cpr: Compare): Boolean = arrIdx.findFirstIn(cpr.path).isDefined

    def arrayIndex(path: String): Vector[String] =
      path.split("/").
        filter((el: String) => integer.findFirstIn(el).isDefined).
        toVector

    def buildKey(path: String): String = {
      val ai = arrayIndex(path)
      if (ai.isEmpty) throw new Exception("Build key just for arrays path (arrIdx empty !)")
      //ai.foreach(println)
      path.reverse.replaceFirst(s"${ai.last.reverse}/", "").reverse
    }

    def parseJson(json: String): Either[Exception, JsonNode] =
      Try(new ObjectMapper().readTree(json)) match {
        case Success(node) => Right(node)
        case Failure(error) => Left(new Exception("error occurs parsing json", error))
      }


  }

}

