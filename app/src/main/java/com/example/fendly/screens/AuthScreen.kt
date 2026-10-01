package com.example.fendly.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fendly.ui.theme.FendlyPrimaryButton
import com.example.fendly.ui.theme.FendlySecondaryButton

@Suppress("unused")
@Composable
fun AuthScreen(
    windowWidthSizeClass: WindowWidthSizeClass,
    onNavigateToSignup: () -> Unit,
    onNavigateToPinLogin: () -> Unit,
    onNavigateToAdminLogin: () -> Unit,
    onContinueWithoutLogin: () -> Unit = {},
) {
    val scrollState = rememberScrollState()
    val horizontalPadding = when (windowWidthSizeClass) {
        WindowWidthSizeClass.Compact -> 24.dp
        WindowWidthSizeClass.Medium -> 64.dp
        else -> 120.dp
    }
    val contentMaxWidth = when (windowWidthSizeClass) {
        WindowWidthSizeClass.Compact -> Modifier.fillMaxWidth()
        else -> Modifier.widthIn(max = 480.dp)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = horizontalPadding, vertical = 40.dp)
                .then(contentMaxWidth),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clickable { onNavigateToAdminLogin() }
                    .padding(8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text("🔍", fontSize = 24.sp)
            }

            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "Fendly",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "Help is right here—get started.",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(modifier = Modifier.height(24.dp))
            FendlyPrimaryButton(text = "Create my profile", onClick = onNavigateToSignup)
            Spacer(modifier = Modifier.height(16.dp))
            FendlySecondaryButton(text = "Login with PIN", onClick = onNavigateToPinLogin)
            Spacer(modifier = Modifier.height(20.dp))
            FendlySecondaryButton(
                text = "Scan QR / Found Item without Login",
                onClick = onContinueWithoutLogin,
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "New here? Create account",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.clickable { onNavigateToSignup() },
            )
        }
    }
}
