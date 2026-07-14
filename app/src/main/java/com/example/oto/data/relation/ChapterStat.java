package com.example.oto.data.relation;

/** Kết quả thống kê tỷ lệ đúng theo một chương. */
public class ChapterStat {
    public int chapterId;
    public int soThuTu;
    public String tenChuong;
    public int tong;
    public int dung;

    public int phanTram() {
        return tong == 0 ? 0 : Math.round(dung * 100f / tong);
    }
}
