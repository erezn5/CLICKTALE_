package com.clicktale.pipeline.storagemaster
package support.serialization.encryption

import java.util.concurrent.ConcurrentHashMap

import com.clicktale.pipeline.framework.storage.support.serialization.encryption.{Encryption, EncryptionCache}
import com.google.inject.Inject

import scala.collection.JavaConverters._
import scala.collection.concurrent

class RijndaelEncryptionCache @Inject()() extends EncryptionCache {
  val map: concurrent.Map[String, Encryption] = new ConcurrentHashMap[String, Encryption]().asScala

  def apply(phrase: String): Encryption = {
    map.getOrElseUpdate(phrase, new RijndaelEncryption(phrase))
  }
}
