package com.clicktale.pipeline.framework.storage
package support.serialization.encryption

trait EncryptionCache {
  def apply(phrase: String): Encryption
}
