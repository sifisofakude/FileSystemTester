package com.slambyte.tester.filesystem


import android.os.Bundle
import android.content.Intent
import androidx.ativity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

import com.slambyte.tester.filesystem.ui.composable.*

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?)  {
    super.onCreate(savedInstanceState)
    setContent  {
      FileManagerApp()
    }
  }
}
