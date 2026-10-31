package com.kopdes;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.Cursor;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginForm extends javax.swing.JFrame {

    public LoginForm() {
        initComponents();
        terapkanIdentitasDanLogo();
    }

    private void terapkanIdentitasDanLogo() {
        panelHeader.setBackground(KoneksiDB.WARNA_HEADER);
        lblNamaToko.setForeground(KoneksiDB.WARNA_TEKS_PUTIH);
        lblSubJudul.setForeground(KoneksiDB.WARNA_RINGKASAN);

        panelIsi.setBackground(KoneksiDB.WARNA_LATAR);
        panelKartuLogin.setBackground(KoneksiDB.WARNA_KARTU);
        lblUsername.setForeground(KoneksiDB.WARNA_TEKS_UTAMA);
        lblPassword.setForeground(KoneksiDB.WARNA_TEKS_UTAMA);
        lblUsername.setLabelFor(txtUsername);
        lblPassword.setLabelFor(txtPassword);

        lblPesan.setForeground(KoneksiDB.WARNA_TEKS_HIJAU);
        lblPesan.setText("Silakan masukkan akun petugas untuk membuka kasir.");

        btnMasuk.setBackground(KoneksiDB.WARNA_HEADER);
        btnMasuk.setForeground(KoneksiDB.WARNA_TEKS_PUTIH);
        btnMasuk.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btnKeluar.setBackground(KoneksiDB.WARNA_KARTU);
        btnKeluar.setForeground(KoneksiDB.WARNA_TEKS_UTAMA);
        btnKeluar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        ImageIcon ikonJendela = KoneksiDB.muatLogoOpsional("logo-ikon.png", 64, 64);
        if (ikonJendela != null) {
            setIconImage(ikonJendela.getImage());
        }

        ImageIcon logoHeader = KoneksiDB.muatLogoOpsional("logo-ikon.png", 64, 64);
        if (logoHeader == null) {
            logoHeader = KoneksiDB.muatLogoOpsional("logo-login.png", 64, 64);
        }
        if (logoHeader != null) {
            lblLogoAtauTeks.setText("");
            lblLogoAtauTeks.setIcon(logoHeader);
        } else {
            lblLogoAtauTeks.setText("[RK]");
            lblLogoAtauTeks.setForeground(KoneksiDB.WARNA_TEKS_PUTIH);
        }

        ImageIcon ikonLoginCard = KoneksiDB.muatLogoOpsional("logo-login.png", 18, 18);
        if (ikonLoginCard != null) {
            lblPesan.setIcon(ikonLoginCard);
        }

        getRootPane().setDefaultButton(btnMasuk);
    }

    public static String[] cekLoginDatabase(String username, String password) {
        if (username == null || password == null || username.isBlank() || password.isBlank()) {
            return null;
        }
        String sql = "SELECT id_pengguna, nama_lengkap, peran FROM pengguna WHERE username = ? AND password = ?";
        try (Connection conn = KoneksiDB.ambilKoneksi();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username.trim());
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new String[]{
                        String.valueOf(rs.getInt("id_pengguna")),
                        rs.getString("nama_lengkap"),
                        rs.getString("peran")
                    };
                }
            }
        } catch (Exception ex) {
            return null;
        }
        return null;
    }

    private void jalankanProsesLogin() {
        String user = txtUsername.getText().trim();
        String pass = new String(txtPassword.getPassword());

        if (user.isEmpty()) {
            lblPesan.setForeground(KoneksiDB.WARNA_BAHAYA);
            lblPesan.setText("Perhatian: Kolom username belum diisi.");
            txtUsername.requestFocusInWindow();
            return;
        }
        if (pass.isEmpty()) {
            lblPesan.setForeground(KoneksiDB.WARNA_BAHAYA);
            lblPesan.setText("Perhatian: Kolom kata sandi belum diisi.");
            txtPassword.requestFocusInWindow();
            return;
        }

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        btnMasuk.setEnabled(false);
        try {
            String[] dataAkun = cekLoginDatabase(user, pass);
            if (dataAkun == null) {
                lblPesan.setForeground(KoneksiDB.WARNA_BAHAYA);
                lblPesan.setText("Gagal masuk: Username atau kata sandi tidak sesuai.");
                txtPassword.selectAll();
                txtPassword.requestFocusInWindow();
                return;
            }

            int idPengguna = Integer.parseInt(dataAkun[0]);
            String namaLengkap = dataAkun[1];
            String peran = dataAkun[2];

            KasirUtamaForm formUtama = new KasirUtamaForm(idPengguna, namaLengkap, peran);
            formUtama.setVisible(true);
            dispose();
        } finally {
            setCursor(Cursor.getDefaultCursor());
            btnMasuk.setEnabled(true);
        }
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelHeader = new javax.swing.JPanel();
        lblLogoAtauTeks = new javax.swing.JLabel();
        lblNamaToko = new javax.swing.JLabel();
        lblSubJudul = new javax.swing.JLabel();
        panelIsi = new javax.swing.JPanel();
        panelKartuLogin = new javax.swing.JPanel();
        lblUsername = new javax.swing.JLabel();
        txtUsername = new javax.swing.JTextField();
        lblPassword = new javax.swing.JLabel();
        txtPassword = new javax.swing.JPasswordField();
        lblPesan = new javax.swing.JLabel();
        btnMasuk = new javax.swing.JButton();
        btnKeluar = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Masuk Petugas - Retail Kopdes");
        setMinimumSize(new java.awt.Dimension(460, 480));
        setPreferredSize(new java.awt.Dimension(480, 520));
        setResizable(false);

        panelHeader.setBackground(new java.awt.Color(6, 95, 70));
        panelHeader.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 24, 20, 24));

        lblLogoAtauTeks.setFont(new java.awt.Font("SansSerif", 1, 20)); // NOI18N
        lblLogoAtauTeks.setForeground(new java.awt.Color(255, 255, 255));
        lblLogoAtauTeks.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblLogoAtauTeks.setText("[RK]");

        lblNamaToko.setFont(new java.awt.Font("SansSerif", 1, 22)); // NOI18N
        lblNamaToko.setForeground(new java.awt.Color(255, 255, 255));
        lblNamaToko.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblNamaToko.setText("RETAIL KOPDES");

        lblSubJudul.setFont(new java.awt.Font("SansSerif", 0, 13)); // NOI18N
        lblSubJudul.setForeground(new java.awt.Color(236, 253, 245));
        lblSubJudul.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblSubJudul.setText("Sistem Kasir Unit Usaha Koperasi Desa");

        javax.swing.GroupLayout panelHeaderLayout = new javax.swing.GroupLayout(panelHeader);
        panelHeader.setLayout(panelHeaderLayout);
        panelHeaderLayout.setHorizontalGroup(
            panelHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblLogoAtauTeks, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(lblNamaToko, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(lblSubJudul, javax.swing.GroupLayout.DEFAULT_SIZE, 392, Short.MAX_VALUE)
        );
        panelHeaderLayout.setVerticalGroup(
            panelHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelHeaderLayout.createSequentialGroup()
                .addComponent(lblLogoAtauTeks)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblNamaToko)
                .addGap(4, 4, 4)
                .addComponent(lblSubJudul))
        );

        getContentPane().add(panelHeader, java.awt.BorderLayout.NORTH);

        panelIsi.setBackground(new java.awt.Color(240, 253, 244));
        panelIsi.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 24, 24, 24));
        panelIsi.setLayout(new java.awt.BorderLayout());

        panelKartuLogin.setBackground(new java.awt.Color(255, 255, 255));

        lblUsername.setFont(new java.awt.Font("SansSerif", 1, 13)); // NOI18N
        lblUsername.setText("Username Petugas");

        txtUsername.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N

        lblPassword.setFont(new java.awt.Font("SansSerif", 1, 13)); // NOI18N
        lblPassword.setText("Kata Sandi");

        txtPassword.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N

        lblPesan.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        lblPesan.setText("Silakan masukkan akun petugas untuk membuka kasir.");

        btnMasuk.setBackground(new java.awt.Color(6, 95, 70));
        btnMasuk.setFont(new java.awt.Font("SansSerif", 1, 14)); // NOI18N
        btnMasuk.setForeground(new java.awt.Color(255, 255, 255));
        btnMasuk.setText("Masuk ke Kasir");
        btnMasuk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMasukActionPerformed(evt);
            }
        });

        btnKeluar.setFont(new java.awt.Font("SansSerif", 1, 13)); // NOI18N
        btnKeluar.setText("Keluar");
        btnKeluar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnKeluarActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelKartuLoginLayout = new javax.swing.GroupLayout(panelKartuLogin);
        panelKartuLogin.setLayout(panelKartuLoginLayout);
        panelKartuLoginLayout.setHorizontalGroup(
            panelKartuLoginLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblUsername, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(txtUsername)
            .addComponent(lblPassword, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(txtPassword)
            .addComponent(lblPesan, javax.swing.GroupLayout.DEFAULT_SIZE, 350, Short.MAX_VALUE)
            .addGroup(panelKartuLoginLayout.createSequentialGroup()
                .addComponent(btnMasuk, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(10, 10, 10)
                .addComponent(btnKeluar, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        panelKartuLoginLayout.setVerticalGroup(
            panelKartuLoginLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelKartuLoginLayout.createSequentialGroup()
                .addComponent(lblUsername)
                .addGap(6, 6, 6)
                .addComponent(txtUsername, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(14, 14, 14)
                .addComponent(lblPassword)
                .addGap(6, 6, 6)
                .addComponent(txtPassword, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(lblPesan)
                .addGap(18, 18, 18)
                .addGroup(panelKartuLoginLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnMasuk, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnKeluar, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        panelIsi.add(panelKartuLogin, java.awt.BorderLayout.CENTER);

        getContentPane().add(panelIsi, java.awt.BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void btnMasukActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMasukActionPerformed
        jalankanProsesLogin();
    }//GEN-LAST:event_btnMasukActionPerformed

    private void btnKeluarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnKeluarActionPerformed
        System.exit(0);
    }//GEN-LAST:event_btnKeluarActionPerformed

    public static void main(String[] args) {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        FlatLightLaf.setup();
        UIManager.put("Button.arc", 8);
        UIManager.put("Component.arc", 8);
        UIManager.put("TextComponent.arc", 6);
        UIManager.put("Component.focusWidth", 2);

        KoneksiDB.siapkanDatabase();
        SwingUtilities.invokeLater(() -> new LoginForm().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnKeluar;
    private javax.swing.JButton btnMasuk;
    private javax.swing.JLabel lblLogoAtauTeks;
    private javax.swing.JLabel lblNamaToko;
    private javax.swing.JLabel lblPassword;
    private javax.swing.JLabel lblPesan;
    private javax.swing.JLabel lblSubJudul;
    private javax.swing.JLabel lblUsername;
    private javax.swing.JPanel panelHeader;
    private javax.swing.JPanel panelIsi;
    private javax.swing.JPanel panelKartuLogin;
    private javax.swing.JPasswordField txtPassword;
    private javax.swing.JTextField txtUsername;
    // End of variables declaration//GEN-END:variables
}

