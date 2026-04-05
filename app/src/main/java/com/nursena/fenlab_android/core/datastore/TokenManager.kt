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
        private const val MAX_RECENT = 10
        // Son aramalar kullanıcı ID'sine göre ayrı key
        private fun recentSearchesKey(userId: Long) = stringPreferencesKey("recent_searches_$userId")
    }

    val tokenFlow: Flow<String?> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[KEY_TOKEN] }

    val userIdFlow: Flow<Long?> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[KEY_USER_ID] }

    val roleFlow: Flow<String?> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[KEY_ROLE] }

    suspend fun getToken(): String?    = context.dataStore.data.first()[KEY_TOKEN]
    suspend fun getUserId(): Long?     = context.dataStore.data.first()[KEY_USER_ID]
    suspend fun getUsername(): String? = context.dataStore.data.first()[KEY_USERNAME]
    suspend fun getFullName(): String? = context.dataStore.data.first()[KEY_FULL_NAME]
    suspend fun getRole(): String?     = context.dataStore.data.first()[KEY_ROLE]
    suspend fun isLoggedIn(): Boolean  = getToken() != null

    suspend fun saveSession(
        token: String, userId: Long, username: String,
        fullName: String, role: String, profileImageUrl: String? = null
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

    // Profil resmi URL'ini güncelle (upload sonrası)
    suspend fun updateProfileImageUrl(url: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_PROFILE_IMAGE] = url
        }
    }

    // Logout — arama geçmişi kullanıcıya özel saklandığı için korunur
    suspend fun clearSession() {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_TOKEN)
            prefs.remove(KEY_USER_ID)
            prefs.remove(KEY_USERNAME)
            prefs.remove(KEY_FULL_NAME)
            prefs.remove(KEY_ROLE)
            prefs.remove(KEY_PROFILE_IMAGE)
        }
    }

    // ── Son aramalar (kullanıcıya özel) ──────────────────────────────────────
    suspend fun getRecentSearches(): List<String> {
        val userId = getUserId() ?: return emptyList()
        val raw = context.dataStore.data.first()[recentSearchesKey(userId)] ?: return emptyList()
        return raw.split("|||").filter { it.isNotBlank() }
    }

    suspend fun addRecentSearch(query: String) {
        val userId = getUserId() ?: return
        val current = getRecentSearches().toMutableList()
        current.remove(query)
        current.add(0, query)
        val updated = current.take(MAX_RECENT).joinToString("|||")
        context.dataStore.edit { it[recentSearchesKey(userId)] = updated }
    }

    suspend fun removeRecentSearch(query: String) {
        val userId = getUserId() ?: return
        val updated = getRecentSearches().filter { it != query }.joinToString("|||")
        context.dataStore.edit { it[recentSearchesKey(userId)] = updated }
    }

    suspend fun clearRecentSearches() {
        val userId = getUserId() ?: return
        context.dataStore.edit { it[recentSearchesKey(userId)] = "" }
    }
}