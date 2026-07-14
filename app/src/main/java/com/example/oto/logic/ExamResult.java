package com.example.oto.logic;

/** Kết quả chấm một bài thi. */
public class ExamResult {

    public boolean dat;
    public int soCauDung;
    public int tongSoCau;
    /** Lý do trượt; rỗng nếu đạt. */
    public String lyDoTruot = "";
    /** true nếu trượt vì sai câu điểm liệt (để UI hiển thị nổi bật). */
    public boolean truotViDiemLiet;

    public String ketQuaText() {
        return dat ? "ĐẠT" : "TRƯỢT";
    }
}
