package utils

import javax.crypto.{Cipher, SecretKey}
import java.util.Base64
import javax.crypto.spec.{SecretKeySpec, IvParameterSpec}

object encryption {
  private val algorithm = "AES"
  private val transformation = "AES/CBC/PKCS5Padding"
  private val charset = "UTF-8"

  def encrypt(plainText: String, secretKey: String, iv: String): String = {
    val keySpec = new SecretKeySpec(secretKey.getBytes(charset), algorithm)
    val ivSpec = new IvParameterSpec(iv.getBytes(charset))
    val cipher = Cipher.getInstance(transformation)
    cipher.init(
      Cipher.ENCRYPT_MODE,
      keySpec,
      ivSpec
    ) // Fixed: using ENCRYPT_MODE

    val encryptedBytes = cipher.doFinal(plainText.getBytes(charset))
    Base64.getEncoder.encodeToString(encryptedBytes)
  }

  def decrypt(encryptedText: String, secretKey: String, iv: String): String = {
    val keySpec = new SecretKeySpec(secretKey.getBytes(charset), algorithm)
    val ivSpec = new IvParameterSpec(iv.getBytes(charset))
    val cipher = Cipher.getInstance(transformation)
    cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)

    val decodedBytes = Base64.getDecoder.decode(encryptedText)
    val decryptedBytes = cipher.doFinal(decodedBytes)
    new String(decryptedBytes, charset)
  }
}
