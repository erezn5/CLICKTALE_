package com.clicktale.pipeline.dataObjects

import org.json4s.{JObject, _}

/**
  * Created by Asia.Salner on 05/09/2016.
  */

class SessionStream (val createDate: String,
                     val data: String,
                     val dataFlags: Int,
                     val dataFlagType: Int,
                     val legacyLiveSessionDataType: String,
                     val liveSessionId: Long,
                     val messageNumber: Int,
                     val streamId: Int,
                     val streamMessageId: Int,
                     val projectId: Int) {
  // If we write val before input vars it constract the same values

  //Override toString method
  override def toString: String = {
      createDate + " " +
      data + " " +
      dataFlags + " " +
      dataFlagType + " " +
      liveSessionId + " " +
      projectId + " " +
      streamMessageId
  }
}


object SessionStream {
  implicit val formats = DefaultFormats

  def apply(createDate: String, data: String, dataFlags: Int, dataFlagType: Int, legacyLiveSessionDataType: String, liveSessionId: Long,
            messageNumber: Int, streamId: Int, streamMessageId: Int, projectId: Int): SessionStream =
    new SessionStream(createDate, data, dataFlags, dataFlagType, legacyLiveSessionDataType, liveSessionId, messageNumber, streamId, streamMessageId, projectId)

  def apply(responseAsJson: JObject): SessionStream =  {
    new SessionStream((responseAsJson \ "CreateDate").extract[String],
      (responseAsJson \ "Data").extract[String],
      (responseAsJson \ "DataFlags").extract[Int],
      (responseAsJson \ "DataFlagType").extract[Int],
      (responseAsJson \ "LegacyLiveSessionDataType").extract[String],
      (responseAsJson \ "LiveSessionId").extract[Int],
      (responseAsJson \ "MessageNumber").extract[Int],
      (responseAsJson \ "StreamId").extract[Int],
      (responseAsJson \ "StreamMessageId").extract[Int],
      (responseAsJson \ "ProjectId").extract[Int])
  }
}