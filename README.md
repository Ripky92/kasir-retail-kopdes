# Retail Kopdes - Aplikasi Kasir Koperasi Desa

Aplikasi kasir desktop (Point of Sale) berbasis Java Swing, FlatLaf, dan SQLite yang dirancang untuk kebutuhan kasir retail Koperasi Desa serta demonstrasi akademik di Apache NetBeans dan Visual Studio Code.

## 1. Prasyarat Sistem

Sebelum menjalankan atau mengedit proyek ini, pastikan komputer Anda dan rekan Anda telah terpasang:
- Java Development Kit (JDK): Versi 17 atau yang lebih baru (JDK 17, 21, atau 25).
- Apache Maven: Versi 3.8 atau yang lebih baru (bawaan NetBeans / CLI).
- IDE (pilih salah satu):
  - Apache NetBeans IDE versi 17 ke atas (disarankan untuk mengedit antarmuka secara drag-and-drop lewat tab Design).
  - Visual Studio Code dengan ekstensi Extension Pack for Java (Microsoft).
- Git: Untuk sinkronisasi kode antar anggota tim.

## 2. Struktur Direktori

Kode sumber dirancang ringkas dalam paket tunggal `com.kopdes` agar alur data dari antarmuka ke database mudah dipahami:

```text
coba baru/
├── pom.xml                               # Konfigurasi dependensi Maven dan build
├── nbactions.xml                         # Konfigurasi aksi Run dan Debug Apache NetBeans
├── README.md                             # Panduan instalasi dan alur kerja proyek
├── PANDUAN_LAPORAN_DAN_BEDAH_KODE.md     # Bahan laporan kuliah, ERD, dan bedah kode dosen
├── src/
│   ├── main/
│   │   ├── java/com/kopdes/
│   │   │   ├── KoneksiDB.java            # Utilitas JDBC SQLite, format Rupiah, dan diskon
│   │   │   ├── LoginForm.java            # Logika dan jendela masuk petugas
│   │   │   ├── LoginForm.form            # Metadata visual NetBeans untuk form login
│   │   │   ├── KasirUtamaForm.java       # Jendela kasir 3 tab (Kasir, Stok, Riwayat)
│   │   │   ├── KasirUtamaForm.form       # Metadata visual NetBeans untuk jendela kasir
│   │   │   ├── BayarDialog.java          # Dialog pembayaran Tunai/QRIS dan transaksi atomik
│   │   │   ├── BayarDialog.form          # Metadata visual NetBeans untuk dialog pembayaran
│   │   │   ├── StrukDialog.java          # Dialog pratinjau dan cetak struk belanja
│   │   │   └── StrukDialog.form          # Metadata visual NetBeans untuk dialog struk
│   │   └── resources/
│   │       └── logo/
│   │           └── CARA_PASANG_LOGO.txt  # Petunjuk penempatan logo PNG buatan sendiri
│   └── test/
│       └── java/com/kopdes/
│           ├── KoneksiDBTest.java        # Pengujian koneksi, format Rupiah, dan penomoran nota
│           └── AlurKasirDatabaseTest.java # Pengujian integrasi alur transaksi, stok, dan EDT
```

## 3. Cara Membuka dan Menjalankan Proyek

### A. Melalui Apache NetBeans
1. Buka Apache NetBeans.
2. Klik menu File -> Open Project...
3. Arahkan ke folder proyek ini (`coba baru`), lalu klik Open Project.
4. Tekan tombol F6 atau klik tombol Run Project (segitiga hijau) di bilah atas.
5. Untuk melihat perancangan visual drag-and-drop: bentangkan `Source Packages -> com.kopdes`, klik dua kali salah satu berkas form (misalnya `KasirUtamaForm.java`), lalu klik tab Design di bagian atas editor.

### B. Melalui Visual Studio Code (VS Code)
1. Pasang ekstensi Extension Pack for Java dari Microsoft di VS Code.
2. Klik File -> Open Folder..., lalu pilih folder proyek ini.
3. Tunggu hingga proses pembacaan dependensi Maven selesai di bilah status bawah.
4. Buka berkas `src/main/java/com/kopdes/LoginForm.java`.
5. Klik tombol Run di pojok kanan atas editor atau klik tautan teks Run di atas baris `public static void main(String[] args)`.

### C. Melalui Terminal (Maven CLI)
```bash
# Menjalankan pengujian otomatis (14 pengujian JUnit 5)
mvn test

# Membangun file JAR mandiri (lengkap dengan driver SQLite dan FlatLaf)
mvn clean package

# Menjalankan file JAR hasil kompilasi
java -jar target/retail-kopdes.jar
```

## 4. Akun Bawaan untuk Pengujian

Database `kopdes.db` otomatis dibuat dan diisi data awal saat aplikasi pertama kali dijalankan, sehingga Anda tidak perlu mengimpor file SQL secara manual. Akun bawaan yang tersedia:

| Peran | Username | Password | Hak Akses |
| :--- | :--- | :--- | :--- |
| Admin | admin | admin123 | Akses penuh: Transaksi Kasir, Kelola Data & Stok Barang (CRUD), dan Riwayat Penjualan. |
| Kasir | kasir | kasir123 | Transaksi Kasir dan Riwayat Penjualan. Tab Kelola Data Barang otomatis dikunci dalam mode baca-saja. |

