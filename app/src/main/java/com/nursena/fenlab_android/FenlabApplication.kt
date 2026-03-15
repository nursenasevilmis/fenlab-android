package com.nursena.fenlab_android

import android.app.Application
import coil.Coil
import coil.ImageLoader
import coil.util.DebugLogger
import dagger.hilt.android.HiltAndroidApp
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

@HiltAndroidApp
class FenlabApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        val imageLoader = ImageLoader.Builder(this)
            .okHttpClient {
                OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    // HTTP (cleartext) için Android güvenlik ayarlarını bypass et
                    .hostnameVerifier { _, _ -> true }
                    .build()
            }
            .logger(DebugLogger())   // geliştirme sırasında Coil loglarını görmek için
            .build()

        Coil.setImageLoader(imageLoader)
    }
}

/*
import android.app.Application

// @HiltAndroidApp KALDIRILDI
// Mock data ile çalışırken Hilt gerekmiyor.
// Backend hazır olunca tekrar eklenecek.
class FenlabApplication : Application()
*/
