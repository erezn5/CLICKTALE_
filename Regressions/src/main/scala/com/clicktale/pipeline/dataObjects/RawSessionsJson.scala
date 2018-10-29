package com.clicktale.pipeline.dataObjects

class RawSessionsJson(val subscriber: Int,
                      val processedCount: Long,
                      val ip: String,
                      val useragent: String,
                      val authTime: Long,
                      val project: Int,
                      val messages: String) {
}
  object RawSessionsJson{
def apply(subscriber: Int,
          processedCount: Long,
          ip: String,
          useragent: String,
          authTime: Long,
          project: Int,
          messages: String): RawSessionsJson = {
  new RawSessionsJson(subscriber, processedCount, ip, useragent,authTime,project,messages)
}
}