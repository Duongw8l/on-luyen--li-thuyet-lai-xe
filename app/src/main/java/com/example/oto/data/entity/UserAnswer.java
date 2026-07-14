package com.example.oto.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Bảng user_answers — chi tiết từng câu trả lời trong một lượt thi.
 * Bảng quan trọng nhất: dùng để xem lại bài, gom câu sai, phân tích điểm yếu.
 */
@Entity(
        tableName = "user_answers",
        foreignKeys = {
                @ForeignKey(
                        entity = Attempt.class,
                        parentColumns = "id",
                        childColumns = "attempt_id",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = Question.class,
                        parentColumns = "id",
                        childColumns = "question_id",
                        onDelete = ForeignKey.CASCADE
                )
        },
        indices = {@Index("attempt_id"), @Index("question_id")}
)
public class UserAnswer {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "attempt_id")
    public int attemptId;

    @ColumnInfo(name = "question_id")
    public int questionId;

    /** Đáp án đã chọn (0 = không chọn / bỏ trống). */
    @ColumnInfo(name = "answer_id")
    public int answerId;

    @ColumnInfo(name = "dung_sai")
    public boolean dungSai;

    public UserAnswer() {
    }

    public UserAnswer(int attemptId, int questionId, int answerId, boolean dungSai) {
        this.attemptId = attemptId;
        this.questionId = questionId;
        this.answerId = answerId;
        this.dungSai = dungSai;
    }
}
