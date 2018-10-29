package com.clicktale.pipeline.framework.storage
package support.serialization.encryption

trait Encryption {
  def encrypt(decrypted: Array[Byte]): Array[Byte]

  def decrypt(encrypted: Array[Byte]): Array[Byte]
}