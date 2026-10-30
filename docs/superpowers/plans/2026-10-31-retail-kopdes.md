# Retail Kopdes Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Membangun aplikasi kasir desktop **Retail Kopdes** berbasis Java Swing 5-file yang kompatibel penuh dengan tab *Design* Apache NetBeans (`.form`), menggunakan database SQLite 4-tabel (`kopdes.db`), palet warna Opsi B (Hijau Daun, Putih, Aksen Oranye Bata), slot logo kustom opsional, serta panduan laporan dan bedah kode mahasiswa.

**Architecture:** Aplikasi dikemas dalam satu paket tunggal `com.kopdes` yang hanya berisi 5 file `.java` (`KoneksiDB`, `LoginForm`, `KasirUtamaForm`, `BayarDialog`, `StrukDialog`) ditambah 4 pasangan file XML `.form` standar NetBeans Matisse GUI Builder. Seluruh query SQL ditulis lurus dan transparan menggunakan `PreparedStatement` pada metode aksi komponen terkait, nominal uang disimpan dan dihitung sebagai bilangan bulat 64-bit `long` Rupiah, serta transaksi penjualan disimpan secara atomik ke SQLite.

**Tech Stack:** Java 17+ (`maven.compiler.release` 17, berjalan di JDK 17 s.d. JDK 25), Apache Maven 3.9+, Java Swing + NetBeans `.form` (Form v1.3), FlatLaf 3.4.1, SQLite JDBC 3.45.2.0, SLF4J NOP 2.0.12, JUnit Jupiter 5.10.2 (khusus pengujian).

**Spec:** `/home/ripky/Documents/Projek Java/coba baru/docs/superpowers/specs/2026-10-31-retail-kopdes-design.md` (rencana ini diturunkan langsung dari spesifikasi tersebut; pelaksana wajib membaca keduanya).

## Global Constraints

- Lokasi proyek Maven berada tepat di `/home/ripky/Documents/Projek Java/coba baru` dan mengabaikan `.superpowers/` serta `target/` melalui `.gitignore`.
- Target kompilasi Maven wajib `<maven.compiler.release>17</maven.compiler.release>` dengan encoding `UTF-8`.
- Jumlah file `.java` utama di `src/main/java/com/kopdes/` dibatasi tepat 5 file: `KoneksiDB.java`, `LoginForm.java`, `KasirUtamaForm.java`, `BayarDialog.java`, dan `StrukDialog.java`.
- Setiap kelas antarmuka (`LoginForm`, `KasirUtamaForm`, `BayarDialog`, `StrukDialog`) wajib memiliki file `.form` pasangan di `src/main/java/com/kopdes/` dengan blok `//GEN-BEGIN:initComponents` s.d. `//GEN-END:initComponents` dan `//GEN-BEGIN:variables` s.d. `//GEN-END:variables` yang sinkron 1:1.
- Seluruh nominal uang Rupiah wajib memakai tipe `long` (bukan `int` atau `double`), dan pemotongan persen diskon anggota (`PERSEN_DISKON_ANGGOTA = 5`) dihitung memakai `Math.round(subtotal * PERSEN_DISKON_ANGGOTA / 100.0)`.
- Palet warna Opsi B wajib terpusat di `KoneksiDB`: Header `#065F46` (teks `#FFFFFF`), Latar `#F0FDF4`, Kartu/Tabel `#FFFFFF` (teks `#0F172A`), Kotak Ringkasan `#ECFDF5` bertepi `#6EE7B7` (teks `#064E3B`), Tombol Aksi Utama Bayar `#C2410C` (teks `#FFFFFF`), dan Tombol Bahaya/Hapus `#B91C1C` (teks `#FFFFFF`).
- Dilarang keras memakai karakter *em dash* pada teks antarmuka, komentar kode, maupun dokumentasi; dilarang memakai emoji dekoratif pada label/tombol; dilarang menyisakan tombol mati tanpa fungsi nyata; semua teks wajib memenuhi kontras minimum WCAG AA (4.5:1).
- Tiga slot gambar opsional di `src/main/resources/logo/` (`logo-ikon.png`, `logo-login.png`, `qris-kopdes.png`) dimuat otomatis bila ada, dan jatuh kembali secara rapi ke teks `[RK]` / info teks QRIS bila file belum ditambahkan pengguna.

