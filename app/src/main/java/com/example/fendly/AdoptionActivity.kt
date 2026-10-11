package com.example.fendly

import android.Manifest
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.LruCache
import android.view.Surface
import java.io.File
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.core.content.ContextCompat
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import com.google.firebase.auth.FirebaseAuth
import com.example.fendly.ui.theme.FendlyCard
import com.example.fendly.ui.theme.FendlyPrimaryButton
import com.example.fendly.ui.theme.FendlyTextField
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import java.util.Locale
import java.util.concurrent.Executors


class AdoptionActivity : ComponentActivity() {
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
                AdoptionScreen(onBack = ::finish)
            }
        }
    }
}

private data class AdoptionListing(
    val id: String,
    val petName: String,
    val species: String,
    val breed: String,
    val ageMonths: Int,
    val gender: String,
    val healthStatus: String,
    val description: String,
    val city: String,
    val image: String,
    val imageCount: Int,
    val status: String,
    val socialStatus: String,
    val socialError: String,
)

private data class AdoptionPhoto(
    val uri: Uri? = null,
    val bitmap: Bitmap? = null,
)

private data class AdoptionApplicationView(
    val id: String,
    val homeType: String,
    val experience: String,
    val phone: String,
    val status: String,
)

private data class MyAdoptionApplication(
    val id: String,
    val petName: String,
    val status: String,
    val listerPhone: String,
)

private fun adoptionListing(json: JSONObject) = AdoptionListing(
    id = json.optString("id"),
    petName = json.optString("pet_name"),
    species = json.optString("species"),
    breed = json.optString("breed"),
    ageMonths = json.optInt("age_months"),
    gender = json.optString("gender"),
    healthStatus = json.optString("health_status"),
    description = json.optString("description"),
    city = json.optString("location_city"),
    image = json.optJSONArray("images")?.optString(0).orEmpty(),
    imageCount = json.optJSONArray("images")?.length() ?: 0,
    status = json.optString("status"),
    socialStatus = json.optString("social_post_status"),
    socialError = json.optString("social_post_error"),
)

