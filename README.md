# Lumina RAW - Pro Photo Editor & Aesthetic Collage

Aplikasi editing foto pro bergaya Adobe Lightroom dengan dukungan RAW, AI Tone Matcher menggunakan Google Gemini, ekspor DNG/JPG kualitas maksimal, serta Aesthetic Collage Studio.

---

## Cara Build & Mendapatkan File APK di GitHub

Untuk mem-build project ini menjadi file `.apk` secara otomatis menggunakan **GitHub Actions**, ikuti langkah-langkah mudah berikut:

### 1. Push / Hubungkan Project ke GitHub
- Dari tampilan **Google AI Studio**, klik menu pengaturan / tombol **Export to GitHub** (atau unduh sebagai ZIP lalu upload/push ke repository GitHub Anda).

### 2. Jalankan Build APK Otomatis (GitHub Actions)
Repository ini sudah dilengkapi workflow otomatis di `.github/workflows/build-apk.yml`.
- Setiap kali Anda melakukan **Push** ke branch `main` atau `master`, GitHub Actions akan otomatis mem-build APK.
- **Cara Menjalankan Manual**:
  1. Buka repository GitHub Anda di browser.
  2. Klik tab **Actions** di bagian atas repository.
  3. Di menu sebelah kiri, pilih workflow **"Build Android APK"**.
  4. Klik tombol **Run workflow** -> pilih branch `main` -> klik tombol hijau **Run workflow**.

### 3. Download File APK yang Sudah Jadi
1. Setelah proses build selesai (bertanda centang hijau ✅), klik judul workflow yang baru saja dijalankan (misal: *"Build Android APK"*).
2. Gulir ke bagian paling bawah pada halaman detail build, cari section **Artifacts**.
3. Klik pada file **`Lumina-RAW-app-debug`**.
4. File ZIP berisi **`app-debug.apk`** akan otomatis terunduh ke komputer / HP Anda.
5. Ekstrak ZIP dan instal file `app-debug.apk` langsung ke perangkat Android Anda!

---

### Opsi Tambahan: Konfigurasi Gemini API Key di GitHub (Opsional)
Jika ingin API Key Gemini disertakan otomatis saat build di GitHub:
1. Masuk ke repository GitHub -> **Settings** -> **Secrets and variables** -> **Actions**.
2. Klik **New repository secret**.
3. Masukkan Name: `GEMINI_API_KEY` dan Value: *(kunci API Gemini Anda)*.
4. Simpan. Kunci ini akan otomatis di-inject ke file `.env` saat proses build APK berjalan.
*(Catatan: Anda juga tetap dapat memasukkan API Key secara manual langsung di dalam aplikasi melalui menu Settings).*

---

### Cara Langsung dari Google AI Studio (Tanpa GitHub Actions)
Anda juga bisa membuat APK langsung dari dalam antarmuka AI Studio:
1. Buka menu project / settings di pojok kanan atas Google AI Studio.
2. Pilih opsi **Generate APK** / **Export**.
3. Unduh berkas APK yang dihasilkan langsung ke perangkat Anda.
