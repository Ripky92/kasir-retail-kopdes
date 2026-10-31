package com.kopdes;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.print.PrinterException;
import java.util.List;

public class StrukDialog extends javax.swing.JDialog {

    public StrukDialog(java.awt.Frame parent, boolean modal, String teksStruk) {
        super(parent, modal);
        initComponents();
        setLocationRelativeTo(parent);
        terapkanGayaDanAksesibilitas();
        areaStruk.setText(teksStruk != null ? teksStruk : "");
        areaStruk.setCaretPosition(0);
    }

    private void terapkanGayaDanAksesibilitas() {
        panelHeader.setBackground(KoneksiDB.WARNA_HEADER);
        lblJudulStruk.setForeground(KoneksiDB.WARNA_TEKS_PUTIH);
        panelUtama.setBackground(KoneksiDB.WARNA_LATAR);
        panelTombol.setBackground(KoneksiDB.WARNA_LATAR);

        areaStruk.setBackground(KoneksiDB.WARNA_KARTU);
        areaStruk.setForeground(KoneksiDB.WARNA_TEKS_UTAMA);
        areaStruk.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));

        btnCetak.setBackground(KoneksiDB.WARNA_HEADER);
        btnCetak.setForeground(KoneksiDB.WARNA_TEKS_PUTIH);
        btnCetak.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btnTutup.setBackground(KoneksiDB.WARNA_KARTU);
        btnTutup.setForeground(KoneksiDB.WARNA_TEKS_UTAMA);
        btnTutup.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        getRootPane().setDefaultButton(btnTutup);
        getRootPane().registerKeyboardAction(
                e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }

    public static String susunTeksStruk(
            String noNota,
            String tanggalWaktu,
            String namaPetugas,
            String jenisPembeli,
            List<String[]> daftarItem,
            long subtotal,
            long potonganAnggota,
            long totalBayar,
            String metodeBayar,
            long uangBayar,
            long kembalian) {

        String garisUtama = "========================================\n";
        String garisTipis = "----------------------------------------\n";
        StringBuilder sb = new StringBuilder();

        sb.append(garisUtama);
        sb.append("             RETAIL KOPDES              \n");
        sb.append("       Unit Usaha Koperasi Desa         \n");
        sb.append(garisUtama);
        sb.append(String.format("No. Nota  : %s%n", noNota));
        sb.append(String.format("Tanggal   : %s%n", tanggalWaktu));
        sb.append(String.format("Petugas   : %s%n", namaPetugas));
        sb.append(String.format("Pembeli   : %s%n", jenisPembeli));
        sb.append(garisTipis);

        if (daftarItem != null) {
            for (String[] item : daftarItem) {
                String nama = item[1];
                if (nama.length() > 38) {
                    nama = nama.substring(0, 38);
                }
                long hargaSatuan = Long.parseLong(item[2]);
                int qty = Integer.parseInt(item[3]);
                long subItem = Long.parseLong(item[4]);

                sb.append(nama).append("\n");
                String kiri = String.format("  %d x %s", qty, KoneksiDB.formatRupiah(hargaSatuan));
                String kanan = KoneksiDB.formatRupiah(subItem);
                sb.append(formatBarisKiriKanan(kiri, kanan, 40)).append("\n");
            }
        }

        sb.append(garisTipis);
        sb.append(formatBarisKiriKanan("Subtotal", KoneksiDB.formatRupiah(subtotal), 40)).append("\n");
        if (potonganAnggota > 0) {
            sb.append(formatBarisKiriKanan(
                    "Potongan Anggota (" + KoneksiDB.PERSEN_DISKON_ANGGOTA + "%)",
                    "-" + KoneksiDB.formatRupiah(potonganAnggota), 40)).append("\n");
        }
        sb.append(formatBarisKiriKanan("TOTAL BAYAR", KoneksiDB.formatRupiah(totalBayar), 40)).append("\n");
        sb.append(garisTipis);
        sb.append(formatBarisKiriKanan("Metode (" + metodeBayar + ")", KoneksiDB.formatRupiah(uangBayar), 40)).append("\n");
        sb.append(formatBarisKiriKanan("Kembalian", KoneksiDB.formatRupiah(kembalian), 40)).append("\n");
        sb.append(garisUtama);
        sb.append("  Terima kasih telah berbelanja di      \n");
        sb.append("      Koperasi Desa (Retail Kopdes)     \n");
        sb.append(garisUtama);
        return sb.toString();
    }

    private static String formatBarisKiriKanan(String kiri, String kanan, int lebarTotal) {
        int sisaSpasi = lebarTotal - kiri.length() - kanan.length();
        if (sisaSpasi < 1) {
            sisaSpasi = 1;
        }
        return kiri + " ".repeat(sisaSpasi) + kanan;
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelHeader = new javax.swing.JPanel();
        lblJudulStruk = new javax.swing.JLabel();
        panelUtama = new javax.swing.JPanel();
        scrollStruk = new javax.swing.JScrollPane();
        areaStruk = new javax.swing.JTextArea();
        panelTombol = new javax.swing.JPanel();
        btnCetak = new javax.swing.JButton();
        btnTutup = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Struk Belanja - Retail Kopdes");
        setMinimumSize(new java.awt.Dimension(420, 520));

        panelHeader.setBackground(new java.awt.Color(6, 95, 70));
        panelHeader.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 16, 12, 16));
        panelHeader.setLayout(new java.awt.BorderLayout());

        lblJudulStruk.setFont(new java.awt.Font("SansSerif", 1, 16)); // NOI18N
        lblJudulStruk.setForeground(new java.awt.Color(255, 255, 255));
        lblJudulStruk.setText("Pratinjau Struk Belanja");
        panelHeader.add(lblJudulStruk, java.awt.BorderLayout.CENTER);

        getContentPane().add(panelHeader, java.awt.BorderLayout.NORTH);

        panelUtama.setBackground(new java.awt.Color(240, 253, 244));
        panelUtama.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 16, 8, 16));
        panelUtama.setLayout(new java.awt.BorderLayout());

        areaStruk.setEditable(false);
        areaStruk.setColumns(42);
        areaStruk.setFont(new java.awt.Font("Monospaced", 0, 13)); // NOI18N
        areaStruk.setRows(22);
        areaStruk.setMargin(new java.awt.Insets(12, 12, 12, 12));
        scrollStruk.setViewportView(areaStruk);

        panelUtama.add(scrollStruk, java.awt.BorderLayout.CENTER);

        getContentPane().add(panelUtama, java.awt.BorderLayout.CENTER);

        panelTombol.setBackground(new java.awt.Color(240, 253, 244));
        panelTombol.setBorder(javax.swing.BorderFactory.createEmptyBorder(6, 16, 14, 16));
        panelTombol.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 10, 4));

        btnCetak.setBackground(new java.awt.Color(6, 95, 70));
        btnCetak.setFont(new java.awt.Font("SansSerif", 1, 13)); // NOI18N
        btnCetak.setForeground(new java.awt.Color(255, 255, 255));
        btnCetak.setText("Cetak Printer");
        btnCetak.setPreferredSize(new java.awt.Dimension(140, 38));
        btnCetak.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCetakActionPerformed(evt);
            }
        });
        panelTombol.add(btnCetak);

        btnTutup.setFont(new java.awt.Font("SansSerif", 1, 13)); // NOI18N
        btnTutup.setText("Tutup (Esc)");
        btnTutup.setPreferredSize(new java.awt.Dimension(120, 38));
        btnTutup.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTutupActionPerformed(evt);
            }
        });
        panelTombol.add(btnTutup);

        getContentPane().add(panelTombol, java.awt.BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void btnCetakActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCetakActionPerformed
        try {
            boolean selesai = areaStruk.print();
            if (selesai) {
                JOptionPane.showMessageDialog(this,
                        "Struk berhasil dikirim ke antrean printer.",
                        "Cetak Berhasil", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (PrinterException ex) {
            JOptionPane.showMessageDialog(this,
                    "Gagal mencetak struk: " + ex.getMessage() + "\nPastikan printer sudah menyala dan terhubung.",
                    "Kendala Printer", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnCetakActionPerformed

    private void btnTutupActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTutupActionPerformed
        dispose();
    }//GEN-LAST:event_btnTutupActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea areaStruk;
    private javax.swing.JButton btnCetak;
    private javax.swing.JButton btnTutup;
    private javax.swing.JLabel lblJudulStruk;
    private javax.swing.JPanel panelHeader;
    private javax.swing.JPanel panelTombol;
    private javax.swing.JPanel panelUtama;
    private javax.swing.JScrollPane scrollStruk;
    // End of variables declaration//GEN-END:variables
}