## Review Focus

1. **Input harga atau uang tunai mengandung titik pemisah ribuan atau awalan `Rp` (misal `"50.000"` atau `"Rp 16.500"`):** pengguna kasir di Indonesia lazim mengetik titik ribuan; parser angka harus membersihkan titik, spasi, dan awalan `Rp` menjadi `long` murni, serta mengembalikan `-1L` bila mengandung huruf atau bernilai negatif.
2. **Penambahan kuantitas barang yang sama ke keranjang melebihi sisa stok di database:** baik lewat klik katalog berulang maupun tombol `+ Qty`, kuantitas di keranjang tidak boleh melampaui kolom `stok` barang tersebut.
3. **Akumulasi omset nominal besar di atas batas `int` (> Rp 2.147.483.647) dan pembulatan diskon 5% pada subtotal ganjil:** penjumlahan `long` tidak boleh mengalami *overflow* negatif, dan potongan 5% dari `Rp 16.550` harus bulat tepat menjadi `Rp 828`.
4. **Penghapusan barang di Tab 2 yang sudah tercatat di `detail_transaksi`:** eksekusi `DELETE` wajib tertahan oleh `FOREIGN KEY` (`PRAGMA foreign_keys = ON`), mengembalikan status tertolak yang bersih tanpa merusak tabel `barang` maupun `detail_transaksi`.
5. **Ketidaksinkronan komponen antara file XML `.form` dan blok variabel `.java`:** setiap atribut `name` komponen pada ke-4 file `.form` wajib cocok 1:1 dengan deklarasi variabel di blok `//GEN-BEGIN:variables` pada file `.java` pasangannya agar tab *Design* Apache NetBeans terbuka tanpa galat.

---

### Task 1: Fondasi Proyek Maven NetBeans, `KoneksiDB.java`, dan Slot Logo Opsional

**Files:**
- Create: `/home/ripky/Documents/Projek Java/coba baru/.gitignore`
- Create: `/home/ripky/Documents/Projek Java/coba baru/pom.xml`
- Create: `/home/ripky/Documents/Projek Java/coba baru/nbactions.xml`
- Create: `/home/ripky/Documents/Projek Java/coba baru/src/main/resources/logo/CARA_PASANG_LOGO.txt`
- Create: `/home/ripky/Documents/Projek Java/coba baru/src/main/java/com/kopdes/KoneksiDB.java`
- Test: `/home/ripky/Documents/Projek Java/coba baru/src/test/java/com/kopdes/KoneksiDBTest.java`

**Interfaces:**
- Consumes: none (tugas dasar pertama)
- Produces (`com.kopdes.KoneksiDB`):
  - Konstanta warna `public static final Color`: `WARNA_HEADER` (`#065F46`), `WARNA_LATAR` (`#F0FDF4`), `WARNA_KARTU` (`#FFFFFF`), `WARNA_RINGKASAN` (`#ECFDF5`), `WARNA_TEPI_RINGKASAN` (`#6EE7B7`), `WARNA_TEKS_UTAMA` (`#0F172A`), `WARNA_TEKS_HIJAU` (`#064E3B`), `WARNA_TOMBOL_BAYAR` (`#C2410C`), `WARNA_BAHAYA` (`#B91C1C`), `WARNA_TEKS_PUTIH` (`#FFFFFF`)
  - Konstanta bisnis: `public static final int PERSEN_DISKON_ANGGOTA = 5;`
  - `public static void aturLokasiDatabase(String pathFileDb)` (bawaan `"kopdes.db"`)
  - `public static Connection ambilKoneksi() throws SQLException` (mengaktifkan `PRAGMA foreign_keys = ON;`)
  - `public static void siapkanDatabase()` (membuat tabel `pengguna`, `barang`, `transaksi`, `detail_transaksi` dan mengisi 2 akun awal `admin`/`admin123` peran `Admin`, `kasir`/`kasir123` peran `Kasir`, serta 10 barang sembako awal jika tabel kosong)
  - `public static String formatRupiah(long nominal)` (menghasilkan `"Rp 101.000"`, mendukung angka triliunan dan tanda minus)
  - `public static long parseNominalRupiah(String teksInput)` (menerima `"50000"`, `"50.000"`, `"Rp 50.000"` -> `50000L`; mengembalikan `-1L` bila kosong, negatif, atau mengandung huruf)
  - `public static long hitungPotonganAnggota(long subtotal, boolean adalahAnggota)`
  - `public static String buatNomorNotaBaru(Connection conn) throws SQLException` (format `"KPD-YYYYMMDD-XXX"`)
  - `public static ImageIcon muatLogoOpsional(String namaFile, int lebar, int tinggi)` (membaca `/logo/<namaFile>` dari classpath atau `src/main/resources/logo/<namaFile>`, mereskalakan halus, atau mengembalikan `null` jika file belum ada)

