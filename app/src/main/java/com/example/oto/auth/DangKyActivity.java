package com.example.oto.auth;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.oto.R;
import com.example.oto.databinding.ActivityDangKyBinding;

/**
 * Đăng ký tài khoản mới. Sau khi tạo xong, Firebase gửi email xác minh
 * tới địa chỉ vừa đăng ký (tiêu chí 2.5, mục 2).
 */
public class DangKyActivity extends AppCompatActivity {

    private AuthManager auth;
    private ActivityDangKyBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDangKyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setTitle(getString(R.string.dang_ky));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        auth = new AuthManager();

        binding.btnDangKy.setOnClickListener(v -> dangKy());
    }

    private void dangKy() {
        String hoTen = binding.edtHoTen.getText().toString().trim();
        String email = binding.edtEmail.getText().toString().trim();
        String matKhau = binding.edtMatKhau.getText().toString();
        String nhapLai = binding.edtNhapLai.getText().toString();

        if (hoTen.isEmpty()) {
            binding.edtHoTen.setError(getString(R.string.loi_chua_nhap_ho_ten));
            return;
        }
        if (email.isEmpty()) {
            binding.edtEmail.setError(getString(R.string.loi_chua_nhap_email));
            return;
        }
        if (matKhau.length() < 6) {
            binding.edtMatKhau.setError(getString(R.string.loi_mat_khau_ngan));
            return;
        }
        if (!matKhau.equals(nhapLai)) {
            binding.edtNhapLai.setError(getString(R.string.loi_mat_khau_khong_khop));
            return;
        }

        dangXuLy(true);
        auth.dangKy(hoTen, email, matKhau, (thanhCong, daGuiEmail, loi) -> {
            dangXuLy(false);
            if (!thanhCong) {
                Toast.makeText(this, loi, Toast.LENGTH_LONG).show();
                return;
            }
            // Tài khoản mới luôn là người dùng thường; hồ sơ được tạo trên Firestore.
            com.google.firebase.auth.FirebaseUser u = auth.getUser();
            if (u != null) {
                VaiTro.taoHoSoMoi(this, u, null);
            }

            if (daGuiEmail) {
                new AlertDialog.Builder(this)
                        .setTitle(R.string.dang_ky_thanh_cong)
                        .setMessage(getString(R.string.da_gui_email_xac_minh, email))
                        .setPositiveButton(R.string.da_hieu, (d, w) -> finish())
                        .setCancelable(false)
                        .show();
                return;
            }

            // Tài khoản đã tạo nhưng email chưa gửi được. Nói thật và cho gửi lại ngay,
            // nếu không người dùng sẽ kẹt ở trạng thái chưa xác minh mà không hiểu vì sao.
            new AlertDialog.Builder(this)
                    .setTitle(R.string.tieu_de_chua_gui_duoc_email)
                    .setMessage(getString(R.string.noi_dung_chua_gui_duoc_email,
                            email, loi == null ? "." : (": " + loi)))
                    .setPositiveButton(R.string.gui_lai, (d, w) -> guiLaiEmail(email))
                    .setNegativeButton(R.string.de_sau, (d, w) -> finish())
                    .setCancelable(false)
                    .show();
        });
    }

    /** Thử gửi lại email xác minh ngay sau khi đăng ký; hỏng nữa thì cho thử tiếp. */
    private void guiLaiEmail(String email) {
        dangXuLy(true);
        auth.guiLaiEmailXacMinh((thanhCong, loi) -> {
            dangXuLy(false);
            if (thanhCong) {
                Toast.makeText(this, getString(R.string.da_gui_email_xac_minh_ngan, email),
                        Toast.LENGTH_LONG).show();
                finish();
                return;
            }
            new AlertDialog.Builder(this)
                    .setTitle(R.string.tieu_de_van_chua_gui_duoc)
                    .setMessage(loi == null ? getString(R.string.loi_khong_gui_duoc_xac_minh) : loi)
                    .setPositiveButton(R.string.thu_lai, (d, w) -> guiLaiEmail(email))
                    .setNegativeButton(R.string.de_sau, (d, w) -> finish())
                    .setCancelable(false)
                    .show();
        });
    }

    private void dangXuLy(boolean dang) {
        binding.progress.setVisibility(dang ? View.VISIBLE : View.GONE);
        binding.btnDangKy.setEnabled(!dang);
    }
}
