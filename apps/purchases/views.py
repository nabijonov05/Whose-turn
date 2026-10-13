from rest_framework import viewsets
from rest_framework.permissions import IsAuthenticated
from .models import Expense, ExpenseCategory, SharedPayment
from .serializers import (
    ExpenseCategorySerializer,
    ExpenseSerializer,
    SharedPaymentSerializer,
)


class ExpenseCategoryViewSet(viewsets.ModelViewSet):
  queryset = ExpenseCategory.objects.all()
  serializer_class = ExpenseCategorySerializer
  permission_classes = [IsAuthenticated]


class ExpenseViewSet(viewsets.ModelViewSet):
  queryset = Expense.objects.all()
  serializer_class = ExpenseSerializer
  permission_classes = [IsAuthenticated]

  def get_queryset(self):
    user = self.request.user
    if user.room:
      return Expense.objects.filter(room=user.room).order_by('-created_at')
    return Expense.objects.all().order_by('-created_at')

  def perform_create(self, serializer):
    user = self.request.user
    expense = serializer.save(
        payer=user, room=user.room
    )

    if user.room:
      room_students = user.room.students.exclude(id=user.id)
      if room_students.exists():
        share_amount = expense.amount / (room_students.count() + 1)
        for student in room_students:
          SharedPayment.objects.create(
              expense=expense, debtor=student, share_amount=share_amount
          )


class SharedPaymentViewSet(viewsets.ModelViewSet):
  queryset = SharedPayment.objects.all()
  serializer_class = SharedPaymentSerializer
  permission_classes = [IsAuthenticated]

  def get_queryset(self):
    # Talaba faqat o'zining qarzlarini ko'rishi uchun
    return SharedPayment.objects.filter(debtor=self.request.user).order_by('-id')