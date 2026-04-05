package com.nursena.fenlab_android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.nursena.fenlab_android.core.datastore.TokenManager
import com.nursena.fenlab_android.ui.navigation.FenlabNavGraph
import com.nursena.fenlab_android.ui.theme.FenlabAndroidTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Token kontrolü yalnızca Activity ilk yaratıldığında (cold start) yapılır.
        // Uygulama arka plana alınıp geri geldiğinde onCreate tekrar çağrılmaz,
        // dolayısıyla splash sadece gerçek ilk açılışta gösterilir.
        val isLoggedIn = runBlocking { tokenManager.isLoggedIn() }

        setContent {
            FenlabAndroidTheme {
                FenlabNavGraph(showSplash = true, initiallyLoggedIn = isLoggedIn)
            }
        }
    }
}
