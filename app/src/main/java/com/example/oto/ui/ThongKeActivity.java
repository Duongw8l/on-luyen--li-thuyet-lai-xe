package com.example.oto.ui;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.oto.R;
import com.example.oto.data.DatabaseSeeder;
import com.example.oto.data.QuizRepository;
import com.example.oto.data.relation.ChapterStat;

import java.util.ArrayList;
import java.util.List;

/**
 * Thống kê tiến độ: biểu đồ tỷ lệ đúng theo 6 chương, chỉ ra chương yếu nhất,
 * và gửi báo cáo tiến độ qua email (Intent ngầm ACTION_SENDTO).
 */
public class ThongKeActivity extends AppCompatActivity {

    private QuizRepository repo;
    private BarChartView chart;
    private TextView tvTrong, tvGoiY;
    private View btnOnChuongYeu, btnGuiEmail;

    private final List<ChapterStat> stats = new ArrayList<>();
    private ChapterStat chuongYeuNhat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thong_ke);
        setTitle(getString(R.string.menu_thong_ke));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        repo = new QuizRepository(this);

        chart = findViewById(R.id.chart);
        tvTrong = findViewById(R.id.tvTrong);
        tvGoiY = findViewById(R.id.tvGoiY);
        btnOnChuongYeu = findViewById(R.id.btnOnChuongYeu);
        btnGuiEmail = findViewById(R.id.btnGuiEmail);

        btnOnChuongYeu.setOnClickListener(v -> onChuongYeuNhat());
        btnGuiEmail.setOnClickListener(v -> guiBaoCaoQuaEmail());
    }

    @Override
    protected void onResume() {
        super.onResume();
        repo.getChapterStats(DatabaseSeeder.LOCAL_USER_ID, this::hienThi);
    }

    private void hienThi(List<ChapterStat> list) {
        stats.clear();
        stats.addAll(list);

        boolean trong = stats.isEmpty();
        tvTrong.setVisibility(trong ? View.VISIBLE : View.GONE);
        chart.setVisibility(trong ? View.GONE : View.VISIBLE);
        tvGoiY.setVisibility(trong ? View.GONE : View.VISIBLE);
        btnOnChuongYeu.setVisibility(trong ? View.GONE : View.VISIBLE);
        btnGuiEmail.setVisibility(trong ? View.GONE : View.VISIBLE);
        if (trong) {
            return;
        }

        chart.setData(stats);

        chuongYeuNhat = stats.get(0);
        for (ChapterStat s : stats) {
            if (s.phanTram() < chuongYeuNhat.phanTram()) {
                chuongYeuNhat = s;
            }
        }
        tvGoiY.setText("Chương yếu nhất: Chương " + chuongYeuNhat.soThuTu
                + " — " + chuongYeuNhat.tenChuong
                + " (chỉ đúng " + chuongYeuNhat.phanTram() + "%). Nên ôn lại chương này trước.");
    }

    /** Intent tường minh: mở thẳng phần ôn tập đúng chương đang yếu. */
    private void onChuongYeuNhat() {
        if (chuongYeuNhat == null) {
            return;
        }
        Intent i = new Intent(this, OnTapActivity.class);
        i.putExtra(OnTapActivity.EXTRA_CHUONG, chuongYeuNhat.chapterId);
        startActivity(i);
    }

    /** Intent ngầm ACTION_SENDTO: gửi báo cáo tiến độ học tập qua email. */
    private void guiBaoCaoQuaEmail() {
        StringBuilder sb = new StringBuilder();
        sb.append("BÁO CÁO TIẾN ĐỘ ÔN THI LÝ THUYẾT HẠNG B\n\n");
        for (ChapterStat s : stats) {
            sb.append("Chương ").append(s.soThuTu).append(" — ").append(s.tenChuong)
                    .append(": ").append(s.dung).append("/").append(s.tong)
                    .append(" câu đúng (").append(s.phanTram()).append("%)\n");
        }
        if (chuongYeuNhat != null) {
            sb.append("\nChương cần ôn thêm: Chương ").append(chuongYeuNhat.soThuTu)
                    .append(" (").append(chuongYeuNhat.phanTram()).append("%)\n");
        }
        sb.append("\n(Gửi từ ứng dụng Ôn thi lái xe B)");

        Intent email = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"));
        email.putExtra(Intent.EXTRA_SUBJECT, "Báo cáo tiến độ ôn thi lái xe hạng B");
        email.putExtra(Intent.EXTRA_TEXT, sb.toString());
        try {
            startActivity(email);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Máy chưa cài ứng dụng email.", Toast.LENGTH_SHORT).show();
        }
    }
}
