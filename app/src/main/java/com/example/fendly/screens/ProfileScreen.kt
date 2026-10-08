package com.example.fendly.screens

import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.fendly.ProfileViewModel
import com.example.fendly.ui.theme.FendlyPrimaryButton

@Suppress("unused")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = ProfileViewModel(),
    @Suppress("UNUSED_PARAMETER") onBack: () -> Unit = {},
) {
    val context = LocalContext.current
    val uiState by viewModel.profileUiState.collectAsState()
    val scrollState = rememberScrollState()

    val currentLocale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        context.resources.configuration.locales
    } else {
        @Suppress("DEPRECATION")
        context.resources.configuration.locale
    }



    // CORE FLICKER BUGFIX: Explicitly wrap inside TextFieldValue structures to lock composition selection bounds
    var firstNameState by remember { mutableStateOf(TextFieldValue(uiState.profile.name)) }
    var surnameState by remember { mutableStateOf(TextFieldValue(uiState.profile.surname)) }
    var emailState by remember { mutableStateOf(TextFieldValue(uiState.profile.email)) }
    var mobileState by remember { mutableStateOf(TextFieldValue(uiState.profile.mobileNumber)) }
    var stateState by remember { mutableStateOf(TextFieldValue(uiState.profile.state)) }
    var cityState by remember { mutableStateOf(TextFieldValue(uiState.profile.city)) }
    LaunchedEffect(currentLocale) {
        viewModel.loadProfileIfAuthenticated(context)
    }

    // Synchronize arriving cloud profile data when profile loads or updates
    LaunchedEffect(uiState.profile) {
        firstNameState = TextFieldValue(uiState.profile.name)
        surnameState = TextFieldValue(uiState.profile.surname)
        emailState = TextFieldValue(uiState.profile.email)
        mobileState = TextFieldValue(uiState.profile.mobileNumber)
        stateState = TextFieldValue(uiState.profile.state)
        cityState = TextFieldValue(uiState.profile.city)
    }

    val displayName = remember(uiState.profile.name, uiState.profile.surname) {
        val full = listOf(uiState.profile.name, uiState.profile.surname)
            .asSequence()
            .filter { it.isNotBlank() }
            .joinToString(" ")
        full.ifEmpty { uiState.labels?.title ?: "" }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.labels?.title ?: "",
                        style = MaterialTheme.typography.titleLarge.copy(
                            platformStyle = PlatformTextStyle(includeFontPadding = false),
                        ),
                    )
                },
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 520.dp)
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
            Text(
                text = displayName,
                style = MaterialTheme.typography.headlineMedium.copy(
                    platformStyle = PlatformTextStyle(includeFontPadding = false),
                ),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground,
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = uiState.labels?.accountDetails ?: "",
                style = MaterialTheme.typography.bodyMedium.copy(
                    platformStyle = PlatformTextStyle(includeFontPadding = false),
                ),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(28.dp))

            OutlinedTextField(
                value = firstNameState,
                onValueChange = { firstNameState = it },
                label = { Text(text = uiState.labels?.firstName ?: "") },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = surnameState,
                onValueChange = { surnameState = it },
                label = { Text(text = uiState.labels?.surname ?: "") },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = emailState,
                    onValueChange = { emailState = it },
                    label = { Text(text = uiState.labels?.email ?: "") },
                    enabled = !uiState.profile.isEmailVerified && !uiState.isVerified,
                    modifier = Modifier.weight(1f),
                )

                TextButton(
                    onClick = {
                        viewModel.checkServerVerification(context)
                    },
                ) {
                    Text(text = uiState.labels?.refreshStatus ?: "Refresh Status")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = mobileState,
                onValueChange = { newValue ->
                    val filteredText = newValue.text
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

                    val newSelectionStart = minOf(newValue.selection.start, filteredText.length)
                    val newSelectionEnd = minOf(newValue.selection.end, filteredText.length)

                    mobileState = newValue.copy(
                        text = filteredText,
                        selection = TextRange(newSelectionStart, newSelectionEnd),
                    )
                },
                label = { Text(text = uiState.labels?.mobile ?: "") },
                enabled = !uiState.profile.isMobileVerified,
                visualTransformation = VisualTransformation { text ->
                    TransformedText(
                        AnnotatedString(com.example.fendly.LanguageManager.localizedDigits(context, text.text)),
                        OffsetMapping.Identity,
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                ),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = stateState,
                onValueChange = { stateState = it },
                label = { Text(text = uiState.labels?.state ?: "") },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = cityState,
                onValueChange = { cityState = it },
                label = { Text(text = uiState.labels?.city ?: "") },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(32.dp))

            FendlyPrimaryButton(
                text = uiState.labels?.saveChanges ?: "",
                onClick = {
                    val updatedProfile = uiState.profile.copy(
                        name = firstNameState.text,
                        surname = surnameState.text,
                        email = emailState.text,
                        mobileNumber = mobileState.text,
                        state = stateState.text,
                        city = cityState.text,
                        isMobileVerified = if (mobileState.text != uiState.profile.mobileNumber) false else uiState.profile.isMobileVerified,
                    )
                    viewModel.saveProfile(updatedProfile)
                },
            )
        }
    }
}
}