- [ ] **Step 1: Inisialisasi repositori Git lokal, `.gitignore`, `pom.xml`, `nbactions.xml`, dan petunjuk `CARA_PASANG_LOGO.txt`**

Buat struktur Maven di `/home/ripky/Documents/Projek Java/coba baru` dengan `artifactId` `retail-kopdes`, `groupId` `com.kopdes`, `mainClass` `com.kopdes.LoginForm`, dependensi `flatlaf` `3.4.1`, `sqlite-jdbc` `3.45.2.0`, `slf4j-api` & `slf4j-nop` `2.0.12`, `junit-jupiter` `5.10.2` (`test`), serta konfigurasi `nbactions.xml` untuk aksi `run` dan `debug` di Apache NetBeans. Jalankan `git init` di `/home/ripky/Documents/Projek Java/coba baru`.

- [ ] **Step 2: Tulis pengujian gagal di `KoneksiDBTest.java`**

```java
package com.kopdes;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import static org.junit.jupiter.api.Assertions.*;

class KoneksiDBTest {
    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        KoneksiDB.aturLokasiDatabase(tempDir.resolve("uji_kopdes.db").toString());
        KoneksiDB.siapkanDatabase();
    }

    @Test
    void databaseMembuatEmpatTabelDanDataAwal() throws Exception {
        try (Connection c = KoneksiDB.ambilKoneksi(); Statement s = c.createStatement()) {
            try (ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM pengguna")) {
                assertTrue(rs.next());
                assertEquals(2, rs.getInt(1));
            }
            try (ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM barang")) {
                assertTrue(rs.next());
                assertTrue(rs.getInt(1) >= 10);
            }
        }
    }

    @Test
    void formatDanParseRupiahAmanUntukNominalTriliunanDanTitikRibuan() {
        assertEquals("Rp 5.000.000.000.000", KoneksiDB.formatRupiah(5_000_000_000_000L));
        assertEquals(50_000L, KoneksiDB.parseNominalRupiah("50.000"));
        assertEquals(16_500L, KoneksiDB.parseNominalRupiah("Rp 16.500"));
        assertEquals(-1L, KoneksiDB.parseNominalRupiah("-500"));
        assertEquals(-1L, KoneksiDB.parseNominalRupiah("12abc"));
    }

    @Test
    void hitungPotonganAnggotaMembulatkanNominalGanjilDanNolUntukUmum() {
        assertEquals(828L, KoneksiDB.hitungPotonganAnggota(16_550L, true));
        assertEquals(0L, KoneksiDB.hitungPotonganAnggota(16_550L, false));
    }

    @Test
    void muatLogoOpsionalMengembalikanNullJikaBelumAdaFile() {
        assertNull(KoneksiDB.muatLogoOpsional("tidak-ada.png", 64, 64));
    }
}
```

- [ ] **Step 3: Jalankan pengujian untuk memastikan gagal karena `KoneksiDB` belum dibuat**

Run: `mvn -f "/home/ripky/Documents/Projek Java/coba baru/pom.xml" test`
Expected: FAIL compilation (`cannot find symbol: class KoneksiDB`).

- [ ] **Step 4: Implementasikan `KoneksiDB.java` sesuai tanda tangan pada blok Interfaces**

Gunakan `DecimalFormatSymbols` Indonesia (titik sebagai pemisah ribuan, tanpa desimal) pada `formatRupiah(long)`, aktifkan `PRAGMA foreign_keys = ON` setiap koneksi dibuka, dan isi 10 barang sembako koperasi desa realistis (seperti `BRG001` Beras Desa Premium 5kg `68000`, `BRG002` Minyak Goreng Sawit 1L `16500`, dst.).

