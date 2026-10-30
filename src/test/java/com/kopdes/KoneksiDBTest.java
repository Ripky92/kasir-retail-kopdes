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

    @Test
    void buatNomorNotaBaruTetapUrutDiAtas999DanTahanSuffixManual() throws Exception {
        String hariIni = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE);
        String awalan = "KPD-" + hariIni + "-";
        try (Connection c = KoneksiDB.ambilKoneksi(); Statement s = c.createStatement()) {
            s.executeUpdate("INSERT INTO transaksi VALUES ('" + awalan + "999', '2026-10-31 08:00:00', 1, 'Umum', 1000, 0, 1000, 'Tunai', 1000, 0)");
            s.executeUpdate("INSERT INTO transaksi VALUES ('" + awalan + "1000', '2026-10-31 08:01:00', 1, 'Umum', 1000, 0, 1000, 'Tunai', 1000, 0)");
            s.executeUpdate("INSERT INTO transaksi VALUES ('" + awalan + "UJI', '2026-10-31 08:02:00', 1, 'Umum', 1000, 0, 1000, 'Tunai', 1000, 0)");
            assertEquals(awalan + "1001", KoneksiDB.buatNomorNotaBaru(c));
        }
    }

    @Test
    void siapkanKoneksiMenutupKoneksiBilaPragmaGagal() {
        boolean[] sudahDitutup = {false};
        Connection koneksiPalsu = (Connection) java.lang.reflect.Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class<?>[]{Connection.class},
                (proxy, method, args) -> {
                    if ("createStatement".equals(method.getName())) {
                        throw new java.sql.SQLException("Simulasi gagal PRAGMA");
                    }
                    if ("close".equals(method.getName())) {
                        sudahDitutup[0] = true;
                        return null;
                    }
                    throw new UnsupportedOperationException(method.getName());
                }
        );
        assertThrows(java.sql.SQLException.class, () -> KoneksiDB.aktifkanForeignKey(koneksiPalsu));
        assertTrue(sudahDitutup[0], "Koneksi wajib ditutup bila eksekusi PRAGMA gagal");
    }
}

