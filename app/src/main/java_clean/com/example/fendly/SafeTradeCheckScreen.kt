package com.example.fendly

import android.Manifest
import android.content.pm.PackageManager
import android.util.Size
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import com.example.fendly.ui.theme.FendlyCard
import com.example.fendly.ui.theme.FendlySecondaryButton
import com.example.fendly.ui.theme.FendlyTextField
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

private const val IMEI_DISCLAIMER =
    "SafeTrade checks device IMEIs against reported lost/stolen databases to protect buyers. By searching, you agree to our terms."

data class ImeiVerificationResponse(
    val status: String,
    val message: String,
    val is_flagged: Boolean,
)

private interface SafeTradeApi {
    @GET("api/v1/imei/verify/{imei_number}")
    fun verifyImei(@Path("imei_number") imeiNumber: String): Call<ImeiVerificationResponse>
}

private object SafeTradeApiClient {
    val api: SafeTradeApi by lazy {
        Retrofit.Builder()
            .baseUrl("${BuildConfig.API_BASE_URL.trimEnd('/')}/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SafeTradeApi::class.java)
    }
}

private enum class VerificationState {
    IDLE,
    LOADING,
    CLEAN,
    FLAGGED,
    ERROR,
}

@Composable
fun SafeTradeCheckScreen(onBack: () -> Unit, darkMode: Boolean) {
    var imei by rememberSaveable { mutableStateOf("") }
    var state by rememberSaveable { mutableStateOf(VerificationState.IDLE) }
    var responseMessage by rememberSaveable { mutableStateOf("") }
    var scanning by rememberSaveable { mutableStateOf(false) }
    var cameraPermissionDenied by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    val hasCameraPermission = remember {
        ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED
    }
    var cameraGranted by remember { mutableStateOf(hasCameraPermission) }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        cameraGranted = granted
        cameraPermissionDenied = !granted
        scanning = granted
    }

