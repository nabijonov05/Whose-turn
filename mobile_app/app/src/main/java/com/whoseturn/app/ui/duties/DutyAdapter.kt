package com.whoseturn.app.ui.duties

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.whoseturn.app.R
import com.whoseturn.app.data.api.TokenManager
import com.whoseturn.app.data.model.DutySchedule
import com.whoseturn.app.databinding.ItemDutyBinding
import com.whoseturn.app.util.ImageLoader

class DutyAdapter(
    private var currentUserId: Int,
    private val onCompleteClick: (DutySchedule) -> Unit,
    private val onSwapClick: (DutySchedule) -> Unit
) : RecyclerView.Adapter<DutyAdapter.DutyViewHolder>() {

    private val allItems = mutableListOf<DutySchedule>()
    private val displayedItems = mutableListOf<DutySchedule>()

    fun submitList(items: List<DutySchedule>) {
        allItems.clear()
        allItems.addAll(items)
        filter("all")
    }

    fun submitList(items: List<DutySchedule>, userId: Int) {
        this.currentUserId = userId
        allItems.clear()
        allItems.addAll(items)
        filter("all")
    }

    fun filter(type: String) {
        displayedItems.clear()
        when (type) {
            "my" -> displayedItems.addAll(allItems.filter { it.assignedTo?.id == currentUserId })
            "pending" -> displayedItems.addAll(allItems.filter { it.status == "pending" })
            "completed" -> displayedItems.addAll(allItems.filter { it.status == "completed" })
            else -> displayedItems.addAll(allItems)
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DutyViewHolder {
        val binding = ItemDutyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DutyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DutyViewHolder, position: Int) {
        holder.bind(displayedItems[position])
    }

    override fun getItemCount(): Int = displayedItems.size

    inner class DutyViewHolder(private val binding: ItemDutyBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: DutySchedule) {
            val context = binding.root.context
            binding.tvDutyType.text = item.dutyType?.name ?: "Navbatchilik"
            val studentName = item.assignedTo?.fullName ?: "Noma'lum"
            binding.tvAssignedStudent.text = studentName
            val orderText = if (item.orderIndex > 0) "#${item.orderIndex}-navbat" else "Ketma-ket"
            binding.tvDutyDate.text = orderText

            // Profil rasmi yoki standart avatar
            val tokenManager = TokenManager(context)
            val baseUrl = tokenManager.getBaseUrl().replace("/api/v1/", "").replace("/api/v1", "")
            val picUrl = item.assignedTo?.profilePicture

            fun setDefaultAvatar() {
                binding.ivStudentAvatar.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.primary))
                val pad = (context.resources.displayMetrics.density * 3).toInt()
                binding.ivStudentAvatar.setPadding(pad, pad, pad, pad)
                binding.ivStudentAvatar.scaleType = ImageView.ScaleType.FIT_CENTER
                binding.ivStudentAvatar.setImageResource(R.drawable.ic_profile)
            }

            if (!picUrl.isNullOrEmpty()) {
                setDefaultAvatar()
                val fullUrl = if (picUrl.startsWith("http")) picUrl else "$baseUrl$picUrl"
                ImageLoader.load(fullUrl, binding.ivStudentAvatar, R.drawable.ic_profile, onSuccess = {
                    binding.ivStudentAvatar.imageTintList = null
                    binding.ivStudentAvatar.setPadding(0, 0, 0, 0)
                    binding.ivStudentAvatar.scaleType = ImageView.ScaleType.CENTER_CROP
                })
            } else {
                setDefaultAvatar()
            }

            when (item.status) {
                "completed" -> {
                    binding.tvDutyStatus.text = context.getString(R.string.status_completed)
                    binding.tvDutyStatus.setBackgroundResource(R.drawable.bg_badge_completed)
                    binding.tvDutyStatus.setTextColor(ContextCompat.getColor(context, R.color.success))
                    binding.layoutActions.visibility = View.GONE
                }
                "failed" -> {
                    binding.tvDutyStatus.text = context.getString(R.string.status_failed)
                    binding.tvDutyStatus.setBackgroundResource(R.drawable.bg_badge_failed)
                    binding.tvDutyStatus.setTextColor(ContextCompat.getColor(context, R.color.danger))
                    binding.layoutActions.visibility = View.GONE
                }
                else -> {
                    binding.tvDutyStatus.text = context.getString(R.string.status_pending)
                    binding.tvDutyStatus.setBackgroundResource(R.drawable.bg_badge_pending)
                    binding.tvDutyStatus.setTextColor(ContextCompat.getColor(context, R.color.warning))

                    // Only show actions if this duty belongs to the current user
                    val isMyDuty = item.assignedTo?.id == currentUserId
                    binding.layoutActions.visibility = if (isMyDuty) View.VISIBLE else View.GONE
                }
            }

            binding.btnComplete.setOnClickListener {
                onCompleteClick(item)
            }
            binding.btnSwap.setOnClickListener {
                onSwapClick(item)
            }
        }
    }
}
