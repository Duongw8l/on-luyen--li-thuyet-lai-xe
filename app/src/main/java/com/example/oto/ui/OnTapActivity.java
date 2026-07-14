package com.example.oto.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.oto.R;
import com.example.oto.data.QuizRepository;
import com.example.oto.data.entity.Answer;
import com.example.oto.data.entity.Chapter;
import com.example.oto.data.relation.QuestionWithAnswers;

import java.util.ArrayList;
import java.util.List;

/**
 * Ôn tập theo chương (chọn qua Spinner) hoặc ôn nhóm câu điểm liệt.
 * Sau khi chọn đáp án và bấm "Kiểm tra": tô màu đáp án đúng/sai + hiện giải thích.
 */
public class OnTapActivity extends AppCompatActivity {

    public static final String EXTRA_DIEM_LIET = "diem_liet";
    /** Mở thẳng một chương (dùng khi từ màn Thống kê bấm "ôn chương yếu nhất"). */
    public static final String EXTRA_CHUONG = "chuong_id";

    private int chuongMoSan = 0;

    private QuizRepository repo;
    private Spinner spinnerChuong;
    private TextView tvTienDo, tvCauHoi, tvGiaiThich;
    private ImageView imgCauHoi;
    private RadioGroup rgDapAn;

    private final List<QuestionWithAnswers> danhSach = new ArrayList<>();
    private int viTri = 0;
    private boolean daKiemTra = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_on_tap);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        repo = new QuizRepository(this);

        spinnerChuong = findViewById(R.id.spinnerChuong);
        tvTienDo = findViewById(R.id.tvTienDo);
        tvCauHoi = findViewById(R.id.tvCauHoi);
        tvGiaiThich = findViewById(R.id.tvGiaiThich);
        imgCauHoi = findViewById(R.id.imgCauHoi);
        rgDapAn = findViewById(R.id.rgDapAn);

        findViewById(R.id.btnKiemTra).setOnClickListener(v -> kiemTra());
        findViewById(R.id.btnCauTiep).setOnClickListener(v -> chuyen(1));
        findViewById(R.id.btnCauTruoc).setOnClickListener(v -> chuyen(-1));

        boolean diemLiet = getIntent().getBooleanExtra(EXTRA_DIEM_LIET, false);
        chuongMoSan = getIntent().getIntExtra(EXTRA_CHUONG, 0);
        if (diemLiet) {
            setTitle(getString(R.string.menu_diem_liet));
            spinnerChuong.setVisibility(View.GONE);
            repo.getDiemLiet(this::napDanhSach);
        } else {
            setTitle(getString(R.string.menu_on_tap));
            napSpinnerChuong();
        }
    }

    private void napSpinnerChuong() {
        repo.getChapters().observe(this, chapters -> {
            ArrayAdapter<Chapter> adapter = new ArrayAdapter<>(
                    this, android.R.layout.simple_spinner_dropdown_item, chapters);
            spinnerChuong.setAdapter(adapter);
            spinnerChuong.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    Chapter c = (Chapter) parent.getItemAtPosition(position);
                    repo.getQuestionsByChapter(c.id, OnTapActivity.this::napDanhSach);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });

            // Được gọi từ màn Thống kê: nhảy thẳng tới chương đang yếu.
            if (chuongMoSan > 0) {
                for (int i = 0; i < chapters.size(); i++) {
                    if (chapters.get(i).id == chuongMoSan) {
                        spinnerChuong.setSelection(i);
                        break;
                    }
                }
                chuongMoSan = 0;
            }
        });
    }

    private void napDanhSach(List<QuestionWithAnswers> list) {
        danhSach.clear();
        danhSach.addAll(list);
        viTri = 0;
        if (danhSach.isEmpty()) {
            tvCauHoi.setText("Chưa có câu hỏi cho mục này.");
            tvTienDo.setText("");
            rgDapAn.removeAllViews();
            tvGiaiThich.setVisibility(View.GONE);
            imgCauHoi.setVisibility(View.GONE);
            return;
        }
        hienThi();
    }

    private void chuyen(int delta) {
        if (danhSach.isEmpty()) {
            return;
        }
        int moi = viTri + delta;
        if (moi < 0 || moi >= danhSach.size()) {
            Toast.makeText(this, "Đã hết câu.", Toast.LENGTH_SHORT).show();
            return;
        }
        viTri = moi;
        hienThi();
    }

    private void hienThi() {
        daKiemTra = false;
        tvGiaiThich.setVisibility(View.GONE);
        QuestionWithAnswers qa = danhSach.get(viTri);

        String nhan = qa.question.isDiemLiet ? "  ⚠ ĐIỂM LIỆT" : "";
        tvTienDo.setText("Câu " + (viTri + 1) + "/" + danhSach.size() + nhan);
        tvCauHoi.setText(qa.question.noiDung);

        // Ảnh câu hỏi (nếu có url). Bản offline chưa nạp ảnh thật -> ẩn nếu rỗng.
        imgCauHoi.setVisibility(View.GONE);

        rgDapAn.removeAllViews();
        char[] nhanDap = {'A', 'B', 'C', 'D'};
        for (int i = 0; i < qa.answers.size(); i++) {
            Answer a = qa.answers.get(i);
            RadioButton rb = new RadioButton(this);
            rb.setId(a.id);
            rb.setText((i < nhanDap.length ? nhanDap[i] + ". " : "") + a.noiDung);
            rb.setTextSize(16f);
            rb.setPadding(8, 16, 8, 16);
            rgDapAn.addView(rb);
        }
    }

    private void kiemTra() {
        if (danhSach.isEmpty() || daKiemTra) {
            return;
        }
        int chon = rgDapAn.getCheckedRadioButtonId();
        if (chon == -1) {
            Toast.makeText(this, "Hãy chọn một đáp án.", Toast.LENGTH_SHORT).show();
            return;
        }
        daKiemTra = true;
        QuestionWithAnswers qa = danhSach.get(viTri);
        int correctId = qa.correctAnswerId();

        for (int i = 0; i < rgDapAn.getChildCount(); i++) {
            RadioButton rb = (RadioButton) rgDapAn.getChildAt(i);
            if (rb.getId() == correctId) {
                rb.setTextColor(Color.parseColor("#2E7D32")); // đúng - xanh
            } else if (rb.getId() == chon) {
                rb.setTextColor(Color.parseColor("#C62828")); // chọn sai - đỏ
            }
        }

        String gt = qa.question.giaiThich;
        tvGiaiThich.setText((chon == correctId ? "Chính xác. " : "Chưa đúng. ")
                + (gt == null ? "" : gt));
        tvGiaiThich.setVisibility(View.VISIBLE);
    }
}
