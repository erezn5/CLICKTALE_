package com.clicktale.pipe.diff.conf

import com.typesafe.config.ConfigFactory
import org.scalatest.FlatSpec

import scala.util.Try

class ConfTest extends FlatSpec {

  "A pipe diff conf" should "let load kafka consumer setting" in {

    import PipeDiffConfModel._

    val conf = ConfigFactory.load("./app-conf-test.conf")

    assert(loadKafkaConsumerSettingA(confPrefix, conf) ==
      BasicKafkaConsumerSetting("localhost:9092", "pipe-diff-a", ConfigFactory.empty()))

    assert(loadKafkaConsumerSettingB(confPrefix, conf) ==
      BasicKafkaConsumerSetting("localhost:9092", "pipe-diff-b", ConfigFactory.empty()))
  }

  it should "load topic" in {


    import PipeDiffConfModel._

    val conf = ConfigFactory.load("./app-conf-test.conf")

    assert(loadTopicSourceAKafka(confPrefix, conf) == BasicTopic("proc"))

    assert(loadTopicSourceBKafka(confPrefix, conf) == BasicTopic("proc_linux"))
  }


  it should "load flow" in {

    import PipeDiffConfModel._

    val conf = ConfigFactory.load("./app-conf-test1.conf")

    assert(
      loadFlowConf(conf.getConfig(s"$confPrefix.flow")) ==
        Try(BasicFlowConf("ConsumeKafkaDisplayA", 123456, 987654, "/tmp/pipe/diff")))
  }

  it should "load amqp cage conf" in {

    import PipeDiffConfModel._

    val conf = ConfigFactory.load("./app-conf-test1.conf")

    assert(loadPipeDiffAMQPCageConf(conf.getConfig(s"$confPrefix.amqp-cage")) ==
      Try(PipeDiffAMQPCageConf(
        BasicAMQPSourceConf(
          AMQPConnParam("nv-p1-ec2-central-rmq-01-1a.nv-ct1.prod",
            5672,
            "pipeline",
            "eb72BrfGU6JA6Ne23UIg"),
          "pdt0.2.preprocess.rawsessions.queue",
          5),
        BasicCageConf("http://datareader.clicktale.net", 8080, ("toto", "tutu")),
        BasicAMQPSourceConf(
          AMQPConnParam("nv-p1-ec2-central-rmq-01-1a.nv-ct1.prod",
            5672,
            "pipeline",
            "eb72BrfGU6JA6Ne23UIg"),
          "pdt0.2.preprocess.rawsessions.queue",
          5),
        BasicCageConf("http://datareader.clicktale.net", 8080, ("toto", "tutu"))
      )))
  }

}













