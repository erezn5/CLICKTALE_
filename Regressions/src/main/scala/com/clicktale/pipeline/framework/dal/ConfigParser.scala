package com.clicktale.pipeline.framework.dal

import com.typesafe.config.{Config, ConfigFactory}
import org.json4s.DefaultFormats

object ConfigParser {

  implicit val formats: DefaultFormats.type =
    DefaultFormats

  final val conf: Config =
    ConfigFactory.load("regConfig.conf")

}
