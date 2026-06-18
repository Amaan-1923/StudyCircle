package com.example.studycircle

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.studycircle.ui.auth.LoginScreen
import com.example.studycircle.ui.theme.StudyCircleTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StudyCircleTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { _ ->
                    LoginScreen(
                        onLoginSuccess = {
                            // We'll navigate to the home/feed screen here later
                        }
                    )
                }
            }
        }
    }
}