package com.aistudio.examtable.xyzabc.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class DataStoreManager(private val context: Context) {
    companion object {
        val THEME_MODE = booleanPreferencesKey("theme_mode")
        val AUTOSAVE_DATA = stringPreferencesKey("autosave_data")
    }

    val isDarkModeFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[THEME_MODE] ?: false
        }

    suspend fun setDarkMode(isDark: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[THEME_MODE] = isDark
        }
    }

    val autosaveDataFlow: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[AUTOSAVE_DATA]
        }

    suspend fun setAutosaveData(data: String?) {
        context.dataStore.edit { preferences ->
            if (data == null) {
                preferences.remove(AUTOSAVE_DATA)
            } else {
                preferences[AUTOSAVE_DATA] = data
            }
        }
    }
}
