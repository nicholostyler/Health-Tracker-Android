package com.nicholostyler.healthtracker

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class PreferenceDataStore(private val context: Context){
    // to make sure there is only one instance
    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("settings")
    }
    private val USER_NAME = stringPreferencesKey("user_name")
    private val USER_GOAL_WEIGHT = doublePreferencesKey("user_goal_weight")
    private val USER_GOAL_DATE = stringPreferencesKey("user_goal_date")

    suspend fun saveUserToPreferencesStore(newName: String, goalWeight: Double, goalDate: LocalDate) {
        context.dataStore.edit { preferences ->
            preferences[USER_NAME] = newName
            preferences[USER_GOAL_WEIGHT] = goalWeight
            preferences[USER_GOAL_DATE] = goalDate.toString()
        }

        // save to viewmodel
        //NameUser = newName
        //GoalWeight = goalWeight
        //GoalDate = goalDate
    }

    // get userName
    val getUserName: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[USER_NAME] ?: "faker"
        }

    val getGoalWeight: Flow<Double?> = context.dataStore.data
        .map { preferences ->
            preferences[USER_GOAL_WEIGHT] ?: 1.0
        }

    val getGoalDate: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[USER_GOAL_DATE] ?: "January 9, 1983"
        }
}