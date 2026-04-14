package tech.kaustubhdeshpande.sos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import tech.kaustubhdeshpande.sos.screens.SosScreen
import tech.kaustubhdeshpande.sos.ui.theme.SOSTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SOSTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    SosScreen()
                }
            }
        }
    }
}

