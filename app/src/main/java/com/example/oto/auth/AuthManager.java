package com.example.oto.auth;

import androidx.annotation.Nullable;

import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

/**
 * Bọc Firebase Authentication lại cho gọn (tiêu chí 1.3 và 2.5).
 *
 * Vì sao dùng Firebase Auth: mật khẩu KHÔNG bao giờ được lưu trong app hay trong
 * database của nhóm — Firebase giữ và băm phía máy chủ. Firebase cũng lo sẵn hạ tầng
 * gửi email đặt lại mật khẩu và email xác minh, nên nhóm không phải dựng SMTP server.
 */
public final class AuthManager {

    /** Kết quả một thao tác đăng nhập/đăng ký/gửi email. */
    public interface Callback {
        void onKetQua(boolean thanhCong, @Nullable String loi);
    }

    /**
     * Kết quả đăng ký. Tách riêng vì tài khoản có thể tạo xong nhưng hai bước sau
     * (lưu họ tên, gửi email xác minh) lại hỏng — ví dụ rớt mạng giữa chừng. Khi đó
     * không được báo "đã gửi email", vì email chưa hề đi.
     */
    public interface CallbackDangKy {
        void onKetQua(boolean thanhCong, boolean daGuiEmailXacMinh, @Nullable String loi);
    }

    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    @Nullable
    public FirebaseUser getUser() {
        return auth.getCurrentUser();
    }

    public boolean daDangNhap() {
        return auth.getCurrentUser() != null;
    }

    public void dangNhap(String email, String matKhau, Callback cb) {
        auth.signInWithEmailAndPassword(email, matKhau)
                .addOnCompleteListener(task ->
                        cb.onKetQua(task.isSuccessful(), thongBaoLoi(task.getException())));
    }

    /**
     * Đăng ký tài khoản mới: tạo tài khoản, lưu họ tên vào hồ sơ, rồi gửi
     * email xác minh (tiêu chí 2.5, mục 2).
     */
    public void dangKy(String hoTen, String email, String matKhau, CallbackDangKy cb) {
        auth.createUserWithEmailAndPassword(email, matKhau)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        cb.onKetQua(false, false, thongBaoLoi(task.getException()));
                        return;
                    }
                    FirebaseUser user = auth.getCurrentUser();
                    if (user == null) {
                        cb.onKetQua(false, false, "Không lấy được thông tin tài khoản.");
                        return;
                    }
                    UserProfileChangeRequest hoSo = new UserProfileChangeRequest.Builder()
                            .setDisplayName(hoTen)
                            .build();
                    // Tài khoản đã tạo xong rồi, nên dù hai bước dưới có hỏng vẫn báo
                    // thanhCong = true; chỉ nói rõ email xác minh chưa gửi được.
                    user.updateProfile(hoSo).addOnCompleteListener(tHoSo ->
                            user.sendEmailVerification().addOnCompleteListener(tMail ->
                                    cb.onKetQua(true, tMail.isSuccessful(),
                                            tMail.isSuccessful()
                                                    ? null
                                                    : thongBaoLoi(tMail.getException()))));
                });
    }

    /** Gửi email đặt lại mật khẩu (tiêu chí 2.5, mục 1). */
    public void guiEmailDatLaiMatKhau(String email, Callback cb) {
        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task ->
                        cb.onKetQua(task.isSuccessful(), thongBaoLoi(task.getException())));
    }

    /** Gửi lại email xác minh cho tài khoản đang đăng nhập. */
    public void guiLaiEmailXacMinh(Callback cb) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            cb.onKetQua(false, "Chưa đăng nhập.");
            return;
        }
        user.sendEmailVerification()
                .addOnCompleteListener(task ->
                        cb.onKetQua(task.isSuccessful(), thongBaoLoi(task.getException())));
    }

    public void dangXuat() {
        auth.signOut();
    }

    /** Đổi thông báo lỗi kỹ thuật của Firebase sang câu tiếng Việt dễ hiểu. */
    @Nullable
    private String thongBaoLoi(@Nullable Exception e) {
        if (e == null) {
            return null;
        }
        String msg = e.getMessage() == null ? "" : e.getMessage();
        if (msg.contains("password is invalid") || msg.contains("INVALID_LOGIN_CREDENTIALS")
                || msg.contains("supplied auth credential is incorrect")) {
            return "Email hoặc mật khẩu không đúng.";
        }
        if (msg.contains("no user record") || msg.contains("USER_NOT_FOUND")) {
            return "Chưa có tài khoản nào dùng email này.";
        }
        if (msg.contains("email address is already in use")) {
            return "Email này đã được đăng ký.";
        }
        if (msg.contains("badly formatted")) {
            return "Email không hợp lệ.";
        }
        if (msg.contains("at least 6 characters")) {
            return "Mật khẩu phải có ít nhất 6 ký tự.";
        }
        if (msg.contains("unusual activity") || msg.contains("TOO_MANY_ATTEMPTS")
                || msg.contains("too-many-requests")) {
            return "Đã gửi quá nhiều lần. Hãy đợi vài phút rồi thử lại.";
        }
        // Mất mạng giữa chừng có khi hiện ra dưới dạng FirebaseNetworkException, có khi chỉ là
        // "internal error ... connection abort" — bắt cả hai kẻo lọt câu tiếng Anh ra màn hình.
        if (e instanceof FirebaseNetworkException
                || msg.contains("network error") || msg.contains("Unable to resolve host")
                || msg.contains("connection abort") || msg.contains("Connection reset")
                || msg.contains("Unable to connect") || msg.contains("timeout")) {
            return "Không có mạng. Đăng nhập cần Internet (phần ôn tập vẫn dùng offline được).";
        }
        return msg;
    }
}
