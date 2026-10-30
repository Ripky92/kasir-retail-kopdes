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
        List<String[]> items = List.<String[]>of(
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
