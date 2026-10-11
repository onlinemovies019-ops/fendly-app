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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import com.google.firebase.auth.FirebaseAuth
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
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { AdoptionScreen(onBack = ::finish) } }
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
            message = "Sign in to use this feature."
            return
        }
        user.getIdToken(false)
            .addOnSuccessListener { result -> onToken(result.token.orEmpty()) }
            .addOnFailureListener { error -> message = error.localizedMessage ?: "Could not authenticate." }
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
                message = error.localizedMessage ?: "Could not load adoption listings."
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
                    message = error.localizedMessage ?: "Could not load your listings."
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
                    message = error.localizedMessage ?: "Could not load your applications."
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
                    message = error.localizedMessage ?: "Could not load applications."
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
                title = { Text("Adopt a pet") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Back") }
                },
                actions = {
                    TextButton(onClick = {
                        showCreate = true
                        message = ""
                    }) { Text("List a pet") }
                },
            )
        },
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            TabRow(selectedTabIndex = tab) {
                listOf("Browse pets", "My listings", "My applications").forEachIndexed { index, title ->
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
                        OutlinedTextField(
                            value = speciesFilter,
                            onValueChange = { speciesFilter = it },
                            label = { Text("Species") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                        )
                        OutlinedTextField(
                            value = cityFilter,
                            onValueChange = { cityFilter = it },
                            label = { Text("City") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = minimumAge,
                            onValueChange = { minimumAge = it.filter(Char::isDigit).take(3) },
                            label = { Text("Min age (months)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                        )
                        OutlinedTextField(
                            value = maximumAge,
                            onValueChange = { maximumAge = it.filter(Char::isDigit).take(3) },
                            label = { Text("Max age (months)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                        )
                    }
                    TextButton(onClick = ::refreshFeed) { Text("Apply filters / refresh") }
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(feed, key = AdoptionListing::id) { listing ->
                            ListingCard(listing) {
                                if (listing.status !in setOf("available", "pending")) {
                                    message = "This pet is not accepting applications right now."
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
                        ListingCard(listing, actionLabel = "Review applications") {
                            loadApplications(listing)
                        }
                        if (listing.id == reviewListing) {
                            reviewApplications.forEach { application ->
                                ApplicationReviewCard(
                                    application = application,
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
                                                    message = error.localizedMessage ?: "Could not update application."
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
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(application.petName, fontWeight = FontWeight.Bold)
                                Text("Status: ${application.status}")
                                if (application.status == "approved") {
                                    Text(
                                        if (application.listerPhone.isBlank()) {
                                            "The lister has no verified phone number on file."
                                        } else {
                                            "Contact the lister: ${application.listerPhone}"
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
            Card(shape = RoundedCornerShape(20.dp)) {
                Column(
                    Modifier.fillMaxWidth().padding(20.dp).verticalScroll(fields),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("Create adoption listing", style = MaterialTheme.typography.titleLarge)
                    OutlinedTextField(petName, { petName = it }, label = { Text("Pet name") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(species, { species = it }, label = { Text("Species") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(breed, { breed = it }, label = { Text("Breed (optional)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(
                        age,
                        { age = it.filter(Char::isDigit).take(3) },
                        label = { Text("Age in months") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                    OutlinedTextField(gender, { gender = it }, label = { Text("Gender") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(health, { health = it }, label = { Text("Health / vaccination status") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(description, { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                    OutlinedTextField(city, { city = it }, label = { Text("City") }, modifier = Modifier.fillMaxWidth())
                    TextButton(onClick = { photoPicker.launch("image/*") }) {
                        Text(if (selectedPhoto == null) "Choose pet photo" else "Pet photo selected")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(autoPostSocial, { autoPostSocial = it })
                        Text(
                            "Auto-post this pet adoption to our official Facebook & Instagram pages to help find a home faster.",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    Text(
                        "Optional. If posting fails, your Fendly listing will remain available.",
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                        TextButton(onClick = { showCreate = false }) { Text("Cancel") }
                        Button(
                            enabled = !busy,
                            onClick = {
                                val ageValue = age.toIntOrNull()
                                val photo = selectedPhoto
                                if (
                                    petName.isBlank() || species.isBlank() || ageValue == null ||
                                    gender.isBlank() || health.isBlank() || description.isBlank() ||
                                    city.isBlank() || photo == null
                                ) {
                                    message = "Complete the required fields and choose a pet photo."
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
                                                    "failed" -> "Listing published. Social post needs attention: ${listing.socialError}"
                                                    "published" -> "Listing published and shared to social pages."
                                                    else -> "Adoption listing published."
                                                }
                                                showCreate = false
                                                tab = 1
                                                loadMine()
                                                refreshFeed()
                                            } catch (error: Exception) {
                                                message = error.localizedMessage ?: "Could not create listing."
                                            } finally {
                                                busy = false
                                            }
                                        }
                                    }
                                }
                            },
                        ) { Text("Publish listing") }
                    }
                }
            }
        }
    }

    applyingTo?.let { listing ->
        AlertDialog(
            onDismissRequest = { applyingTo = null },
            title = { Text("Request to adopt ${listing.petName}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(homeType, { homeType = it }, label = { Text("Home type") })
                    OutlinedTextField(petExperience, { petExperience = it }, label = { Text("Pet experience") }, minLines = 2)
                    OutlinedTextField(
                        contactPhone,
                        { contactPhone = it },
                        label = { Text("Your contact phone") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (homeType.isBlank() || petExperience.isBlank() || contactPhone.isBlank()) {
                        message = "Complete all application fields."
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
                                    message = "Adoption request sent."
                                    applyingTo = null
                                    homeType = ""
                                    petExperience = ""
                                    contactPhone = ""
                                    tab = 2
                                    loadMyApplications()
                                    refreshFeed()
                                } catch (error: Exception) {
                                    message = error.localizedMessage ?: "Could not send application."
                                }
                            }
                        }
                    }
                }) { Text("Send request") }
            },
            dismissButton = {
                TextButton(onClick = { applyingTo = null }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun ListingCard(
    listing: AdoptionListing,
    actionLabel: String = "Request to Adopt",
    onAction: () -> Unit,
) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            if (listing.image.isNotBlank()) AdoptionImage(listing.image)
            Text(listing.petName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("${listing.species.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }}${if (listing.breed.isBlank()) "" else " · ${listing.breed}"}")
            Text("${listing.ageMonths} months · ${listing.gender} · ${listing.city}")
            Text("Health: ${listing.healthStatus}")
            Text(listing.description)
            Text("Status: ${listing.status}")
            if (listing.socialStatus.isNotBlank()) {
                Text(
                    if (listing.socialStatus == "failed") {
                        "Social post: needs attention. ${listing.socialError}"
                    } else {
                        "Social post: ${listing.socialStatus}"
                    },
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Button(onClick = onAction) { Text(actionLabel) }
        }
    }
}

@Composable
private fun AdoptionImage(url: String) {
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
            contentDescription = "Pet photo",
            modifier = Modifier.fillMaxWidth().height(190.dp),
            contentScale = ContentScale.Crop,
        )
    }
}

@Composable
private fun ApplicationReviewCard(
    application: AdoptionApplicationView,
    onDecision: (String) -> Unit,
) {
    Card(Modifier.fillMaxWidth().padding(start = 14.dp)) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Applicant (${application.status})", fontWeight = FontWeight.Bold)
            Text("Home: ${application.homeType}")
            Text("Experience: ${application.experience}")
            Text("Phone: ${application.phone}")
            if (application.status == "submitted") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { onDecision("approved") }) { Text("Approve") }
                    TextButton(onClick = { onDecision("rejected") }) { Text("Reject") }
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
