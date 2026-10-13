package com.whoseturn.app.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.whoseturn.app.data.api.ApiClient
import com.whoseturn.app.data.model.NotificationItem
import com.whoseturn.app.data.model.UpdateStatusRequest
import com.whoseturn.app.databinding.FragmentNotificationsBinding
import kotlinx.coroutines.launch

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: NotificationAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = NotificationAdapter { item ->
            handleNotificationClick(item)
        }
        binding.rvNotifications.layoutManager = LinearLayoutManager(requireContext())
        binding.rvNotifications.adapter = adapter

        binding.swipeRefresh.setOnRefreshListener {
            loadNotifications()
        }

        loadNotifications()
    }

    private fun loadNotifications() {
        binding.swipeRefresh.isRefreshing = true
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val api = ApiClient.getService(requireContext())
                val resp = api.getNotifications()
                if (resp.isSuccessful && resp.body() != null) {
                    val notifs = resp.body()!!
                    adapter.submitList(notifs)
                    binding.tvEmptyNotifications.visibility =
                        if (notifs.isEmpty()) View.VISIBLE else View.GONE
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Xabarlarni yuklashda xatolik: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.swipeRefresh.isRefreshing = false
            }
        }
    }

    private fun handleNotificationClick(item: NotificationItem) {
        if (item.notificationType == "swap_request") {
            // Check swap requests and allow approval
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val api = ApiClient.getService(requireContext())
                    val swapsResp = api.getSwapRequests()
                    if (swapsResp.isSuccessful && swapsResp.body() != null) {
                        val pendingSwap = swapsResp.body()!!.find { it.status == "pending" }
                        if (pendingSwap != null) {
                            showSwapDecisionDialog(pendingSwap.id, pendingSwap.requester?.fullName ?: "Xonadoshingiz")
                        } else {
                            Toast.makeText(requireContext(), item.message, Toast.LENGTH_LONG).show()
                        }
                    }
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), item.message, Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            AlertDialog.Builder(requireContext())
                .setTitle(item.title)
                .setMessage(item.message)
                .setPositiveButton("Tushundim", null)
                .show()
        }
    }

    private fun showSwapDecisionDialog(swapId: Int, requesterName: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Navbat almashtirish")
            .setMessage("$requesterName siz bilan navbat almashtirmoqchi. Rozimisiz?")
            .setPositiveButton("Qabul qilish") { _, _ ->
                updateSwap(swapId, "approved")
            }
            .setNegativeButton("Rad etish") { _, _ ->
                updateSwap(swapId, "rejected")
            }
            .setNeutralButton("Bekor qilish", null)
            .show()
    }

    private fun updateSwap(swapId: Int, status: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val api = ApiClient.getService(requireContext())
                val resp = api.updateSwapRequest(swapId, UpdateStatusRequest(status))
                if (resp.isSuccessful) {
                    val msg = if (status == "approved") "Navbat qabul qilindi!" else "Navbat rad etildi."
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                    loadNotifications()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Xatolik: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
