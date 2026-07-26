package com.example.wattcalculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.wattcalculator.ui.screens.MainTabScreen
import com.example.wattcalculator.ui.theme.WattCalculatorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WattCalculatorTheme {
                MainTabScreen()
            }
        }
    }
}
