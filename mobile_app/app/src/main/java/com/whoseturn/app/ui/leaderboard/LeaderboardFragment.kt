package com.whoseturn.app.ui.leaderboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.whoseturn.app.data.api.ApiClient
import com.whoseturn.app.databinding.FragmentLeaderboardBinding
import kotlinx.coroutines.launch

class LeaderboardFragment : Fragment() {

    private var _binding: FragmentLeaderboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: LeaderboardAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLeaderboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = LeaderboardAdapter()
        binding.rvLeaderboard.layoutManager = LinearLayoutManager(requireContext())
        binding.rvLeaderboard.adapter = adapter

        binding.swipeRefresh.setOnRefreshListener {
            loadLeaderboard()
        }

        loadLeaderboard()
    }

    private fun loadLeaderboard() {
        binding.swipeRefresh.isRefreshing = true
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val api = ApiClient.getService(requireContext())
                val resp = api.getRatings()
                if (resp.isSuccessful && resp.body() != null) {
                    val sortedList = resp.body()!!.sortedByDescending { it.score }
                    adapter.submitList(sortedList)
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Reytingni yuklashda xatolik: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.swipeRefresh.isRefreshing = false
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
