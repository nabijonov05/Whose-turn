from rest_framework import viewsets
from rest_framework.permissions import IsAuthenticated
from .models import Penalty, StudentRating
from .serializers import PenaltySerializer, StudentRatingSerializer


class PenaltyViewSet(viewsets.ModelViewSet):
  queryset = Penalty.objects.all()
  serializer_class = PenaltySerializer
  permission_classes = [IsAuthenticated]


class StudentRatingViewSet(viewsets.ModelViewSet):
  queryset = StudentRating.objects.all()
  serializer_class = StudentRatingSerializer
  permission_classes = [IsAuthenticated]