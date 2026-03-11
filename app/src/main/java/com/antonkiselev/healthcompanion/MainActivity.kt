package com.antonkiselev.healthcompanion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.antonkiselev.healthcompanion.ui.HealthCompassApp
import com.antonkiselev.healthcompanion.ui.HealthViewModel
import com.antonkiselev.healthcompanion.ui.theme.HealthCompassTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val viewModel: HealthViewModel = viewModel(factory = HealthViewModel.Factory(application))
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            HealthCompassTheme {
                HealthCompassApp(
                    uiState = uiState,
                    viewModel = viewModel,
                )
            }
        }
    }
}

