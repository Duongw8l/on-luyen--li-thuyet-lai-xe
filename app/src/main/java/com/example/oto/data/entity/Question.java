package com.example.oto.data.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/** Bảng questions — câu hỏi lý thuyết. */
@Entity(
        tableName = "questions",
        foreignKeys = @ForeignKey(
                entity = Chapter.class,
                parentColumns = "id",
                childColumns = "chapter_id",
                onDelete = ForeignKey.RESTRICT
        ),
        indices = {@Index("chapter_id")}
)
public class Question {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "chapter_id")
    public int chapterId; // FK -> chapters

    @ColumnInfo(name = "noi_dung")
    @NonNull
    public String noiDung = "";

    /** Ảnh biển báo / sa hình (có thể rỗng). */
    @ColumnInfo(name = "anh_url")
    public String anhUrl;

    /** Đánh dấu câu điểm liệt — sai câu này là trượt ngay. */
    @ColumnInfo(name = "is_diem_liet")
    public boolean isDiemLiet;

    @ColumnInfo(name = "giai_thich")
    public String giaiThich;

    /**
     * Thời điểm sửa gần nhất (epoch millis).
     *
     * Dùng cho cơ chế đồng bộ delta: máy khách chỉ hỏi Firestore những câu có
     * updated_at mới hơn lần đồng bộ cuối, thay vì tải lại toàn bộ 600 câu.
     */
    @ColumnInfo(name = "updated_at")
    public long updatedAt;

    public Question() {
    }

    public Question(int chapterId, @NonNull String noiDung, boolean isDiemLiet, String giaiThich) {
        this.chapterId = chapterId;
        this.noiDung = noiDung;
        this.isDiemLiet = isDiemLiet;
        this.giaiThich = giaiThich;
        this.updatedAt = System.currentTimeMillis();
    }
}
