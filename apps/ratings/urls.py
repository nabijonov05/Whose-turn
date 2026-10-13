from django.urls import include, path
from rest_framework.routers import DefaultRouter
from .views import PenaltyViewSet, StudentRatingViewSet

router = DefaultRouter()
router.register(r'penalties', PenaltyViewSet)
router.register(r'ratings', StudentRatingViewSet)

urlpatterns = [
    path('', include(router.urls)),
]