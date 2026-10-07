package com.example.fendly

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
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
import java.util.concurrent.Executors

class VaultActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { VaultScreen(onBack = ::finish) } }
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

    fun withToken(onToken: (String) -> Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            message = "Sign in to use your Vault."
            return
        }
        user.getIdToken(false)
            .addOnSuccessListener { result -> onToken(result.token.orEmpty()) }
            .addOnFailureListener { error -> message = "Could not authenticate: ${error.localizedMessage}" }
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
                    if (code !in 200..299) error("Vault request failed ($code)")
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
                        message = "Could not load Vault: ${error.localizedMessage}"
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
                        message = if (ocr.isBlank()) "No text was detected. Enter the details manually."
                        else "Text scanned. Review and correct the details before saving."
                    }
                    .addOnFailureListener { error ->
                        busy = false
                        message = "OCR failed: ${error.localizedMessage}"
                    }
            } catch (error: Exception) {
                busy = false
                message = "Could not read the photo: ${error.localizedMessage}"
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
            message = "Add a photo, item name, valid purchase date (YYYY-MM-DD), and warranty period (0–120 months)."
            return
        }
        val expiry = date.plusMonths(months.toLong()).toString()
        busy = true
        message = ""
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
                    if (code !in 200..299) error("Vault upload failed ($code): $response")
                    connection.disconnect()
                    android.os.Handler(android.os.Looper.getMainLooper()).post {
                        busy = false
                        message = "Bill saved securely."
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
                        message = "Could not save bill: ${error.localizedMessage}"
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Digital Bill Locker") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Back") } },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Add a bill or serial sticker", style = MaterialTheme.typography.titleMedium)
                        Text("The image is encrypted by Fendly before it is stored. OCR runs on this device; review the detected fields before saving.")
                        Button(onClick = ::captureBill, enabled = !busy, modifier = Modifier.fillMaxWidth()) {
                            Text(if (photoUri == null) "Take invoice / serial photo" else "Retake photo")
                        }
                        if (photoUri != null) Text("Photo ready for upload")
                        OutlinedTextField(store, { store = it }, label = { Text("Store") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                        OutlinedTextField(purchaseDate, { purchaseDate = it }, label = { Text("Purchase date (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                        OutlinedTextField(item, { item = it }, label = { Text("Item") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                        OutlinedTextField(serial, { serial = it }, label = { Text("Serial number") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                        OutlinedTextField(imei, { imei = it }, label = { Text("IMEI (if applicable)") }, modifier = Modifier.fillMaxWidth(), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                        OutlinedTextField(warrantyMonths, { warrantyMonths = it.filter(Char::isDigit).take(3) }, label = { Text("Warranty period (months)") }, modifier = Modifier.fillMaxWidth(), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                        Button(onClick = ::saveRecord, enabled = !busy && photoUri != null, modifier = Modifier.fillMaxWidth()) {
                            if (busy) CircularProgressIndicator()
                            else Text("Save to Vault")
                        }
                        if (message.isNotBlank()) Text(message, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
            item { Text("Your warranties", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 8.dp)) }
            if (records.isEmpty()) {
                item { Text("No bills saved yet.") }
            } else {
                items(records, key = VaultRecord::id) { record ->
                    WarrantyCard(record)
                }
            }
            item { Spacer(Modifier.height(20.dp)) }
        }
    }
}

@Composable
private fun WarrantyCard(record: VaultRecord) {
    val daysRemaining = runCatching {
        ChronoUnit.DAYS.between(LocalDate.now(), LocalDate.parse(record.warrantyExpires))
    }.getOrNull()
    Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(record.item.ifBlank { "Saved item" }, style = MaterialTheme.typography.titleMedium)
            if (record.store.isNotBlank()) Text("Store: ${record.store}")
            if (record.purchaseDate.isNotBlank()) Text("Purchased: ${record.purchaseDate}")
            if (record.serial.isNotBlank()) Text("Serial: ${record.serial}")
            if (record.imei.isNotBlank()) Text("IMEI: ${record.imei}")
            Text(
                when {
                    daysRemaining == null -> "Warranty expiration date unavailable"
                    daysRemaining < 0 -> "Warranty expired ${-daysRemaining} days ago"
                    daysRemaining == 0L -> "Warranty expires today"
                    else -> "Warranty: $daysRemaining days remaining"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = if (daysRemaining != null && daysRemaining < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            )
        }
    }
}
