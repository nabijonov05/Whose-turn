from django.contrib import admin
from .models import Penalty, StudentRating


@admin.register(Penalty)
class PenaltyAdmin(admin.ModelAdmin):
  list_display = ('id', 'user', 'points', 'created_at')
  search_fields = ('user__username',)


@admin.register(StudentRating)
class StudentRatingAdmin(admin.ModelAdmin):
  list_display = (
      'id',
      'user',
      'score',
      'completed_duties_count',
      'failed_duties_count',
  )
  search_fields = ('user__username',)