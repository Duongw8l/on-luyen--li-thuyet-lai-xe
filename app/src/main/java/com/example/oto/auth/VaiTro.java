package com.example.oto.auth;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * Phân quyền người dùng (user / admin).
 *
 * Vai trò được lưu trên Firestore tại users/{uid}. Firestore Security Rules không
 * cho người dùng tự sửa field vaiTro của mình, nên người dùng thường KHÔNG THỂ
 * tự nâng mình lên admin — kể cả khi họ sửa app. Việc cấp quyền admin chỉ làm được
 * từ Firebase Console.
 *
 * Vai trò đọc được sẽ được nhớ lại trong SharedPreferences để app biết quyền ngay
 * cả khi mở lại lúc không có mạng.
 */
public final class VaiTro {

    public static final String USER = "user";
    public static final String ADMIN = "admin";

    private static final String PREF = "phien_nguoi_dung";
    private static final String KEY_VAI_TRO = "vai_tro";

    /** Kết quả tra vai trò. */
    public interface Callback {
        void onVaiTro(String vaiTro);
    }

    private VaiTro() {
    }

    /**
     * Đọc vai trò từ Firestore; nếu người dùng chưa có hồ sơ (tài khoản tạo trước khi
     * có tính năng này) thì tạo hồ sơ mới với vai trò "user".
     * Không có mạng thì dùng lại vai trò đã nhớ trong máy.
     */
    public static void dongBo(Context context, Callback cb) {
        FirebaseUser u = FirebaseAuth.getInstance().getCurrentUser();
        if (u == null) {
            luu(context, USER);
            cb.onVaiTro(USER);
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(u.getUid()).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists() && doc.getString("vaiTro") != null) {
                        String vaiTro = doc.getString("vaiTro");
                        luu(context, vaiTro);
                        cb.onVaiTro(vaiTro);
                    } else {
                        taoHoSoMoi(context, u, cb);
                    }
                })
                .addOnFailureListener(e -> {
                    // Mất mạng hoặc bị Rules chặn -> dùng vai trò đã nhớ (mặc định "user").
                    cb.onVaiTro(layTuMay(context));
                });
    }

    /** Tạo hồ sơ Firestore cho tài khoản mới. Luôn là "user" — Rules cũng bắt buộc vậy. */
    public static void taoHoSoMoi(Context context, FirebaseUser u, @Nullable Callback cb) {
        Map<String, Object> hoSo = new HashMap<>();
        hoSo.put("hoTen", u.getDisplayName() == null ? "" : u.getDisplayName());
        hoSo.put("email", u.getEmail());
        hoSo.put("vaiTro", USER);
        hoSo.put("ngayTao", System.currentTimeMillis());

        FirebaseFirestore.getInstance()
                .collection("users").document(u.getUid())
                .set(hoSo)
                .addOnCompleteListener(t -> {
                    luu(context, USER);
                    if (cb != null) {
                        cb.onVaiTro(USER);
                    }
                });
    }

    /** Vai trò đã nhớ trong máy — dùng được ngay, kể cả offline. */
    public static String layTuMay(Context context) {
        return prefs(context).getString(KEY_VAI_TRO, USER);
    }

    public static boolean laAdmin(Context context) {
        return ADMIN.equals(layTuMay(context));
    }

    /**
     * Chốt chặn thật của mọi màn hình quản trị: không phải admin thì đóng màn hình ngay.
     * Gọi ở đầu onCreate. Trả về true nếu đã chặn (activity đang bị đóng).
     */
    public static boolean chanNeuKhongPhaiAdmin(android.app.Activity activity) {
        if (laAdmin(activity)) {
            return false;
        }
        android.widget.Toast.makeText(activity,
                "Chức năng này chỉ dành cho quản trị viên.",
                android.widget.Toast.LENGTH_LONG).show();
        activity.finish();
        return true;
    }

    public static void luu(Context context, String vaiTro) {
        prefs(context).edit().putString(KEY_VAI_TRO, vaiTro).apply();
    }

    /** Đăng xuất -> quên vai trò, tránh người sau dùng máy vẫn thấy menu quản trị. */
    public static void xoa(Context context) {
        prefs(context).edit().remove(KEY_VAI_TRO).apply();
    }

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }
}
