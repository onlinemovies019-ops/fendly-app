package com.example.fendly

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

data class ProfileLabels(
    val title: String,
    val accountDetails: String,
    val firstName: String,
    val surname: String,
    val email: String,
    val mobile: String,
    val state: String,
    val city: String,
    val saveChanges: String,
    val refreshStatus: String,
)

data class ProfileUiState(
    val profile: UserProfile = UserProfile(),
    val labels: ProfileLabels? = null,
    val isLoading: Boolean = true,
    val isVerified: Boolean = false,
)

class ProfileViewModel(
    private val repository: UserRepository = UserRepository(),
) : ViewModel() {

    private val _profileUiState = MutableStateFlow(ProfileUiState())
    val profileUiState: StateFlow<ProfileUiState> = _profileUiState

    fun refreshLocalizedStrings(context: Context) {
        val current = _profileUiState.value
        _profileUiState.value = current.copy(labels = localizedLabels(context))
    }

    fun saveProfile(profile: UserProfile) {
        _profileUiState.value = _profileUiState.value.copy(isLoading = true)
        viewModelScope.launch {
            repository.saveUserProfile(profile)
                .addOnSuccessListener {
                    val user = FirebaseAuth.getInstance().currentUser
                    user?.getIdToken(true)?.addOnSuccessListener { tokenResult ->
                        val token = tokenResult.token ?: ""
                        viewModelScope.launch(Dispatchers.IO) {
                            try {
                                val url = URL("https://fendly-api.onrender.com/api/users/profile")
                                val connection = url.openConnection() as HttpURLConnection
                                connection.requestMethod = "PUT"
                                connection.setRequestProperty("Authorization", "Bearer $token")
                                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                                connection.doOutput = true
                                connection.connectTimeout = 15000
                                connection.readTimeout = 30000

                                val body = JSONObject().apply {
                                    put("username", profile.username.ifBlank { profile.email.substringBefore("@") })
                                    put("full_name", "${profile.name} ${profile.surname}".trim())
                                    put("email", profile.email)
                                    put("mobile", profile.mobileNumber)
                                    put("state", profile.state)
                                    put("city", profile.city)
                                    put("profile_photo_url", profile.imageUrl ?: "")
                                }.toString()

                                connection.outputStream.use { output ->
                                    output.write(body.toByteArray(StandardCharsets.UTF_8))
                                }
                                connection.responseCode
                                connection.disconnect()
                            } catch (e: Exception) {
                                Log.e("ProfileViewModel", "Backend profile sync failed", e)
                            }

                            Handler(Looper.getMainLooper()).post {
                                _profileUiState.value = _profileUiState.value.copy(
                                    profile = profile,
                                    isLoading = false,
                                )
                            }
                        }
                    } ?: run {
                        Handler(Looper.getMainLooper()).post {
                            _profileUiState.value = _profileUiState.value.copy(
                                profile = profile,
                                isLoading = false,
                            )
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("FIREBASE_ERROR", "Data save failed: ", exception)
                    Handler(Looper.getMainLooper()).post {
                        _profileUiState.value = _profileUiState.value.copy(isLoading = false)
                    }
                }
        }
    }

    fun loadProfile(context: Context) {
        _profileUiState.value = _profileUiState.value.copy(isLoading = true)
        reloadUserAndFetchProfile(context)
    }

    private fun reloadUserAndFetchProfile(context: Context) {
        val user = FirebaseAuth.getInstance().currentUser ?: run {
            _profileUiState.value = _profileUiState.value.copy(isLoading = false, isVerified = false)
            return
        }

        user.reload().addOnCompleteListener {
            user.getIdToken(true).addOnSuccessListener { result ->
                val token = result.token ?: ""
                fetchUserProfileFromBackend(context, token)
            }.addOnFailureListener {
                fallbackToFirestoreProfile(context)
            }
        }.addOnFailureListener {
            fallbackToFirestoreProfile(context)
        }
    }

    fun fetchUserProfileFromBackend(context: Context, token: String) {
        viewModelScope.launch(Dispatchers.IO) {
            var backendProfile = UserProfile()
            var isVerified = false
            try {
                val url = URL("https://fendly-api.onrender.com/api/users/profile")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("Authorization", "Bearer $token")
                connection.connectTimeout = 15000
                connection.readTimeout = 30000

                if (connection.responseCode in (200..299)) {
                    val payload = connection.inputStream.bufferedReader().use { it.readText() }
                    if (payload.isNotBlank()) {
                        val json = JSONObject(payload)
                        val fullName = json.optString("full_name", "")
                        val parts = fullName.split(" ", limit = 2)
                        val firstName = parts.getOrNull(0) ?: ""
                        val surname = parts.getOrNull(1) ?: ""
                        val email = json.optString("email", "")
                        val mobile = json.optString("mobile", "")
                        val state = json.optString("state", "")
                        val city = json.optString("city", "")
                        val username = json.optString("username", "")
                        val photoUrl = json.optString("profile_photo_url", json.optString("imageUrl", ""))
                        val emailVerified = json.optBoolean("email_verified", false)
                        val mobileVerified = json.optBoolean("mobile_verified", false)
                        isVerified = json.optBoolean("is_verified", emailVerified)

                        backendProfile = UserProfile(
                            name = firstName,
                            surname = surname,
                            email = email,
                            mobileNumber = mobile,
                            state = state,
                            city = city,
                            username = username,
                            imageUrl = photoUrl.ifBlank { null },
                            isEmailVerified = emailVerified,
                            isMobileVerified = mobileVerified,
                        )
                    }
                }
                connection.disconnect()
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Fetch profile from backend failed", e)
            }

            repository.getUserProfile()
                .addOnSuccessListener { firestoreProfile ->
                    val mergedProfile = UserProfile(
                        name = backendProfile.name.ifBlank { firestoreProfile.name },
                        surname = backendProfile.surname.ifBlank { firestoreProfile.surname },
                        email = backendProfile.email.ifBlank { firestoreProfile.email },
                        mobileNumber = backendProfile.mobileNumber.ifBlank { firestoreProfile.mobileNumber },
                        state = backendProfile.state.ifBlank { firestoreProfile.state },
                        city = backendProfile.city.ifBlank { firestoreProfile.city },
                        username = backendProfile.username.ifBlank { firestoreProfile.username },
                        imageUrl = backendProfile.imageUrl?.ifBlank { firestoreProfile.imageUrl } ?: firestoreProfile.imageUrl,
                        isEmailVerified = backendProfile.isEmailVerified || firestoreProfile.isEmailVerified,
                        isMobileVerified = backendProfile.isMobileVerified || firestoreProfile.isMobileVerified,
                    )

                    val firebaseUserVerified = FirebaseAuth.getInstance().currentUser?.isEmailVerified == true

                    Handler(Looper.getMainLooper()).post {
                        _profileUiState.value = ProfileUiState(
                            profile = mergedProfile,
                            labels = localizedLabels(context),
                            isLoading = false,
                            isVerified = isVerified || mergedProfile.isEmailVerified || firebaseUserVerified,
                        )
                    }
                }
                .addOnFailureListener {
                    Handler(Looper.getMainLooper()).post {
                        _profileUiState.value = ProfileUiState(
                            profile = backendProfile,
                            labels = localizedLabels(context),
                            isLoading = false,
                            isVerified = isVerified || backendProfile.isEmailVerified || (FirebaseAuth.getInstance().currentUser?.isEmailVerified == true),
                        )
                    }
                }
        }
    }

    private fun fallbackToFirestoreProfile(context: Context) {
        viewModelScope.launch {
            repository.getUserProfile()
                .addOnSuccessListener { profile ->
                    val firebaseUserVerified = FirebaseAuth.getInstance().currentUser?.isEmailVerified == true
                    Handler(Looper.getMainLooper()).post {
                        _profileUiState.value = ProfileUiState(
                            profile = profile,
                            labels = localizedLabels(context),
                            isLoading = false,
                            isVerified = profile.isEmailVerified || firebaseUserVerified,
                        )
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("FIREBASE_ERROR", "Data fetch failed: ", exception)
                    Handler(Looper.getMainLooper()).post {
                        _profileUiState.value = _profileUiState.value.copy(
                            labels = localizedLabels(context),
                            isLoading = false,
                        )
                    }
                }
        }
    }

    fun checkServerVerification(context: Context, onResult: (Boolean) -> Unit = {}) {
        val user = FirebaseAuth.getInstance().currentUser ?: run {
            _profileUiState.value = _profileUiState.value.copy(isLoading = false, isVerified = false)
            onResult(false)
            return
        }

        user.reload().addOnCompleteListener {
            user.getIdToken(true).addOnSuccessListener { result ->
                val token = result.token ?: ""
                fetchUserProfileFromBackend(context, token)
                onResult(_profileUiState.value.isVerified)
            }.addOnFailureListener {
                onResult(false)
            }
        }.addOnFailureListener {
            onResult(false)
        }
    }

    fun loadProfileIfAuthenticated(context: Context) {
        if (FirebaseAuth.getInstance().currentUser == null) {
            _profileUiState.value = _profileUiState.value.copy(isLoading = false)
            refreshLocalizedStrings(context)
            return
        }
        loadProfile(context)
    }

    private fun localizedLabels(context: Context): ProfileLabels {
        return ProfileLabels(
            title = context.getString(R.string.profile_title),
            accountDetails = context.getString(R.string.profile_account_details),
            firstName = context.getString(R.string.profile_first_name),
            surname = context.getString(R.string.profile_surname),
            email = context.getString(R.string.profile_email),
            mobile = context.getString(R.string.profile_mobile),
            state = context.getString(R.string.profile_state),
            city = context.getString(R.string.profile_city),
            saveChanges = context.getString(R.string.profile_save_changes),
            refreshStatus = "Refresh Status",
        )
    }
}
