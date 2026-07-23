package com.example.oto.data;

/**
 * Cấu hình cấu trúc đề thi hạng B — theo quy chế sát hạch áp dụng từ 01/6/2025
 * (bộ 600 câu của Cục CSGT): mỗi đề 30 câu, làm trong 20 phút, đúng tối thiểu
 * 27/30 câu VÀ không sai câu điểm liệt nào thì ĐẠT.
 */
public final class ExamConfig {

    private ExamConfig() {
    }

    /** Số câu trong một đề. */
    public static final int SO_CAU = 30;

    /** Thời gian làm bài (phút). */
    public static final int THOI_GIAN_PHUT = 20;

    /** Số câu đúng tối thiểu để đạt. */
    public static final int NGUONG_DAT = 27;

    /** Số câu điểm liệt bắt buộc có trong một đề ngẫu nhiên. */
    public static final int SO_CAU_DIEM_LIET = 1;
}
