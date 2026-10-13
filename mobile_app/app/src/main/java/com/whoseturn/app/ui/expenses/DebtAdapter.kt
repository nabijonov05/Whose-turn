package com.whoseturn.app.ui.expenses

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.whoseturn.app.data.model.SharedPayment
import com.whoseturn.app.databinding.ItemDebtBinding
import java.text.DecimalFormat

class DebtAdapter(
    private val onPayClick: (SharedPayment) -> Unit
) : RecyclerView.Adapter<DebtAdapter.DebtViewHolder>() {

    private val items = mutableListOf<SharedPayment>()
    private val formatter = DecimalFormat("#,###")

    fun submitList(newItems: List<SharedPayment>) {
        items.clear()
        // Only show unpaid debts
        items.addAll(newItems.filter { !it.isPaid })
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DebtViewHolder {
        val binding = ItemDebtBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DebtViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DebtViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class DebtViewHolder(private val binding: ItemDebtBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SharedPayment) {
            val expenseTitle = item.expense?.title ?: "Xarajat ulushi"
            val payer = item.expense?.payer?.fullName ?: ""
            binding.tvDebtTitle.text = if (payer.isNotEmpty()) "$expenseTitle ($payer uchun)" else expenseTitle

            val amountDouble = item.shareAmount.toDoubleOrNull() ?: 0.0
            binding.tvDebtAmount.text = "${formatter.format(amountDouble)} so'm"

            binding.btnPayDebt.setOnClickListener {
                onPayClick(item)
            }
        }
    }
}
