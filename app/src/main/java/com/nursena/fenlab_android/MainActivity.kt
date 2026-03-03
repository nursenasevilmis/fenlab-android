package com.nursena.fenlab_android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.nursena.fenlab_android.ui.screens.home.HomeScreen
import com.nursena.fenlab_android.ui.theme.FenlabAndroidTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FenlabAndroidTheme {
                // Gerçek HomeScreen — ViewModel üzerinden backend'e bağlı
                HomeScreen(
                    onExperimentClick = { experimentId ->
                        // TODO: detay sayfasına navigasyon
                        // Örn: navController.navigate("detail/$experimentId")
                    }
                )
            }
        }
    }
}