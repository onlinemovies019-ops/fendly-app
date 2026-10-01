package com.example.fendly

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AdminViewModelTest {
    @Test
    fun verifyAdminCredentials_validCredentials_returnsTrue() {
        val viewModel = AdminViewModel()
        assertTrue(viewModel.verifyAdminCredentials("AlcaBurg", "9055"))
    }

    @Test
    fun verifyAdminCredentials_invalidCredentials_returnsFalse() {
        val viewModel = AdminViewModel()
        assertFalse(viewModel.verifyAdminCredentials("wrongadmin", "9055"))
        assertFalse(viewModel.verifyAdminCredentials("AlcaBurg", "0000"))
        assertFalse(viewModel.verifyAdminCredentials("other", "9999"))
    }
}
