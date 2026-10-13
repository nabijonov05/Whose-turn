from rest_framework import serializers
from apps.users.models import Room, User
from apps.users.serializers import RoomSerializer, UserSerializer
from .models import DutySchedule, DutySwapRequest, DutyType


class DutyTypeSerializer(serializers.ModelSerializer):

  class Meta:
    model = DutyType
    fields = '__all__'


class DutyScheduleSerializer(serializers.ModelSerializer):
  room = RoomSerializer(read_only=True)
  assigned_to = UserSerializer(read_only=True)
  duty_type = DutyTypeSerializer(read_only=True)

  room_id = serializers.PrimaryKeyRelatedField(
      queryset=Room.objects.all(), source='room', write_only=True
  )
  assigned_to_id = serializers.PrimaryKeyRelatedField(
      queryset=User.objects.all(), source='assigned_to', write_only=True
  )
  duty_type_id = serializers.PrimaryKeyRelatedField(
      queryset=DutyType.objects.all(), source='duty_type', write_only=True
  )

  date = serializers.DateField(required=False, allow_null=True)

  class Meta:
    model = DutySchedule
    fields = [
        'id',
        'room',
        'room_id',
        'assigned_to',
        'assigned_to_id',
        'duty_type',
        'duty_type_id',
        'date',
        'order_index',
        'status',
        'proof_image',
    ]


class DutySwapRequestSerializer(serializers.ModelSerializer):
  requester = UserSerializer(read_only=True)
  requested_to = UserSerializer(read_only=True)
  duty_schedule = DutyScheduleSerializer(read_only=True)

  requested_to_id = serializers.PrimaryKeyRelatedField(
      queryset=User.objects.all(), source='requested_to', write_only=True
  )
  duty_schedule_id = serializers.PrimaryKeyRelatedField(
      queryset=DutySchedule.objects.all(), source='duty_schedule', write_only=True
  )

  class Meta:
    model = DutySwapRequest
    fields = [
        'id',
        'requester',
        'requested_to',
        'requested_to_id',
        'duty_schedule',
        'duty_schedule_id',
        'reason',
        'status',
        'created_at',
    ]
    read_only_fields = ['requester']