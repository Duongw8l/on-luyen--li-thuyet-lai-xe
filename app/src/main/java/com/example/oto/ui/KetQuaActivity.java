package com.example.oto.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.oto.MainActivity;
import com.example.oto.R;
import com.example.oto.databinding.ActivityKetQuaBinding;

import java.util.Locale;

/** Màn hình kết quả thi: ĐẠT/TRƯỢT, lý do (nổi bật nếu do điểm liệt), chia sẻ qua Intent. */
public class KetQuaActivity extends AppCompatActivity {

    public static final String EXTRA_DAT = "dat";
    public static final String EXTRA_SO_DUNG = "so_dung";
    public static final String EXTRA_TONG = "tong";
    public static final String EXTRA_LY_DO = "ly_do";
    public static final String EXTRA_DIEM_LIET = "diem_liet";
    public static final String EXTRA_THOI_GIAN = "thoi_gian";

    private ActivityKetQuaBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityKetQuaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setTitle(R.string.ket_qua_thi);

        boolean dat = getIntent().getBooleanExtra(EXTRA_DAT, false);
        int soDung = getIntent().getIntExtra(EXTRA_SO_DUNG, 0);
        int tong = getIntent().getIntExtra(EXTRA_TONG, 0);
        String lyDo = getIntent().getStringExtra(EXTRA_LY_DO);
        boolean diemLiet = getIntent().getBooleanExtra(EXTRA_DIEM_LIET, false);
        int giay = getIntent().getIntExtra(EXTRA_THOI_GIAN, 0);

        binding.tvKetQua.setText(dat ? R.string.ket_qua_dat : R.string.ket_qua_truot);
        binding.tvKetQua.setTextColor(
                dat ? Color.parseColor("#2E7D32") : Color.parseColor("#C62828"));

        binding.tvSoCau.setText(getString(R.string.so_cau_dung, soDung, tong));
        binding.tvThoiGian.setText(getString(R.string.thoi_gian_lam_bai,
                giay / 60, giay % 60));

        if (!dat && lyDo != null && !lyDo.isEmpty()) {
            binding.tvLyDo.setVisibility(View.VISIBLE);
            binding.tvLyDo.setText(getString(
                    diemLiet ? R.string.ly_do_diem_liet : R.string.ly_do_truot, lyDo));
        }

        binding.btnChiaSe.setOnClickListener(v ->
                chiaSe(dat, soDung, tong, lyDo));
        binding.btnVeTrangChu.setOnClickListener(v -> {
            Intent i = new Intent(this, MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(i);
            finish();
        });
    }

    /** Chia sẻ kết quả qua Intent ngầm ACTION_SEND (Zalo/Messenger/Email...). */
    private void chiaSe(boolean dat, int soDung, int tong, String lyDo) {
        StringBuilder sb = new StringBuilder();
        sb.append(getString(R.string.chia_se_ket_qua_tieu_de)).append("\n");
        sb.append(getString(dat ? R.string.ket_qua_dat : R.string.ket_qua_truot)).append("\n");
        sb.append(getString(R.string.so_cau_dung, soDung, tong)).append("\n");
        if (!dat && lyDo != null && !lyDo.isEmpty()) {
            sb.append(getString(R.string.ly_do_truot, lyDo)).append("\n");
        }
        sb.append(getString(R.string.chan_trang_chia_se));

        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType("text/plain");
        send.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.chia_se_ket_qua_tieu_de));
        send.putExtra(Intent.EXTRA_TEXT, sb.toString());
        startActivity(Intent.createChooser(send, getString(R.string.chia_se_ket_qua_qua)));
    }
}
