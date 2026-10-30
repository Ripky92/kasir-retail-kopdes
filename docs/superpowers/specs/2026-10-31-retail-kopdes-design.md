# Dokumen Rancangan Aplikasi Kasir: Retail Kopdes

Tanggal: 2026-10-31  
Lokasi Target Proyek: `/home/ripky/Documents/Projek Java/coba baru`  
Platform & Alat: Java Swing (JDK 17+ / Maven) untuk Apache NetBeans GUI Builder + SQLite

---

## 1. Tujuan dan Latar Belakang (Intent & Why)

Proyek ini membangun ulang aplikasi kasir desktop untuk toko **Retail Kopdes** (Koperasi Desa) dari awal di dalam folder `coba baru`. Aplikasi ini dibuat untuk memenuhi tugas kuliah pemrograman berbasis Java Swing yang akan:
1. Didemonstrasikan secara langsung di hadapan dosen dan teman sekelas melalui **Apache NetBeans** (termasuk pengecekan tampilan visual pada tab **Design** / GUI Builder).
2. Dijadikan bahan penyusunan laporan kuliah yang menuntut kejelasan struktur tabel database, relasi antar tabel (ERD), serta diagram alur kerja (*flowchart*) aplikasi.

Versi sebelumnya pada folder `Aplikasi java part 2` dinilai belum memuaskan karena dua masalah utama:
- **Tampilan terlalu pucat dan datar:** hanya mengandalkan warna hitam-putih-abu bawaan tanpa aksen warna identitas toko.
- **Kode terlalu rumit untuk dijelaskan saat demo:** dipecah menjadi 17 file berlapis tanpa file `.form` NetBeans dan memakai teknik tingkat lanjut (seperti enkripsi BCrypt, migrasi skema otomatis `PRAGMA`, serta `AbstractAction`), sehingga menyulitkan mahasiswa saat harus menjawab pertanyaan dosen baris demi baris.

---

## 2. Sasaran, Batasan, dan Anti-Sasaran

### Sasaran Utama (Goals)
1. **Visual Berkarakter dan Akrab (Palet Opsi B):** Menghadirkan tampilan kasir bernuansa Hijau Daun Tua, Putih Bersih, Hijau Lembut, dan satu tombol aksi utama berwarna Oranye Bata (Terracotta) yang kontras di layar proyektor kelas.
2. **Kompatibel Penuh dengan Tab Design Apache NetBeans:** Setiap jendela dan dialog memiliki pasangan file `.java` dan `.form` yang valid sehingga dapat dibuka, dilihat, dan diklik melalui tab **Design** di Apache NetBeans.
3. **Kode Ringkas dan Mudah Dikuasai Mahasiswa:** Seluruh program dibatasi hanya **5 file `.java`** dengan penamaan variabel/metode berbahasa Indonesia yang lugas dan query SQL standar yang ditulis lurus di dekat aksi tombolnya.
4. **Database Relasional Siap Laporan:** Menggunakan **SQLite** (`kopdes.db`) dengan 4 tabel berelasi jelas (`pengguna`, `barang`, `transaksi`, `detail_transaksi`) yang mudah diedit lewat **DB Browser for SQLite** dan otomatis membentuk gambar **ER Diagram** saat dibuka di **DBeaver**.
5. **Empat Alur Fitur Fungsional Utuh:** Mencakup Login Petugas, Transaksi Kasir & Cetak Struk (dengan pembeda pembeli *Umum* vs *Anggota Kopdes*), Kelola Stok Barang (CRUD), serta Riwayat Transaksi.

### Di Luar Ruang Lingkup (Non-Goals)
- Manajemen akun pengguna (tambah/hapus kasir) dari dalam antarmuka aplikasi; pengelolaan akun cukup melalui data awal otomatis atau edit tabel di DB Browser for SQLite agar jumlah jendela tetap ramping.
- Sistem promo diskon terjadwal per tanggal atau simulasi mesin EDC kartu debit yang kompleks; pembayaran difokuskan pada **Tunai** (otomatis hitung kembalian) dan **QRIS**.
- Sinkronisasi jaringan multi-kasir atau server daring.

