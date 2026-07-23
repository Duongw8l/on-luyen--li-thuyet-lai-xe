package com.example.oto.ui;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.oto.R;
import com.example.oto.data.entity.Answer;
import com.example.oto.data.entity.Chapter;
import com.example.oto.data.relation.QuestionWithAnswers;
import com.example.oto.databinding.ActivityOnTapBinding;
import com.example.oto.ui.viewmodel.OnTapViewModel;
import com.example.oto.util.AnhUtil;

/**
 * Ôn tập theo chương (chọn qua Spinner) hoặc ôn nhóm câu điểm liệt.
 * Sau khi chọn đáp án và bấm "Kiểm tra": tô màu đáp án đúng/sai + hiện giải thích.
 */
public class OnTapActivity extends AppCompatActivity {

    public static final String EXTRA_DIEM_LIET = "diem_liet";
    /** Mở thẳng một chương (dùng khi từ màn Thống kê bấm "ôn chương yếu nhất"). */
    public static final String EXTRA_CHUONG = "chuong_id";

    private int chuongMoSan = 0;

    private OnTapViewModel viewModel;
    private ActivityOnTapBinding binding;

    /** Đã bấm "Kiểm tra" cho câu đang xem chưa — trạng thái thuần giao diện. */
    private boolean daKiemTra = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOnTapBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        viewModel = new ViewModelProvider(this).get(OnTapViewModel.class);

        binding.btnKiemTra.setOnClickListener(v -> kiemTra());
        binding.btnCauTiep.setOnClickListener(v -> chuyen(1));
        binding.btnCauTruoc.setOnClickListener(v -> chuyen(-1));

        viewModel.getDanhSach().observe(this, res -> {
            if (res.laThanhCong()) {
                veDanhSach();
            }
        });

        boolean diemLiet = getIntent().getBooleanExtra(EXTRA_DIEM_LIET, false);
        chuongMoSan = getIntent().getIntExtra(EXTRA_CHUONG, 0);
        if (diemLiet) {
            setTitle(getString(R.string.menu_diem_liet));
            binding.spinnerChuong.setVisibility(View.GONE);
            viewModel.napDiemLiet();
        } else {
            setTitle(getString(R.string.menu_on_tap));
            napSpinnerChuong();
        }
    }

    private void napSpinnerChuong() {
        viewModel.getChuong().observe(this, chapters -> {
            ArrayAdapter<Chapter> adapter = new ArrayAdapter<>(
                    this, android.R.layout.simple_spinner_dropdown_item, chapters);
            binding.spinnerChuong.setAdapter(adapter);
            binding.spinnerChuong.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    Chapter c = (Chapter) parent.getItemAtPosition(position);
                    viewModel.napTheoChuong(c.id);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });

            // Được gọi từ màn Thống kê: nhảy thẳng tới chương đang yếu.
            if (chuongMoSan > 0) {
                for (int i = 0; i < chapters.size(); i++) {
                    if (chapters.get(i).id == chuongMoSan) {
                        binding.spinnerChuong.setSelection(i);
                        break;
                    }
                }
                chuongMoSan = 0;
            }
        });
    }

    /** Vẽ lại sau khi ViewModel nạp xong danh sách mới. */
    private void veDanhSach() {
        if (viewModel.soCau() == 0) {
            binding.tvCauHoi.setText(R.string.chua_co_cau_hoi);
            binding.tvTienDo.setText("");
            binding.rgDapAn.removeAllViews();
            binding.tvGiaiThich.setVisibility(View.GONE);
            binding.imgCauHoi.setVisibility(View.GONE);
            return;
        }
        hienThi();
    }

    private void chuyen(int delta) {
        if (viewModel.soCau() == 0) {
            return;
        }
        if (!viewModel.chuyen(delta)) {
            Toast.makeText(this, R.string.da_het_cau, Toast.LENGTH_SHORT).show();
            return;
        }
        hienThi();
    }

    private void hienThi() {
        daKiemTra = false;
        binding.tvGiaiThich.setVisibility(View.GONE);
        QuestionWithAnswers qa = viewModel.cauHienTai();
        if (qa == null) {
            return;
        }

        binding.tvTienDo.setText(getString(
                qa.question.isDiemLiet ? R.string.tien_do_cau_diem_liet : R.string.tien_do_cau,
                viewModel.getViTri() + 1, viewModel.soCau()));
        binding.tvCauHoi.setText(qa.question.noiDung);
        hienAnhCauHoi(qa.question.anhUrl);

        binding.rgDapAn.removeAllViews();
        char[] nhanDap = {'A', 'B', 'C', 'D'};
        for (int i = 0; i < qa.answers.size(); i++) {
            Answer a = qa.answers.get(i);
            RadioButton rb = new RadioButton(this);
            rb.setId(a.id);
            rb.setText((i < nhanDap.length ? nhanDap[i] + ". " : "") + a.noiDung);
            rb.setTextSize(16f);
            rb.setPadding(8, 16, 8, 16);
            binding.rgDapAn.addView(rb);
        }
    }

    /** Ảnh minh hoạ của câu hỏi: có thì hiện, không thì ẩn ImageView. */
    private void hienAnhCauHoi(String anhUrl) {
        Bitmap anh = AnhUtil.docAnhCauHoi(this, anhUrl);
        if (anh != null) {
            binding.imgCauHoi.setImageBitmap(anh);
            binding.imgCauHoi.setVisibility(View.VISIBLE);
        } else {
            binding.imgCauHoi.setVisibility(View.GONE);
        }
    }

    private void kiemTra() {
        if (viewModel.soCau() == 0 || daKiemTra) {
            return;
        }
        int chon = binding.rgDapAn.getCheckedRadioButtonId();
        if (chon == -1) {
            Toast.makeText(this, R.string.hay_chon_dap_an, Toast.LENGTH_SHORT).show();
            return;
        }
        daKiemTra = true;
        QuestionWithAnswers qa = viewModel.cauHienTai();
        if (qa == null) {
            return;
        }
        int correctId = qa.correctAnswerId();

        for (int i = 0; i < binding.rgDapAn.getChildCount(); i++) {
            RadioButton rb = (RadioButton) binding.rgDapAn.getChildAt(i);
            if (rb.getId() == correctId) {
                rb.setTextColor(Color.parseColor("#2E7D32")); // đúng - xanh
            } else if (rb.getId() == chon) {
                rb.setTextColor(Color.parseColor("#C62828")); // chọn sai - đỏ
            }
        }

        String gt = qa.question.giaiThich;
        binding.tvGiaiThich.setText(getString(
                chon == correctId ? R.string.ket_qua_chinh_xac : R.string.ket_qua_chua_dung,
                gt == null ? "" : gt));
        binding.tvGiaiThich.setVisibility(View.VISIBLE);
    }
}
