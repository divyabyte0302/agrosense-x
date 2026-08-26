package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.screens.AgroSenseApp
import com.example.ui.viewmodel.AgroViewModel

class MainActivity : ComponentActivity() {
  private val viewModel: AgroViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      AgroSenseApp(viewModel = viewModel)
    }
  }
}
