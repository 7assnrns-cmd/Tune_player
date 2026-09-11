package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.MainMusicApp
import com.example.ui.theme.MusicPlayerTheme
import com.example.ui.viewmodel.MusicViewModel
import com.example.ui.viewmodel.MusicViewModelFactory

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as MusicApplication
        val viewModelFactory = MusicViewModelFactory(
            repository = app.repository,
            playerManager = app.playerManager,
            dataStoreManager = app.dataStoreManager
        )

        setContent {
            val viewModel: MusicViewModel = viewModel(factory = viewModelFactory)
            val themeMode by viewModel.themeMode.collectAsState()
            val dynamicColor by viewModel.dynamicColor.collectAsState()

            // Request permission for local media
            RequestMediaPermission(onPermissionGranted = {
                viewModel.rescanLibrary()
            })

            MusicPlayerTheme(
                themeMode = themeMode,
                dynamicColor = dynamicColor
            ) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MainMusicApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun RequestMediaPermission(onPermissionGranted: () -> Unit) {
    val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onPermissionGranted()
        }
    }

    LaunchedEffect(Unit) {
        launcher.launch(permission)
    }
}
