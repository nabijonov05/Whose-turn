from django.db import models
from rest_framework import viewsets
from rest_framework.permissions import IsAuthenticated
from .models import DutySchedule, DutySwapRequest, DutyType
from .serializers import (
    DutyScheduleSerializer,
    DutySwapRequestSerializer,
    DutyTypeSerializer,
)


class DutyTypeViewSet(viewsets.ModelViewSet):
  queryset = DutyType.objects.all()
  serializer_class = DutyTypeSerializer
  permission_classes = [IsAuthenticated]


class DutyScheduleViewSet(viewsets.ModelViewSet):
  queryset = DutySchedule.objects.all()
  serializer_class = DutyScheduleSerializer
  permission_classes = [IsAuthenticated]

  def get_queryset(self):
    user = self.request.user
    # Agar talaba bo'lsa, faqat o'zining yoki o'z xonasining navbatlarini ko'rsatish mumkin
    if user.role == 'student' and user.room:
      return DutySchedule.objects.filter(room=user.room)
    return super().get_queryset()


class DutySwapRequestViewSet(viewsets.ModelViewSet):
  queryset = DutySwapRequest.objects.all()
  serializer_class = DutySwapRequestSerializer
  permission_classes = [IsAuthenticated]

  def get_queryset(self):
    user = self.request.user
    if user.is_authenticated:
      return DutySwapRequest.objects.filter(
          models.Q(requester=user) | models.Q(requested_to=user)
      ).order_by('-created_at')
    return super().get_queryset()

  def perform_create(self, serializer):
    # So'rov yuboruvchini avtomatik ravishda hozirgi foydalanuvchi qilib belgilash
    swap = serializer.save(requester=self.request.user)
    try:
      from apps.notifications.models import Notification
      Notification.objects.create(
          user=swap.requested_to,
          title="Navbat almashtirish so'rovi",
          message=f"{self.request.user.first_name or self.request.user.username} {swap.duty_schedule.date} sanadagi navbatni siz bilan almashtirmoqchi.",
          notification_type="swap_request",
      )
    except Exception:
      pass

  def perform_update(self, serializer):
    swap = serializer.save()
    if swap.status == 'approved':
      # Navbatchilik egasini yangilaymiz
      schedule = swap.duty_schedule
      schedule.assigned_to = swap.requested_to
      schedule.save()
      try:
        from apps.notifications.models import Notification
        Notification.objects.create(
            user=swap.requester,
            title="Navbat almashtirildi",
            message=f"{swap.requested_to.first_name or swap.requested_to.username} sizning navbat almashtirish so'rovingizni qabul qildi.",
            notification_type="swap_request",
        )
      except Exception:
        pass