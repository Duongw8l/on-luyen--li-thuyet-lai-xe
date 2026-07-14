package com.example.oto.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.oto.R;
import com.example.oto.auth.VaiTro;
import com.example.oto.data.QuizRepository;
import com.example.oto.data.entity.Chapter;
import com.example.oto.data.relation.QuestionWithAnswers;

import java.util.ArrayList;
import java.util.List;

/**
 * Màn Quản trị ngân hàng câu hỏi (tiêu chí 2.1): xem danh sách, tìm kiếm theo từ khóa,
 * lọc theo chương / câu điểm liệt, thêm, sửa, xoá.
 */
public class AdminActivity extends AppCompatActivity implements CauHoiAdminAdapter.OnItem {

    private QuizRepository repo;
    private CauHoiAdminAdapter adapter;
    private Spinner spinnerChuong;
    private EditText edtTimKiem;
    private CheckBox cbDiemLiet;
    private TextView tvSoLuong;

    @Nullable
    private LiveData<List<QuestionWithAnswers>> nguonHienTai;

    private String tuKhoa = "";
    private int chuongDangChon = 0; // 0 = tất cả chương
    private boolean chiDiemLiet = false;

    /** id chương tương ứng từng vị trí trong Spinner (vị trí 0 = tất cả). */
    private final List<Integer> idTheoViTri = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (VaiTro.chanNeuKhongPhaiAdmin(this)) {
            return;
        }
        setContentView(R.layout.activity_admin);
        setTitle(getString(R.string.menu_admin));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        repo = new QuizRepository(this);

        edtTimKiem = findViewById(R.id.edtTimKiem);
        spinnerChuong = findViewById(R.id.spinnerChuong);
        cbDiemLiet = findViewById(R.id.cbDiemLiet);
        tvSoLuong = findViewById(R.id.tvSoLuong);

        RecyclerView rv = findViewById(R.id.rvCauHoi);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CauHoiAdminAdapter(this);
        rv.setAdapter(adapter);

        findViewById(R.id.fabThem).setOnClickListener(v ->
                startActivity(new Intent(this, SuaCauHoiActivity.class)));

        cbDiemLiet.setOnCheckedChangeListener((v, checked) -> {
            chiDiemLiet = checked;
            apDungBoLoc();
        });

        theoDoiTimKiem();
        napSpinnerChuong();
        apDungBoLoc();
    }

    private void theoDoiTimKiem() {
        edtTimKiem.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                tuKhoa = s.toString().trim();
                apDungBoLoc();
            }
        });
    }

    private void napSpinnerChuong() {
        repo.getChapters().observe(this, chapters -> {
            // Mục đầu tiên là "Tất cả chương", ứng với id 0 trong truy vấn lọc.
            List<String> nhan = new ArrayList<>();
            idTheoViTri.clear();
            nhan.add("Tất cả chương");
            idTheoViTri.add(0);
            if (chapters != null) {
                for (Chapter c : chapters) {
                    nhan.add(c.toString());
                    idTheoViTri.add(c.id);
                }
            }

            ArrayAdapter<String> ad = new ArrayAdapter<>(
                    this, android.R.layout.simple_spinner_dropdown_item, nhan);
            spinnerChuong.setAdapter(ad);
            spinnerChuong.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    chuongDangChon = idTheoViTri.get(position);
                    apDungBoLoc();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        });
    }

    /** Truy vấn lại mỗi khi từ khóa / chương / bộ lọc điểm liệt thay đổi. */
    private void apDungBoLoc() {
        if (nguonHienTai != null) {
            nguonHienTai.removeObservers(this);
        }
        nguonHienTai = repo.filterQuestions(tuKhoa, chuongDangChon, chiDiemLiet);
        nguonHienTai.observe(this, list -> {
            adapter.setData(list);
            int n = list == null ? 0 : list.size();
            tvSoLuong.setText(n == 0
                    ? "Không có câu hỏi nào khớp bộ lọc."
                    : n + " câu hỏi");
        });
    }

    @Override
    public void onSua(QuestionWithAnswers qa) {
        Intent i = new Intent(this, SuaCauHoiActivity.class);
        i.putExtra(SuaCauHoiActivity.EXTRA_QUESTION_ID, qa.question.id);
        startActivity(i);
    }

    @Override
    public void onXoa(QuestionWithAnswers qa) {
        new AlertDialog.Builder(this)
                .setTitle("Xoá câu hỏi")
                .setMessage("Xoá câu hỏi #" + qa.question.id
                        + "? Toàn bộ đáp án của câu này cũng bị xoá theo và không khôi phục được.")
                .setPositiveButton("Xoá", (d, w) -> repo.deleteQuestion(qa.question, ok ->
                        Toast.makeText(this, "Đã xoá câu hỏi.", Toast.LENGTH_SHORT).show()))
                .setNegativeButton("Huỷ", null)
                .show();
    }
}
