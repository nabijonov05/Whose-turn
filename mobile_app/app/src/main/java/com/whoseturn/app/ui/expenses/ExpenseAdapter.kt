package com.whoseturn.app.ui.expenses

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.whoseturn.app.data.model.Expense
import com.whoseturn.app.databinding.ItemExpenseBinding
import java.text.DecimalFormat

class ExpenseAdapter : RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

    private val items = mutableListOf<Expense>()
    private val formatter = DecimalFormat("#,###")

    fun submitList(newItems: List<Expense>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val binding = ItemExpenseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ExpenseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ExpenseViewHolder(private val binding: ItemExpenseBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Expense) {
            binding.tvExpenseTitle.text = item.title

            val amountDouble = item.amount.toDoubleOrNull() ?: 0.0
            binding.tvExpenseAmount.text = "${formatter.format(amountDouble)} so'm"

            binding.tvExpenseCategory.text = item.category?.name ?: "Umumiy"

            val payerName = item.payer?.fullName ?: "Noma'lum"
            binding.tvExpensePayer.text = "To'ladi: $payerName"

            val dateStr = if (!item.createdAt.isNullOrEmpty() && item.createdAt.length >= 10) {
                item.createdAt.substring(0, 10)
            } else {
                "Yaqinda"
            }
            binding.tvExpenseDate.text = dateStr
        }
    }
}
