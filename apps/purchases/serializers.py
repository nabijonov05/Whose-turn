from rest_framework import serializers
from apps.users.serializers import UserSerializer
from .models import Expense, ExpenseCategory, SharedPayment


class ExpenseCategorySerializer(serializers.ModelSerializer):

  class Meta:
    model = ExpenseCategory
    fields = '__all__'


class SharedPaymentSerializer(serializers.ModelSerializer):
  debtor = UserSerializer(read_only=True)

  class Meta:
    model = SharedPayment
    fields = ['id', 'debtor', 'share_amount', 'is_paid']


class ExpenseSerializer(serializers.ModelSerializer):
  payer = UserSerializer(read_only=True)
  category = ExpenseCategorySerializer(read_only=True)
  shares = SharedPaymentSerializer(many=True, read_only=True)

  category_id = serializers.PrimaryKeyRelatedField(
      queryset=ExpenseCategory.objects.all(), source='category', write_only=True,
      required=False, allow_null=True
  )

  class Meta:
    model = Expense
    fields = [
        'id',
        'room',
        'payer',
        'category',
        'category_id',
        'title',
        'amount',
        'shares',
        'created_at',
    ]
    read_only_fields = ['payer', 'room']