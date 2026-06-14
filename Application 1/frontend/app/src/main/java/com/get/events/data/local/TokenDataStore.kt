package com.get.events.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "get_prefs")

object TokenDataStore {

    private val TOKEN_KEY      = stringPreferencesKey("jwt_token")
    private val USER_NAME_KEY  = stringPreferencesKey("user_name")
    private val USER_EMAIL_KEY = stringPreferencesKey("user_email")
    private val USER_ROLE_KEY  = stringPreferencesKey("user_role")

    const val ROLE_VISITOR = "VISITOR"
    const val ROLE_ADMIN   = "ADMIN"
    const val ROLE_USER    = "USER"

    suspend fun saveToken(context: Context, token: String) {
        context.dataStore.edit { prefs -> prefs[TOKEN_KEY] = token }
    }

    fun getToken(context: Context) = context.dataStore.data
        .map { prefs -> prefs[TOKEN_KEY] }

    suspend fun saveUserInfo(context: Context, name: String, role: String, email: String = "") {
        context.dataStore.edit { prefs ->
            prefs[USER_NAME_KEY] = name
            prefs[USER_ROLE_KEY] = role
            if (email.isNotBlank()) prefs[USER_EMAIL_KEY] = email
        }
    }

    suspend fun setVisitorMode(context: Context) {
        context.dataStore.edit { prefs ->
            prefs.remove(TOKEN_KEY)
            prefs[USER_NAME_KEY] = "Visiteur"
            prefs[USER_ROLE_KEY] = ROLE_VISITOR
            prefs.remove(USER_EMAIL_KEY)
        }
    }

    fun isLoggedIn(context: Context) = context.dataStore.data
        .map { prefs -> prefs[TOKEN_KEY] != null }

    fun isVisitor(context: Context) = context.dataStore.data
        .map { prefs -> prefs[USER_ROLE_KEY] == ROLE_VISITOR }

    fun getUserEmail(context: Context) = context.dataStore.data
        .map { prefs -> prefs[USER_EMAIL_KEY] }

    fun getUserName(context: Context) = context.dataStore.data
        .map { prefs -> prefs[USER_NAME_KEY] }

    fun getUserRole(context: Context) = context.dataStore.data
        .map { prefs -> prefs[USER_ROLE_KEY] }

    suspend fun clear(context: Context) {
        context.dataStore.edit { it.clear() }
    }
}
