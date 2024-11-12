package com.minichain.minicamera

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.minichain.minicamera.ui.theme.MiniCameraTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    Log.d("MAIN_ACTIVITY", "Main Activity onCreate")
    super.onCreate(savedInstanceState)
    startMainService()
    val mainViewModel = MainViewModel(applicationContext)
    enableEdgeToEdge()
    setContent {
      MiniCameraTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
          CameraPreview(
            modifier = Modifier.padding(innerPadding),
            viewModel = mainViewModel
          )
        }
      }
    }
  }

  private fun startMainService() {
    Intent(this, MainService::class.java).also {
      it.action = MainService.MainServiceAction.Start.toString()
      startForegroundService(it)
    }
  }
}