### Anti-Sasaran / Hal yang Wajib Dihindari (Anti-Goals)
- **Arsitektur berlapis yang berlebihan:** Memecah kode menjadi belasan kelas `Service`, `DAO`, `Model`, dan *listener lambda* berlapis yang membuat alur eksekusi sulit ditelusuri saat tanya-jawab dengan dosen.
- **Tampilan generik atau komponen mati:** Menampilkan tombol yang tidak melakukan apa-apa, menyebar emoji dekoratif pada judul/tombol, memakai angka statistik rekaan yang tidak dihitung dari database, atau kembali ke warna putih-abu polos tanpa identitas.

---

## 3. Batasan Teknis (Constraints)

- **Direktori Kerja Proyek:** Seluruh kode proyek Maven ditempatkan langsung di `/home/ripky/Documents/Projek Java/coba baru` (file `.superpowers/` dimasukkan ke `.gitignore` agar tidak mencemari proyek).
- **Target Kompilasi Java:** Mengatur `<maven.compiler.release>17</maven.compiler.release>` di `pom.xml` agar berjalan lancar di JDK 25 yang terpasang di laptop saat ini sekaligus tetap kompatibel bila dibuka di komputer kampus/teman yang memakai JDK 17 atau JDK 21.
- **Dependensi Minimal di `pom.xml`:**
  1. `com.formdev:flatlaf` (untuk merapikan sudut tombol, input, dan tabel Swing agar tidak kaku).
  2. `org.xerial:sqlite-jdbc` (driver koneksi SQLite satu file) beserta peredam pesan log standar `slf4j-nop` agar konsol NetBeans tetap bersih.
- **Aturan Sinkronisasi NetBeans `.form`:** Setiap file form wajib memuat penanda blok `// <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents` ... `// </editor-fold>//GEN-END:initComponents` serta blok deklarasi variabel `//GEN-BEGIN:variables` ... `//GEN-END:variables` yang cocok 1:1 dengan isi file XML `.form` pasangannya.
- **Kepatuhan Antislop & Aksesibilitas:** Kontras teks terhadap latar wajib memenuhi minimal WCAG AA (4.5:1 untuk teks biasa, 3:1 untuk teks besar), tanpa karakter *em dash* pada teks antarmuka maupun dokumentasi proyek, tanpa emoji dekoratif, dan setiap elemen interaktif wajib memiliki fungsi nyata serta dapat dioperasikan dengan keyboard.

---

## 4. Perbandingan Pendekatan Pembangunan (Approaches)

Tiga alternatif arsitektur telah dipertimbangkan bersama pemilik proyek:

1. **Pendekatan 1 (Dipilih): Struktur 5-File Terpadu dengan Tab (`JTabbedPane`) + FlatLaf Ringan + SQLite**
   - *Deskripsi:* Membatasi aplikasi menjadi 5 file `.java` (`KoneksiDB` + 4 form NetBeans: `LoginForm`, `KasirUtamaForm`, `BayarDialog`, `StrukDialog`). Fitur Kasir, Kelola Stok, dan Riwayat disatukan menggunakan 3 Tab di dalam `KasirUtamaForm`, sementara perintah SQL diletakkan langsung di metode event tombol terkait.
   - *Pertimbangan:* Paling mudah didemokan di NetBeans karena klik 2x pada tombol di tab *Design* langsung mengarah ke logika SQL-nya tanpa berpindah banyak file. Konsekuensinya, `KasirUtamaForm.java` berukuran lebih panjang dibanding file lainnya, namun terbagi rapi per fungsi.
2. **Pendekatan 2: Multi-Jendela Murni Bawaan JDK Tanpa FlatLaf (7 File)**
   - *Deskripsi:* Memecah setiap fitur ke jendela terpisah dan hanya mengandalkan tampilan dasar Java Swing tanpa FlatLaf.
   - *Pertimbangan:* Dependensi paling sedikit, tetapi tampilan tombol berwarna dan tabel Swing dasar di Linux terlihat kaku serta mengharuskan kasir sering membuka-tutup jendela terpisah.