private fun adoptionRequest(
    method: String,
    path: String,
    token: String? = null,
    body: String? = null,
    contentType: String = "application/json",
): String {
    val connection = URL("${BuildConfig.API_BASE_URL}$path").openConnection() as HttpURLConnection
    try {
        connection.requestMethod = method
        connection.connectTimeout = 15000
        connection.readTimeout = 30000
        if (token != null) connection.setRequestProperty("Authorization", "Bearer $token")
        if (body != null) {
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", contentType)
            connection.outputStream.use { it.write(body.toByteArray(StandardCharsets.UTF_8)) }
        }
        val code = connection.responseCode
        val response = (if (code in 200..299) connection.inputStream else connection.errorStream)
            ?.bufferedReader()?.use { it.readText() }.orEmpty()
        if (code !in 200..299) {
            val detail = runCatching { JSONObject(response).optString("detail") }.getOrDefault("")
            error(detail.ifBlank { "Request failed (HTTP $code)" })
        }
        return response
    } finally {
        connection.disconnect()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdoptionScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    fun t(key: String): String = LanguageManager.adoptionText(context, key)
    val scope = rememberCoroutineScope()
    val feed = remember { mutableStateListOf<AdoptionListing>() }
    val mine = remember { mutableStateListOf<AdoptionListing>() }
    val myApplications = remember { mutableStateListOf<MyAdoptionApplication>() }
    val reviewApplications = remember { mutableStateListOf<AdoptionApplicationView>() }
    var reviewListing by remember { mutableStateOf("") }
    var reviewLoadingListing by remember { mutableStateOf("") }
    var expandedListing by remember { mutableStateOf("") }
    var editingListing by remember { mutableStateOf<AdoptionListing?>(null) }
    var deletingListing by remember { mutableStateOf<AdoptionListing?>(null) }
    var tab by remember { mutableStateOf(0) }
    var cityFilter by remember { mutableStateOf("") }
    var speciesFilter by remember { mutableStateOf("") }
    var minimumAge by remember { mutableStateOf("") }
    var maximumAge by remember { mutableStateOf("") }
    var showCreate by remember { mutableStateOf(false) }
    var applyingTo by remember { mutableStateOf<AdoptionListing?>(null) }
    var petName by remember { mutableStateOf("") }
    var species by remember { mutableStateOf("") }
    var breed by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var health by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var autoPostSocial by remember { mutableStateOf(false) }
    val selectedPhotos = remember {
        mutableStateListOf<AdoptionPhoto?>(null, null, null)
    }
    var selectedPhotoSlot by remember { mutableStateOf(-1) }
    var showPhotoSource by remember { mutableStateOf(false) }
    var showCameraCapture by remember { mutableStateOf(false) }
    var homeType by remember { mutableStateOf("") }
    var petExperience by remember { mutableStateOf("") }
    var contactPhone by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    val speciesFilterFocus = remember { FocusRequester() }
    val cityFilterFocus = remember { FocusRequester() }
    val minimumAgeFilterFocus = remember { FocusRequester() }
    val maximumAgeFilterFocus = remember { FocusRequester() }

    fun withToken(onFailure: (() -> Unit)? = null, onToken: (String) -> Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            message = t("adopt_sign_in")
            onFailure?.invoke()
            return
        }
        user.getIdToken(false)
            .addOnSuccessListener { result -> onToken(result.token.orEmpty()) }
            .addOnFailureListener {
                message = t("adopt_auth_failed")
                onFailure?.invoke()
            }
    }

    fun refreshFeed() {
        busy = true
        scope.launch {
            try {
                val query = buildList {
                    if (speciesFilter.isNotBlank()) add("species=${Uri.encode(speciesFilter.trim())}")
                    if (cityFilter.isNotBlank()) add("city=${Uri.encode(cityFilter.trim())}")
                    minimumAge.toIntOrNull()?.let { add("min_age_months=$it") }
                    maximumAge.toIntOrNull()?.let { add("max_age_months=$it") }
                }.joinToString("&").let { if (it.isBlank()) "" else "?$it" }
                val result = withContext(Dispatchers.IO) {
                    JSONArray(adoptionRequest("GET", "/api/adoptions$query"))
                }
                feed.clear()
                for (index in 0 until result.length()) feed.add(adoptionListing(result.getJSONObject(index)))
                message = ""
            } catch (error: Exception) {
                message = t("adopt_load_feed_failed")
            } finally {
                busy = false
            }
        }
    }

    fun loadMine() {
        withToken { token ->
            scope.launch {
                try {
                    val result = withContext(Dispatchers.IO) {
                        JSONArray(adoptionRequest("GET", "/api/adoptions/mine", token))
                    }
                    mine.clear()
                    for (index in 0 until result.length()) mine.add(adoptionListing(result.getJSONObject(index)))
                    feed.removeAll { listing -> mine.any { it.id == listing.id } }
                    message = ""
                } catch (error: Exception) {
                    message = t("adopt_load_listings_failed")
                }
            }
        }
    }

    fun loadMyApplications() {
        withToken { token ->
            scope.launch {
                try {
                    val result = withContext(Dispatchers.IO) {
                        JSONArray(adoptionRequest("GET", "/api/adoptions/applications/mine", token))
                    }
                    myApplications.clear()
                    for (index in 0 until result.length()) {
                        val value = result.getJSONObject(index)
                        myApplications.add(
                            MyAdoptionApplication(
                                id = value.optString("id"),
                                petName = value.optString("pet_name"),
                                status = value.optString("status"),
                                listerPhone = value.optString("lister_contact_phone"),
                            )
                        )
                    }
                    message = ""
                } catch (error: Exception) {
                    message = t("adopt_load_applications_failed")
                }
            }
        }
    }

    fun loadApplications(listing: AdoptionListing) {
        reviewListing = listing.id
        reviewLoadingListing = listing.id
        reviewApplications.clear()
        withToken(
            onFailure = { reviewLoadingListing = "" },
            onToken = { token ->
            scope.launch {
                try {
                    val result = withContext(Dispatchers.IO) {
                        JSONArray(adoptionRequest("GET", "/api/adoptions/${listing.id}/applications", token))
                    }
                    reviewApplications.clear()
                    for (index in 0 until result.length()) {
                        val value = result.getJSONObject(index)
                        reviewApplications.add(
                            AdoptionApplicationView(
                                id = value.optString("id"),
                                homeType = value.optString("home_type"),
                                experience = value.optString("pet_experience"),
                                phone = value.optString("contact_phone"),
                                status = value.optString("status"),
                            )
                        )
                    }
                    message = ""
                } catch (error: Exception) {
                    message = t("adopt_load_reviews_failed")
                } finally {
                    if (reviewLoadingListing == listing.id) reviewLoadingListing = ""
                }
            }
            },
        )
    }

    fun updateListing(listing: AdoptionListing) {
        withToken { token ->
            scope.launch {
                try {
                    val body = JSONObject().apply {
                        put("pet_name", petName.trim())
                        put("species", species.trim())
                        put("breed", breed.trim())
                        put("age_months", age.toInt())
                        put("gender", gender)
                        put("health_status", health.trim())
                        put("description", description.trim())
                        put("location_city", city.trim())
                    }.toString()
                    withContext(Dispatchers.IO) {
                        adoptionRequest(
                            "PUT",
                            "/api/adoptions/${listing.id}",
                            token,
                            body,
                            "application/json",
                        )
                    }
                    editingListing = null
                    message = t("adopt_listing_updated")
                    loadMine()
                    refreshFeed()
                } catch (error: Exception) {
                    message = t("adopt_update_failed")
                }
            }
        }
    }

    fun deleteListing(listing: AdoptionListing) {
        withToken { token ->
            scope.launch {
                try {
                    withContext(Dispatchers.IO) {
                        adoptionRequest("DELETE", "/api/adoptions/${listing.id}", token)
                    }
                    deletingListing = null
                    reviewListing = ""
                    reviewApplications.clear()
                    mine.removeAll { it.id == listing.id }
                    feed.removeAll { it.id == listing.id }
                    message = t("adopt_listing_deleted")
                } catch (error: Exception) {
                    message = t("adopt_delete_failed")
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        refreshFeed()
        loadMine()
    }
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null && selectedPhotoSlot in selectedPhotos.indices) {
            selectedPhotos[selectedPhotoSlot] = AdoptionPhoto(uri = uri)
        }
    }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            showCameraCapture = true
        } else {
            message = t("adopt_camera_permission")
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        t("adopt_title"),
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 1,
                        textAlign = TextAlign.Center,
                    )
                },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text(t("adopt_back")) }
                },
                actions = {
                    TextButton(onClick = {
                        showCreate = true
                        message = ""
                    }) { Text(t("adopt_list_pet")) }
                },
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            TabRow(selectedTabIndex = tab) {
                listOf(t("adopt_browse"), t("adopt_my_listings"), t("adopt_my_applications"))
                    .forEachIndexed { index, title ->
                    Tab(selected = tab == index, onClick = {
                        tab = index
                        when (index) {
                            0 -> {
                                refreshFeed()
                                loadMine()
                            }
                            1 -> loadMine()
                            else -> loadMyApplications()
                        }
                    }, text = { Text(title) })
                }
            }
            if (message.isNotBlank()) {
                Text(message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
            if (busy) CircularProgressIndicator(Modifier.size(28.dp))
            when (tab) {
                0 -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FendlyTextField(
                            value = speciesFilter,
                            onValueChange = { speciesFilter = it },
                            label = t("adopt_species"),
                            modifier = Modifier.weight(1f).focusRequester(speciesFilterFocus),
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Words,
                                imeAction = ImeAction.Next,
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { cityFilterFocus.requestFocus() },
                            ),
                            singleLine = true,
                        )
                        FendlyTextField(
                            value = cityFilter,
                            onValueChange = { cityFilter = it },
                            label = t("adopt_city"),
                            modifier = Modifier.weight(1f).focusRequester(cityFilterFocus),
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Words,
                                imeAction = ImeAction.Next,
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { minimumAgeFilterFocus.requestFocus() },
                            ),
                            singleLine = true,
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FendlyTextField(
                            value = minimumAge,
                            onValueChange = { minimumAge = it.filter(Char::isDigit).take(3) },
                            label = t("adopt_min_age"),
                            modifier = Modifier.weight(1f).focusRequester(minimumAgeFilterFocus),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next,
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { maximumAgeFilterFocus.requestFocus() },
                            ),
                            singleLine = true,
                        )
                        FendlyTextField(
                            value = maximumAge,
                            onValueChange = { maximumAge = it.filter(Char::isDigit).take(3) },
                            label = t("adopt_max_age"),
                            modifier = Modifier.weight(1f).focusRequester(maximumAgeFilterFocus),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done,
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = { focusManager.clearFocus() },
                            ),
                            singleLine = true,
                        )
                    }
                    TextButton(onClick = ::refreshFeed) { Text(t("adopt_refresh")) }
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(feed.filterNot { listing -> mine.any { it.id == listing.id } }, key = AdoptionListing::id) { listing ->
                            ListingCard(
                                listing = listing,
                                t = ::t,
                                actionLabel = t("adopt_request"),
                            ) {
                                if (listing.status !in setOf("available", "pending")) {
                                    message = t("adopt_not_accepting")
                                } else {
                                    applyingTo = listing
                                }
                            }
                        }
                    }
                }
                1 -> LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    if (mine.isEmpty()) {
                        item { Text(t("adopt_no_listings")) }
                    }
                    items(mine, key = AdoptionListing::id) { listing ->
                        ListingCard(
                            listing = listing,
                            t = ::t,
                            actionLabel = t("adopt_review"),
                            expanded = expandedListing == listing.id,
                            onToggleExpand = {
                                expandedListing =
                                    if (expandedListing == listing.id) "" else listing.id
                            },
                            ownerActions = {
                                TextButton(onClick = {
                                    petName = listing.petName
                                    species = listing.species
                                    breed = listing.breed
                                    age = listing.ageMonths.toString()
                                    gender = listing.gender
                                    health = listing.healthStatus
                                    description = listing.description
                                    city = listing.city
                                    editingListing = listing
                                }) { Text(t("adopt_edit")) }
                                TextButton(onClick = { deletingListing = listing }) {
                                    Text(t("adopt_delete"), color = MaterialTheme.colorScheme.error)
                                }
                            },
                        ) {
                            if (reviewListing == listing.id) {
                                reviewListing = ""
                                reviewApplications.clear()
                            } else {
                                loadApplications(listing)
                            }
                        }
                        if (listing.id == reviewListing) {
                            when {
                                reviewLoadingListing == listing.id -> {
                                    CircularProgressIndicator(Modifier.size(24.dp))
                                }
                                reviewApplications.isEmpty() -> {
                                    Text(
                                        t("adopt_no_applications"),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                            reviewApplications.forEach { application ->
                                ApplicationReviewCard(
                                    application = application,
                                    t = ::t,
                                    onDecision = { decision ->
                                        withToken { token ->
                                            scope.launch {
                                                try {
                                                    val form = "decision=$decision"
                                                    withContext(Dispatchers.IO) {
                                                        adoptionRequest(
                                                            "POST",
                                                            "/api/adoptions/applications/${application.id}/decision",
                                                            token,
                                                            form,
                                                            "application/x-www-form-urlencoded",
                                                        )
                                                    }
                                                    loadApplications(listing)
                                                    loadMine()
                                                } catch (error: Exception) {
                                                    message = t("adopt_decision_failed")
                                                }
                                            }
                                        }
                                    },
                                )
                            }
                        }
                    }
                }
                else -> Column(Modifier.weight(1f)) {
                    Text(
                        t("adopt_applications_explainer"),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                    if (myApplications.isEmpty()) {
                        Text(t("adopt_no_my_applications"))
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(myApplications, key = MyAdoptionApplication::id) { application ->
                                FendlyCard(Modifier.fillMaxWidth()) {
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(application.petName, fontWeight = FontWeight.Bold)
                                        Text("${t("adopt_status")}: ${t("adopt_${application.status}")}")
                                        if (application.status == "approved") {
                                            Text(
                                                if (application.listerPhone.isBlank()) {
                                                    t("adopt_no_verified_phone")
                                                } else {
                                                    t("adopt_contact_lister").replace(
                                                        "{phone}",
                                                        application.listerPhone,
                                                    )
                                                },
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreate) {
        val fields = rememberScrollState()
        val petNameFocus = remember { FocusRequester() }
        val speciesFocus = remember { FocusRequester() }
        val breedFocus = remember { FocusRequester() }
        val ageFocus = remember { FocusRequester() }
        val healthFocus = remember { FocusRequester() }
        val descriptionFocus = remember { FocusRequester() }
        val cityFocus = remember { FocusRequester() }
        Dialog(onDismissRequest = { showCreate = false }) {
            FendlyCard(Modifier.fillMaxWidth()) {
                Column(
                    Modifier.fillMaxWidth().verticalScroll(fields),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(t("adopt_create_title"), style = MaterialTheme.typography.titleLarge)
                    FendlyTextField(
                        value = petName,
                        onValueChange = { petName = it },
                        label = t("adopt_pet_name"),
                        modifier = Modifier.fillMaxWidth().focusRequester(petNameFocus),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Next,
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { speciesFocus.requestFocus() },
                        ),
                        singleLine = true,
                    )
                    FendlyTextField(
                        value = species,
                        onValueChange = { species = it },
                        label = t("adopt_species"),
                        modifier = Modifier.fillMaxWidth().focusRequester(speciesFocus),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Next,
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { breedFocus.requestFocus() },
                        ),
                        singleLine = true,
                    )
                    FendlyTextField(
                        value = breed,
                        onValueChange = { breed = it },
                        label = t("adopt_breed_optional"),
                        modifier = Modifier.fillMaxWidth().focusRequester(breedFocus),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Next,
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { ageFocus.requestFocus() },
                        ),
                        singleLine = true,
                    )
                    FendlyTextField(
                        value = age,
                        onValueChange = { age = it.filter(Char::isDigit).take(3) },
                        label = t("adopt_age_months"),
                        modifier = Modifier.fillMaxWidth().focusRequester(ageFocus),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next,
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { healthFocus.requestFocus() },
                        ),
                        singleLine = true,
                    )
                    Text(t("adopt_gender"), style = MaterialTheme.typography.labelLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = gender == "Male",
                            onClick = { gender = "Male" },
                            label = { Text(t("adopt_male")) },
                        )
                        FilterChip(
                            selected = gender == "Female",
                            onClick = { gender = "Female" },
                            label = { Text(t("adopt_female")) },
                        )
                    }
                    FendlyTextField(
                        value = health,
                        onValueChange = { health = it },
                        label = t("adopt_health"),
                        modifier = Modifier.fillMaxWidth().focusRequester(healthFocus),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Next,
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { descriptionFocus.requestFocus() },
                        ),
                    )
                    FendlyTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = t("adopt_description"),
                        modifier = Modifier.fillMaxWidth().focusRequester(descriptionFocus),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Next,
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { cityFocus.requestFocus() },
                        ),
                    )
                    FendlyTextField(
                        value = city,
                        onValueChange = { city = it },
                        label = t("adopt_city"),
                        modifier = Modifier.fillMaxWidth().focusRequester(cityFocus),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Done,
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { focusManager.clearFocus() },
                        ),
                        singleLine = true,
                    )
                    Text(t("adopt_photos"), style = MaterialTheme.typography.labelLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        selectedPhotos.forEachIndexed { index, photo ->
                            AdoptionPhotoSlot(
                                photo = photo,
                                label = t("adopt_add_photo"),
                                removeLabel = t("adopt_remove_photo"),
                                onSelect = {
                                    selectedPhotoSlot = index
                                    showPhotoSource = true
                                },
                                onRemove = { selectedPhotos[index] = null },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(autoPostSocial, { autoPostSocial = it })
                        Text(
                            t("adopt_social_optin"),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    Text(
                        t("adopt_social_optional"),
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                        TextButton(onClick = { showCreate = false }) { Text(t("adopt_cancel")) }
                        FendlyPrimaryButton(
                            text = t("adopt_publish"),
                            enabled = !busy,
                            onClick = {
                                val ageValue = age.toIntOrNull()
                                val photos = selectedPhotos.filterNotNull()
                                if (
                                    petName.isBlank() || species.isBlank() || ageValue == null ||
                                    gender.isBlank() || health.isBlank() || description.isBlank() ||
                                    city.isBlank() || photos.isEmpty()
                                ) {
                                    message = t("adopt_required_fields")
                                } else {
                                    withToken { token ->
                                        busy = true
                                        scope.launch {
                                            try {
                                                val result = withContext(Dispatchers.IO) {
                                                    uploadAdoptionListing(
                                                        context,
                                                        token,
                                                        photos,
                                                        petName,
                                                        species,
                                                        breed,
                                                        ageValue,
                                                        gender,
                                                        health,
                                                        description,
                                                        city,
                                                        autoPostSocial,
                                                    )
                                                }
                                                val listing = adoptionListing(JSONObject(result))
                                                message = when (listing.socialStatus) {
                                                    "failed" -> "${t("adopt_social_attention")} ${listing.socialError}"
                                                    "published" -> t("adopt_shared")
                                                    else -> t("adopt_listing_published")
                                                }
                                                showCreate = false
                                                tab = 1
                                                loadMine()
                                                refreshFeed()
                                            } catch (error: Exception) {
                                                message = t("adopt_submit_failed")
                                            } finally {
                                                busy = false
                                            }
                                        }
                                    }
                                }
                            },
                        )
                    }
                }
                if (showPhotoSource) {
                    AlertDialog(
                        onDismissRequest = { showPhotoSource = false },
                        title = { Text(t("adopt_add_photo")) },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    showPhotoSource = false
                                    photoPicker.launch("image/*")
                                },
                            ) { Text(t("adopt_choose_gallery")) }
                        },
                        dismissButton = {
                            Row {
                                TextButton(
                                    onClick = {
                                        showPhotoSource = false
                                        if (ContextCompat.checkSelfPermission(
                                                context,
                                                Manifest.permission.CAMERA,
                                            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                                        ) {
                                            showCameraCapture = true
                                        } else {
                                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                        }
                                    },
                                ) { Text(t("adopt_take_photo")) }
                                TextButton(onClick = { showPhotoSource = false }) {
                                    Text(t("adopt_cancel"))
                                }
                            }
                        },
                    )
                }
            }
        }
    }
    if (showCameraCapture) {
        AdoptionCameraDialog(
            t = ::t,
            onCapture = { bitmap ->
                if (selectedPhotoSlot in selectedPhotos.indices) {
                    selectedPhotos[selectedPhotoSlot] = AdoptionPhoto(bitmap = bitmap)
                }
                showCameraCapture = false
            },
            onDismiss = { showCameraCapture = false },
        )
    }

    editingListing?.let { listing ->
        AlertDialog(
            onDismissRequest = { editingListing = null },
            title = { Text(t("adopt_edit_listing")) },
            text = {
                Column(
                    Modifier.fillMaxWidth().height(440.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FendlyTextField(petName, { petName = it }, t("adopt_pet_name"), singleLine = true)
                    FendlyTextField(species, { species = it }, t("adopt_species"), singleLine = true)
                    FendlyTextField(breed, { breed = it }, t("adopt_breed_optional"), singleLine = true)
                    FendlyTextField(
                        age,
                        { age = it.filter(Char::isDigit).take(3) },
                        t("adopt_age_months"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                    )
                    Text(t("adopt_gender"), style = MaterialTheme.typography.labelLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = gender == "Male",
                            onClick = { gender = "Male" },
                            label = { Text(t("adopt_male")) },
                        )
                        FilterChip(
                            selected = gender == "Female",
                            onClick = { gender = "Female" },
                            label = { Text(t("adopt_female")) },
                        )
                    }
                    FendlyTextField(health, { health = it }, t("adopt_health"))
                    FendlyTextField(description, { description = it }, t("adopt_description"))
                    FendlyTextField(city, { city = it }, t("adopt_city"), singleLine = true)
                    if (listing.imageCount > 0) {
                        Text(t("adopt_edit_photos_note"))
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !busy &&
                        petName.isNotBlank() && species.isNotBlank() &&
                        age.toIntOrNull() != null && gender.isNotBlank() &&
                        health.isNotBlank() && description.isNotBlank() && city.isNotBlank(),
                    onClick = { updateListing(listing) },
                ) { Text(t("adopt_save_changes")) }
            },
            dismissButton = {
                TextButton(onClick = { editingListing = null }) { Text(t("adopt_cancel")) }
            },
        )
    }

    deletingListing?.let { listing ->
        AlertDialog(
            onDismissRequest = { deletingListing = null },
            title = { Text(t("adopt_delete_title")) },
            text = { Text(t("adopt_delete_confirmation").replace("{pet}", listing.petName)) },
            confirmButton = {
                TextButton(
                    enabled = !busy,
                    onClick = { deleteListing(listing) },
                ) { Text(t("adopt_delete"), color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { deletingListing = null }) { Text(t("adopt_cancel")) }
            },
        )
    }

    applyingTo?.let { listing ->
        val homeTypeFocus = remember { FocusRequester() }
        val experienceFocus = remember { FocusRequester() }
        val contactPhoneFocus = remember { FocusRequester() }
        AlertDialog(
            onDismissRequest = { applyingTo = null },
            title = {
                Text(
                    t("adopt_request_title").replace("{pet}", listing.petName),
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FendlyTextField(
                        value = homeType,
                        onValueChange = { homeType = it },
                        label = t("adopt_home_type"),
                        modifier = Modifier.fillMaxWidth().focusRequester(homeTypeFocus),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Next,
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { experienceFocus.requestFocus() },
                        ),
                        singleLine = true,
                    )
                    FendlyTextField(
                        value = petExperience,
                        onValueChange = { petExperience = it },
                        label = t("adopt_experience"),
                        modifier = Modifier.fillMaxWidth().focusRequester(experienceFocus),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Next,
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { contactPhoneFocus.requestFocus() },
                        ),
                    )
                    FendlyTextField(
                        value = contactPhone,
                        onValueChange = { contactPhone = it },
                        label = t("adopt_your_phone"),
                        modifier = Modifier.fillMaxWidth().focusRequester(contactPhoneFocus),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Phone,
                            imeAction = ImeAction.Done,
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { focusManager.clearFocus() },
                        ),
                        singleLine = true,
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (homeType.isBlank() || petExperience.isBlank() || contactPhone.isBlank()) {
                        message = t("adopt_complete_application")
                    } else {
                        withToken { token ->
                            scope.launch {
                                try {
                                    val form = listOf(
                                        "home_type" to homeType,
                                        "pet_experience" to petExperience,
                                        "contact_phone" to contactPhone,
                                    ).joinToString("&") { (key, value) ->
                                        "${Uri.encode(key)}=${Uri.encode(value)}"
                                    }
                                    withContext(Dispatchers.IO) {
                                        adoptionRequest(
                                            "POST",
                                            "/api/adoptions/${listing.id}/apply",
                                            token,
                                            form,
                                            "application/x-www-form-urlencoded",
                                        )
                                    }
                                    message = t("adopt_request_sent")
                                    applyingTo = null
                                    homeType = ""
                                    petExperience = ""
                                    contactPhone = ""
                                    tab = 2
                                    loadMyApplications()
                                    refreshFeed()
                                } catch (error: Exception) {
                                    message = t("adopt_apply_failed")
                                }
                            }
                        }
                    }
                }) { Text(t("adopt_send_request")) }
            },
            dismissButton = {
                TextButton(onClick = { applyingTo = null }) { Text(t("adopt_cancel")) }
            },
        )
    }
}

