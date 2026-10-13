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
    # Xonadagi BARCHA xonadoshlarning navbatlarini tartib bilan ko'rsatish
    if user.room:
      return DutySchedule.objects.filter(room=user.room).order_by('order_index', 'id')
    return DutySchedule.objects.filter(assigned_to=user).order_by('order_index', 'id')

  def perform_create(self, serializer):
    user = self.request.user
    room = serializer.validated_data.get('room') or user.room
    last_duty = DutySchedule.objects.filter(room=room).order_by('-order_index').first()
    next_order = (last_duty.order_index + 1) if last_duty else 1
    serializer.save(order_index=next_order)

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

          # Bajaruvchiga bildirishnoma
          from apps.notifications.models import Notification
          Notification.objects.create(
              user=schedule.assigned_to,
              title="✅ Navbat bajarildi! +10 ball",
              message=f"'{schedule.duty_type.name}' navbatini bajarganingiz uchun 10 ball qo'shildi. Umumiy ballingiz: {rating.score}",
              notification_type="general",
          )

          # KETMA-KETLIK: Keyingi navbatchini aniqlash va navbatni unga o'tkazish!
          next_duty = DutySchedule.objects.filter(
              room=schedule.room,
              status='pending',
              order_index__gt=schedule.order_index
          ).order_by('order_index').first()

          # Agar navbat oxiriga yetgan bo'lsa (hamma bajargan bo'lsa), zanjirni boshidan qaytarish (aylanma navbat)
          if not next_duty:
            # Hamma bajargan navbatlarni qaytadan aylanma qilish uchun pending qilish
            all_room_duties = DutySchedule.objects.filter(room=schedule.room).order_by('order_index')
            if all_room_duties.count() > 1:
              # O'zidan boshqa barcha navbatlarni pending holatiga qaytarish
              DutySchedule.objects.filter(room=schedule.room).exclude(id=schedule.id).update(status='pending')
              next_duty = DutySchedule.objects.filter(room=schedule.room, status='pending').order_by('order_index').first()

          if next_duty:
            next_name = next_duty.assigned_to.first_name or next_duty.assigned_to.username
            Notification.objects.create(
                user=next_duty.assigned_to,
                title="🔔 Navbat sizga o'tdi!",
                message=f"{schedule.assigned_to.first_name or schedule.assigned_to.username} '{schedule.duty_type.name}' navbatini bajardi. Endi '{next_duty.duty_type.name}' navbati sizga keldi!",
                notification_type="duty_reminder",
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
      req_name = self.request.user.first_name or self.request.user.username
      reason_txt = f" Sabab: {swap.reason}" if swap.reason else ""
      Notification.objects.create(
          user=swap.requested_to,
          title="🔄 Navbat almashtirish so'rovi!",
          message=f"{req_name} siz bilan '{swap.duty_schedule.duty_type.name}' navbatini almashtirmoqchi.{reason_txt}",
          notification_type="swap_request",
      )
    except Exception:
      pass

  def perform_update(self, serializer):
    swap = serializer.save()
    if swap.status == 'approved':
      schedule = swap.duty_schedule
      # Ikki talabaning navbatlarini to'liq almashtirish (agar requested_to ning ham navbati bo'lsa)
      other_duty = DutySchedule.objects.filter(
          room=schedule.room,
          assigned_to=swap.requested_to,
          status='pending'
      ).exclude(id=schedule.id).order_by('order_index').first()

      if other_duty:
        # Ikkalasining o'rnini almashtirish
        other_duty.assigned_to = swap.requester
        other_duty.save(update_fields=['assigned_to'])

      schedule.assigned_to = swap.requested_to
      schedule.save(update_fields=['assigned_to'])

      try:
        from apps.notifications.models import Notification
        req_name = swap.requester.first_name or swap.requester.username
        resp_name = swap.requested_to.first_name or swap.requested_to.username
        Notification.objects.create(
            user=swap.requester,
            title="✅ Navbat almashinuvi qabul qilindi!",
            message=f"{resp_name} navbat almashtirish so'rovingizni qabul qildi. Navbatlar o'rni almashdi.",
            notification_type="swap_request",
        )
      except Exception:
        pass
    elif swap.status == 'rejected':
      try:
        from apps.notifications.models import Notification
        resp_name = swap.requested_to.first_name or swap.requested_to.username
        Notification.objects.create(
            user=swap.requester,
            title="❌ Navbat almashinuvi rad etildi",
            message=f"{resp_name} navbat almashtirish so'rovingizni rad etdi.",
            notification_type="swap_request",
        )
      except Exception:
        pass
