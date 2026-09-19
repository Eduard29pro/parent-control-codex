package ru.makdigital.parentcontrol.security

import android.content.Context
import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

class PinManager(context: Context) {
    private val prefs = context.getSharedPreferences("pin_security", Context.MODE_PRIVATE)

    fun hasPin(): Boolean = prefs.contains(KEY_HASH) && prefs.contains(KEY_SALT)

    fun setPin(pin: CharArray) {
        require(isValidPin(pin)) { "PIN must contain 4 to 8 digits" }
        val salt = ByteArray(16).also(SecureRandom()::nextBytes)
        try {
            val hash = derive(pin, salt)
            prefs.edit()
                .putString(KEY_SALT, Base64.encodeToString(salt, Base64.NO_WRAP))
                .putString(KEY_HASH, Base64.encodeToString(hash, Base64.NO_WRAP))
                .apply()
        } finally {
            pin.fill('\u0000')
        }
    }

    fun verify(pin: CharArray): Boolean {
        return try {
            val salt = prefs.getString(KEY_SALT, null)?.let { Base64.decode(it, Base64.NO_WRAP) } ?: return false
            val expected = prefs.getString(KEY_HASH, null)?.let { Base64.decode(it, Base64.NO_WRAP) } ?: return false
            val actual = derive(pin, salt)
            MessageDigest.isEqual(expected, actual)
        } finally {
            pin.fill('\u0000')
        }
    }

    private fun derive(pin: CharArray, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(pin, salt, 120_000, 256)
        return try { SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded }
        finally { spec.clearPassword() }
    }

    companion object {
        private const val KEY_SALT = "salt"
        private const val KEY_HASH = "hash"

        fun isValidPin(pin: CharArray): Boolean =
            pin.size in 4..8 && pin.all(Char::isDigit)
    }
}
