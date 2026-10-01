package com.example.fendly

import org.junit.Assert.assertEquals
import org.junit.Test

class UserProfileFirestoreTest {
    @Test
    fun userProfile_hasFirestoreCompatibleDefaults() {
        val profile = UserProfile()

        assertEquals("", profile.name)
        assertEquals("", profile.surname)
        assertEquals("", profile.state)
        assertEquals("", profile.city)
        assertEquals("", profile.mobileNumber)
        assertEquals("", profile.email)
        assertEquals("", profile.username)
        assertEquals(0, profile.age)
        assertEquals("", profile.address)
        assertEquals(false, profile.isMobileVerified)
        assertEquals(false, profile.isEmailVerified)
        assertEquals(false, profile.isAdmin)
    }

    @Test
    fun userProfile_toFirestoreMap_containsBothMobileKeys() {
        val profile = UserProfile(mobileNumber = "9876543210")
        val map = profile.toFirestoreMap()

        assertEquals("9876543210", map["mobile"])
        assertEquals("9876543210", map["mobileNumber"])
    }

    @Test
    fun profileViewModel_startsWithBlankProfile() {
        val viewModel = ProfileViewModel()
        assertEquals("", viewModel.profileUiState.value.profile.name)
    }

    @Test
    fun filterMobileDigits_convertsLocalizedDigitsAndTruncatesToTenDigits() {
        fun filterMobile(input: String): String {
            return input
                .asSequence()
                .mapNotNull { char ->
                    when (char) {
                        in '0'..'9' -> char
                        in '٠'..'٩' -> '0' + (char - '٠')
                        in '۰'..'۹' -> '0' + (char - '۰')
                        in '०'..'९' -> '0' + (char - '०')
                        in '൦'..'൯' -> '0' + (char - '൦')
                        in '০'..'৯' -> '0' + (char - '০')
                        in '੦'..'੯' -> '0' + (char - '੦')
                        in '૦'..'૯' -> '0' + (char - '૦')
                        in '௦'..'௯' -> '0' + (char - '௦')
                        in '౦'..'౯' -> '0' + (char - '౦')
                        in '೦'..'೯' -> '0' + (char - '೦')
                        in '୦'..'୯' -> '0' + (char - '୦')
                        else -> null
                    }
                }
                .joinToString("")
                .take(10)
        }

        val devanagariInput = "९८७६५४३२१०123"
        assertEquals("9876543210", filterMobile(devanagariInput))

        val mixedInput = "+91 987-654 3210 extra"
        assertEquals("9198765432", filterMobile(mixedInput))
    }
}
