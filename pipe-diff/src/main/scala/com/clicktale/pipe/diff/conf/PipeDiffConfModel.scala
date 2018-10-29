package com.clicktale.pipe.diff.conf

import akka.stream.alpakka.amqp.AmqpSourceSettings
import com.clicktale.pipe.diff.conf.conf._
import com.typesafe.config.Config

import scala.util.Try

object PipeDiffConfModel {

  val confPrefix = "com.clicktale.pipe.diff"

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // AMQP-Cage Configuration
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  case class BasicFlowConf(mode: String,
                           pid: Int,
                           subid: Int,
                           resultDirectory: String) extends FlowConf

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // AMQP-Cage Configuration
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  case class AMQPSourceSetting(amqpSourceSettings: AmqpSourceSettings, prefetch: Int)

  case class AMQPConnParam(host: String,
                           port: Int,
                           username: String,
                           password: String)

  case class BasicAMQPSourceConf(conn: AMQPConnParam,
                                 queueName: String,
                                 prefetchCount: Int) extends AMQPSourceConf {
    override def host: String = conn.host

    override def port: Int = conn.port

    override def username: String = conn.username

    override def password: String = conn.password
  }

  case class BasicCageConf(host: String,
                           port: Int,
                           apikey: (String, String)) extends CageConf

  case class PipeDiffAMQPCageConf(amqpA: AMQPSourceConf,
                                  cageA: CageConf,
                                  amqpB: AMQPSourceConf,
                                  cageB: CageConf)

  def loadPipeDiffAMQPCageConf(c: Config): Try[PipeDiffAMQPCageConf] =
    Try(PipeDiffAMQPCageConf(
      loadAMQPSourcesConf(c.getConfig("source-a.amqp")),
      loadCageConf(c.getConfig("source-a.cage")),
      loadAMQPSourcesConf(c.getConfig("source-b.amqp")),
      loadCageConf(c.getConfig("source-b.cage"))))

  def loadCageConf(c: Config): CageConf =
    BasicCageConf(
      c.getString("host"),
      c.getInt("port"),
      (c.getString("apikey.name"), c.getString("apikey.value"))
    )

  def loadAMQPSourcesConf(c: Config): AMQPSourceConf =
    BasicAMQPSourceConf(
      AMQPConnParam(c.getString("host"),
        c.getInt("port"),
        c.getString("username"),
        c.getString("password")
      ),
      c.getString("queueName"),
      c.getInt("prefetchCount"))


  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Kafka Configuration
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


  case class PipeDiffConfKafka(kafkaSettingA: KafkaConsumerSetting, topicA: Topic,
                               kafkaSettingB: KafkaConsumerSetting, topicB: Topic)

  case class BasicTopic(topic: String) extends Topic

  case class BasicKafkaConsumerSetting(boostrapServer: String,
                                       groupId: String,
                                       tuning: Config) extends KafkaConsumerSetting

  def loadPipeDiffConfKafka(prefix: String, conf: Config): Try[PipeDiffConfKafka] =
    Try(PipeDiffConfKafka(loadKafkaConsumerSettingA(prefix, conf),
      loadTopicSourceAKafka(prefix, conf),
      loadKafkaConsumerSettingB(prefix, conf),
      loadTopicSourceBKafka(prefix, conf)))

  def loadTopicSourceAKafka(prefix: String, conf: Config): Topic =
    loadTopic(prefix,
      s"kafka.source-a", conf)

  def loadTopicSourceBKafka(prefix: String, conf: Config): Topic =
    loadTopic(prefix,
      s"kafka.source-b", conf)

  def loadTopic(prefix: String, source: String, conf: Config): Topic =
    BasicTopic(conf.getConfig(s"$prefix.$source").getString("topic"))


  def loadKafkaConsumerSettingA(prefix: String, conf: Config): KafkaConsumerSetting =
    loadKafkaConsumerSetting(prefix,
      s"kafka.source-a",
      conf)

  def loadKafkaConsumerSettingB(prefix: String, conf: Config): KafkaConsumerSetting =
    loadKafkaConsumerSetting(prefix,
      s"kafka.source-b",
      conf)

  def loadKafkaConsumerSetting(prefix: String,
                               source: String,
                               conf: Config): KafkaConsumerSetting = {
    val c = conf.getConfig(s"$prefix.$source")
    BasicKafkaConsumerSetting(c.getString("bootstrap-server"),
      c.getString("group-id"),
      c.getConfig("akka.kafka.consumer")
    )
  }

  def loadFlowConf(conf: Config): Try[BasicFlowConf] =
    Try(BasicFlowConf(
      conf.getString("mode"),
      conf.getInt("pid"),
      conf.getInt("sid"),
      conf.getString("resultDirectory")))

}
