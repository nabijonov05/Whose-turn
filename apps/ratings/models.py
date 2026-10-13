from django.conf import settings
from django.db import models
from apps.duties.models import DutySchedule


class Penalty(models.Model):
  """Bajarilmagan vazifa uchun jarima ballari"""

  user = models.ForeignKey(
      settings.AUTH_USER_MODEL,
      on_delete=models.CASCADE,
      related_name='penalties',
      verbose_name='Jarima olgan talaba',
  )
  duty_schedule = models.ForeignKey(
      DutySchedule,
      on_delete=models.SET_NULL,
      null=True,
      blank=True,
      verbose_name='Qaysi navbat uchun',
  )
  points = models.IntegerField(default=10, verbose_name='Jarima bali (minus)')
  reason = models.TextField(verbose_name='Jarima sababi')
  created_at = models.DateTimeField(auto_now_add=True)

  def __str__(self):
    return f'{self.user.username} - {self.points} jarima bali'


class StudentRating(models.Model):
  """Talabaning umumiy ball/reyting hisobi (Leaderboard uchun)"""

  user = models.OneToOneField(
      settings.AUTH_USER_MODEL,
      on_delete=models.CASCADE,
      related_name='rating_profile',
      verbose_name='Talaba',
  )
  score = models.IntegerField(default=100, verbose_name='Umumiy ball')
  completed_duties_count = models.IntegerField(
      default=0, verbose_name='Muvaffaqiyatli bajarilgan navbatlar'
  )
  failed_duties_count = models.IntegerField(
      default=0, verbose_name='Bajarilmagan navbatlar'
  )

  class Meta:
    ordering = ['-score']

  def __str__(self):
    return f'{self.user.username} - {self.score} ball'