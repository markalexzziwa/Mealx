package com.example.mealx.ui.screens

import android.Manifest
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mealx.ui.screens.viewmodels.ScanViewModel
import com.example.mealx.utils.PermissionUtils
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(
    scanViewModel: ScanViewModel = viewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Collect state from ViewModel
    val scanState by scanViewModel.scanState.collectAsStateWithLifecycle()
    val scanResult by scanViewModel.scanResult.collectAsStateWithLifecycle()

    // Permission state
    val cameraPermissionState = PermissionUtils.rememberCameraPermissionState()

    // Local UI state
    var showResultDialog by remember { mutableStateOf(false) }
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }

    // Update ViewModel with permission state
    LaunchedEffect(cameraPermissionState.isGranted) {
        scanViewModel.setCameraPermission(cameraPermissionState.isGranted)
    }

    // Camera setup
    LaunchedEffect(cameraPermissionState.isGranted) {
        if (cameraPermissionState.isGranted) {
            try {
                val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                cameraProviderFuture.addListener({
                    cameraProvider = cameraProviderFuture.get()
                    scanViewModel.setScanning(true)
                    scanViewModel.setCameraInitialized(true)
                }, ContextCompat.getMainExecutor(context))
            } catch (e: Exception) {
                Log.e("ScanScreen", "Camera initialization failed", e)
            }
        }
    }

    // Show result dialog when scan result changes
    LaunchedEffect(scanResult) {
        if (scanResult != null) {
            showResultDialog = true
        }
    }

    // Clean up when composable leaves composition
    DisposableEffect(Unit) {
        onDispose {
            scanViewModel.setScanning(false)
        }
    }

    // Scan result dialog
    if (showResultDialog && scanResult != null) {
        AlertDialog(
            onDismissRequest = {
                showResultDialog = false
                scanViewModel.clearScanResult()
                scanViewModel.setScanning(true)
            },
            title = {
                Text(
                    text = "Scan Successful!",
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Column {
                    Text(
                        text = "Scanned Content:",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text(
                            text = scanViewModel.formatBarcodeData(scanResult!!),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                    Text(
                        text = "Scan Count: ${scanState.scanCount}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Camera preview active - Test mode",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showResultDialog = false
                        scanViewModel.clearScanResult()
                        scanViewModel.setScanning(true)
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Scan Again")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showResultDialog = false
                        scanViewModel.clearScanResult()
                        scanViewModel.setScanning(false)
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Close")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Scan")
                        if (scanState.scanCount > 0) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Badge(
                                containerColor = MaterialTheme.colorScheme.primary
                            ) {
                                Text(
                                    text = "${scanState.scanCount}",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            scanViewModel.setScanning(!scanState.isScanning)
                            if (scanState.isScanning) {
                                scanViewModel.clearScanResult()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (scanState.isScanning) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (scanState.isScanning) "Pause Scanning" else "Resume Scanning"
                        )
                    }
                    IconButton(
                        onClick = {
                            scanViewModel.resetScanState()
                        }
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Reset Scanner"
                        )
                    }
                    // Manual test scan button
                    IconButton(
                        onClick = {
                            if (scanState.isScanning) {
                                // Simulate different types of scans
                                val testScans = listOf(
                                    "https://example.com/product-123",
                                    "9781234567890", // ISBN barcode
                                    "DATA-MATRIX-TEST-123",
                                    "PDF417|TEST|DATA|HERE"
                                )
                                val randomScan = testScans.random()
                                scanViewModel.updateScanResult(randomScan)
                            }
                        }
                    ) {
                        Icon(
                            Icons.Default.QrCode,
                            contentDescription = "Test Scan"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!cameraPermissionState.isGranted) {
                // Permission request UI
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Camera,
                        contentDescription = "Camera",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Camera Access Required",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "To scan barcodes and QR codes, we need access to your camera",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { cameraPermissionState.onRequestPermission() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary,
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Grant Camera Permission")
                    }
                }
            } else if (!scanState.isScanning) {
                // Paused state
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Pause,
                        contentDescription = "Scanning Paused",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Scanning Paused",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tap the play button to resume scanning",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Total scans: ${scanState.scanCount}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // Camera preview with scanning overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                ) {
                    // Camera preview
                    CameraPreview(
                        modifier = Modifier.fillMaxSize(),
                        cameraProvider = cameraProvider
                    )

                    // Scanning overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp)
                    ) {
                        // Scanning frame
                        Box(
                            modifier = Modifier
                                .size(250.dp)
                                .align(Alignment.Center)
                        ) {
                            // Top border
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(2.dp)
                                    .align(Alignment.TopStart)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                            // Bottom border
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(2.dp)
                                    .align(Alignment.BottomStart)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                            // Left border
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(2.dp)
                                    .align(Alignment.TopStart)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                            // Right border
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(2.dp)
                                    .align(Alignment.TopEnd)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                        }

                        // Corner indicators
                        Box(
                            modifier = Modifier
                                .size(250.dp)
                                .align(Alignment.Center)
                        ) {
                            // Top-left corner
                            Box(
                                modifier = Modifier
                                    .size(24.dp, 4.dp)
                                    .align(Alignment.TopStart)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                            Box(
                                modifier = Modifier
                                    .size(4.dp, 24.dp)
                                    .align(Alignment.TopStart)
                                    .background(MaterialTheme.colorScheme.primary)
                            )

                            // Top-right corner
                            Box(
                                modifier = Modifier
                                    .size(24.dp, 4.dp)
                                    .align(Alignment.TopEnd)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                            Box(
                                modifier = Modifier
                                    .size(4.dp, 24.dp)
                                    .align(Alignment.TopEnd)
                                    .background(MaterialTheme.colorScheme.primary)
                            )

                            // Bottom-left corner
                            Box(
                                modifier = Modifier
                                    .size(24.dp, 4.dp)
                                    .align(Alignment.BottomStart)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                            Box(
                                modifier = Modifier
                                    .size(4.dp, 24.dp)
                                    .align(Alignment.BottomStart)
                                    .background(MaterialTheme.colorScheme.primary)
                            )

                            // Bottom-right corner
                            Box(
                                modifier = Modifier
                                    .size(24.dp, 4.dp)
                                    .align(Alignment.BottomEnd)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                            Box(
                                modifier = Modifier
                                    .size(4.dp, 24.dp)
                                    .align(Alignment.BottomEnd)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                        }

                        // Instructions
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Position barcode/QR code within the frame",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color.White,
                                    fontSize = 16.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Camera preview active",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Use test scan button to simulate",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color.White.copy(alpha = 0.5f)
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    cameraProvider: ProcessCameraProvider?
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    AndroidView(
        factory = { ctx ->
            PreviewView(ctx).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            }
        },
        modifier = modifier,
        update = { previewView ->
            cameraProvider?.let { provider ->
                try {
                    // Unbind existing use cases
                    provider.unbindAll()

                    // Preview use case
                    val preview = Preview.Builder().build()
                    preview.setSurfaceProvider(previewView.surfaceProvider)

                    // Image analysis use case (simplified for compatibility)
                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()

                    val executor = Executors.newSingleThreadExecutor()
                    imageAnalysis.setAnalyzer(executor) { imageProxy ->
                        // Simplified - just close the image proxy
                        imageProxy.close()
                    }

                    // Camera selector
                    val cameraSelector = CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build()

                    // Bind use cases to lifecycle
                    provider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalysis
                    )
                } catch (e: Exception) {
                    Log.e("CameraPreview", "Camera setup failed", e)
                }
            }
        }
    )
}