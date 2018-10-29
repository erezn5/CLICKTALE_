package com.clicktale.pipeline.framework.queue

import com.clicktale.pipeline.framework.dal.ConfigParser.conf
import com.rabbitmq.client.{Channel, Connection, ConnectionFactory}

object RabbitMQ {

  val factory = new ConnectionFactory()
  factory.setHost(conf.getString(s"WebRecorder.RabbitMQ.${conf.getString("WebRecorder.Current.Environment")}.Host"))
  //factory.setPort(15671)
  factory.setUsername(conf.getString(s"WebRecorder.RabbitMQ.${conf.getString("WebRecorder.Current.Environment")}.User"))
  factory.setPassword(conf.getString(s"WebRecorder.RabbitMQ.${conf.getString("WebRecorder.Current.Environment")}.Password"))

  val connection: Connection = factory.newConnection()
  val channel: Channel = connection.createChannel()

  def clearQueue(queueName: String,exchange: String = null,key: String = null): Unit ={
    channel.queueDeclare(queueName, true, false, false, null)
    channel.queuePurge(queueName)
    if (exchange!=null)
      channel.queueBind(queueName,exchange,key)
  }

  def getMessagesForQueue(queueName: String): String ={
    val response =  channel.basicGet(queueName,false)
    if (response == null) {
      ""
    }
    else {
      val body = response.getBody
      val deliveryTag = response.getEnvelope.getDeliveryTag
      val domain = new String(body, conf.getString(s"WebRecorder.RabbitMQ.${conf.getString("WebRecorder.Current.Environment")}.DefaultCharset"))
      channel.basicAck(deliveryTag, false)
      domain
    }

  }
}
