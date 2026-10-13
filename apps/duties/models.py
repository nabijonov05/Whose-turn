from django.conf import settings
from django.db import models
from apps.users.models import Room


class DutyType(models.Model):
  """Navbatchilik turlari (masalan: Xonani tozalash, Suv olib kelish, Non/Ovqat)"""

  name = models.CharField(
      max_length=50, verbose_name="Navbatchilik turi nomi"
  )
  description = models.TextField(
      blank=True, null=True, verbose_name="Tavsifi"
  )

  def __str__(self):
    return self.name


class DutySchedule(models.Model):
  """Navbatchilik jadvali"""

  STATUS_CHOICES = (
      ('pending', 'Kutilmoqda'),
      ('completed', 'Bajarildi'),
  )

  room = models.ForeignKey(
      Room,
      on_delete=models.CASCADE,
      related_name='duty_schedules',
      verbose_name='Xona',
  )
  assigned_to = models.ForeignKey(
      settings.AUTH_USER_MODEL,
      on_delete=models.CASCADE,
      related_name='my_duties',
      verbose_name='Biriktirilgan talaba',
  )
  duty_type = models.ForeignKey(
      DutyType, on_delete=models.CASCADE, verbose_name='Navbat turi'
  )
  date = models.DateField(
      blank=True, null=True, verbose_name='Navbat sanasi'
  )
  order_index = models.PositiveIntegerField(
      default=0, verbose_name="Navbat ketma-ketlik indeksi"
  )
  status = models.CharField(
      max_length=15,
      choices=STATUS_CHOICES,
      default='pending',
      verbose_name='Holati',
  )
  proof_image = models.ImageField(
      upload_to='duty_proofs/',
      blank=True,
      null=True,
      verbose_name='Bajarilganlik rasmi',
  )
  created_at = models.DateTimeField(auto_now_add=True)

  class Meta:
    ordering = ['order_index', 'id']

  def __str__(self):
    return f'#{self.order_index} {self.assigned_to.username} ({self.duty_type.name}) - {self.status}'


class DutySwapRequest(models.Model):
  """Navbatni almashtirish so'rovi (Task Swapping)"""

  STATUS_CHOICES = (
      ('pending', 'Kutilmoqda'),
      ('approved', 'Tasdiqlandi'),
      ('rejected', 'Rad etildi'),
  )

  requester = models.ForeignKey(
      settings.AUTH_USER_MODEL,
      on_delete=models.CASCADE,
      related_name='swap_requests_sent',
      verbose_name='So`rov yuboruvchi',
  )
  requested_to = models.ForeignKey(
      settings.AUTH_USER_MODEL,
      on_delete=models.CASCADE,
      related_name='swap_requests_received',
      verbose_name='Kimga yuborildi',
  )
  duty_schedule = models.ForeignKey(
      DutySchedule,
      on_delete=models.CASCADE,
      related_name='swap_requests',
      verbose_name='Qaysi navbat',
  )
  reason = models.TextField(
      blank=True, null=True, verbose_name='Sabab (Ixtiyoriy)'
  )
  status = models.CharField(
      max_length=15,
      choices=STATUS_CHOICES,
      default='pending',
      verbose_name='Holati',
  )
  created_at = models.DateTimeField(auto_now_add=True)

  def __str__(self):
    return f"{self.requester.username} -> {self.requested_to.username} ({self.status})"