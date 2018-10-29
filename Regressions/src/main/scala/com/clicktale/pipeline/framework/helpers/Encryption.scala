package com.clicktale.pipeline.framework.helpers

import javax.crypto._
import javax.crypto.spec._
import java.security.InvalidKeyException

import com.clicktale.pipeline.framework.PathStrategies
import org.apache.commons.codec.digest.DigestUtils
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

object Encryption {
  def createCipher(phrase: String): (SecretKeySpec, IvParameterSpec, Cipher) = {
    val salt = (phrase + "salt").getBytes("US-ASCII")
    CreateRijndaelCipher(phrase.toCharArray, salt)
  }
  def encrypt(data: Array[Byte], phrase: String): Array[Byte] = {
    val (secret, ivSpec, cipher) = createCipher(phrase)
    cipher.init(Cipher.ENCRYPT_MODE, secret, ivSpec)
    cipher.doFinal(data)
  }
  def decrypt(data: Array[Byte], phrase: String): Array[Byte] = {
    val (secret, ivSpec, cipher) = createCipher(phrase)
    cipher.init(Cipher.DECRYPT_MODE, secret, ivSpec)
    cipher.doFinal(data)
  }
  def CreateRijndaelCipher(password: Array[Char], salt: Array[Byte]): (SecretKeySpec, IvParameterSpec, Cipher) = {
    val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
    val pbeKeySpec = new PBEKeySpec(password, salt, 1000, 384)
    val secretKey = factory.generateSecret(pbeKeySpec)
    val key = new Array[Byte](32)
    val iv = new Array[Byte](16)
    java.lang.System.arraycopy(secretKey.getEncoded, 0, key, 0, 32)
    java.lang.System.arraycopy(secretKey.getEncoded, 32, iv, 0, 16)
    val secret = new SecretKeySpec(key, "AES")
    val ivSpec = new IvParameterSpec(iv)
    try {
      val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding") // In Java, PKCS5Padding is actually PKCS7...
      (secret, ivSpec, cipher)
    }
    catch {
      case ex: InvalidKeyException if ex.getMessage == "Illegal key size" =>
        throw new UnsupportedOperationException("This machine's JRE has a 128-bit key restriction. Please deploy Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy Files.")
    }
  }

  def toShortDateFormat(sessionId: Long): String = {
    val epoch = new DateTime(2015,1,1,0,0)
    val date = epoch.plus(sessionId >> 14)
    val format = DateTimeFormat.forPattern("yyMMdd")
    format.print(date)
  }

  def getPath(sessionId: Long, subscriberId: Int, projectId: Int): String = {
    val md5Prefix = DigestUtils.md5Hex(sessionId.toString).toUpperCase.replace("/","").substring(0, 4)
    val date = toShortDateFormat(sessionId)
    val path = s"${md5Prefix}-${date}/${subscriberId}_${projectId}_${sessionId}.zip"
    path
  }
}