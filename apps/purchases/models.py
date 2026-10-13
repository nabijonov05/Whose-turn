from django.conf import settings
from django.db import models
from apps.users.models import Room


class ExpenseCategory(models.Model):
  """Xarajat kategoriyalari (Suv, Non, Masalliq, Tozalash vositasi)"""

  name = models.CharField(max_length=50, verbose_name='Kategoriya nomi')

  def __str__(self):
    return self.name


class Expense(models.Model):
  """Xona uchun qilingan umumiy xarajat"""

  room = models.ForeignKey(
      Room,
      on_delete=models.CASCADE,
      related_name='expenses',
      verbose_name='Xona',
  )
  payer = models.ForeignKey(
      settings.AUTH_USER_MODEL,
      on_delete=models.CASCADE,
      related_name='paid_expenses',
      verbose_name='To`lagan talaba',
  )
  category = models.ForeignKey(
      ExpenseCategory,
      on_delete=models.SET_NULL,
      null=True,
      verbose_name='Kategoriya',
  )
  title = models.CharField(max_length=100, verbose_name='Xarajat nomi')
  amount = models.DecimalField(
      max_digits=10, decimal_places=2, verbose_name='Summa (so`m)'
  )
  created_at = models.DateTimeField(
      auto_now_add=True, verbose_name='Sana va vaqt'
  )

  def __str__(self):
    return f'{self.title} - {self.amount} so`m ({self.payer.username})'


class SharedPayment(models.Model):
  """Har bir talabaning bo'lingan qarzdorlik hisobi"""

  expense = models.ForeignKey(
      Expense,
      on_delete=models.CASCADE,
      related_name='shares',
      verbose_name='Xarajat',
  )
  debtor = models.ForeignKey(
      settings.AUTH_USER_MODEL,
      on_delete=models.CASCADE,
      related_name='debts',
      verbose_name='Qarzdor talaba',
  )
  share_amount = models.DecimalField(
      max_digits=10, decimal_places=2, verbose_name='Tushgan ulush summasi'
  )
  is_paid = models.BooleanField(
      default=False, verbose_name='To`landimi / Qaytarildimi'
  )

  def __str__(self):
    return f'{self.debtor.username} - {self.share_amount} so`m ({'To`langan' if self.is_paid else 'To`lanmagan'})'