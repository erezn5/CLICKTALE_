package com.clicktale.pipeline.framework.queue

import scala.util._
import org.apache.kafka.clients._
import collection.JavaConverters._
import org.apache.kafka.common.config._
import org.apache.kafka.clients.consumer._
import org.apache.kafka.clients.producer._

class KafkaClient(servers: String = "172.22.0.212:9092",
                  isSSL: Boolean = false,
                  password: String = "",
                  encryptionFile: String = "",
                  startConsumer: Boolean = true) {

  private val producer: KafkaProducer[String, String] = initializeProducer()
  private val consumer: Option[KafkaConsumer[String, String]] =
    if (startConsumer) Some(initializeConsumer()) else None

  def publish(kafkaTopic: String, sid: Long, data: String): RecordMetadata = {
    Try(send(kafkaTopic, sid, data)) match {
      case Success(x) => x
      case Failure(x) => throw new Exception(
        s"publish failed for sid: $sid, $x")
    }
  }

  private def send(kafkaTopic: String, sid: Long, data: String): RecordMetadata = {
    val record = new ProducerRecord[String, String](kafkaTopic, sid.toString, data)
    producer.send(record).get()
  }

  def consume(topics: Traversable[String], autoCommit: Boolean = false): String = {
    consumer.get.subscribe(topics.toSeq.asJavaCollection)
    //println(consumer.get.subscribe(topics.toSeq.asJavaCollection))
    //consumer.seekToBeginning(new TopicPartition(topics.head, ))
    val records = consumer.get.poll(10000)
    val iter = records.iterator()
    val message = iter.next().value()
    if (autoCommit) consumer.get.commitSync()
    message
  }

  private def initializeProducer(): KafkaProducer[String, String] = {
    val props = new java.util.Properties()
    props.put("bootstrap.servers", servers)
    props.put("client.id", "KafkaProducer")
    props.put("acks", "1") // Wait for an ack from the leader of the brokers that the message was received
    // Option linger for an amount of ms
    props.put("compression.type", "gzip")
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer") // LongSerializer does not exist
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    // define ssl
    if (isSSL) defineSSL(props)
    new KafkaProducer[String, String](props)
  }

  private def initializeConsumer(): KafkaConsumer[String, String] = {
    val props = new java.util.Properties()
    props.put("bootstrap.servers", servers)
    props.put("group.id", "regressionTests")
    props.put("client.id", "regTestsKafkaConsumer")
    props.put("enable.auto.commit", "true")
    props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    // define ssl
    if (isSSL) defineSSL(props)
    new KafkaConsumer[String, String](props)
  }

  private def defineSSL(props: java.util.Properties) = {
    //configure the following three settings for SSL Encryption
    props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SSL")
    props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, password)
    props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, encryptionFile)
    // configure the following three settings for SSL Authentication/
    props.put(SslConfigs.SSL_KEY_PASSWORD_CONFIG, password)
    props.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, password)
    props.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, encryptionFile)
  }

  def shutDown(): Unit = {
    producer.close()

    if (consumer.nonEmpty) {
      consumer.get.unsubscribe()
      consumer.get.close()
    }
  }
}