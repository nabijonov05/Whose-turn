from django.urls import include, path
from rest_framework.routers import DefaultRouter
from .views import BlockViewSet, RegisterView, RoomViewSet, UserViewSet

router = DefaultRouter()
router.register(r'blocks', BlockViewSet)
router.register(r'rooms', RoomViewSet)
router.register(r'profile', UserViewSet, basename='user-profile')

urlpatterns = [
    path('auth/register/', RegisterView.as_view(), name='auth-register'),
    path('', include(router.urls)),
]