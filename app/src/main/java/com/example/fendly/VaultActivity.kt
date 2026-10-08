package com.example.fendly

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.Locale
import java.util.concurrent.Executors
import com.example.fendly.ui.theme.FendlyCard
import com.example.fendly.ui.theme.FendlyPrimaryButton
import com.example.fendly.ui.theme.FendlyTextField

class VaultActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val darkMode = getSharedPreferences("fendly_settings", MODE_PRIVATE)
            .getBoolean("dark_mode", false)
        setTheme(if (darkMode) R.style.Theme_Fendly_Dark else R.style.Theme_Fendly)
        super.onCreate(savedInstanceState)
        val systemBarColor = if (darkMode) android.graphics.Color.rgb(18, 19, 25)
        else android.graphics.Color.rgb(247, 243, 238)
        window.statusBarColor = systemBarColor
        window.navigationBarColor = systemBarColor
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = !darkMode
            isAppearanceLightNavigationBars = !darkMode
        }
        val colors = if (darkMode) {
            androidx.compose.material3.darkColorScheme(
                primary = androidx.compose.ui.graphics.Color(0xFFE8B24A),
                onPrimary = androidx.compose.ui.graphics.Color(0xFF2B1D05),
                background = androidx.compose.ui.graphics.Color(0xFF121319),
                onBackground = androidx.compose.ui.graphics.Color(0xFFF7F9FC),
                surface = androidx.compose.ui.graphics.Color(0xFF181D24),
                onSurface = androidx.compose.ui.graphics.Color(0xFFF7F9FC),
                onSurfaceVariant = androidx.compose.ui.graphics.Color(0xFFAAB1BC),
                outline = androidx.compose.ui.graphics.Color(0xFF444D5A),
            )
        } else {
            androidx.compose.material3.lightColorScheme(
                primary = androidx.compose.ui.graphics.Color(0xFFE8B24A),
                onPrimary = androidx.compose.ui.graphics.Color(0xFF2B1D05),
                background = androidx.compose.ui.graphics.Color(0xFFF7F3EE),
                onBackground = androidx.compose.ui.graphics.Color.Black,
                surface = androidx.compose.ui.graphics.Color(0xFFFDFBF8),
                onSurface = androidx.compose.ui.graphics.Color.Black,
                onSurfaceVariant = androidx.compose.ui.graphics.Color(0xFF4D505A),
                outline = androidx.compose.ui.graphics.Color(0xFFCDC5B8),
            )
        }
        setContent {
            MaterialTheme(colorScheme = colors) {
                VaultScreen(onBack = ::finish)
            }
        }
    }
}