- [ ] **Step 5: Jalankan pengujian untuk memastikan seluruh tes `KoneksiDBTest` lulus**

Run: `mvn -f "/home/ripky/Documents/Projek Java/coba baru/pom.xml" test`
Expected: PASS (`Tests run: 4, Failures: 0`).

- [ ] **Step 6: Commit Task 1**

```bash
git -C "/home/ripky/Documents/Projek Java/coba baru" add .gitignore pom.xml nbactions.xml src/ docs/
git -C "/home/ripky/Documents/Projek Java/coba baru" commit -m "feat: inisialisasi proyek maven netbeans dan KoneksiDB sqlite"
```

---

### Task 2: Form Login Petugas (`LoginForm`) dan Dialog Struk (`StrukDialog`) beserta `.form` NetBeans

**Files:**
- Create: `/home/ripky/Documents/Projek Java/coba baru/src/main/java/com/kopdes/LoginForm.java`
- Create: `/home/ripky/Documents/Projek Java/coba baru/src/main/java/com/kopdes/LoginForm.form`
- Create: `/home/ripky/Documents/Projek Java/coba baru/src/main/java/com/kopdes/StrukDialog.java`
- Create: `/home/ripky/Documents/Projek Java/coba baru/src/main/java/com/kopdes/StrukDialog.form`
- Test: `/home/ripky/Documents/Projek Java/coba baru/src/test/java/com/kopdes/AlurKasirDatabaseTest.java`

**Interfaces:**
- Consumes: `KoneksiDB` (Task 1)
- Produces:
  - `com.kopdes.LoginForm`:
    - `public static String[] cekLoginDatabase(String username, String password)` -> mengembalikan `new String[]{idPengguna, namaLengkap, peran}` jika cocok, atau `null` jika gagal/kosong.
    - `public static void main(String[] args)` -> mengatur `FlatLightLaf.setup()`, memanggil `KoneksiDB.siapkanDatabase()`, dan membuka `LoginForm`.
  - `com.kopdes.StrukDialog`:
    - `public StrukDialog(java.awt.Frame parent, boolean modal, String teksStruk)`
    - `public static String susunTeksStruk(String noNota, String tanggalWaktu, String namaPetugas, String jenisPembeli, java.util.List<String[]> daftarItem, long subtotal, long potonganAnggota, long totalBayar, String metodeBayar, long uangBayar, long kembalian)` (tiap elemen `daftarItem` berisi `{kode, nama, hargaSatuanStr, jumlahStr, subtotalItemStr}`).

- [ ] **Step 1: Tulis pengujian gagal di `AlurKasirDatabaseTest.java` untuk login dan penyusunan teks struk**

```java
package com.kopdes;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class AlurKasirDatabaseTest {
    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        KoneksiDB.aturLokasiDatabase(tempDir.resolve("uji_alur.db").toString());
        KoneksiDB.siapkanDatabase();
    }

    @Test
    void cekLoginDatabaseMenerimaAkunValidDanMenolakSandiSalah() {
        String[] admin = LoginForm.cekLoginDatabase("admin", "admin123");
        assertNotNull(admin);
        assertEquals("Admin", admin[2]);
        assertNull(LoginForm.cekLoginDatabase("admin", "salah"));
        assertNull(LoginForm.cekLoginDatabase("", ""));
    }

    @Test
    void susunTeksStrukMemuatIdentitasKopdesPotonganDanKembalian() {
        List<String[]> items = List.of(
            new String[]{"BRG001", "Beras Desa 5kg", "68000", "1", "68000"}
        );
        String struk = StrukDialog.susunTeksStruk(
            "KPD-20261031-001", "2026-10-31 09:30:00", "Siti Rahma",
            "Anggota Kopdes", items, 68_000L, 3_400L, 64_600L, "Tunai", 70_000L, 5_400L
        );
        assertTrue(struk.contains("RETAIL KOPDES"));
        assertTrue(struk.contains("KPD-20261031-001"));
        assertTrue(struk.contains("Anggota Kopdes"));
        assertTrue(struk.contains("Rp 3.400"));
        assertTrue(struk.contains("Rp 5.400"));
    }
}
```