@Composable
private fun ListingCard(
    listing: AdoptionListing,
    t: (String) -> String,
    actionLabel: String,
    expanded: Boolean = true,
    onToggleExpand: () -> Unit = {},
    ownerActions: (@Composable () -> Unit)? = null,
    onAction: () -> Unit,
) {
    FendlyCard(Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(listing.petName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("${t("adopt_status")}: ${t("adopt_${listing.status}")}")
                }
                if (ownerActions != null) {
                    TextButton(onClick = onToggleExpand) {
                        Text(t(if (expanded) "adopt_collapse" else "adopt_expand"))
                    }
                }
            }
            if (expanded) {
                if (listing.image.isNotBlank()) {
                    AdoptionImage(listing.image, t("adopt_photo_content_description"))
                }
                Text("${listing.species.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }}${if (listing.breed.isBlank()) "" else " · ${listing.breed}"}")
                Text("${listing.ageMonths} ${t("adopt_months")} · ${listing.gender} · ${listing.city}")
                Text("${t("adopt_health_label")}: ${listing.healthStatus}")
                Text(listing.description)
                if (listing.imageCount > 1) {
                    Text(t("adopt_more_photos").replace("{count}", (listing.imageCount - 1).toString()))
                }
                if (listing.socialStatus.isNotBlank()) {
                    Text(
                        if (listing.socialStatus == "failed") {
                            "${t("adopt_social_failed")} ${listing.socialError}"
                        } else {
                            t("adopt_social_status").replace(
                                "{status}",
                                t("adopt_social_${listing.socialStatus}"),
                            )
                        },
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
            if (expanded || ownerActions == null) {
                FendlyPrimaryButton(text = actionLabel, onClick = onAction)
                if (ownerActions != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        ownerActions()
                    }
                }
            }
        }
    }
}

