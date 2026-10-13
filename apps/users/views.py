from rest_framework import generics, permissions, status, viewsets
from rest_framework.decorators import action
from rest_framework.parsers import FormParser, JSONParser, MultiPartParser
from rest_framework.response import Response
from .models import Block, Room, User
from .serializers import (
    BlockSerializer,
    RegisterSerializer,
    RoomSerializer,
    UserSerializer,
)


class RegisterView(generics.CreateAPIView):
  queryset = User.objects.all()
  serializer_class = RegisterSerializer
  permission_classes = [permissions.AllowAny]


class BlockViewSet(viewsets.ModelViewSet):
  queryset = Block.objects.all()
  serializer_class = BlockSerializer


class RoomViewSet(viewsets.ModelViewSet):
  queryset = Room.objects.all()
  serializer_class = RoomSerializer


class UserViewSet(viewsets.ModelViewSet):
  queryset = User.objects.all()
  serializer_class = UserSerializer
  permission_classes = [permissions.IsAuthenticated]
  parser_classes = [MultiPartParser, FormParser, JSONParser]

  def get_object(self):
    # Talaba faqat o'z profil ma'lumotlarini ko'rishi va tahrirlashi uchun
    return self.request.user

  @action(
      detail=False,
      methods=['get', 'put', 'patch'],
      parser_classes=[MultiPartParser, FormParser, JSONParser]
  )
  def me(self, request):
    user = request.user
    if request.method == 'GET':
      serializer = self.get_serializer(user)
      return Response(serializer.data)

    # Room/Block update
    block_name = request.data.get('block_name')
    room_number = request.data.get('room_number')
    if room_number:
      clean_block = (block_name or (user.room.block.name if user.room else "A-blok")).strip()
      if not clean_block.lower().endswith("blok") and not clean_block.lower().endswith("block"):
        clean_block = f"{clean_block.upper()}-blok"
      block, _ = Block.objects.get_or_create(name=clean_block)
      room, _ = Room.objects.get_or_create(number=str(room_number).strip(), block=block, defaults={'capacity': 4})
      user.room = room
      user.save()

    if 'profile_picture' in request.FILES:
      user.profile_picture = request.FILES['profile_picture']
      user.save()

    serializer = self.get_serializer(user, data=request.data, partial=True)
    serializer.is_valid(raise_exception=True)
    serializer.save()
    return Response(self.get_serializer(user).data)

  @action(detail=False, methods=['post'], url_path='change-password')
  def change_password(self, request):
    old_password = request.data.get('old_password')
    new_password = request.data.get('new_password')
    if not request.user.check_password(old_password):
      return Response({'detail': "Eski parol noto'g'ri!"}, status=status.HTTP_400_BAD_REQUEST)
    if not new_password or len(new_password) < 4:
      return Response({'detail': "Yangi parol kamida 4 belgidan iborat bo'lishi kerak!"}, status=status.HTTP_400_BAD_REQUEST)
    request.user.set_password(new_password)
    request.user.save()
    return Response({'detail': "Parol muvaffaqiyatli o'zgartirildi!"})

  @action(detail=False, methods=['get'])
  def roommates(self, request):
    if request.user.room:
      students = User.objects.filter(room=request.user.room).exclude(id=request.user.id)
      serializer = self.get_serializer(students, many=True)
      return Response(serializer.data)
    return Response([])
