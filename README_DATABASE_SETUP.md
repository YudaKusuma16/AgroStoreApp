# Setup Database MySQL untuk AgroStore

## Langkah 1: Install Database

### Install XAMPP (Recommended)
1. Download dan install XAMPP dari https://www.apachefriends.org/
2. Install di folder `C:\xampp` (default)
3. Buka XAMPP Control Panel
4. Start Apache dan MySQL

### Install MySQL Standalone
Jika tidak ingin menggunakan XAMPP, install MySQL dari:
- https://dev.mysql.com/downloads/mysql/
- Atau gunakan MariaDB: https://mariadb.org/download/

## Langkah 2: Setup Database

1. Buka phpMyAdmin (http://localhost/phpmyadmin)
2. Buat database baru dengan nama `agrostore`
3. Import file SQL yang ada di folder `database/agrostore_mysql_schema.sql`

Atau jalankan via command line:
```bash
mysql -u root -p
CREATE DATABASE agrostore;
USE agrostore;
SOURCE database/agrostore_mysql_schema.sql;
```

## Langkah 3: Setup API Backend

1. Copy folder `api` ke dalam `C:\xampp\htdocs\agrostore`
2. Pastikan struktur folder:
```
C:\xampp\htdocs\agrostore\
├── api\
│   ├── config.php
│   ├── auth\
│   │   ├── login.php
│   │   └── register.php
│   ├── products\
│   │   ├── index.php
│   │   └── show.php
│   └── orders\
│       └── index.php
```

3. Edit file `api/config.php` jika menggunakan password berbeda:
```php
define('DB_HOST', 'localhost');
define('DB_NAME', 'agrostore');
define('DB_USER', 'root');
define('DB_PASS', ''); // Ganti jika ada password
```

## Langkah 4: Testing API

Buka browser dan test API endpoints:

- Test Products: http://localhost/agrostore/api/products/index.php
- Test Login: http://localhost/agrostore/api/auth/login.php

## Langkah 5: Konfigurasi Aplikasi Android

1. Buka file `app/src/main/java/com/apk/agrostore/data/remote/RetrofitClient.kt`
2. Sesuaikan BASE_URL:
   - Untuk Android Emulator: `http://10.0.2.2/agrostore/api/` (sudah default)
   - Untuk device fisik: ganti dengan IP komputer, contoh `http://192.168.1.100/agrostore/api/`

### Cara mengetahui IP komputer:
- Windows: Buka CMD, ketik `ipconfig`
- Cari IPv4 Address (biasanya 192.168.1.x atau 192.168.0.x)

## Langkah 6: Testing Aplikasi

1. Build dan run aplikasi Android
2. Test login dengan:
   - Email: petani@agro.com
   - Password: 123456

## Data Default

### Users:
- **Pembeli 1**: petani@agro.com / 123456
- **Pembeli 2**: siti@agro.com / 123456
- **Penjual 1**: toko@agro.com / 123456
- **Penjual 2**: benih@agro.com / 123456
- **Penjual 3**: alat@agro.com / 123456

### Produk:
10 sample produk sudah tersedia dengan kategori:
- Pupuk
- Alat Pertanian
- Benih
- Alat Semprot
- Alat Panen
- Perlengkapan
- Pestisida

## Troubleshooting

### 1. API tidak bisa diakses
- Pastikan Apache dan MySQL sudah running di XAMPP
- Cek port Apache (biasanya 80) tidak bentrok
- Pastikan folder API sudah di `htdocs`

### 2. Connection refused
- Cek firewall Windows
- Pastikan BASE_URL di RetrofitClient sudah benar

### 3. Database connection error
- Pastikan MySQL service running
- Cek username dan password di config.php
- Pastikan database agrostore sudah dibuat

### 4. Android tidak bisa akses localhost
- Untuk emulator gunakan `10.0.2.2`
- Untuk device fisik gunakan IP komputer
- Pastikan device dan komputer di WiFi yang sama

## Fitur yang sudah tersedia:
- ✅ Login/Register
- ✅ Lihat Produk
- ✅ Search Produk
- ✅ Tambah ke Keranjang
- ✅ Checkout
- ✅ Riwayat Pesanan
- ✅ Tambah Produk (Penjual)
- ✅ Edit/Hapus Produk (Penjual)

## Note:
- Password user default: `123456`
- Status checkout langsung "Dibayar" (sesuai requirement)
- API menggunakan RESTful dengan response format JSON