@Composable
private fun AdoptionCameraDialog(
    t: (String) -> String,
    onCapture: (Bitmap) -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember(context) {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
            implementationMode = PreviewView.ImplementationMode.PERFORMANCE
        }
    }
    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
    }
    val executor = remember { Executors.newSingleThreadExecutor() }
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    var cameraPreview by remember { mutableStateOf<Preview?>(null) }
    var camera by remember { mutableStateOf<androidx.camera.core.Camera?>(null) }
    var cameraReady by remember { mutableStateOf(false) }
    var cameraError by remember { mutableStateOf(false) }

    LaunchedEffect(previewView, lifecycleOwner) {
        val providerFuture = ProcessCameraProvider.getInstance(context)
        providerFuture.addListener(
            {
                try {
                    val provider = providerFuture.get()
                    val selector = if (provider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA)) {
                        CameraSelector.DEFAULT_BACK_CAMERA
                    } else {
                        CameraSelector.DEFAULT_FRONT_CAMERA
                    }
                    val preview = Preview.Builder().build().also {
                        it.surfaceProvider = previewView.surfaceProvider
                    }
                    previewView.post {
                        try {
                            val boundCamera = provider.bindToLifecycle(
                                lifecycleOwner,
                                selector,
                                preview,
                                imageCapture,
                            )
                            cameraProvider = provider
                            cameraPreview = preview
                            camera = boundCamera
                        } catch (exception: Exception) {
                            Log.e("AdoptionCamera", "Could not bind camera preview", exception)
                            cameraError = true
                        }
                    }
                } catch (exception: Exception) {
                    Log.e("AdoptionCamera", "Could not initialize camera provider", exception)
                    cameraError = true
                }
            },
            ContextCompat.getMainExecutor(context),
        )
    }

    DisposableEffect(camera, lifecycleOwner) {
        val boundCamera = camera
        val state = boundCamera?.cameraInfo?.cameraState
        val observer = androidx.lifecycle.Observer<androidx.camera.core.CameraState> { cameraState ->
            cameraReady = cameraState.type == androidx.camera.core.CameraState.Type.OPEN
            cameraState.error?.let { error ->
                Log.e("AdoptionCamera", "Camera entered an error state", error.cause)
                cameraError = true
            }
        }
        state?.observe(lifecycleOwner, observer)
        onDispose {
            state?.removeObserver(observer)
        }
    }

    DisposableEffect(cameraProvider, cameraPreview, imageCapture) {
        onDispose {
            val preview = cameraPreview
            if (preview != null) {
                cameraProvider?.unbind(preview, imageCapture)
            }
        }
    }
    DisposableEffect(executor) {
        onDispose {
            executor.shutdown()
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        FendlyCard(Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(t("adopt_take_photo"), style = MaterialTheme.typography.titleLarge)
                AndroidView(
                    factory = { previewView },
                    modifier = Modifier.fillMaxWidth().height(360.dp),
                )
                if (cameraError) {
                    Text(
                        t("adopt_camera_unavailable"),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                FendlyPrimaryButton(
                    text = t("adopt_capture"),
                    enabled = cameraReady && !cameraError,
                    onClick = {
                        val outputFile = try {
                            File.createTempFile("adoption-photo-", ".jpg", context.cacheDir)
                        } catch (exception: Exception) {
                            Log.e("AdoptionCamera", "Could not create a temporary photo file", exception)
                            cameraError = true
                            return@FendlyPrimaryButton
                        }
                        imageCapture.targetRotation = previewView.display?.rotation
                            ?: Surface.ROTATION_0
                        val outputOptions = ImageCapture.OutputFileOptions.Builder(outputFile).build()
                        imageCapture.takePicture(
                            outputOptions,
                            executor,
                            object : ImageCapture.OnImageSavedCallback {
                                override fun onImageSaved(
                                    outputFileResults: ImageCapture.OutputFileResults,
                                ) {
                                    val bitmap = BitmapFactory.decodeFile(outputFile.absolutePath)
                                    outputFile.delete()
                                    ContextCompat.getMainExecutor(context).execute {
                                        if (bitmap == null) {
                                            Log.e("AdoptionCamera", "Camera saved an unreadable JPEG")
                                            cameraError = true
                                        } else {
                                            onCapture(bitmap)
                                        }
                                    }
                                }

                                override fun onError(exception: ImageCaptureException) {
                                    outputFile.delete()
                                    Log.e("AdoptionCamera", "Could not capture adoption photo", exception)
                                    ContextCompat.getMainExecutor(context).execute {
                                        cameraError = true
                                    }
                                }
                            },
                        )
                    },
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onDismiss) { Text(t("adopt_cancel")) }
                }
            }
        }
    }
}

