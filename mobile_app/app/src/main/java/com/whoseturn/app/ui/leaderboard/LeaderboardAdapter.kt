package com.whoseturn.app.ui.leaderboard

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.whoseturn.app.R
import com.whoseturn.app.data.model.StudentRating
import com.whoseturn.app.databinding.ItemLeaderboardBinding

class LeaderboardAdapter : RecyclerView.Adapter<LeaderboardAdapter.LeaderboardViewHolder>() {

    private val items = mutableListOf<StudentRating>()

    fun submitList(newItems: List<StudentRating>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeaderboardViewHolder {
        val binding = ItemLeaderboardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LeaderboardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LeaderboardViewHolder, position: Int) {
        holder.bind(items[position], position + 1)
    }

    override fun getItemCount(): Int = items.size

    inner class LeaderboardViewHolder(private val binding: ItemLeaderboardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: StudentRating, rank: Int) {
            val context = binding.root.context
            binding.tvRank.text = when (rank) {
                1 -> "🥇"
                2 -> "🥈"
                3 -> "🥉"
                else -> "#$rank"
            }

            val badgeColor = when (rank) {
                1 -> ContextCompat.getColor(context, R.color.gold)
                2 -> ContextCompat.getColor(context, R.color.silver)
                3 -> ContextCompat.getColor(context, R.color.bronze)
                else -> ContextCompat.getColor(context, R.color.primary)
            }
            binding.tvRank.backgroundTintList = ColorStateList.valueOf(badgeColor)

            binding.tvStudentName.text = item.user.fullName
            val blockName = item.user.room?.block?.name ?: ""
            val roomNumber = item.user.room?.number
            val roomDisplay = if (roomNumber != null) {
                if (blockName.isNotEmpty()) "$blockName • $roomNumber-xona" else "$roomNumber-xona"
            } else "Xonasiz"
            binding.tvStudentRoom.text = "$roomDisplay • ✅${item.completedDutiesCount} ta bajarilgan"

            binding.tvScore.text = "${item.score} ball"
            val scoreColor = when {
                item.score >= 90 -> ContextCompat.getColor(context, R.color.success)
                item.score >= 60 -> ContextCompat.getColor(context, R.color.warning)
                else -> ContextCompat.getColor(context, R.color.danger)
            }
            binding.tvScore.setTextColor(scoreColor)
        }
    }
}
