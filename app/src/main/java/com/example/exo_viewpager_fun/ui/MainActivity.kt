package com.example.exo_viewpager_fun.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import com.example.exo_viewpager_fun.R
import com.example.exo_viewpager_fun.di.MainModule

class MainActivity : AppCompatActivity(R.layout.main_activity) {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Manual dependency injection
        val module = MainModule(this)
        supportFragmentManager.fragmentFactory = module.fragmentFactory
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                replace<MainFragment>(R.id.fragment_container)
            }
        }
    }
}
