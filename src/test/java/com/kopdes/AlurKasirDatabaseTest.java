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

    @Test
    void simpanTransaksiMemotongStokSecaraAtomikDanRollbackJikaStokKurang() throws Exception {
        List<String[]> belanjaValid = List.<String[]>of(
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

        List<String[]> belanjaMelebihiStok = List.<String[]>of(
            new String[]{"BRG001", "Beras Desa Premium 5kg", "68000", "99999", "6799932000"}
        );
        assertThrows(IllegalArgumentException.class, () ->
            BayarDialog.simpanTransaksiKeDatabase(
                1, "Umum", belanjaMelebihiStok, 6_799_932_000L, 0L, 6_799_932_000L, "QRIS", 6_799_932_000L
            )
        );
    }

    @Test
    void batasStokKeranjangDanProteksiHapusBarangTerjualBerjalanBenar() throws Exception {
        assertEquals(3, KasirUtamaForm.hitungJumlahBaruKeranjang(2, 1, 3));
        assertThrows(IllegalArgumentException.class, () ->
            KasirUtamaForm.hitungJumlahBaruKeranjang(3, 1, 3)
        );

        KasirUtamaForm.simpanBarangBaru("BRG999", "Teh Desa", "Minuman", 5_000L, 10);
        KasirUtamaForm.hapusDataBarang("BRG999");

        List<String[]> jual = List.<String[]>of(
            new String[]{"BRG001", "Beras Desa Premium 5kg", "68000", "1", "68000"}
        );
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
            var matcher = java.util.regex.Pattern
                    .compile("<(?:Component|Container) class=\"[^\"]+\" name=\"([^\"]+)\"")
                    .matcher(isiForm);
            while (matcher.find()) {
                String varName = matcher.group(1);
                assertTrue(isiJava.contains(" " + varName + ";"), nama + ".java kehilangan deklarasi variabel: " + varName);
            }
        }
    }

    @Test
    void seluruhKontrolInteraktifSwingBereaksiTanpaGalatDiEdt() throws Exception {
        org.junit.jupiter.api.Assumptions.assumeFalse(
                java.awt.GraphicsEnvironment.isHeadless(),
                "Membutuhkan lingkungan grafis lokal"
        );
        com.formdev.flatlaf.FlatLightLaf.setup();

        javax.swing.SwingUtilities.invokeAndWait(() -> {
            try {
                // 1. Uji LoginForm (validasi kosong & sandi salah)
                LoginForm login = new LoginForm();
                ambilKomponen(login, "btnMasuk", javax.swing.JButton.class).doClick();
                assertTrue(ambilKomponen(login, "lblPesan", javax.swing.JLabel.class).getText().contains("username belum diisi"));

                ambilKomponen(login, "txtUsername", javax.swing.JTextField.class).setText("admin");
                ambilKomponen(login, "txtPassword", javax.swing.JPasswordField.class).setText("salah");
                ambilKomponen(login, "btnMasuk", javax.swing.JButton.class).doClick();
                assertTrue(ambilKomponen(login, "lblPesan", javax.swing.JLabel.class).getText().contains("tidak sesuai"));
                login.dispose();

                // 2. Uji KasirUtamaForm peran Kasir (mode baca-saja di Tab Stok)
                KasirUtamaForm formKasir = new KasirUtamaForm(2, "Budi Santoso", "Kasir");
                assertFalse(ambilKomponen(formKasir, "btnSimpanBarang", javax.swing.JButton.class).isEnabled());
                assertTrue(ambilKomponen(formKasir, "lblInfoAksesStok", javax.swing.JLabel.class).getText().contains("Baca Saja"));
                formKasir.dispose();

                // 3. Uji KasirUtamaForm peran Admin (Tab 1 Kasir, Tab 2 Stok, Tab 3 Riwayat)
                KasirUtamaForm formAdmin = new KasirUtamaForm(1, "Siti Rahma", "Admin");
                assertTrue(ambilKomponen(formAdmin, "btnSimpanBarang", javax.swing.JButton.class).isEnabled());
                assertFalse(ambilKomponen(formAdmin, "btnProsesBayar", javax.swing.JButton.class).isEnabled());

                javax.swing.JTextField txtCari = ambilKomponen(formAdmin, "txtCariBarang", javax.swing.JTextField.class);
                javax.swing.JTable tblKatalog = ambilKomponen(formAdmin, "tblKatalog", javax.swing.JTable.class);
                txtCari.setText("Beras");
                assertEquals(1, tblKatalog.getRowCount());

                tblKatalog.setRowSelectionInterval(0, 0);
                ambilKomponen(formAdmin, "spnJumlahBeli", javax.swing.JSpinner.class).setValue(2);
                ambilKomponen(formAdmin, "btnTambahKeranjang", javax.swing.JButton.class).doClick();

                javax.swing.JTable tblKeranjang = ambilKomponen(formAdmin, "tblKeranjang", javax.swing.JTable.class);
                assertEquals(1, tblKeranjang.getRowCount());
                assertTrue(ambilKomponen(formAdmin, "btnProsesBayar", javax.swing.JButton.class).isEnabled());

                ambilKomponen(formAdmin, "rbAnggota", javax.swing.JRadioButton.class).doClick();
                assertEquals("-Rp 6.800", ambilKomponen(formAdmin, "lblPotonganNilai", javax.swing.JLabel.class).getText());
                assertEquals("Rp 129.200", ambilKomponen(formAdmin, "lblTotalBayarNilai", javax.swing.JLabel.class).getText());

                ambilKomponen(formAdmin, "rbUmum", javax.swing.JRadioButton.class).doClick();
                assertEquals("Rp 0", ambilKomponen(formAdmin, "lblPotonganNilai", javax.swing.JLabel.class).getText());
                assertEquals("Rp 136.000", ambilKomponen(formAdmin, "lblTotalBayarNilai", javax.swing.JLabel.class).getText());

                tblKeranjang.setRowSelectionInterval(0, 0);
                ambilKomponen(formAdmin, "btnTambahQty", javax.swing.JButton.class).doClick();
                assertEquals(3, tblKeranjang.getValueAt(0, 3));
                ambilKomponen(formAdmin, "btnKurangQty", javax.swing.JButton.class).doClick();
                assertEquals(2, tblKeranjang.getValueAt(0, 3));

                // 4. Uji BayarDialog (Tunai cepat & QRIS)
                List<String[]> keranjangUji = List.<String[]>of(
                        new String[]{"BRG001", "Beras Desa Premium 5kg", "68000", "2", "136000"}
                );
                BayarDialog bayar = new BayarDialog(
                        formAdmin, false, 1, "Siti Rahma", "Umum", keranjangUji, 136_000L, 0L, 136_000L
                );
                ambilKomponen(bayar, "btn20Ribu", javax.swing.JButton.class).doClick();
                assertFalse(ambilKomponen(bayar, "btnSelesaikan", javax.swing.JButton.class).isEnabled());

                ambilKomponen(bayar, "btnUangPas", javax.swing.JButton.class).doClick();
                assertTrue(ambilKomponen(bayar, "btnSelesaikan", javax.swing.JButton.class).isEnabled());

                javax.swing.JTextField txtUang = ambilKomponen(bayar, "txtUangDiterima", javax.swing.JTextField.class);
                txtUang.setText("200.000");
                assertTrue(ambilKomponen(bayar, "lblNilaiKembalian", javax.swing.JLabel.class).getText().contains("Rp 64.000"));

                @SuppressWarnings("unchecked")
                javax.swing.JComboBox<String> cmbMetode = ambilKomponen(bayar, "cmbMetodeBayar", javax.swing.JComboBox.class);
                cmbMetode.setSelectedItem("QRIS");
                assertTrue(ambilKomponen(bayar, "lblInfoQris", javax.swing.JLabel.class).isVisible());
                assertFalse(txtUang.isEditable());
                assertTrue(ambilKomponen(bayar, "btnSelesaikan", javax.swing.JButton.class).isEnabled());
                bayar.dispose();

                // 5. Uji Tab 2 Stok & Tab 3 Riwayat (termasuk reaksi langsung txtCariNota)
                String noNota = BayarDialog.simpanTransaksiKeDatabase(
                        1, "Anggota Kopdes", keranjangUji, 136_000L, 6_800L, 129_200L, "Tunai", 150_000L
                );

                javax.swing.JTable tblStok = ambilKomponen(formAdmin, "tblStokBarang", javax.swing.JTable.class);
                tblStok.setRowSelectionInterval(0, 0);
                assertEquals("BRG001", ambilKomponen(formAdmin, "txtKodeBarang", javax.swing.JTextField.class).getText());
                ambilKomponen(formAdmin, "btnBersihkanForm", javax.swing.JButton.class).doClick();
                assertEquals("", ambilKomponen(formAdmin, "txtKodeBarang", javax.swing.JTextField.class).getText());

                ambilKomponen(formAdmin, "btnMuatUlangRiwayat", javax.swing.JButton.class).doClick();
                javax.swing.JTable tblRiwayat = ambilKomponen(formAdmin, "tblRiwayat", javax.swing.JTable.class);
                assertEquals(1, tblRiwayat.getRowCount());
                tblRiwayat.setRowSelectionInterval(0, 0);
                assertEquals(1, ambilKomponen(formAdmin, "tblDetailRiwayat", javax.swing.JTable.class).getRowCount());
                assertTrue(ambilKomponen(formAdmin, "btnBukaStruk", javax.swing.JButton.class).isEnabled());

                javax.swing.JTextField txtCariNota = ambilKomponen(formAdmin, "txtCariNota", javax.swing.JTextField.class);
                txtCariNota.setText("NOTA-TIDAK-ADA");
                assertEquals(0, tblRiwayat.getRowCount(), "txtCariNota harus langsung menyaring tabel riwayat saat diketik");
                txtCariNota.setText(noNota);
                assertEquals(1, tblRiwayat.getRowCount());

                // 6. Uji StrukDialog
                String teksStruk = StrukDialog.susunTeksStruk(
                        noNota, "2026-10-31 10:00:00", "Siti Rahma", "Anggota Kopdes",
                        keranjangUji, 136_000L, 6_800L, 129_200L, "Tunai", 150_000L, 20_800L
                );
                StrukDialog strukDialog = new StrukDialog(formAdmin, false, teksStruk);
                assertTrue(ambilKomponen(strukDialog, "areaStruk", javax.swing.JTextArea.class).getText().contains(noNota));
                ambilKomponen(strukDialog, "btnTutup", javax.swing.JButton.class).doClick();
                assertFalse(strukDialog.isDisplayable());

                formAdmin.dispose();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    private static <T> T ambilKomponen(Object pemilik, String namaField, Class<T> tipe) throws Exception {
        java.lang.reflect.Field f = pemilik.getClass().getDeclaredField(namaField);
        f.setAccessible(true);
        return tipe.cast(f.get(pemilik));
    }
}
