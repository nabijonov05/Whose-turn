from django.contrib import admin
from .models import DutySchedule, DutySwapRequest, DutyType


@admin.register(DutyType)
class DutyTypeAdmin(admin.ModelAdmin):
  list_display = ('id', 'name')


@admin.register(DutySchedule)
class DutyScheduleAdmin(admin.ModelAdmin):
  list_display = ('id', 'room', 'order_index', 'assigned_to', 'duty_type', 'status')
  list_editable = ('order_index', 'status')
  list_filter = ('room', 'status', 'duty_type')
  search_fields = (
      'assigned_to__username',
      'assigned_to__first_name',
      'assigned_to__last_name',
      'room__number',
  )
  fields = ('room', 'order_index', 'assigned_to', 'duty_type', 'status')


@admin.register(DutySwapRequest)
class DutySwapRequestAdmin(admin.ModelAdmin):
  list_display = ('id', 'requester', 'requested_to', 'duty_schedule', 'status', 'created_at')
  list_filter = ('status', 'created_at')
  search_fields = (
      'requester__username',
      'requester__first_name',
      'requested_to__username',
      'requested_to__first_name',
  )