- [ ] **Step 2: Jalankan pengujian untuk memastikan gagal**

Run: `mvn -f "/home/ripky/Documents/Projek Java/coba baru/pom.xml" test`
Expected: FAIL compilation (`cannot find symbol: variable LoginForm / StrukDialog`).

- [ ] **Step 3: Implementasikan `StrukDialog.java` + `StrukDialog.form` dan `LoginForm.java` + `LoginForm.form`**

Buat kedua kelas menggunakan struktur `org.netbeans.modules.form.forminfo.JDialogFormInfo` dan `JFrameFormInfo` yang valid beserta blok `//GEN-BEGIN:initComponents` dan `//GEN-BEGIN:variables`. Di `LoginForm`, muat `logo-login.png` dan `logo-ikon.png` secara opsional lewat `KoneksiDB.muatLogoOpsional`; saat login berhasil, panggil metode pembuka jendela kasir (sementara jika `KasirUtamaForm` belum dibuat di Task 2, buat kerangka kelas minimal `KasirUtamaForm(int idPengguna, String namaLengkap, String peran)` yang akan dilengkapi penuh pada Task 4 agar proyek selalu dapat dikompilasi utuh).

- [ ] **Step 4: Jalankan pengujian untuk memastikan lulus**

Run: `mvn -f "/home/ripky/Documents/Projek Java/coba baru/pom.xml" test`
Expected: PASS (`Tests run: 6, Failures: 0`).

- [ ] **Step 5: Commit Task 2**

```bash
git -C "/home/ripky/Documents/Projek Java/coba baru" add src/
git -C "/home/ripky/Documents/Projek Java/coba baru" commit -m "feat: tambahkan LoginForm dan StrukDialog beserta file form netbeans"
```

---

### Task 3: Dialog Pembayaran Transaksional (`BayarDialog.java` + `BayarDialog.form`)

**Files:**
- Create: `/home/ripky/Documents/Projek Java/coba baru/src/main/java/com/kopdes/BayarDialog.java`
- Create: `/home/ripky/Documents/Projek Java/coba baru/src/main/java/com/kopdes/BayarDialog.form`
- Modify: `/home/ripky/Documents/Projek Java/coba baru/src/test/java/com/kopdes/AlurKasirDatabaseTest.java`

**Interfaces:**
- Consumes: `KoneksiDB` (Task 1), `StrukDialog` (Task 2)
- Produces (`com.kopdes.BayarDialog`):
  - `public BayarDialog(java.awt.Frame parent, boolean modal, int idPengguna, String namaPetugas, String jenisPembeli, java.util.List<String[]> itemKeranjang, long subtotal, long potonganAnggota, long totalBayar)`
  - `public boolean apakahTransaksiBerhasil()`
  - `public static String simpanTransaksiKeDatabase(int idPengguna, String jenisPembeli, java.util.List<String[]> itemKeranjang, long subtotal, long potonganAnggota, long totalBayar, String metodeBayar, long uangBayar) throws Exception` -> menjalankan transaksi atomik (`INSERT INTO transaksi`, `INSERT INTO detail_transaksi`, `UPDATE barang SET stok = stok - ? WHERE kode_barang = ? AND stok >= ?`), melempar `IllegalArgumentException` berbahasa Indonesia jika `uangBayar < totalBayar` atau stok barang tidak mencukupi (disertai `rollback()`), dan mengembalikan `noNota` baru jika sukses.

- [ ] **Step 1: Tambahkan pengujian gagal ke `AlurKasirDatabaseTest.java` untuk transaksi sukses, uang kurang, dan rollback stok kurang**

