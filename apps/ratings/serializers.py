from rest_framework import serializers
from apps.users.serializers import UserSerializer
from .models import Penalty, StudentRating


class PenaltySerializer(serializers.ModelSerializer):
  user = UserSerializer(read_only=True)

  class Meta:
    model = Penalty
    fields = '__all__'


class StudentRatingSerializer(serializers.ModelSerializer):
  user = UserSerializer(read_only=True)

  class Meta:
    model = StudentRating
    fields = [
        'id',
        'user',
        'score',
        'completed_duties_count',
        'failed_duties_count',
    ]