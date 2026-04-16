package com.example.project2026

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.example.project2026.ui.NavigazioneApp

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
                // Avviamo il nostro navigatore
                NavigazioneApp()
        }
    }
}