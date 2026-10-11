package com.example.fendly

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.window.Dialog
import androidx.core.view.WindowCompat
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
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
import java.nio.charset.StandardCharsets
import java.util.Locale


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
    val status: String,
    val socialStatus: String,
    val socialError: String,
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
    fun t(key: String): String = LanguageManager.adoptionText(context, key)
    val scope = rememberCoroutineScope()
    val feed = remember { mutableStateListOf<AdoptionListing>() }
    val mine = remember { mutableStateListOf<AdoptionListing>() }
    val myApplications = remember { mutableStateListOf<MyAdoptionApplication>() }
    val reviewApplications = remember { mutableStateListOf<AdoptionApplicationView>() }
    var reviewListing by remember { mutableStateOf("") }
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
    var selectedPhoto by remember { mutableStateOf<Uri?>(null) }
    var homeType by remember { mutableStateOf("") }
    var petExperience by remember { mutableStateOf("") }
    var contactPhone by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }

    fun withToken(onToken: (String) -> Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            message = t("adopt_sign_in")
            return
        }
        user.getIdToken(false)
            .addOnSuccessListener { result -> onToken(result.token.orEmpty()) }
            .addOnFailureListener { message = t("adopt_auth_failed") }
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
        withToken { token ->
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
                    reviewListing = listing.id
                    message = ""
                } catch (error: Exception) {
                    message = t("adopt_load_reviews_failed")
                }
            }
        }
    }

    LaunchedEffect(Unit) { refreshFeed() }
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        selectedPhoto = uri
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(t("adopt_title"), maxLines = 1) },
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
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                        )
                        FendlyTextField(
                            value = cityFilter,
                            onValueChange = { cityFilter = it },
                            label = t("adopt_city"),
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FendlyTextField(
                            value = minimumAge,
                            onValueChange = { minimumAge = it.filter(Char::isDigit).take(3) },
                            label = t("adopt_min_age"),
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                        )
                        FendlyTextField(
                            value = maximumAge,
                            onValueChange = { maximumAge = it.filter(Char::isDigit).take(3) },
                            label = t("adopt_max_age"),
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                        )
                    }
                    TextButton(onClick = ::refreshFeed) { Text(t("adopt_refresh")) }
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(feed, key = AdoptionListing::id) { listing ->
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
                    items(mine, key = AdoptionListing::id) { listing ->
                        ListingCard(
                            listing,
                            t = ::t,
                            actionLabel = t("adopt_review"),
                        ) {
                            loadApplications(listing)
                        }
                        if (listing.id == reviewListing) {
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
                else -> LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
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

    if (showCreate) {
        val fields = rememberScrollState()
        Dialog(onDismissRequest = { showCreate = false }) {
            FendlyCard(Modifier.fillMaxWidth()) {
                Column(
                    Modifier.fillMaxWidth().verticalScroll(fields),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(t("adopt_create_title"), style = MaterialTheme.typography.titleLarge)
                    FendlyTextField(petName, { petName = it }, t("adopt_pet_name"), modifier = Modifier.fillMaxWidth(), singleLine = true)
                    FendlyTextField(species, { species = it }, t("adopt_species"), modifier = Modifier.fillMaxWidth(), singleLine = true)
                    FendlyTextField(breed, { breed = it }, t("adopt_breed_optional"), modifier = Modifier.fillMaxWidth(), singleLine = true)
                    FendlyTextField(
                        age,
                        { age = it.filter(Char::isDigit).take(3) },
                        label = t("adopt_age_months"),
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                    )
                    FendlyTextField(gender, { gender = it }, t("adopt_gender"), modifier = Modifier.fillMaxWidth(), singleLine = true)
                    FendlyTextField(health, { health = it }, t("adopt_health"), modifier = Modifier.fillMaxWidth())
                    FendlyTextField(
                        description,
                        { description = it },
                        t("adopt_description"),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    FendlyTextField(city, { city = it }, t("adopt_city"), modifier = Modifier.fillMaxWidth(), singleLine = true)
                    TextButton(onClick = { photoPicker.launch("image/*") }) {
                        Text(if (selectedPhoto == null) t("adopt_choose_photo") else t("adopt_photo_selected"))
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
                                val photo = selectedPhoto
                                if (
                                    petName.isBlank() || species.isBlank() || ageValue == null ||
                                    gender.isBlank() || health.isBlank() || description.isBlank() ||
                                    city.isBlank() || photo == null
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
                                                        photo,
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
            }
        }
    }

    applyingTo?.let { listing ->
        AlertDialog(
            onDismissRequest = { applyingTo = null },
            title = {
                Text(
                    t("adopt_request_title").replace("{pet}", listing.petName),
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FendlyTextField(homeType, { homeType = it }, t("adopt_home_type"), modifier = Modifier.fillMaxWidth(), singleLine = true)
                    FendlyTextField(
                        petExperience,
                        { petExperience = it },
                        label = t("adopt_experience"),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    FendlyTextField(
                        contactPhone,
                        { contactPhone = it },
                        label = t("adopt_your_phone"),
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
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
    onAction: () -> Unit,
) {
    FendlyCard(Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (listing.image.isNotBlank()) AdoptionImage(listing.image, t("adopt_photo_content_description"))
            Text(listing.petName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("${listing.species.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }}${if (listing.breed.isBlank()) "" else " · ${listing.breed}"}")
            Text("${listing.ageMonths} ${t("adopt_months")} · ${listing.gender} · ${listing.city}")
            Text("${t("adopt_health_label")}: ${listing.healthStatus}")
            Text(listing.description)
            Text("${t("adopt_status")}: ${t("adopt_${listing.status}")}")
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
            FendlyPrimaryButton(text = actionLabel, onClick = onAction)
        }
    }
}

@Composable
private fun AdoptionImage(url: String, contentDescription: String) {
    val bitmap by produceState<android.graphics.Bitmap?>(initialValue = null, url) {
        value = withContext(Dispatchers.IO) {
            runCatching {
                URL(url).openStream().use { BitmapFactory.decodeStream(it) }
            }.getOrNull()
        }
    }
    bitmap?.let {
        Image(
            bitmap = it.asImageBitmap(),
            contentDescription = contentDescription,
            modifier = Modifier.fillMaxWidth().height(190.dp),
            contentScale = ContentScale.Crop,
        )
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
    photo: Uri,
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
            val mimeType = context.contentResolver.getType(photo) ?: "image/jpeg"
            output.write("--$boundary\r\nContent-Disposition: form-data; name=\"images\"; filename=\"pet-photo\"\r\nContent-Type: $mimeType\r\n\r\n".toByteArray(StandardCharsets.UTF_8))
            context.contentResolver.openInputStream(photo)?.use { it.copyTo(output) }
                ?: error("The selected photo is no longer available")
            output.write("\r\n".toByteArray(StandardCharsets.UTF_8))
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
