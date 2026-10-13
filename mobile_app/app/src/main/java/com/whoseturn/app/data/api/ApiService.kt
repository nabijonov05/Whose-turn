package com.whoseturn.app.data.api

import com.whoseturn.app.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // Auth
    @POST("auth/login/")
    suspend fun login(@Body body: LoginRequest): Response<AuthResponse>

    @POST("auth/register/")
    suspend fun register(@Body body: RegisterRequest): Response<User>

    // Profile & Users
    @GET("profile/me/")
    suspend fun getProfile(): Response<User>

    @PATCH("profile/me/")
    suspend fun updateProfile(@Body body: UpdateProfileRequest): Response<User>

    @Multipart
    @PATCH("profile/me/")
    suspend fun updateProfilePicture(
        @Part profilePicture: okhttp3.MultipartBody.Part
    ): Response<User>

    @POST("profile/change-password/")
    suspend fun changePassword(@Body body: ChangePasswordRequest): Response<Map<String, String>>

    @GET("profile/roommates/")
    suspend fun getRoommates(): Response<List<User>>

    @GET("blocks/")
    suspend fun getBlocks(): Response<List<Block>>

    @GET("rooms/")
    suspend fun getRooms(): Response<List<Room>>

    // Duties
    @GET("duty-types/")
    suspend fun getDutyTypes(): Response<List<DutyType>>

    @GET("schedules/")
    suspend fun getSchedules(): Response<List<DutySchedule>>

    @POST("schedules/")
    suspend fun createSchedule(@Body body: CreateScheduleRequest): Response<DutySchedule>

    @PATCH("schedules/{id}/")
    suspend fun updateScheduleStatus(
        @Path("id") id: Int,
        @Body body: UpdateStatusRequest
    ): Response<DutySchedule>

    @GET("swap-requests/")
    suspend fun getSwapRequests(): Response<List<DutySwapRequest>>

    @POST("swap-requests/")
    suspend fun createSwapRequest(@Body body: CreateSwapRequest): Response<DutySwapRequest>

    @PATCH("swap-requests/{id}/")
    suspend fun updateSwapRequest(
        @Path("id") id: Int,
        @Body body: UpdateStatusRequest
    ): Response<DutySwapRequest>

    // Expenses
    @GET("categories/")
    suspend fun getExpenseCategories(): Response<List<ExpenseCategory>>

    @GET("expenses/")
    suspend fun getExpenses(): Response<List<Expense>>

    @POST("expenses/")
    suspend fun createExpense(@Body body: CreateExpenseRequest): Response<Expense>

    @GET("shared-payments/")
    suspend fun getSharedPayments(): Response<List<SharedPayment>>

    @PATCH("shared-payments/{id}/")
    suspend fun updateSharedPayment(
        @Path("id") id: Int,
        @Body body: UpdatePaidRequest
    ): Response<SharedPayment>

    // Ratings & Penalties
    @GET("ratings/")
    suspend fun getRatings(): Response<List<StudentRating>>

    @GET("penalties/")
    suspend fun getPenalties(): Response<List<Penalty>>

    // Notifications
    @GET("notifications/")
    suspend fun getNotifications(): Response<List<NotificationItem>>

    @PATCH("notifications/{id}/")
    suspend fun markNotificationRead(
        @Path("id") id: Int,
        @Body body: Map<String, Boolean>
    ): Response<NotificationItem>
}