```java
    @Test
    void simpanTransaksiMemotongStokSecaraAtomikDanRollbackJikaStokKurang() throws Exception {
        List<String[]> belanjaValid = List.of(
            new String[]{"BRG001", "Beras Desa Premium 5kg", "68000", "2", "136000"}
        );
        String nota = BayarDialog.simpanTransaksiKeDatabase(
            1, "Umum", belanjaValid, 136_000L, 0L, 136_000L, "Tunai", 150_000L
        );
        assertTrue(nota.startsWith("KPD-"));

        assertThrows(IllegalArgumentException.class, () ->
            BayarDialog.simpanTransaksiKeDatabase(
                1, "Umum", belanjaValid, 136_000L, 0L, 136_000L, "Tunai", 100_000L
            )
        );

        List<String[]> belanjaMelebihiStok = List.of(
            new String[]{"BRG001", "Beras Desa Premium 5kg", "68000", "99999", "6799932000"}
        );
        assertThrows(IllegalArgumentException.class, () ->
            BayarDialog.simpanTransaksiKeDatabase(
                1, "Umum", belanjaMelebihiStok, 6_799_932_000L, 0L, 6_799_932_000L, "QRIS", 6_799_932_000L
            )
        );
    }
```

- [ ] **Step 2: Jalankan pengujian untuk memastikan gagal**

Run: `mvn -f "/home/ripky/Documents/Projek Java/coba baru/pom.xml" test`
Expected: FAIL compilation (`cannot find symbol: variable BayarDialog`).

- [ ] **Step 3: Implementasikan `BayarDialog.java` dan `BayarDialog.form`**

Sediakan pilihan metode **Tunai** dan **QRIS**, tombol uang cepat (**Uang Pas**, **Rp 20.000**, **Rp 50.000**, **Rp 100.000**), perhitungan kembalian real-time saat mengetik `txtUangDiterima` (memakai `KoneksiDB.parseNominalRupiah`), pemuatan opsional `qris-kopdes.png` saat metode QRIS dipilih, penutupan via `Escape`, tombol **Selesaikan Transaksi** berwarna `WARNA_TOMBOL_BAYAR` (`#C2410C`), dan pembukaan `StrukDialog` otomatis setelah transaksi tersimpan.

- [ ] **Step 4: Jalankan pengujian untuk memastikan lulus**

Run: `mvn -f "/home/ripky/Documents/Projek Java/coba baru/pom.xml" test`
Expected: PASS (`Tests run: 7, Failures: 0`).

- [ ] **Step 5: Commit Task 3**

```bash
git -C "/home/ripky/Documents/Projek Java/coba baru" add src/
git -C "/home/ripky/Documents/Projek Java/coba baru" commit -m "feat: tambahkan BayarDialog transaksional tunai dan QRIS"
```

---

### Task 4: Jendela Utama 3-Tab (`KasirUtamaForm.java` + `KasirUtamaForm.form`) dan Pengujian Sinkronisasi `.form`

**Files:**
- Modify: `/home/ripky/Documents/Projek Java/coba baru/src/main/java/com/kopdes/KasirUtamaForm.java`
- Create: `/home/ripky/Documents/Projek Java/coba baru/src/main/java/com/kopdes/KasirUtamaForm.form`
- Modify: `/home/ripky/Documents/Projek Java/coba baru/src/test/java/com/kopdes/AlurKasirDatabaseTest.java`

**Interfaces:**
- Consumes: `KoneksiDB` (Task 1), `LoginForm` & `StrukDialog` (Task 2), `BayarDialog` (Task 3)
- Produces (`com.kopdes.KasirUtamaForm`):
  - `public KasirUtamaForm(int idPengguna, String namaLengkap, String peran)`
  - `public static int hitungJumlahBaruKeranjang(int jumlahSaatIniDiKeranjang, int tambahanDiminta, int stokTersedia)` -> melempar `IllegalArgumentException` jika melebihi `stokTersedia`, mengembalikan total jumlah baru jika valid.
  - `public static void simpanBarangBaru(String kode, String nama, String kategori, long harga, int stok) throws Exception`
  - `public static void ubahDataBarang(String kode, String nama, String kategori, long harga, int stok) throws Exception`
  - `public static void hapusDataBarang(String kode) throws Exception` -> melempar `IllegalStateException` berbahasa Indonesia yang sopan jika `kode` terikat `FOREIGN KEY` di `detail_transaksi`.

- [ ] **Step 1: Tambahkan pengujian gagal ke `AlurKasirDatabaseTest.java` untuk batas stok keranjang, proteksi FK hapus barang, dan kesesuaian 1:1 file `.form` NetBeans**

