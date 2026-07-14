package com.example.oto.data.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/** Bảng traffic_signs — biển báo giao thông. */
@Entity(tableName = "traffic_signs")
public class TrafficSign {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "ma_bien")
    @NonNull
    public String maBien = "";

    @ColumnInfo(name = "ten_bien")
    @NonNull
    public String tenBien = "";

    /** Cấm / Nguy hiểm / Hiệu lệnh / Chỉ dẫn / Phụ */
    @ColumnInfo(name = "nhom_bien")
    public String nhomBien;

    @ColumnInfo(name = "anh_url")
    public String anhUrl;

    @ColumnInfo(name = "mo_ta")
    public String moTa;

    public TrafficSign() {
    }

    public TrafficSign(@NonNull String maBien, @NonNull String tenBien, String nhomBien, String moTa) {
        this.maBien = maBien;
        this.tenBien = tenBien;
        this.nhomBien = nhomBien;
        this.moTa = moTa;
    }
}
