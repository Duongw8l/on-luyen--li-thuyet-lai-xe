package com.example.oto.data;

import com.example.oto.auth.HoSoNguoiDung;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Repository cho dữ liệu tài khoản trên Firestore (collection "users").
 *
 * Vì sao dữ liệu này KHÔNG nằm trong Room: Room dưới máy chỉ giữ đúng hồ sơ của người
 * đang dùng máy đó. Danh sách toàn bộ tài khoản là dữ liệu của máy chủ, nên màn Quản trị
 * người dùng cần mạng — đây là ngoại lệ có chủ đích với nguyên tắc offline-first, vì nó
 * không thuộc luồng ôn tập/thi.
 *
 * Mọi thao tác ghi ở đây đều được Firestore Security Rules kiểm tra lại phía máy chủ:
 * client không thể tự nâng quyền cho mình dù có sửa mã nguồn. Xem firestore.rules.
 */
public class NguoiDungRepository {

    private static final String COLLECTION = "users";
    private static final String FIELD_VAI_TRO = "vaiTro";

    /** uid của người đang đăng nhập; chuỗi rỗng nếu chưa đăng nhập. */
    public String uidHienTai() {
        FirebaseUser u = FirebaseAuth.getInstance().getCurrentUser();
        return u == null ? "" : u.getUid();
    }

    /**
     * Tải toàn bộ danh sách tài khoản, đã sắp xếp: admin lên đầu, còn lại theo tên.
     * Số tài khoản của một lớp học nhỏ nên tải một lần là đủ, không cần phân trang.
     */
    public void taiDanhSach(Callback<List<HoSoNguoiDung>> khiXong, Callback<String> khiLoi) {
        FirebaseFirestore.getInstance().collection(COLLECTION).get()
                .addOnSuccessListener(snap -> {
                    List<HoSoNguoiDung> ds = new ArrayList<>();
                    for (DocumentSnapshot doc : snap.getDocuments()) {
                        HoSoNguoiDung hoSo = HoSoNguoiDung.tuDocument(doc);
                        if (hoSo != null) {
                            ds.add(hoSo);
                        }
                    }
                    Collections.sort(ds, (a, b) -> {
                        if (a.laAdmin() != b.laAdmin()) {
                            return a.laAdmin() ? -1 : 1;
                        }
                        return a.tenHienThi().compareToIgnoreCase(b.tenHienThi());
                    });
                    khiXong.onResult(ds);
                })
                .addOnFailureListener(e -> khiLoi.onResult(
                        e.getMessage() == null ? "Không tải được danh sách." : e.getMessage()));
    }

    /**
     * Đổi vai trò của một tài khoản khác (nâng lên admin hoặc hạ về người dùng thường).
     *
     * Rules chỉ cho phép khi: người gọi là admin, đối tượng KHÁC chính mình, và chỉ
     * đúng field vaiTro bị thay đổi. Nếu không thoả, Firestore từ chối và callback lỗi
     * được gọi — không có cách nào lách từ phía client.
     */
    public void doiVaiTro(HoSoNguoiDung hoSo, Callback<String> khiXong, Callback<String> khiLoi) {
        String vaiTroMoi = hoSo.vaiTroDoiNguoc();
        FirebaseFirestore.getInstance()
                .collection(COLLECTION).document(hoSo.uid)
                .update(FIELD_VAI_TRO, vaiTroMoi)
                .addOnSuccessListener(v -> khiXong.onResult(vaiTroMoi))
                .addOnFailureListener(e -> khiLoi.onResult(
                        "Không đổi được vai trò. Kiểm tra mạng hoặc quyền quản trị của bạn."));
    }
}
