package com.liberium.ai

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object SecureKeyStore {
    private const val ALIAS = "liberium_ai_api_key"
    private const val PREFS = "secure_ai_settings"
    private const val CIPHER = "AES/GCM/NoPadding"

    fun write(context: Context, value: String) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (value.isBlank()) {
            prefs.edit().clear().apply()
            return
        }
        val cipher = Cipher.getInstance(CIPHER)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        val encrypted = cipher.doFinal(value.toByteArray(Charsets.UTF_8))
        prefs.edit()
            .putString("iv", Base64.encodeToString(cipher.iv, Base64.NO_WRAP))
            .putString("data", Base64.encodeToString(encrypted, Base64.NO_WRAP))
            .apply()
    }

    fun read(context: Context): String {
        return runCatching {
            val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            val iv = Base64.decode(prefs.getString("iv", null), Base64.NO_WRAP)
            val data = Base64.decode(prefs.getString("data", null), Base64.NO_WRAP)
            val cipher = Cipher.getInstance(CIPHER)
            cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), GCMParameterSpec(128, iv))
            String(cipher.doFinal(data), Charsets.UTF_8)
        }.getOrDefault("")
    }

    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (keyStore.getKey(ALIAS, null) as? SecretKey)?.let { return it }

        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        generator.init(
            KeyGenParameterSpec.Builder(
                ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true)
                .build()
        )
        return generator.generateKey()
    }
}
