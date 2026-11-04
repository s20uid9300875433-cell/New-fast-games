package com.hexacolor.quest.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.hexacolor.quest.model.UserProgress
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "hexa_color_quest_progress"
)

class ProgressRepository(private val context: Context) {

    private val progressJsonKey = stringPreferencesKey("user_progress_v1")

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    val progressFlow: Flow<UserProgress> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { prefs ->
            val raw = prefs[progressJsonKey]
            if (raw.isNullOrBlank()) {
                UserProgress()
            } else {
                runCatching { json.decodeFromString(UserProgress.serializer(), raw) }
                    .getOrDefault(UserProgress())
            }
        }

    suspend fun updateProgress(transform: (UserProgress) -> UserProgress): UserProgress {
        var result = UserProgress()
        context.dataStore.edit { prefs ->
            val current = prefs[progressJsonKey]?.let { raw ->
                runCatching { json.decodeFromString(UserProgress.serializer(), raw) }.getOrNull()
            } ?: UserProgress()
            val updated = transform(current)
            result = updated
            prefs[progressJsonKey] = json.encodeToString(UserProgress.serializer(), updated)
        }
        return result
    }

    suspend fun recordLevelCompletion(
        levelId: Int,
        starsEarned: Int,
        scoreEarned: Int,
        coinsBonus: Int
    ): UserProgress {
        return updateProgress { current ->
            val prevStars = current.stars[levelId] ?: 0
            val newStars = maxOf(prevStars, starsEarned.coerceIn(1, 3))
            val prevScore = current.highScores[levelId] ?: 0
            val newScore = maxOf(prevScore, scoreEarned)

            current.copy(
                unlockedLevel = minOf(100, maxOf(current.unlockedLevel, levelId + 1)),
                stars = current.stars + (levelId to newStars),
                highScores = current.highScores + (levelId to newScore),
                coins = current.coins + coinsBonus
            )
        }
    }

    suspend fun resetProgress() {
        context.dataStore.edit { prefs ->
            prefs.remove(progressJsonKey)
        }
    }
}
