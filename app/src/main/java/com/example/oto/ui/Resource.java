package com.example.oto.ui;

import androidx.annotation.Nullable;

/**
 * Trạng thái của một lần lấy dữ liệu, phát qua LiveData cho View.
 *
 * Vì sao cần class này: nếu ViewModel chỉ phát ra {@code LiveData<List<T>>} thì View
 * không phân biệt được ba tình huống khác nhau — "đang tải", "tải xong nhưng rỗng",
 * và "tải lỗi" — cả ba đều là danh sách rỗng hoặc null. Resource gói dữ liệu kèm
 * trạng thái nên View biết chính xác cần hiện vòng quay, hiện danh sách, hay hiện lỗi.
 *
 * Cách dùng ở View:
 * <pre>
 * viewModel.getDanhSach().observe(this, res -&gt; {
 *     if (res.laDangTai())   { ... hiện ProgressBar ... }
 *     if (res.laThanhCong()) { adapter.submitList(res.duLieu); }
 *     if (res.laLoi())       { hienLoi(res.thongBaoLoi); }
 * });
 * </pre>
 *
 * @param <T> kiểu dữ liệu trả về khi thành công
 */
public class Resource<T> {

    public enum TrangThai {
        DANG_TAI,
        THANH_CONG,
        LOI
    }

    public final TrangThai trangThai;

    @Nullable
    public final T duLieu;

    @Nullable
    public final String thongBaoLoi;

    private Resource(TrangThai trangThai, @Nullable T duLieu, @Nullable String thongBaoLoi) {
        this.trangThai = trangThai;
        this.duLieu = duLieu;
        this.thongBaoLoi = thongBaoLoi;
    }

    public static <T> Resource<T> dangTai() {
        return new Resource<>(TrangThai.DANG_TAI, null, null);
    }

    public static <T> Resource<T> thanhCong(@Nullable T duLieu) {
        return new Resource<>(TrangThai.THANH_CONG, duLieu, null);
    }

    public static <T> Resource<T> loi(String thongBao) {
        return new Resource<>(TrangThai.LOI, null, thongBao);
    }

    // Các hàm kiểm tra trạng thái có tiền tố "la" để không trùng chữ ký với ba hàm
    // tạo tĩnh ở trên (Java coi dangTai() tĩnh và dangTai() thường là trùng tên).

    public boolean laDangTai() {
        return trangThai == TrangThai.DANG_TAI;
    }

    public boolean laThanhCong() {
        return trangThai == TrangThai.THANH_CONG;
    }

    public boolean laLoi() {
        return trangThai == TrangThai.LOI;
    }
}
