package com.nursena.fenlab_android



import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class FenlabApplication : Application()



/*
import android.app.Application

// @HiltAndroidApp KALDIRILDI
// Mock data ile çalışırken Hilt gerekmiyor.
// Backend hazır olunca tekrar eklenecek.
class FenlabApplication : Application()
*/
