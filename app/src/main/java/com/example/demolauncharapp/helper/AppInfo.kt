package com.example.demolauncharapp.helper

import android.graphics.drawable.Drawable

data class AppInfo(
    val packageName: String,
    val label: String,
    val icon: Drawable,
    val dominantColor: Int = 0xFFA500.toInt() // Default orange color
)