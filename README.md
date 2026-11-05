# Cari Cepat — pencarian langsung ke Shopee

Widget layar utama untuk membuka **hasil pencarian Shopee Indonesia** tanpa mampir ke tab video.

## Kenapa begini?

Widget Android (`RemoteViews`) **tidak mengizinkan kolom ketik (`EditText`)** di dalam widget.
Jadi alurnya dibuat sependek mungkin:

1. Ketuk widget di layar utama.
2. Dialog ketik langsung muncul, keyboard sudah terbuka.
3. Tulis barang → Enter / tombol cari.
4. Shopee terbuka langsung di halaman hasil pencarian.

Total: 1 ketukan + mengetik. Tidak ada tab video, tidak ada banner, hemat waktu/baterai/kuota.

## Fitur

- Widget 4x1 bergaya kolom pencarian.
- Riwayat pencarian (bisa dimatikan, hanya lokal di perangkat).
- Tombol "Pasang widget" langsung dari aplikasi (Android 8+).
- Deteksi Shopee terpasang; jika tidak ada, kata kunci disalin dan Play Store dibuka.
- Strategi deep link berlapis: `shopeeid://search?...` → `shopee://search?...` → App Link `https://shopee.co.id/search?...` → buka aplikasi Shopee.

## Build

Butuh JDK 17 dan Android SDK (platform 35, build-tools 35.0.0).

```powershell
gradle assembleDebug
```

APK: `app/build/outputs/apk/debug/app-debug.apk`

## Install

```powershell
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Atau salin APK ke HP lalu buka (izinkan "Instal dari sumber tidak dikenal").

## Catatan

- Ini aplikasi pihak ketiga, tidak berafiliasi dengan Shopee.
- Skema deep link Shopee tidak terdokumentasi resmi; aplikasi mencoba beberapa varian otomatis dan punya fallback ke browser/aplikasi Shopee.
- Jika Shopee mengubah skema internalnya, cukup sesuaikan `ShopeeLauncher.kt`.
