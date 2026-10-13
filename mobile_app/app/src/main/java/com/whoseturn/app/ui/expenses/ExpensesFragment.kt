package com.whoseturn.app.ui.expenses

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
import com.whoseturn.app.databinding.FragmentExpensesBinding
import kotlinx.coroutines.launch
import android.graphics.Color
import com.whoseturn.app.ui.views.BarChartView
import com.whoseturn.app.ui.views.DonutChartView
import java.text.DecimalFormat

class ExpensesFragment : Fragment() {

    private var _binding: FragmentExpensesBinding? = null
    private val binding get() = _binding!!

    private lateinit var expenseAdapter: ExpenseAdapter
    private lateinit var debtAdapter: DebtAdapter
    private val categories = mutableListOf<ExpenseCategory>()
    private val formatter = DecimalFormat("#,###")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExpensesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerViews()

        binding.swipeRefresh.setOnRefreshListener {
            loadData()
        }

        binding.fabAddExpense.setOnClickListener {
            showAddExpenseDialog()
        }

        loadData()
    }

    private fun setupRecyclerViews() {
        debtAdapter = DebtAdapter { debt ->
            payDebt(debt)
        }
        binding.rvDebts.layoutManager = LinearLayoutManager(requireContext())
        binding.rvDebts.adapter = debtAdapter

        expenseAdapter = ExpenseAdapter()
        binding.rvExpenses.layoutManager = LinearLayoutManager(requireContext())
        binding.rvExpenses.adapter = expenseAdapter
    }

    private fun loadData() {
        binding.swipeRefresh.isRefreshing = true
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val api = ApiClient.getService(requireContext())

                // 1. Categories
                val catResp = api.getExpenseCategories()
                if (catResp.isSuccessful && catResp.body() != null) {
                    categories.clear()
                    categories.addAll(catResp.body()!!)
                }

                // 2. My Debts
                val debtsResp = api.getSharedPayments()
                if (debtsResp.isSuccessful && debtsResp.body() != null) {
                    val unpaidDebts = debtsResp.body()!!.filter { !it.isPaid }
                    debtAdapter.submitList(unpaidDebts)

                    binding.tvDebtsHeader.visibility = if (unpaidDebts.isNotEmpty()) View.VISIBLE else View.GONE
                    binding.rvDebts.visibility = if (unpaidDebts.isNotEmpty()) View.VISIBLE else View.GONE
                }

                // 3. Room Expenses
                val expResp = api.getExpenses()
                if (expResp.isSuccessful && expResp.body() != null) {
                    val expenses = expResp.body()!!
                    expenseAdapter.submitList(expenses)

                    val totalSum = expenses.sumOf { it.amount.toDoubleOrNull() ?: 0.0 }
                    binding.tvTotalAmount.text = "${formatter.format(totalSum)} so'm"
                    binding.tvExpenseCount.text = "${expenses.size} ta umumiy xarajat"

                    binding.tvEmptyExpenses.visibility = if (expenses.isEmpty()) View.VISIBLE else View.GONE

                    updateCharts(expenses, totalSum)
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Xatolik: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.swipeRefresh.isRefreshing = false
            }
        }
    }

    private fun updateCharts(expenses: List<Expense>, totalSum: Double) {
        if (expenses.isEmpty()) {
            binding.cardCategoryChart.visibility = View.GONE
            binding.cardBarChart.visibility = View.GONE
            return
        }

        binding.cardCategoryChart.visibility = View.VISIBLE
        binding.cardBarChart.visibility = View.VISIBLE

        val colors = listOf(
            Color.parseColor("#4F46E5"),
            Color.parseColor("#06B6D4"),
            Color.parseColor("#10B981"),
            Color.parseColor("#F59E0B"),
            Color.parseColor("#EC4899"),
            Color.parseColor("#8B5CF6")
        )

        // 1. Donut Chart by Category
        val categoryTotals = expenses.groupBy { it.category?.name ?: "Boshqa" }
            .mapValues { entry -> entry.value.sumOf { it.amount.toDoubleOrNull() ?: 0.0 } }

        val slices = categoryTotals.entries.mapIndexed { index, entry ->
            DonutChartView.Slice(
                label = entry.key,
                value = entry.value,
                color = colors[index % colors.size]
            )
        }
        binding.donutChartView.setData(slices, "${formatter.format(totalSum)} so'm", "Jami xarajat")

        val legendText = categoryTotals.entries.mapIndexed { index, entry ->
            val pct = if (totalSum > 0) ((entry.value / totalSum) * 100).toInt() else 0
            "● ${entry.key}: ${formatter.format(entry.value)} so'm ($pct%)"
        }.joinToString("   ")
        binding.tvChartLegend.text = legendText

        // 2. Bar Chart by Roommate (Payer)
        val payerTotals = expenses.groupBy { it.payer?.fullName ?: "Noma'lum" }
            .mapValues { entry -> entry.value.sumOf { it.amount.toDoubleOrNull() ?: 0.0 } }

        val bars = payerTotals.entries.mapIndexed { index, entry ->
            BarChartView.Bar(
                label = entry.key,
                value = entry.value,
                color = colors[index % colors.size],
                valueDisplay = "${formatter.format(entry.value)}"
            )
        }
        binding.barChartView.setData(bars)
    }

    private fun payDebt(debt: SharedPayment) {
        AlertDialog.Builder(requireContext())
            .setTitle("Qarzni to'lash")
            .setMessage("${debt.shareAmount} so'm miqdoridagi ulushni to'ladingizmi?")
            .setPositiveButton("Ha, to'ladim") { _, _ ->
                viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        val api = ApiClient.getService(requireContext())
                        val resp = api.updateSharedPayment(debt.id, UpdatePaidRequest(true))
                        if (resp.isSuccessful) {
                            Toast.makeText(requireContext(), "To'lov tasdiqlandi!", Toast.LENGTH_SHORT).show()
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

    private fun showAddExpenseDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_expense, null)
        val etTitle = dialogView.findViewById<EditText>(R.id.etExpenseTitle)
        val etAmount = dialogView.findViewById<EditText>(R.id.etExpenseAmount)
        val spinner = dialogView.findViewById<Spinner>(R.id.spinnerCategory)

        val catNames = categories.map { it.name }
        spinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, catNames)

        AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setPositiveButton("Qo'shish") { _, _ ->
                val title = etTitle.text.toString().trim()
                val amountStr = etAmount.text.toString().trim()
                val amount = amountStr.toDoubleOrNull()

                if (title.isEmpty() || amount == null || amount <= 0) {
                    Toast.makeText(requireContext(), "Xarajat nomi va summasini to'g'ri kiriting!", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val selectedCategory = if (categories.isNotEmpty()) categories[spinner.selectedItemPosition] else null
                val catId = selectedCategory?.id ?: 1

                viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        val api = ApiClient.getService(requireContext())
                        val req = CreateExpenseRequest(title = title, amount = amount, categoryId = catId)
                        val resp = api.createExpense(req)

                        if (resp.isSuccessful) {
                            Toast.makeText(requireContext(), "Xarajat qo'shildi va xonadoshlarga teng bo'lindi!", Toast.LENGTH_LONG).show()
                            loadData()
                        } else {
                            Toast.makeText(requireContext(), "Qo'shishda xatolik: ${resp.code()}", Toast.LENGTH_SHORT).show()
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
