from django.contrib import admin
from .models import DutySchedule, DutySwapRequest, DutyType


@admin.register(DutyType)
class DutyTypeAdmin(admin.ModelAdmin):
  list_display = ('id', 'name')


@admin.register(DutySchedule)
class DutyScheduleAdmin(admin.ModelAdmin):
  list_display = ('id', 'room', 'assigned_to', 'duty_type', 'date', 'status')
  list_filter = ('status', 'duty_type', 'date')
  search_fields = (
      'assigned_to__username',
      'assigned_to__first_name',
      'room__number',
  )


@admin.register(DutySwapRequest)
class DutySwapRequestAdmin(admin.ModelAdmin):
  list_display = ('id', 'requester', 'requested_to', 'duty_schedule', 'status')
  list_filter = ('status',)