package com.example.testsehat;

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.testsehat.LoginActivity
import com.example.testsehat.R
import com.example.testsehat.RetrofitClient
import com.example.testsehat.data.RegisterRequest
import com.example.testsehat.data.AuthResponse
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {
    private lateinit var etUsername: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var etConfirmPassword: TextInputEditText
    private lateinit var btnRegister: MaterialButton
    private lateinit var progressBar: ProgressBar
    private lateinit var tvLogin: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register_activity)

        initializeViews()

        setupClickListeners()
    }

    private fun initializeViews() {
        etUsername = findViewById(R.id.et_username)
        etEmail = findViewById(R.id.et_email)
        etPassword = findViewById(R.id.et_password)
        etConfirmPassword = findViewById(R.id.et_confirm_password)
        btnRegister = findViewById(R.id.btn_register)
        progressBar = findViewById(R.id.progress_bar)
        tvLogin = findViewById(R.id.tv_login)
    }

    private fun setupClickListeners() {
        btnRegister.setOnClickListener {
            if (validateInputs()) {
                performRegistration()
            }
        }

        tvLogin.setOnClickListener {
            navigateToLogin()
        }
    }

    private fun validateInputs(): Boolean {
        clearErrors()

        val username = etUsername.text.toString().trim()
        if (username.isEmpty()) {
            showError(etUsername, "Username cannot be empty")
            return false
        }

        val email = etEmail.text.toString().trim()
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError(etEmail, "Enter a valid email address")
            return false
        }

        val password = etPassword.text.toString()
        if (password.isEmpty()) {
            showError(etPassword, "Password cannot be empty")
            return false
        }
        if (password.length < 6) {
            showError(etPassword, "Password must be at least 6 characters")
            return false
        }

        val confirmPassword = etConfirmPassword.text.toString()
        if (confirmPassword.isEmpty()) {
            showError(etConfirmPassword, "Please confirm your password")
            return false
        }
        if (password != confirmPassword) {
            showError(etConfirmPassword, "Passwords do not match")
            return false
        }

        return true
    }

    private fun performRegistration() {
        progressBar.visibility = View.VISIBLE
        btnRegister.isEnabled = false

        val registerRequest = RegisterRequest(
            username = etUsername.text.toString().trim(),
            email = etEmail.text.toString().trim(),
            password = etPassword.text.toString(),
            role = "user"
        )

        RetrofitClient.instance.register(registerRequest)
            .enqueue(object : Callback<AuthResponse> {
                override fun onResponse(
                    call: Call<AuthResponse>,
                    response: Response<AuthResponse>
                ) {
                    progressBar.visibility = View.GONE
                    btnRegister.isEnabled = true

                    if (response.isSuccessful) {
                        val authResponse = response.body()
                        Toast.makeText(
                            this@RegisterActivity,
                            "Registration Successful!",
                            Toast.LENGTH_SHORT
                        ).show()

                        navigateToLogin()
                    } else {
                        val errorMessage = response.errorBody()?.string()
                            ?: "Registration failed. Please try again."
                        Toast.makeText(
                            this@RegisterActivity,
                            errorMessage,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                    progressBar.visibility = View.GONE
                    btnRegister.isEnabled = true
                    Toast.makeText(
                        this@RegisterActivity,
                        "Network error: ${t.localizedMessage}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun showError(editText: TextInputEditText, errorMessage: String) {
        val parent = editText.parent.parent as? TextInputLayout
        parent?.error = errorMessage
    }

    private fun clearErrors() {
        val inputLayouts = listOf(
            etUsername.parent.parent as TextInputLayout,
            etEmail.parent.parent as TextInputLayout,
            etPassword.parent.parent as TextInputLayout,
            etConfirmPassword.parent.parent as TextInputLayout
        )
        inputLayouts.forEach { it.error = null }
    }
}