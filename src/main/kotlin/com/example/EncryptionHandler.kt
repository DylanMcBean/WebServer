package com.example

import org.mindrot.jbcrypt.BCrypt
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import java.security.SecureRandom
import java.util.Base64

object EncryptionHandler {
	private const val KEY_ALGORITHM = "AES"
	private const val TRANSFORMATION = "AES/GCM/NoPadding"
	private const val GCM_TAG_LENGTH = 128
	private const val ITERATION_COUNT = 65536
	private const val KEY_LENGTH = 256
	private const val SALT_LENGTH = 16

	private val secureRandom = SecureRandom()

	fun generateSalt(): String = BCrypt.gensalt(12, SecureRandom())
	fun hashPassword(password: String, salt: String): String = BCrypt.hashpw(password, salt).toString()
	fun verifyPassword(password: String, hashedPassword: String): Boolean = BCrypt.checkpw(password,hashedPassword)
	fun generateEncryptionKey(): String {
		val random = SecureRandom()
		val bytes = ByteArray(32)
		random.nextBytes(bytes)
		return bytes.joinToString("") { "%02x".format(it) }
	}

	fun encryptCookie(input: String, key: String?): String {
		val cipher = Cipher.getInstance(TRANSFORMATION)
		val secretKey = deriveKey(key!!)
		val iv = ByteArray(SALT_LENGTH).apply { secureRandom.nextBytes(this) }
		val gcmParameterSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
		cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmParameterSpec)
		val encryptedBytes = cipher.doFinal(input.toByteArray(Charsets.UTF_8))
		val resultBytes = iv + encryptedBytes
		return Base64.getEncoder().encodeToString(resultBytes)
	}

	fun decryptCookie(input: String, key: String?): String {
		val decodedBytes = Base64.getDecoder().decode(input)
		val iv = decodedBytes.sliceArray(0 until SALT_LENGTH)
		val encryptedBytes = decodedBytes.sliceArray(SALT_LENGTH until decodedBytes.size)
		val cipher = Cipher.getInstance(TRANSFORMATION)
		val secretKey = deriveKey(key!!)
		val gcmParameterSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
		cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmParameterSpec)
		val decryptedBytes = cipher.doFinal(encryptedBytes)
		return String(decryptedBytes, Charsets.UTF_8)
	}

	private fun deriveKey(password: String): SecretKey {
		val salt = ByteArray(SALT_LENGTH).apply { secureRandom.nextBytes(this) }
		val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
		val keySpec = PBEKeySpec(password.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH)
		val secretKey = factory.generateSecret(keySpec)
		return SecretKeySpec(secretKey.encoded, KEY_ALGORITHM)
	}
}