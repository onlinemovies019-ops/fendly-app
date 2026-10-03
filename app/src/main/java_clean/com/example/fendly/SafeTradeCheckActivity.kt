package com.example.fendly

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class SafeTradeCheckActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SafeTradeCheckScreen(onBack = { finish() })
        }
    }
}
