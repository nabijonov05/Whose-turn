package com.whoseturn.app.ui.duties

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.whoseturn.app.R
import com.whoseturn.app.data.api.ApiClient
import com.whoseturn.app.data.model.*
import com.whoseturn.app.databinding.FragmentDutiesBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class DutiesFragment : Fragment() {

    private var _binding: FragmentDutiesBinding? = null
    private val binding get() = _binding!!

    private lateinit var dutyAdapter: DutyAdapter
    private var currentUser: User? = null
    private val schedules = mutableListOf<DutySchedule>()
    private val roommates = mutableListOf<User>()
    private val dutyTypes = mutableListOf<DutyType>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDutiesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupFilterChips()

        binding.swipeRefresh.setOnRefreshListener {
            loadData()
        }

        binding.fabAddDuty.setOnClickListener {
            showAddDutyDialog()
        }

        loadData()
    }

    private fun setupRecyclerView() {
        dutyAdapter = DutyAdapter(
            currentUserId = currentUser?.id ?: -1,
            onCompleteClick = { schedule -> markDutyCompleted(schedule) },
            onSwapClick = { schedule -> showSwapDialog(schedule) }
        )
        binding.rvDuties.layoutManager = LinearLayoutManager(requireContext())
        binding.rvDuties.adapter = dutyAdapter
    }

    private fun setupFilterChips() {
        binding.chipGroupFilter.setOnCheckedStateChangeListener { _, checkedIds ->
            when {
                checkedIds.contains(R.id.chipMyDuties) -> dutyAdapter.filter("my")
                checkedIds.contains(R.id.chipPending) -> dutyAdapter.filter("pending")
                checkedIds.contains(R.id.chipCompleted) -> dutyAdapter.filter("completed")
                else -> dutyAdapter.filter("all")
            }
        }
    }

    private fun loadData() {
        binding.swipeRefresh.isRefreshing = true
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val api = ApiClient.getService(requireContext())

                // 1. Current user
                val profileResp = api.getProfile()
                if (profileResp.isSuccessful && profileResp.body() != null) {
                    currentUser = profileResp.body()
                    setupRecyclerView()
                }

                // 2. Roommates
                val roommatesResp = api.getRoommates()
                if (roommatesResp.isSuccessful && roommatesResp.body() != null) {
                    roommates.clear()
                    roommates.addAll(roommatesResp.body()!!)
                }

                // 3. Duty Types
                val dutyTypesResp = api.getDutyTypes()
                if (dutyTypesResp.isSuccessful && dutyTypesResp.body() != null) {
                    dutyTypes.clear()
                    dutyTypes.addAll(dutyTypesResp.body()!!)
                }

                // 4. Schedules
                val schedulesResp = api.getSchedules()
                if (schedulesResp.isSuccessful && schedulesResp.body() != null) {
                    schedules.clear()
                    schedules.addAll(schedulesResp.body()!!)
                    dutyAdapter.submitList(schedules)

                    updateTodayDutyBanner(schedules)
                    updateDutyStatistics(schedules)

                    binding.tvEmptyDuties.visibility =
                        if (schedules.isEmpty()) View.VISIBLE else View.GONE
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Ma'lumotlarni yuklashda xatolik: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.swipeRefresh.isRefreshing = false
            }
        }
    }

    private fun updateDutyStatistics(items: List<DutySchedule>) {
        if (items.isEmpty()) {
            binding.dutyCircularProgress.setProgress(100, android.graphics.Color.parseColor("#10B981"), "100%")
            binding.tvDutyStatsSummary.text = "Hozircha navbatlar mavjud emas"
            binding.tvDutyRankSummary.text = "Jadval yaratilishi kutilmoqda"
            return
        }

        val completedCount = items.count { it.status == "completed" }
        val pendingCount = items.count { it.status == "pending" }
        val total = items.size
        val percentage = ((completedCount.toDouble() / total) * 100).toInt()

        val color = when {
            percentage >= 70 -> android.graphics.Color.parseColor("#10B981")
            percentage >= 40 -> android.graphics.Color.parseColor("#F59E0B")
            else -> android.graphics.Color.parseColor("#EF4444")
        }

        binding.dutyCircularProgress.setProgress(percentage, color, "Bajarildi")
        binding.tvDutyStatsSummary.text = "$completedCount ta bajarilgan • $pendingCount ta kutilmoqda"
        binding.tvDutyRankSummary.text = if (pendingCount == 0) "Barcha navbatlar to'liq bajarildi! 👏" else "Rejadagi navbatlarni bajaring"
    }

    private fun updateTodayDutyBanner(items: List<DutySchedule>) {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val todayDuty = items.find { it.date == todayStr } ?: items.firstOrNull()

        if (todayDuty != null) {
            binding.cardTodayDuty.visibility = View.VISIBLE
            binding.tvTodayDutyStudent.text = todayDuty.assignedTo?.fullName ?: "Noma'lum"
            binding.tvTodayDutyType.text = "🧹 ${todayDuty.dutyType?.name ?: "Navbatchilik"}"
            binding.tvTodayDate.text = if (todayDuty.date == todayStr) "Bugun (${todayDuty.date})" else todayDuty.date

            // If today's duty is assigned to the current user, show SMS-style notification!
            if (todayDuty.date == todayStr && todayDuty.assignedTo?.id == currentUser?.id && todayDuty.status == "pending") {
                com.whoseturn.app.util.NotificationHelper.showSmsNotification(
                    requireContext(),
                    "🔔 Bugungi Navbatchilik Eslatmasi!",
                    "Bugun sizning ${todayDuty.dutyType?.name ?: "xonani tozalash"} navbatingiz! Vazifani bajarishni unutmang."
                )
            }
        } else {
            binding.cardTodayDuty.visibility = View.GONE
        }
    }

    private fun markDutyCompleted(schedule: DutySchedule) {
        AlertDialog.Builder(requireContext())
            .setTitle("Vazifani tasdiqlash")
            .setMessage("${schedule.dutyType?.name ?: "Navbatchilik"} vazifasini bajardingizmi?")
            .setPositiveButton("Ha, bajarildi") { _, _ ->
                viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        val api = ApiClient.getService(requireContext())
                        val resp = api.updateScheduleStatus(schedule.id, UpdateStatusRequest("completed"))
                        if (resp.isSuccessful) {
                            Toast.makeText(requireContext(), "Vazifa bajarildi deb belgilandi! +Ball", Toast.LENGTH_SHORT).show()
                            loadData()
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

    private fun showSwapDialog(schedule: DutySchedule) {
        if (roommates.isEmpty()) {
            Toast.makeText(requireContext(), "Xonada boshqa talabalar topilmadi", Toast.LENGTH_SHORT).show()
            return
        }

        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_swap_request, null)
        val spinner = dialogView.findViewById<Spinner>(R.id.spinnerRoommate)
        val etReason = dialogView.findViewById<EditText>(R.id.etReason)

        val roommateNames = roommates.map { it.fullName }
        spinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, roommateNames)

        AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setPositiveButton("Yuborish") { _, _ ->
                val selectedRoommate = roommates[spinner.selectedItemPosition]
                val reason = etReason.text.toString().trim()

                viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        val api = ApiClient.getService(requireContext())
                        val req = CreateSwapRequest(
                            requestedToId = selectedRoommate.id,
                            dutyScheduleId = schedule.id,
                            reason = reason
                        )
                        val resp = api.createSwapRequest(req)
                        if (resp.isSuccessful) {
                            Toast.makeText(requireContext(), "${selectedRoommate.fullName}ga almashtirish so'rovi yuborildi!", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(requireContext(), "So'rov yuborilmadi: ${resp.code()}", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), "Xatolik: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Bekor qilish", null)
            .show()
    }

    private fun showAddDutyDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_duty, null)
        val spinnerDuty = dialogView.findViewById<Spinner>(R.id.spinnerDutyType)
        val spinnerAssign = dialogView.findViewById<Spinner>(R.id.spinnerAssignTo)
        val etDate = dialogView.findViewById<EditText>(R.id.etDate)

        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        etDate.setText(todayStr)

        val dutyNames = dutyTypes.map { it.name }
        spinnerDuty.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, dutyNames)

        val allStudents = mutableListOf<User>()
        currentUser?.let { allStudents.add(it) }
        allStudents.addAll(roommates)
        val studentNames = allStudents.map { it.fullName }
        spinnerAssign.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, studentNames)

        AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setPositiveButton("Biriktirish") { _, _ ->
                if (dutyTypes.isEmpty() || allStudents.isEmpty()) return@setPositiveButton

                val selectedDuty = dutyTypes[spinnerDuty.selectedItemPosition]
                val selectedStudent = allStudents[spinnerAssign.selectedItemPosition]
                val date = etDate.text.toString().trim()
                val roomId = currentUser?.room?.id ?: 1

                viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        val api = ApiClient.getService(requireContext())
                        val req = CreateScheduleRequest(
                            roomId = roomId,
                            assignedToId = selectedStudent.id,
                            dutyTypeId = selectedDuty.id,
                            date = date
                        )
                        val resp = api.createSchedule(req)
                        if (resp.isSuccessful) {
                            Toast.makeText(requireContext(), "Navbatchilik qo'shildi!", Toast.LENGTH_SHORT).show()
                            loadData()
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
