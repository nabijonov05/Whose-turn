package com.whoseturn.app.ui.main

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.whoseturn.app.R
import com.whoseturn.app.data.api.ApiClient
import com.whoseturn.app.data.api.TokenManager
import com.whoseturn.app.databinding.ActivityMainBinding
import com.whoseturn.app.ui.auth.LoginActivity
import com.whoseturn.app.ui.duties.DutiesFragment
import com.whoseturn.app.ui.expenses.ExpensesFragment
import com.whoseturn.app.ui.leaderboard.LeaderboardFragment
import com.whoseturn.app.ui.notifications.NotificationsFragment
import com.whoseturn.app.ui.profile.ProfileFragment
import com.whoseturn.app.util.LocaleHelper
import com.whoseturn.app.util.NotificationHelper
import com.whoseturn.app.util.ThemeHelper
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var tokenManager: TokenManager

    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { _ ->
            // Permission result handled
        }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeHelper.applyTheme(this)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager(this)

        if (!tokenManager.isLoggedIn()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        NotificationHelper.createNotificationChannel(this)
        requestNotificationPermissionIfNeeded()

        setupBottomNavigation()
        loadUserProfile()
        checkNewNotifications()

        // Default fragment
        if (savedInstanceState == null) {
            replaceFragment(DutiesFragment(), getString(R.string.nav_duties))
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_duties -> {
                    replaceFragment(DutiesFragment(), getString(R.string.nav_duties))
                    true
                }
                R.id.navigation_expenses -> {
                    replaceFragment(ExpensesFragment(), getString(R.string.nav_expenses))
                    true
                }
                R.id.navigation_leaderboard -> {
                    replaceFragment(LeaderboardFragment(), getString(R.string.nav_leaderboard))
                    true
                }
                R.id.navigation_notifications -> {
                    replaceFragment(NotificationsFragment(), getString(R.string.nav_notifications))
                    true
                }
                R.id.navigation_profile -> {
                    replaceFragment(ProfileFragment(), getString(R.string.nav_profile))
                    true
                }
                else -> false
            }
        }
    }

    private fun replaceFragment(fragment: Fragment, title: String) {
        binding.tvTopTitle.text = title
        supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, fragment)
            .commit()
    }

    private fun loadUserProfile() {
        lifecycleScope.launch {
            try {
                val api = ApiClient.getService(this@MainActivity)
                val resp = api.getProfile()
                if (resp.isSuccessful && resp.body() != null) {
                    val user = resp.body()!!
                    val roomText = if (user.room != null) "${user.room.number}-xona" else "Xonasiz"
                    binding.tvUserRoomBadge.text = roomText
                }
            } catch (e: Exception) {
                // Ignore profile header load failure gracefully
            }
        }
    }

    private fun checkNewNotifications() {
        lifecycleScope.launch {
            try {
                val api = ApiClient.getService(this@MainActivity)
                val resp = api.getNotifications()
                if (resp.isSuccessful && resp.body() != null) {
                    val unread = resp.body()!!.filter { !it.isRead }
                    if (unread.isNotEmpty()) {
                        val latest = unread.first()
                        NotificationHelper.showSmsNotification(
                            this@MainActivity,
                            latest.title,
                            latest.message,
                            latest.id
                        )
                    }
                }
            } catch (e: Exception) {
                // Ignore background check error
            }
        }
    }
}
