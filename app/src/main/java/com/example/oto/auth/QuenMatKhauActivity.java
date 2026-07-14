package com.example.oto.auth;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.oto.R;

/**
 * Quên mật khẩu — gửi email đặt lại mật khẩu qua Firebase Auth
 * (tiêu chí 2.5, mục 1: "Chức năng gửi email").
 */
public class QuenMatKhauActivity extends AppCompatActivity {

    private AuthManager auth;
    private EditText edtEmail;
    private TextView tvDaGui;
    private ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quen_mat_khau);
        setTitle(getString(R.string.quen_mat_khau));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        auth = new AuthManager();

        edtEmail = findViewById(R.id.edtEmail);
        tvDaGui = findViewById(R.id.tvDaGui);
        progress = findViewById(R.id.progress);

        findViewById(R.id.btnGui).setOnClickListener(v -> gui());
    }

    private void gui() {
        String email = edtEmail.getText().toString().trim();
        if (email.isEmpty()) {
            edtEmail.setError("Chưa nhập email");
            return;
        }

        dangXuLy(true);
        auth.guiEmailDatLaiMatKhau(email, (thanhCong, loi) -> {
            dangXuLy(false);
            if (!thanhCong) {
                Toast.makeText(this, loi, Toast.LENGTH_LONG).show();
                return;
            }
            tvDaGui.setVisibility(View.VISIBLE);
            tvDaGui.setText("Đã gửi email đặt lại mật khẩu tới " + email
                    + ".\n\nHãy mở hộp thư (kiểm tra cả mục Spam), bấm liên kết trong email"
                    + " để đặt mật khẩu mới, rồi quay lại đăng nhập.");
        });
    }

    private void dangXuLy(boolean dang) {
        progress.setVisibility(dang ? View.VISIBLE : View.GONE);
        findViewById(R.id.btnGui).setEnabled(!dang);
    }
}
