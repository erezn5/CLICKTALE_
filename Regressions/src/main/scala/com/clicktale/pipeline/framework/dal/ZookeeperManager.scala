package com.clicktale.pipeline.framework.dal

import akka.actor.{ActorSystem, Props}
import com.clicktale.pipeline.common.configuration.ZookeeperActor
import com.clicktale.pipeline.common.configuration.ConfigurationDefinitions.Configuration
import com.clicktale.pipeline.common.configuration.ZookeeperConfigurationClient
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.ExecutionContext
import akka.agent.Agent
import com.typesafe.config.ConfigFactory
import scala.concurrent.duration._
import ConfigParser.{conf, formats}

object ZookeeperManager {
  var zooConfigLoaded: Boolean = false
  implicit val system = ActorSystem("test")
  val zooConfig = Configuration(Agent(Map[String, String]()), conf.getString(s"WebRecorder.Zookeeper.${conf.getString("WebRecorder.Current.Environment")}.Environment"),
    conf.getString(s"WebRecorder.Zookeeper.${conf.getString("WebRecorder.Current.Environment")}.ComponentType"),
    s"${conf.getString(s"WebRecorder.Zookeeper.${conf.getString("WebRecorder.Current.Environment")}.Nodes")}:${conf.getString(s"WebRecorder.Zookeeper.${conf.getString("WebRecorder.Current.Environment")}.Port")}")
  system.actorOf(Props(classOf[ZookeeperActor], zooConfig, new ZookeeperConfigurationClient(zooConfig),
    6.seconds, ExecutionContext.global), name = "test")

  /**
    *  Gets the value which is pulled out of the Zookeeper config given a key and an instance name
    * @param key - The key to pull.
    * @param instanceName - The instance name e.g: "Global", "ProjectMetrics", "RegularQueue"...
    *                     Default value is "Global"
    * @return - The value which correlates to the given key.
    */
  def getValue(key: String, instanceName: String = "Global"): String = {
    if (zooConfigLoaded) zooConfig(instanceName, key)
    else {
      Thread.sleep(12000)
      zooConfigLoaded = true
      zooConfig(instanceName, key)
    }
  }
}