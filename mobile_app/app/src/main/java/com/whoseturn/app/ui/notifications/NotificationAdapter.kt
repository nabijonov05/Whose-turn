package com.whoseturn.app.ui.notifications

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.whoseturn.app.R
import com.whoseturn.app.data.model.NotificationItem
import com.whoseturn.app.databinding.ItemNotificationBinding

class NotificationAdapter(
    private val onItemClick: (NotificationItem) -> Unit
) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    private val items = mutableListOf<NotificationItem>()

    fun submitList(newItems: List<NotificationItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val binding = ItemNotificationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NotificationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class NotificationViewHolder(private val binding: ItemNotificationBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: NotificationItem) {
            binding.tvNotifTitle.text = item.title
            binding.tvNotifMessage.text = item.message

            val iconRes = when (item.notificationType) {
                "duty_reminder" -> R.drawable.ic_duties
                "swap_request" -> R.drawable.ic_swap
                "expense_added" -> R.drawable.ic_expenses
                else -> R.drawable.ic_notifications
            }
            binding.imgNotifIcon.setImageResource(iconRes)

            val dateStr = if (!item.createdAt.isNullOrEmpty() && item.createdAt.length >= 10) {
                item.createdAt.substring(0, 10)
            } else {
                "Yangi"
            }
            binding.tvNotifDate.text = dateStr

            binding.root.setOnClickListener {
                onItemClick(item)
            }
        }
    }
}
