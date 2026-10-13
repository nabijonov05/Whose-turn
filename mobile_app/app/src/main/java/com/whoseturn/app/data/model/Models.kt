package com.whoseturn.app.data.model

import com.google.gson.annotations.SerializedName

data class Block(
    val id: Int,
    val name: String,
    val description: String? = null
)

data class Room(
    val id: Int,
    val number: String,
    val block: Block? = null,
    @SerializedName("block_id") val blockId: Int? = null,
    val capacity: Int = 4
)

data class User(
    val id: Int,
    val username: String,
    @SerializedName("first_name") val firstName: String? = null,
    @SerializedName("last_name") val lastName: String? = null,
    @SerializedName("phone_number") val phoneNumber: String? = null,
    val role: String = "student",
    val room: Room? = null,
    @SerializedName("room_id") val roomId: Int? = null,
    @SerializedName("profile_picture") val profilePicture: String? = null
) {
    val fullName: String
        get() {
            val name = "${firstName.orEmpty()} ${lastName.orEmpty()}".trim()
            return if (name.isNotEmpty()) name else username
        }

    val roleDisplay: String
        get() = when (role) {
            "block_leader" -> "Blok sardori"
            "admin" -> "Komendant / Admin"
            else -> "Talaba"
        }
}

data class DutyType(
    val id: Int,
    val name: String,
    val description: String? = null
)

data class DutySchedule(
    val id: Int,
    val room: Room? = null,
    @SerializedName("room_id") val roomId: Int? = null,
    @SerializedName("assigned_to") val assignedTo: User? = null,
    @SerializedName("assigned_to_id") val assignedToId: Int? = null,
    @SerializedName("duty_type") val dutyType: DutyType? = null,
    @SerializedName("duty_type_id") val dutyTypeId: Int? = null,
    val date: String,
    val status: String = "pending",
    @SerializedName("proof_image") val proofImage: String? = null
)

data class DutySwapRequest(
    val id: Int,
    val requester: User? = null,
    @SerializedName("requested_to") val requestedTo: User? = null,
    @SerializedName("requested_to_id") val requestedToId: Int? = null,
    @SerializedName("duty_schedule") val dutySchedule: DutySchedule? = null,
    @SerializedName("duty_schedule_id") val dutyScheduleId: Int? = null,
    val reason: String? = null,
    val status: String = "pending",
    @SerializedName("created_at") val createdAt: String? = null
)

data class ExpenseCategory(
    val id: Int,
    val name: String
)

data class Expense(
    val id: Int,
    val room: Room? = null,
    val payer: User? = null,
    val category: ExpenseCategory? = null,
    val title: String,
    val amount: String,
    @SerializedName("created_at") val createdAt: String? = null,
    val shares: List<SharedPayment>? = null
)

data class SharedPayment(
    val id: Int,
    val expense: Expense? = null,
    val debtor: User? = null,
    @SerializedName("share_amount") val shareAmount: String,
    @SerializedName("is_paid") val isPaid: Boolean = false
)

data class StudentRating(
    val id: Int,
    val user: User,
    val score: Int = 100,
    @SerializedName("completed_duties_count") val completedDutiesCount: Int = 0,
    @SerializedName("failed_duties_count") val failedDutiesCount: Int = 0
)

data class Penalty(
    val id: Int,
    val user: User,
    @SerializedName("duty_schedule") val dutySchedule: DutySchedule? = null,
    val points: Int = 10,
    val reason: String,
    @SerializedName("created_at") val createdAt: String? = null
)

data class NotificationItem(
    val id: Int,
    val title: String,
    val message: String,
    @SerializedName("notification_type") val notificationType: String,
    @SerializedName("is_read") val isRead: Boolean = false,
    @SerializedName("created_at") val createdAt: String? = null
)

// Request payloads
data class LoginRequest(
    val username: String,
    val password: String
)

data class RegisterRequest(
    val username: String,
    val password: String,
    @SerializedName("first_name") val firstName: String,
    @SerializedName("last_name") val lastName: String,
    @SerializedName("phone_number") val phoneNumber: String,
    val role: String = "student",
    @SerializedName("block_name") val blockName: String? = null,
    @SerializedName("room_number") val roomNumber: String? = null,
    @SerializedName("room_id") val roomId: Int? = null
)

data class UpdateProfileRequest(
    @SerializedName("first_name") val firstName: String? = null,
    @SerializedName("last_name") val lastName: String? = null,
    @SerializedName("phone_number") val phoneNumber: String? = null,
    @SerializedName("block_name") val blockName: String? = null,
    @SerializedName("room_number") val roomNumber: String? = null
)

data class ChangePasswordRequest(
    @SerializedName("old_password") val oldPassword: String,
    @SerializedName("new_password") val newPassword: String
)


data class AuthResponse(
    val access: String,
    val refresh: String
)

data class CreateExpenseRequest(
    val title: String,
    val amount: Double,
    @SerializedName("category_id") val categoryId: Int
)

data class CreateSwapRequest(
    @SerializedName("requested_to_id") val requestedToId: Int,
    @SerializedName("duty_schedule_id") val dutyScheduleId: Int,
    val reason: String = ""
)

data class CreateScheduleRequest(
    @SerializedName("room_id") val roomId: Int,
    @SerializedName("assigned_to_id") val assignedToId: Int,
    @SerializedName("duty_type_id") val dutyTypeId: Int,
    val date: String
)

data class UpdateStatusRequest(
    val status: String
)

data class UpdatePaidRequest(
    @SerializedName("is_paid") val isPaid: Boolean
)
