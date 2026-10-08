package com.example.fendly

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

object SharePosterUtil {
    @JvmStatic
    fun sharePoster(
        context: Context,
        bitmap: Bitmap,
        caption: String,
        profileTags: Map<String, String?> = emptyMap(),
    ) {
        val shareDirectory = File(context.cacheDir, "share_flyers").apply { mkdirs() }
        val outputFile = File(shareDirectory, "fendly-community-poster-${System.currentTimeMillis()}.png")
        FileOutputStream(outputFile).use { output ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
        }

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            outputFile,
        )

        val socialTags = profileTags.entries
            .asSequence()
            .mapNotNull { (_, value) -> value?.trim()?.takeIf { it.isNotEmpty() } }
            .joinToString(" ")

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, buildString {
                append(caption)
                if (socialTags.isNotEmpty()) {
                    append("\n\n")
                    append(socialTags)
                }
            })
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(shareIntent, "Share community poster")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}
