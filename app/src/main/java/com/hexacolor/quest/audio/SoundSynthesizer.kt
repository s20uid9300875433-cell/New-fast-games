package com.hexacolor.quest.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/**
 * Offline procedural PCM audio synthesizer & tactile haptics engine.
 * Synthesizes warm harmonic chimes, tactile slides, victory chords, and
 * relaxing ambient Lydian pad chords with zero external audio assets.
 */
class SoundSynthesizer(private val context: Context) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val sampleRate = 22050

    @Volatile
    var soundEnabled: Boolean = true

    @Volatile
    var musicEnabled: Boolean = true
        private set

    @Volatile
    var hapticsEnabled: Boolean = true

    @Volatile
    var soundVolume: Float = 0.8f

    @Volatile
    var musicVolume: Float = 0.5f

    private var musicJob: Job? = null
    private var appInForeground: Boolean = true

    fun updateSettings(
        soundEnabled: Boolean,
        musicEnabled: Boolean,
        hapticsEnabled: Boolean,
        soundVolume: Float,
        musicVolume: Float
    ) {
        this.soundEnabled = soundEnabled
        this.hapticsEnabled = hapticsEnabled
        this.soundVolume = soundVolume.coerceIn(0f, 1f)
        this.musicVolume = musicVolume.coerceIn(0f, 1f)
        setMusicActive(musicEnabled)
    }

    fun setMusicActive(enabled: Boolean) {
        musicEnabled = enabled
        if (enabled && appInForeground) {
            startMusicLoopIfNeeded()
        } else {
            stopMusicLoop()
        }
    }

    fun onAppResumed() {
        appInForeground = true
        if (musicEnabled) {
            startMusicLoopIfNeeded()
        }
    }

    fun onAppPaused() {
        appInForeground = false
        stopMusicLoop()
    }

    // --- Haptics ---

    private fun vibrate(durationMs: Long, amplitude: Int) {
        if (!hapticsEnabled) return
        runCatching {
            val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            if (vibrator?.hasVibrator() == true) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(
                        VibrationEffect.createOneShot(
                            durationMs,
                            amplitude.coerceIn(1, 255)
                        )
                    )
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(durationMs)
                }
            }
        }
    }

    // --- Procedural Sound Effects ---

    fun playClick() {
        vibrate(10L, 45)
        if (!soundEnabled) return
        playToneSweep(startHz = 640.0, endHz = 640.0, durationSec = 0.045, gain = 0.14f)
    }

    fun playSelect() {
        vibrate(14L, 65)
        if (!soundEnabled) return
        playToneSweep(startHz = 540.0, endHz = 820.0, durationSec = 0.08, gain = 0.22f)
    }

    fun playMove() {
        vibrate(18L, 80)
        if (!soundEnabled) return
        playToneSweep(startHz = 320.0, endHz = 480.0, durationSec = 0.11, gain = 0.24f)
    }

    fun playInvalidMove() {
        vibrate(35L, 120)
        if (!soundEnabled) return
        playToneSweep(startHz = 180.0, endHz = 120.0, durationSec = 0.15, gain = 0.26f)
    }

    fun playUndo() {
        vibrate(15L, 60)
        if (!soundEnabled) return
        playToneSweep(startHz = 620.0, endHz = 310.0, durationSec = 0.14, gain = 0.22f)
    }

    fun playSuccess() {
        if (!soundEnabled) return
        playArpeggio(
            frequencies = listOf(587.33, 880.0),
            stepDelaySec = 0.08,
            noteDurationSec = 0.26,
            gain = 0.22f
        )
    }

    fun playHint() {
        vibrate(20L, 90)
        if (!soundEnabled) return
        playArpeggio(
            frequencies = listOf(880.0, 1108.73, 1318.51, 1661.22),
            stepDelaySec = 0.065,
            noteDurationSec = 0.25,
            gain = 0.20f
        )
    }

    fun playMerge(tierNumber: Int, combo: Int = 1) {
        vibrate(28L + (combo * 8L), minOf(255, 130 + combo * 35))
        if (!soundEnabled) return

        val baseFreqs = listOf(329.63, 392.00, 493.88, 587.33, 659.25, 880.00)
        val base = baseFreqs[(tierNumber - 1).coerceIn(0, baseFreqs.lastIndex)]
        val mult = 1.0 + (combo - 1) * 0.12
        val notes = listOf(
            base * mult,
            base * 1.25 * mult,
            base * 1.5 * mult,
            base * 2.0 * mult
        )
        playArpeggio(
            frequencies = notes,
            stepDelaySec = 0.05,
            noteDurationSec = 0.36,
            gain = 0.28f
        )
    }

    fun playVictory() {
        vibrate(60L, 180)
        if (!soundEnabled) return
        scope.launch {
            val chords = listOf(
                listOf(523.25, 659.25, 783.99),
                listOf(587.33, 739.99, 880.00),
                listOf(659.25, 830.61, 987.77),
                listOf(783.99, 987.77, 1174.66)
            )
            for (chord in chords) {
                if (!soundEnabled) break
                val pcm = synthesizeChord(chord, durationSec = 0.42, peakGain = 0.22f * soundVolume)
                playStaticBuffer(pcm)
                delay(150L)
            }
        }
    }

    fun playGameOver() {
        vibrate(45L, 140)
        if (!soundEnabled) return
        playArpeggio(
            frequencies = listOf(440.0, 392.0, 349.23, 293.66),
            stepDelaySec = 0.13,
            noteDurationSec = 0.32,
            gain = 0.20f
        )
    }

    // --- Synthesis Helpers ---

    private fun playToneSweep(
        startHz: Double,
        endHz: Double,
        durationSec: Double,
        gain: Float
    ) {
        scope.launch {
            val totalSamples = (sampleRate * durationSec).toInt().coerceAtLeast(64)
            val buffer = ShortArray(totalSamples)
            val effectiveGain = (gain * soundVolume).coerceIn(0f, 1f)
            var phase = 0.0
            for (i in 0 until totalSamples) {
                val progress = i.toDouble() / totalSamples
                val freq = startHz + (endHz - startHz) * progress
                phase += (2.0 * PI * freq) / sampleRate
                val env = exp(-4.5 * progress)
                val sample = sin(phase) * env * effectiveGain
                buffer[i] = (sample * Short.MAX_VALUE).toInt()
                    .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
            playStaticBuffer(buffer)
        }
    }

    private fun playArpeggio(
        frequencies: List<Double>,
        stepDelaySec: Double,
        noteDurationSec: Double,
        gain: Float
    ) {
        scope.launch {
            val totalDuration = (frequencies.size - 1) * stepDelaySec + noteDurationSec
            val totalSamples = (sampleRate * totalDuration).toInt().coerceAtLeast(128)
            val mix = FloatArray(totalSamples)
            val effectiveGain = (gain * soundVolume).coerceIn(0f, 1f)

            frequencies.forEachIndexed { idx, freq ->
                val startSample = (idx * stepDelaySec * sampleRate).toInt()
                val noteSamples = (noteDurationSec * sampleRate).toInt()
                var phase = 0.0
                for (n in 0 until noteSamples) {
                    val outIdx = startSample + n
                    if (outIdx >= totalSamples) break
                    val p = n.toDouble() / noteSamples
                    val attack = (n / (sampleRate * 0.008)).coerceAtMost(1.0)
                    val decay = exp(-5.0 * p)
                    phase += (2.0 * PI * freq) / sampleRate
                    mix[outIdx] += (sin(phase) * attack * decay * effectiveGain).toFloat()
                }
            }

            val pcm = ShortArray(totalSamples) { i ->
                (mix[i].coerceIn(-0.95f, 0.95f) * Short.MAX_VALUE).toInt().toShort()
            }
            playStaticBuffer(pcm)
        }
    }

    private fun synthesizeChord(
        frequencies: List<Double>,
        durationSec: Double,
        peakGain: Float,
        softAttackSec: Double = 0.015
    ): ShortArray {
        val totalSamples = (sampleRate * durationSec).toInt().coerceAtLeast(128)
        val pcm = ShortArray(totalSamples)
        val perVoiceGain = (peakGain / frequencies.size.coerceAtLeast(1)).coerceIn(0f, 0.5f)
        val attackSamples = (sampleRate * softAttackSec).coerceAtLeast(1.0)

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / sampleRate
            val progress = i.toDouble() / totalSamples
            val attack = (i / attackSamples).coerceAtMost(1.0)
            val release = exp(-3.2 * progress)
            var sum = 0.0
            for (freq in frequencies) {
                sum += sin(2.0 * PI * freq * t)
            }
            val value = (sum * perVoiceGain * attack * release).coerceIn(-0.95, 0.95)
            pcm[i] = (value * Short.MAX_VALUE).toInt().toShort()
        }
        return pcm
    }

    private fun startMusicLoopIfNeeded() {
        if (musicJob?.isActive == true) return
        musicJob = scope.launch {
            val progressions = listOf(
                listOf(146.83, 220.00, 293.66, 369.99, 440.00), // Dmaj7
                listOf(196.00, 246.94, 293.66, 369.99, 493.88), // Gmaj7
                listOf(123.47, 220.00, 293.66, 329.63, 440.00), // Bm7
                listOf(110.00, 220.00, 293.66, 329.63, 440.00)  // Asus4
            )
            var idx = 0
            while (isActive && musicEnabled && appInForeground) {
                val vol = (musicVolume * 0.14f).coerceIn(0f, 0.25f)
                if (vol > 0.005f) {
                    val chord = progressions[idx % progressions.size]
                    idx++
                    val pcm = synthesizeChord(
                        frequencies = chord,
                        durationSec = 3.2,
                        peakGain = vol,
                        softAttackSec = 0.65
                    )
                    playStaticBuffer(pcm, holdMs = 3100L)
                }
                delay(3500L)
            }
        }
    }

    private fun stopMusicLoop() {
        musicJob?.cancel()
        musicJob = null
    }

    private fun playStaticBuffer(pcm: ShortArray, holdMs: Long? = null) {
        runCatching {
            val byteSize = pcm.size * 2
            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(byteSize)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            track.write(pcm, 0, pcm.size)
            track.play()
            val sleepDuration = holdMs ?: ((pcm.size * 1000L) / sampleRate + 60L)
            scope.launch {
                delay(sleepDuration)
                runCatching {
                    track.stop()
                    track.release()
                }
            }
        }
    }
}
