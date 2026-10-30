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
