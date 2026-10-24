package com.example.dayoffset

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dayoffset.data.SharedPreferencesDataSource
import com.example.dayoffset.navigation.DayOffsetApp
import com.example.dayoffset.repository.DayOffsetRepository
import com.example.dayoffset.ui.DayOffsetViewModel
import com.example.dayoffset.ui.theme.DayOffsetTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val repository = remember {
                DayOffsetRepository(SharedPreferencesDataSource.create(applicationContext))
            }
            val dayOffsetViewModel: DayOffsetViewModel = viewModel(
                factory = DayOffsetViewModel.provideFactory(repository)
            )
            val uiState by dayOffsetViewModel.uiState.collectAsStateWithLifecycle()

            DayOffsetTheme(
                themeMode = uiState.settings.themeMode,
                dynamicColor = uiState.settings.dynamicColor
            ) {
                DayOffsetApp(viewModel = dayOffsetViewModel)
            }
        }
    }
}
