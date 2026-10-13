package com.whoseturn.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import com.whoseturn.app.R
import com.whoseturn.app.data.api.ApiClient
import com.whoseturn.app.data.api.TokenManager
import com.whoseturn.app.data.model.LoginRequest
import com.whoseturn.app.databinding.ActivityLoginBinding
import com.whoseturn.app.ui.main.MainActivity
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var tokenManager: TokenManager

    override fun attachBaseContext(newBase: android.content.Context) {
        super.attachBaseContext(com.whoseturn.app.util.LocaleHelper.onAttach(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        com.whoseturn.app.util.ThemeHelper.applyTheme(this)
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager(this)

        if (tokenManager.isLoggedIn()) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        binding.tvCurrentServer.text = "Server: ${tokenManager.getBaseUrl()}"

        binding.btnServerSettings.setOnClickListener {
            showServerConfigDialog()
        }

        binding.btnLogin.setOnClickListener {
            performLogin()
        }

        binding.tvRegisterLink.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun performLogin() {
        val username = binding.etUsername.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (username.isEmpty()) {
            binding.tilUsername.error = "Foydalanuvchi nomini kiriting"
            return
        }
        binding.tilUsername.error = null

        if (password.isEmpty()) {
            binding.tilPassword.error = "Parolni kiriting"
            return
        }
        binding.tilPassword.error = null

        binding.progressBar.visibility = View.VISIBLE
        binding.btnLogin.isEnabled = false

        lifecycleScope.launch {
            try {
                val api = ApiClient.getService(this@LoginActivity)
                val response = api.login(LoginRequest(username, password))

                if (response.isSuccessful && response.body() != null) {
                    val auth = response.body()!!
                    tokenManager.saveTokens(auth.access, auth.refresh)
                    tokenManager.saveUsername(username)

                    Toast.makeText(this@LoginActivity, "Xush kelibsiz!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finish()
                } else {
                    val errorMsg = if (response.code() == 401) {
                        "Login yoki parol xato kiritildi!"
                    } else {
                        "Xatolik yuz berdi: ${response.code()}"
                    }
                    Toast.makeText(this@LoginActivity, errorMsg, Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@LoginActivity,
                    "Serverga ulanib bo'lmadi: ${e.localizedMessage ?: "Noma'lum xatolik"}\nServer manzilini sozlamalardan tekshiring.",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                binding.progressBar.visibility = View.GONE
                binding.btnLogin.isEnabled = true
            }
        }
    }

    private fun showServerConfigDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_server_config, null)
        val etServerUrl = dialogView.findViewById<TextInputEditText>(R.id.etServerUrl)
        etServerUrl.setText(tokenManager.getBaseUrl())

        AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Saqlash") { _, _ ->
                val newUrl = etServerUrl.text.toString().trim()
                if (newUrl.isNotEmpty()) {
                    tokenManager.setBaseUrl(newUrl)
                    ApiClient.resetService()
                    binding.tvCurrentServer.text = "Server: ${tokenManager.getBaseUrl()}"
                    Toast.makeText(this, "Server manzili yangilandi!", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Bekor qilish", null)
            .show()
    }
}
