package com.clicktale.pipeline.framework.helpers

object CsvReader {

  def GetAllLines(file : String): Array[String] = {
    val source = scala.io.Source.fromFile(file)
    val ret = source.getLines mkString "\n"
    ret.split('\n')
  }
}
