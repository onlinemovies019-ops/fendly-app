package com.example.fendly

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File

class VoiceAudioRecorder(private val context: Context) {
    private var recorder: MediaRecorder? = null
    private var outputFile: File? = null

    val isRecording: Boolean
        get() = recorder != null

    @Synchronized
    fun start(): File {
        check(recorder == null) { "An audio recording is already active" }
        val file = File.createTempFile("fendly-voice-", ".m4a", context.cacheDir)
        val newRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }
        try {
            newRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
            newRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            newRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            newRecorder.setAudioEncodingBitRate(96_000)
            newRecorder.setAudioSamplingRate(44_100)
            newRecorder.setOutputFile(file.absolutePath)
            newRecorder.prepare()
            newRecorder.start()
        } catch (error: Exception) {
            newRecorder.release()
            file.delete()
            throw error
        }
        recorder = newRecorder
        outputFile = file
        return file
    }

    @Synchronized
    fun stop(): File {
        val activeRecorder = checkNotNull(recorder) { "No audio recording is active" }
        val file = checkNotNull(outputFile) { "No audio file is available" }
        recorder = null
        outputFile = null
        try {
            activeRecorder.stop()
        } catch (error: Exception) {
            activeRecorder.release()
            file.delete()
            throw error
        }
        activeRecorder.release()
        if (file.length() == 0L) {
            file.delete()
            throw IllegalStateException("The audio recording is empty")
        }
        return file
    }

    @Synchronized
    fun cancel() {
        val activeRecorder = recorder ?: return
        val file = outputFile
        recorder = null
        outputFile = null
        try {
            activeRecorder.stop()
        } catch (_: RuntimeException) {
        } finally {
            activeRecorder.release()
            file?.delete()
        }
    }
}
