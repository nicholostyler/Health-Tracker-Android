package com.nicholostyler.healthtracker

import android.app.Activity
import android.util.Size
import android.view.WindowMetrics
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.toComposeRect
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.window.layout.WindowMetricsCalculator

enum class WindowSize {
    Compact,
    Medium,
    Expanded
}

@Composable
fun Activity.rememberWindowSizeClass():WindowSize {
    val windowSize = rememberWindowSize()
    val windowDpSize = with(LocalDensity.current){
        windowSize.toDpSize()
    }
    
    return getWindowSizeClass(windowSizeDpSize = windowDpSize)
}

@Composable
fun Activity.rememberWindowSize(): androidx.compose.ui.geometry.Size {
    val configuration = LocalConfiguration.current

    val windowMetric = remember(configuration) {
        WindowMetricsCalculator.getOrCreate().computeCurrentWindowMetrics(this)
    }

    return windowMetric.bounds.toComposeRect().size
}

@Composable
fun getWindowSizeClass(windowSizeDpSize: DpSize) = when {
    windowSizeDpSize.width < 0.dp -> {
        throw IllegalArgumentException("Dp values can not be negative")
    }
    windowSizeDpSize.width < 600.dp -> {
        WindowSize.Compact
    }
    windowSizeDpSize.width < 840.dp -> {
        WindowSize.Medium
    }
    else -> {
        WindowSize.Expanded
    }
}