private data class VaultRecord(
    val id: String,
    val store: String,
    val purchaseDate: String,
    val item: String,
    val serial: String,
    val imei: String,
    val warrantyExpires: String,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VaultScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val t: (String) -> String = { key -> LanguageManager.profileText(context, key) }
    val format: (String, Long) -> String = { key, value ->
        String.format(Locale.getDefault(), t(key), value)
    }
    val records = remember { mutableStateListOf<VaultRecord>() }
    val executor = remember { Executors.newSingleThreadExecutor() }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var store by remember { mutableStateOf("") }
    var purchaseDate by remember { mutableStateOf("") }
    var item by remember { mutableStateOf("") }
    var serial by remember { mutableStateOf("") }
    var imei by remember { mutableStateOf("") }
    var warrantyMonths by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    var messageIsError by remember { mutableStateOf(false) }

    fun withToken(onToken: (String) -> Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            message = t("vault_auth_required")
            messageIsError = true
            return
        }
        user.getIdToken(false)
            .addOnSuccessListener { result -> onToken(result.token.orEmpty()) }
            .addOnFailureListener { error ->
                message = String.format(Locale.getDefault(), t("vault_auth_failed"), error.localizedMessage.orEmpty())
                messageIsError = true
            }
    }

    fun loadRecords() {
        withToken { token ->
            executor.execute {
                try {
                    val connection = URL("${BuildConfig.API_BASE_URL}/api/v1/vault/items").openConnection() as HttpURLConnection
                    connection.requestMethod = "GET"
                    connection.setRequestProperty("Authorization", "Bearer $token")
                    connection.connectTimeout = 15000
                    connection.readTimeout = 30000
                    val code = connection.responseCode
                    val body = (if (code in 200..299) connection.inputStream else connection.errorStream)
                        .bufferedReader().use { it.readText() }
                    if (code !in 200..299) error("HTTP $code")
                    val array = JSONArray(body)
                    val loaded = (0 until array.length()).map { index ->
                        val json = array.getJSONObject(index)
                        VaultRecord(
                            id = json.optString("id"),
                            store = json.optString("store"),
                            purchaseDate = json.optString("purchase_date"),
                            item = json.optString("item"),
                            serial = json.optString("serial_imei"),
                            imei = "",
                            warrantyExpires = json.optString("warranty_expiry"),
                        )
                    }
                    connection.disconnect()
                    android.os.Handler(android.os.Looper.getMainLooper()).post {
                        records.clear()
                        records.addAll(loaded)
                    }
                } catch (error: Exception) {
                    android.os.Handler(android.os.Looper.getMainLooper()).post {
                        message = String.format(Locale.getDefault(), t("vault_load_failed"), error.localizedMessage.orEmpty())
                        messageIsError = true
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) { loadRecords() }

    val captureLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { captured ->
        if (captured) {
            val uri = photoUri ?: return@rememberLauncherForActivityResult
            busy = true
            try {
                val input = InputImage.fromFilePath(context, uri)
                TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                    .process(input)
                    .addOnSuccessListener { result ->
                        val ocr = result.text
                        val serialMatch = Regex("(?i)(?:serial|s/?n|imei)\\s*[:#-]?\\s*([A-Z0-9-]{8,20})")
                            .find(ocr)?.groupValues?.getOrNull(1).orEmpty()
                        val imeiMatch = Regex("(?<!\\d)\\d{15}(?!\\d)").find(ocr)?.value.orEmpty()
                        if (serial.isBlank()) serial = serialMatch
                        if (imei.isBlank()) imei = imeiMatch
                        if (item.isBlank()) item = ocr.lineSequence().map(String::trim).firstOrNull { it.length in 3..80 }.orEmpty()
                        busy = false
                        message = if (ocr.isBlank()) t("vault_ocr_empty")
                        else t("vault_ocr_review")
                        messageIsError = ocr.isBlank()
                    }
                    .addOnFailureListener { error ->
                        busy = false
                        message = String.format(Locale.getDefault(), t("vault_ocr_failed"), error.localizedMessage.orEmpty())
                        messageIsError = true
                    }
            } catch (error: Exception) {
                busy = false
                message = String.format(Locale.getDefault(), t("vault_photo_failed"), error.localizedMessage.orEmpty())
                messageIsError = true
            }
        }
    }

    fun captureBill() {
        val file = File(context.cacheDir, "vault").apply { mkdirs() }
            .resolve("bill-${System.currentTimeMillis()}.jpg")
        photoUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        captureLauncher.launch(photoUri!!)
    }

    fun saveRecord() {
        val uri = photoUri
        val months = warrantyMonths.toIntOrNull()
        val date = runCatching { LocalDate.parse(purchaseDate) }.getOrNull()
        if (uri == null || date == null || months == null || months !in 0..120 || item.isBlank()) {
            message = t("vault_form_invalid")
            messageIsError = true
            return
        }
        val expiry = date.plusMonths(months.toLong()).toString()
        busy = true
        message = ""
        messageIsError = false
        withToken { token ->
            executor.execute {
                try {
                    val connection = URL("${BuildConfig.API_BASE_URL}/api/v1/vault/items").openConnection() as HttpURLConnection
                    val boundary = "FendlyVault${System.currentTimeMillis()}"
                    connection.requestMethod = "POST"
                    connection.doOutput = true
                    connection.connectTimeout = 20000
                    connection.readTimeout = 60000
                    connection.setRequestProperty("Authorization", "Bearer $token")
                    connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
                    connection.outputStream.buffered().use { output ->
                        fun field(name: String, value: String) {
                            output.write("--$boundary\r\nContent-Disposition: form-data; name=\"$name\"\r\n\r\n$value\r\n".toByteArray(StandardCharsets.UTF_8))
                        }
                        val name = "bill-${System.currentTimeMillis()}.jpg"
                            output.write("--$boundary\r\nContent-Disposition: form-data; name=\"file\"; filename=\"$name\"\r\nContent-Type: image/jpeg\r\n\r\n".toByteArray(StandardCharsets.UTF_8))
                        context.contentResolver.openInputStream(uri)?.use { it.copyTo(output) }
                            ?: error("The selected photo is no longer available")
                        output.write("\r\n".toByteArray(StandardCharsets.UTF_8))
                        field("store", store)
                        field("purchase_date", purchaseDate)
                        field("item", item)
                        field("serial_imei", listOf(serial, imei).filter(String::isNotBlank).joinToString(" / "))
                        field("warranty_months", months.toString())
                        field("warranty_expiry", expiry)
                        output.write("--$boundary--\r\n".toByteArray(StandardCharsets.UTF_8))
                    }
                    val code = connection.responseCode
                    val response = (if (code in 200..299) connection.inputStream else connection.errorStream)
                        .bufferedReader().use { it.readText() }
                    if (code !in 200..299) error("HTTP $code: $response")
                    connection.disconnect()
                    android.os.Handler(android.os.Looper.getMainLooper()).post {
                        busy = false
                        message = t("vault_saved")
                        messageIsError = false
                        photoUri = null
                        store = ""
                        purchaseDate = ""
                        item = ""
                        serial = ""
                        imei = ""
                        warrantyMonths = ""
                        loadRecords()
                    }
                } catch (error: Exception) {
                    android.os.Handler(android.os.Looper.getMainLooper()).post {
                        busy = false
                        message = String.format(Locale.getDefault(), t("vault_save_failed"), error.localizedMessage.orEmpty())
                        messageIsError = true
                    }
                }
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        t("vault_title"),
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 2,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Outlined.ArrowBack,
                            contentDescription = t("vault_back"),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                ),
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                FendlyCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(t("vault_add_heading"), style = MaterialTheme.typography.titleLarge)
                        Text(
                            t("vault_intro"),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        FendlyPrimaryButton(
                            text = if (photoUri == null) t("vault_scan_invoice") else t("vault_retake_photo"),
                            onClick = ::captureBill,
                            enabled = !busy,
                        )
                        if (photoUri != null) {
                            Text(
                                t("vault_photo_ready"),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                        FendlyTextField(
                            value = store,
                            onValueChange = { store = it },
                            label = t("vault_store"),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                        )
                        FendlyTextField(
                            value = purchaseDate,
                            onValueChange = { purchaseDate = it },
                            label = t("vault_purchase_date"),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                        )
                        FendlyTextField(
                            value = item,
                            onValueChange = { item = it },
                            label = t("vault_item"),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                        )
                        FendlyTextField(
                            value = serial,
                            onValueChange = { serial = it },
                            label = t("vault_serial"),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                        )
                        FendlyTextField(
                            value = imei,
                            onValueChange = { imei = it },
                            label = t("vault_imei"),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        )
                        FendlyTextField(
                            value = warrantyMonths,
                            onValueChange = { warrantyMonths = it.filter(Char::isDigit).take(3) },
                            label = t("vault_warranty_months"),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        )
                        FendlyPrimaryButton(
                            text = if (busy) t("vault_saving") else t("vault_save"),
                            onClick = ::saveRecord,
                            enabled = !busy && photoUri != null,
                        )
                        if (busy) {
                            CircularProgressIndicator(
                                modifier = Modifier.height(24.dp),
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 2.dp,
                            )
                        }
                        if (message.isNotBlank()) {
                            Text(
                                text = message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (messageIsError) MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            }
            item {
                Text(
                    t("vault_warranties"),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
            if (records.isEmpty()) {
                item {
                    FendlyCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            t("vault_empty"),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            } else {
                items(records, key = VaultRecord::id) { record ->
                    WarrantyCard(record, t, format)
                }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun WarrantyCard(
    record: VaultRecord,
    t: (String) -> String,
    format: (String, Long) -> String,
) {
    val daysRemaining = runCatching {
        ChronoUnit.DAYS.between(LocalDate.now(), LocalDate.parse(record.warrantyExpires))
    }.getOrNull()
    FendlyCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(record.item.ifBlank { t("vault_saved_item") }, style = MaterialTheme.typography.titleMedium)
            if (record.store.isNotBlank()) Text("${t("vault_store_prefix")}: ${record.store}")
            if (record.purchaseDate.isNotBlank()) Text("${t("vault_purchased_prefix")}: ${record.purchaseDate}")
            if (record.serial.isNotBlank()) Text("${t("vault_serial_prefix")}: ${record.serial}")
            if (record.imei.isNotBlank()) Text("${t("vault_imei")}: ${record.imei}")
            Text(
                when {
                    daysRemaining == null -> t("vault_expiry_unavailable")
                    daysRemaining < 0 -> format("vault_expired_days", -daysRemaining)
                    daysRemaining == 0L -> t("vault_expires_today")
                    else -> format("vault_remaining_days", daysRemaining)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = if (daysRemaining != null && daysRemaining < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            )
        }
    }
}
