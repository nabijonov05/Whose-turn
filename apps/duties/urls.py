from django.urls import include, path
from rest_framework.routers import DefaultRouter
from .views import (
    DutyScheduleViewSet,
    DutySwapRequestViewSet,
    DutyTypeViewSet,
)

router = DefaultRouter()
router.register(r'duty-types', DutyTypeViewSet)
router.register(r'schedules', DutyScheduleViewSet)
router.register(r'swap-requests', DutySwapRequestViewSet)

urlpatterns = [
    path('', include(router.urls)),
]