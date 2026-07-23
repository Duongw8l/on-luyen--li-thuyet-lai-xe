package com.example.oto.data;

import com.example.oto.data.entity.Answer;
import com.example.oto.data.entity.Question;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Chuyển đổi câu hỏi giữa entity Room và "document" Firestore (một {@code Map}).
 *
 * TÁCH RIÊNG khỏi {@link QuestionSyncRepository} để viết unit test THUẦN JAVA được —
 * cùng lý do như {@code ExamScorer}: lớp này không đụng Android hay Firebase, chỉ thao
 * tác trên Map/entity nên chạy được trên JVM mà không cần thiết bị.
 *
 * Firestore trả số về dạng {@link Long}/{@link Double}, nên các hàm đọc ở đây nhận
 * {@link Number} cho chắc, không giả định đúng kiểu.
 */
public final class QuestionFirestoreMapper {

    // Tên các trường trong document Firestore (dùng chung cho cả ghi và đọc).
    public static final String F_CHAPTER_ID = "chapterId";
    public static final String F_NOI_DUNG = "noiDung";
    public static final String F_ANH_URL = "anhUrl";
    /**
     * Ảnh câu hỏi mã hoá Base64, nhúng thẳng trong document (câu do admin thêm mới).
     * Việc mã hoá/giải mã ảnh cần Android I/O nên KHÔNG làm trong lớp thuần này —
     * QuestionSyncRepository lo phần đó; ở đây chỉ khai báo tên trường để dùng chung.
     */
    public static final String F_ANH_DATA = "anhData";
    public static final String F_DIEM_LIET = "isDiemLiet";
    public static final String F_GIAI_THICH = "giaiThich";
    public static final String F_UPDATED_AT = "updatedAt";
    public static final String F_ANSWERS = "answers";
    public static final String F_A_NOI_DUNG = "noiDung";
    public static final String F_A_IS_CORRECT = "isCorrect";

    private QuestionFirestoreMapper() {
    }

    /** Entity Room -> document Firestore. Đáp án được nhúng thành mảng con. */
    public static Map<String, Object> toDocument(Question q, List<Answer> answers) {
        Map<String, Object> m = new HashMap<>();
        m.put(F_CHAPTER_ID, q.chapterId);
        m.put(F_NOI_DUNG, q.noiDung);
        m.put(F_ANH_URL, q.anhUrl);
        m.put(F_DIEM_LIET, q.isDiemLiet);
        m.put(F_GIAI_THICH, q.giaiThich);
        m.put(F_UPDATED_AT, q.updatedAt);

        List<Map<String, Object>> ds = new ArrayList<>();
        if (answers != null) {
            for (Answer a : answers) {
                Map<String, Object> ma = new HashMap<>();
                ma.put(F_A_NOI_DUNG, a.noiDung);
                ma.put(F_A_IS_CORRECT, a.isCorrect);
                ds.add(ma);
            }
        }
        m.put(F_ANSWERS, ds);
        return m;
    }

    /**
     * Document Firestore -> Question. id lấy từ TÊN document.
     *
     * Trả về {@code null} (bỏ qua câu này) nếu dữ liệu không dùng được:
     * thiếu chapterId / noiDung, hoặc tên document không phải số.
     */
    public static Question toQuestion(String docId, Map<String, Object> data) {
        if (data == null) {
            return null;
        }
        Object chapterId = data.get(F_CHAPTER_ID);
        Object noiDung = data.get(F_NOI_DUNG);
        if (!(chapterId instanceof Number) || !(noiDung instanceof String)) {
            return null;
        }
        int id;
        try {
            id = Integer.parseInt(docId);
        } catch (NumberFormatException e) {
            return null;
        }

        Question q = new Question();
        q.id = id;
        q.chapterId = ((Number) chapterId).intValue();
        q.noiDung = (String) noiDung;
        q.anhUrl = asString(data.get(F_ANH_URL));
        q.isDiemLiet = Boolean.TRUE.equals(data.get(F_DIEM_LIET));
        q.giaiThich = asString(data.get(F_GIAI_THICH));
        Object updatedAt = data.get(F_UPDATED_AT);
        q.updatedAt = updatedAt instanceof Number ? ((Number) updatedAt).longValue() : 0L;
        return q;
    }

    /** Đọc mảng đáp án trong document. Phần tử hỏng/thiếu thì bỏ qua, không ném lỗi. */
    @SuppressWarnings("unchecked")
    public static List<Answer> toAnswers(Map<String, Object> data, int questionId) {
        List<Answer> ds = new ArrayList<>();
        if (data == null) {
            return ds;
        }
        Object raw = data.get(F_ANSWERS);
        if (!(raw instanceof List)) {
            return ds;
        }
        for (Object o : (List<Object>) raw) {
            if (!(o instanceof Map)) {
                continue;
            }
            Map<String, Object> m = (Map<String, Object>) o;
            String noiDung = asString(m.get(F_A_NOI_DUNG));
            ds.add(new Answer(
                    questionId,
                    noiDung == null ? "" : noiDung,
                    Boolean.TRUE.equals(m.get(F_A_IS_CORRECT))));
        }
        return ds;
    }

    private static String asString(Object o) {
        return o == null ? null : o.toString();
    }
}
