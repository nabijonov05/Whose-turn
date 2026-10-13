package com.whoseturn.app.ui.profile

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import com.whoseturn.app.R
import com.whoseturn.app.data.api.ApiClient
import com.whoseturn.app.data.api.TokenManager
import com.whoseturn.app.data.model.ChangePasswordRequest
import com.whoseturn.app.data.model.UpdateProfileRequest
import com.whoseturn.app.data.model.User
import com.whoseturn.app.databinding.FragmentProfileBinding
import com.whoseturn.app.ui.auth.LoginActivity
import com.whoseturn.app.util.ImageLoader
import com.whoseturn.app.util.LocaleHelper
import com.whoseturn.app.util.NotificationHelper
import com.whoseturn.app.util.ThemeHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.InputStream

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var tokenManager: TokenManager
    private var currentUser: User? = null

    // Image Picker Launcher
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { uploadProfilePicture(it) }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tokenManager = TokenManager(requireContext())

        updateSettingsUI()
        loadProfile()

        // Avatar click to upload photo
        binding.layoutAvatarContainer.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }
        binding.btnUploadPhoto.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        // Edit Profile
        binding.btnEditProfile.setOnClickListener {
            showEditProfileDialog()
        }

        // Theme Setting (Day / Night)
        binding.rowThemeSetting.setOnClickListener {
            showThemeDialog()
        }

        // Language Setting
        binding.rowLanguageSetting.setOnClickListener {
            showLanguageDialog()
        }

        // Test SMS / Notification
        binding.rowTestNotification.setOnClickListener {
            NotificationHelper.showSmsNotification(
                requireContext(),
                "Whose Turn: Navbat Eslatmasi! 🧹",
                "Bugun sizning xonani tozalash navbatingiz. Vazifani o'z vaqtida bajaring va ball to'plang!"
            )
            Toast.makeText(requireContext(), "Telefonga SMS xabarnoma yuborildi!", Toast.LENGTH_SHORT).show()
        }

        // Change Password
        binding.rowChangePassword.setOnClickListener {
            showChangePasswordDialog()
        }

        // Server Settings
        binding.rowServerSettings.setOnClickListener {
            showServerConfigDialog()
        }

        // Logout
        binding.btnLogout.setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun updateSettingsUI() {
        val currentTheme = ThemeHelper.getThemeMode(requireContext())
        binding.tvCurrentTheme.text = when (currentTheme) {
            ThemeHelper.THEME_LIGHT -> getString(R.string.theme_light)
            ThemeHelper.THEME_DARK -> getString(R.string.theme_dark)
            else -> getString(R.string.theme_system)
        }

        val currentLang = LocaleHelper.getLanguage(requireContext())
        binding.tvCurrentLanguage.text = when (currentLang) {
            LocaleHelper.LANG_RU -> getString(R.string.lang_ru)
            LocaleHelper.LANG_EN -> getString(R.string.lang_en)
            else -> getString(R.string.lang_uz)
        }

        val url = tokenManager.getBaseUrl()
        val cleanHost = url.replace("http://", "").replace("https://", "").replace("/api/v1/", "").replace("/", "")
        binding.tvServerDisplay.text = cleanHost
    }

    private fun loadProfile() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val api = ApiClient.getService(requireContext())
                val resp = api.getProfile()
                if (resp.isSuccessful && resp.body() != null) {
                    currentUser = resp.body()!!
                    val user = currentUser!!

                    binding.tvProfileName.text = user.fullName
                    binding.tvProfileUsername.text = "@${user.username}"
                    binding.tvProfileRole.text = user.roleDisplay

                    // Avatar or Image
                    val baseUrl = tokenManager.getBaseUrl().replace("/api/v1/", "").replace("/api/v1", "")
                    if (!user.profilePicture.isNullOrEmpty()) {
                        val fullPicUrl = if (user.profilePicture.startsWith("http")) user.profilePicture else "$baseUrl${user.profilePicture}"
                        binding.ivProfilePicture.visibility = View.VISIBLE
                        binding.tvAvatarInitials.visibility = View.GONE
                        ImageLoader.load(fullPicUrl, binding.ivProfilePicture)
                    } else {
                        binding.ivProfilePicture.visibility = View.GONE
                        binding.tvAvatarInitials.visibility = View.VISIBLE
                        val initials = (user.firstName?.take(1) ?: "").uppercase() + (user.lastName?.take(1) ?: "").uppercase()
                        binding.tvAvatarInitials.text = if (initials.isNotEmpty()) initials else user.username.take(2).uppercase()
                    }

                    binding.tvProfileBlock.text = user.room?.block?.name ?: "A-blok"
                    binding.tvProfileRoom.text = if (user.room != null) "${user.room.number}-xona" else "Belgilanmagan"
                    binding.tvProfilePhone.text = if (!user.phoneNumber.isNullOrEmpty()) user.phoneNumber else "Kiritilmagan"
                }

                // Load roommates
                val roomResp = api.getRoommates()
                if (roomResp.isSuccessful && roomResp.body() != null) {
                    val roommates = roomResp.body()!!
                    if (roommates.isNotEmpty()) {
                        val text = roommates.joinToString("\n") { "• ${it.fullName} (${it.phoneNumber ?: "telefon yo'q"})" }
                        binding.tvRoommatesList.text = text
                    } else {
                        binding.tvRoommatesList.text = "Xonada boshqa talabalar yo'q"
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Profilni yuklashda xatolik: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uploadProfilePicture(uri: Uri) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val context = requireContext()
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                val bytes = withContext(Dispatchers.IO) {
                    inputStream?.readBytes()
                }

                if (bytes == null || bytes.isEmpty()) {
                    Toast.makeText(context, "Rasmni o'qib bo'lmadi", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val requestBody = bytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
                val part = MultipartBody.Part.createFormData("profile_picture", "profile.jpg", requestBody)

                val api = ApiClient.getService(context)
                val resp = api.updateProfilePicture(part)

                if (resp.isSuccessful) {
                    Toast.makeText(context, "Profil rasmi yangilandi!", Toast.LENGTH_SHORT).show()
                    loadProfile()
                } else {
                    Toast.makeText(context, "Yuklashda xatolik: ${resp.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Xatolik: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showEditProfileDialog() {
        val user = currentUser ?: return
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_profile, null)
        val etFirstName = dialogView.findViewById<EditText>(R.id.etEditFirstName)
        val etLastName = dialogView.findViewById<EditText>(R.id.etEditLastName)
        val etPhone = dialogView.findViewById<EditText>(R.id.etEditPhone)
        val spinnerBlock = dialogView.findViewById<Spinner>(R.id.spinnerEditBlock)
        val etRoom = dialogView.findViewById<EditText>(R.id.etEditRoomNumber)

        etFirstName.setText(user.firstName ?: "")
        etLastName.setText(user.lastName ?: "")
        etPhone.setText(user.phoneNumber ?: "")
        etRoom.setText(user.room?.number ?: "")

        val blocks = listOf("A-blok", "B-blok", "C-blok", "D-blok")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, blocks)
        spinnerBlock.adapter = adapter
        val currentBlock = user.room?.block?.name ?: "A-blok"
        val idx = blocks.indexOfFirst { it.equals(currentBlock, ignoreCase = true) }
        if (idx >= 0) spinnerBlock.setSelection(idx)

        AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setPositiveButton("Saqlash") { _, _ ->
                val fName = etFirstName.text.toString().trim()
                val lName = etLastName.text.toString().trim()
                val phone = etPhone.text.toString().trim()
                val block = blocks[spinnerBlock.selectedItemPosition]
                val room = etRoom.text.toString().trim()

                viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        val api = ApiClient.getService(requireContext())
                        val req = UpdateProfileRequest(
                            firstName = fName,
                            lastName = lName,
                            phoneNumber = phone,
                            blockName = block,
                            roomNumber = room
                        )
                        val resp = api.updateProfile(req)
                        if (resp.isSuccessful) {
                            Toast.makeText(requireContext(), "Profil ma'lumotlari yangilandi!", Toast.LENGTH_SHORT).show()
                            loadProfile()
                        } else {
                            Toast.makeText(requireContext(), "Xatolik: ${resp.code()}", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), "Xatolik: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Bekor qilish", null)
            .show()
    }

    private fun showThemeDialog() {
        val themes = arrayOf(
            getString(R.string.theme_light),
            getString(R.string.theme_dark),
            getString(R.string.theme_system)
        )
        val current = ThemeHelper.getThemeMode(requireContext())
        val checkedItem = when (current) {
            ThemeHelper.THEME_LIGHT -> 0
            ThemeHelper.THEME_DARK -> 1
            else -> 2
        }

        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.theme_setting))
            .setSingleChoiceItems(themes, checkedItem) { dialog, which ->
                val chosen = when (which) {
                    0 -> ThemeHelper.THEME_LIGHT
                    1 -> ThemeHelper.THEME_DARK
                    else -> ThemeHelper.THEME_SYSTEM
                }
                ThemeHelper.setThemeMode(requireContext(), chosen)
                dialog.dismiss()
                activity?.recreate()
            }
            .setNegativeButton("Bekor qilish", null)
            .show()
    }

    private fun showLanguageDialog() {
        val languages = arrayOf(
            getString(R.string.lang_uz),
            getString(R.string.lang_ru),
            getString(R.string.lang_en)
        )
        val current = LocaleHelper.getLanguage(requireContext())
        val checkedItem = when (current) {
            LocaleHelper.LANG_RU -> 1
            LocaleHelper.LANG_EN -> 2
            else -> 0
        }

        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.language_setting))
            .setSingleChoiceItems(languages, checkedItem) { dialog, which ->
                val chosen = when (which) {
                    1 -> LocaleHelper.LANG_RU
                    2 -> LocaleHelper.LANG_EN
                    else -> LocaleHelper.LANG_UZ
                }
                LocaleHelper.setLocale(requireContext(), chosen)
                dialog.dismiss()
                activity?.recreate()
            }
            .setNegativeButton("Bekor qilish", null)
            .show()
    }

    private fun showChangePasswordDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_change_password, null)
        val etOld = dialogView.findViewById<EditText>(R.id.etOldPassword)
        val etNew = dialogView.findViewById<EditText>(R.id.etNewPassword)
        val etConfirm = dialogView.findViewById<EditText>(R.id.etConfirmPassword)

        AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setPositiveButton("O'zgartirish") { _, _ ->
                val oldPass = etOld.text.toString().trim()
                val newPass = etNew.text.toString().trim()
                val confPass = etConfirm.text.toString().trim()

                if (oldPass.isEmpty() || newPass.isEmpty()) {
                    Toast.makeText(requireContext(), "Barcha maydonlarni to'ldiring!", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                if (newPass != confPass) {
                    Toast.makeText(requireContext(), "Yangi parollar mos kelmadi!", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        val api = ApiClient.getService(requireContext())
                        val req = ChangePasswordRequest(oldPassword = oldPass, newPassword = newPass)
                        val resp = api.changePassword(req)
                        if (resp.isSuccessful) {
                            Toast.makeText(requireContext(), "Parol muvaffaqiyatli yangilandi!", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(requireContext(), "Eski parol noto'g'ri!", Toast.LENGTH_LONG).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), "Xatolik: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Bekor qilish", null)
            .show()
    }

    private fun showServerConfigDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_server_config, null)
        val etServerUrl = dialogView.findViewById<TextInputEditText>(R.id.etServerUrl)
        etServerUrl.setText(tokenManager.getBaseUrl())

        AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setPositiveButton("Saqlash") { _, _ ->
                val newUrl = etServerUrl.text.toString().trim()
                if (newUrl.isNotEmpty()) {
                    tokenManager.setBaseUrl(newUrl)
                    ApiClient.resetService()
                    updateSettingsUI()
                    Toast.makeText(requireContext(), "Server manzili yangilandi!", Toast.LENGTH_SHORT).show()
                    loadProfile()
                }
            }
            .setNegativeButton("Bekor qilish", null)
            .show()
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.logout))
            .setMessage("Haqiqatan ham hisobingizdan chiqmoqchimisiz?")
            .setPositiveButton("Chiqish") { _, _ ->
                tokenManager.clear()
                ApiClient.resetService()
                val intent = Intent(requireContext(), LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
            .setNegativeButton("Bekor qilish", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
