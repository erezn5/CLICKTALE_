package com.clicktale.pipe.diff.conf

import com.typesafe.config.Config

package object conf {


  trait KafkaConsumerSetting {
    def boostrapServer: String

    def groupId: String

    def tuning: Config
  }


  trait Topic {
    def topic: String
  }

  trait FlowConf {
    def mode: String

    def pid: Int

    def subid: Int

    def resultDirectory: String
  }

  trait AMQPSourceConf {
    // TODO - add virtualHost (String)

    def host: String

    def port: Int

    def username: String

    def password: String

    def queueName: String

    def prefetchCount: Int
  }

  trait CageConf {
    // TODO - add port as configurable

    def host: String

    def port: Int

    def apikey: (String, String)
  }

}
