package com.clicktale.pipe.diff.compare

import com.clicktale.pipe.diff.model.compare.CompareModel.Compare
import io.circe.parser._
import org.scalatest.FlatSpec

import scala.io.Source

class TestCompare extends FlatSpec {

  "List[Compare]" should "be empty when comparing two identical json (I)" in {

    basicTestStructure("./compare/synthetic-data/test-1.1.json",
      "./compare/synthetic-data/test-1.2.json",
      l => {
        //        debugListCompare(l)
        assert(l.isEmpty)
      })
  }


  it should "contain a [replace] action when there is a difference in one field value" in {

    basicTestStructure("./compare/synthetic-data/test-2.1.json",
      "./compare/synthetic-data/test-2.2.json",
      l => {
        //        debugListCompare(l)
        assert(l.size == 1)
        assert(l.head == Compare("replace", "/field2", parse("4").toOption, ""))
      })
  }

  it should "contain a [remove] action when there is a difference in one field value" in {

    basicTestStructure("./compare/synthetic-data/test-3.1.json",
      "./compare/synthetic-data/test-3.2.json",
      l => {
        //        debugListCompare(l)
        assert(l.size == 1)
        assert(l.head == Compare("remove", "/field2", parse("2").toOption, ""))
      })
  }

  it should "contain a [add] action when there is a difference in one field value" in {

    basicTestStructure("./compare/synthetic-data/test-4.1.json",
      "./compare/synthetic-data/test-4.2.json",
      l => {
        //        debugListCompare(l)
        assert(l.size == 1)
        assert(l.head == Compare("add", "/field3", parse("3").toOption, ""))
      })
  }

  it should "be empty when comparing two identical json with array" in {

    basicTestStructure("./compare/synthetic-data/test-5.1.json",
      "./compare/synthetic-data/test-5.2.json",
      l => {
        //        debugListCompare(l)
        assert(l.isEmpty)
      })
  }

  it should "contain [add/remove] field for different array with add op" in {

    basicTestStructure("./compare/synthetic-data/test-6.1.json",
      "./compare/synthetic-data/test-6.2.json",
      l => {
        //        debugListCompare(l)
        assert(l.head == Compare("add/remove", "/myarray",
          None,
          "array compare yield add/remove in patch."))
      })
  }

  it should "contain [add/remove] field for different array with remove op" in {

    basicTestStructure("./compare/synthetic-data/test-7.1.json",
      "./compare/synthetic-data/test-7.2.json",
      l => {
        //        debugListCompare(l)
        assert(l.head == Compare("add/remove", "/myarray",
          None,
          "array compare yield add/remove in patch."))
      })
  }

  it should "should be empty for identical array not in same order" in {

    basicTestStructure("./compare/synthetic-data/test-8.1.json",
      "./compare/synthetic-data/test-8.2.json",
      l => {
        //        debugListCompare(l)
        assert(l.isEmpty)

      })
  }

  it should "should yield discrepancy when same size array with difference in data" in {

    basicTestStructure("./compare/synthetic-data/test-9.1.json",
      "./compare/synthetic-data/test-9.2.json",
      l => {
        //        debugListCompare(l)
        assert(l.head == Compare("replace", "/myarray",
          Some(parse("5").getOrElse(null)),
          "array compare yield replace with at least one discrepancy."))

      })
  }

  it should "be empty when comparing two identical files (real data)" in {

    basicTestStructure("./compare/analytics-1.1.json",
      "./compare/analytics-1.2.json",
      l => {
        //        debugListCompare(l)
        assert(l.isEmpty)
      })
  }

  it should "be empty when comparing arrays identical but for the order (real data)" in {

    basicTestStructure("./compare/analytics-2.1.json",
      "./compare/analytics-2.2.json",
      l => {
        //        debugListCompare(l)
        assert(l.isEmpty)
      })
  }

//  it should "be xml pilot test" in {
//    val xml1 = scala.xml.XML.loadFile("C:\\Git\\pipe-diff\\src\\test\\resources\\compare\\synthetic-xml-files\\prod_dsr.xml")
//    val xml2 = scala.xml.XML.loadFile("C:\\Git\\pipe-diff\\src\\test\\resources\\compare\\synthetic-xml-files\\staging_dsr.xml")
//
//    val xml3 = scala.xml.XML.loadFile("C:\\Git\\pipe-diff\\src\\test\\resources\\compare\\synthetic-xml-files\\processorNET.xml")
//    val xml4 = scala.xml.XML.loadFile("C:\\Git\\pipe-diff\\src\\test\\resources\\compare\\synthetic-xml-files\\pipe.xml")
//
//    println("Results for the first xml file: ")
//    val recording1: Map[String, Int] = (xml3 \\ "_").groupBy(_.label).map {
//      case (k, v) => (k, v.size)
//    }
//    recording1 foreach {
//      case (k, v) => println(s"$k count: $v")
//    }
//
//    println("=================================================")
//    println("Results for the second xml file: \n")
//    val recording2: Map[String, Int] = (xml4 \\ "_").groupBy(_.label).map {
//      case (k, v) => (k, v.size)
//    }
//
//    recording2 foreach {
//      case (k, v) => println(s"$k count: $v")
//    }
//
//    recording1.toList.union(recording2.toList).groupBy(_._1).collect{
//      case (k,(_,h)::(_,t)::Nil) if h != t => (k,(h,t))
//      case (k,(_,h)::Nil) => (k,(h,-1))
//    }.foreach(println)
//
//    //recording1.zipAll(recording2,("", -1),("",-1)).filter(x => x._1._2 != x._2._2).foreach(println)
//  }

  def debugListCompare(lc: List[Compare]): Unit = {
    println("List Compare content: ")
    lc.foreach(println)
  }

  def loadResourceAsStr(path: String): String = Source.fromResource(path).mkString("")

  def basicTestStructure[R](path1: String,
                            path2: String,
                            actionOnListCompare: List[Compare] => R): R = {
    import com.clicktale.pipe.diff.model.compare.CompareModel.CompareJson._

    val lc = compare(loadResourceAsStr(path1),
      loadResourceAsStr(path2))

    lc.fold(ex => throw ex, actionOnListCompare)
  }



}
