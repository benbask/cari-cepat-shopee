# Cari Cepat — pencarian langsung ke Shopee

Widget layar utama untuk membuka **hasil pencarian Shopee Indonesia** tanpa mampir ke tab video.

## Unduh APK

APK siap pasang ada di halaman [Releases](https://github.com/benbask/cari-cepat-shopee/releases/latest).

## Kenapa begini?

Widget Android (`RemoteViews`) **tidak mengizinkan kolom ketik (`EditText`)** di dalam widget.
Jadi alurnya dibuat sependek mungkin:

1. Ketuk widget di layar utama.
2. Dialog ketik langsung muncul, keyboard sudah terbuka.
3. Tulis barang → Enter / tombol cari.
4. Shopee terbuka langsung di halaman hasil pencarian.

Total: 1 ketukan + mengetik. Tidak ada tab video, tidak ada banner, hemat waktu/baterai/kuota.

## Fitur

- Widget 4x1 ringkas, tinggi kartu sekitar 54dp (mirip search bar Google).
- Kustomisasi tampilan widget (v1.4+): 4 tema berupa mini widget yang bisa diketuk (Putih, Oranye Shopee, Gelap, Mint), slider transparansi 0–100%, sudut kecil/sedang/besar, ukuran teks, dan tombol Cari bisa disembunyikan. Ada pratinjau langsung di aplikasi.
- Tekan lama widget di layar utama, lalu pilih tombol Edit/Setelan untuk membuka halaman tampilan widget (v1.7+).
- Riwayat pencarian (bisa dimatikan, hanya lokal di perangkat).
- Tombol "Pasang widget" langsung dari aplikasi (Android 8+).
- Deteksi Shopee terpasang; jika tidak ada, kata kunci disalin dan Play Store dibuka.
- Kata kunci selalu disalin ke clipboard sebagai cadangan.
- Strategi deep link berlapis (v1.2): App Link `https://shopee.co.id/search?…` lebih dulu, lalu rute internal `shopeeid://home?apprl=rn/@shopee-rn/search/SEARCH_RESULT` dengan `searchPageType=GLOBAL` + `searchParams`, lalu varian `search-result`, `PRE_SEARCH`, dan `shopeeid://search`.
- Alat uji tautan bawaan (v1.1+) untuk mencoba 10 format tautan satu per satu; metode yang berhasil bisa disimpan sebagai default.

## Jika aplikasi gagal dibuka (v1.1+)

Mulai v1.1, aplikasi menangkap error dan menampilkannya di layar:

1. Buka aplikasi sekali lagi.
2. Layar "Aplikasi gagal dibuka" akan muncul beserta detail teknis.
3. Ketuk **Salin detail error**, lalu kirimkan teksnya ke pengembang.

## Jika pencarian tidak muncul

Buka aplikasi → ketuk **"Tautan tidak membuka hasil pencarian? Uji format lain di sini"**. Mulai dari nomor 1 (App Link https), lalu nomor 2–7 (rute internal). Setelah kembali ke aplikasi, jawab **"Ya, simpan"** pada metode yang membuka halaman hasil pencarian — metode itu akan dipakai otomatis untuk pencarian berikutnya (ditandai ★).

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