3. **Pendekatan 3: Pemisahan Paket Tampilan dan Kelas Database / DAO (9 sampai 10 File) + FlatLaf**
   - *Deskripsi:* Memisahkan seluruh query SQL ke kelas `BarangDAO`, `TransaksiDAO`, `PenggunaDAO`, dan kelas model terpisah.
   - *Pertimbangan:* Rapi untuk proyek besar, tetapi memaksa mahasiswa menjelaskan lompatan data melewati tiga file berbeda untuk satu klik tombol saat diuji dosen.

**Keputusan:** Pemilik proyek memilih **Pendekatan 1** beserta database **SQLite** (didampingi *DB Browser for SQLite* untuk edit isi tabel dan *DBeaver* untuk cetak ER Diagram laporan).

---

## 5. Rincian Desain Sistem (The How)

### 5.1 Struktur Paket dan 5 File Kode Sumber

Paket tunggal: `com.kopdes` (di dalam `src/main/java/com/kopdes/`) agar pohon proyek di panel *Projects* NetBeans langsung terbuka tanpa folder bertingkat-tingkat:

1. **`KoneksiDB.java`** (Kelas utilitas & titik awal database):
   - Menyediakan metode statis `ambilKoneksi()` ke `jdbc:sqlite:kopdes.db` dengan pengaktifan `PRAGMA foreign_keys = ON;` supaya relasi antar tabel benar-benar aktif dan terbaca di DBeaver.
   - Menyediakan metode `siapkanDatabase()` yang membuat 4 tabel jika belum ada, lalu mengisi data contoh awal realistis khas toko koperasi desa bila tabel masih kosong.
   - Menyimpan konstanta warna palet Opsi B (`WARNA_HEADER`, `WARNA_LATAR`, `WARNA_KARTU`, `WARNA_RINGKASAN`, `WARNA_TEKS_UTAMA`, `WARNA_TOMBOL_BAYAR`, `WARNA_BAHAYA`), metode bantu `formatRupiah(long nominal)` (seluruh nominal Rupiah memakai bilangan bulat 64-bit `long` agar aman menampung rekap omset hingga triliunan Rupiah tanpa risiko *overflow* batas 2,14 miliar milik `int` serta bebas dari *bug* presisi koma `double`, dengan hasil persen potongan dibulatkan memakai `Math.round`), dan metode bantu `muatLogoOpsional(String namaFile, int lebar, int tinggi)` yang otomatis memakai gambar PNG buatan pengguna di `src/main/resources/logo/` bila tersedia atau kembali ke teks `[RK]` bila belum ada.

2. **`LoginForm.java` + `LoginForm.form`** (Titik masuk utama `main`):
   - Mengatur tema dasar `FlatLightLaf.setup()`, memanggil `KoneksiDB.siapkanDatabase()`, lalu menampilkan jendela Login di tengah layar.
   - Komponen di tab Design: Panel header hijau tua bertuliskan **RETAIL KOPDES** dan subjudul *Sistem Kasir Koperasi Desa*, input `txtUsername`, input `txtPassword`, label pesan status/validasi `lblPesan`, tombol **Masuk**, dan tombol **Keluar**.
   - Validasi jelas: jika kolom kosong atau akun tidak cocok di tabel `pengguna`, pesan kesalahan tampil jelas dengan warna kontras dan fokus kembali ke kolom terkait; tekan `Enter` pada kolom sandi langsung menjalankan aksi Masuk.

