package com.example.oto.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.oto.MainActivity;
import com.example.oto.R;
import com.example.oto.data.DatabaseSeeder;
import com.example.oto.data.QuizRepository;
import com.example.oto.databinding.ActivityDangNhapBinding;
import com.google.firebase.auth.FirebaseUser;

/**
 * Đăng nhập bằng email + mật khẩu (Firebase Auth).
 * Có lối "dùng offline" để phần ôn tập/thi vẫn chạy được khi không có mạng.
 */
public class DangNhapActivity extends AppCompatActivity {

    private AuthManager auth;
    private QuizRepository repo;
    private ActivityDangNhapBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDangNhapBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setTitle(getString(R.string.dang_nhap));

        auth = new AuthManager();
        repo = new QuizRepository(this);

        binding.btnDangNhap.setOnClickListener(v -> dangNhap());
        binding.btnDangKy.setOnClickListener(v ->
                startActivity(new Intent(this, DangKyActivity.class)));
        binding.btnQuenMatKhau.setOnClickListener(v ->
                startActivity(new Intent(this, QuenMatKhauActivity.class)));
        binding.btnDungOffline.setOnClickListener(v -> {
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
        String email = binding.edtEmail.getText().toString().trim();
        String matKhau = binding.edtMatKhau.getText().toString();
        if (email.isEmpty()) {
            binding.edtEmail.setError(getString(R.string.loi_chua_nhap_email));
            return;
        }
        if (matKhau.isEmpty()) {
            binding.edtMatKhau.setError(getString(R.string.loi_chua_nhap_mat_khau));
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
                Toast.makeText(this, R.string.dang_nhap_thanh_cong, Toast.LENGTH_SHORT).show();
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
                u.getDisplayName() == null ? getString(R.string.ten_hoc_vien) : u.getDisplayName(),
                u.getEmail());
    }

    private void dangXuLy(boolean dang) {
        binding.progress.setVisibility(dang ? View.VISIBLE : View.GONE);
        binding.btnDangNhap.setEnabled(!dang);
    }

    private void vaoTrangChu() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
