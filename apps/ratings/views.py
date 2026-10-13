from rest_framework import viewsets
from rest_framework.permissions import IsAuthenticated
from .models import Penalty, StudentRating
from .serializers import PenaltySerializer, StudentRatingSerializer


class PenaltyViewSet(viewsets.ModelViewSet):
  serializer_class = PenaltySerializer
  permission_classes = [IsAuthenticated]

  def get_queryset(self):
    # Faqat o'z xonasidagi jarimalarni ko'rsatish
    user = self.request.user
    if user.room:
      return Penalty.objects.filter(user__room=user.room).order_by('-created_at')
    return Penalty.objects.filter(user=user).order_by('-created_at')


class StudentRatingViewSet(viewsets.ModelViewSet):
  serializer_class = StudentRatingSerializer
  permission_classes = [IsAuthenticated]

  def get_queryset(self):
    user = self.request.user
    # Xonadoshlarning reytinglarini ko'rsatish (Ranking bo'limi)
    if user.room:
      # O'z xonasidagilarning reytingi + boshqa barcha talabalar
      return StudentRating.objects.filter(
          user__room=user.room
      ).select_related('user', 'user__room', 'user__room__block').order_by('-score')
    return StudentRating.objects.all().select_related(
        'user', 'user__room', 'user__room__block'
    ).order_by('-score')