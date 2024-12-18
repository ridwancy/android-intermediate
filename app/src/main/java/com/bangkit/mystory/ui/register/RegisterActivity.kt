package com.bangkit.mystory.ui.register

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bangkit.mystory.R
import com.bangkit.mystory.data.local.UserPreferences
import com.bangkit.mystory.data.remote.api.ApiConfig
import com.bangkit.mystory.data.repository.UserRepository
import com.bangkit.mystory.databinding.ActivityRegisterBinding
import com.bangkit.mystory.ui.ViewModelFactory
import com.bangkit.mystory.ui.login.LoginActivity
import androidx.datastore.preferences.preferencesDataStore

private val Context.dataStore by preferencesDataStore(name = "user_preferences")

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val registerViewModel: RegisterViewModel by viewModels {
        ViewModelFactory(
            UserRepository.getInstance(
                ApiConfig.getApiService(),
                UserPreferences.getInstance(dataStore)
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupAction()
        observeViewModel()
    }

    private fun setupAction() {
        binding.btnRegister.setOnClickListener {
            val name = binding.edRegisterName.text.toString().trim()
            val email = binding.edLoginEmail.text.toString().trim()
            val password = binding.edLoginPassword.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, R.string.error_empty_fields, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            showLoading(true)
            registerViewModel.register(name, email, password)
        }

        binding.tvLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
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
        registerViewModel.isSuccess.observe(this) { isSuccess ->
            if (isSuccess) {
                showLoading(false)
                Toast.makeText(this, R.string.success_register, Toast.LENGTH_SHORT).show()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

        registerViewModel.errorMessage.observe(this) { errorMessage ->
            showLoading(false)
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}