3. **`KasirUtamaForm.java` + `KasirUtamaForm.form`** (Jendela kerja utama):
   - **Bilah Header Atas (`#065F46`):** Menampilkan teks identitas `[RK] RETAIL KOPDES`, info petugas yang sedang bertugas (`Petugas: <nama_lengkap> (<peran>)`), jam & tanggal berjalan, serta tombol **Ganti Petugas (Logout)**.
   - **Area Utama (`JTabbedPane` 3 Tab):**
     - **Tab 1: Transaksi Kasir**
       - *Sisi Kiri (Katalog Barang):* Kolom cari kode/nama barang (`txtCariBarang`, fokus cepat dengan `F2`, ketik untuk saring langsung atau tekan `Enter` bila kode barang persis untuk langsung masuk keranjang), pilihan kategori (`cmbKategori`: Semua, Sembako, Minuman, Kebutuhan Rumah), tabel daftar barang (`tblKatalog`: Kode, Nama Barang, Kategori, Harga, Sisa Stok), input jumlah beli (`spnJumlahBeli`, bawaan 1), dan tombol hijau **+ Tambah ke Keranjang** (klik dua kali pada baris tabel katalog juga menambahkan 1 barang ke keranjang).
       - *Sisi Kanan (Keranjang & Ringkasan):* Pilihan jenis pembeli menggunakan `JRadioButton` (**Pembeli Umum** vs **Anggota Kopdes - Potongan 5%**), tabel keranjang (`tblKeranjang`: Kode, Nama Barang, Harga, Qty, Subtotal), deret tombol keranjang (**+ Qty**, **- Qty**, **Hapus Item**, **Kosongkan**), panel ringkasan hijau lembut (`#ECFDF5`) berisi label *Subtotal*, *Potongan Anggota (5%)*, dan angka besar **Total Bayar**, ditutup oleh satu tombol utama berwarna Oranye Bata (`#C2410C`) **PROSES BAYAR (F8)**.
     - **Tab 2: Kelola Data Barang (CRUD Stok)**
       - *Panel Form Input:* `txtKodeBarang`, `txtNamaBarang`, `cmbKategoriBarang`, `txtHargaBarang`, `txtStokBarang`, serta tombol **Simpan Baru**, **Ubah Data**, **Hapus Barang** (merah `#B91C1C`), dan **Bersihkan Form**.
       - *Panel Tabel Stok:* `tblStokBarang` yang menampilkan seluruh isi tabel `barang` beserta penanda status stok jika menipis (`<= 5`). Klik baris pada tabel otomatis mengisi kolom input di atasnya untuk memudahkan pengeditan. Perubahan stok langsung memperbarui katalog di Tab 1.
     - **Tab 3: Riwayat Penjualan**
       - *Bilah Filter & Ringkasan Nyata:* Pilihan tampilkan **Hari Ini** atau **Semua Transaksi**, kotak cari nomor nota, serta kartu ringkasan nyata yang dihitung langsung dari hasil query SQL (Jumlah Transaksi & Total Pemasukan Rupiah).
       - *Tabel Daftar Transaksi (`tblRiwayat`) & Rincian Item (`tblDetailRiwayat`):* Memilih satu baris nota pada `tblRiwayat` langsung memuat rincian barang yang dibeli ke `tblDetailRiwayat` di bawahnya, dilengkapi tombol **Buka Struk** untuk menampilkan ulang struk transaksi terpilih.

4. **`BayarDialog.java` + `BayarDialog.form`** (Dialog modal pembayaran):
   - Menampilkan rincian Total Tagihan (setelah potongan anggota jika Anggota Kopdes dipilih).
   - Pilihan metode pembayaran (`cmbMetodeBayar` atau tombol radio: **Tunai** dan **QRIS**):
     - Jika **Tunai**: mengaktifkan input `txtUangDiterima`, tombol pecahan uang cepat (**Uang Pas**, **Rp 20.000**, **Rp 50.000**, **Rp 100.000**), dan label **Kembalian** yang dihitung otomatis setiap angka diketik.
     - Jika **QRIS**: otomatis mengisi nominal uang diterima sama dengan Total Tagihan (kembalian Rp 0) dan menampilkan kotak kode referensi pembayaran QRIS toko Retail Kopdes (tanpa klaim koneksi bank palsu, jujur sebagai pencatatan pembayaran non-tunai di kasir).
   - Tombol **Selesaikan Transaksi** (`#C2410C`) dan **Batal (`Esc`)**. Saat diselesaikan: menjalankan `INSERT` ke `transaksi`, `INSERT` ke `detail_transaksi`, dan `UPDATE` pengurangan stok di `barang` dalam satu transaksi database (`conn.setAutoCommit(false)` lalu `commit()`), kemudian membuka `StrukDialog`.

