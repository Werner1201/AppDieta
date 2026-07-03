package com.romling.diettracker

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun DietTrackerApp() {
    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF07100D))
                .padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Dieta Local",
                color = Color(0xFFF7F7F2),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black,
            )
        }
    }
}
