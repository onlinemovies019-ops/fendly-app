package com.example.fendly

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.LruCache
import androidx.core.content.FileProvider
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
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
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import androidx.core.view.WindowCompat
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.example.fendly.ui.theme.FendlyCard
import com.example.fendly.ui.theme.FendlyPrimaryButton
import com.example.fendly.ui.theme.FendlyTextField
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import java.util.Locale


class AdoptionActivity : ComponentActivity() {
    companion object {
        const val EXTRA_OPEN_LISTING_ID = "open_adoption_listing_id"
    }

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
    val images: List<String>,
    val imageCount: Int,
    val status: String,
    val canEdit: Boolean,
    val canDelete: Boolean,
    val socialStatus: String,
    val socialError: String,
    val socialPosterImage: String,
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

private fun adoptionListing(json: JSONObject): AdoptionListing {
    val imagesJson = json.optJSONArray("images")
    val images = imagesJson?.let { array ->
        (0 until array.length())
            .map { array.optString(it).trim() }
            .filter(String::isNotEmpty)
    }.orEmpty()
    return AdoptionListing(
        id = json.optString("id"),
        petName = json.optString("pet_name"),
        species = json.optString("species"),
        breed = json.optString("breed"),
        ageMonths = json.optInt("age_months"),
        gender = json.optString("gender"),
        healthStatus = json.optString("health_status"),
        description = json.optString("description"),
        city = json.optString("location_city"),
        images = images,
        imageCount = images.size,
        status = json.optString("status"),
        canEdit = json.optBoolean("can_edit", false),
        canDelete = json.optBoolean("can_delete", false),
        socialStatus = json.optString("social_post_status"),
        socialError = json.optString("social_post_error"),
        socialPosterImage = json.optString("social_post_image_url"),
    )
}

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
        connection.useCaches = false
        connection.connectTimeout = 15000
        connection.readTimeout = 30000
        connection.setRequestProperty("Cache-Control", "no-cache, no-store")
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
private fun AdoptionScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val notificationListingId = remember {
        (context as? AdoptionActivity)?.intent?.getStringExtra(
            AdoptionActivity.EXTRA_OPEN_LISTING_ID,
        ).orEmpty()
    }
    fun t(key: String): String = LanguageManager.adoptionText(context, key)
    val scope = rememberCoroutineScope()
    val feed = remember { mutableStateListOf<AdoptionListing>() }
    val mine = remember { mutableStateListOf<AdoptionListing>() }
    val myApplications = remember { mutableStateListOf<MyAdoptionApplication>() }
    val reviewApplications = remember { mutableStateListOf<AdoptionApplicationView>() }
    val mineListState = rememberLazyListState()
    var mineLoaded by remember { mutableStateOf(false) }
    var mineLoading by remember { mutableStateOf(false) }
    var reviewListing by remember { mutableStateOf("") }
    var reviewLoadingListing by remember { mutableStateOf("") }
    var expandedListing by remember { mutableStateOf("") }
    var expandedBrowseListings by remember { mutableStateOf(emptySet<String>()) }
    var editingListing by remember { mutableStateOf<AdoptionListing?>(null) }
    var deletingListing by remember { mutableStateOf<AdoptionListing?>(null) }
    var tab by remember { mutableStateOf(if (notificationListingId.isBlank()) 0 else 1) }
    var cityFilter by remember { mutableStateOf("") }
    var speciesFilter by remember { mutableStateOf("") }
    var minimumAge by remember { mutableStateOf("") }
    var maximumAge by remember { mutableStateOf("") }
    var showCreate by remember { mutableStateOf(false) }
    var publishingListing by remember { mutableStateOf(false) }
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

