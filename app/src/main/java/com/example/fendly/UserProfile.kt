package com.example.fendly

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName

data class UserProfile(
    val name: String = "",
    val surname: String = "",
    val state: String = "",
    val city: String = "",
    @get:PropertyName("mobileNumber") @set:PropertyName("mobileNumber") var mobileNumber: String = "",
    val username: String = "",
    val email: String = "",
    val address: String = "",
    val age: Int = 0,
    @get:PropertyName("isMobileVerified") var isMobileVerified: Boolean = false,
    @get:PropertyName("isEmailVerified") var isEmailVerified: Boolean = false,
    val isAdmin: Boolean = false,
    val imageUrl: String? = null,
    @get:Exclude val uid: String? = null,
) {
    fun toFirestoreMap(): Map<String, Any> = mapOf(
        "name" to name,
        "surname" to surname,
        "state" to state,
        "city" to city,
        "mobile" to mobileNumber,
        "mobileNumber" to mobileNumber,
        "username" to username,
        "email" to email,
        "address" to address,
        "age" to age,
        "isMobileVerified" to isMobileVerified,
        "mobileVerified" to isMobileVerified,
        "mobile_verified" to isMobileVerified,
        "isEmailVerified" to isEmailVerified,
        "emailVerified" to isEmailVerified,
        "email_verified" to isEmailVerified,
        "isAdmin" to isAdmin,
        "imageUrl" to (imageUrl ?: ""),
    )
}
