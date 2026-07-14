package com.example.oto.data.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/** Bảng answers — 4 đáp án của một câu hỏi. Xoá câu hỏi -> CASCADE xoá đáp án. */
@Entity(
        tableName = "answers",
        foreignKeys = @ForeignKey(
                entity = Question.class,
                parentColumns = "id",
                childColumns = "question_id",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("question_id")}
)
public class Answer {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "question_id")
    public int questionId; // FK -> questions (CASCADE)

    @ColumnInfo(name = "noi_dung")
    @NonNull
    public String noiDung = "";

    @ColumnInfo(name = "is_correct")
    public boolean isCorrect;

    public Answer() {
    }

    public Answer(int questionId, @NonNull String noiDung, boolean isCorrect) {
        this.questionId = questionId;
        this.noiDung = noiDung;
        this.isCorrect = isCorrect;
    }
}
