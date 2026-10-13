from django.conf import settings
from django.db import models


class Notification(models.Model):
  """Foydalanuvchiga yuboriladigan xabarlar"""

  NOTIFICATION_TYPES = (
      ('duty_reminder', 'Navbat eslatmasi'),
      ('swap_request', 'Navbat almashtirish so`rovi'),
      ('expense_added', 'Yangi xarajat qo`shildi'),
      ('penalty', 'Jarima haqida ogohlantirish'),
  )

  user = models.ForeignKey(
      settings.AUTH_USER_MODEL,
      on_delete=models.CASCADE,
      related_name='notifications',
      verbose_name='Foydalanuvchi',
  )
  title = models.CharField(max_length=100, verbose_name='Sarlavha')
  message = models.TextField(verbose_name='Xabar matni')
  notification_type = models.CharField(
      max_length=30, choices=NOTIFICATION_TYPES, verbose_name='Xabar turi'
  )
  is_read = models.BooleanField(default=False, verbose_name='O`qildimi')
  created_at = models.DateTimeField(auto_now_add=True)

  class Meta:
    ordering = ['-created_at']

  def __str__(self):
    return f'{self.user.username} - {self.title}'