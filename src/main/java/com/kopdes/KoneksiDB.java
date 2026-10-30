package com.kopdes;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class KoneksiDB {

    public static final Color WARNA_HEADER = new Color(6, 95, 70);
    public static final Color WARNA_LATAR = new Color(240, 253, 244);
    public static final Color WARNA_KARTU = new Color(255, 255, 255);
    public static final Color WARNA_RINGKASAN = new Color(236, 253, 245);
    public static final Color WARNA_TEPI_RINGKASAN = new Color(110, 231, 183);
    public static final Color WARNA_TEKS_UTAMA = new Color(15, 23, 42);
    public static final Color WARNA_TEKS_HIJAU = new Color(6, 78, 59);
    public static final Color WARNA_TOMBOL_BAYAR = new Color(194, 65, 12);
    public static final Color WARNA_BAHAYA = new Color(185, 28, 28);
    public static final Color WARNA_TEKS_PUTIH = new Color(255, 255, 255);

    public static final int PERSEN_DISKON_ANGGOTA = 5;

    private static String lokasiDatabase = "kopdes.db";

    public static void aturLokasiDatabase(String pathFileDb) {
        if (pathFileDb != null && !pathFileDb.isBlank()) {
            lokasiDatabase = pathFileDb.trim();
        }
    }

    public static Connection ambilKoneksi() throws SQLException {
        Connection conn = DriverManager.getConnection("jdbc:sqlite:" + lokasiDatabase);
        // SQLite mematikan foreign key secara bawaan; wajib diaktifkan tiap koneksi dibuka.
        try (Statement st = conn.createStatement()) {
            st.execute("PRAGMA foreign_keys = ON;");
        }
        return conn;
    }

    public static void siapkanDatabase() {
        try (Connection conn = ambilKoneksi(); Statement st = conn.createStatement()) {
            st.execute("""
                CREATE TABLE IF NOT EXISTS pengguna (
                    id_pengguna INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT NOT NULL UNIQUE,
                    password TEXT NOT NULL,
                    nama_lengkap TEXT NOT NULL,
                    peran TEXT NOT NULL
                )
                """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS barang (
                    kode_barang TEXT PRIMARY KEY,
                    nama_barang TEXT NOT NULL,
                    kategori TEXT NOT NULL,
                    harga INTEGER NOT NULL CHECK(harga >= 0),
                    stok INTEGER NOT NULL CHECK(stok >= 0)
                )
                """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS transaksi (
                    no_nota TEXT PRIMARY KEY,
                    tanggal_waktu TEXT NOT NULL,
                    id_pengguna INTEGER NOT NULL,
                    jenis_pembeli TEXT NOT NULL,
                    subtotal INTEGER NOT NULL,
                    potongan_anggota INTEGER NOT NULL,
                    total_bayar INTEGER NOT NULL,
                    metode_bayar TEXT NOT NULL,
                    uang_bayar INTEGER NOT NULL,
                    kembalian INTEGER NOT NULL,
                    FOREIGN KEY (id_pengguna) REFERENCES pengguna(id_pengguna)
                )
                """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS detail_transaksi (
                    id_detail INTEGER PRIMARY KEY AUTOINCREMENT,
                    no_nota TEXT NOT NULL,
                    kode_barang TEXT NOT NULL,
                    nama_barang TEXT NOT NULL,
                    harga_satuan INTEGER NOT NULL,
                    jumlah INTEGER NOT NULL,
                    subtotal_item INTEGER NOT NULL,
                    FOREIGN KEY (no_nota) REFERENCES transaksi(no_nota) ON DELETE CASCADE,
                    FOREIGN KEY (kode_barang) REFERENCES barang(kode_barang)
                )
                """);

            isiDataAwalJikaKosong(conn);
        } catch (SQLException ex) {
            throw new RuntimeException("Gagal menyiapkan database SQLite: " + ex.getMessage(), ex);
        }
    }

    private static void isiDataAwalJikaKosong(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM pengguna")) {
            if (rs.next() && rs.getInt(1) == 0) {
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO pengguna (username, password, nama_lengkap, peran) VALUES (?, ?, ?, ?)")) {
                    ps.setString(1, "admin");
                    ps.setString(2, "admin123");
                    ps.setString(3, "Pengurus Kopdes");
                    ps.setString(4, "Admin");
                    ps.executeUpdate();

                    ps.setString(1, "kasir");
                    ps.setString(2, "kasir123");
                    ps.setString(3, "Petugas Kasir");
                    ps.setString(4, "Kasir");
                    ps.executeUpdate();
                }
            }
        }

        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM barang")) {
            if (rs.next() && rs.getInt(1) == 0) {
                Object[][] daftarAwal = {
                    {"BRG001", "Beras Desa Premium 5kg", "Sembako", 68000L, 25},
                    {"BRG002", "Minyak Goreng Sawit 1L", "Sembako", 16500L, 40},
                    {"BRG003", "Gula Pasir Putih 1kg", "Sembako", 17000L, 35},
                    {"BRG004", "Telur Ayam Negeri 1kg", "Sembako", 28000L, 20},
                    {"BRG005", "Tepung Terigu Serbaguna 1kg", "Sembako", 12500L, 30},
                    {"BRG006", "Mie Instan Goreng", "Sembako", 3500L, 80},
                    {"BRG007", "Teh Celup Melati Isi 25", "Minuman", 6500L, 45},
                    {"BRG008", "Kopi Bubuk Nusantara 165g", "Minuman", 14500L, 30},
                    {"BRG009", "Air Mineral Botol 600ml", "Minuman", 3500L, 60},
                    {"BRG010", "Sabun Cuci Piring 750ml", "Kebutuhan Rumah", 15000L, 25}
                };
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO barang (kode_barang, nama_barang, kategori, harga, stok) VALUES (?, ?, ?, ?, ?)")) {
                    for (Object[] b : daftarAwal) {
                        ps.setString(1, (String) b[0]);
                        ps.setString(2, (String) b[1]);
                        ps.setString(3, (String) b[2]);
                        ps.setLong(4, (Long) b[3]);
                        ps.setInt(5, (Integer) b[4]);
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
            }
        }
    }

    public static String formatRupiah(long nominal) {
        DecimalFormatSymbols simbol = new DecimalFormatSymbols();
        simbol.setGroupingSeparator('.');
        DecimalFormat df = new DecimalFormat("#,###", simbol);
        if (nominal < 0) {
            // Hindari overflow Math.abs pada Long.MIN_VALUE dengan memotong tanda minus dari hasil format.
            return "-Rp " + df.format(nominal).substring(1);
        }
        return "Rp " + df.format(nominal);
    }

    public static long parseNominalRupiah(String teksInput) {
        if (teksInput == null) {
            return -1L;
        }
        String bersih = teksInput.trim();
        if (bersih.regionMatches(true, 0, "Rp", 0, 2)) {
            bersih = bersih.substring(2).trim();
        }
        bersih = bersih.replace(".", "").replace(" ", "");
        if (bersih.isEmpty() || !bersih.matches("\\d+")) {
            return -1L;
        }
        try {
            return Long.parseLong(bersih);
        } catch (NumberFormatException ex) {
            return -1L;
        }
    }

    public static long hitungPotonganAnggota(long subtotal, boolean adalahAnggota) {
        if (!adalahAnggota || subtotal <= 0) {
            return 0L;
        }
        return Math.round(subtotal * (PERSEN_DISKON_ANGGOTA / 100.0));
    }

    public static String buatNomorNotaBaru(Connection conn) throws SQLException {
        String tanggalHariIni = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String awalan = "KPD-" + tanggalHariIni + "-";
        int urutanBerikutnya = 1;

        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT no_nota FROM transaksi WHERE no_nota LIKE ? ORDER BY no_nota DESC LIMIT 1")) {
            ps.setString(1, awalan + "%");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String notaTerakhir = rs.getString(1);
                    String angkaAkhir = notaTerakhir.substring(awalan.length());
                    urutanBerikutnya = Integer.parseInt(angkaAkhir) + 1;
                }
            }
        }
        return String.format("%s%03d", awalan, urutanBerikutnya);
    }

    public static ImageIcon muatLogoOpsional(String namaFile, int lebar, int tinggi) {
        if (namaFile == null || namaFile.isBlank()) {
            return null;
        }
        try {
            BufferedImage gambarAsli = null;
            try (InputStream is = KoneksiDB.class.getResourceAsStream("/logo/" + namaFile)) {
                if (is != null) {
                    gambarAsli = ImageIO.read(is);
                }
            }
            if (gambarAsli == null) {
                File fileLokal = new File("src/main/resources/logo/" + namaFile);
                if (fileLokal.isFile()) {
                    gambarAsli = ImageIO.read(fileLokal);
                }
            }
            if (gambarAsli == null) {
                return null;
            }
            Image hasilSkala = gambarAsli.getScaledInstance(lebar, tinggi, Image.SCALE_SMOOTH);
            return new ImageIcon(hasilSkala);
        } catch (Exception ex) {
            return null;
        }
    }
}

