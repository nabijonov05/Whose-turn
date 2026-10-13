from django.db import models
from rest_framework import viewsets
from rest_framework.permissions import IsAuthenticated
from .models import DutySchedule, DutySwapRequest, DutyType
from .serializers import (
    DutyScheduleSerializer,
    DutySwapRequestSerializer,
    DutyTypeSerializer,
)

DEFAULT_DUTY_TYPES = [
    ("Xonani tozalash", "Xona va yo'lakni supurib, latta bosish"),
    ("Suv olib kelish", "Ichimlik suvi ta'minlash"),
    ("Non olib kelish", "Nonvoydan non yoki ovqat olib kelish"),
    ("Axlat tashish", "Axlat qopini tashlab kelish"),
]


class DutyTypeViewSet(viewsets.ModelViewSet):
  serializer_class = DutyTypeSerializer
  permission_classes = [IsAuthenticated]

  def get_queryset(self):
    if not DutyType.objects.exists():
      for name, desc in DEFAULT_DUTY_TYPES:
        DutyType.objects.get_or_create(name=name, defaults={'description': desc})
    return DutyType.objects.all().order_by('id')


class DutyScheduleViewSet(viewsets.ModelViewSet):
  queryset = DutySchedule.objects.all()
  serializer_class = DutyScheduleSerializer
  permission_classes = [IsAuthenticated]

  def get_queryset(self):
    user = self.request.user
    # Xonadagi BARCHA xonadoshlarning navbatlarini ko'rsatish
    if user.room:
      return DutySchedule.objects.filter(room=user.room).order_by('date')
    return DutySchedule.objects.filter(assigned_to=user).order_by('date')

  def perform_update(self, serializer):
    old_status = self.get_object().status
    schedule = serializer.save()
    new_status = schedule.status

    # Navbat bajarilganda yoki bajarilmaganida reyting yangilash
    if old_status != new_status:
      try:
        from apps.ratings.models import StudentRating
        rating, _ = StudentRating.objects.get_or_create(user=schedule.assigned_to)
        if new_status == 'completed' and old_status == 'pending':
          rating.score += 10
          rating.completed_duties_count += 1
          rating.save()
          # Bildirishnoma yuborish
          from apps.notifications.models import Notification
          Notification.objects.create(
              user=schedule.assigned_to,
              title="✅ Navbat bajarildi! +10 ball",
              message=f"'{schedule.duty_type.name}' navbatini bajarganingiz uchun 10 ball qo'shildi. Umumiy ball: {rating.score}",
              notification_type="general",
          )
        elif new_status == 'failed' and old_status == 'pending':
          rating.score = max(0, rating.score - 15)
          rating.failed_duties_count += 1
          rating.save()
          from apps.notifications.models import Notification
          Notification.objects.create(
              user=schedule.assigned_to,
              title="❌ Navbat bajarilmadi! -15 ball",
              message=f"'{schedule.duty_type.name}' navbati bajarilmadi. 15 ball ayrildi. Umumiy ball: {rating.score}",
              notification_type="general",
          )
      except Exception:
        pass


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
    swap = serializer.save(requester=self.request.user)
    try:
      from apps.notifications.models import Notification
      Notification.objects.create(
          user=swap.requested_to,
          title="🔄 Navbat almashtirish so'rovi",
          message=f"{self.request.user.first_name or self.request.user.username} {swap.duty_schedule.date} sanadagi navbatni siz bilan almashtirmoqchi.",
          notification_type="swap_request",
      )
    except Exception:
      pass

  def perform_update(self, serializer):
    swap = serializer.save()
    if swap.status == 'approved':
      schedule = swap.duty_schedule
      schedule.assigned_to = swap.requested_to
      schedule.save()
      try:
        from apps.notifications.models import Notification
        Notification.objects.create(
            user=swap.requester,
            title="✅ Navbat almashtirildi",
            message=f"{swap.requested_to.first_name or swap.requested_to.username} sizning navbat almashtirish so'rovingizni qabul qildi.",
            notification_type="swap_request",
        )
      except Exception:
        pass
    elif swap.status == 'rejected':
      try:
        from apps.notifications.models import Notification
        Notification.objects.create(
            user=swap.requester,
            title="❌ Navbat almashtirilmadi",
            message=f"{swap.requested_to.first_name or swap.requested_to.username} navbat almashtirish so'rovingizni rad etdi.",
            notification_type="swap_request",
        )
      except Exception:
        pass
