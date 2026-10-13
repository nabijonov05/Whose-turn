from django.urls import include, path
from rest_framework.routers import DefaultRouter
from .views import (
    DutyScheduleViewSet,
    DutySwapRequestViewSet,
    DutyTypeViewSet,
)

router = DefaultRouter()
router.register(r'duty-types', DutyTypeViewSet, basename='duty-type')
router.register(r'schedules', DutyScheduleViewSet, basename='schedule')
router.register(r'swap-requests', DutySwapRequestViewSet, basename='swap-request')

urlpatterns = [
    path('', include(router.urls)),
]