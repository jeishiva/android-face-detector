package com.experiment.facedetector

import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat

@Composable
fun RequestPermission(
    permissions: List<String>,
    onResult: ((Boolean) -> Unit)
) {
    var launched by remember { mutableStateOf(false) }
    val currentOnResult by rememberUpdatedState(onResult)
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val allGranted = results.values.all { it }
        currentOnResult(allGranted)
    }
    LaunchedEffect(Unit) {
        if (!launched) {
            permissionLauncher.launch(permissions.toTypedArray())
            launched = true
        }
    }
}

fun checkPermissions(context: Context, permissions: List<String>): Boolean {
    return permissions.all { permission ->
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }
}