package com.example.oto.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.oto.MainActivity;
import com.example.oto.R;

import java.util.Locale;

/** Màn hình kết quả thi: ĐẠT/TRƯỢT, lý do (nổi bật nếu do điểm liệt), chia sẻ qua Intent. */
public class KetQuaActivity extends AppCompatActivity {

    public static final String EXTRA_DAT = "dat";
    public static final String EXTRA_SO_DUNG = "so_dung";
    public static final String EXTRA_TONG = "tong";
    public static final String EXTRA_LY_DO = "ly_do";
    public static final String EXTRA_DIEM_LIET = "diem_liet";
    public static final String EXTRA_THOI_GIAN = "thoi_gian";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ket_qua);
        setTitle("Kết quả thi");

        boolean dat = getIntent().getBooleanExtra(EXTRA_DAT, false);
        int soDung = getIntent().getIntExtra(EXTRA_SO_DUNG, 0);
        int tong = getIntent().getIntExtra(EXTRA_TONG, 0);
        String lyDo = getIntent().getStringExtra(EXTRA_LY_DO);
        boolean diemLiet = getIntent().getBooleanExtra(EXTRA_DIEM_LIET, false);
        int giay = getIntent().getIntExtra(EXTRA_THOI_GIAN, 0);

        TextView tvKetQua = findViewById(R.id.tvKetQua);
        TextView tvSoCau = findViewById(R.id.tvSoCau);
        TextView tvThoiGian = findViewById(R.id.tvThoiGian);
        TextView tvLyDo = findViewById(R.id.tvLyDo);

        tvKetQua.setText(dat ? "ĐẠT" : "TRƯỢT");
        tvKetQua.setTextColor(dat ? Color.parseColor("#2E7D32") : Color.parseColor("#C62828"));

        tvSoCau.setText("Số câu đúng: " + soDung + "/" + tong);
        tvThoiGian.setText(String.format(Locale.getDefault(),
                "Thời gian làm bài: %02d:%02d", giay / 60, giay % 60));

        if (!dat && lyDo != null && !lyDo.isEmpty()) {
            tvLyDo.setVisibility(View.VISIBLE);
            tvLyDo.setText((diemLiet ? "❌ TRƯỢT DO CÂU ĐIỂM LIỆT\n" : "Lý do: ") + lyDo);
        }

        findViewById(R.id.btnChiaSe).setOnClickListener(v ->
                chiaSe(dat, soDung, tong, lyDo));
        findViewById(R.id.btnVeTrangChu).setOnClickListener(v -> {
            Intent i = new Intent(this, MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(i);
            finish();
        });
    }

    /** Chia sẻ kết quả qua Intent ngầm ACTION_SEND (Zalo/Messenger/Email...). */
    private void chiaSe(boolean dat, int soDung, int tong, String lyDo) {
        StringBuilder sb = new StringBuilder();
        sb.append("Kết quả thi thử lý thuyết lái xe hạng B\n");
        sb.append("Kết quả: ").append(dat ? "ĐẠT" : "TRƯỢT").append("\n");
        sb.append("Số câu đúng: ").append(soDung).append("/").append(tong).append("\n");
        if (!dat && lyDo != null && !lyDo.isEmpty()) {
            sb.append("Lý do: ").append(lyDo).append("\n");
        }
        sb.append("(Gửi từ ứng dụng Ôn thi lái xe B)");

        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType("text/plain");
        send.putExtra(Intent.EXTRA_SUBJECT, "Kết quả thi thử lái xe hạng B");
        send.putExtra(Intent.EXTRA_TEXT, sb.toString());
        startActivity(Intent.createChooser(send, "Chia sẻ kết quả qua"));
    }
}
