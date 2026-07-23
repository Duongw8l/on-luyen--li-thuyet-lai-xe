package com.example.oto.ui;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.oto.R;
import com.example.oto.data.relation.ChapterStat;
import com.example.oto.databinding.ActivityThongKeBinding;
import com.example.oto.ui.viewmodel.ThongKeViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Thống kê tiến độ: biểu đồ tỷ lệ đúng theo 6 chương, chỉ ra chương yếu nhất,
 * và gửi báo cáo tiến độ qua email (Intent ngầm ACTION_SENDTO).
 */
public class ThongKeActivity extends AppCompatActivity {

    private ThongKeViewModel viewModel;
    private ActivityThongKeBinding binding;

    /** Bản sao dữ liệu đang hiển thị — dùng để dựng nội dung email báo cáo. */
    private final List<ChapterStat> stats = new ArrayList<>();
    private ChapterStat chuongYeuNhat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityThongKeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setTitle(getString(R.string.menu_thong_ke));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        viewModel = new ViewModelProvider(this).get(ThongKeViewModel.class);

        binding.btnOnChuongYeu.setOnClickListener(v -> onChuongYeuNhat());
        binding.btnGuiEmail.setOnClickListener(v -> guiBaoCaoQuaEmail());

        viewModel.getThongKe().observe(this, res -> {
            if (res.laThanhCong()) {
                hienThi(res.duLieu);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Nạp lại mỗi lần quay lại màn này: người dùng có thể vừa thi thêm một lượt.
        viewModel.nap();
    }

    private void hienThi(List<ChapterStat> list) {
        stats.clear();
        if (list != null) {
            stats.addAll(list);
        }

        boolean trong = stats.isEmpty();
        binding.tvTrong.setVisibility(trong ? View.VISIBLE : View.GONE);
        binding.chart.setVisibility(trong ? View.GONE : View.VISIBLE);
        binding.tvGoiY.setVisibility(trong ? View.GONE : View.VISIBLE);
        binding.btnOnChuongYeu.setVisibility(trong ? View.GONE : View.VISIBLE);
        binding.btnGuiEmail.setVisibility(trong ? View.GONE : View.VISIBLE);
        if (trong) {
            return;
        }

        binding.chart.setData(stats);

        // Việc tìm chương yếu nhất là logic nghiệp vụ -> nằm ở ViewModel.
        chuongYeuNhat = viewModel.chuongYeuNhat();
        if (chuongYeuNhat == null) {
            return;
        }
        binding.tvGoiY.setText(getString(R.string.goi_y_chuong_yeu,
                chuongYeuNhat.soThuTu, chuongYeuNhat.tenChuong, chuongYeuNhat.phanTram()));
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
        sb.append(getString(R.string.bao_cao_dau_de));
        for (ChapterStat s : stats) {
            sb.append(getString(R.string.bao_cao_dong_chuong,
                    s.soThuTu, s.tenChuong, s.dung, s.tong, s.phanTram()));
        }
        if (chuongYeuNhat != null) {
            sb.append(getString(R.string.bao_cao_chuong_can_on,
                    chuongYeuNhat.soThuTu, chuongYeuNhat.phanTram()));
        }
        sb.append("\n").append(getString(R.string.chan_trang_chia_se));

        Intent email = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"));
        email.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.bao_cao_tieu_de));
        email.putExtra(Intent.EXTRA_TEXT, sb.toString());
        try {
            startActivity(email);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.loi_khong_co_email, Toast.LENGTH_SHORT).show();
        }
    }
}
