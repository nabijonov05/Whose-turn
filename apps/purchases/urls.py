from django.urls import include, path
from rest_framework.routers import DefaultRouter
from .views import (
    ExpenseCategoryViewSet,
    ExpenseViewSet,
    SharedPaymentViewSet,
)

router = DefaultRouter()
router.register(r'categories', ExpenseCategoryViewSet)
router.register(r'expenses', ExpenseViewSet)
router.register(r'shared-payments', SharedPaymentViewSet)

urlpatterns = [
    path('', include(router.urls)),
]