package com.bangkit.mystory.ui.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.preferences.preferencesDataStore
import com.bangkit.mystory.R
import com.bangkit.mystory.data.local.UserPreferences
import com.bangkit.mystory.data.remote.api.ApiConfig
import com.bangkit.mystory.data.repository.UserRepository
import com.bangkit.mystory.databinding.ActivityLoginBinding
import com.bangkit.mystory.ui.ViewModelFactory
import com.bangkit.mystory.ui.main.MainActivity
import com.bangkit.mystory.ui.register.RegisterActivity

private val Context.dataStore by preferencesDataStore(name = "user_preferences")

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val loginViewModel: LoginViewModel by viewModels {
        ViewModelFactory(
            UserRepository.getInstance(
                ApiConfig.getApiService(),
                UserPreferences.getInstance(applicationContext.dataStore)
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupAction()
        observeViewModel()
    }

    private fun setupAction() {
        binding.btnLogin.setOnClickListener {
            val email = binding.edLoginEmail.text.toString().trim()
            val password = binding.edLoginPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, R.string.error_empty_fields, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            showLoading(true)
            loginViewModel.login(email, password)
        }

        binding.tvRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        binding.showPassword.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Tampilkan password
                binding.edLoginPassword.inputType =
                    android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                // Sembunyikan password
                binding.edLoginPassword.inputType =
                    android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            binding.edLoginPassword.setSelection(binding.edLoginPassword.text?.length ?: 0)
        }
    }

    private fun observeViewModel() {
        loginViewModel.isLoginSuccessful.observe(this) { isSuccessful ->
            if (isSuccessful) {
                showLoading(false)
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

        loginViewModel.errorMessage.observe(this) { errorMessage ->
            showLoading(false)
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}