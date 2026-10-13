package com.whoseturn.app.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.whoseturn.app.data.api.ApiClient
import com.whoseturn.app.data.api.TokenManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationReceiver : BroadcastReceiver() {

    companion object {
        private const val REQUEST_CODE = 9051
        private const val FIVE_HOURS_MS = 5 * 60 * 60 * 1000L // 5 soat

        fun schedulePeriodicCheck(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, NotificationReceiver::class.java).apply {
                action = "com.whoseturn.app.ACTION_CHECK_NOTIFICATIONS"
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Har 5 soatda bir marta tekshiruvchi aylanma signal
            alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + FIVE_HOURS_MS,
                FIVE_HOURS_MS,
                pendingIntent
            )
        }

        fun isOnline(context: Context): Boolean {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = cm.activeNetwork ?: return false
            val capabilities = cm.getNetworkCapabilities(network) ?: return false
            return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        val tokenManager = TokenManager(context)
        if (!tokenManager.isLoggedIn() || !isOnline(context)) {
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val api = ApiClient.getService(context)
                val resp = api.getNotifications()
                if (resp.isSuccessful && resp.body() != null) {
                    val unread = resp.body()!!.filter { !it.isRead }
                    val prefs = context.getSharedPreferences("notification_worker_prefs", Context.MODE_PRIVATE)
                    val lastShownId = prefs.getInt("last_bg_notified_id", -1)

                    for (notif in unread) {
                        if (notif.id > lastShownId) {
                            NotificationHelper.showSmsNotification(
                                context,
                                notif.title,
                                notif.message,
                                notif.id
                            )
                            prefs.edit().putInt("last_bg_notified_id", notif.id).apply()
                            break
                        }
                    }
                }
            } catch (e: Exception) {
                // Ignore background check failure
            }
        }
    }
}
