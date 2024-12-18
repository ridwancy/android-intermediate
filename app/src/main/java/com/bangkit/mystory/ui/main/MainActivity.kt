package com.bangkit.mystory.ui.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bangkit.mystory.R
import com.bangkit.mystory.databinding.ActivityMainBinding
import com.bangkit.mystory.ui.ViewModelFactory
import com.bangkit.mystory.ui.login.LoginActivity
import com.bangkit.mystory.ui.onboarding.WelcomeActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val mainViewModel by viewModels<MainViewModel> {
        ViewModelFactory.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewModel(savedInstanceState)
        setSupportActionBar(binding.voiceToolbar)
    }

    private fun setupViewModel(savedInstanceState: Bundle?) {
        mainViewModel.getLogin().observe(this) { user ->
            Log.d("MainActivity", "user: $user")
            if (user.isLogin) {
                binding.root.visibility = View.VISIBLE


            } else {
                startActivity(Intent(this, WelcomeActivity::class.java))
                finish()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                performLogout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun performLogout() {
        mainViewModel.deleteLogin()
        Toast.makeText(this, R.string.logout_succes, Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, WelcomeActivity::class.java))
        finish()
    }
}