package com.example.oto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.example.oto.data.QuestionFirestoreMapper;
import com.example.oto.data.entity.Answer;
import com.example.oto.data.entity.Question;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Kiểm thử THUẦN JAVA phần chuyển đổi câu hỏi Room <-> document Firestore.
 *
 * Không cần Firebase/Android vì {@link QuestionFirestoreMapper} chỉ thao tác trên
 * Map/entity — đây chính là lý do phần này được tách riêng khỏi QuestionSyncRepository.
 */
public class QuestionFirestoreMapperTest {

    private Question mauCauHoi() {
        Question q = new Question();
        q.id = 42;
        q.chapterId = 3;
        q.noiDung = "Khi nào được vượt xe?";
        q.anhUrl = "bien_101.png";
        q.isDiemLiet = true;
        q.giaiThich = "Giải thích mẫu";
        q.updatedAt = 1_700_000_000_000L;
        return q;
    }

    private List<Answer> mauDapAn() {
        return Arrays.asList(
                new Answer(42, "Đáp án A", false),
                new Answer(42, "Đáp án B đúng", true),
                new Answer(42, "Đáp án C", false));
    }

    // ---------- toDocument ----------

    @Test
    public void toDocument_ghi_du_cac_truong_va_dap_an() {
        Map<String, Object> doc = QuestionFirestoreMapper.toDocument(mauCauHoi(), mauDapAn());

        assertEquals(3, doc.get(QuestionFirestoreMapper.F_CHAPTER_ID));
        assertEquals("Khi nào được vượt xe?", doc.get(QuestionFirestoreMapper.F_NOI_DUNG));
        assertEquals("bien_101.png", doc.get(QuestionFirestoreMapper.F_ANH_URL));
        assertEquals(true, doc.get(QuestionFirestoreMapper.F_DIEM_LIET));
        assertEquals(1_700_000_000_000L, doc.get(QuestionFirestoreMapper.F_UPDATED_AT));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> answers =
                (List<Map<String, Object>>) doc.get(QuestionFirestoreMapper.F_ANSWERS);
        assertEquals(3, answers.size());
        assertEquals("Đáp án B đúng", answers.get(1).get(QuestionFirestoreMapper.F_A_NOI_DUNG));
        assertEquals(true, answers.get(1).get(QuestionFirestoreMapper.F_A_IS_CORRECT));
        assertEquals(false, answers.get(0).get(QuestionFirestoreMapper.F_A_IS_CORRECT));
    }

    @Test
    public void toDocument_dap_an_null_thi_thanh_mang_rong() {
        Map<String, Object> doc = QuestionFirestoreMapper.toDocument(mauCauHoi(), null);

        @SuppressWarnings("unchecked")
        List<Object> answers = (List<Object>) doc.get(QuestionFirestoreMapper.F_ANSWERS);
        assertTrue(answers.isEmpty());
    }

    // ---------- Đi vòng: Room -> document -> Room ----------

    @Test
    public void di_vong_giu_nguyen_du_lieu() {
        Question goc = mauCauHoi();
        Map<String, Object> doc = QuestionFirestoreMapper.toDocument(goc, mauDapAn());

        // Firestore trả số nguyên về dạng Long -> mô phỏng lại để test sát thực tế.
        doc.put(QuestionFirestoreMapper.F_CHAPTER_ID, 3L);

        Question q = QuestionFirestoreMapper.toQuestion(String.valueOf(goc.id), doc);
        List<Answer> dapAn = QuestionFirestoreMapper.toAnswers(doc, goc.id);

        assertEquals(goc.id, q.id);
        assertEquals(goc.chapterId, q.chapterId);
        assertEquals(goc.noiDung, q.noiDung);
        assertEquals(goc.anhUrl, q.anhUrl);
        assertEquals(goc.isDiemLiet, q.isDiemLiet);
        assertEquals(goc.giaiThich, q.giaiThich);
        assertEquals(goc.updatedAt, q.updatedAt);

        assertEquals(3, dapAn.size());
        assertEquals(goc.id, dapAn.get(0).questionId);
        assertTrue(dapAn.get(1).isCorrect);
        assertFalse(dapAn.get(0).isCorrect);
    }

    // ---------- toQuestion: dữ liệu hỏng bị bỏ qua (null) ----------

    @Test
    public void toQuestion_thieu_chapterId_tra_null() {
        Map<String, Object> doc = new HashMap<>();
        doc.put(QuestionFirestoreMapper.F_NOI_DUNG, "Có nội dung nhưng thiếu chương");
        assertNull(QuestionFirestoreMapper.toQuestion("5", doc));
    }

    @Test
    public void toQuestion_thieu_noiDung_tra_null() {
        Map<String, Object> doc = new HashMap<>();
        doc.put(QuestionFirestoreMapper.F_CHAPTER_ID, 2L);
        assertNull(QuestionFirestoreMapper.toQuestion("5", doc));
    }

    @Test
    public void toQuestion_ten_document_khong_phai_so_tra_null() {
        Map<String, Object> doc = new HashMap<>();
        doc.put(QuestionFirestoreMapper.F_CHAPTER_ID, 2L);
        doc.put(QuestionFirestoreMapper.F_NOI_DUNG, "Nội dung");
        assertNull(QuestionFirestoreMapper.toQuestion("abc", doc));
    }

    @Test
    public void toQuestion_data_null_tra_null() {
        assertNull(QuestionFirestoreMapper.toQuestion("5", null));
    }

    @Test
    public void toQuestion_thieu_truong_khong_bat_buoc_van_dung() {
        // Chỉ có chương + nội dung; các trường còn lại vắng mặt.
        Map<String, Object> doc = new HashMap<>();
        doc.put(QuestionFirestoreMapper.F_CHAPTER_ID, 1L);
        doc.put(QuestionFirestoreMapper.F_NOI_DUNG, "Câu tối thiểu");

        Question q = QuestionFirestoreMapper.toQuestion("7", doc);
        assertEquals(7, q.id);
        assertEquals(1, q.chapterId);
        assertFalse(q.isDiemLiet);   // mặc định false khi vắng mặt
        assertNull(q.anhUrl);
        assertNull(q.giaiThich);
        assertEquals(0L, q.updatedAt); // mặc định 0 khi vắng mặt
    }

    // ---------- toAnswers: chịu được dữ liệu méo ----------

    @Test
    public void toAnswers_khong_co_mang_tra_rong() {
        Map<String, Object> doc = new HashMap<>();
        assertTrue(QuestionFirestoreMapper.toAnswers(doc, 1).isEmpty());
        assertTrue(QuestionFirestoreMapper.toAnswers(null, 1).isEmpty());
    }

    @Test
    public void toAnswers_bo_qua_phan_tu_hong() {
        List<Object> mang = new ArrayList<>();
        mang.add("chuỗi rác không phải map"); // phần tử hỏng -> bỏ qua
        Map<String, Object> hople = new HashMap<>();
        hople.put(QuestionFirestoreMapper.F_A_NOI_DUNG, "Đáp án hợp lệ");
        hople.put(QuestionFirestoreMapper.F_A_IS_CORRECT, true);
        mang.add(hople);

        Map<String, Object> doc = new HashMap<>();
        doc.put(QuestionFirestoreMapper.F_ANSWERS, mang);

        List<Answer> ds = QuestionFirestoreMapper.toAnswers(doc, 9);
        assertEquals(1, ds.size());
        assertEquals("Đáp án hợp lệ", ds.get(0).noiDung);
        assertTrue(ds.get(0).isCorrect);
        assertEquals(9, ds.get(0).questionId);
    }
}
