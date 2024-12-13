package com.example.testsehat

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.testsehat.databinding.MainActivityBinding
import com.example.testsehat.fragment.ChatFragment
import com.example.testsehat.fragment.HomeFragment
import com.example.testsehat.fragment.ArticlesFragment
import com.example.testsehat.fragment.ProfileFragment

class MainActivity : AppCompatActivity() {
    private lateinit var binding: MainActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MainActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment())
                .commit()
        }

        setupBottomNavigation()
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            var selectedFragment: Fragment = HomeFragment()

            when (item.itemId) {
                R.id.nav_home -> selectedFragment = HomeFragment()
                R.id.nav_articles -> selectedFragment = ArticlesFragment()
                R.id.nav_chat -> selectedFragment = ChatFragment()
                R.id.nav_profile -> selectedFragment = ProfileFragment()
            }

            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, selectedFragment)
                .commit()

            true
        }
    }
}