package com.kopdes;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.Cursor;
import java.awt.Frame;
import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class BayarDialog extends javax.swing.JDialog {

    private final Frame indukFrame;
    private final int idPengguna;
    private final String namaPetugas;
    private final String jenisPembeli;
    private final List<String[]> itemKeranjang;
    private final long subtotal;
    private final long potonganAnggota;
    private final long totalBayar;
    private boolean transaksiBerhasil = false;

    public BayarDialog(
            Frame parent,
            boolean modal,
            int idPengguna,
            String namaPetugas,
            String jenisPembeli,
            List<String[]> itemKeranjang,
            long subtotal,
            long potonganAnggota,
            long totalBayar) {
        super(parent, modal);
        this.indukFrame = parent;
        this.idPengguna = idPengguna;
        this.namaPetugas = namaPetugas;
        this.jenisPembeli = jenisPembeli;
        this.itemKeranjang = itemKeranjang;
        this.subtotal = subtotal;
        this.potonganAnggota = potonganAnggota;
        this.totalBayar = totalBayar;

        initComponents();
        setLocationRelativeTo(parent);
        siapkanTampilanAwal();
    }

    public boolean apakahTransaksiBerhasil() {
        return transaksiBerhasil;
    }

    private void siapkanTampilanAwal() {
        panelHeader.setBackground(KoneksiDB.WARNA_HEADER);
        lblJudulBayar.setForeground(KoneksiDB.WARNA_TEKS_PUTIH);

        panelIsi.setBackground(KoneksiDB.WARNA_LATAR);
        panelRingkasanTagihan.setBackground(KoneksiDB.WARNA_RINGKASAN);
        lblInfoPembeli.setForeground(KoneksiDB.WARNA_TEKS_HIJAU);
        lblTotalTagihanJudul.setForeground(KoneksiDB.WARNA_TEKS_HIJAU);
        lblNilaiTotalTagihan.setForeground(KoneksiDB.WARNA_TEKS_HIJAU);

        panelFormBayar.setBackground(KoneksiDB.WARNA_KARTU);
        panelUangCepat.setBackground(KoneksiDB.WARNA_KARTU);
        panelBawah.setBackground(KoneksiDB.WARNA_LATAR);

        lblMetodeBayar.setLabelFor(cmbMetodeBayar);
        lblUangDiterima.setLabelFor(txtUangDiterima);

        lblInfoPembeli.setText("Kategori Pembeli: " + jenisPembeli
                + (potonganAnggota > 0 ? " (Hemat " + KoneksiDB.formatRupiah(potonganAnggota) + ")" : ""));
        lblNilaiTotalTagihan.setText(KoneksiDB.formatRupiah(totalBayar));

        Cursor tangan = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
        cmbMetodeBayar.setCursor(tangan);
        btnUangPas.setCursor(tangan);
        btn20Ribu.setCursor(tangan);
        btn50Ribu.setCursor(tangan);
        btn100Ribu.setCursor(tangan);
        btnBatal.setCursor(tangan);

        btnSelesaikan.setBackground(KoneksiDB.WARNA_TOMBOL_BAYAR);
        btnSelesaikan.setForeground(KoneksiDB.WARNA_TEKS_PUTIH);
        btnSelesaikan.setCursor(tangan);

        txtUangDiterima.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                hitungKembalianOtomatis();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                hitungKembalianOtomatis();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                hitungKembalianOtomatis();
            }
        });

        getRootPane().setDefaultButton(btnSelesaikan);
        getRootPane().registerKeyboardAction(
                e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        txtUangDiterima.setText(String.valueOf(totalBayar));
        txtUangDiterima.selectAll();
        perbaruiModeMetodeBayar();
    }

    private void perbaruiModeMetodeBayar() {
        String metode = String.valueOf(cmbMetodeBayar.getSelectedItem());
        boolean modeQris = "QRIS".equalsIgnoreCase(metode);

        txtUangDiterima.setEditable(!modeQris);
        btnUangPas.setEnabled(!modeQris);
        btn20Ribu.setEnabled(!modeQris);
        btn50Ribu.setEnabled(!modeQris);
        btn100Ribu.setEnabled(!modeQris);

        if (modeQris) {
            txtUangDiterima.setText(String.valueOf(totalBayar));
            ImageIcon gambarQris = KoneksiDB.muatLogoOpsional("qris-kopdes.png", 160, 160);
            if (gambarQris != null) {
                lblInfoQris.setIcon(gambarQris);
                lblInfoQris.setText("Pindai kode QRIS Retail Kopdes di samping.");
            } else {
                lblInfoQris.setIcon(null);
                lblInfoQris.setText("QRIS aktif: nominal otomatis pas sesuai tagihan.");
            }
        } else {
            lblInfoQris.setIcon(null);
            lblInfoQris.setText("Masukkan nominal uang tunai atau gunakan tombol cepat.");
            txtUangDiterima.requestFocusInWindow();
            txtUangDiterima.selectAll();
        }
        hitungKembalianOtomatis();
    }

    private void hitungKembalianOtomatis() {
        long uang = KoneksiDB.parseNominalRupiah(txtUangDiterima.getText());
        if (uang < 0) {
            lblNilaiKembalian.setForeground(KoneksiDB.WARNA_BAHAYA);
            lblNilaiKembalian.setText("Nominal belum valid (masukkan angka Rupiah)");
            btnSelesaikan.setEnabled(false);
            return;
        }
        long selisih = uang - totalBayar;
        if (selisih < 0) {
            lblNilaiKembalian.setForeground(KoneksiDB.WARNA_BAHAYA);
            lblNilaiKembalian.setText("Uang kurang " + KoneksiDB.formatRupiah(Math.abs(selisih)));
            btnSelesaikan.setEnabled(false);
        } else {
            lblNilaiKembalian.setForeground(KoneksiDB.WARNA_TEKS_HIJAU);
            lblNilaiKembalian.setText(KoneksiDB.formatRupiah(selisih));
            btnSelesaikan.setEnabled(true);
        }
    }

    public static String simpanTransaksiKeDatabase(
            int idPengguna,
            String jenisPembeli,
            List<String[]> itemKeranjang,
            long subtotal,
            long potonganAnggota,
            long totalBayar,
            String metodeBayar,
            long uangBayar) throws Exception {

        if (itemKeranjang == null || itemKeranjang.isEmpty()) {
            throw new IllegalArgumentException("Keranjang belanja masih kosong.");
        }
        if (uangBayar < totalBayar) {
            throw new IllegalArgumentException("Nominal uang diterima kurang dari total tagihan.");
        }

        long kembalian = uangBayar - totalBayar;
        String waktuSekarang = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        try (Connection conn = KoneksiDB.ambilKoneksi()) {
            return jalankanTransaksiPadaKoneksi(
                    conn, idPengguna, jenisPembeli, itemKeranjang,
                    subtotal, potonganAnggota, totalBayar, metodeBayar, uangBayar, waktuSekarang
            );
        }
    }

    static String jalankanTransaksiPadaKoneksi(
            Connection conn,
            int idPengguna,
            String jenisPembeli,
            List<String[]> itemKeranjang,
            long subtotal,
            long potonganAnggota,
            long totalBayar,
            String metodeBayar,
            long uangBayar,
            String waktuSekarang) throws Exception {
        long kembalian = uangBayar - totalBayar;
        conn.setAutoCommit(false);
        try {
            String noNota = KoneksiDB.buatNomorNotaBaru(conn);

            String sqlNota = """
                INSERT INTO transaksi (
                    no_nota, tanggal_waktu, id_pengguna, jenis_pembeli,
                    subtotal, potongan_anggota, total_bayar, metode_bayar, uang_bayar, kembalian
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
            try (PreparedStatement psNota = conn.prepareStatement(sqlNota)) {
                psNota.setString(1, noNota);
                psNota.setString(2, waktuSekarang);
                psNota.setInt(3, idPengguna);
                psNota.setString(4, jenisPembeli);
                psNota.setLong(5, subtotal);
                psNota.setLong(6, potonganAnggota);
                psNota.setLong(7, totalBayar);
                psNota.setString(8, metodeBayar);
                psNota.setLong(9, uangBayar);
                psNota.setLong(10, kembalian);
                psNota.executeUpdate();
            }

            String sqlPotongStok = "UPDATE barang SET stok = stok - ? WHERE kode_barang = ? AND stok >= ?";
            String sqlDetail = """
                INSERT INTO detail_transaksi (
                    no_nota, kode_barang, nama_barang, harga_satuan, jumlah, subtotal_item
                ) VALUES (?, ?, ?, ?, ?, ?)
                """;

            try (PreparedStatement psStok = conn.prepareStatement(sqlPotongStok);
                 PreparedStatement psDetail = conn.prepareStatement(sqlDetail)) {
                for (String[] baris : itemKeranjang) {
                    String kode = baris[0];
                    String nama = baris[1];
                    long hargaSatuan = Long.parseLong(baris[2]);
                    int jumlah = Integer.parseInt(baris[3]);
                    long subItem = Long.parseLong(baris[4]);

                    psStok.setInt(1, jumlah);
                    psStok.setString(2, kode);
                    psStok.setInt(3, jumlah);
                    int barisTerubah = psStok.executeUpdate();
                    if (barisTerubah == 0) {
                        throw new IllegalArgumentException(
                                "Stok barang \"" + nama + "\" tidak mencukupi untuk jumlah " + jumlah + ".");
                    }

                    psDetail.setString(1, noNota);
                    psDetail.setString(2, kode);
                    psDetail.setString(3, nama);
                    psDetail.setLong(4, hargaSatuan);
                    psDetail.setInt(5, jumlah);
                    psDetail.setLong(6, subItem);
                    psDetail.executeUpdate();
                }
            }

            conn.commit();
            return noNota;
        } catch (Exception ex) {
            try {
                conn.rollback();
            } catch (java.sql.SQLException rollbackEx) {
                ex.addSuppressed(rollbackEx);
            }
            throw ex;
        }
    }

    private void prosesPenyelesaianBayar() {
        long uangDiterima = KoneksiDB.parseNominalRupiah(txtUangDiterima.getText());
        if (uangDiterima < totalBayar) {
            JOptionPane.showMessageDialog(this,
                    "Nominal uang yang dimasukkan belum mencukupi total tagihan.",
                    "Pembayaran Belum Cukup", JOptionPane.WARNING_MESSAGE);
            txtUangDiterima.requestFocusInWindow();
            return;
        }

        String metode = String.valueOf(cmbMetodeBayar.getSelectedItem());
        String waktuStruk = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        btnSelesaikan.setEnabled(false);
        try {
            String noNota = simpanTransaksiKeDatabase(
                    idPengguna, jenisPembeli, itemKeranjang,
                    subtotal, potonganAnggota, totalBayar, metode, uangDiterima
            );
            transaksiBerhasil = true;
            long kembalian = uangDiterima - totalBayar;
            String teksStruk = StrukDialog.susunTeksStruk(
                    noNota, waktuStruk, namaPetugas, jenisPembeli, itemKeranjang,
                    subtotal, potonganAnggota, totalBayar, metode, uangDiterima, kembalian
            );
            dispose();
            StrukDialog dialogStruk = new StrukDialog(indukFrame, true, teksStruk);
            dialogStruk.setVisible(true);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Transaksi tidak dapat disimpan:\n" + ex.getMessage(),
                    "Gagal Menyimpan Transaksi", JOptionPane.ERROR_MESSAGE);
            btnSelesaikan.setEnabled(true);
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelHeader = new javax.swing.JPanel();
        lblJudulBayar = new javax.swing.JLabel();
        panelIsi = new javax.swing.JPanel();
        panelRingkasanTagihan = new javax.swing.JPanel();
        lblInfoPembeli = new javax.swing.JLabel();
        lblTotalTagihanJudul = new javax.swing.JLabel();
        lblNilaiTotalTagihan = new javax.swing.JLabel();
        panelFormBayar = new javax.swing.JPanel();
        lblMetodeBayar = new javax.swing.JLabel();
        cmbMetodeBayar = new javax.swing.JComboBox<>();
        lblUangDiterima = new javax.swing.JLabel();
        txtUangDiterima = new javax.swing.JTextField();
        panelUangCepat = new javax.swing.JPanel();
        btnUangPas = new javax.swing.JButton();
        btn20Ribu = new javax.swing.JButton();
        btn50Ribu = new javax.swing.JButton();
        btn100Ribu = new javax.swing.JButton();
        lblKembalianJudul = new javax.swing.JLabel();
        lblNilaiKembalian = new javax.swing.JLabel();
        lblInfoQris = new javax.swing.JLabel();
        panelBawah = new javax.swing.JPanel();
        btnBatal = new javax.swing.JButton();
        btnSelesaikan = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Pembayaran Kasir - Retail Kopdes");
        setResizable(false);

        panelHeader.setBackground(new java.awt.Color(6, 95, 70));
        panelHeader.setBorder(javax.swing.BorderFactory.createEmptyBorder(14, 18, 14, 18));
        panelHeader.setLayout(new java.awt.BorderLayout());

        lblJudulBayar.setFont(new java.awt.Font("SansSerif", 1, 17)); // NOI18N
        lblJudulBayar.setForeground(new java.awt.Color(255, 255, 255));
        lblJudulBayar.setText("Penyelesaian Pembayaran");
        panelHeader.add(lblJudulBayar, java.awt.BorderLayout.CENTER);

        getContentPane().add(panelHeader, java.awt.BorderLayout.NORTH);

        panelIsi.setBackground(new java.awt.Color(240, 253, 244));
        panelIsi.setBorder(javax.swing.BorderFactory.createEmptyBorder(14, 18, 10, 18));

        panelRingkasanTagihan.setBackground(new java.awt.Color(236, 253, 245));
        panelRingkasanTagihan.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(new java.awt.Color(110, 231, 183)),
            javax.swing.BorderFactory.createEmptyBorder(12, 14, 12, 14)));

        lblInfoPembeli.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        lblInfoPembeli.setText("Kategori Pembeli: Umum");

        lblTotalTagihanJudul.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        lblTotalTagihanJudul.setText("TOTAL TAGIHAN");

        lblNilaiTotalTagihan.setFont(new java.awt.Font("SansSerif", 1, 24)); // NOI18N
        lblNilaiTotalTagihan.setText("Rp 0");

        javax.swing.GroupLayout panelRingkasanTagihanLayout = new javax.swing.GroupLayout(panelRingkasanTagihan);
        panelRingkasanTagihan.setLayout(panelRingkasanTagihanLayout);
        panelRingkasanTagihanLayout.setHorizontalGroup(
            panelRingkasanTagihanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblInfoPembeli, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(panelRingkasanTagihanLayout.createSequentialGroup()
                .addComponent(lblTotalTagihanJudul)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(lblNilaiTotalTagihan))
        );
        panelRingkasanTagihanLayout.setVerticalGroup(
            panelRingkasanTagihanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelRingkasanTagihanLayout.createSequentialGroup()
                .addComponent(lblInfoPembeli)
                .addGap(6, 6, 6)
                .addGroup(panelRingkasanTagihanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblTotalTagihanJudul)
                    .addComponent(lblNilaiTotalTagihan)))
        );

        panelFormBayar.setBackground(new java.awt.Color(255, 255, 255));
        panelFormBayar.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(new java.awt.Color(203, 213, 225)),
            javax.swing.BorderFactory.createEmptyBorder(14, 14, 14, 14)));

        lblMetodeBayar.setFont(new java.awt.Font("SansSerif", 1, 13)); // NOI18N
        lblMetodeBayar.setText("Metode Pembayaran");

        cmbMetodeBayar.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        cmbMetodeBayar.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Tunai", "QRIS" }));
        cmbMetodeBayar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbMetodeBayarActionPerformed(evt);
            }
        });

        lblUangDiterima.setFont(new java.awt.Font("SansSerif", 1, 13)); // NOI18N
        lblUangDiterima.setText("Nominal Uang Diterima (Rp)");

        txtUangDiterima.setFont(new java.awt.Font("SansSerif", 1, 15)); // NOI18N

        panelUangCepat.setBackground(new java.awt.Color(255, 255, 255));
        panelUangCepat.setLayout(new java.awt.GridLayout(1, 4, 6, 0));

        btnUangPas.setText("Uang Pas");
        btnUangPas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUangPasActionPerformed(evt);
            }
        });
        panelUangCepat.add(btnUangPas);

        btn20Ribu.setText("Rp 20.000");
        btn20Ribu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn20RibuActionPerformed(evt);
            }
        });
        panelUangCepat.add(btn20Ribu);

        btn50Ribu.setText("Rp 50.000");
        btn50Ribu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn50RibuActionPerformed(evt);
            }
        });
        panelUangCepat.add(btn50Ribu);

        btn100Ribu.setText("Rp 100.000");
        btn100Ribu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn100RibuActionPerformed(evt);
            }
        });
        panelUangCepat.add(btn100Ribu);

        lblKembalianJudul.setFont(new java.awt.Font("SansSerif", 1, 13)); // NOI18N
        lblKembalianJudul.setText("Kembalian");

        lblNilaiKembalian.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        lblNilaiKembalian.setText("Rp 0");

        lblInfoQris.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        lblInfoQris.setText("Masukkan nominal uang tunai atau gunakan tombol cepat.");

        javax.swing.GroupLayout panelFormBayarLayout = new javax.swing.GroupLayout(panelFormBayar);
        panelFormBayar.setLayout(panelFormBayarLayout);
        panelFormBayarLayout.setHorizontalGroup(
            panelFormBayarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblMetodeBayar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(cmbMetodeBayar, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(lblUangDiterima, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(txtUangDiterima)
            .addComponent(panelUangCepat, javax.swing.GroupLayout.DEFAULT_SIZE, 412, Short.MAX_VALUE)
            .addComponent(lblInfoQris, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(panelFormBayarLayout.createSequentialGroup()
                .addComponent(lblKembalianJudul)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(lblNilaiKembalian))
        );
        panelFormBayarLayout.setVerticalGroup(
            panelFormBayarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelFormBayarLayout.createSequentialGroup()
                .addComponent(lblMetodeBayar)
                .addGap(6, 6, 6)
                .addComponent(cmbMetodeBayar, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(lblUangDiterima)
                .addGap(6, 6, 6)
                .addComponent(txtUangDiterima, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(panelUangCepat, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(14, 14, 14)
                .addGroup(panelFormBayarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblKembalianJudul)
                    .addComponent(lblNilaiKembalian))
                .addGap(10, 10, 10)
                .addComponent(lblInfoQris))
        );

        javax.swing.GroupLayout panelIsiLayout = new javax.swing.GroupLayout(panelIsi);
        panelIsi.setLayout(panelIsiLayout);
        panelIsiLayout.setHorizontalGroup(
            panelIsiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelRingkasanTagihan, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(panelFormBayar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        panelIsiLayout.setVerticalGroup(
            panelIsiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelIsiLayout.createSequentialGroup()
                .addComponent(panelRingkasanTagihan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(panelFormBayar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        getContentPane().add(panelIsi, java.awt.BorderLayout.CENTER);

        panelBawah.setBackground(new java.awt.Color(240, 253, 244));
        panelBawah.setBorder(javax.swing.BorderFactory.createEmptyBorder(6, 18, 16, 18));
        panelBawah.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 10, 4));

        btnBatal.setFont(new java.awt.Font("SansSerif", 1, 13)); // NOI18N
        btnBatal.setText("Batal (Esc)");
        btnBatal.setPreferredSize(new java.awt.Dimension(115, 40));
        btnBatal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBatalActionPerformed(evt);
            }
        });
        panelBawah.add(btnBatal);

        btnSelesaikan.setBackground(new java.awt.Color(194, 65, 12));
        btnSelesaikan.setFont(new java.awt.Font("SansSerif", 1, 14)); // NOI18N
        btnSelesaikan.setForeground(new java.awt.Color(255, 255, 255));
        btnSelesaikan.setText("Selesaikan Transaksi");
        btnSelesaikan.setPreferredSize(new java.awt.Dimension(210, 40));
        btnSelesaikan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSelesaikanActionPerformed(evt);
            }
        });
        panelBawah.add(btnSelesaikan);

        getContentPane().add(panelBawah, java.awt.BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void cmbMetodeBayarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbMetodeBayarActionPerformed
        perbaruiModeMetodeBayar();
    }//GEN-LAST:event_cmbMetodeBayarActionPerformed

    private void btnUangPasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUangPasActionPerformed
        txtUangDiterima.setText(String.valueOf(totalBayar));
    }//GEN-LAST:event_btnUangPasActionPerformed

    private void btn20RibuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn20RibuActionPerformed
        txtUangDiterima.setText("20000");
    }//GEN-LAST:event_btn20RibuActionPerformed

    private void btn50RibuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn50RibuActionPerformed
        txtUangDiterima.setText("50000");
    }//GEN-LAST:event_btn50RibuActionPerformed

    private void btn100RibuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn100RibuActionPerformed
        txtUangDiterima.setText("100000");
    }//GEN-LAST:event_btn100RibuActionPerformed

    private void btnBatalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBatalActionPerformed
        dispose();
    }//GEN-LAST:event_btnBatalActionPerformed

    private void btnSelesaikanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSelesaikanActionPerformed
        prosesPenyelesaianBayar();
    }//GEN-LAST:event_btnSelesaikanActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btn100Ribu;
    private javax.swing.JButton btn20Ribu;
    private javax.swing.JButton btn50Ribu;
    private javax.swing.JButton btnBatal;
    private javax.swing.JButton btnSelesaikan;
    private javax.swing.JButton btnUangPas;
    private javax.swing.JComboBox<String> cmbMetodeBayar;
    private javax.swing.JLabel lblInfoPembeli;
    private javax.swing.JLabel lblInfoQris;
    private javax.swing.JLabel lblJudulBayar;
    private javax.swing.JLabel lblKembalianJudul;
    private javax.swing.JLabel lblMetodeBayar;
    private javax.swing.JLabel lblNilaiKembalian;
    private javax.swing.JLabel lblNilaiTotalTagihan;
    private javax.swing.JLabel lblTotalTagihanJudul;
    private javax.swing.JLabel lblUangDiterima;
    private javax.swing.JPanel panelBawah;
    private javax.swing.JPanel panelFormBayar;
    private javax.swing.JPanel panelHeader;
    private javax.swing.JPanel panelIsi;
    private javax.swing.JPanel panelRingkasanTagihan;
    private javax.swing.JPanel panelUangCepat;
    private javax.swing.JTextField txtUangDiterima;
    // End of variables declaration//GEN-END:variables
}

