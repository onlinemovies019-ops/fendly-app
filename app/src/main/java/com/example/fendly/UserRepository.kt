package com.example.fendly

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class UserRepository(
    private val auth: FirebaseAuth? = null,
    private val firestore: FirebaseFirestore? = null,
) {
    private fun getAuth(): FirebaseAuth? = auth ?: runCatching { FirebaseAuth.getInstance() }
        .onFailure { exception ->
            Log.e("FirestoreProfile", "Error getting FirebaseAuth instance", exception)
        }
        .getOrNull()

    private fun getFirestore(): FirebaseFirestore? = firestore ?: runCatching { FirebaseFirestore.getInstance() }
        .onFailure { exception ->
            Log.e("FirestoreProfile", "Error getting Firestore instance", exception)
        }
        .getOrNull()

    private fun currentUserDocument(): Pair<FirebaseAuth, FirebaseFirestore>? {
        val firebaseAuth = getAuth()
        val firebaseFirestore = getFirestore()
        val currentUser = firebaseAuth?.currentUser

        if ((firebaseAuth == null) || (firebaseFirestore == null) || (currentUser == null)) {
            val error = IllegalStateException("No authenticated user available for Firestore profile access")
            Log.e("FirestoreProfile", "Error loading profile", error)
            return null
        }

        return firebaseAuth to firebaseFirestore
    }

    fun saveUserProfile(profile: UserProfile): Task<Void> {
        val authAndFirestore = currentUserDocument() ?: run {
            val error = IllegalStateException("No authenticated user available for profile save")
            Log.e("FirestoreProfile", "FAILED to save profile", error)
            return Tasks.forException(error)
        }

        val currentUser = authAndFirestore.first.currentUser ?: run {
            val error = IllegalStateException("No authenticated user available for profile save")
            Log.e("FirestoreProfile", "FAILED to save profile", error)
            return Tasks.forException(error)
        }

        return authAndFirestore.second.collection("users")
            .document(currentUser.uid)
            .set(profile.toFirestoreMap(), SetOptions.merge())
            .addOnSuccessListener {
                Log.d("FirestoreProfile", "Profile saved successfully to cloud!")
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreProfile", "FAILED to save profile", e)
            }
    }

    fun getUserProfile(): Task<UserProfile> {
        val authAndFirestore = currentUserDocument() ?: run {
            val error = IllegalStateException("No authenticated user available for profile load")
            Log.e("FirestoreProfile", "FAILED to fetch profile", error)
            return Tasks.forException(error)
        }

        val currentUser = authAndFirestore.first.currentUser ?: run {
            val error = IllegalStateException("No authenticated user available for profile load")
            Log.e("FirestoreProfile", "FAILED to fetch profile", error)
            return Tasks.forException(error)
        }

        return authAndFirestore.second.collection("users")
            .document(currentUser.uid)
            .get()
            .addOnSuccessListener { doc ->
                Log.d("FirestoreProfile", "Fetched doc data: ${doc.data}")
            }
            .addOnFailureListener { exception ->
                Log.e("FIREBASE_ERROR", "Data fetch failed: ", exception)
            }
            .continueWith { task ->
                if (!task.isSuccessful) {
                    throw task.exception ?: IllegalStateException("Profile fetch failed")
                }

                val document = task.result
                if ((document == null) || !document.exists()) {
                    return@continueWith UserProfile()
                }

                try {
                    val profile = document.toObject(UserProfile::class.java) ?: UserProfile()
                    val mob = document.getString("mobileNumber")
                        ?: document.getString("mobile")
                        ?: profile.mobileNumber
                    val mobVerified = document.getBoolean("isMobileVerified")
                        ?: document.getBoolean("mobileVerified")
                        ?: document.getBoolean("mobile_verified")
                        ?: profile.isMobileVerified
                    val emailVerified = document.getBoolean("isEmailVerified")
                        ?: document.getBoolean("emailVerified")
                        ?: document.getBoolean("email_verified")
                        ?: profile.isEmailVerified

                    return@continueWith profile.copy(
                        mobileNumber = mob,
                        isMobileVerified = mobVerified,
                        isEmailVerified = emailVerified,
                    )
                } catch (exception: Exception) {
                    Log.e("FirestoreProfile", "Unable to parse profile document", exception)
                    return@continueWith UserProfile()
                }
            }
    }

    @Suppress("unused")
    fun saveProfile(profile: UserProfile): Task<Void> = saveUserProfile(profile)

    @Suppress("unused")
    fun loadProfile(): Task<UserProfile> = getUserProfile()
}
