package com.example.exo_viewpager_fun

import android.app.Application
import androidx.annotation.VisibleForTesting
import com.example.exo_viewpager_fun.di.DefaultMainModule
import com.example.exo_viewpager_fun.di.MainModule

class App : Application() {
    @VisibleForTesting
    var mainModule: MainModule = DefaultMainModule(this)
}
