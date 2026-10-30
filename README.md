# Retail Kopdes - Aplikasi Kasir Koperasi Desa

Aplikasi kasir (*Point of Sale*) desktop berbasis **Java Swing**, **FlatLaf**, dan **SQLite** yang dirancang agar tetap ringkas, rapi di tab **Design** Apache NetBeans, serta mudah dijelaskan baris demi baris saat demonstrasi tugas kuliah.

## Fitur Utama

1. **Login Petugas Berbasis Peran**: Membedakan hak akses antara `Admin` dan `Kasir`.
2. **Transaksi Kasir & Potongan Anggota**: Pencarian barang instan, filter kategori, pembeda pembeli **Umum** dan **Anggota Kopdes** (potongan otomatis 5%), metode bayar **Tunai** (tombol nominal cepat + hitung kembalian langsung) dan **QRIS**, serta pratinjau/cetak struk termal.
3. **Kelola Data & Stok Barang (CRUD)**: Tambah, ubah, dan hapus data barang bagi `Admin` (dilengkapi proteksi relasi nota), serta tampilan baca-saja bagi `Kasir`.
4. **Riwayat Penjualan**: Filter transaksi hari ini atau seluruh periode, pencarian nomor nota, rekap jumlah nota & pemasukan Rupiah, tabel rincian item per nota, dan cetak ulang struk.

## Struktur Kode Sumber (5 Kelas Utama)

Seluruh kode sumber berada di paket `com.kopdes` pada folder `src/main/java/com/kopdes/`:

| Berkas Java | Pasangan NetBeans | Tanggung Jawab |
| :--- | :--- | :--- |
| `KoneksiDB.java` | *(Kelas Utilitas)* | Koneksi JDBC ke `kopdes.db`, pembuatan 4 tabel otomatis, pengisian 10 barang & 2 akun awal, format Rupiah `long`, dan pemuat logo opsional. |
| `LoginForm.java` | `LoginForm.form` | Jendela masuk aplikasi, inisialisasi tema FlatLaf, dan validasi akun ke tabel `pengguna`. |
| `KasirUtamaForm.java` | `KasirUtamaForm.form` | Jendela kerja utama 3-tab (*Transaksi Kasir*, *Data & Stok Barang*, *Riwayat Penjualan*) beserta jam digital dan pintasan `F2` / `F8`. |
| `BayarDialog.java` | `BayarDialog.form` | Dialog pembayaran Tunai/QRIS, kalkulator kembalian langsung, dan penyimpanan transaksi atomik (`commit`/`rollback`). |
| `StrukDialog.java` | `StrukDialog.form` | Dialog pratinjau struk belanja huruf *Monospaced* dan pencetakan ke printer/PDF (`areaStruk.print()`). |

## Akun Bawaan untuk Demonstrasi

Saat aplikasi pertama kali dijalankan, berkas database `kopdes.db` otomatis dibuat di folder proyek dengan dua akun siap pakai:

| Peran | Username | Password | Hak Akses |
| :--- | :--- | :--- | :--- |
| **Admin** | `admin` | `admin123` | Akses penuh: Transaksi Kasir, tambah/ubah/hapus Stok Barang, dan Riwayat Penjualan. |
| **Kasir** | `kasir` | `kasir123` | Transaksi Kasir dan Riwayat Penjualan; tab Stok Barang otomatis dalam mode baca-saja. |

## Cara Membuka dan Menjalankan

### 1. Melalui Apache NetBeans (Disarankan saat Demo Kelas)
1. Buka **Apache NetBeans** (versi 17 ke atas dengan JDK 17+).
2. Klik **File -> Open Project...**, lalu arahkan ke folder proyek ini (`coba baru`) dan klik **Open Project**.
3. Untuk melihat tampilan visual drag-and-drop: buka `Source Packages -> com.kopdes`, klik dua kali `LoginForm.java` atau `KasirUtamaForm.java`, lalu klik tab **Design** di bagian atas editor.
4. Tekan tombol **Run Project (`F6`)** untuk menjalankan aplikasi.

### 2. Melalui Terminal (Maven & JAR Mandiri)
```bash
# Menjalankan seluruh pengujian otomatis (JUnit 5)
mvn test

# Membungkus aplikasi beserta library SQLite & FlatLaf ke dalam 1 file JAR
mvn clean package

# Menjalankan file JAR hasil build
java -jar target/retail-kopdes.jar
```

## Cara Memasang Logo Buatan Sendiri (Opsional)

Tanpa file gambar apa pun, aplikasi tetap berjalan rapi menggunakan penanda teks `[RK]`. Jika ingin menggantinya dengan logo koperasi buatan sendiri, cukup simpan file PNG ke folder `src/main/resources/logo/`:

- `logo-ikon.png` (disarankan `64x64 px`): ikon jendela aplikasi dan pojok kiri header utama.
- `logo-login.png` (disarankan `120x120 px`): logo pada bagian atas halaman Login.
- `qris-kopdes.png` (disarankan `220x220 px`): gambar kode QR pada dialog pembayaran saat memilih metode `QRIS`.

Setelah menyalin gambar, jalankan ulang proyek (`Clean and Build` di NetBeans atau `mvn package`).

## Bahan Laporan Kuliah & Tanya-Jawab Dosen

Buka berkas **`PANDUAN_LAPORAN_DAN_BEDAH_KODE.md`** untuk mendapatkan:
- Tabel alur kerja & diagram alur (*flowchart*) keempat fitur utama untuk disalin ke dokumen laporan.
- Kamus data keempat tabel database beserta panduan membuat gambar **ER Diagram** otomatis di **DBeaver** dan mengedit isi tabel di **DB Browser for SQLite**.
- Penjelasan fungsi baris kode tiap kelas `.java` dengan bahasa sehari-hari serta contekan jawaban untuk pertanyaan dosen saat presentasi.
