package com.example.oto.data;

/**
 * Cấu hình cấu trúc đề thi hạng B.
 *
 * ⚠️ CẦN XÁC MINH LẠI từ nguồn chính thức của Cục CSGT trước khi bảo vệ:
 * số câu mỗi đề, thời gian làm bài, số câu đúng tối thiểu để đạt, số câu điểm liệt.
 * Các nguồn trên mạng không thống nhất — để tất cả thông số ở một chỗ cho dễ sửa.
 */
public final class ExamConfig {

    private ExamConfig() {
    }

    /** Số câu trong một đề (đề ngẫu nhiên). Giá trị mẫu — CHỜ XÁC MINH. */
    public static final int SO_CAU = 25;

    /** Thời gian làm bài (phút). Giá trị mẫu — CHỜ XÁC MINH. */
    public static final int THOI_GIAN_PHUT = 19;

    /** Số câu đúng tối thiểu để đạt. Giá trị mẫu — CHỜ XÁC MINH. */
    public static final int NGUONG_DAT = 21;

    /** Số câu điểm liệt bắt buộc có trong một đề ngẫu nhiên. */
    public static final int SO_CAU_DIEM_LIET = 1;
}