```java
    @Test
    void batasStokKeranjangDanProteksiHapusBarangTerjualBerjalanBenar() throws Exception {
        assertEquals(3, KasirUtamaForm.hitungJumlahBaruKeranjang(2, 1, 3));
        assertThrows(IllegalArgumentException.class, () ->
            KasirUtamaForm.hitungJumlahBaruKeranjang(3, 1, 3)
        );

        KasirUtamaForm.simpanBarangBaru("BRG999", "Teh Desa", "Minuman", 5_000L, 10);
        KasirUtamaForm.hapusDataBarang("BRG999");

        List<String[]> jual = List.of(new String[]{"BRG001", "Beras Desa Premium 5kg", "68000", "1", "68000"});
        BayarDialog.simpanTransaksiKeDatabase(1, "Umum", jual, 68_000L, 0L, 68_000L, "Tunai", 70_000L);
        assertThrows(IllegalStateException.class, () -> KasirUtamaForm.hapusDataBarang("BRG001"));
    }

    @Test
    void seluruhPasanganFormNetBeansDanFileJavaSinkronSatuLawanSatu() throws Exception {
        String[] daftarForm = {"LoginForm", "KasirUtamaForm", "BayarDialog", "StrukDialog"};
        Path dir = Path.of("src/main/java/com/kopdes");
        for (String nama : daftarForm) {
            String isiJava = java.nio.file.Files.readString(dir.resolve(nama + ".java"));
            String isiForm = java.nio.file.Files.readString(dir.resolve(nama + ".form"));
            assertTrue(isiJava.contains("//GEN-BEGIN:initComponents"), nama + " wajib punya blok initComponents");
            assertTrue(isiJava.contains("//GEN-BEGIN:variables"), nama + " wajib punya blok variables");
            var matcher = java.util.regex.Pattern.compile("<Component class=\"[^\"]+\" name=\"([^\"]+)\"").matcher(isiForm);
            while (matcher.find()) {
                String varName = matcher.group(1);
                assertTrue(isiJava.contains(" " + varName + ";"), nama + ".java kehilangan deklarasi variabel: " + varName);
            }
        }
    }
```

- [ ] **Step 2: Jalankan pengujian untuk memastikan gagal**

Run: `mvn -f "/home/ripky/Documents/Projek Java/coba baru/pom.xml" test`
Expected: FAIL (`cannot find symbol` pada metode baru `KasirUtamaForm` dan file `.form` belum lengkap).

- [ ] **Step 3: Implementasikan `KasirUtamaForm.java` dan `KasirUtamaForm.form` secara lengkap**

Bangun Header Hijau Tua (`#065F46`) dengan slot `logo-ikon.png` / `[RK] RETAIL KOPDES`, info petugas & peran, jam digital, dan tombol **Ganti Petugas**; serta `JTabbedPane` 3 Tab:
1. **Tab 1 (Transaksi Kasir):** Kiri berisi `txtCariBarang` (`F2`), `cmbKategori`, `tblKatalog`, `spnJumlahBeli`, dan tombol `+ Tambah ke Keranjang` (serta klik 2x tabel). Kanan berisi `rbUmum` & `rbAnggota` (potongan 5%), `tblKeranjang`, tombol `+ Qty`, `- Qty`, `Hapus Item`, `Kosongkan`, label status kondisi kosong, kotak ringkasan `#ECFDF5` (Subtotal, Potongan Anggota, Total Bayar), dan satu tombol utama `#C2410C` `PROSES BAYAR (F8)`.
2. **Tab 2 (Kelola Data Barang):** Form CRUD stok lengkap yang otomatis terkunci dengan keterangan jelas jika peran pengguna adalah `Kasir` (aktif penuh untuk `Admin`), serta menyegarkan `tblKatalog` setiap ada perubahan.
3. **Tab 3 (Riwayat Penjualan):** Filter Hari Ini / Semua + pencarian nota, kartu ringkasan nyata dari hasil SQL (`COUNT` nota & `SUM` `long` pemasukan), tabel master `tblRiwayat` & tabel detail `tblDetailRiwayat`, serta tombol **Buka Struk** untuk mencetak/melihat ulang struk dari database.

- [ ] **Step 4: Jalankan seluruh pengujian untuk memastikan lulus 100%**

