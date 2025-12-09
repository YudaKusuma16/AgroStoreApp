# AGROSTORE: Project Context & Development Guidelines

Dokumen ini berisi spesifikasi teknis dan roadmap pengembangan untuk aplikasi Android "AgroStore".
**Konteks:** Tugas Kuliah / Proyek Akademik.
**Tujuan:** Mendemonstrasikan penerapan arsitektur MVVM, Jetpack Compose, dan integrasi Database.

## 1. Project Overview
* **Nama Aplikasi:** AgroStore
* **Platform:** Android (Mobile)
* **Bahasa:** Kotlin
* **UI Framework:** Jetpack Compose (Material Design 3)
* **Arsitektur:** MVVM (Model-View-ViewModel) dengan Repository Pattern.
* **Database:** MariaDB.
    * *Catatan Penting:* Karena ini tugas kuliah, komunikasi ke MariaDB idealnya tetap menggunakan REST API (Retrofit). Namun, jika backend belum tersedia, AI harus siap membuatkan "FakeRepository" (Mock Data) terlebih dahulu agar aplikasi Android tetap bisa berjalan dan dinilai.

## 2. Tech Stack & Library Rules
* **Dependency Injection:** Hilt (Recommended) atau Manual DI (jika Hilt terlalu berat untuk tugas). -> *Kita gunakan Hilt agar nilai lebih bagus.*
* **Network:** Retrofit + OkHttp + Gson.
* **Async:** Coroutines + Flow.
* **Image Loading:** Coil.
* **Navigation:** Jetpack Compose Navigation.
* **Storage:** DataStore (untuk simpan status login user).

## 3. Aturan Kerja AI (SANGAT PENTING)
1.  **Coding Bertahap (Step-by-step):** Jangan generate semua kode sekaligus. Ikuti **PHASE** di bawah.
2.  **Simulasi Pembayaran:** Jangan buat integrasi Payment Gateway nyata. Gunakan logika "Dummy": User klik bayar -> Loading 2 detik -> Sukses.
3.  **Error Handling:** Pastikan aplikasi tidak crash jika data kosong.
4.  **Penjelasan:** Berikan komentar singkat pada kode untuk bahan belajar/presentasi tugas.

## 4. User Roles & Features

### A. Pembeli (Petani / Konsumen)
1.  **Auth:** Login & Register Sederhana.
2.  **Produk:**
    * List Produk & Pencarian.
    * Detail Produk.
3.  **Transaksi (Simulasi):**
    * Add to Cart.
    * Checkout: Mengisi alamat (form biasa).
    * **Metode Pembayaran:** Pilihan Dropdown (Transfer Bank, COD, E-Wallet).
    * **Proses Bayar:** Tombol "Bayar Sekarang" yang langsung mengubah status pesanan menjadi "Dibayar" (Tanpa validasi bank nyata).
    * Riwayat Pesanan.
4.  **Ulasan:** Input rating (bintang 1-5) dan teks.

### B. Penjual (Toko Pertanian)
1.  **Auth:** Login khusus penjual.
2.  **Manajemen Produk:**
    * Input Produk Baru.
    * *Upload Gambar:* Gunakan gambar dari galeri HP, lalu konversi ke Base64 string (simpan ke DB) ATAU simpan path lokal saja jika server belum siap.
3.  **Manajemen Pesanan:**
    * Ubah status: "Dikemas" -> "Dikirim" -> "Selesai".
4.  **Laporan:** Tampilan teks sederhana total pendapatan (Dummy/Hitungan kasar).

## 5. Struktur Data (Schema Representation)
* `users`: id, name, email, password, role.
* `products`: id, name, category, price, stock, description, image_path.
* `orders`: id, user_id, total, status, payment_method.
* `reviews`: id, product_id, user_id, rating, comment.

## 6. Development Roadmap (Bertahap)

* **Phase 1: Project Setup**
    * Setup Gradle (Compose, Hilt, Retrofit, Coil).
    * Struktur Folder (Clean Architecture: Data, Domain, Presentation).

* **Phase 2: Data Layer (Mocking First)**
    * Membuat Model (Data Class).
    * Membuat "FakeRepository" yang berisi data dummy (List produk palsu) agar UI bisa dikerjakan dulu tanpa pusing mikirin Database/API di awal. **Ini strategi terbaik untuk tugas kuliah.**

* **Phase 3: Auth & Navigation**
    * Login Screen UI & Logic.
    * Navigasi antar halaman.

* **Phase 4: Fitur Penjual**
    * Halaman Input Produk (Form).
    * List Produk Penjual.

* **Phase 5: Fitur Pembeli (Browsing)**
    * Home Screen (Grid Produk).
    * Detail Screen.

* **Phase 6: Fitur Pembeli (Checkout Simulasi)**
    * Cart Screen.
    * Checkout Screen (Form Alamat + Pilih Metode Bayar).
    * Dialog "Pembayaran Berhasil".

* **Phase 7: Integrasi API / Database (Opsional/Terakhir)**
    * Jika sisa waktu cukup, ubah FakeRepository menjadi RealRepository, saya ingin menggunakan MySQL.
