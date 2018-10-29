package com.clicktale.pipeline.framework.helpers

import org.json4s.JsonAST.JField
import org.json4s._

object JsonFunctions {
  /**
    * Receives a Json Object and returns it in a more convinient way.
    * @param jObj - The Json object
    * @return - A list of tuples.
    */
  def getTupleListFromJObject(jObj : JValue) : List[(String, JValue)] = {
    jObj filterField {
      case JField(_, _) => true
      case _ => false
    }
  }
}
