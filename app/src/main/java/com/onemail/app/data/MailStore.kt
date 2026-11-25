package com.onemail.app.data

import android.content.Context
import android.content.SharedPreferences

class MailStore(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun loadAccount(): AccountData? {
        val address = prefs.getString(KEY_ADDRESS, null) ?: return null
        val accountId = prefs.getString(KEY_ACCOUNT_ID, null) ?: return null
        val password = prefs.getString(KEY_PASSWORD, null) ?: return null
        val token = prefs.getString(KEY_TOKEN, null) ?: return null
        return AccountData(
            id = accountId,
            address = address,
            password = password,
            token = token
        )
    }

    fun save(account: AccountData) {
        prefs.edit()
            .putString(KEY_ADDRESS, account.address)
            .putString(KEY_ACCOUNT_ID, account.id)
            .putString(KEY_PASSWORD, account.password)
            .putString(KEY_TOKEN, account.token)
            .apply()
    }

    fun saveToken(token: String) {
        prefs.edit().putString(KEY_TOKEN, token).apply()
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    private companion object {
        const val PREFS_NAME = "onemail_prefs"
        const val KEY_ADDRESS = "address"
        const val KEY_ACCOUNT_ID = "accountId"
        const val KEY_PASSWORD = "password"
        const val KEY_TOKEN = "token"
    }
}
