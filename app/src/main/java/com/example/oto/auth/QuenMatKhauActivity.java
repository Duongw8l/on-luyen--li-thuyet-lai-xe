package com.example.oto.auth;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.oto.R;
import com.example.oto.databinding.ActivityQuenMatKhauBinding;

/**
 * Quên mật khẩu — gửi email đặt lại mật khẩu qua Firebase Auth
 * (tiêu chí 2.5, mục 1: "Chức năng gửi email").
 */
public class QuenMatKhauActivity extends AppCompatActivity {

    private AuthManager auth;
    private ActivityQuenMatKhauBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityQuenMatKhauBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setTitle(getString(R.string.quen_mat_khau));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        auth = new AuthManager();

        binding.btnGui.setOnClickListener(v -> gui());
    }

    private void gui() {
        String email = binding.edtEmail.getText().toString().trim();
        if (email.isEmpty()) {
            binding.edtEmail.setError(getString(R.string.loi_chua_nhap_email));
            return;
        }

        dangXuLy(true);
        auth.guiEmailDatLaiMatKhau(email, (thanhCong, loi) -> {
            dangXuLy(false);
            if (!thanhCong) {
                Toast.makeText(this, loi, Toast.LENGTH_LONG).show();
                return;
            }
            binding.tvDaGui.setVisibility(View.VISIBLE);
            binding.tvDaGui.setText(getString(R.string.da_gui_email_dat_lai, email));
        });
    }

    private void dangXuLy(boolean dang) {
        binding.progress.setVisibility(dang ? View.VISIBLE : View.GONE);
        binding.btnGui.setEnabled(!dang);
    }
}
