package com.kopdes;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class KasirUtamaForm extends javax.swing.JFrame {

    private final int idPengguna;
    private final String namaLengkap;
    private final String peran;

    // Setiap elemen menyimpan {kode, nama, hargaSatuanStr, qtyStr, subtotalStr}
    private final List<String[]> daftarKeranjang = new ArrayList<>();
    private Timer timerJam;

    private static final DateTimeFormatter FORMAT_JAM =
            DateTimeFormatter.ofPattern("dd MMM yyyy  HH:mm:ss", Locale.forLanguageTag("id-ID"));

    public KasirUtamaForm(int idPengguna, String namaLengkap, String peran) {
        this.idPengguna = idPengguna;
        this.namaLengkap = namaLengkap;
        this.peran = peran;

        initComponents();
        siapkanAntarmukaAwal();
        muatTabelKatalog();
        muatTabelStokBarang();
        muatTabelRiwayat();
        perbaruiTampilanKeranjang();
    }

    private void siapkanAntarmukaAwal() {
        setTitle("Retail Kopdes - Petugas: " + namaLengkap + " (" + peran + ")");

        panelHeaderAtas.setBackground(KoneksiDB.WARNA_HEADER);
        lblLogoHeader.setForeground(KoneksiDB.WARNA_TEKS_PUTIH);
        lblJudulHeader.setForeground(KoneksiDB.WARNA_TEKS_PUTIH);
        lblInfoPetugas.setForeground(KoneksiDB.WARNA_RINGKASAN);
        lblJamDigital.setForeground(KoneksiDB.WARNA_RINGKASAN);

        lblInfoPetugas.setText("Petugas: " + namaLengkap + " (" + peran + ")");

        ImageIcon logoIkon = KoneksiDB.muatLogoOpsional("logo-ikon.png", 36, 36);
        if (logoIkon != null) {
            setIconImage(logoIkon.getImage());
            lblLogoHeader.setText("");
            lblLogoHeader.setIcon(logoIkon);
        } else {
            lblLogoHeader.setText("[RK]");
        }

        panelTabKasir.setBackground(KoneksiDB.WARNA_LATAR);
        panelKiriKatalog.setBackground(KoneksiDB.WARNA_KARTU);
        panelCariKatalog.setBackground(KoneksiDB.WARNA_KARTU);
        panelBawahKatalog.setBackground(KoneksiDB.WARNA_KARTU);

        panelKananKeranjang.setBackground(KoneksiDB.WARNA_KARTU);
        panelJenisPembeli.setBackground(KoneksiDB.WARNA_LATAR);
        panelAksiKeranjang.setBackground(KoneksiDB.WARNA_KARTU);
        panelRingkasanTotal.setBackground(KoneksiDB.WARNA_RINGKASAN);
        lblSubtotalJudul.setForeground(KoneksiDB.WARNA_TEKS_HIJAU);
        lblSubtotalNilai.setForeground(KoneksiDB.WARNA_TEKS_HIJAU);
        lblPotonganJudul.setForeground(KoneksiDB.WARNA_TEKS_HIJAU);
        lblPotonganNilai.setForeground(KoneksiDB.WARNA_TEKS_HIJAU);
        lblTotalBayarJudul.setForeground(KoneksiDB.WARNA_TEKS_HIJAU);
        lblTotalBayarNilai.setForeground(KoneksiDB.WARNA_TEKS_HIJAU);

        panelTabStok.setBackground(KoneksiDB.WARNA_LATAR);
        panelFormBarang.setBackground(KoneksiDB.WARNA_KARTU);
        panelTombolCrud.setBackground(KoneksiDB.WARNA_KARTU);

        panelTabRiwayat.setBackground(KoneksiDB.WARNA_LATAR);
        panelAtasRiwayat.setBackground(KoneksiDB.WARNA_KARTU);
        panelBawahDetailRiwayat.setBackground(KoneksiDB.WARNA_KARTU);

        tblKatalog.getTableHeader().setBackground(KoneksiDB.WARNA_HEADER);
        tblKatalog.getTableHeader().setForeground(KoneksiDB.WARNA_TEKS_PUTIH);
        tblKeranjang.getTableHeader().setBackground(KoneksiDB.WARNA_HEADER);
        tblKeranjang.getTableHeader().setForeground(KoneksiDB.WARNA_TEKS_PUTIH);
        tblStokBarang.getTableHeader().setBackground(KoneksiDB.WARNA_HEADER);
        tblStokBarang.getTableHeader().setForeground(KoneksiDB.WARNA_TEKS_PUTIH);
        tblRiwayat.getTableHeader().setBackground(KoneksiDB.WARNA_HEADER);
        tblRiwayat.getTableHeader().setForeground(KoneksiDB.WARNA_TEKS_PUTIH);
        tblDetailRiwayat.getTableHeader().setBackground(KoneksiDB.WARNA_HEADER);
        tblDetailRiwayat.getTableHeader().setForeground(KoneksiDB.WARNA_TEKS_PUTIH);

        inisialisasiModelTabel();

        btnTambahKeranjang.setBackground(KoneksiDB.WARNA_HEADER);
        btnTambahKeranjang.setForeground(KoneksiDB.WARNA_TEKS_PUTIH);
        btnProsesBayar.setBackground(KoneksiDB.WARNA_TOMBOL_BAYAR);
        btnProsesBayar.setForeground(KoneksiDB.WARNA_TEKS_PUTIH);

        btnSimpanBarang.setBackground(KoneksiDB.WARNA_HEADER);
        btnSimpanBarang.setForeground(KoneksiDB.WARNA_TEKS_PUTIH);
        btnHapusBarang.setBackground(KoneksiDB.WARNA_BAHAYA);
        btnHapusBarang.setForeground(KoneksiDB.WARNA_TEKS_PUTIH);

        btnBukaStruk.setBackground(KoneksiDB.WARNA_HEADER);
        btnBukaStruk.setForeground(KoneksiDB.WARNA_TEKS_PUTIH);

        spnJumlahBeli.setModel(new SpinnerNumberModel(1, 1, 9999, 1));

        lblCariBarang.setLabelFor(txtCariBarang);
        lblJumlahBeli.setLabelFor(spnJumlahBeli);
        lblKodeBarang.setLabelFor(txtKodeBarang);
        lblNamaBarang.setLabelFor(txtNamaBarang);
        lblKategoriBarang.setLabelFor(cmbKategoriBarang);
        lblHargaBarang.setLabelFor(txtHargaBarang);
        lblStokBarang.setLabelFor(txtStokBarang);
        lblCariNota.setLabelFor(txtCariNota);

        Cursor tangan = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
        btnLogout.setCursor(tangan);
        cmbKategori.setCursor(tangan);
        btnTambahKeranjang.setCursor(tangan);
        rbUmum.setCursor(tangan);
        rbAnggota.setCursor(tangan);
        btnTambahQty.setCursor(tangan);
        btnKurangQty.setCursor(tangan);
        btnHapusItem.setCursor(tangan);
        btnKosongkanKeranjang.setCursor(tangan);
        btnProsesBayar.setCursor(tangan);
        btnSimpanBarang.setCursor(tangan);
        btnUbahBarang.setCursor(tangan);
        btnHapusBarang.setCursor(tangan);
        btnBersihkanForm.setCursor(tangan);
        cmbPeriodeRiwayat.setCursor(tangan);
        btnMuatUlangRiwayat.setCursor(tangan);
        btnBukaStruk.setCursor(tangan);

        aturHakAksesPeran();
        pasangPendengarInteraksi();
        pasangPintasanKeyboard();
        mulaiJamDigital();
    }

    private void inisialisasiModelTabel() {
        tblKatalog.setModel(new DefaultTableModel(
            new Object[][]{},
            new String[]{"Kode", "Nama Barang", "Kategori", "Harga", "Stok"}
        ) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        });

        tblKeranjang.setModel(new DefaultTableModel(
            new Object[][]{},
            new String[]{"Kode", "Nama Barang", "Harga", "Qty", "Subtotal"}
        ) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        });

        tblStokBarang.setModel(new DefaultTableModel(
            new Object[][]{},
            new String[]{"Kode", "Nama Barang", "Kategori", "Harga", "Stok", "Status"}
        ) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        });

        tblRiwayat.setModel(new DefaultTableModel(
            new Object[][]{},
            new String[]{"No. Nota", "Tanggal & Waktu", "Petugas", "Pembeli", "Metode Bayar", "Total Bayar"}
        ) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        });

        tblDetailRiwayat.setModel(new DefaultTableModel(
            new Object[][]{},
            new String[]{"Kode Barang", "Nama Barang", "Harga Satuan", "Jumlah", "Subtotal Item"}
        ) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        });

        tblKatalog.setRowHeight(28);
        tblKeranjang.setRowHeight(28);
        tblStokBarang.setRowHeight(28);
        tblRiwayat.setRowHeight(28);
        tblDetailRiwayat.setRowHeight(26);

        tblKatalog.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tblKeranjang.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tblStokBarang.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tblRiwayat.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tblDetailRiwayat.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
    }

    private void aturHakAksesPeran() {
        boolean adalahAdmin = "Admin".equalsIgnoreCase(peran);
        txtKodeBarang.setEnabled(adalahAdmin);
        txtNamaBarang.setEnabled(adalahAdmin);
        cmbKategoriBarang.setEnabled(adalahAdmin);
        txtHargaBarang.setEnabled(adalahAdmin);
        txtStokBarang.setEnabled(adalahAdmin);
        btnSimpanBarang.setEnabled(adalahAdmin);
        btnUbahBarang.setEnabled(adalahAdmin);
        btnHapusBarang.setEnabled(adalahAdmin);
        btnBersihkanForm.setEnabled(adalahAdmin);

        if (adalahAdmin) {
            lblInfoAksesStok.setForeground(KoneksiDB.WARNA_TEKS_HIJAU);
            lblInfoAksesStok.setText("Mode Admin: Isi form untuk menambah barang baru, atau klik baris tabel untuk mengubah/menghapus.");
        } else {
            lblInfoAksesStok.setForeground(KoneksiDB.WARNA_BAHAYA);
            lblInfoAksesStok.setText("Mode Kasir (Baca Saja): Penambahan dan perubahan stok barang dikhususkan bagi akun Admin.");
        }
    }

    private void pasangPendengarInteraksi() {
        txtCariBarang.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                muatTabelKatalog();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                muatTabelKatalog();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                muatTabelKatalog();
            }
        });

        txtCariNota.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                muatTabelRiwayat();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                muatTabelRiwayat();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                muatTabelRiwayat();
            }
        });

        tblKatalog.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && tblKatalog.getSelectedRow() >= 0) {
                    masukkanBarangTerpilihKeKeranjang(1);
                }
            }
        });

        tblStokBarang.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tblStokBarang.getSelectedRow() >= 0) {
                isiFormDariBarisStokTerpilih();
            }
        });

        tblRiwayat.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                muatDetailRiwayatTerpilih();
            }
        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                if (timerJam != null) {
                    timerJam.stop();
                }
            }
        });
    }

    private void pasangPintasanKeyboard() {
        JComponent akar = getRootPane();
        akar.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), "fokusCari");
        akar.getActionMap().put("fokusCari", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tabMenuUtama.setSelectedIndex(0);
                txtCariBarang.requestFocusInWindow();
                txtCariBarang.selectAll();
            }
        });

        akar.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0), "bukaBayar");
        akar.getActionMap().put("bukaBayar", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!daftarKeranjang.isEmpty()) {
                    bukaDialogPembayaran();
                }
            }
        });
    }

    private void mulaiJamDigital() {
        lblJamDigital.setText(LocalDateTime.now().format(FORMAT_JAM));
        timerJam = new Timer(1000, e -> lblJamDigital.setText(LocalDateTime.now().format(FORMAT_JAM)));
        timerJam.start();
    }

    public static int hitungJumlahBaruKeranjang(int jumlahSaatIniDiKeranjang, int tambahanDiminta, int stokTersedia) {
        if (tambahanDiminta <= 0) {
            throw new IllegalArgumentException("Jumlah barang yang ditambahkan minimal 1.");
        }
        int hasilBaru = jumlahSaatIniDiKeranjang + tambahanDiminta;
        if (hasilBaru > stokTersedia) {
            throw new IllegalArgumentException(
                    "Jumlah di keranjang (" + hasilBaru + ") melebihi sisa stok tersedia (" + stokTersedia + ").");
        }
        return hasilBaru;
    }

    public static void simpanBarangBaru(String kode, String nama, String kategori, long harga, int stok) throws Exception {
        validasiInputBarang(kode, nama, kategori, harga, stok);
        try (Connection conn = KoneksiDB.ambilKoneksi()) {
            try (PreparedStatement cek = conn.prepareStatement("SELECT 1 FROM barang WHERE kode_barang = ?")) {
                cek.setString(1, kode.trim());
                try (ResultSet rs = cek.executeQuery()) {
                    if (rs.next()) {
                        throw new IllegalArgumentException("Kode barang \"" + kode.trim() + "\" sudah terdaftar. Gunakan kode lain atau klik Perbarui Data.");
                    }
                }
            }
            String sql = "INSERT INTO barang (kode_barang, nama_barang, kategori, harga, stok) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, kode.trim());
                ps.setString(2, nama.trim());
                ps.setString(3, kategori.trim());
                ps.setLong(4, harga);
                ps.setInt(5, stok);
                ps.executeUpdate();
            }
        }
    }

    public static void ubahDataBarang(String kode, String nama, String kategori, long harga, int stok) throws Exception {
        validasiInputBarang(kode, nama, kategori, harga, stok);
        String sql = "UPDATE barang SET nama_barang = ?, kategori = ?, harga = ?, stok = ? WHERE kode_barang = ?";
        try (Connection conn = KoneksiDB.ambilKoneksi();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nama.trim());
            ps.setString(2, kategori.trim());
            ps.setLong(3, harga);
            ps.setInt(4, stok);
            ps.setString(5, kode.trim());
            int terubah = ps.executeUpdate();
            if (terubah == 0) {
                throw new IllegalArgumentException("Barang dengan kode \"" + kode.trim() + "\" tidak ditemukan di database.");
            }
        }
    }

    public static void hapusDataBarang(String kode) throws Exception {
        if (kode == null || kode.isBlank()) {
            throw new IllegalArgumentException("Pilih barang yang ingin dihapus terlebih dahulu.");
        }
        try (Connection conn = KoneksiDB.ambilKoneksi()) {
            try (PreparedStatement cekPakai = conn.prepareStatement(
                    "SELECT COUNT(*) FROM detail_transaksi WHERE kode_barang = ?")) {
                cekPakai.setString(1, kode.trim());
                try (ResultSet rs = cekPakai.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        throw new IllegalStateException(
                                "Barang \"" + kode.trim() + "\" tidak dapat dihapus karena sudah tercatat di riwayat nota penjualan.\n"
                                + "Silakan ubah sisa stoknya menjadi 0 bila barang sudah tidak dijual.");
                    }
                }
            }
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM barang WHERE kode_barang = ?")) {
                ps.setString(1, kode.trim());
                ps.executeUpdate();
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Gagal menghapus barang karena masih terhubung dengan data transaksi.", ex);
        }
    }

    private static void validasiInputBarang(String kode, String nama, String kategori, long harga, int stok) {
        if (kode == null || kode.isBlank()) {
            throw new IllegalArgumentException("Kode barang wajib diisi.");
        }
        if (nama == null || nama.isBlank()) {
            throw new IllegalArgumentException("Nama barang wajib diisi.");
        }
        if (kategori == null || kategori.isBlank()) {
            throw new IllegalArgumentException("Kategori barang wajib dipilih.");
        }
        if (harga < 0) {
            throw new IllegalArgumentException("Harga barang harus berupa angka Rupiah 0 atau lebih.");
        }
        if (stok < 0) {
            throw new IllegalArgumentException("Stok barang harus berupa angka bulat 0 atau lebih.");
        }
    }

    private void muatTabelKatalog() {
        DefaultTableModel model = (DefaultTableModel) tblKatalog.getModel();
        model.setRowCount(0);

        String kataKunci = txtCariBarang.getText().trim();
        String kategori = String.valueOf(cmbKategori.getSelectedItem());
        boolean semuaKategori = "Semua".equalsIgnoreCase(kategori);

        StringBuilder sql = new StringBuilder(
                "SELECT kode_barang, nama_barang, kategori, harga, stok FROM barang WHERE 1=1");
        if (!semuaKategori) {
            sql.append(" AND kategori = ?");
        }
        if (!kataKunci.isEmpty()) {
            sql.append(" AND (LOWER(kode_barang) LIKE ? OR LOWER(nama_barang) LIKE ?)");
        }
        sql.append(" ORDER BY nama_barang ASC");

        try (Connection conn = KoneksiDB.ambilKoneksi();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            if (!semuaKategori) {
                ps.setString(idx++, kategori);
            }
            if (!kataKunci.isEmpty()) {
                String pola = "%" + kataKunci.toLowerCase(Locale.ROOT) + "%";
                ps.setString(idx++, pola);
                ps.setString(idx, pola);
            }
            int jumlahBaris = 0;
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    jumlahBaris++;
                    model.addRow(new Object[]{
                        rs.getString("kode_barang"),
                        rs.getString("nama_barang"),
                        rs.getString("kategori"),
                        KoneksiDB.formatRupiah(rs.getLong("harga")),
                        rs.getInt("stok")
                    });
                }
            }
            if (jumlahBaris == 0) {
                lblStatusKatalog.setForeground(KoneksiDB.WARNA_BAHAYA);
                lblStatusKatalog.setText("Barang tidak ditemukan. Hapus kata pencarian atau pilih kategori Semua.");
            } else {
                lblStatusKatalog.setForeground(KoneksiDB.WARNA_TEKS_HIJAU);
                lblStatusKatalog.setText("Menampilkan " + jumlahBaris + " barang. Klik dua kali baris barang atau tekan + Tambah ke Keranjang.");
            }
        } catch (Exception ex) {
            lblStatusKatalog.setForeground(KoneksiDB.WARNA_BAHAYA);
            lblStatusKatalog.setText("Gagal memuat katalog barang: " + ex.getMessage());
        }
    }

    private int ambilStokDatabase(String kodeBarang) throws SQLException {
        try (Connection conn = KoneksiDB.ambilKoneksi();
             PreparedStatement ps = conn.prepareStatement("SELECT stok FROM barang WHERE kode_barang = ?")) {
            ps.setString(1, kodeBarang);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    private void masukkanBarangTerpilihKeKeranjang(int tambahanQty) {
        int baris = tblKatalog.getSelectedRow();
        if (baris < 0) {
            JOptionPane.showMessageDialog(this,
                    "Pilih salah satu barang pada tabel katalog di sebelah kiri terlebih dahulu.",
                    "Belum Ada Barang Dipilih", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String kode = String.valueOf(tblKatalog.getValueAt(baris, 0));
        String nama = String.valueOf(tblKatalog.getValueAt(baris, 1));
        long harga = KoneksiDB.parseNominalRupiah(String.valueOf(tblKatalog.getValueAt(baris, 3)));
        int stokTersedia = Integer.parseInt(String.valueOf(tblKatalog.getValueAt(baris, 4)));

        tambahAtauPerbaruiItemKeranjang(kode, nama, harga, tambahanQty, stokTersedia);
    }

    private void tambahAtauPerbaruiItemKeranjang(String kode, String nama, long harga, int tambahanQty, int stokTersedia) {
        if (stokTersedia <= 0) {
            JOptionPane.showMessageDialog(this,
                    "Stok barang \"" + nama + "\" sudah habis (0).",
                    "Stok Habis", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            for (String[] item : daftarKeranjang) {
                if (item[0].equalsIgnoreCase(kode)) {
                    int qtyLama = Integer.parseInt(item[3]);
                    int qtyBaru = hitungJumlahBaruKeranjang(qtyLama, tambahanQty, stokTersedia);
                    item[3] = String.valueOf(qtyBaru);
                    item[4] = String.valueOf(harga * qtyBaru);
                    perbaruiTampilanKeranjang();
                    return;
                }
            }
            int qtyAwal = hitungJumlahBaruKeranjang(0, tambahanQty, stokTersedia);
            daftarKeranjang.add(new String[]{
                kode, nama, String.valueOf(harga), String.valueOf(qtyAwal), String.valueOf(harga * qtyAwal)
            });
            perbaruiTampilanKeranjang();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Melebihi Sisa Stok", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void perbaruiTampilanKeranjang() {
        DefaultTableModel model = (DefaultTableModel) tblKeranjang.getModel();
        model.setRowCount(0);

        long subtotal = 0L;
        for (String[] item : daftarKeranjang) {
            long hargaSatuan = Long.parseLong(item[2]);
            int qty = Integer.parseInt(item[3]);
            long sub = Long.parseLong(item[4]);
            subtotal += sub;
            model.addRow(new Object[]{
                item[0],
                item[1],
                KoneksiDB.formatRupiah(hargaSatuan),
                qty,
                KoneksiDB.formatRupiah(sub)
            });
        }

        boolean adalahAnggota = rbAnggota.isSelected();
        long potongan = KoneksiDB.hitungPotonganAnggota(subtotal, adalahAnggota);
        long totalBayar = subtotal - potongan;

        lblSubtotalNilai.setText(KoneksiDB.formatRupiah(subtotal));
        lblPotonganJudul.setText("Potongan Anggota (" + KoneksiDB.PERSEN_DISKON_ANGGOTA + "%)");
        lblPotonganNilai.setText(potongan > 0 ? "-" + KoneksiDB.formatRupiah(potongan) : "Rp 0");
        lblTotalBayarNilai.setText(KoneksiDB.formatRupiah(totalBayar));

        boolean adaIsi = !daftarKeranjang.isEmpty();
        btnTambahQty.setEnabled(adaIsi);
        btnKurangQty.setEnabled(adaIsi);
        btnHapusItem.setEnabled(adaIsi);
        btnKosongkanKeranjang.setEnabled(adaIsi);
        btnProsesBayar.setEnabled(adaIsi);

        if (!adaIsi) {
            lblStatusKeranjang.setForeground(KoneksiDB.WARNA_TEKS_UTAMA);
            lblStatusKeranjang.setText("Keranjang masih kosong. Pilih barang di tabel kiri untuk memulai transaksi.");
        } else {
            lblStatusKeranjang.setForeground(KoneksiDB.WARNA_TEKS_HIJAU);
            lblStatusKeranjang.setText("Keranjang berisi " + daftarKeranjang.size() + " jenis barang.");
        }
    }

    private void bukaDialogPembayaran() {
        if (daftarKeranjang.isEmpty()) {
            return;
        }
        long subtotal = 0L;
        for (String[] item : daftarKeranjang) {
            subtotal += Long.parseLong(item[4]);
        }
        boolean adalahAnggota = rbAnggota.isSelected();
        String jenisPembeli = adalahAnggota ? "Anggota Kopdes" : "Umum";
        long potongan = KoneksiDB.hitungPotonganAnggota(subtotal, adalahAnggota);
        long totalBayar = subtotal - potongan;

        BayarDialog dialog = new BayarDialog(
                this, true, idPengguna, namaLengkap, jenisPembeli,
                daftarKeranjang, subtotal, potongan, totalBayar
        );
        dialog.setVisible(true);

        if (dialog.apakahTransaksiBerhasil()) {
            daftarKeranjang.clear();
            rbUmum.setSelected(true);
            spnJumlahBeli.setValue(1);
            perbaruiTampilanKeranjang();
            muatTabelKatalog();
            muatTabelStokBarang();
            muatTabelRiwayat();
            txtCariBarang.requestFocusInWindow();
        }
    }

    private void muatTabelStokBarang() {
        DefaultTableModel model = (DefaultTableModel) tblStokBarang.getModel();
        model.setRowCount(0);

        String sql = "SELECT kode_barang, nama_barang, kategori, harga, stok FROM barang ORDER BY kode_barang ASC";
        try (Connection conn = KoneksiDB.ambilKoneksi();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int stok = rs.getInt("stok");
                String statusStok = stok == 0 ? "Habis (0)" : (stok <= 5 ? "Stok Menipis" : "Tersedia");
                model.addRow(new Object[]{
                    rs.getString("kode_barang"),
                    rs.getString("nama_barang"),
                    rs.getString("kategori"),
                    KoneksiDB.formatRupiah(rs.getLong("harga")),
                    stok,
                    statusStok
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Gagal memuat tabel stok barang: " + ex.getMessage(),
                    "Galat Database", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void isiFormDariBarisStokTerpilih() {
        int baris = tblStokBarang.getSelectedRow();
        if (baris < 0) {
            return;
        }
        txtKodeBarang.setText(String.valueOf(tblStokBarang.getValueAt(baris, 0)));
        txtNamaBarang.setText(String.valueOf(tblStokBarang.getValueAt(baris, 1)));
        cmbKategoriBarang.setSelectedItem(String.valueOf(tblStokBarang.getValueAt(baris, 2)));
        long harga = KoneksiDB.parseNominalRupiah(String.valueOf(tblStokBarang.getValueAt(baris, 3)));
        txtHargaBarang.setText(String.valueOf(harga));
        txtStokBarang.setText(String.valueOf(tblStokBarang.getValueAt(baris, 4)));
    }

    private void bersihkanFormStok() {
        tblStokBarang.clearSelection();
        txtKodeBarang.setText("");
        txtNamaBarang.setText("");
        cmbKategoriBarang.setSelectedIndex(0);
        txtHargaBarang.setText("");
        txtStokBarang.setText("");
        if ("Admin".equalsIgnoreCase(peran)) {
            txtKodeBarang.requestFocusInWindow();
        }
    }

    private void muatTabelRiwayat() {
        DefaultTableModel model = (DefaultTableModel) tblRiwayat.getModel();
        model.setRowCount(0);
        ((DefaultTableModel) tblDetailRiwayat.getModel()).setRowCount(0);
        btnBukaStruk.setEnabled(false);

        boolean hanyaHariIni = cmbPeriodeRiwayat.getSelectedIndex() == 0;
        String cariNota = txtCariNota.getText().trim();

        StringBuilder sql = new StringBuilder("""
            SELECT t.no_nota, t.tanggal_waktu, p.nama_lengkap, t.jenis_pembeli, t.metode_bayar, t.total_bayar
            FROM transaksi t
            JOIN pengguna p ON t.id_pengguna = p.id_pengguna
            WHERE 1=1
            """);
        if (hanyaHariIni) {
            sql.append(" AND t.tanggal_waktu LIKE ?");
        }
        if (!cariNota.isEmpty()) {
            sql.append(" AND LOWER(t.no_nota) LIKE ?");
        }
        sql.append(" ORDER BY t.tanggal_waktu DESC");

        int jumlahNota = 0;
        long totalOmset = 0L;

        try (Connection conn = KoneksiDB.ambilKoneksi();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            if (hanyaHariIni) {
                ps.setString(idx++, LocalDate.now().toString() + "%");
            }
            if (!cariNota.isEmpty()) {
                ps.setString(idx, "%" + cariNota.toLowerCase(Locale.ROOT) + "%");
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    jumlahNota++;
                    long nilaiNota = rs.getLong("total_bayar");
                    totalOmset += nilaiNota;
                    model.addRow(new Object[]{
                        rs.getString("no_nota"),
                        rs.getString("tanggal_waktu"),
                        rs.getString("nama_lengkap"),
                        rs.getString("jenis_pembeli"),
                        rs.getString("metode_bayar"),
                        KoneksiDB.formatRupiah(nilaiNota)
                    });
                }
            }

            lblRingkasanRiwayat.setText("Jumlah Transaksi: " + jumlahNota
                    + " nota   |   Total Pemasukan: " + KoneksiDB.formatRupiah(totalOmset));

            if (jumlahNota == 0) {
                lblStatusRiwayat.setForeground(KoneksiDB.WARNA_TEKS_UTAMA);
                lblStatusRiwayat.setText("Belum ada transaksi pada filter ini. Ubah periode ke Semua Transaksi atau lakukan transaksi di Tab Kasir.");
            } else {
                lblStatusRiwayat.setForeground(KoneksiDB.WARNA_TEKS_HIJAU);
                lblStatusRiwayat.setText("Klik salah satu baris nota di bawah untuk melihat rincian barang atau membuka ulang struk.");
            }
        } catch (Exception ex) {
            lblStatusRiwayat.setForeground(KoneksiDB.WARNA_BAHAYA);
            lblStatusRiwayat.setText("Gagal memuat riwayat transaksi: " + ex.getMessage());
        }
    }

    private void muatDetailRiwayatTerpilih() {
        DefaultTableModel modelDetail = (DefaultTableModel) tblDetailRiwayat.getModel();
        modelDetail.setRowCount(0);

        int baris = tblRiwayat.getSelectedRow();
        if (baris < 0) {
            btnBukaStruk.setEnabled(false);
            return;
        }
        btnBukaStruk.setEnabled(true);
        String noNota = String.valueOf(tblRiwayat.getValueAt(baris, 0));

        String sql = "SELECT kode_barang, nama_barang, harga_satuan, jumlah, subtotal_item FROM detail_transaksi WHERE no_nota = ?";
        try (Connection conn = KoneksiDB.ambilKoneksi();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, noNota);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    modelDetail.addRow(new Object[]{
                        rs.getString("kode_barang"),
                        rs.getString("nama_barang"),
                        KoneksiDB.formatRupiah(rs.getLong("harga_satuan")),
                        rs.getInt("jumlah"),
                        KoneksiDB.formatRupiah(rs.getLong("subtotal_item"))
                    });
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Gagal memuat rincian nota: " + ex.getMessage(),
                    "Galat Database", JOptionPane.ERROR_MESSAGE);
        }
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        grupPembeli = new javax.swing.ButtonGroup();
        panelHeaderAtas = new javax.swing.JPanel();
        panelKiriHeader = new javax.swing.JPanel();
        lblLogoHeader = new javax.swing.JLabel();
        lblJudulHeader = new javax.swing.JLabel();
        panelKananHeader = new javax.swing.JPanel();
        lblInfoPetugas = new javax.swing.JLabel();
        lblJamDigital = new javax.swing.JLabel();
        btnLogout = new javax.swing.JButton();
        tabMenuUtama = new javax.swing.JTabbedPane();
        panelTabKasir = new javax.swing.JPanel();
        splitKasir = new javax.swing.JSplitPane();
        panelKiriKatalog = new javax.swing.JPanel();
        panelCariKatalog = new javax.swing.JPanel();
        lblCariBarang = new javax.swing.JLabel();
        txtCariBarang = new javax.swing.JTextField();
        cmbKategori = new javax.swing.JComboBox<>();
        scrollKatalog = new javax.swing.JScrollPane();
        tblKatalog = new javax.swing.JTable();
        panelBawahKatalog = new javax.swing.JPanel();
        lblStatusKatalog = new javax.swing.JLabel();
        lblJumlahBeli = new javax.swing.JLabel();
        spnJumlahBeli = new javax.swing.JSpinner();
        btnTambahKeranjang = new javax.swing.JButton();
        panelKananKeranjang = new javax.swing.JPanel();
        panelJenisPembeli = new javax.swing.JPanel();
        lblJenisPembeli = new javax.swing.JLabel();
        rbUmum = new javax.swing.JRadioButton();
        rbAnggota = new javax.swing.JRadioButton();
        scrollKeranjang = new javax.swing.JScrollPane();
        tblKeranjang = new javax.swing.JTable();
        lblStatusKeranjang = new javax.swing.JLabel();
        panelAksiKeranjang = new javax.swing.JPanel();
        btnTambahQty = new javax.swing.JButton();
        btnKurangQty = new javax.swing.JButton();
        btnHapusItem = new javax.swing.JButton();
        btnKosongkanKeranjang = new javax.swing.JButton();
        panelRingkasanTotal = new javax.swing.JPanel();
        lblSubtotalJudul = new javax.swing.JLabel();
        lblSubtotalNilai = new javax.swing.JLabel();
        lblPotonganJudul = new javax.swing.JLabel();
        lblPotonganNilai = new javax.swing.JLabel();
        lblTotalBayarJudul = new javax.swing.JLabel();
        lblTotalBayarNilai = new javax.swing.JLabel();
        btnProsesBayar = new javax.swing.JButton();
        panelTabStok = new javax.swing.JPanel();
        panelFormBarang = new javax.swing.JPanel();
        lblInfoAksesStok = new javax.swing.JLabel();
        lblKodeBarang = new javax.swing.JLabel();
        txtKodeBarang = new javax.swing.JTextField();
        lblNamaBarang = new javax.swing.JLabel();
        txtNamaBarang = new javax.swing.JTextField();
        lblKategoriBarang = new javax.swing.JLabel();
        cmbKategoriBarang = new javax.swing.JComboBox<>();
        lblHargaBarang = new javax.swing.JLabel();
        txtHargaBarang = new javax.swing.JTextField();
        lblStokBarang = new javax.swing.JLabel();
        txtStokBarang = new javax.swing.JTextField();
        panelTombolCrud = new javax.swing.JPanel();
        btnSimpanBarang = new javax.swing.JButton();
        btnUbahBarang = new javax.swing.JButton();
        btnHapusBarang = new javax.swing.JButton();
        btnBersihkanForm = new javax.swing.JButton();
        scrollStokBarang = new javax.swing.JScrollPane();
        tblStokBarang = new javax.swing.JTable();
        panelTabRiwayat = new javax.swing.JPanel();
        panelAtasRiwayat = new javax.swing.JPanel();
        lblFilterPeriode = new javax.swing.JLabel();
        cmbPeriodeRiwayat = new javax.swing.JComboBox<>();
        lblCariNota = new javax.swing.JLabel();
        txtCariNota = new javax.swing.JTextField();
        btnMuatUlangRiwayat = new javax.swing.JButton();
        lblRingkasanRiwayat = new javax.swing.JLabel();
        lblStatusRiwayat = new javax.swing.JLabel();
        splitRiwayat = new javax.swing.JSplitPane();
        scrollRiwayat = new javax.swing.JScrollPane();
        tblRiwayat = new javax.swing.JTable();
        panelBawahDetailRiwayat = new javax.swing.JPanel();
        scrollDetailRiwayat = new javax.swing.JScrollPane();
        tblDetailRiwayat = new javax.swing.JTable();
        btnBukaStruk = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Retail Kopdes");
        setMinimumSize(new java.awt.Dimension(1024, 680));
        setPreferredSize(new java.awt.Dimension(1220, 740));

        panelHeaderAtas.setBackground(new java.awt.Color(6, 95, 70));
        panelHeaderAtas.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 18, 10, 18));
        panelHeaderAtas.setLayout(new java.awt.BorderLayout(12, 0));

        panelKiriHeader.setOpaque(false);
        panelKiriHeader.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 2));

        lblLogoHeader.setFont(new java.awt.Font("SansSerif", 1, 16)); // NOI18N
        lblLogoHeader.setForeground(new java.awt.Color(255, 255, 255));
        lblLogoHeader.setText("[RK]");
        panelKiriHeader.add(lblLogoHeader);

        lblJudulHeader.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        lblJudulHeader.setForeground(new java.awt.Color(255, 255, 255));
        lblJudulHeader.setText("RETAIL KOPDES - Kasir Koperasi Desa");
        panelKiriHeader.add(lblJudulHeader);

        panelHeaderAtas.add(panelKiriHeader, java.awt.BorderLayout.WEST);

        panelKananHeader.setOpaque(false);
        panelKananHeader.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 14, 2));

        lblInfoPetugas.setFont(new java.awt.Font("SansSerif", 1, 13)); // NOI18N
        lblInfoPetugas.setForeground(new java.awt.Color(236, 253, 245));
        lblInfoPetugas.setText("Petugas: -");
        panelKananHeader.add(lblInfoPetugas);

        lblJamDigital.setFont(new java.awt.Font("SansSerif", 0, 13)); // NOI18N
        lblJamDigital.setForeground(new java.awt.Color(236, 253, 245));
        lblJamDigital.setText("00:00:00");
        panelKananHeader.add(lblJamDigital);

        btnLogout.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        btnLogout.setText("Ganti Petugas");
        btnLogout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLogoutActionPerformed(evt);
            }
        });
        panelKananHeader.add(btnLogout);

        panelHeaderAtas.add(panelKananHeader, java.awt.BorderLayout.EAST);

        getContentPane().add(panelHeaderAtas, java.awt.BorderLayout.NORTH);

        tabMenuUtama.setFont(new java.awt.Font("SansSerif", 1, 13)); // NOI18N

        panelTabKasir.setBackground(new java.awt.Color(240, 253, 244));
        panelTabKasir.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 12, 12, 12));
        panelTabKasir.setLayout(new java.awt.BorderLayout());

        splitKasir.setDividerLocation(640);
        splitKasir.setResizeWeight(0.55);

        panelKiriKatalog.setBackground(new java.awt.Color(255, 255, 255));
        panelKiriKatalog.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 12, 12, 12));
        panelKiriKatalog.setLayout(new java.awt.BorderLayout(0, 10));

        panelCariKatalog.setBackground(new java.awt.Color(255, 255, 255));
        panelCariKatalog.setLayout(new java.awt.BorderLayout(8, 0));

        lblCariBarang.setFont(new java.awt.Font("SansSerif", 1, 13)); // NOI18N
        lblCariBarang.setText("Cari Barang (F2):");
        panelCariKatalog.add(lblCariBarang, java.awt.BorderLayout.WEST);

        txtCariBarang.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        txtCariBarang.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtCariBarangActionPerformed(evt);
            }
        });
        panelCariKatalog.add(txtCariBarang, java.awt.BorderLayout.CENTER);

        cmbKategori.setFont(new java.awt.Font("SansSerif", 0, 13)); // NOI18N
        cmbKategori.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Semua", "Sembako", "Minuman", "Kebutuhan Rumah", "Lainnya" }));
        cmbKategori.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbKategoriActionPerformed(evt);
            }
        });
        panelCariKatalog.add(cmbKategori, java.awt.BorderLayout.EAST);

        panelKiriKatalog.add(panelCariKatalog, java.awt.BorderLayout.NORTH);

        tblKatalog.setRowHeight(28);
        scrollKatalog.setViewportView(tblKatalog);

        panelKiriKatalog.add(scrollKatalog, java.awt.BorderLayout.CENTER);

        panelBawahKatalog.setBackground(new java.awt.Color(255, 255, 255));
        panelBawahKatalog.setLayout(new java.awt.BorderLayout(8, 6));

        lblStatusKatalog.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        lblStatusKatalog.setText("Daftar barang siap dipilih.");
        panelBawahKatalog.add(lblStatusKatalog, java.awt.BorderLayout.NORTH);

        lblJumlahBeli.setFont(new java.awt.Font("SansSerif", 1, 13)); // NOI18N
        lblJumlahBeli.setText("Jumlah Beli:");
        panelBawahKatalog.add(lblJumlahBeli, java.awt.BorderLayout.WEST);
        panelBawahKatalog.add(spnJumlahBeli, java.awt.BorderLayout.CENTER);

        btnTambahKeranjang.setBackground(new java.awt.Color(6, 95, 70));
        btnTambahKeranjang.setFont(new java.awt.Font("SansSerif", 1, 13)); // NOI18N
        btnTambahKeranjang.setForeground(new java.awt.Color(255, 255, 255));
        btnTambahKeranjang.setText("+ Tambah ke Keranjang");
        btnTambahKeranjang.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTambahKeranjangActionPerformed(evt);
            }
        });
        panelBawahKatalog.add(btnTambahKeranjang, java.awt.BorderLayout.EAST);

        panelKiriKatalog.add(panelBawahKatalog, java.awt.BorderLayout.SOUTH);

        splitKasir.setLeftComponent(panelKiriKatalog);

        panelKananKeranjang.setBackground(new java.awt.Color(255, 255, 255));

        panelJenisPembeli.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        lblJenisPembeli.setText("Pembeli:");
        panelJenisPembeli.add(lblJenisPembeli);

        grupPembeli.add(rbUmum);
        rbUmum.setSelected(true);
        rbUmum.setText("Umum");
        rbUmum.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rbUmumActionPerformed(evt);
            }
        });
        panelJenisPembeli.add(rbUmum);

        grupPembeli.add(rbAnggota);
        rbAnggota.setText("Anggota Kopdes (Diskon 5%)");
        rbAnggota.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rbAnggotaActionPerformed(evt);
            }
        });
        panelJenisPembeli.add(rbAnggota);

        tblKeranjang.setRowHeight(28);
        scrollKeranjang.setViewportView(tblKeranjang);

        lblStatusKeranjang.setText("Keranjang masih kosong.");

        panelAksiKeranjang.setLayout(new java.awt.GridLayout(1, 4, 6, 0));

        btnTambahQty.setText("+ Qty");
        btnTambahQty.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTambahQtyActionPerformed(evt);
            }
        });
        panelAksiKeranjang.add(btnTambahQty);

        btnKurangQty.setText("- Qty");
        btnKurangQty.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnKurangQtyActionPerformed(evt);
            }
        });
        panelAksiKeranjang.add(btnKurangQty);

        btnHapusItem.setText("Hapus Baris");
        btnHapusItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnHapusItemActionPerformed(evt);
            }
        });
        panelAksiKeranjang.add(btnHapusItem);

        btnKosongkanKeranjang.setText("Kosongkan");
        btnKosongkanKeranjang.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnKosongkanKeranjangActionPerformed(evt);
            }
        });
        panelAksiKeranjang.add(btnKosongkanKeranjang);

        lblSubtotalJudul.setText("Subtotal Belanja");

        lblSubtotalNilai.setText("Rp 0");

        lblPotonganJudul.setText("Potongan Anggota (5%)");

        lblPotonganNilai.setText("Rp 0");

        lblTotalBayarJudul.setText("TOTAL BAYAR");

        lblTotalBayarNilai.setText("Rp 0");

        javax.swing.GroupLayout panelRingkasanTotalLayout = new javax.swing.GroupLayout(panelRingkasanTotal);
        panelRingkasanTotal.setLayout(panelRingkasanTotalLayout);
        panelRingkasanTotalLayout.setHorizontalGroup(
            panelRingkasanTotalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelRingkasanTotalLayout.createSequentialGroup()
                .addComponent(lblSubtotalJudul)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(lblSubtotalNilai))
            .addGroup(panelRingkasanTotalLayout.createSequentialGroup()
                .addComponent(lblPotonganJudul)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(lblPotonganNilai))
            .addGroup(panelRingkasanTotalLayout.createSequentialGroup()
                .addComponent(lblTotalBayarJudul)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(lblTotalBayarNilai))
        );
        panelRingkasanTotalLayout.setVerticalGroup(
            panelRingkasanTotalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelRingkasanTotalLayout.createSequentialGroup()
                .addGroup(panelRingkasanTotalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblSubtotalJudul)
                    .addComponent(lblSubtotalNilai))
                .addGap(4, 4, 4)
                .addGroup(panelRingkasanTotalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblPotonganJudul)
                    .addComponent(lblPotonganNilai))
                .addGap(8, 8, 8)
                .addGroup(panelRingkasanTotalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblTotalBayarJudul)
                    .addComponent(lblTotalBayarNilai)))
        );

        btnProsesBayar.setBackground(new java.awt.Color(194, 65, 12));
        btnProsesBayar.setForeground(new java.awt.Color(255, 255, 255));
        btnProsesBayar.setText("PROSES BAYAR (F8)");
        btnProsesBayar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnProsesBayarActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelKananKeranjangLayout = new javax.swing.GroupLayout(panelKananKeranjang);
        panelKananKeranjang.setLayout(panelKananKeranjangLayout);
        panelKananKeranjangLayout.setHorizontalGroup(
            panelKananKeranjangLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelJenisPembeli, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(scrollKeranjang, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
            .addComponent(lblStatusKeranjang, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(panelAksiKeranjang, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(panelRingkasanTotal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(btnProsesBayar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        panelKananKeranjangLayout.setVerticalGroup(
            panelKananKeranjangLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelKananKeranjangLayout.createSequentialGroup()
                .addComponent(panelJenisPembeli, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(scrollKeranjang, javax.swing.GroupLayout.DEFAULT_SIZE, 250, Short.MAX_VALUE)
                .addGap(6, 6, 6)
                .addComponent(lblStatusKeranjang)
                .addGap(6, 6, 6)
                .addComponent(panelAksiKeranjang, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(panelRingkasanTotal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(btnProsesBayar, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        splitKasir.setRightComponent(panelKananKeranjang);

        panelTabKasir.add(splitKasir, java.awt.BorderLayout.CENTER);

        tabMenuUtama.addTab("1. Transaksi Kasir", panelTabKasir);

        panelTabStok.setLayout(new java.awt.BorderLayout(0, 12));

        lblInfoAksesStok.setText("Kelola Data Stok Barang");

        lblKodeBarang.setText("Kode Barang");

        lblNamaBarang.setText("Nama Barang");

        lblKategoriBarang.setText("Kategori");

        cmbKategoriBarang.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Sembako", "Minuman", "Kebutuhan Rumah", "Lainnya" }));

        lblHargaBarang.setText("Harga Jual (Rp)");

        lblStokBarang.setText("Jumlah Stok");

        panelTombolCrud.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 0));

        btnSimpanBarang.setText("Simpan Baru");
        btnSimpanBarang.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSimpanBarangActionPerformed(evt);
            }
        });
        panelTombolCrud.add(btnSimpanBarang);

        btnUbahBarang.setText("Perbarui Data");
        btnUbahBarang.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUbahBarangActionPerformed(evt);
            }
        });
        panelTombolCrud.add(btnUbahBarang);

        btnHapusBarang.setText("Hapus Barang");
        btnHapusBarang.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnHapusBarangActionPerformed(evt);
            }
        });
        panelTombolCrud.add(btnHapusBarang);

        btnBersihkanForm.setText("Bersihkan Form");
        btnBersihkanForm.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBersihkanFormActionPerformed(evt);
            }
        });
        panelTombolCrud.add(btnBersihkanForm);

        javax.swing.GroupLayout panelFormBarangLayout = new javax.swing.GroupLayout(panelFormBarang);
        panelFormBarang.setLayout(panelFormBarangLayout);
        panelFormBarangLayout.setHorizontalGroup(
            panelFormBarangLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblInfoAksesStok, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(panelFormBarangLayout.createSequentialGroup()
                .addGroup(panelFormBarangLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblKodeBarang)
                    .addComponent(txtKodeBarang, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12)
                .addGroup(panelFormBarangLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblNamaBarang)
                    .addComponent(txtNamaBarang))
                .addGap(12, 12, 12)
                .addGroup(panelFormBarangLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblKategoriBarang)
                    .addComponent(cmbKategoriBarang, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12)
                .addGroup(panelFormBarangLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblHargaBarang)
                    .addComponent(txtHargaBarang, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12)
                .addGroup(panelFormBarangLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblStokBarang)
                    .addComponent(txtStokBarang, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addComponent(panelTombolCrud, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        panelFormBarangLayout.setVerticalGroup(
            panelFormBarangLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelFormBarangLayout.createSequentialGroup()
                .addComponent(lblInfoAksesStok)
                .addGap(10, 10, 10)
                .addGroup(panelFormBarangLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblKodeBarang)
                    .addComponent(lblNamaBarang)
                    .addComponent(lblKategoriBarang)
                    .addComponent(lblHargaBarang)
                    .addComponent(lblStokBarang))
                .addGap(4, 4, 4)
                .addGroup(panelFormBarangLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtKodeBarang, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtNamaBarang, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbKategoriBarang, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtHargaBarang, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtStokBarang, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12)
                .addComponent(panelTombolCrud, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        panelTabStok.add(panelFormBarang, java.awt.BorderLayout.NORTH);

        scrollStokBarang.setViewportView(tblStokBarang);

        panelTabStok.add(scrollStokBarang, java.awt.BorderLayout.CENTER);

        tabMenuUtama.addTab("2. Kelola Data Barang", panelTabStok);

        panelTabRiwayat.setLayout(new java.awt.BorderLayout(0, 10));

        lblFilterPeriode.setText("Periode:");

        cmbPeriodeRiwayat.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Hari Ini", "Semua Transaksi" }));
        cmbPeriodeRiwayat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbPeriodeRiwayatActionPerformed(evt);
            }
        });

        lblCariNota.setText("No. Nota:");

        btnMuatUlangRiwayat.setText("Tampilkan");
        btnMuatUlangRiwayat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMuatUlangRiwayatActionPerformed(evt);
            }
        });

        lblRingkasanRiwayat.setText("Jumlah Transaksi: 0 nota   |   Total Pemasukan: Rp 0");

        lblStatusRiwayat.setText("Pilih baris nota untuk melihat rincian barang.");

        javax.swing.GroupLayout panelAtasRiwayatLayout = new javax.swing.GroupLayout(panelAtasRiwayat);
        panelAtasRiwayat.setLayout(panelAtasRiwayatLayout);
        panelAtasRiwayatLayout.setHorizontalGroup(
            panelAtasRiwayatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelAtasRiwayatLayout.createSequentialGroup()
                .addComponent(lblFilterPeriode)
                .addGap(8, 8, 8)
                .addComponent(cmbPeriodeRiwayat, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16)
                .addComponent(lblCariNota)
                .addGap(8, 8, 8)
                .addComponent(txtCariNota, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(btnMuatUlangRiwayat, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(lblRingkasanRiwayat))
            .addComponent(lblStatusRiwayat, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        panelAtasRiwayatLayout.setVerticalGroup(
            panelAtasRiwayatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelAtasRiwayatLayout.createSequentialGroup()
                .addGroup(panelAtasRiwayatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblFilterPeriode)
                    .addComponent(cmbPeriodeRiwayat, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblCariNota)
                    .addComponent(txtCariNota, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnMuatUlangRiwayat, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblRingkasanRiwayat))
                .addGap(8, 8, 8)
                .addComponent(lblStatusRiwayat))
        );

        panelTabRiwayat.add(panelAtasRiwayat, java.awt.BorderLayout.NORTH);

        splitRiwayat.setDividerLocation(260);
        splitRiwayat.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        scrollRiwayat.setViewportView(tblRiwayat);

        splitRiwayat.setTopComponent(scrollRiwayat);

        panelBawahDetailRiwayat.setLayout(new java.awt.BorderLayout(0, 8));

        scrollDetailRiwayat.setViewportView(tblDetailRiwayat);

        panelBawahDetailRiwayat.add(scrollDetailRiwayat, java.awt.BorderLayout.CENTER);

        btnBukaStruk.setText("Buka & Cetak Ulang Struk");
        btnBukaStruk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBukaStrukActionPerformed(evt);
            }
        });
        panelBawahDetailRiwayat.add(btnBukaStruk, java.awt.BorderLayout.SOUTH);

        splitRiwayat.setBottomComponent(panelBawahDetailRiwayat);

        panelTabRiwayat.add(splitRiwayat, java.awt.BorderLayout.CENTER);

        tabMenuUtama.addTab("3. Riwayat Penjualan", panelTabRiwayat);

        getContentPane().add(tabMenuUtama, java.awt.BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void btnLogoutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLogoutActionPerformed
        int jawab = JOptionPane.showConfirmDialog(this,
                "Keluar dari sesi petugas \"" + namaLengkap + "\" dan kembali ke halaman masuk?",
                "Konfirmasi Ganti Petugas", JOptionPane.YES_NO_OPTION);
        if (jawab == JOptionPane.YES_OPTION) {
            if (timerJam != null) {
                timerJam.stop();
            }
            new LoginForm().setVisible(true);
            dispose();
        }
    }//GEN-LAST:event_btnLogoutActionPerformed

    private void txtCariBarangActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtCariBarangActionPerformed
        String ketikan = txtCariBarang.getText().trim();
        if (ketikan.isEmpty()) {
            return;
        }
        try (Connection conn = KoneksiDB.ambilKoneksi();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT kode_barang, nama_barang, harga, stok FROM barang WHERE LOWER(kode_barang) = ?")) {
            ps.setString(1, ketikan.toLowerCase(Locale.ROOT));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int qty = (Integer) spnJumlahBeli.getValue();
                    tambahAtauPerbaruiItemKeranjang(
                            rs.getString("kode_barang"),
                            rs.getString("nama_barang"),
                            rs.getLong("harga"),
                            qty,
                            rs.getInt("stok")
                    );
                    txtCariBarang.setText("");
                    spnJumlahBeli.setValue(1);
                    return;
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Gagal mencari kode barang: " + ex.getMessage(),
                    "Galat Pencarian", JOptionPane.ERROR_MESSAGE);
        }
        if (tblKatalog.getRowCount() == 1) {
            tblKatalog.setRowSelectionInterval(0, 0);
            masukkanBarangTerpilihKeKeranjang((Integer) spnJumlahBeli.getValue());
            txtCariBarang.setText("");
            spnJumlahBeli.setValue(1);
        }
    }//GEN-LAST:event_txtCariBarangActionPerformed

    private void cmbKategoriActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbKategoriActionPerformed
        muatTabelKatalog();
    }//GEN-LAST:event_cmbKategoriActionPerformed

    private void btnTambahKeranjangActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTambahKeranjangActionPerformed
        int qty = (Integer) spnJumlahBeli.getValue();
        masukkanBarangTerpilihKeKeranjang(qty);
        spnJumlahBeli.setValue(1);
    }//GEN-LAST:event_btnTambahKeranjangActionPerformed

    private void rbUmumActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbUmumActionPerformed
        perbaruiTampilanKeranjang();
    }//GEN-LAST:event_rbUmumActionPerformed

    private void rbAnggotaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbAnggotaActionPerformed
        perbaruiTampilanKeranjang();
    }//GEN-LAST:event_rbAnggotaActionPerformed

    private void btnTambahQtyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTambahQtyActionPerformed
        int baris = tblKeranjang.getSelectedRow();
        if (baris < 0) {
            JOptionPane.showMessageDialog(this, "Pilih baris barang di keranjang terlebih dahulu.");
            return;
        }
        try {
            String[] item = daftarKeranjang.get(baris);
            int stokDb = ambilStokDatabase(item[0]);
            int qtyBaru = hitungJumlahBaruKeranjang(Integer.parseInt(item[3]), 1, stokDb);
            long harga = Long.parseLong(item[2]);
            item[3] = String.valueOf(qtyBaru);
            item[4] = String.valueOf(harga * qtyBaru);
            perbaruiTampilanKeranjang();
            tblKeranjang.setRowSelectionInterval(baris, baris);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Batas Stok", JOptionPane.WARNING_MESSAGE);
        }
    }//GEN-LAST:event_btnTambahQtyActionPerformed

    private void btnKurangQtyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnKurangQtyActionPerformed
        int baris = tblKeranjang.getSelectedRow();
        if (baris < 0) {
            JOptionPane.showMessageDialog(this, "Pilih baris barang di keranjang terlebih dahulu.");
            return;
        }
        String[] item = daftarKeranjang.get(baris);
        int qtySekarang = Integer.parseInt(item[3]);
        if (qtySekarang <= 1) {
            daftarKeranjang.remove(baris);
            perbaruiTampilanKeranjang();
        } else {
            int qtyBaru = qtySekarang - 1;
            long harga = Long.parseLong(item[2]);
            item[3] = String.valueOf(qtyBaru);
            item[4] = String.valueOf(harga * qtyBaru);
            perbaruiTampilanKeranjang();
            tblKeranjang.setRowSelectionInterval(baris, baris);
        }
    }//GEN-LAST:event_btnKurangQtyActionPerformed

    private void btnHapusItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnHapusItemActionPerformed
        int baris = tblKeranjang.getSelectedRow();
        if (baris < 0) {
            JOptionPane.showMessageDialog(this, "Pilih baris barang di keranjang yang ingin dihapus.");
            return;
        }
        daftarKeranjang.remove(baris);
        perbaruiTampilanKeranjang();
    }//GEN-LAST:event_btnHapusItemActionPerformed

    private void btnKosongkanKeranjangActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnKosongkanKeranjangActionPerformed
        if (daftarKeranjang.isEmpty()) {
            return;
        }
        int konfirmasi = JOptionPane.showConfirmDialog(this,
                "Kosongkan seluruh item di keranjang belanja?",
                "Konfirmasi Kosongkan", JOptionPane.YES_NO_OPTION);
        if (konfirmasi == JOptionPane.YES_OPTION) {
            daftarKeranjang.clear();
            perbaruiTampilanKeranjang();
        }
    }//GEN-LAST:event_btnKosongkanKeranjangActionPerformed

    private void btnProsesBayarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnProsesBayarActionPerformed
        bukaDialogPembayaran();
    }//GEN-LAST:event_btnProsesBayarActionPerformed

    private void btnSimpanBarangActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSimpanBarangActionPerformed
        try {
            long harga = KoneksiDB.parseNominalRupiah(txtHargaBarang.getText());
            if (harga < 0) {
                throw new IllegalArgumentException("Harga jual harus diisi angka Rupiah yang benar.");
            }
            int stok = Integer.parseInt(txtStokBarang.getText().trim());
            simpanBarangBaru(
                    txtKodeBarang.getText(),
                    txtNamaBarang.getText(),
                    String.valueOf(cmbKategoriBarang.getSelectedItem()),
                    harga,
                    stok
            );
            muatTabelStokBarang();
            muatTabelKatalog();
            bersihkanFormStok();
            JOptionPane.showMessageDialog(this, "Data barang baru berhasil disimpan ke database.");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Jumlah stok harus diisi angka bulat (contoh: 25).",
                    "Input Belum Tepat", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Gagal Menyimpan", JOptionPane.WARNING_MESSAGE);
        }
    }//GEN-LAST:event_btnSimpanBarangActionPerformed

    private void btnUbahBarangActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUbahBarangActionPerformed
        try {
            long harga = KoneksiDB.parseNominalRupiah(txtHargaBarang.getText());
            if (harga < 0) {
                throw new IllegalArgumentException("Harga jual harus diisi angka Rupiah yang benar.");
            }
            int stok = Integer.parseInt(txtStokBarang.getText().trim());
            ubahDataBarang(
                    txtKodeBarang.getText(),
                    txtNamaBarang.getText(),
                    String.valueOf(cmbKategoriBarang.getSelectedItem()),
                    harga,
                    stok
            );
            muatTabelStokBarang();
            muatTabelKatalog();
            bersihkanFormStok();
            JOptionPane.showMessageDialog(this, "Data barang berhasil diperbarui.");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Jumlah stok harus diisi angka bulat (contoh: 25).",
                    "Input Belum Tepat", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Gagal Memperbarui", JOptionPane.WARNING_MESSAGE);
        }
    }//GEN-LAST:event_btnUbahBarangActionPerformed

    private void btnHapusBarangActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnHapusBarangActionPerformed
        String kode = txtKodeBarang.getText().trim();
        if (kode.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Klik salah satu barang pada tabel stok terlebih dahulu.");
            return;
        }
        int jawab = JOptionPane.showConfirmDialog(this,
                "Hapus barang berkode \"" + kode + "\" dari database?",
                "Konfirmasi Hapus Barang", JOptionPane.YES_NO_OPTION);
        if (jawab != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            hapusDataBarang(kode);
            muatTabelStokBarang();
            muatTabelKatalog();
            bersihkanFormStok();
            JOptionPane.showMessageDialog(this, "Barang berhasil dihapus dari database.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Tidak Dapat Menghapus", JOptionPane.WARNING_MESSAGE);
        }
    }//GEN-LAST:event_btnHapusBarangActionPerformed

    private void btnBersihkanFormActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBersihkanFormActionPerformed
        bersihkanFormStok();
    }//GEN-LAST:event_btnBersihkanFormActionPerformed

    private void cmbPeriodeRiwayatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbPeriodeRiwayatActionPerformed
        muatTabelRiwayat();
    }//GEN-LAST:event_cmbPeriodeRiwayatActionPerformed

    private void btnMuatUlangRiwayatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMuatUlangRiwayatActionPerformed
        muatTabelRiwayat();
    }//GEN-LAST:event_btnMuatUlangRiwayatActionPerformed

    private void btnBukaStrukActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBukaStrukActionPerformed
        int baris = tblRiwayat.getSelectedRow();
        if (baris < 0) {
            return;
        }
        String noNota = String.valueOf(tblRiwayat.getValueAt(baris, 0));

        String sqlNota = """
            SELECT t.no_nota, t.tanggal_waktu, p.nama_lengkap, t.jenis_pembeli,
                   t.subtotal, t.potongan_anggota, t.total_bayar, t.metode_bayar, t.uang_bayar, t.kembalian
            FROM transaksi t
            JOIN pengguna p ON t.id_pengguna = p.id_pengguna
            WHERE t.no_nota = ?
            """;
        String sqlDetail = "SELECT kode_barang, nama_barang, harga_satuan, jumlah, subtotal_item FROM detail_transaksi WHERE no_nota = ?";

        try (Connection conn = KoneksiDB.ambilKoneksi();
             PreparedStatement psNota = conn.prepareStatement(sqlNota);
             PreparedStatement psDetail = conn.prepareStatement(sqlDetail)) {

            psDetail.setString(1, noNota);
            List<String[]> rincian = new ArrayList<>();
            try (ResultSet rsD = psDetail.executeQuery()) {
                while (rsD.next()) {
                    rincian.add(new String[]{
                        rsD.getString("kode_barang"),
                        rsD.getString("nama_barang"),
                        String.valueOf(rsD.getLong("harga_satuan")),
                        String.valueOf(rsD.getInt("jumlah")),
                        String.valueOf(rsD.getLong("subtotal_item"))
                    });
                }
            }

            psNota.setString(1, noNota);
            try (ResultSet rsN = psNota.executeQuery()) {
                if (rsN.next()) {
                    String teksStruk = StrukDialog.susunTeksStruk(
                            rsN.getString("no_nota"),
                            rsN.getString("tanggal_waktu"),
                            rsN.getString("nama_lengkap"),
                            rsN.getString("jenis_pembeli"),
                            rincian,
                            rsN.getLong("subtotal"),
                            rsN.getLong("potongan_anggota"),
                            rsN.getLong("total_bayar"),
                            rsN.getString("metode_bayar"),
                            rsN.getLong("uang_bayar"),
                            rsN.getLong("kembalian")
                    );
                    new StrukDialog(this, true, teksStruk).setVisible(true);
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Gagal membuka struk transaksi: " + ex.getMessage(),
                    "Galat Struk", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnBukaStrukActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBersihkanForm;
    private javax.swing.JButton btnBukaStruk;
    private javax.swing.JButton btnHapusBarang;
    private javax.swing.JButton btnHapusItem;
    private javax.swing.JButton btnKosongkanKeranjang;
    private javax.swing.JButton btnKurangQty;
    private javax.swing.JButton btnLogout;
    private javax.swing.JButton btnMuatUlangRiwayat;
    private javax.swing.JButton btnProsesBayar;
    private javax.swing.JButton btnSimpanBarang;
    private javax.swing.JButton btnTambahKeranjang;
    private javax.swing.JButton btnTambahQty;
    private javax.swing.JButton btnUbahBarang;
    private javax.swing.JComboBox<String> cmbKategori;
    private javax.swing.JComboBox<String> cmbKategoriBarang;
    private javax.swing.JComboBox<String> cmbPeriodeRiwayat;
    private javax.swing.ButtonGroup grupPembeli;
    private javax.swing.JLabel lblCariBarang;
    private javax.swing.JLabel lblCariNota;
    private javax.swing.JLabel lblFilterPeriode;
    private javax.swing.JLabel lblHargaBarang;
    private javax.swing.JLabel lblInfoAksesStok;
    private javax.swing.JLabel lblInfoPetugas;
    private javax.swing.JLabel lblJamDigital;
    private javax.swing.JLabel lblJenisPembeli;
    private javax.swing.JLabel lblJudulHeader;
    private javax.swing.JLabel lblJumlahBeli;
    private javax.swing.JLabel lblKategoriBarang;
    private javax.swing.JLabel lblKodeBarang;
    private javax.swing.JLabel lblLogoHeader;
    private javax.swing.JLabel lblNamaBarang;
    private javax.swing.JLabel lblPotonganJudul;
    private javax.swing.JLabel lblPotonganNilai;
    private javax.swing.JLabel lblRingkasanRiwayat;
    private javax.swing.JLabel lblStatusKatalog;
    private javax.swing.JLabel lblStatusKeranjang;
    private javax.swing.JLabel lblStatusRiwayat;
    private javax.swing.JLabel lblStokBarang;
    private javax.swing.JLabel lblSubtotalJudul;
    private javax.swing.JLabel lblSubtotalNilai;
    private javax.swing.JLabel lblTotalBayarJudul;
    private javax.swing.JLabel lblTotalBayarNilai;
    private javax.swing.JPanel panelAksiKeranjang;
    private javax.swing.JPanel panelAtasRiwayat;
    private javax.swing.JPanel panelBawahDetailRiwayat;
    private javax.swing.JPanel panelBawahKatalog;
    private javax.swing.JPanel panelCariKatalog;
    private javax.swing.JPanel panelFormBarang;
    private javax.swing.JPanel panelHeaderAtas;
    private javax.swing.JPanel panelJenisPembeli;
    private javax.swing.JPanel panelKananHeader;
    private javax.swing.JPanel panelKananKeranjang;
    private javax.swing.JPanel panelKiriHeader;
    private javax.swing.JPanel panelKiriKatalog;
    private javax.swing.JPanel panelRingkasanTotal;
    private javax.swing.JPanel panelTabKasir;
    private javax.swing.JPanel panelTabRiwayat;
    private javax.swing.JPanel panelTabStok;
    private javax.swing.JPanel panelTombolCrud;
    private javax.swing.JRadioButton rbAnggota;
    private javax.swing.JRadioButton rbUmum;
    private javax.swing.JScrollPane scrollDetailRiwayat;
    private javax.swing.JScrollPane scrollKatalog;
    private javax.swing.JScrollPane scrollKeranjang;
    private javax.swing.JScrollPane scrollRiwayat;
    private javax.swing.JScrollPane scrollStokBarang;
    private javax.swing.JSplitPane splitKasir;
    private javax.swing.JSplitPane splitRiwayat;
    private javax.swing.JSpinner spnJumlahBeli;
    private javax.swing.JTabbedPane tabMenuUtama;
    private javax.swing.JTable tblDetailRiwayat;
    private javax.swing.JTable tblKatalog;
    private javax.swing.JTable tblKeranjang;
    private javax.swing.JTable tblRiwayat;
    private javax.swing.JTable tblStokBarang;
    private javax.swing.JTextField txtCariBarang;
    private javax.swing.JTextField txtCariNota;
    private javax.swing.JTextField txtHargaBarang;
    private javax.swing.JTextField txtKodeBarang;
    private javax.swing.JTextField txtNamaBarang;
    private javax.swing.JTextField txtStokBarang;
    // End of variables declaration//GEN-END:variables
}
