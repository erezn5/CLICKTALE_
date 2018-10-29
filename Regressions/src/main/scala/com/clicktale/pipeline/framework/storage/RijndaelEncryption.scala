package com.clicktale.pipeline.storagemaster
package support.serialization.encryption

import java.nio.charset.StandardCharsets
import java.security.InvalidKeyException
import javax.crypto.{Cipher, SecretKeyFactory}
import javax.crypto.spec.{IvParameterSpec, PBEKeySpec, SecretKeySpec}

import com.clicktale.pipeline.framework.storage.support.serialization.encryption.Encryption
import com.clicktale.pipeline.storagemaster.support.serialization.encryption.RijndaelEncryption._

class RijndaelEncryption(phrase: String) extends Encryption {
  val encryption: CipherEncrypt = createRijndaelEncrypt(phrase)
  val decryption: CipherDecrypt = createRijndaelDecrypt(phrase)

  override def encrypt(decrypted: Array[Byte]): Array[Byte] = {
    val encryptedBytes = encryption(decrypted)
    encryptedBytes
  }

  override def decrypt(encrypted: Array[Byte]): Array[Byte] = {
    val dectyptedBytes = decryption(encrypted)
    dectyptedBytes
  }
}

object RijndaelEncryption {
  final val DefaultSalt: String = "salt"

  def createRijndaelEncrypt(phrase: String): CipherEncrypt = {
    val cipherParameters = createRijndaelCipherParameters(phrase)
    CipherEncrypt(cipherParameters)
  }

  def createRijndaelDecrypt(phrase: String): CipherDecrypt = {
    val cipherParameters = createRijndaelCipherParameters(phrase)
    CipherDecrypt(cipherParameters)
  }

  def createRijndaelCipherParameters(phrase: String, salt: String = DefaultSalt): CipherParameters = {
    val saltCharArray = (phrase + salt).getBytes(StandardCharsets.US_ASCII)
    val phraseCharArray = phrase.toCharArray
    createRijndaelCipherParameters(phraseCharArray, saltCharArray)
  }

  def createRijndaelCipherParameters(phrase: Array[Char], salt: Array[Byte]): CipherParameters = {
    val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
    val pbeKeySpec = new PBEKeySpec(phrase, salt, 1000, 384)
    val secretKey = factory.generateSecret(pbeKeySpec)
    val key = new Array[Byte](32)
    val iv = new Array[Byte](16)

    java.lang.System.arraycopy(secretKey.getEncoded, 0, key, 0, 32)
    java.lang.System.arraycopy(secretKey.getEncoded, 32, iv, 0, 16)

    val secret = new SecretKeySpec(key, "AES")
    val ivSpec = new IvParameterSpec(iv)
    try {
      val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding") // In Java, PKCS5Padding is actually PKCS7...
      CipherParameters(cipher, ivSpec, secret)
    } catch {
      case ex: InvalidKeyException if ex.getMessage == "Illegal key size" =>
        throw new UnsupportedOperationException("This machine's JRE has a 128-bit key restriction. Please deploy Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy Files.")
      case throwable: Throwable =>
        throw throwable
    }
  }

  case class CipherParameters(cipher: Cipher, iv: IvParameterSpec, secret: SecretKeySpec)
  case class CipherEncrypt(parameters: CipherParameters) {
    parameters.cipher.init(Cipher.ENCRYPT_MODE, parameters.secret, parameters.iv)
    def apply(bytes: Array[Byte]): Array[Byte] = parameters.cipher.doFinal(bytes)
  }
  case class CipherDecrypt(parameters: CipherParameters) {
    parameters.cipher.init(Cipher.DECRYPT_MODE, parameters.secret, parameters.iv)
    def apply(bytes: Array[Byte]): Array[Byte] = parameters.cipher.doFinal(bytes)
  }
}
