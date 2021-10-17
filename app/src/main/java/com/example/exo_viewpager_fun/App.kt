package com.example.exo_viewpager_fun

import android.app.Application
import androidx.annotation.VisibleForTesting

class App : Application() {
    @VisibleForTesting
    var mainModule: MainModule = DefaultMainModule(this)
}
