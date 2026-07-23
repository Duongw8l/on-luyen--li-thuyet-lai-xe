package com.example.oto.auth;

import androidx.annotation.Nullable;

import com.google.firebase.firestore.DocumentSnapshot;

/**
 * Hồ sơ một người dùng đọc từ Firestore users/{uid}.
 *
 * Chỉ dùng cho màn Quản trị người dùng. Hồ sơ của chính người đang đăng nhập thì
 * app đọc thẳng từ FirebaseAuth / Room, không cần lớp này.
 */
public class HoSoNguoiDung {

    public final String uid;
    public final String hoTen;
    public final String email;
    public final String vaiTro;
    public final long ngayTao;

    public HoSoNguoiDung(String uid, String hoTen, String email, String vaiTro, long ngayTao) {
        this.uid = uid;
        this.hoTen = hoTen == null ? "" : hoTen;
        this.email = email == null ? "" : email;
        this.vaiTro = VaiTro.ADMIN.equals(vaiTro) ? VaiTro.ADMIN : VaiTro.USER;
        this.ngayTao = ngayTao;
    }

    /** Đọc một document trong collection users. Trả về null nếu document không đủ dữ liệu. */
    @Nullable
    public static HoSoNguoiDung tuDocument(DocumentSnapshot doc) {
        if (!doc.exists()) {
            return null;
        }
        Long ngayTao = doc.getLong("ngayTao");
        return new HoSoNguoiDung(
                doc.getId(),
                doc.getString("hoTen"),
                doc.getString("email"),
                doc.getString("vaiTro"),
                ngayTao == null ? 0L : ngayTao);
    }

    public boolean laAdmin() {
        return VaiTro.ADMIN.equals(vaiTro);
    }

    /** Tài khoản chưa đặt họ tên thì hiện email cho đỡ trống. */
    public String tenHienThi() {
        return hoTen.isEmpty() ? email : hoTen;
    }

    /** Vai trò sau khi bấm đổi: user <-> admin. */
    public String vaiTroDoiNguoc() {
        return laAdmin() ? VaiTro.USER : VaiTro.ADMIN;
    }

    public boolean khop(String tuKhoa) {
        String k = tuKhoa.toLowerCase();
        return hoTen.toLowerCase().contains(k) || email.toLowerCase().contains(k);
    }
}
