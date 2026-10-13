from django.contrib.auth.models import AbstractUser
from django.db import models


class Block(models.Model):
  """Yotoqxona bloki yoki binosi (masalan: A-blok, 3-qavat)"""

  name = models.CharField(
      max_length=50, unique=True, verbose_name="Blok nomi"
  )
  description = models.TextField(
      blank=True, null=True, verbose_name="Tavsif"
  )

  def __str__(self):
    return self.name


class Room(models.Model):
  """Xonalar modeli"""

  number = models.CharField(
      max_length=10, unique=True, verbose_name="Xona raqami"
  )
  block = models.ForeignKey(
      Block,
      on_delete=models.CASCADE,
      related_name="rooms",
      verbose_name="Blok",
  )
  capacity = models.IntegerField(default=4, verbose_name="Xona sig'imi")

  def __str__(self):
    return f"{self.block.name} - {self.number}-xona"


class User(AbstractUser):
  """Maxsus foydalanuvchi (Talaba, Sardor, Komendant)"""

  ROLE_CHOICES = (
      ("student", "Talaba"),
      ("block_leader", "Blok sardori"),
      ("admin", "Komendant / Admin"),
  )

  role = models.CharField(
      max_length=20, choices=ROLE_CHOICES, default="student", verbose_name="Roli"
  )
  room = models.ForeignKey(
      Room,
      on_delete=models.SET_NULL,
      null=True,
      blank=True,
      related_name="students",
      verbose_name="Xonasi",
  )
  phone_number = models.CharField(
      max_length=15, unique=True, null=True, blank=True, verbose_name="Telefon"
  )

  # Profil uchun rasm yuklash maydoni
  profile_picture = models.ImageField(
      upload_to="profile_pics/",
      blank=True,
      null=True,
      verbose_name="Profil rasmi",
  )

  def __str__(self):
    return f"{self.first_name} {self.last_name} ({self.username}) - {self.room if self.room else 'Xonasiz'}"