package com.example.fendly

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

data class VoiceIntakeResult(
    val transcription: String,
    val title: String,
    val category: String,
    val color: String,
    val locationHint: String,
    val additionalNotes: String,
)

interface VoiceIntakeCallback {
    fun onSuccess(result: VoiceIntakeResult)
    fun onError(message: String)
}

class VoiceIntakeViewModel : ViewModel() {
    fun submitRecording(file: File, callback: VoiceIntakeCallback) {
        if (!file.isFile || file.length() == 0L) {
            callback.onError("The audio recording is empty. Please record again.")
            return
        }
        if (file.length() > MAX_AUDIO_BYTES) {
            callback.onError("The audio recording exceeds the 20 MB limit.")
            return
        }
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            callback.onError("Please sign in before using voice intake.")
            return
        }
        user.getIdToken(true)
            .addOnSuccessListener { tokenResult ->
                val token = tokenResult.token
                if (token.isNullOrBlank()) {
                    callback.onError("Could not authenticate the voice intake request.")
                    return@addOnSuccessListener
                }
                viewModelScope.launch {
                    try {
                        val result = withContext(Dispatchers.IO) {
                            uploadRecording(file, token)
                        }
                        callback.onSuccess(result)
                    } catch (error: Exception) {
                        callback.onError(error.message ?: "Voice intake failed. Please try again.")
                    }
                }
            }
            .addOnFailureListener {
                callback.onError("Could not authenticate the voice intake request.")
            }
    }

    private fun uploadRecording(file: File, token: String): VoiceIntakeResult {
        val boundary = "FendlyVoice${UUID.randomUUID()}"
        val connection = URL("${BuildConfig.API_BASE_URL}/api/intake/voice")
            .openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.connectTimeout = 15_000
        connection.readTimeout = 180_000
        connection.doOutput = true
        connection.setRequestProperty("Authorization", "Bearer $token")
        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
        connection.setRequestProperty("Accept", "application/json")

        try {
            connection.outputStream.use { output ->
                output.write(
                    ("--$boundary\r\n" +
                        "Content-Disposition: form-data; name=\"audio\"; filename=\"report.m4a\"\r\n" +
                        "Content-Type: audio/mp4\r\n\r\n").toByteArray(Charsets.UTF_8)
                )
                file.inputStream().use { input -> input.copyTo(output) }
                output.write("\r\n--$boundary--\r\n".toByteArray(Charsets.UTF_8))
            }

            val responseCode = connection.responseCode
            val responseStream = if (responseCode in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream
            }
            val responseBody = responseStream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            if (responseCode !in 200..299) {
                val detail = try {
                    JSONObject(responseBody).optString("detail")
                } catch (_: Exception) {
                    ""
                }
                throw IOException(detail.ifBlank { "Voice intake failed (HTTP $responseCode)." })
            }

            val json = JSONObject(responseBody)
            return VoiceIntakeResult(
                transcription = json.optString("transcription"),
                title = json.optString("title"),
                category = json.optString("category"),
                color = json.optString("color"),
                locationHint = json.optString("location_hint"),
                additionalNotes = json.optString("additional_notes"),
            )
        } finally {
            connection.disconnect()
        }
    }

    private companion object {
        const val MAX_AUDIO_BYTES = 20L * 1024L * 1024L
    }
}
