from rest_framework import serializers
from .models import Block, Room, User


class BlockSerializer(serializers.ModelSerializer):

  class Meta:
    model = Block
    fields = '__all__'


class RoomSerializer(serializers.ModelSerializer):
  block = BlockSerializer(read_only=True)
  block_id = serializers.PrimaryKeyRelatedField(
      queryset=Block.objects.all(), source='block', write_only=True
  )

  class Meta:
    model = Room
    fields = ['id', 'number', 'block', 'block_id', 'capacity']


class UserSerializer(serializers.ModelSerializer):
  room = RoomSerializer(read_only=True)
  room_id = serializers.PrimaryKeyRelatedField(
      queryset=Room.objects.all(), source='room', write_only=True, allow_null=True
  )

  class Meta:
    model = User
    fields = [
        'id',
        'username',
        'first_name',
        'last_name',
        'phone_number',
        'role',
        'room',
        'room_id',
        'profile_picture',
    ]
    read_only_fields = ['role']


class RegisterSerializer(serializers.ModelSerializer):
  password = serializers.CharField(write_only=True, required=True)
  block_name = serializers.CharField(write_only=True, required=False, allow_blank=True)
  room_number = serializers.CharField(write_only=True, required=False, allow_blank=True)
  room_id = serializers.PrimaryKeyRelatedField(
      queryset=Room.objects.all(), source='room', write_only=True, required=False, allow_null=True
  )

  class Meta:
    model = User
    fields = [
        'id',
        'username',
        'password',
        'first_name',
        'last_name',
        'phone_number',
        'role',
        'room_id',
        'block_name',
        'room_number',
    ]

  def create(self, validated_data):
    password = validated_data.pop('password')
    block_name = validated_data.pop('block_name', None)
    room_number = validated_data.pop('room_number', None)
    room = validated_data.pop('room', None)

    if room_number:
      clean_block = (block_name or "A").strip()
      if not clean_block.lower().endswith("blok") and not clean_block.lower().endswith("block"):
        clean_block = f"{clean_block.upper()}-blok"
      block, _ = Block.objects.get_or_create(name=clean_block)
      room, _ = Room.objects.get_or_create(number=str(room_number).strip(), block=block, defaults={'capacity': 4})

    user = User(**validated_data)
    if room:
      user.room = room
    user.set_password(password)
    user.save()
    from apps.ratings.models import StudentRating
    StudentRating.objects.get_or_create(user=user)
    return user