@Composable
private fun AdoptionPhotoSlot(
    photo: AdoptionPhoto?,
    label: String,
    removeLabel: String,
    onSelect: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val bitmap by produceState<Bitmap?>(initialValue = photo?.bitmap, photo) {
        value = photo?.bitmap ?: photo?.uri?.let { uri ->
            withContext(Dispatchers.IO) {
                context.contentResolver.openInputStream(uri)?.use(BitmapFactory::decodeStream)
            }
        }
    }
    val shape = RoundedCornerShape(10.dp)
    Box(
        modifier
            .height(96.dp)
            .border(1.dp, MaterialTheme.colorScheme.outline, shape)
            .background(MaterialTheme.colorScheme.surface, shape)
            .clickable(onClick = onSelect),
        contentAlignment = Alignment.Center,
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap!!.asImageBitmap(),
                contentDescription = label,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
            Text(
                text = "×",
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .background(Color.Black.copy(alpha = 0.65f), CircleShape)
                    .clickable(onClick = onRemove)
                    .semantics { contentDescription = removeLabel }
                    .padding(horizontal = 7.dp, vertical = 1.dp),
            )
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("+", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleLarge)
                Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun AdoptionImage(url: String, contentDescription: String) {
    var loadFailed by remember(url) { mutableStateOf(false) }
    val bitmap by produceState<Bitmap?>(initialValue = AdoptionImageCache.get(url), url) {
        if (value == null) {
            val loadedBitmap = withContext(Dispatchers.IO) {
                runCatching {
                    URL(url).openStream().use { BitmapFactory.decodeStream(it) }
                }.getOrNull()?.also { AdoptionImageCache.put(url, it) }
            }
            if (loadedBitmap == null) {
                loadFailed = true
            } else {
                value = loadedBitmap
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(190.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap!!.asImageBitmap(),
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } else if (!loadFailed) {
            CircularProgressIndicator()
        }
    }
}

private object AdoptionImageCache {
    private val bitmaps = object : LruCache<String, Bitmap>(
        (Runtime.getRuntime().maxMemory() / 1024 / 8).toInt(),
    ) {
        override fun sizeOf(key: String, bitmap: Bitmap): Int = bitmap.byteCount / 1024
    }

    fun get(url: String): Bitmap? = bitmaps.get(url)

    fun put(url: String, bitmap: Bitmap) {
        bitmaps.put(url, bitmap)
    }
}

@Composable
private fun ApplicationReviewCard(
    application: AdoptionApplicationView,
    t: (String) -> String,
    onDecision: (String) -> Unit,
) {
    FendlyCard(Modifier.fillMaxWidth().padding(start = 14.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                "${t("adopt_applicant")} (${t("adopt_${application.status}")})",
                fontWeight = FontWeight.Bold,
            )
            Text("${t("adopt_home_label")}: ${application.homeType}")
            Text("${t("adopt_experience_label")}: ${application.experience}")
            Text("${t("adopt_phone_label")}: ${application.phone}")
            if (application.status == "submitted") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { onDecision("approved") }) { Text(t("adopt_approve")) }
                    TextButton(onClick = { onDecision("rejected") }) { Text(t("adopt_reject")) }
                }
            }
        }
    }
}

