package com.example.mealx.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

object PermissionUtils {

    fun hasCameraPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun hasStoragePermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_MEDIA_IMAGES
        ) == PackageManager.PERMISSION_GRANTED
    }

    @Composable
    fun rememberCameraPermissionState(): PermissionState {
        val context = LocalContext.current
        val permissionGranted = remember { mutableStateOf(hasCameraPermission(context)) }

        val launcher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            permissionGranted.value = isGranted
        }

        return PermissionState(
            isGranted = permissionGranted.value,
            onRequestPermission = { launcher.launch(Manifest.permission.CAMERA) }
        )
    }

    @Composable
    fun rememberStoragePermissionState(): PermissionState {
        val context = LocalContext.current
        val permissionGranted = remember { mutableStateOf(hasStoragePermission(context)) }

        val launcher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            permissionGranted.value = isGranted
        }

        // Request appropriate permission based on Android version
        val permissionToRequest = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        return PermissionState(
            isGranted = permissionGranted.value,
            onRequestPermission = { launcher.launch(permissionToRequest) }
        )
    }

    data class PermissionState(
        val isGranted: Boolean,
        val onRequestPermission: () -> Unit
    )
}

@Composable
fun RequestPermission(
    permission: String,
    onPermissionGranted: () -> Unit,
    onPermissionDenied: () -> Unit = {}
) {
    val context = LocalContext.current
    val permissionGranted = ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onPermissionGranted()
        } else {
            onPermissionDenied()
        }
    }

    SideEffect {
        if (!permissionGranted) {
            launcher.launch(permission)
        } else {
            onPermissionGranted()
        }
    }
}