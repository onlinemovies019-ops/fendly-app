package com.example.fendly

import androidx.lifecycle.ViewModel

class AdminViewModel : ViewModel() {
    companion object {
        const val ADMIN_NAME = "AlcaBurg"
        const val ADMIN_PIN = "9055"
    }

    fun verifyAdminCredentials(enteredName: String, enteredPin: String): Boolean {
        return (enteredName.trim() == ADMIN_NAME) && (enteredPin.trim() == ADMIN_PIN)
    }
}
