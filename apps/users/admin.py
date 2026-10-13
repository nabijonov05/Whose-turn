from django.contrib import admin
from django.contrib.auth.admin import UserAdmin
from .models import Block, Room, User


@admin.register(Block)
class BlockAdmin(admin.ModelAdmin):
  list_display = ('id', 'name')
  search_fields = ('name',)


@admin.register(Room)
class RoomAdmin(admin.ModelAdmin):
  list_display = ('id', 'number', 'block', 'capacity')
  list_filter = ('block',)
  search_fields = ('number',)


@admin.register(User)
class CustomUserAdmin(UserAdmin):
  list_display = (
      'username',
      'first_name',
      'last_name',
      'room',
      'role',
      'phone_number',
      'is_active',
  )
  list_filter = ('role', 'room__block', 'is_active')
  search_fields = ('username', 'first_name', 'last_name', 'phone_number', 'room__number')
  ordering = ('username',)
  fieldsets = UserAdmin.fieldsets + (
      (
          'Qo\'shimcha ma\'lumotlar',
          {
              'fields': (
                  'role',
                  'room',
                  'phone_number',
                  'profile_picture',
              )
          },
      ),
  )
  add_fieldsets = UserAdmin.add_fieldsets + (
      (
          'Qo\'shimcha ma\'lumotlar',
          {
              'fields': (
                  'first_name',
                  'last_name',
                  'role',
                  'room',
                  'phone_number',
              )
          },
      ),
  )