5. **`StrukDialog.java` + `StrukDialog.form`** (Dialog modal pratinjau & cetak struk):
   - Menampilkan struk teks bergaya kertas termal menggunakan `JTextArea` tidak-bisa-diedit berhuruf `Monospaced` (memuat nama toko **RETAIL KOPDES**, alamat singkat koperasi desa, nomor nota, waktu, nama kasir, jenis pembeli, daftar item, subtotal, potongan anggota, total akhir, metode bayar, uang diterima, dan kembalian).
   - Dilengkapi tombol **Cetak Printer** (memanggil fitur cetak bawaan Java `areaStruk.print()`) dan tombol **Tutup (`Esc`)**.

---

### 5.2 Skema Database SQLite (`kopdes.db`)

Dirancang bersih menggunakan tipe data SQL standar agar mudah dipahami dan membentuk diagram relasi 4-tabel yang utuh di DBeaver:

1. **Tabel `pengguna`**
   - `id_pengguna` (`INTEGER PRIMARY KEY AUTOINCREMENT`)
   - `username` (`TEXT NOT NULL UNIQUE`)
   - `password` (`TEXT NOT NULL`) - disimpan teks biasa khusus kebutuhan edukasi/demo kuliah sesuai kesepakatan agar kueri `SELECT` mudah dibuktikan langsung di depan dosen
   - `nama_lengkap` (`TEXT NOT NULL`)
   - `peran` (`TEXT NOT NULL`) - berisi `'Admin'` atau `'Kasir'`

2. **Tabel `barang`**
   - `kode_barang` (`TEXT PRIMARY KEY`) - misal `'BRG001'` atau kode barcode angka
   - `nama_barang` (`TEXT NOT NULL`)
   - `kategori` (`TEXT NOT NULL`)
   - `harga` (`INTEGER NOT NULL CHECK(harga >= 0)`)
   - `stok` (`INTEGER NOT NULL CHECK(stok >= 0)`)

3. **Tabel `transaksi`**
   - `no_nota` (`TEXT PRIMARY KEY`) - format mudah dibaca, misal `'KPD-20261031-001'`
   - `tanggal_waktu` (`TEXT NOT NULL`) - format `'YYYY-MM-DD HH:mm:ss'`
   - `id_pengguna` (`INTEGER NOT NULL`, `FOREIGN KEY (id_pengguna) REFERENCES pengguna(id_pengguna)`)
   - `jenis_pembeli` (`TEXT NOT NULL`) - `'Umum'` atau `'Anggota Kopdes'`
   - `subtotal` (`INTEGER NOT NULL`)
   - `potongan_anggota` (`INTEGER NOT NULL`)
   - `total_bayar` (`INTEGER NOT NULL`)
   - `metode_bayar` (`TEXT NOT NULL`) - `'Tunai'` atau `'QRIS'`
   - `uang_bayar` (`INTEGER NOT NULL`)
   - `kembalian` (`INTEGER NOT NULL`)

4. **Tabel `detail_transaksi`**
   - `id_detail` (`INTEGER PRIMARY KEY AUTOINCREMENT`)
   - `no_nota` (`TEXT NOT NULL`, `FOREIGN KEY (no_nota) REFERENCES transaksi(no_nota) ON DELETE CASCADE`)
   - `kode_barang` (`TEXT NOT NULL`, `FOREIGN KEY (kode_barang) REFERENCES barang(kode_barang)`)
   - `nama_barang` (`TEXT NOT NULL`)
   - `harga_satuan` (`INTEGER NOT NULL`)
   - `jumlah` (`INTEGER NOT NULL`)
   - `subtotal_item` (`INTEGER NOT NULL`)

