package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.example.ui.screens.AcademyMainScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AcademyViewModel
import com.example.ui.viewmodel.AcademyViewModelFactory

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Access the repository from the Application instance
        val app = application as CyberAcademyApplication
        val repository = app.repository

        // Instantiate our AcademyViewModel using our factory
        val viewModel: AcademyViewModel by viewModels {
            AcademyViewModelFactory(app, repository)
        }

        setContent {
            MyApplicationTheme {
                AcademyMainScreen(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

