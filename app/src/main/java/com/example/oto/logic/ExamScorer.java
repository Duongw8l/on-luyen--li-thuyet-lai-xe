package com.example.oto.logic;

import com.example.oto.data.relation.QuestionWithAnswers;

import java.util.List;
import java.util.Map;

/**
 * Thuật toán chấm điểm ĐÚNG QUY CHẾ thi sát hạch hạng B (mục 2.2 tài liệu thiết kế).
 *
 * Gồm hai bước, thứ tự KHÔNG được đảo:
 *   Bước 1 — sai BẤT KỲ câu điểm liệt nào  → TRƯỢT ngay (dừng, không xét tiếp).
 *   Bước 2 — số câu đúng >= ngưỡng đạt      → ĐẠT, ngược lại TRƯỢT.
 *
 * Tách khỏi Android để có thể viết unit test thuần Java.
 */
public final class ExamScorer {

    private ExamScorer() {
    }

    /**
     * @param questions       danh sách câu hỏi trong đề (kèm đáp án), theo đúng thứ tự hiển thị
     * @param chosenAnswerIds map: questionId -> answerId người dùng đã chọn
     *                        (thiếu key hoặc giá trị 0 nghĩa là chưa trả lời)
     * @param nguongDat       số câu đúng tối thiểu để đạt
     */
    public static ExamResult cham(List<QuestionWithAnswers> questions,
                                  Map<Integer, Integer> chosenAnswerIds,
                                  int nguongDat) {
        ExamResult r = new ExamResult();
        r.tongSoCau = questions.size();

        // ----- Bước 1: kiểm tra câu điểm liệt -----
        for (int i = 0; i < questions.size(); i++) {
            QuestionWithAnswers qa = questions.get(i);
            if (!qa.question.isDiemLiet) {
                continue;
            }
            Integer chosen = chosenAnswerIds.get(qa.question.id);
            boolean dung = chosen != null && chosen == qa.correctAnswerId() && chosen != 0;
            if (!dung) {
                r.dat = false;
                r.truotViDiemLiet = true;
                r.lyDoTruot = "Trả lời sai câu điểm liệt số " + (i + 1);
                // Vẫn đếm số câu đúng để hiển thị, nhưng kết quả đã là TRƯỢT.
                r.soCauDung = demSoCauDung(questions, chosenAnswerIds);
                return r;
            }
        }

        // ----- Bước 2: xét ngưỡng điểm -----
        r.soCauDung = demSoCauDung(questions, chosenAnswerIds);
        if (r.soCauDung >= nguongDat) {
            r.dat = true;
            r.lyDoTruot = "";
        } else {
            r.dat = false;
            r.lyDoTruot = "Chỉ đúng " + r.soCauDung + "/" + r.tongSoCau
                    + " câu, chưa đạt ngưỡng " + nguongDat + " câu.";
        }
        return r;
    }

    private static int demSoCauDung(List<QuestionWithAnswers> questions,
                                    Map<Integer, Integer> chosenAnswerIds) {
        int dung = 0;
        for (QuestionWithAnswers qa : questions) {
            Integer chosen = chosenAnswerIds.get(qa.question.id);
            if (chosen != null && chosen != 0 && chosen == qa.correctAnswerId()) {
                dung++;
            }
        }
        return dung;
    }
}
