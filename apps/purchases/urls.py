from django.urls import include, path
from rest_framework.routers import DefaultRouter
from .views import (
    ExpenseCategoryViewSet,
    ExpenseViewSet,
    SharedPaymentViewSet,
)

router = DefaultRouter()
router.register(r'categories', ExpenseCategoryViewSet, basename='expensecategory')
router.register(r'expenses', ExpenseViewSet, basename='expense')
router.register(r'shared-payments', SharedPaymentViewSet, basename='sharedpayment')

urlpatterns = [
    path('', include(router.urls)),
]