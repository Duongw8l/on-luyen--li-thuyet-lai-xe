package com.example.oto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.oto.data.entity.Answer;
import com.example.oto.data.entity.Question;
import com.example.oto.data.relation.QuestionWithAnswers;
import com.example.oto.logic.ExamResult;
import com.example.oto.logic.ExamScorer;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Kiểm thử thuật toán chấm điểm — trọng tâm là luật câu điểm liệt. */
public class ExamScorerTest {

    /** Tạo 1 câu hỏi có 2 đáp án (đúng ở index 0), gán id thủ công. */
    private QuestionWithAnswers cau(int qid, boolean diemLiet, int correctAnswerId, int wrongAnswerId) {
        QuestionWithAnswers qa = new QuestionWithAnswers();
        qa.question = new Question(1, "Câu " + qid, diemLiet, "");
        qa.question.id = qid;
        Answer dung = new Answer(qid, "Đúng", true);
        dung.id = correctAnswerId;
        Answer sai = new Answer(qid, "Sai", false);
        sai.id = wrongAnswerId;
        qa.answers = new ArrayList<>();
        qa.answers.add(dung);
        qa.answers.add(sai);
        return qa;
    }

    @Test
    public void sai_cau_diem_liet_thi_truot_du_dung_het_cac_cau_khac() {
        List<QuestionWithAnswers> de = new ArrayList<>();
        de.add(cau(1, false, 10, 11));
        de.add(cau(2, true, 20, 21)); // câu điểm liệt
        de.add(cau(3, false, 30, 31));

        Map<Integer, Integer> chon = new HashMap<>();
        chon.put(1, 10); // đúng
        chon.put(2, 21); // SAI câu điểm liệt
        chon.put(3, 30); // đúng

        ExamResult r = ExamScorer.cham(de, chon, /*nguongDat*/ 2);

        assertFalse("Sai câu điểm liệt phải TRƯỢT", r.dat);
        assertTrue(r.truotViDiemLiet);
        assertEquals("Trả lời sai câu điểm liệt số 2", r.lyDoTruot);
    }

    @Test
    public void dung_het_thi_dat() {
        List<QuestionWithAnswers> de = new ArrayList<>();
        de.add(cau(1, false, 10, 11));
        de.add(cau(2, true, 20, 21));

        Map<Integer, Integer> chon = new HashMap<>();
        chon.put(1, 10);
        chon.put(2, 20);

        ExamResult r = ExamScorer.cham(de, chon, 2);

        assertTrue(r.dat);
        assertEquals(2, r.soCauDung);
    }

    @Test
    public void khong_du_nguong_thi_truot() {
        List<QuestionWithAnswers> de = new ArrayList<>();
        de.add(cau(1, false, 10, 11));
        de.add(cau(2, false, 20, 21));
        de.add(cau(3, false, 30, 31));

        Map<Integer, Integer> chon = new HashMap<>();
        chon.put(1, 10); // đúng
        chon.put(2, 21); // sai
        chon.put(3, 31); // sai

        ExamResult r = ExamScorer.cham(de, chon, 2);

        assertFalse(r.dat);
        assertFalse(r.truotViDiemLiet);
        assertEquals(1, r.soCauDung);
    }
}