    if (scanning && cameraGranted) {
        ImeiCameraScanner(
            onImeiDetected = { detected ->
                imei = detected
                state = VerificationState.IDLE
                responseMessage = ""
                scanning = false
            },
            onClose = { scanning = false },
            onScannerError = {
                scanning = false
                state = VerificationState.ERROR
                responseMessage = "The barcode scanner could not start. Enter the IMEI manually."
            },
        )
        return
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        color = MaterialTheme.colorScheme.background,
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                ) {
                    Text(
                        text = "SafeTrade IMEI check",
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.Center),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    TextButton(
                        onClick = onBack,
                        modifier = Modifier.align(Alignment.CenterStart),
                    ) {
                        Text("Back")
                    }
                }
            }
        ) { contentPadding ->
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopCenter,
            ) {
                Column(
                    modifier = Modifier
                        .widthIn(max = 640.dp)
                        .fillMaxWidth()
                        .fillMaxSize()
                        .padding(contentPadding)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 22.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Check before you buy",
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = "Enter the 15-digit IMEI shown on the device or its box.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                FendlyCard(modifier = Modifier.fillMaxWidth()) {
                    FendlyTextField(
                        value = imei,
                        onValueChange = { value ->
                            imei = value.filter { it in '0'..'9' }.take(15)
                            state = VerificationState.IDLE
                            responseMessage = ""
                        },
                        label = "15-digit IMEI",
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        Text(
                            text = "${imei.length}/15 digits",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                FendlyCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "ABOUT THIS CHECK",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = IMEI_DISCLAIMER,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                FendlySecondaryButton(
                    text = "Scan IMEI barcode",
                    onClick = {
                        if (cameraGranted) {
                            scanning = true
                        } else {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    },
                )

                if (cameraPermissionDenied) {
                    Text(
                        text = "Camera access was denied. You can still enter the IMEI manually.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }

                Button(
                    onClick = {
                        state = VerificationState.LOADING
                        responseMessage = ""
                        SafeTradeApiClient.api.verifyImei(imei).enqueue(
                            object : Callback<ImeiVerificationResponse> {
                                override fun onResponse(
                                    call: Call<ImeiVerificationResponse>,
                                    response: Response<ImeiVerificationResponse>,
                                ) {
                                    val result = response.body()
                                    if (response.isSuccessful && result != null) {
                                        state = when {
                                            result.is_flagged -> VerificationState.FLAGGED
                                            result.status == "CLEAN" -> VerificationState.CLEAN
                                            else -> VerificationState.ERROR
                                        }
                                        responseMessage = if (state == VerificationState.ERROR) {
                                            "The verification service returned an unexpected result."
                                        } else {
                                            result.message
                                        }
                                    } else {
                                        state = VerificationState.ERROR
                                        responseMessage = when (response.code()) {
                                            429 -> "Too many checks. Please wait a moment and try again."
                                            400 -> "Enter a valid 15-digit IMEI."
                                            else -> "Could not verify this IMEI right now. Please try again."
                                        }
                                    }
                                }

                                override fun onFailure(
                                    call: Call<ImeiVerificationResponse>,
                                    error: Throwable,
                                ) {
                                    state = VerificationState.ERROR
                                    responseMessage =
                                        "Could not connect to SafeTrade. Check your connection and try again."
                                }
                            },
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    enabled = imei.length == 15 && state != VerificationState.LOADING,
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                ) {
                    if (state == VerificationState.LOADING) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text("Verify IMEI")
                    }
                }

                when (state) {
                    VerificationState.CLEAN -> VerificationBanner(
                        message = responseMessage.ifBlank {
                            "No active loss reports were found for this device."
                        },
                        background = if (darkMode) Color(0xFF18352B) else Color(0xFFE3F4E8),
                        foreground = if (darkMode) Color(0xFFB9E6D0) else Color(0xFF14532D),
                    )
                    VerificationState.FLAGGED -> VerificationBanner(
                        message = "This device is currently reported missing. Do not complete purchase.",
                        background = if (darkMode) Color(0xFF452522) else Color(0xFFFFE8E6),
                        foreground = if (darkMode) Color(0xFFFFC5BE) else Color(0xFF8B1E18),
                    )
                    VerificationState.ERROR -> VerificationBanner(
                        message = responseMessage,
                        background = if (darkMode) Color(0xFF42351E) else Color(0xFFFFF1D6),
                        foreground = if (darkMode) Color(0xFFFFD88A) else Color(0xFF6D4600),
                    )
                    VerificationState.IDLE,
                    VerificationState.LOADING -> Unit
                }
                }
            }
        }
    }
}

@Composable
private fun VerificationBanner(
    message: String,
    background: Color,
    foreground: Color,
) {
    Text(
        text = message,
        modifier = Modifier
            .fillMaxWidth()
            .background(background, RoundedCornerShape(12.dp))
            .padding(16.dp),
        color = foreground,
        fontWeight = FontWeight.SemiBold,
        style = MaterialTheme.typography.bodyMedium,
    )
}

@Composable
private fun ImeiCameraScanner(
    onImeiDetected: (String) -> Unit,
    onClose: () -> Unit,
    onScannerError: () -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scannerOptions = remember {
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
            .build()
    }
    val barcodeScanner = remember(scannerOptions) { BarcodeScanning.getClient(scannerOptions) }
    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }
    var previewView by remember { mutableStateOf<PreviewView?>(null) }
    val detected = remember { AtomicBoolean(false) }

    DisposableEffect(previewView, lifecycleOwner, barcodeScanner) {
        val view = previewView
        if (view == null) {
            onDispose { }
        } else {
            val providerFuture = ProcessCameraProvider.getInstance(context)
            providerFuture.addListener(
                {
                    try {
                        val cameraProvider = providerFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(view.surfaceProvider)
                        }
                        val analysis = ImageAnalysis.Builder()
                            .setTargetResolution(Size(1280, 720))
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()
                        analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                            val mediaImage = imageProxy.image
                            if (mediaImage == null || detected.get()) {
                                imageProxy.close()
                            } else {
                                barcodeScanner.process(
                                    InputImage.fromMediaImage(
                                        mediaImage,
                                        imageProxy.imageInfo.rotationDegrees,
                                    ),
                                ).addOnSuccessListener { barcodes ->
                                    if (detected.compareAndSet(false, true)) {
                                        val candidate = barcodes
                                            .asSequence()
                                            .mapNotNull { it.rawValue }
                                            .mapNotNull { Regex("[0-9]{15}").find(it)?.value }
                                            .firstOrNull()
                                        if (candidate != null) {
                                            onImeiDetected(candidate)
                                        } else {
                                            detected.set(false)
                                        }
                                    }
                                }.addOnFailureListener {
                                    detected.set(false)
                                }.addOnCompleteListener {
                                    imageProxy.close()
                                }
                            }
                        }
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            analysis,
                        )
                    } catch (_: Exception) {
                        onScannerError()
                    }
                },
                ContextCompat.getMainExecutor(context),
            )

            onDispose {
                if (providerFuture.isDone) {
                    runCatching { providerFuture.get().unbindAll() }
                }
                barcodeScanner.close()
                cameraExecutor.shutdown()
            }
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
        Box(modifier = Modifier.fillMaxSize()) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { viewContext ->
                    PreviewView(viewContext).also { view ->
                        view.scaleType = PreviewView.ScaleType.FILL_CENTER
                        previewView = view
                    }
                },
            )
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .background(Color(0x99000000))
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("Scan device IMEI", color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Hold a 15-digit barcode inside the camera view.", color = Color.White)
                TextButton(onClick = onClose) { Text("Cancel", color = Color.White) }
            }
        }
    }
}
