package com.example.videopager.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import com.example.videopager.R
import com.example.videopager.di.MainModule

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
