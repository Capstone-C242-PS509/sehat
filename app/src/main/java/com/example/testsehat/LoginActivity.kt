package com.example.testsehat

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.testsehat.data.LoginRequest
import com.example.testsehat.databinding.LoginActivityBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.awaitResponse

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: LoginActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LoginActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (AuthManager.isLoggedIn(this)) {
            navigateToMainActivity()
            return
        }

        setupListeners()
    }

    private fun setupListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (validateInputs(email, password)) {
                performLogin(email, password)
            }
        }

        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun validateInputs(email: String, password: String): Boolean {
        return when {
            email.isEmpty() -> {
                binding.etEmail.error = "Email cannot be empty"
                false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                binding.etEmail.error = "Invalid email format"
                false
            }
            password.isEmpty() -> {
                binding.etPassword.error = "Password cannot be empty"
                false
            }
            password.length < 6 -> {
                binding.etPassword.error = "Password must be at least 6 characters"
                false
            }
            else -> true
        }
    }

    private fun performLogin(email: String, password: String) {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnLogin.isEnabled = false

        lifecycleScope.launch {
            try {
                val loginRequest = LoginRequest(email, password)
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.instance.login(loginRequest).awaitResponse()
                }

                binding.progressBar.visibility = View.GONE
                binding.btnLogin.isEnabled = true

                if (response.isSuccessful) {
                    response.body()?.let { authResponse ->
                        AuthManager.saveAuthDetails(
                            this@LoginActivity,
                            authResponse.data.token,
                            email
                        )

                        Toast.makeText(this@LoginActivity, "Login Successful", Toast.LENGTH_SHORT).show()
                        navigateToMainActivity()
                    }
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Login failed"
                    Toast.makeText(this@LoginActivity, errorMessage, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                binding.btnLogin.isEnabled = true
                Toast.makeText(this@LoginActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                println(e.message)
            }
        }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}