## 5. Alur Kerja Kolaborasi Menggunakan GitHub

Berikut panduan langkah demi langkah untuk mengunggah proyek ini ke GitHub dan bekerja bersama rekan tim.

### Langkah untuk Pemilik Proyek (Anda):
1. Buka akun GitHub Anda di browser, lalu buat repositori baru (misalnya dengan nama `retail-kopdes`).
   - Jangan centang opsi "Add a README file" atau "Add .gitignore" karena proyek lokal sudah memilikinya.
2. Buka terminal di folder proyek Anda:
   ```bash
   cd "/home/ripky/Documents/Projek Java/coba baru"
   ```
3. Hubungkan repositori lokal Anda ke GitHub dan unggah seluruh berkas:
   ```bash
   # Ganti USERNAME dan retail-kopdes dengan URL repositori GitHub Anda
   git remote add origin https://github.com/USERNAME/retail-kopdes.git
   git branch -M main
   git push -u origin main
   ```
4. Tambahkan rekan Anda sebagai kolaborator:
   - Di halaman repositori GitHub Anda, klik menu Settings -> Collaborators.
   - Klik Add people, masukkan username atau email GitHub rekan Anda, lalu kirim undangan kolaborasi.

### Langkah untuk Rekan Anda:
1. Rekan Anda menerima undangan kolaborasi di email atau notifikasi GitHub.
2. Rekan Anda mengunduh (clone) repositori ke laptopnya melalui terminal:
   ```bash
   git clone https://github.com/USERNAME/retail-kopdes.git
   cd retail-kopdes
   ```
3. Rekan Anda dapat langsung membuka folder tersebut di Apache NetBeans (`File -> Open Project`) atau di VS Code (`File -> Open Folder`).
4. Rekan Anda dapat langsung menekan tombol Run (`F6` di NetBeans atau tombol Run di VS Code). Berkas database `kopdes.db` akan langsung dibuat otomatis saat pertama kali dibuka.

### Alur Kerja Saat Rekan Selesai Mengedit Kode:
Agar perubahan kode tidak saling menimpa atau konflik, gunakan alur standar Git berikut:

1. Sebelum mulai mengedit, tarik selalu pembaruan terbaru:
   ```bash
   git pull origin main
   ```
2. Setelah selesai mengedit berkas kode:
   ```bash
   git add .
   git commit -m "feat: deskripsi perubahan yang dibuat"
   git push origin main
   ```
3. Di sisi Anda (atau rekan yang lain), cukup jalankan perintah berikut untuk mengambil hasil editan rekan:
   ```bash
   git pull origin main
   ```

## 6. Arsitektur Teknis dan Aturan Perubahan Kode

Saat mengedit kode bersama rekan, perhatikan aturan teknis berikut agar proyek tetap berjalan normal:
1. Kompatibilitas NetBeans Form: Setiap form antarmuka memiliki dua berkas yang berpasangan (`.java` dan `.form`). Jangan menghapus atau mengubah baris penanda `//GEN-BEGIN:initComponents` dan `//GEN-END:variables` secara manual lewat teks editor biasa; lakukan penyesuaian visual lewat tab Design di Apache NetBeans agar kedua berkas tetap sinkron.
2. Tipe Data Uang Rupiah: Seluruh nominal harga, subtotal, potongan, tagihan, uang bayar, dan omset menggunakan tipe bilangan bulat 64-bit `long` untuk mencegah risiko *overflow* nominal besar serta menghindari pembulatan biner pecahan `double`. Gunakan metode `KoneksiDB.formatRupiah(long)` untuk menampilkan teks dan `KoneksiDB.parseNominalRupiah(String)` untuk membaca masukan ketikan pengguna.
3. Transaksi Atomik: Proses penyelesaian transaksi kasir dibungkus dalam blok transaksi `conn.setAutoCommit(false)`, `conn.commit()`, dan `conn.rollback()` di `BayarDialog.java` untuk menjamin pemotongan stok barang dan pencatatan nota tersimpan secara utuh.
4. Logo Kustom (Opsional): Aplikasi secara bawaan menampilkan teks `[RK]`. Jika ingin memasang logo buatan sendiri, simpan berkas gambar PNG ke dalam folder `src/main/resources/logo/`:
   - `logo-ikon.png` (atau `1.png`): Ikon jendela dan header utama toko Retail Kopdes.
   - `logo-login.png` (atau `2.png`): Ikon kartu formulir login petugas.
   - `qris-kopdes.png` (atau `3.png`): Gambar kode QR saat memilih metode pembayaran QRIS.

## 7. Dokumen Terkait

Dokumentasi akademik dan teknis mendalam tersedia di berkas `PANDUAN_LAPORAN_DAN_BEDAH_KODE.md`, yang mencakup:
- Diagram alur kerja (flowchart) dan tabel langkah Input-Proses-Output untuk disalin ke laporan tugas kuliah.
- Kamus data lengkap keempat tabel database (`pengguna`, `barang`, `transaksi`, `detail_transaksi`).
- Panduan ekspor gambar ER Diagram otomatis di DBeaver dan panduan edit data di DB Browser for SQLite.
- Bedah fungsi setiap baris kode kelima berkas Java beserta rangkuman jawaban siap pakai untuk demonstrasi di depan dosen.
