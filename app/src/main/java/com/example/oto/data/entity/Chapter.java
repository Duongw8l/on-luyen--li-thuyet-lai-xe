package com.example.oto.data.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/** Bảng chapters — 6 chương của bộ đề. */
@Entity(tableName = "chapters")
public class Chapter {

    @PrimaryKey
    public int id;

    @ColumnInfo(name = "ten_chuong")
    @NonNull
    public String tenChuong = "";

    @ColumnInfo(name = "so_thu_tu")
    public int soThuTu; // 1..6

    public Chapter() {
    }

    public Chapter(int id, @NonNull String tenChuong, int soThuTu) {
        this.id = id;
        this.tenChuong = tenChuong;
        this.soThuTu = soThuTu;
    }

    @NonNull
    @Override
    public String toString() {
        // Dùng trực tiếp cho Spinner chọn chương
        return "Chương " + soThuTu + " — " + tenChuong;
    }
}
