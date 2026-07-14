package com.example.oto.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/** Bảng attempts — một lượt thi. */
@Entity(
        tableName = "attempts",
        foreignKeys = @ForeignKey(
                entity = User.class,
                parentColumns = "id",
                childColumns = "user_id",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("user_id")}
)
public class Attempt {

    public static final String KET_QUA_DAT = "DAT";
    public static final String KET_QUA_TRUOT = "TRUOT";

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "user_id")
    public String userId; // FK -> users

    /** FK -> exam_sets, = 0 nếu là đề ngẫu nhiên. */
    @ColumnInfo(name = "exam_set_id")
    public int examSetId;

    @ColumnInfo(name = "so_cau_dung")
    public int soCauDung;

    /** "DAT" / "TRUOT" */
    @ColumnInfo(name = "ket_qua")
    public String ketQua;

    /** Lý do trượt (rỗng nếu đạt). */
    @ColumnInfo(name = "ly_do_truot")
    public String lyDoTruot;

    /** Số giây đã dùng. */
    @ColumnInfo(name = "thoi_gian_lam")
    public int thoiGianLam;

    /** Thời điểm thi (epoch millis). */
    @ColumnInfo(name = "ngay_thi")
    public long ngayThi;

    public Attempt() {
    }
}