> **Catatan Relasi untuk Penghapusan Barang:** Jika suatu barang sudah pernah tercatat di `detail_transaksi`, upaya menghapus barang tersebut di Tab 2 akan dicegah secara sopan dengan pesan penjelas (karena terikat `FOREIGN KEY` riwayat nota) dan menyarankan pengubahan stok menjadi `0` agar histori laporan keuangan tidak rusak. Barang baru yang belum pernah terjual dapat dihapus penuh.

---

### 5.3 Identitas Visual, Liveliness Dials, dan Alasan Desain

- **Design Read:** Membaca rancangan ini sebagai aplikasi kasir desktop koperasi desa untuk operasional petugas kasir dan demonstrasi akademik di proyektor kelas, bergaya fungsional-hangat khas ritel sembako modern, dengan pengaturan dial **ENERGY 2 / RHYTHM 2 / MOTION 1**.
- **Pengaturan Tiga Dial:**
  - `ENERGY 2` (Seimbang): Header hijau tua yang tegas dan tombol bayar oranye bata memberi sambutan visual yang jelas tanpa dekorasi berlebihan.
  - `RHYTHM 2` (Konsisten dengan variasi fungsional): Tata letak antar tab konsisten, tetapi panel transaksi memadukan tabel katalog lebar di kiri dengan kartu ringkasan vertikal di kanan sesuai hirarki kerja kasir.
  - `MOTION 1` (Hanya perubahan status klik/hover): Sesuai karakter aplikasi kasir desktop yang mengutamakan kecepatan respons input tanpa animasi dekoratif yang mengganggu.
- **Alasan Satu Baris untuk Setiap Keputusan Visual Utama:**
  - *Mengapa Hijau Hutan Tua (`#065F46`) dengan teks putih (`#FFFFFF`, kontras 7.7:1):* Merepresentasikan identitas Koperasi Desa yang akrab dan menjaga judul aplikasi tetap terbaca tajam di layar proyektor kelas.
  - *Mengapa latar dasar Hijau Sangat Muda (`#F0FDF4`) dan bidang tabel Putih (`#FFFFFF`) dengan teks gelap (`#0F172A`, kontras >15:1):* Menghilangkan kesan pucat hitam-putih pada versi lama sambil menjaga daftar barang nyaman dipindai mata.
  - *Mengapa panel Total Bayar memakai Hijau Lembut (`#ECFDF5`) bertepi `#6EE7B7` dengan teks `#064E3B` (kontras 9.1:1):* Membedakan kotak nominal tagihan dari tabel biasa tanpa mencuri perhatian dari tombol bayar utama.
  - *Mengapa Oranye Bata (`#C2410C`) dengan teks putih (`#FFFFFF`, kontras 5.18:1) hanya dipakai pada satu tombol utama Bayar:* Menciptakan satu titik fokus tunggal di layar kasir agar aksi penyelesaian transaksi langsung terlihat dan lolos batas kontras WCAG AA.
  - *Mengapa memakai font Sans-Serif sistem bawaan dan Monospaced khusus kertas struk:* Menjamin tampilan huruf tajam dan konsisten di Linux maupun Windows tanpa ketergantungan font luar, sementara Monospaced menjaga perataan kolom angka pada struk belanja.
  - *Mengapa lencana teks `[RK] RETAIL KOPDES` digunakan saat ini dengan dukungan slot gambar opsional (`src/main/resources/logo/`):* Menghindari pembuatan aset logo asumsi tanpa instruksi khusus, sekaligus menyiapkan 3 slot otomatis (`logo-ikon.png` untuk header & ikon jendela aplikasi, `logo-login.png` untuk halaman masuk, dan `qris-kopdes.png` untuk dialog pembayaran QRIS) agar pemilik proyek bisa langsung menaruh logo buatannya sendiri kapan saja tanpa perlu merombak kode.

