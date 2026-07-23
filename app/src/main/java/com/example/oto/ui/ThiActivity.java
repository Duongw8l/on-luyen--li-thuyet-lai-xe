package com.example.oto.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.oto.R;
import com.example.oto.data.ExamConfig;
import com.example.oto.data.entity.Answer;
import com.example.oto.data.relation.QuestionWithAnswers;
import com.example.oto.databinding.ActivityThiBinding;
import com.example.oto.logic.ExamResult;
import com.example.oto.ui.viewmodel.ThiViewModel;
import com.example.oto.util.AnhUtil;

import java.util.Locale;

/** Màn hình làm bài thi: đề ngẫu nhiên, đếm ngược, tự nộp khi hết giờ. */
public class ThiActivity extends AppCompatActivity {

    private ThiViewModel viewModel;
    private ActivityThiBinding binding;

    private CountDownTimer timer;
    private long tongThoiGianMs;
    private long thoiGianConLaiMs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityThiBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setTitle(getString(R.string.menu_thi));
        viewModel = new ViewModelProvider(this).get(ThiViewModel.class);

        binding.btnCauTiep.setOnClickListener(v -> chuyen(1));
        binding.btnCauTruoc.setOnClickListener(v -> chuyen(-1));
        binding.btnNop.setOnClickListener(v -> xacNhanNop());

        tongThoiGianMs = ExamConfig.THOI_GIAN_PHUT * 60_000L;

        viewModel.getDeThi().observe(this, res -> {
            if (res.laLoi()) {
                Toast.makeText(this, res.thongBaoLoi, Toast.LENGTH_LONG).show();
                finish();
                return;
            }
            if (res.laThanhCong()) {
                hienThi();
                batDauDongHo();
            }
        });
        viewModel.sinhDeNeuChua();
    }

    private void batDauDongHo() {
        timer = new CountDownTimer(tongThoiGianMs, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                thoiGianConLaiMs = millisUntilFinished;
                long phut = millisUntilFinished / 60_000;
                long giay = (millisUntilFinished % 60_000) / 1000;
                binding.tvDongHo.setText(
                        String.format(Locale.getDefault(), "%02d:%02d", phut, giay));
                binding.progressTime.setProgress(
                        (int) (millisUntilFinished * 100 / tongThoiGianMs));
            }

            @Override
            public void onFinish() {
                binding.tvDongHo.setText("00:00");
                binding.progressTime.setProgress(0);
                Toast.makeText(ThiActivity.this, R.string.het_gio_tu_nop, Toast.LENGTH_LONG).show();
                nopBai();
            }
        }.start();
    }

    private void chuyen(int delta) {
        luuLuaChonHienTai();
        if (!viewModel.chuyen(delta)) {
            return;
        }
        hienThi();
    }

    private void luuLuaChonHienTai() {
        QuestionWithAnswers qa = viewModel.cauHienTai();
        if (qa == null) {
            return;
        }
        int chon = binding.rgDapAn.getCheckedRadioButtonId();
        if (chon != -1) {
            viewModel.chonDapAn(qa.question.id, chon);
        }
    }

    private void hienThi() {
        QuestionWithAnswers qa = viewModel.cauHienTai();
        if (qa == null) {
            return;
        }
        binding.tvTienDo.setText(getString(R.string.tien_do_cau,
                viewModel.getViTri() + 1, viewModel.cacCau().size()));
        binding.tvCauHoi.setText(qa.question.noiDung);

        // Ảnh minh hoạ (nếu có): hiện hoặc ẩn ImageView.
        Bitmap anh = AnhUtil.docAnhCauHoi(this, qa.question.anhUrl);
        if (anh != null) {
            binding.imgCauHoi.setImageBitmap(anh);
            binding.imgCauHoi.setVisibility(View.VISIBLE);
        } else {
            binding.imgCauHoi.setVisibility(View.GONE);
        }

        binding.rgDapAn.removeAllViews();
        char[] nhan = {'A', 'B', 'C', 'D'};
        Integer daChon = viewModel.dapAnCua(qa.question.id);
        for (int i = 0; i < qa.answers.size(); i++) {
            Answer a = qa.answers.get(i);
            RadioButton rb = new RadioButton(this);
            rb.setId(a.id);
            rb.setText((i < nhan.length ? nhan[i] + ". " : "") + a.noiDung);
            rb.setTextSize(16f);
            rb.setPadding(8, 16, 8, 16);
            binding.rgDapAn.addView(rb);
            if (daChon != null && daChon == a.id) {
                rb.setChecked(true);
            }
        }
    }

    private void xacNhanNop() {
        luuLuaChonHienTai();
        int chuaLam = viewModel.soCauChuaLam();
        String msg = chuaLam > 0
                ? getString(R.string.hoi_nop_bai_con_thieu, chuaLam)
                : getString(R.string.hoi_nop_bai);
        new AlertDialog.Builder(this)
                .setTitle(R.string.nop_bai)
                .setMessage(msg)
                .setPositiveButton(R.string.nop_bai, (d, w) -> nopBai())
                .setNegativeButton(R.string.tiep_tuc_lam, null)
                .show();
    }

    private void nopBai() {
        if (viewModel.daNop()) {
            return;
        }
        if (timer != null) {
            timer.cancel();
        }
        luuLuaChonHienTai();

        int giayDaDung = (int) ((tongThoiGianMs - thoiGianConLaiMs) / 1000);

        // Chấm điểm (gồm luật điểm liệt) và lưu lượt thi đều nằm trong ViewModel.
        ExamResult kq = viewModel.chamVaLuu(giayDaDung);
        if (kq == null) {
            return; // đã nộp rồi
        }

        Intent i = new Intent(this, KetQuaActivity.class);
        i.putExtra(KetQuaActivity.EXTRA_DAT, kq.dat);
        i.putExtra(KetQuaActivity.EXTRA_SO_DUNG, kq.soCauDung);
        i.putExtra(KetQuaActivity.EXTRA_TONG, kq.tongSoCau);
        i.putExtra(KetQuaActivity.EXTRA_LY_DO, kq.lyDoTruot);
        i.putExtra(KetQuaActivity.EXTRA_DIEM_LIET, kq.truotViDiemLiet);
        i.putExtra(KetQuaActivity.EXTRA_THOI_GIAN, giayDaDung);
        startActivity(i);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
        }
    }
}
