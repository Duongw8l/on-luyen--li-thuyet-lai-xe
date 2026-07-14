package com.example.oto.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.oto.MainActivity;
import com.example.oto.R;
import com.example.oto.data.DatabaseSeeder;
import com.example.oto.data.QuizRepository;
import com.google.firebase.auth.FirebaseUser;

/**
 * Đăng nhập bằng email + mật khẩu (Firebase Auth).
 * Có lối "dùng offline" để phần ôn tập/thi vẫn chạy được khi không có mạng.
 */
public class DangNhapActivity extends AppCompatActivity {

    private AuthManager auth;
    private QuizRepository repo;
    private EditText edtEmail, edtMatKhau;
    private ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dang_nhap);
        setTitle(getString(R.string.dang_nhap));

        auth = new AuthManager();
        repo = new QuizRepository(this);

        edtEmail = findViewById(R.id.edtEmail);
        edtMatKhau = findViewById(R.id.edtMatKhau);
        progress = findViewById(R.id.progress);

        findViewById(R.id.btnDangNhap).setOnClickListener(v -> dangNhap());
        findViewById(R.id.btnDangKy).setOnClickListener(v ->
                startActivity(new Intent(this, DangKyActivity.class)));
        findViewById(R.id.btnQuenMatKhau).setOnClickListener(v ->
                startActivity(new Intent(this, QuenMatKhauActivity.class)));
        findViewById(R.id.btnDungOffline).setOnClickListener(v -> {
            // Không đăng nhập -> không có vai trò -> không thấy chức năng quản trị.
            VaiTro.xoa(this);
            vaoTrangChu();
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Đã đăng nhập từ lần trước -> vào thẳng trang chủ.
        if (auth.daDangNhap()) {
            luuHoSoVaoRoom();
            VaiTro.dongBo(this, vaiTro -> {
            });
            vaoTrangChu();
        }
    }

    private void dangNhap() {
        String email = edtEmail.getText().toString().trim();
        String matKhau = edtMatKhau.getText().toString();
        if (email.isEmpty()) {
            edtEmail.setError("Chưa nhập email");
            return;
        }
        if (matKhau.isEmpty()) {
            edtMatKhau.setError("Chưa nhập mật khẩu");
            return;
        }

        dangXuLy(true);
        auth.dangNhap(email, matKhau, (thanhCong, loi) -> {
            if (!thanhCong) {
                dangXuLy(false);
                Toast.makeText(this, loi, Toast.LENGTH_LONG).show();
                return;
            }
            luuHoSoVaoRoom();
            // Đọc vai trò (user/admin) từ Firestore trước khi vào trang chủ,
            // để trang chủ biết có hiện menu quản trị hay không.
            VaiTro.dongBo(this, vaiTro -> {
                dangXuLy(false);
                Toast.makeText(this, "Đăng nhập thành công.", Toast.LENGTH_SHORT).show();
                vaoTrangChu();
            });
        });
    }

    /**
     * Đồng bộ hồ sơ Firebase xuống bảng users của Room, để trang chủ và phần
     * thống kê vẫn đọc được tên/email khi máy không có mạng.
     * Lưu ý: KHÔNG lưu mật khẩu — Firebase giữ, app không bao giờ thấy.
     */
    private void luuHoSoVaoRoom() {
        FirebaseUser u = auth.getUser();
        if (u == null) {
            return;
        }
        repo.capNhatHoSo(DatabaseSeeder.LOCAL_USER_ID,
                u.getDisplayName() == null ? "Học viên" : u.getDisplayName(),
                u.getEmail());
    }

    private void dangXuLy(boolean dang) {
        progress.setVisibility(dang ? View.VISIBLE : View.GONE);
        findViewById(R.id.btnDangNhap).setEnabled(!dang);
    }

    private void vaoTrangChu() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
