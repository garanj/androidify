package com.android.developers.androidify.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.android.developers.androidify.wear.common.WatchFaceInstallationStatus
import com.android.developers.androidify.wear.common.watchFaceInstallationStatusDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "wfp")

/**
 * Storage for various app settings.
 */
class StoredStateManager(val context: Context) {
    private val activeWatchFaceApiUsedKey = booleanPreferencesKey("setActiveUsed")
    private val permissionDeniedKey = booleanPreferencesKey("permissionDenied")

    /**
     * The [setWatchFaceAsActive] API is a single-shot API - after one once it will not
     * work again. This indicates whether the API has already been used.
     */
    val activeWatchFaceApiUsed: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[activeWatchFaceApiUsedKey] == true
        }

    /**
     * Marks that the [setWatchFaceAsActive] API call has already been used.
     */
    suspend fun setActiveWatchFaceApiUsedKey(value: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[activeWatchFaceApiUsedKey] = value
        }
    }

    /**
     * Indicates whether the SET_PUSHED_WATCH_FACE_AS_ACTIVE permission has been denied already.
     */
    val watchFacePermissionDenied: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[permissionDeniedKey] == true
        }

    /**
     * Sets whether the SET_PUSHED_WATCH_FACE_AS_ACTIVE permission has already been denied once.
     */
    suspend fun setWatchFacePermissionDenied(value: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[permissionDeniedKey] = value
        }
    }

    /**
     * Sets the current status of a watch face installation.
     */
    suspend fun setWatchFaceInstallationStatus(watchFaceInstallationStatus: WatchFaceInstallationStatus) {
        context.watchFaceInstallationStatusDataStore.updateData { watchFaceInstallationStatus }
    }

    /**
     * The current status of a watch face installation.
     */
    val watchFaceInstallationStatus = context.watchFaceInstallationStatusDataStore.data
}