### 5.4 Penanganan Tiga Kondisi Antarmuka (Empty, Loading, Error) & Aksesibilitas
- **Kondisi Kosong (Empty State):**
  - Saat keranjang belanja kosong, tabel menampilkan keterangan status *"Keranjang masih kosong. Pilih atau cari barang di tabel sebelah kiri untuk memulai transaksi."* dan tombol **Proses Bayar** otomatis dinonaktifkan (`setEnabled(false)`).
  - Saat pencarian barang atau riwayat transaksi tidak menemukan hasil, label status memberitahukan penyebabnya secara spesifik beserta saran menghapus filter pencarian.
- **Kondisi Memuat / Memproses (Loading State):**
  - Saat menyimpan transaksi atau memuat ulang data tabel dari SQLite, kursor jendela berubah menjadi `Cursor.WAIT_CURSOR` dan tombol simpan dinonaktifkan sementara untuk mencegah klik ganda.
- **Kondisi Galat (Error State):**
  - Kegagalan input (misal stok tidak cukup, uang tunai kurang dari tagihan, harga bukan angka, kode barang kembar) maupun kendala SQLite ditampilkan lewat dialog peringatan berbahasa Indonesia yang menjelaskan masalahnya dan apa yang harus diperbaiki pengguna.
- **Navigasi Keyboard:**
  - Urutan pindah kolom dengan tombol `Tab` disusun berurutan dari atas ke bawah, indikator fokus komponen FlatLaf terlihat tegas, dialog tertutup dengan `Escape`, dan pintasan `F2` (fokus cari barang) serta `F8` (buka dialog bayar) disediakan untuk kelancaran demo.

---

## 6. Keputusan Kecil yang Ditetapkan Perancang & Bagian untuk Pembuat (Builder)

### Keputusan Teknis Ringan yang Ditetapkan di Rancangan Ini (Untuk Dicek Pemilik Proyek)
1. **Besaran potongan Anggota Kopdes ditetapkan 5%** dari subtotal belanja (disimpan dalam 1 konstanta `PERSEN_DISKON_ANGGOTA = 5` agar sangat mudah diubah jika diinginkan).
2. **Peran Admin vs Kasir dibuat sederhana:** Akun `Admin` dapat mengakses ketiga tab (Kasir, Kelola Barang, Riwayat), sedangkan akun `Kasir` difokuskan pada tab Kasir dan Riwayat (tab Kelola Barang dikunci atau diberi keterangan khusus Admin) supaya pembagian hak akses terlihat nyata saat didemokan ke dosen.
3. **Data awal otomatis (`kopdes.db`):** Diisi sekitar 10 barang kebutuhan pokok koperasi desa yang wajar (Beras, Minyak Goreng, Gula Pasir, Telur, Teh, Kopi, Sabun, Mie Instan, dll.) serta 2 akun contoh (`admin` / `admin123` dan `kasir` / `kasir123`) agar aplikasi siap langsung didemokan begitu pertama kali dijalankan.
4. **Berkas Pendamping Laporan Kuliah:** Menyertakan berkas `PANDUAN_LAPORAN_DAN_BEDAH_KODE.md` di dalam folder proyek yang memuat diagram alur kerja (*flowchart*), tabel kamus database, dan penjelasan fungsi tiap bagian kode dengan bahasa sederhana untuk persiapan tanya-jawab dengan dosen.

### Bagian yang Diserahkan kepada Pembuat Kode (Left to the Builder)
- Penataan ukuran piksel margin/padding antar komponen di dalam `GroupLayout` / `BorderLayout` pada file `.form` NetBeans selama tetap proporsional dan responsif saat jendela diperbesar.
- Format penomoran urut nota harian (misal `KPD-YYYYMMDD-XXX`) dan tata letak spasi karakter pada teks struk belanja.
- Konfigurasi `nbactions.xml` pada proyek Maven agar tombol **Run Project (F6)** di Apache NetBeans langsung mengeksekusi `com.kopdes.LoginForm` tanpa pengaturan manual tambahan.

