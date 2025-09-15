package com.example.snappydrawingapp

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.snappydrawingapp.draw.DrawingScreen
import com.example.snappydrawingapp.ui.theme.SnappyDrawingAppTheme

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SnappyDrawingAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { _ ->
                    DrawingScreen(Modifier.fillMaxSize())
                }
            }
        }
    }
}