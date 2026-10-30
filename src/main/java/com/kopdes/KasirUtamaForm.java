package com.kopdes;

public class KasirUtamaForm extends javax.swing.JFrame {

    private final int idPengguna;
    private final String namaLengkap;
    private final String peran;

    public KasirUtamaForm(int idPengguna, String namaLengkap, String peran) {
        this.idPengguna = idPengguna;
        this.namaLengkap = namaLengkap;
        this.peran = peran;
        setTitle("Retail Kopdes - " + namaLengkap + " (" + peran + ")");
        setSize(1100, 700);
        setLocationRelativeTo(null);
    }
}
