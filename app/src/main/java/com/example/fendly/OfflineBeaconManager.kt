package com.example.fendly

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext

class OfflineBeaconManager(private val context: Context) {
    fun isInternetConnected(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            connectivityManager.getNetworkCapabilities(network) != null
        } else {
            @Suppress("DEPRECATION")
            connectivityManager.activeNetworkInfo?.isConnected == true
        }
    }

    fun buildPayload(
        userId: String,
        latitude: Double? = null,
        longitude: Double? = null,
        additional: Map<String, Any> = emptyMap(),
    ): Map<String, Any> {
        val payload = linkedMapOf<String, Any>(
            "userId" to userId.ifBlank { "anonymous" },
            "offline" to true,
            "lat" to (latitude ?: 0.0),
            "lng" to (longitude ?: 0.0),
            "source" to "android-offline-beacon",
        )
        payload.putAll(additional)
        return payload
    }

    fun triggerOfflinePing(
        userId: String,
        latitude: Double? = null,
        longitude: Double? = null,
    ): Boolean {
        val payload = buildPayload(userId, latitude, longitude)
        if (isInternetConnected()) {
            Log.d("OfflineBeaconManager", "Network available, skipping offline ping. Payload=${payload}")
            return true
        }

        val message = buildString {
            append("FENDLY SOS: ")
            append("Please contact me. ")
            if (latitude != null && longitude != null) {
                append("Location: https://maps.google.com/?q=$latitude,$longitude ")
            }
            append("(Fendly user ")
            append(userId.ifBlank { "anonymous" })
            append(")")
        }

        return try {
            val smsIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:")).apply {
                putExtra("sms_body", message)
            }
            context.startActivity(smsIntent)
            Log.d("OfflineBeaconManager", "Opened SMS composer for offline ping; awaiting user confirmation")
            true
        } catch (exception: Exception) {
            Log.e("OfflineBeaconManager", "Unable to open SMS composer for offline ping", exception)
            false
        }
    }
}

@Composable
fun OfflineBeaconActionButton(
    userId: String,
    modifier: Modifier = Modifier,
    latitude: Double? = null,
    longitude: Double? = null,
    onResult: (Boolean) -> Unit = {},
) {
    val context = LocalContext.current
    val beaconManager = remember(context) { OfflineBeaconManager(context) }

    Button(
        onClick = {
            val sent = beaconManager.triggerOfflinePing(userId, latitude, longitude)
            onResult(sent)
        },
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(text = "Send Offline Ping / Panic Beacon")
    }
}
