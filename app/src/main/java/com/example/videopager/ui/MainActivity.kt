package com.example.videopager.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import com.example.videopager.databinding.MainActivityBinding
import com.example.videopager.di.MainModule
import com.videopager.ui.VideoPagerFragment

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Manual dependency injection
        val module = MainModule(this)
        supportFragmentManager.fragmentFactory = module.fragmentFactory

        super.onCreate(savedInstanceState)
        val binding = MainActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                replace<VideoPagerFragment>(binding.fragmentContainer.id)
            }
        }
    }
}
