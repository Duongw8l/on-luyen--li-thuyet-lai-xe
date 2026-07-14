package com.example.oto.data.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/** Bảng users — người dùng (khóa chính là Firebase UID, ở bản offline dùng UID cục bộ). */
@Entity(tableName = "users")
public class User {

    @PrimaryKey
    @NonNull
    public String id = "";

    @ColumnInfo(name = "ho_ten")
    public String hoTen;

    public String email;

    @ColumnInfo(name = "anh_dai_dien")
    public String anhDaiDien;

    /** "user" hoặc "admin" */
    @ColumnInfo(name = "vai_tro")
    public String vaiTro;

    /** Ngày thi dự kiến (epoch millis) — dùng cho đếm ngược. 0 = chưa đặt. */
    @ColumnInfo(name = "ngay_thi_du_kien")
    public long ngayThiDuKien;

    public User() {
    }

    public User(@NonNull String id, String hoTen, String email, String vaiTro) {
        this.id = id;
        this.hoTen = hoTen;
        this.email = email;
        this.vaiTro = vaiTro;
    }
}
