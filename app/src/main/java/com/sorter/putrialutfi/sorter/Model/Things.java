package com.sorter.putrialutfi.sorter.Model;

import android.support.annotation.NonNull;

public class Things implements Comparable<Things> {
    private String nama_barang;
    private String harga_ecer;
    private String harga_resell;
    private int is_deleted;
    private String key;

    public Things(){

    }

    public Things(String nama_barang, String harga_ecer, String harga_resell, int is_deleted) {
        this.nama_barang = nama_barang;
        this.harga_ecer = harga_ecer;
        this.harga_resell = harga_resell;
        this.is_deleted = is_deleted;
    }

    public String getNama_barang() {
        return nama_barang;
    }

    public void setNama_barang(String nama_barang) {
        this.nama_barang = nama_barang;
    }

    public String getHarga_ecer() {
        return harga_ecer;
    }

    public void setHarga_ecer(String harga_ecer) {
        this.harga_ecer = harga_ecer;
    }


    public String getHarga_resell() {
        return harga_resell;
    }

    public void setHarga_resell(String harga_resell) {
        this.harga_resell = harga_resell;
    }

    public int getIs_deleted() {
        return is_deleted;
    }

    public void setIs_deleted(int is_deleted) {
        this.is_deleted = is_deleted;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public int compareTo(@NonNull Things o) {
        return this.nama_barang.compareTo(o.getNama_barang());
    }
}
