package com.whoseturn.app.ui.auth

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.whoseturn.app.data.api.ApiClient
import com.whoseturn.app.data.model.RegisterRequest
import com.whoseturn.app.databinding.ActivityRegisterBinding
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val blockOptions = mutableListOf("A-blok", "B-blok", "C-blok", "D-blok")

    override fun attachBaseContext(newBase: android.content.Context) {
        super.attachBaseContext(com.whoseturn.app.util.LocaleHelper.onAttach(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        com.whoseturn.app.util.ThemeHelper.applyTheme(this)
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBlockSpinner()
        loadBlocksFromApi()

        binding.btnRegister.setOnClickListener {
            performRegister()
        }

        binding.tvLoginLink.setOnClickListener {
            finish()
        }
    }

    private fun setupBlockSpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, blockOptions)
        binding.spinnerBlock.adapter = adapter
    }

    private fun loadBlocksFromApi() {
        lifecycleScope.launch {
            try {
                val api = ApiClient.getService(this@RegisterActivity)
                val response = api.getBlocks()
                if (response.isSuccessful && response.body() != null) {
                    val serverBlocks = response.body()!!.map { it.name }
                    for (b in serverBlocks) {
                        if (!blockOptions.contains(b)) {
                            blockOptions.add(b)
                        }
                    }
                    setupBlockSpinner()
                }
            } catch (e: Exception) {
                // Ignore API fetch error, default blocks are ready
            }
        }
    }

    private fun performRegister() {
        val firstName = binding.etFirstName.text.toString().trim()
        val lastName = binding.etLastName.text.toString().trim()
        val roomNumber = binding.etRoomNumber.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val username = binding.etUsername.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (firstName.isEmpty()) {
            binding.tilFirstName.error = "Ismingizni kiriting"
            return
        }
        binding.tilFirstName.error = null

        if (lastName.isEmpty()) {
            binding.tilLastName.error = "Familiyangizni kiriting"
            return
        }
        binding.tilLastName.error = null

        if (roomNumber.isEmpty()) {
            binding.tilRoomNumber.error = "Xona raqamini kiriting (masalan: 905)"
            return
        }
        binding.tilRoomNumber.error = null

        if (username.isEmpty()) {
            binding.tilUsername.error = "Foydalanuvchi nomini kiriting"
            return
        }
        binding.tilUsername.error = null

        if (password.isEmpty() || password.length < 4) {
            binding.tilPassword.error = "Parol kamida 4 belgidan iborat bo'lishi kerak"
            return
        }
        binding.tilPassword.error = null

        val selectedBlock = blockOptions.getOrElse(binding.spinnerBlock.selectedItemPosition) { "A-blok" }

        binding.progressBar.visibility = View.VISIBLE
        binding.btnRegister.isEnabled = false

        lifecycleScope.launch {
            try {
                val api = ApiClient.getService(this@RegisterActivity)
                val request = RegisterRequest(
                    username = username,
                    password = password,
                    firstName = firstName,
                    lastName = lastName,
                    phoneNumber = phone,
                    role = "student",
                    blockName = selectedBlock,
                    roomNumber = roomNumber
                )
                val response = api.register(request)

                if (response.isSuccessful) {
                    Toast.makeText(this@RegisterActivity, "Muvaffaqiyatli ro'yxatdan o'tdingiz! Endi kiring.", Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    val code = response.code()
                    Toast.makeText(this@RegisterActivity, "Ro'yxatdan o'tishda xatolik ($code): Bu foydalanuvchi nomi band bo'lishi mumkin.", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@RegisterActivity, "Xatolik: ${e.localizedMessage ?: "Serverga ulanib bo'lmadi"}", Toast.LENGTH_LONG).show()
            } finally {
                binding.progressBar.visibility = View.GONE
                binding.btnRegister.isEnabled = true
            }
        }
    }
}
