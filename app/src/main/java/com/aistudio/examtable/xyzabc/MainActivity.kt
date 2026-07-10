package com.aistudio.examtable.xyzabc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aistudio.examtable.xyzabc.ui.screens.MainScreen
import com.aistudio.examtable.xyzabc.ui.theme.MyApplicationTheme
import com.aistudio.examtable.xyzabc.viewmodel.TableViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val tableViewModel: TableViewModel = viewModel()
            val isDarkMode by tableViewModel.isDarkMode.collectAsState()

            MyApplicationTheme(darkTheme = isDarkMode) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MainScreen(viewModel = tableViewModel)
                }
            }
        }
    }
}
