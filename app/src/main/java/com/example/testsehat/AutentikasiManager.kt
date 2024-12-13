package com.example.testsehat
import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object AuthManager {
    private const val PREF_NAME = "AuthPreferences"
    private const val KEY_TOKEN = "auth_token"
    private const val KEY_EMAIL = "user_email"

    private fun getEncryptedPreferences(context: Context): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            context,
            PREF_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun saveAuthDetails(context: Context, token: String, email: String) {
        getEncryptedPreferences(context).edit().apply {
            putString(KEY_TOKEN, token)
            putString(KEY_EMAIL, email)
        }.apply()
    }

    fun getToken(context: Context): String? {
        return getEncryptedPreferences(context).getString(KEY_TOKEN, null)
    }

    fun getEmail(context: Context): String? {
        return getEncryptedPreferences(context).getString(KEY_EMAIL, null)
    }



    fun isLoggedIn(context: Context): Boolean {
        return getToken(context) != null
    }

    fun logout(context: Context) {
        getEncryptedPreferences(context).edit().clear().apply()
    }
}