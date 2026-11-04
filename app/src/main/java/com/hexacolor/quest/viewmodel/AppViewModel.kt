package com.hexacolor.quest.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hexacolor.quest.audio.SoundSynthesizer
import com.hexacolor.quest.data.ProgressRepository
import com.hexacolor.quest.model.UserProgress
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppViewModel(
    private val progressRepository: ProgressRepository,
    val soundSynthesizer: SoundSynthesizer
) : ViewModel() {

    val progress: StateFlow<UserProgress> = progressRepository.progressFlow
        .onEach { p ->
            soundSynthesizer.updateSettings(
                soundEnabled = p.soundEnabled,
                musicEnabled = p.musicEnabled,
                hapticsEnabled = p.hapticsEnabled,
                soundVolume = p.soundVolume,
                musicVolume = p.musicVolume
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = UserProgress()
        )

    fun toggleSound() {
        viewModelScope.launch {
            val next = !progress.value.soundEnabled
            soundSynthesizer.soundEnabled = next
            if (next) soundSynthesizer.playClick()
            progressRepository.updateProgress { it.copy(soundEnabled = next) }
        }
    }

    fun toggleMusic() {
        soundSynthesizer.playClick()
        viewModelScope.launch {
            val next = !progress.value.musicEnabled
            soundSynthesizer.setMusicActive(next)
            progressRepository.updateProgress { it.copy(musicEnabled = next) }
        }
    }

    fun toggleHaptics() {
        viewModelScope.launch {
            val next = !progress.value.hapticsEnabled
            soundSynthesizer.hapticsEnabled = next
            soundSynthesizer.playClick()
            progressRepository.updateProgress { it.copy(hapticsEnabled = next) }
        }
    }

    fun setSoundVolume(volume: Float) {
        val clamped = volume.coerceIn(0f, 1f)
        soundSynthesizer.soundVolume = clamped
        viewModelScope.launch {
            progressRepository.updateProgress { it.copy(soundVolume = clamped) }
        }
    }

    fun setMusicVolume(volume: Float) {
        val clamped = volume.coerceIn(0f, 1f)
        soundSynthesizer.musicVolume = clamped
        viewModelScope.launch {
            progressRepository.updateProgress { it.copy(musicVolume = clamped) }
        }
    }

    fun resetProgress() {
        soundSynthesizer.playClick()
        viewModelScope.launch {
            progressRepository.resetProgress()
        }
    }

    fun playClick() {
        soundSynthesizer.playClick()
    }

    class Factory(
        private val progressRepository: ProgressRepository,
        private val soundSynthesizer: SoundSynthesizer
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AppViewModel(progressRepository, soundSynthesizer) as T
        }
    }
}
