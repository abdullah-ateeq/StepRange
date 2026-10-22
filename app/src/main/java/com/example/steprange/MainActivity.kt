package com.example.steprange

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.steprange.ui.StepRangeViewModel
import com.example.steprange.ui.navigation.StepRangeNavigation
import com.example.steprange.ui.theme.StepRangeTheme

class MainActivity : ComponentActivity() {

    private val viewModel: StepRangeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val uiState by viewModel.uiState.collectAsState()
            StepRangeTheme(
                appTheme = uiState.settings.theme,
                dynamicColor = uiState.settings.dynamicColor
            ) {
                StepRangeNavigation(
                    viewModel = viewModel,
                    uiState = uiState
                )
            }
        }
    }
}
