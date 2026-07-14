package com.example.oto.data.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/** Bảng exam_sets — bộ đề cố định. */
@Entity(tableName = "exam_sets")
public class ExamSet {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "ten_de")
    @NonNull
    public String tenDe = "";

    @ColumnInfo(name = "so_cau")
    public int soCau;

    /** Thời gian làm bài (phút). */
    @ColumnInfo(name = "thoi_gian")
    public int thoiGian;

    /** Số câu đúng tối thiểu để đạt. */
    @ColumnInfo(name = "nguong_dat")
    public int nguongDat;

    public ExamSet() {
    }

    public ExamSet(@NonNull String tenDe, int soCau, int thoiGian, int nguongDat) {
        this.tenDe = tenDe;
        this.soCau = soCau;
        this.thoiGian = thoiGian;
        this.nguongDat = nguongDat;
    }

    @NonNull
    @Override
    public String toString() {
        return tenDe + " (" + soCau + " câu / " + thoiGian + " phút)";
    }
}
