# Whose Turn - Mobile Ilova (Android Native Kotlin)

**Whose Turn** ("Kimning navbati?") — talabalar yotoqxonasida navbatchiliklar, umumiy xaridlar, xarajatlar taqsimoti va talabalar reytingini boshqarish uchun maxsus ishlab chiqilgan zamonaviy Android mobil ilovasi.

---

## 📱 Asosiy Imkoniyatlar va Bo'limlar

1. **Kirish va Ro'yxatdan o'tish (Auth):**
   - JWT token asosidagi xavfsiz autentifikatsiya.
   - Yangi talabalar uchun qulay ro'yxatdan o'tish oynasi (blok va xonani tanlash bilan).
   - Server manzilini ilovaning o'zidan o'zgartirish imkoniyati (Emulator, Real telefon, Wi-Fi IP).

2. **Navbatchilik Jadvali (Duties):**
   - **"Bugun kimning navbati?"** - Bugungi kun uchun bosh banner.
   - Mening navbatlarim / Xona navbatlari / Bajarilgan / Kutilayotgan filtrlar.
   - **"Bajarildi"** - Navbatni yakunlangan deb belgilash.
   - **"Almashtirish" (Duty Swap)** - Navbatni boshqa xonadoshga topshirish so'rovi.
   - Yangi navbatchilik biriktirish (FAB tugmasi orqali).

3. **Xaridlar va Qarzdorlik (Expenses & Splits):**
   - Xona bo'yicha umumiy xarajatlar statistikasi.
   - **"Mening qarzlarim"** - Xonadoshlar olgan narsalardan tushgan ulushlar va bir tugma bilan "To'landi" qilish.
   - Yangi xarajat qo'shganda barcha xonadoshlarga avtomatik teng bo'lib chiqish.

4. **Talabalar Reytingi (Leaderboard):**
   - Eng yaxshi talabalar ro'yxati (ballar bo'yicha tartiblangan).
   - 1, 2 va 3-o'rinlar uchun 🥇, 🥈, 🥉 medallar.
   - Muvaffaqiyatli bajarilgan navbatlar soni.

5. **Bildirishnomalar (Notifications):**
   - Navbat eslatmalari, almashtirish so'rovlari va jarima xabarlari.
   - So'rovlarni to'g'ridan-to'g'ri bildirishnomadan qabul qilish yoki rad etish.

6. **Profil (Profile):**
   - Talabaning shaxsiy ma'lumotlari, roli, blok va xona raqami.
   - Xonadoshlar ro'yxati va ularning telefon raqamlari.
   - Tizimdan xavfsiz chiqish (Logout).

---

## 🛠 Ishlatilgan Texnologiyalar

- **Til:** Kotlin 2.0 (100% Native)
- **UI:** Material Design 3, ViewBinding, ConstraintLayout, SwipeRefreshLayout
- **Tarmoq (Network):** Retrofit 2, OkHttp 3, Gson, HttpLoggingInterceptor
- **Autentifikatsiya:** JWT TokenManager (SharedPreferences), AuthInterceptor
- **Arxitektura:** Repository Pattern, Coroutines, AndroidX Lifecycle, ViewModel

---

## 🚀 Ishga Tushirish Qo'llanmasi

### 1. Django Backendni ishga tushirish:
Terminalda quyidagi buyruqni bering:
```bash
.\venv\Scripts\python.exe manage.py runserver 0.0.0.0:8000
```

> **Eslatma:**
> - Android Emulator ishlatilganda backend manzili: `http://10.0.2.2:8000/api/v1/`
> - Haqiqiy telefon kompyuter bilan bir xil Wi-Fi tarmog'iga ulanganda: `http://192.168.x.x:8000/api/v1/`

### 2. Android Studio'da ochish:
- Android Studio (`D:\android_studio`) dasturini oching.
- **Open** tugmasini bosing va ushbu papkani tanlang:
  `c:\Users\user\Desktop\Whose-turn\mobile_app`
- Emulator yoki ulangan telefonni tanlab **Run (Shift+F10)** tugmasini bosing.

### 3. Tayyor APK fayli:
Loyiha allaqachon muvaffaqiyatli kompilyatsiya qilingan va tayyor `.apk` fayli mavjud:
- `c:\Users\user\Desktop\Whose-turn\WhoseTurn-v1.0.apk`

---

## 🔑 Test Uchun Tayyor Hisoblar

| Username | Parol | Roli | Xona |
|---|---|---|---|
| `jasur` | `123456` | Talaba | 905-xona |
| `sardor` | `123456` | Blok sardori | 905-xona |
| `bekzod` | `123456` | Talaba | 905-xona |
| `admin` | `admin123` | Komendant / Admin | 905-xona |
