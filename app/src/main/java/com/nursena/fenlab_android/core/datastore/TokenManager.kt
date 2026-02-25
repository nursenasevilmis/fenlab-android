package com.nursena.fenlab_android.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "fenlab_prefs")

@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val KEY_TOKEN         = stringPreferencesKey("jwt_token")
        private val KEY_USER_ID       = longPreferencesKey("user_id")
        private val KEY_USERNAME      = stringPreferencesKey("username")
        private val KEY_FULL_NAME     = stringPreferencesKey("full_name")
        private val KEY_ROLE          = stringPreferencesKey("role")
        private val KEY_PROFILE_IMAGE = stringPreferencesKey("profile_image_url")
    }

    // ── Flow (sürekli izleme) ──────────────────────────────────────────────
    val tokenFlow: Flow<String?> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[KEY_TOKEN] }

    val userIdFlow: Flow<Long?> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[KEY_USER_ID] }

    val roleFlow: Flow<String?> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[KEY_ROLE] }

    // ── Tek seferlik okuma (suspend) ───────────────────────────────────────
    suspend fun getToken(): String?   = context.dataStore.data.first()[KEY_TOKEN]
    suspend fun getUserId(): Long?    = context.dataStore.data.first()[KEY_USER_ID]
    suspend fun getUsername(): String? = context.dataStore.data.first()[KEY_USERNAME]
    suspend fun getFullName(): String? = context.dataStore.data.first()[KEY_FULL_NAME]
    suspend fun getRole(): String?    = context.dataStore.data.first()[KEY_ROLE]
    suspend fun isLoggedIn(): Boolean = getToken() != null

    // ── Kaydet (login/register sonrası) ───────────────────────────────────
    suspend fun saveSession(
        token: String,
        userId: Long,
        username: String,
        fullName: String,
        role: String,
        profileImageUrl: String? = null
    ) {
        context.dataStore.edit { prefs ->
            prefs[KEY_TOKEN]     = token
            prefs[KEY_USER_ID]   = userId
            prefs[KEY_USERNAME]  = username
            prefs[KEY_FULL_NAME] = fullName
            prefs[KEY_ROLE]      = role
            profileImageUrl?.let { prefs[KEY_PROFILE_IMAGE] = it }
        }
    }

    // ── Temizle (logout) ───────────────────────────────────────────────────
    suspend fun clearSession() {
        context.dataStore.edit { it.clear() }
    }
}