Run: `mvn -f "/home/ripky/Documents/Projek Java/coba baru/pom.xml" test`
Expected: PASS (`Tests run: 9, Failures: 0`).

- [ ] **Step 5: Commit Task 4**

```bash
git -C "/home/ripky/Documents/Projek Java/coba baru" add src/
git -C "/home/ripky/Documents/Projek Java/coba baru" commit -m "feat: lengkapi KasirUtamaForm 3-tab dan sinkronisasi form netbeans"
```

---

### Task 5: Panduan Laporan Kuliah, Bedah Kode Dosen, Paket JAR, dan Verifikasi Antislop Delivery Gate

**Files:**
- Create: `/home/ripky/Documents/Projek Java/coba baru/PANDUAN_LAPORAN_DAN_BEDAH_KODE.md`
- Create: `/home/ripky/Documents/Projek Java/coba baru/README.md`

**Interfaces:**
- Consumes: Seluruh 5 file kode sumber di `src/main/java/com/kopdes/` dan skema `kopdes.db`
- Produces: Dokumentasi siap pakai untuk laporan kuliah & tanya-jawab dosen, berkas `target/retail-kopdes.jar` yang teruji jalan, serta laporan pemeriksaan penuh *Antislop Delivery Gate*.

- [ ] **Step 1: Tulis `README.md` dan `PANDUAN_LAPORAN_DAN_BEDAH_KODE.md`**

Muat di dalam `PANDUAN_LAPORAN_DAN_BEDAH_KODE.md`:
1. Diagram alur kerja (*flowchart* Mermaid dan tabel langkah alur kerja) untuk ke-4 fitur utama agar bisa langsung disalin ke laporan kuliah.
2. Kamus data lengkap ke-4 tabel SQLite (`pengguna`, `barang`, `transaksi`, `detail_transaksi`) beserta langkah klik demi klik membuka `kopdes.db` di **DB Browser for SQLite** (edit data) dan **DBeaver** (ekspor gambar ER Diagram otomatis).
3. Panduan membuka proyek dan menampilkan tab **Design** di **Apache NetBeans**, serta cara memasang logo buatan sendiri di `src/main/resources/logo/`.
4. Bedah kode baris demi baris untuk ke-5 file `.java` dengan bahasa mahasiswa sehari-hari dan daftar jawaban siap pakai untuk pertanyaan dosen (termasuk alasan memilih `SQLite`, tipe `long` untuk Rupiah, `PreparedStatement`, dan `FlatLaf`).

- [ ] **Step 2: Jalankan build paket lengkap (`mvn clean package`) dan audit karakter terlarang**

Run: `mvn -f "/home/ripky/Documents/Projek Java/coba baru/pom.xml" clean package && ! grep -rnP "\x{2014}" "/home/ripky/Documents/Projek Java/coba baru/src" "/home/ripky/Documents/Projek Java/coba baru/README.md" "/home/ripky/Documents/Projek Java/coba baru/PANDUAN_LAPORAN_DAN_BEDAH_KODE.md"`
Expected: `BUILD SUCCESS`, `target/retail-kopdes.jar` terbentuk, dan perintah `grep` tidak menemukan satu pun karakter *em dash*.

- [ ] **Step 3: Jalankan verifikasi tampilan & komponen interaktif aplikasi (R-35 Delivery Gate)**

Jalankan aplikasi pada lingkungan grafis lokal (atau uji instansiasi & klik terprogram seluruh kontrol Swing `LoginForm`, `KasirUtamaForm` Tab 1/2/3, `BayarDialog`, `StrukDialog` di EDT tanpa `HeadlessException`/error konsol), verifikasi rasio kontras WCAG AA seluruh pasangan warna di `KoneksiDB`, dan catat bukti setiap kontrol interaktif untuk laporan *Delivery Gate*.

- [ ] **Step 4: Commit Task 5**

```bash
git -C "/home/ripky/Documents/Projek Java/coba baru" add README.md PANDUAN_LAPORAN_DAN_BEDAH_KODE.md
git -C "/home/ripky/Documents/Projek Java/coba baru" commit -m "docs: tambahkan panduan laporan kuliah, ERD, dan bedah kode dosen"
```
