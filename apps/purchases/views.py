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

  def get_queryset(self):
    if not ExpenseCategory.objects.exists():
      for name in ['Oziq-ovqat', 'Tozalash vositalari', 'Kommunal', 'Boshqa']:
        ExpenseCategory.objects.get_or_create(name=name)
    return ExpenseCategory.objects.all().order_by('id')


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
    if not user.room:
      from apps.users.models import Block, Room
      default_block, _ = Block.objects.get_or_create(name="A-blok")
      default_room, _ = Room.objects.get_or_create(number="101", block=default_block, defaults={'capacity': 4})
      user.room = default_room
      user.save(update_fields=['room'])

    category = serializer.validated_data.get('category')
    if not category:
      category, _ = ExpenseCategory.objects.get_or_create(name="Boshqa")

    expense = serializer.save(
        payer=user, room=user.room, category=category
    )

    if user.room:
      room_students = user.room.students.exclude(id=user.id)
      if room_students.exists():
        share_amount = round(expense.amount / (room_students.count() + 1), 2)
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