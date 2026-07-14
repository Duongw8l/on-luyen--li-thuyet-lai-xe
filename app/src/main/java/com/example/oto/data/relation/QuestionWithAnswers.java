package com.example.oto.data.relation;

import androidx.room.Embedded;
import androidx.room.Relation;

import com.example.oto.data.entity.Answer;
import com.example.oto.data.entity.Question;

import java.util.ArrayList;
import java.util.List;

/** Một câu hỏi kèm toàn bộ đáp án của nó (dùng khi làm bài / ôn tập). */
public class QuestionWithAnswers {

    @Embedded
    public Question question;

    @Relation(parentColumn = "id", entityColumn = "question_id")
    public List<Answer> answers = new ArrayList<>();

    /** Trả về id đáp án đúng, hoặc 0 nếu dữ liệu lỗi (không có đáp án đúng). */
    public int correctAnswerId() {
        for (Answer a : answers) {
            if (a.isCorrect) {
                return a.id;
            }
        }
        return 0;
    }
}
