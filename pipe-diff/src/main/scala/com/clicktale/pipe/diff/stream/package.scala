package com.clicktale.pipe.diff

import akka.NotUsed
import akka.kafka.scaladsl.Consumer
import akka.stream.alpakka.amqp.IncomingMessage
import akka.stream.scaladsl.Source
import org.apache.kafka.clients.consumer.ConsumerRecord

package object stream {

  type KafkaSource[K, V] = Source[KafkaRecord[K, V], Consumer.Control]
  type PDKafkaSource = KafkaSource[String, String]

  type KafkaRecord[K, V] = ConsumerRecord[K, V]
  type PDKafkaRecord = KafkaRecord[String, String]

  type AMQPInput = IncomingMessage

  type PDAMQPSource = Source[AMQPInput, NotUsed]

}
