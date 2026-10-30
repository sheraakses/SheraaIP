# SheraaIP Android

Project Android (Kotlin + Jetpack Compose), dark dashboard for managing DataImpulse proxy endpoint settings.

## Build otomatis melalui GitHub Actions
Workflow disediakan di `.github/workflows/android.yml`. Setelah project diunggah ke repository GitHub pada branch `main`, workflow akan berjalan ketika ada push atau dapat dijalankan manual melalui Actions.

**Penting:** Project harus menyertakan Gradle Wrapper (`gradlew`, `gradle/wrapper/gradle-wrapper.jar`, dan `gradle-wrapper.properties`). Jika belum ada, buat wrapper dengan Gradle 8.9 pada lingkungan yang memiliki Gradle, lalu commit semua file wrapper.

## Fitur saat ini
- Form endpoint HTTP proxy: host, port, username, password.
- Simpan host, port, username, dan negara pilihan secara lokal (password tidak disimpan).
- Tes endpoint dengan membuka `https://api.ipify.org` melalui HTTP proxy.
- Label kategori Residential / Datacenter / Mobile dan pilihan negara.
- Nama aplikasi Android: SheraaIP.

## Batasan
Ini adalah pengelola konfigurasi dan alat tes endpoint, bukan VPN Android. Aplikasi belum mengalihkan seluruh trafik perangkat. Pilihan negara/jenis hanya label UI; endpoint harus sesuai paket DataImpulse. Tes koneksi saat ini memakai HTTP proxy.
