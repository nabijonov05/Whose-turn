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
import com.whoseturn.app.util.NotificationHelper
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

                // 1. Joriy foydalanuvchi
                val profileResp = api.getProfile()
                if (profileResp.isSuccessful && profileResp.body() != null) {
                    currentUser = profileResp.body()
                    setupRecyclerView()
                }

                // 2. Xonadoshlar
                val roommatesResp = api.getRoommates()
                if (roommatesResp.isSuccessful && roommatesResp.body() != null) {
                    roommates.clear()
                    roommates.addAll(roommatesResp.body()!!)
                }

                // 3. Navbat turlari (auto-seed server tomonida bo'ladi)
                val dutyTypesResp = api.getDutyTypes()
                if (dutyTypesResp.isSuccessful && dutyTypesResp.body() != null) {
                    dutyTypes.clear()
                    dutyTypes.addAll(dutyTypesResp.body()!!)
                }

                // 4. Barcha xonadoshlar navbatlari (server xonaga qarab filterlaydi)
                val schedulesResp = api.getSchedules()
                if (schedulesResp.isSuccessful && schedulesResp.body() != null) {
                    schedules.clear()
                    schedules.addAll(schedulesResp.body()!!)
                    dutyAdapter.submitList(schedules, currentUser?.id ?: -1)

                    updateTodayDutyBanner(schedules)
                    updateUpcomingDuties(schedules)
                    updateDutyStatistics(schedules)

                    binding.tvEmptyDuties.visibility =
                        if (schedules.isEmpty()) View.VISIBLE else View.GONE
                }
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Ma'lumotlarni yuklashda xatolik: ${e.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                binding.swipeRefresh.isRefreshing = false
            }
        }
    }

    private fun updateTodayDutyBanner(items: List<DutySchedule>) {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val todayDuties = items.filter { it.date == todayStr }

        if (todayDuties.isNotEmpty()) {
            val todayDuty = todayDuties.first()
            binding.cardTodayDuty.visibility = View.VISIBLE

            val assignedName = todayDuty.assignedTo?.fullName ?: "Noma'lum"
            val isMyTurn = todayDuty.assignedTo?.id == currentUser?.id

            binding.tvTodayDutyStudent.text = if (isMyTurn) "🙋 Siz (bugun sizning navbatingiz!)" else "👤 $assignedName"
            binding.tvTodayDutyType.text = "🧹 ${todayDuty.dutyType?.name ?: "Navbatchilik"}"
            binding.tvTodayDate.text = "Bugun • ${todayDuty.date}"

            // Agar bugun sizning navbatingiz bo'lsa va bajarilmagan bo'lsa bildirishnoma
            if (isMyTurn && todayDuty.status == "pending") {
                if (NotificationHelper.shouldShowDailyNotification(requireContext(), todayDuty.id)) {
                    NotificationHelper.showSmsNotification(
                        requireContext(),
                        "🔔 Bugungi Navbatchilik Eslatmasi!",
                        "Bugun sizning ${todayDuty.dutyType?.name ?: "xonani tozalash"} navbatingiz! Vazifani bajarishni unutmang."
                    )
                    NotificationHelper.markDailyNotificationShown(requireContext(), todayDuty.id)
                }
            }
        } else {
            // Bugun navbat yo'q - keyingi navbatni ko'rsatish
            val upcoming = items.filter { it.date > SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) && it.status == "pending" }
                .sortedBy { it.date }
            if (upcoming.isNotEmpty()) {
                val next = upcoming.first()
                binding.cardTodayDuty.visibility = View.VISIBLE
                val isMyNext = next.assignedTo?.id == currentUser?.id
                val assignedName = if (isMyNext) "Sizning" else "${next.assignedTo?.fullName ?: "Noma'lum"}ning"
                binding.tvTodayDutyStudent.text = "📅 $assignedName navbati"
                binding.tvTodayDutyType.text = "🧹 ${next.dutyType?.name ?: "Navbatchilik"}"
                binding.tvTodayDate.text = next.date
            } else {
                binding.cardTodayDuty.visibility = View.GONE
            }
        }
    }

    /**
     * Keyingi navbatlar: Kim qancha kundan keyin navbat keladi
     */
    private fun updateUpcomingDuties(items: List<DutySchedule>) {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(todayStr) ?: Date()

        // Har bir xonadoshning keyingi navbati
        val allPeople = mutableListOf<User>()
        currentUser?.let { allPeople.add(it) }
        allPeople.addAll(roommates)

        val sb = StringBuilder()
        for (person in allPeople) {
            val nextDuty = items
                .filter { it.assignedTo?.id == person.id && it.date >= todayStr && it.status == "pending" }
                .sortedBy { it.date }
                .firstOrNull()

            if (nextDuty != null) {
                val dutyDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(nextDuty.date) ?: continue
                val diffDays = ((dutyDate.time - today.time) / (1000 * 60 * 60 * 24)).toInt()
                val name = if (person.id == currentUser?.id) "Siz" else person.fullName
                val whenStr = when (diffDays) {
                    0 -> "Bugun"
                    1 -> "Ertaga"
                    else -> "$diffDays kundan keyin"
                }
                sb.append("• $name — ${nextDuty.dutyType?.name ?: "Navbat"} ($whenStr)\n")
            }
        }

        if (sb.isNotEmpty()) {
            binding.tvDutyRankSummary.text = sb.toString().trimEnd()
        } else {
            binding.tvDutyRankSummary.text = "Rejalashtirilgan navbatlar yo'q"
        }
    }

    private fun updateDutyStatistics(items: List<DutySchedule>) {
        if (items.isEmpty()) {
            binding.dutyCircularProgress.setProgress(100, android.graphics.Color.parseColor("#10B981"), "100%")
            binding.tvDutyStatsSummary.text = "Hozircha navbatlar mavjud emas"
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

        binding.dutyCircularProgress.setProgress(percentage, color, "$percentage%")
        binding.tvDutyStatsSummary.text = "$completedCount bajarilgan • $pendingCount kutilmoqda • $total jami"
    }

    private fun markDutyCompleted(schedule: DutySchedule) {
        val isMyDuty = schedule.assignedTo?.id == currentUser?.id
        if (!isMyDuty) {
            Toast.makeText(requireContext(), "Faqat o'z navbatingizni belgilashingiz mumkin!", Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Vazifani tasdiqlash")
            .setMessage("${schedule.dutyType?.name ?: "Navbatchilik"} vazifasini bajardingizmi? (+10 ball qo'shiladi)")
            .setPositiveButton("Ha, bajarildi") { _, _ ->
                viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        val api = ApiClient.getService(requireContext())
                        val resp = api.updateScheduleStatus(schedule.id, UpdateStatusRequest("completed"))
                        if (resp.isSuccessful) {
                            Toast.makeText(requireContext(), "✅ Vazifa bajarildi! +10 ball qo'shildi!", Toast.LENGTH_LONG).show()
                            loadData()
                        } else {
                            val err = resp.errorBody()?.string() ?: resp.code().toString()
                            Toast.makeText(requireContext(), "Xatolik: $err", Toast.LENGTH_SHORT).show()
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
            .setTitle("Navbat almashtirish")
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
                            Toast.makeText(
                                requireContext(),
                                "🔄 ${selectedRoommate.fullName}ga almashtirish so'rovi yuborildi!",
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            val err = resp.errorBody()?.string() ?: resp.code().toString()
                            Toast.makeText(requireContext(), "So'rov yuborilmadi: $err", Toast.LENGTH_SHORT).show()
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
        if (dutyTypes.isEmpty()) {
            Toast.makeText(requireContext(), "Navbat turlari yuklanmoqda, qayta urinib ko'ring", Toast.LENGTH_SHORT).show()
            loadData()
            return
        }

        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_duty, null)
        val spinnerDuty = dialogView.findViewById<Spinner>(R.id.spinnerDutyType)
        val spinnerAssign = dialogView.findViewById<Spinner>(R.id.spinnerAssignTo)
        val etDate = dialogView.findViewById<EditText>(R.id.etDate)

        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        etDate.setText(todayStr)

        val dutyNames = dutyTypes.map { it.name }
        spinnerDuty.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, dutyNames)

        // Barcha xonadoshlarni ko'rsatish (o'zi + xonadoshlari)
        val allStudents = mutableListOf<User>()
        currentUser?.let { allStudents.add(it) }
        allStudents.addAll(roommates.filter { it.id != currentUser?.id })
        val studentNames = allStudents.map { if (it.id == currentUser?.id) "👤 Siz (${it.fullName})" else it.fullName }
        spinnerAssign.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, studentNames)

        AlertDialog.Builder(requireContext())
            .setTitle("Navbat biriktirish")
            .setView(dialogView)
            .setPositiveButton("Biriktirish") { _, _ ->
                if (dutyTypes.isEmpty() || allStudents.isEmpty()) return@setPositiveButton

                val selectedDuty = dutyTypes[spinnerDuty.selectedItemPosition]
                val selectedStudent = allStudents[spinnerAssign.selectedItemPosition]
                val date = etDate.text.toString().trim()
                val roomId = currentUser?.room?.id

                if (roomId == null) {
                    Toast.makeText(requireContext(), "Xona belgilanmagan! Profilingizni yangilang.", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (date.isEmpty() || !date.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) {
                    Toast.makeText(requireContext(), "Sana formatini to'g'ri kiriting: yyyy-MM-dd", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

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
                            Toast.makeText(
                                requireContext(),
                                "✅ ${selectedStudent.fullName}ga navbat biriktirildi!",
                                Toast.LENGTH_SHORT
                            ).show()
                            loadData()
                        } else {
                            val err = resp.errorBody()?.string() ?: resp.code().toString()
                            Toast.makeText(requireContext(), "Xatolik: $err", Toast.LENGTH_LONG).show()
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
