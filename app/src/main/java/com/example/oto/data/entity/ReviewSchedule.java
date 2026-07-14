package com.example.oto.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/** Bảng review_schedule — lịch ôn lại câu sai theo cơ chế giãn dần (spaced repetition). */
@Entity(
        tableName = "review_schedule",
        foreignKeys = {
                @ForeignKey(
                        entity = User.class,
                        parentColumns = "id",
                        childColumns = "user_id",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = Question.class,
                        parentColumns = "id",
                        childColumns = "question_id",
                        onDelete = ForeignKey.CASCADE
                )
        },
        indices = {@Index("user_id"), @Index("question_id")}
)
public class ReviewSchedule {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "user_id")
    public String userId;

    @ColumnInfo(name = "question_id")
    public int questionId;

    @ColumnInfo(name = "so_lan_sai")
    public int soLanSai;

    /** Ngày cần ôn lại (epoch millis). */
    @ColumnInfo(name = "lan_on_tiep")
    public long lanOnTiep;

    /** Số ngày giãn cách hiện tại (tăng dần: 1, 3, 7, 14...). */
    @ColumnInfo(name = "khoang_cach")
    public int khoangCach;

    public ReviewSchedule() {
    }
}
