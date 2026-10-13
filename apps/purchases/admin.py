from django.contrib import admin
from .models import Expense, ExpenseCategory, SharedPayment


@admin.register(ExpenseCategory)
class ExpenseCategoryAdmin(admin.ModelAdmin):
  list_display = ('id', 'name')


@admin.register(Expense)
class ExpenseAdmin(admin.ModelAdmin):
  list_display = ('id', 'title', 'room', 'payer', 'amount', 'created_at')
  list_filter = ('category', 'created_at')


@admin.register(SharedPayment)
class SharedPaymentAdmin(admin.ModelAdmin):
  list_display = ('id', 'expense', 'debtor', 'share_amount', 'is_paid')
  list_filter = ('is_paid',)