    suspend fun fetchFeed(token: String?) {
        try {
            val query = buildList {
                if (speciesFilter.isNotBlank()) add("species=${Uri.encode(speciesFilter.trim())}")
                if (cityFilter.isNotBlank()) add("city=${Uri.encode(cityFilter.trim())}")
                minimumAge.toIntOrNull()?.let { add("min_age_months=$it") }
                maximumAge.toIntOrNull()?.let { add("max_age_months=$it") }
            }.joinToString("&").let { if (it.isBlank()) "" else "?$it" }
            val result = withContext(Dispatchers.IO) {
                JSONArray(adoptionRequest("GET", "/api/adoptions$query", token))
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

    fun refreshFeed() {
        busy = true
        message = ""
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            scope.launch { fetchFeed(null) }
            return
        }
        user.getIdToken(false)
            .addOnSuccessListener { result ->
                scope.launch { fetchFeed(result.token) }
            }
            .addOnFailureListener {
                message = t("adopt_auth_failed")
                busy = false
            }
    }

    fun loadMine() {
        mineLoaded = false
        mineLoading = true
        message = ""
        withToken(
            onFailure = { mineLoading = false },
        ) { token ->
            scope.launch {
                try {
                    val result = withContext(Dispatchers.IO) {
                        JSONArray(adoptionRequest("GET", "/api/adoptions/mine", token))
                    }
                    mine.clear()
                    for (index in 0 until result.length()) mine.add(adoptionListing(result.getJSONObject(index)))
                    feed.removeAll { listing -> mine.any { it.id == listing.id } }
                    mineLoaded = true
                    message = ""
                } catch (error: Exception) {
                    message = t("adopt_load_listings_failed")
                } finally {
                    mineLoading = false
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

    fun shareAdoptionPoster(listing: AdoptionListing) {
        if (listing.socialPosterImage.isBlank()) {
            message = t("adopt_share_poster_unavailable")
            return
        }
        scope.launch {
            message = t("adopt_preparing_share")
            try {
                val poster = withContext(Dispatchers.IO) {
                    val connection = URL(listing.socialPosterImage).openConnection()
                    connection.connectTimeout = 15000
                    connection.readTimeout = 30000
                    connection.getInputStream().use(BitmapFactory::decodeStream)
                        ?: error("Could not decode the adoption poster image")
                }
                SharePosterUtil.sharePoster(
                    context,
                    poster,
                    t("adopt_share_caption")
                        .replace("{pet}", listing.petName)
                        .replace("{species}", listing.species)
                        .replace("{city}", listing.city),
                )
                message = ""
            } catch (error: Exception) {
                message = t("adopt_share_failed")
            }
        }
    }

    LaunchedEffect(Unit) {
        if (notificationListingId.isBlank()) refreshFeed() else loadMine()
    }
    LaunchedEffect(mineLoaded, notificationListingId) {
        if (!mineLoaded || notificationListingId.isBlank()) return@LaunchedEffect
        val listing = mine.firstOrNull { it.id == notificationListingId }
        if (listing != null) {
            expandedListing = listing.id
            mineListState.animateScrollToItem(mine.indexOf(listing))
            loadApplications(listing)
            (context as? AdoptionActivity)?.intent?.removeExtra(
                AdoptionActivity.EXTRA_OPEN_LISTING_ID,
            )
        } else {
            message = t("adopt_notification_listing_unavailable")
        }
    }
    fun addSelectedPhotos(uris: List<Uri>) {
        val emptySlots = selectedPhotos.indices.filter { selectedPhotos[it] == null }
        val targetSlots = if (selectedPhotoSlot in selectedPhotos.indices
            && selectedPhotos[selectedPhotoSlot] != null
        ) {
            listOf(selectedPhotoSlot) + emptySlots.filter { it != selectedPhotoSlot }
        } else {
            emptySlots
        }
        uris.distinct().take(targetSlots.size).forEachIndexed { index, uri ->
            selectedPhotos[targetSlots[index]] = AdoptionPhoto(uri = uri)
        }
    }
    val availablePhotoSlots = (
        selectedPhotos.count { it == null } +
            if (selectedPhotoSlot in selectedPhotos.indices
                && selectedPhotos[selectedPhotoSlot] != null
            ) 1 else 0
        ).coerceIn(1, selectedPhotos.size)
    val singlePhotoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) addSelectedPhotos(listOf(uri))
    }
    val multiplePhotoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(
            maxItems = availablePhotoSlots.coerceAtLeast(2),
        ),
    ) { uris ->
        addSelectedPhotos(uris)
    }
    var cameraPhotoUri by remember { mutableStateOf<Uri?>(null) }
    val photoCropLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val croppedPhotoUri = result.data
                ?.getStringExtra(PhotoCropActivity.EXTRA_CROPPED_IMAGE_URI)
                ?.let(Uri::parse)
            if (croppedPhotoUri != null && selectedPhotoSlot in selectedPhotos.indices) {
                selectedPhotos[selectedPhotoSlot] = AdoptionPhoto(uri = croppedPhotoUri)
            }
        }
    }
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture(),
    ) { captured ->
        val capturedPhotoUri = cameraPhotoUri
        cameraPhotoUri = null
        if (captured && capturedPhotoUri != null) {
            val cropIntent = Intent(context, PhotoCropActivity::class.java)
                .putExtra(PhotoCropActivity.EXTRA_IMAGE_URI, capturedPhotoUri.toString())
            photoCropLauncher.launch(cropIntent)
        }
    }
    fun startCameraCapture() {
        try {
            val directory = File(context.filesDir, "image_provider")
            if (!directory.exists() && !directory.mkdirs()) {
                message = t("adopt_camera_unavailable")
                return
            }
            val captureFile = File(directory, "camera_${System.currentTimeMillis()}.jpg")
            val captureUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                captureFile,
            )
            cameraPhotoUri = captureUri
            cameraLauncher.launch(captureUri)
        } catch (error: ActivityNotFoundException) {
            cameraPhotoUri = null
            message = t("adopt_camera_unavailable")
        } catch (error: IllegalArgumentException) {
            cameraPhotoUri = null
            message = t("adopt_camera_unavailable")
        }
    }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            startCameraCapture()
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
                            0 -> refreshFeed()
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
                                onDone = {
                                    focusManager.clearFocus()
                                    keyboardController?.hide()
                                },
                            ),
                            singleLine = true,
                        )
                    }
                    TextButton(onClick = {
                        refreshFeed()
                    }) { Text(t("adopt_refresh")) }
                    if (feed.isEmpty() && busy) {
                        Column(
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            CircularProgressIndicator(Modifier.size(28.dp))
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            items(feed.filterNot { listing -> mine.any { it.id == listing.id } }, key = AdoptionListing::id) { listing ->
                                ListingCard(
                                    listing = listing,
                                    t = ::t,
                                    actionLabel = t("adopt_request"),
                                    expanded = listing.id in expandedBrowseListings,
                                    onToggleExpand = {
                                        expandedBrowseListings =
                                            if (listing.id in expandedBrowseListings) {
                                                expandedBrowseListings - listing.id
                                            } else {
                                                expandedBrowseListings + listing.id
                                            }
                                    },
                                ) {
                                    if (listing.status !in setOf("available", "pending")) {
                                        message = t("adopt_not_accepting")
                                    } else {
                                        applyingTo = listing
                                    }
                                }
                            }
                            if (feed.isEmpty() && message.isNotBlank()) {
                                item {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(message, color = MaterialTheme.colorScheme.error)
                                        TextButton(onClick = { refreshFeed() }) {
                                            Text(t("adopt_refresh"))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> when {
                    mineLoading -> Box(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(Modifier.size(28.dp))
                    }
                    !mineLoaded -> Column(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(t("adopt_load_listings_failed"))
                        TextButton(onClick = { loadMine() }) { Text(t("adopt_refresh")) }
                    }
                    else -> LazyColumn(
                        state = mineListState,
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
                                if (listing.socialPosterImage.isNotBlank()) {
                                    TextButton(
                                        modifier = Modifier.weight(1f),
                                        onClick = { shareAdoptionPoster(listing) },
                                    ) {
                                        Text(t("adopt_share_poster"), maxLines = 1, softWrap = false)
                                    }
                                }
                                if (listing.canEdit) {
                                    TextButton(
                                        modifier = Modifier.weight(1f),
                                        onClick = {
                                            petName = listing.petName
                                            species = listing.species
                                            breed = listing.breed
                                            age = listing.ageMonths.toString()
                                            gender = listing.gender
                                            health = listing.healthStatus
                                            description = listing.description
                                            city = listing.city
                                            editingListing = listing
                                        },
                                    ) {
                                        Text(t("adopt_edit"), maxLines = 1, softWrap = false)
                                    }
                                }
                                if (listing.canDelete) {
                                    TextButton(
                                        modifier = Modifier.weight(1f),
                                        onClick = { deletingListing = listing },
                                    ) {
                                        Text(
                                            t("adopt_delete"),
                                            color = MaterialTheme.colorScheme.error,
                                            maxLines = 1,
                                            softWrap = false,
                                        )
                                    }
                                }
                                if (!listing.canEdit && !listing.canDelete) {
                                    Text(
                                        t("adopt_edit_window_expired"),
                                        modifier = Modifier.weight(1f),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
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
                                        } else if (application.status == "owner_approved") {
                                            Text(t("adopt_contact_pending"))
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
        Dialog(onDismissRequest = {
            if (!publishingListing) showCreate = false
        }) {
            val dialogFocusManager = LocalFocusManager.current
            val dialogKeyboardController = LocalSoftwareKeyboardController.current
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
                            onDone = {
                                dialogKeyboardController?.hide()
                                dialogFocusManager.clearFocus(force = true)
                            },
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
                        TextButton(
                            enabled = !publishingListing,
                            onClick = { showCreate = false },
                        ) { Text(t("adopt_cancel")) }
                        FendlyPrimaryButton(
                            text = if (publishingListing) {
                                t("adopt_publishing")
                            } else {
                                t("adopt_publish")
                            },
                            enabled = !busy && !publishingListing,
                            loading = publishingListing,
                            onClick = {
                                if (publishingListing) return@FendlyPrimaryButton
                                val ageValue = age.toIntOrNull()
                                val photos = selectedPhotos.filterNotNull()
                                if (
                                    petName.isBlank() || species.isBlank() || ageValue == null ||
                                    gender.isBlank() || health.isBlank() || description.isBlank() ||
                                    city.isBlank() || photos.isEmpty()
                                ) {
                                    message = t("adopt_required_fields")
                                } else if (
                                    ageValue !in 0..600 ||
                                    petName.trim().length > 120 ||
                                    species.trim().length > 60 ||
                                    breed.trim().length > 120 ||
                                    health.trim().length > 500 ||
                                    description.trim().length > 5000 ||
                                    city.trim().length > 120
                                ) {
                                    message = t("adopt_invalid_fields")
                                } else {
                                    publishingListing = true
                                    withToken(
                                        onFailure = { publishingListing = false },
                                    ) { token ->
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
                                                        LanguageManager.getSavedLanguage(context),
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
                                                message = listOf(
                                                    t("adopt_submit_failed"),
                                                    error.message?.takeIf(String::isNotBlank),
                                                ).filterNotNull().joinToString(" ")
                                            } finally {
                                                busy = false
                                                publishingListing = false
                                            }
                                        }
                                    }
                                }
                            },
                        )
                        if (message.isNotBlank()) {
                            Text(
                                message,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
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
                                    val request = PickVisualMediaRequest(
                                        ActivityResultContracts.PickVisualMedia.ImageOnly,
                                    )
                                    if (availablePhotoSlots == 1) {
                                        singlePhotoPicker.launch(request)
                                    } else {
                                        multiplePhotoPicker.launch(request)
                                    }
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
                                            startCameraCapture()
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
    editingListing?.let { listing ->
        AlertDialog(
            onDismissRequest = { editingListing = null },
            title = { Text(t("adopt_edit_listing")) },
            text = {
                val dialogFocusManager = LocalFocusManager.current
                val dialogKeyboardController = LocalSoftwareKeyboardController.current
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
                    FendlyTextField(
                        city,
                        { city = it },
                        t("adopt_city"),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Done,
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                dialogKeyboardController?.hide()
                                dialogFocusManager.clearFocus(force = true)
                            },
                        ),
                        singleLine = true,
                    )
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
                            onDone = {
                                focusManager.clearFocus()
                                keyboardController?.hide()
                            },
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
                val toggleShape = RoundedCornerShape(24.dp)
                val toggleSurface = MaterialTheme.colorScheme.surface
                val toggleBorder = if (toggleSurface.red < 0.5f) {
                    Color(0xFF323943)
                } else {
                    Color(0xFFD8D0C4)
                }
                val screenWidthDp = LocalContext.current.resources.configuration.screenWidthDp
                val textScale = when {
                    screenWidthDp < 360 -> 0.90f
                    screenWidthDp < 480 -> 0.94f
                    screenWidthDp < 720 -> 1f
                    else -> 1.08f
                }
                val userFontScale = LocalContext.current
                    .getSharedPreferences("fendly_settings", ComponentActivity.MODE_PRIVATE)
                    .getFloat("font_scale", 1f)
                    .coerceIn(0.85f, 1.25f)
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .shadow(3.dp, toggleShape)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    lerp(toggleSurface, Color.White, 0.14f),
                                    toggleSurface,
                                    lerp(toggleSurface, Color.Black, 0.12f),
                                ),
                            ),
                            toggleShape,
                        )
                        .border(1.dp, toggleBorder, toggleShape)
                        .clickable(role = Role.Button, onClick = onToggleExpand)
                        .semantics {
                            contentDescription = t(
                                if (expanded) "adopt_collapse" else "adopt_expand",
                            )
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        if (expanded) "▲" else "▼",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = (12 * textScale * userFontScale).sp,
                        fontWeight = FontWeight.Normal,
                    )
                }
            }
            if (expanded) {
                if (listing.images.isNotEmpty()) {
                    AdoptionImageCarousel(
                        images = listing.images,
                        contentDescription = t("adopt_photo_content_description"),
                    )
                }
                Text("${listing.species.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }}${if (listing.breed.isBlank()) "" else " · ${listing.breed}"}")
                Text("${listing.ageMonths} ${t("adopt_months")} · ${listing.gender} · ${listing.city}")
                Text("${t("adopt_health_label")}: ${listing.healthStatus}")
                Text(listing.description)
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
private fun AdoptionImageCarousel(images: List<String>, contentDescription: String) {
    var imageIndex by remember(images) { mutableStateOf(0) }
    Box {
        val imageUrl = images[imageIndex]
        key(imageUrl) {
            AdoptionImage(imageUrl, contentDescription)
        }
        if (images.size > 1) {
            TextButton(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(8.dp)
                    .background(Color.Black.copy(alpha = 0.65f), CircleShape),
                onClick = { imageIndex = (imageIndex - 1 + images.size) % images.size },
            ) {
                Text("‹", color = Color.White, style = MaterialTheme.typography.titleLarge)
            }
            TextButton(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(8.dp)
                    .background(Color.Black.copy(alpha = 0.65f), CircleShape),
                onClick = { imageIndex = (imageIndex + 1) % images.size },
            ) {
                Text("›", color = Color.White, style = MaterialTheme.typography.titleLarge)
            }
            Text(
                text = "${imageIndex + 1} / ${images.size}",
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp)
                    .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall,
            )
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
    var bitmap by remember(photo) { mutableStateOf(photo?.bitmap) }
    LaunchedEffect(photo) {
        bitmap = photo?.bitmap ?: photo?.uri?.let { uri ->
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
    var bitmap by remember(url) { mutableStateOf(AdoptionImageCache.get(url)) }
    LaunchedEffect(url) {
        if (bitmap == null) {
            val loadedBitmap = withContext(Dispatchers.IO) {
                runCatching {
                    val connection = URL(url).openConnection()
                    connection.useCaches = false
                    connection.connectTimeout = 15000
                    connection.readTimeout = 30000
                    connection.getInputStream().use(BitmapFactory::decodeStream)
                }.getOrNull()?.also { AdoptionImageCache.put(url, it) }
            }
            bitmap = loadedBitmap
            loadFailed = loadedBitmap == null
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

private fun adoptionErrorDetail(response: String): String {
    val detail = runCatching { JSONObject(response).opt("detail") }.getOrNull()
    return when (detail) {
        is String -> detail
        is JSONArray -> (0 until detail.length()).mapNotNull { index ->
            val issue = detail.optJSONObject(index) ?: return@mapNotNull null
            val location = issue.optJSONArray("loc")
            val field = location?.let { parts ->
                (0 until parts.length())
                    .map(parts::optString)
                    .filterNot { it == "body" }
                    .joinToString(".")
            }.orEmpty()
            val reason = issue.optString("msg")
            when {
                field.isNotBlank() && reason.isNotBlank() -> "$field: $reason"
                reason.isNotBlank() -> reason
                else -> null
            }
        }.joinToString("; ")
        else -> ""
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
            if (application.phone.isBlank()) {
                Text(t("adopt_contact_pending"))
            } else {
                Text("${t("adopt_phone_label")}: ${application.phone}")
            }
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
    posterLanguage: String,
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
            field("poster_language", posterLanguage)
            output.write("--$boundary--\r\n".toByteArray(StandardCharsets.UTF_8))
        }
        val code = connection.responseCode
        val response = (if (code in 200..299) connection.inputStream else connection.errorStream)
            ?.bufferedReader()?.use { it.readText() }.orEmpty()
        if (code !in 200..299) {
            val detail = adoptionErrorDetail(response)
            error(detail.ifBlank { "Request failed (HTTP $code)" })
        }
        return response
    } finally {
        connection.disconnect()
    }
}
