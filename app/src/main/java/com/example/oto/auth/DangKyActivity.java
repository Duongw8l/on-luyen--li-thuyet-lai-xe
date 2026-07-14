package com.example.oto.auth;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.oto.R;

/**
 * Đăng ký tài khoản mới. Sau khi tạo xong, Firebase gửi email xác minh
 * tới địa chỉ vừa đăng ký (tiêu chí 2.5, mục 2).
 */
public class DangKyActivity extends AppCompatActivity {

    private AuthManager auth;
    private EditText edtHoTen, edtEmail, edtMatKhau, edtNhapLai;
    private ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dang_ky);
        setTitle(getString(R.string.dang_ky));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        auth = new AuthManager();

        edtHoTen = findViewById(R.id.edtHoTen);
        edtEmail = findViewById(R.id.edtEmail);
        edtMatKhau = findViewById(R.id.edtMatKhau);
        edtNhapLai = findViewById(R.id.edtNhapLai);
        progress = findViewById(R.id.progress);

        findViewById(R.id.btnDangKy).setOnClickListener(v -> dangKy());
    }

    private void dangKy() {
        String hoTen = edtHoTen.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String matKhau = edtMatKhau.getText().toString();
        String nhapLai = edtNhapLai.getText().toString();

        if (hoTen.isEmpty()) {
            edtHoTen.setError("Chưa nhập họ tên");
            return;
        }
        if (email.isEmpty()) {
            edtEmail.setError("Chưa nhập email");
            return;
        }
        if (matKhau.length() < 6) {
            edtMatKhau.setError("Mật khẩu phải có ít nhất 6 ký tự");
            return;
        }
        if (!matKhau.equals(nhapLai)) {
            edtNhapLai.setError("Hai mật khẩu không khớp");
            return;
        }

        dangXuLy(true);
        auth.dangKy(hoTen, email, matKhau, (thanhCong, loi) -> {
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

            new AlertDialog.Builder(this)
                    .setTitle("Đăng ký thành công")
                    .setMessage("Một email xác minh đã được gửi tới " + email
                            + ". Hãy mở hộp thư và bấm liên kết xác minh.")
                    .setPositiveButton("Đã hiểu", (d, w) -> finish())
                    .setCancelable(false)
                    .show();
        });
    }

    private void dangXuLy(boolean dang) {
        progress.setVisibility(dang ? View.VISIBLE : View.GONE);
        findViewById(R.id.btnDangKy).setEnabled(!dang);
    }
}
