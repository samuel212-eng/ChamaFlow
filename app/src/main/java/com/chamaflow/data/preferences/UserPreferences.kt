package com.chamaflow.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "chamaflow_prefs")

data class UserPreferences(
    val activeChamaId: String = "",
    val activeChamaName: String = "",
    val userId: String = "",
    val userName: String = "",
    val userRole: String = "MEMBER",
    val onboardingComplete: Boolean = false
)

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    companion object {
        val ACTIVE_CHAMA_ID     = stringPreferencesKey("active_chama_id")
        val ACTIVE_CHAMA_NAME   = stringPreferencesKey("active_chama_name")
        val USER_ID             = stringPreferencesKey("user_id")
        val USER_NAME           = stringPreferencesKey("user_name")
        val USER_ROLE           = stringPreferencesKey("user_role")
        val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
    }

    val userPreferences: Flow<UserPreferences> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { prefs ->
            UserPreferences(
                activeChamaId      = prefs[ACTIVE_CHAMA_ID] ?: "",
                activeChamaName    = prefs[ACTIVE_CHAMA_NAME] ?: "",
                userId             = prefs[USER_ID] ?: "",
                userName           = prefs[USER_NAME] ?: "",
                userRole           = prefs[USER_ROLE] ?: "MEMBER",
                onboardingComplete = prefs[ONBOARDING_COMPLETE] ?: false
            )
        }

    suspend fun saveActiveChamaId(chamaId: String, chamaName: String) {
        dataStore.edit { prefs ->
            prefs[ACTIVE_CHAMA_ID]   = chamaId
            prefs[ACTIVE_CHAMA_NAME] = chamaName
        }
    }

    suspend fun saveUserInfo(userId: String, name: String, role: String) {
        dataStore.edit { prefs ->
            prefs[USER_ID]   = userId
            prefs[USER_NAME] = name
            prefs[USER_ROLE] = role
        }
    }

    suspend fun setOnboardingComplete() {
        dataStore.edit { prefs -> prefs[ONBOARDING_COMPLETE] = true }
    }

    suspend fun clearAll() {
        dataStore.edit { it.clear() }
    }
}