private fun uploadAdoptionListing(
    context: android.content.Context,
    token: String,
    photos: List<AdoptionPhoto>,
    petName: String,
    species: String,
    breed: String,
    ageMonths: Int,
    gender: String,
    healthStatus: String,
    description: String,
    city: String,
    autoPost: Boolean,
): String {
    val boundary = "FendlyAdoption${System.currentTimeMillis()}"
    val connection = URL("${BuildConfig.API_BASE_URL}/api/adoptions").openConnection() as HttpURLConnection
    try {
        connection.requestMethod = "POST"
        connection.doOutput = true
        connection.connectTimeout = 20000
        connection.readTimeout = 90000
        connection.setRequestProperty("Authorization", "Bearer $token")
        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
        connection.outputStream.buffered().use { output ->
            fun field(name: String, value: String) {
                val safeValue = value.replace("\r", " ").replace("\n", " ")
                output.write("--$boundary\r\nContent-Disposition: form-data; name=\"$name\"\r\n\r\n$safeValue\r\n".toByteArray(StandardCharsets.UTF_8))
            }
            photos.forEachIndexed { index, photo ->
                val mimeType = when {
                    photo.bitmap != null -> "image/jpeg"
                    photo.uri != null -> context.contentResolver.getType(photo.uri)
                        ?: error("Could not determine the selected photo type")
                    else -> error("A selected pet photo is no longer available")
                }.substringBefore(';').lowercase(Locale.ROOT)
                val extension = when (mimeType) {
                    "image/jpeg" -> "jpg"
                    "image/png" -> "png"
                    "image/webp" -> "webp"
                    else -> error("Choose JPEG, PNG, or WebP pet photos")
                }
                output.write(
                    "--$boundary\r\nContent-Disposition: form-data; name=\"images\"; filename=\"pet-${index + 1}.$extension\"\r\nContent-Type: $mimeType\r\n\r\n"
                        .toByteArray(StandardCharsets.UTF_8),
                )
                if (photo.bitmap != null) {
                    val bytes = ByteArrayOutputStream()
                    check(photo.bitmap.compress(Bitmap.CompressFormat.JPEG, 92, bytes)) {
                        "Could not prepare the captured pet photo"
                    }
                    output.write(bytes.toByteArray())
                } else {
                    val uri = checkNotNull(photo.uri)
                    context.contentResolver.openInputStream(uri)?.use { it.copyTo(output) }
                        ?: error("The selected photo is no longer available")
                }
                output.write("\r\n".toByteArray(StandardCharsets.UTF_8))
            }
            field("pet_name", petName)
            field("species", species)
            field("breed", breed)
            field("age_months", ageMonths.toString())
            field("gender", gender)
            field("health_status", healthStatus)
            field("description", description)
            field("location_city", city)
            field("auto_post_social", autoPost.toString())
            output.write("--$boundary--\r\n".toByteArray(StandardCharsets.UTF_8))
        }
        val code = connection.responseCode
        val response = (if (code in 200..299) connection.inputStream else connection.errorStream)
            ?.bufferedReader()?.use { it.readText() }.orEmpty()
        if (code !in 200..299) {
            val detail = runCatching { JSONObject(response).optString("detail") }.getOrDefault("")
            error(detail.ifBlank { "Request failed (HTTP $code)" })
        }
        return response
    } finally {
        connection.disconnect()
    }
}
