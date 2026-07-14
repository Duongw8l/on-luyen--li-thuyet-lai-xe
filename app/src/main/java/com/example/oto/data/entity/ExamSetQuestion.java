package com.example.oto.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

/** Bảng nối exam_set_questions — bộ đề <-> câu hỏi (nhiều-nhiều). */
@Entity(
        tableName = "exam_set_questions",
        primaryKeys = {"exam_set_id", "question_id"},
        foreignKeys = {
                @ForeignKey(
                        entity = ExamSet.class,
                        parentColumns = "id",
                        childColumns = "exam_set_id",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = Question.class,
                        parentColumns = "id",
                        childColumns = "question_id",
                        onDelete = ForeignKey.CASCADE
                )
        },
        indices = {@Index("question_id")}
)
public class ExamSetQuestion {

    @ColumnInfo(name = "exam_set_id")
    public int examSetId;

    @ColumnInfo(name = "question_id")
    public int questionId;

    public ExamSetQuestion() {
    }

    public ExamSetQuestion(int examSetId, int questionId) {
        this.examSetId = examSetId;
        this.questionId = questionId;
    }
}
