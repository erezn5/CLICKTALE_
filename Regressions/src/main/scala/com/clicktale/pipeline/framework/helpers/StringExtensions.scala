package com.clicktale.pipeline.framework.helpers

import scala.util.{Failure, Success, Try}
import scala.xml.{Elem, XML}

/**
  * Created by Asia.Salner on 17/05/2017.
  */
object StringExtensions {
  implicit class MyStringExtension(val value: String) extends AnyVal {
    def escapeChars = StringContext.treatEscapes(value).replaceAll("(\\t|\\n|\\r)", "")
    def exctratedXml: Elem = {
      Try (XML.loadString(value.escapeChars)) match {
        case Success(xml) if value != null => xml
        case Failure(_) if value != null => <empty></empty>
        case _ => null
      }}
  }

}
