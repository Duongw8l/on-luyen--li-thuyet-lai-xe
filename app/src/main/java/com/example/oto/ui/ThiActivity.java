package com.example.oto.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.oto.R;
import com.example.oto.data.DatabaseSeeder;
import com.example.oto.data.ExamConfig;
import com.example.oto.data.QuizRepository;
import com.example.oto.data.entity.Answer;
import com.example.oto.data.entity.Attempt;
import com.example.oto.data.entity.UserAnswer;
import com.example.oto.data.relation.QuestionWithAnswers;
import com.example.oto.logic.ExamResult;
import com.example.oto.logic.ExamScorer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Màn hình làm bài thi: đề ngẫu nhiên, đếm ngược, tự nộp khi hết giờ. */
public class ThiActivity extends AppCompatActivity {

    private QuizRepository repo;
    private TextView tvTienDo, tvDongHo, tvCauHoi;
    private ProgressBar progressTime;
    private RadioGroup rgDapAn;

    private final List<QuestionWithAnswers> deThi = new ArrayList<>();
    private final Map<Integer, Integer> dapAnDaChon = new HashMap<>(); // questionId -> answerId
    private int viTri = 0;

    private CountDownTimer timer;
    private long tongThoiGianMs;
    private long thoiGianConLaiMs;
    private boolean daNop = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thi);
        setTitle(getString(R.string.menu_thi));
        repo = new QuizRepository(this);

        tvTienDo = findViewById(R.id.tvTienDo);
        tvDongHo = findViewById(R.id.tvDongHo);
        tvCauHoi = findViewById(R.id.tvCauHoi);
        progressTime = findViewById(R.id.progressTime);
        rgDapAn = findViewById(R.id.rgDapAn);

        findViewById(R.id.btnCauTiep).setOnClickListener(v -> chuyen(1));
        findViewById(R.id.btnCauTruoc).setOnClickListener(v -> chuyen(-1));
        findViewById(R.id.btnNop).setOnClickListener(v -> xacNhanNop());

        tongThoiGianMs = ExamConfig.THOI_GIAN_PHUT * 60_000L;

        repo.generateRandomExam(list -> {
            if (list.isEmpty()) {
                Toast.makeText(this, "Chưa có câu hỏi trong ngân hàng.", Toast.LENGTH_LONG).show();
                finish();
                return;
            }
            deThi.addAll(list);
            hienThi();
            batDauDongHo();
        });
    }

    private void batDauDongHo() {
        timer = new CountDownTimer(tongThoiGianMs, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                thoiGianConLaiMs = millisUntilFinished;
                long phut = millisUntilFinished / 60_000;
                long giay = (millisUntilFinished % 60_000) / 1000;
                tvDongHo.setText(String.format(Locale.getDefault(), "%02d:%02d", phut, giay));
                progressTime.setProgress((int) (millisUntilFinished * 100 / tongThoiGianMs));
            }

            @Override
            public void onFinish() {
                tvDongHo.setText("00:00");
                progressTime.setProgress(0);
                Toast.makeText(ThiActivity.this, "Hết giờ — tự động nộp bài.", Toast.LENGTH_LONG).show();
                nopBai();
            }
        }.start();
    }

    private void chuyen(int delta) {
        luuLuaChonHienTai();
        int moi = viTri + delta;
        if (moi < 0 || moi >= deThi.size()) {
            return;
        }
        viTri = moi;
        hienThi();
    }

    private void luuLuaChonHienTai() {
        if (deThi.isEmpty()) {
            return;
        }
        int chon = rgDapAn.getCheckedRadioButtonId();
        int qid = deThi.get(viTri).question.id;
        if (chon != -1) {
            dapAnDaChon.put(qid, chon);
        }
    }

    private void hienThi() {
        QuestionWithAnswers qa = deThi.get(viTri);
        tvTienDo.setText("Câu " + (viTri + 1) + "/" + deThi.size());
        tvCauHoi.setText(qa.question.noiDung);

        rgDapAn.removeAllViews();
        char[] nhan = {'A', 'B', 'C', 'D'};
        Integer daChon = dapAnDaChon.get(qa.question.id);
        for (int i = 0; i < qa.answers.size(); i++) {
            Answer a = qa.answers.get(i);
            RadioButton rb = new RadioButton(this);
            rb.setId(a.id);
            rb.setText((i < nhan.length ? nhan[i] + ". " : "") + a.noiDung);
            rb.setTextSize(16f);
            rb.setPadding(8, 16, 8, 16);
            rgDapAn.addView(rb);
            if (daChon != null && daChon == a.id) {
                rb.setChecked(true);
            }
        }
    }

    private void xacNhanNop() {
        luuLuaChonHienTai();
        int chuaLam = deThi.size() - dapAnDaChon.size();
        String msg = chuaLam > 0
                ? "Bạn còn " + chuaLam + " câu chưa trả lời. Vẫn nộp bài?"
                : "Bạn chắc chắn muốn nộp bài?";
        new AlertDialog.Builder(this)
                .setTitle("Nộp bài")
                .setMessage(msg)
                .setPositiveButton("Nộp bài", (d, w) -> nopBai())
                .setNegativeButton("Tiếp tục làm", null)
                .show();
    }

    private void nopBai() {
        if (daNop) {
            return;
        }
        daNop = true;
        if (timer != null) {
            timer.cancel();
        }
        luuLuaChonHienTai();

        ExamResult kq = ExamScorer.cham(deThi, dapAnDaChon, ExamConfig.NGUONG_DAT);
        luuLuotThi(kq);

        Intent i = new Intent(this, KetQuaActivity.class);
        i.putExtra(KetQuaActivity.EXTRA_DAT, kq.dat);
        i.putExtra(KetQuaActivity.EXTRA_SO_DUNG, kq.soCauDung);
        i.putExtra(KetQuaActivity.EXTRA_TONG, kq.tongSoCau);
        i.putExtra(KetQuaActivity.EXTRA_LY_DO, kq.lyDoTruot);
        i.putExtra(KetQuaActivity.EXTRA_DIEM_LIET, kq.truotViDiemLiet);
        int giayDaDung = (int) ((tongThoiGianMs - thoiGianConLaiMs) / 1000);
        i.putExtra(KetQuaActivity.EXTRA_THOI_GIAN, giayDaDung);
        startActivity(i);
        finish();
    }

    private void luuLuotThi(ExamResult kq) {
        Attempt attempt = new Attempt();
        attempt.userId = DatabaseSeeder.LOCAL_USER_ID;
        attempt.examSetId = 0; // đề ngẫu nhiên
        attempt.soCauDung = kq.soCauDung;
        attempt.ketQua = kq.dat ? Attempt.KET_QUA_DAT : Attempt.KET_QUA_TRUOT;
        attempt.lyDoTruot = kq.lyDoTruot;
        attempt.thoiGianLam = (int) ((tongThoiGianMs - thoiGianConLaiMs) / 1000);
        attempt.ngayThi = System.currentTimeMillis();

        List<UserAnswer> chiTiet = new ArrayList<>();
        for (QuestionWithAnswers qa : deThi) {
            Integer chon = dapAnDaChon.get(qa.question.id);
            boolean dung = chon != null && chon == qa.correctAnswerId();
            chiTiet.add(new UserAnswer(0, qa.question.id, chon == null ? 0 : chon, dung));
        }
        repo.saveAttempt(attempt, chiTiet, id -> {
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
        }
    }
}
