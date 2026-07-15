package com.example.oto.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.oto.R;
import com.example.oto.data.QuizRepository;
import com.example.oto.data.entity.TrafficSign;

import java.util.ArrayList;
import java.util.List;

/**
 * Tra cứu biển báo: RecyclerView + Spinner lọc nhóm + ô tìm kiếm theo tên/mã biển.
 * Bấm vào một biển -> Intent tường minh mở màn hình chi tiết.
 */
public class BienBaoActivity extends AppCompatActivity {

    private static final String TAT_CA = "Tất cả nhóm";

    private QuizRepository repo;
    private BienBaoAdapter adapter;
    private Spinner spinnerNhom;
    private EditText edtTimKiem;
    private TextView tvSoLuong;

    /** LiveData đang được quan sát — phải gỡ trước khi đổi bộ lọc. */
    @Nullable
    private LiveData<List<TrafficSign>> nguonHienTai;

    private String nhomDangChon = null; // null = tất cả
    private String tuKhoa = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bien_bao);
        setTitle(getString(R.string.menu_bien_bao));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        repo = new QuizRepository(this);

        spinnerNhom = findViewById(R.id.spinnerNhom);
        edtTimKiem = findViewById(R.id.edtTimKiem);
        tvSoLuong = findViewById(R.id.tvSoLuong);

        RecyclerView rv = findViewById(R.id.rvBienBao);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BienBaoAdapter(this::moChiTiet);
        rv.setAdapter(adapter);

        napSpinnerNhom();
        theoDoiTimKiem();
        apDungBoLoc();
    }

    private void napSpinnerNhom() {
        repo.getSignGroups().observe(this, nhomList -> {
            List<String> items = new ArrayList<>();
            items.add(TAT_CA);
            if (nhomList != null) {
                items.addAll(nhomList);
            }
            ArrayAdapter<String> ad = new ArrayAdapter<>(
                    this, android.R.layout.simple_spinner_dropdown_item, items);
            spinnerNhom.setAdapter(ad);
            spinnerNhom.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String chon = (String) parent.getItemAtPosition(position);
                    nhomDangChon = TAT_CA.equals(chon) ? null : chon;
                    apDungBoLoc();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        });
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

    /** Truy vấn lại database mỗi khi nhóm hoặc từ khóa thay đổi. */
    private void apDungBoLoc() {
        if (nguonHienTai != null) {
            nguonHienTai.removeObservers(this);
        }
        nguonHienTai = repo.filterSigns(nhomDangChon, tuKhoa);
        nguonHienTai.observe(this, list -> {
            adapter.setData(list);
            int n = list == null ? 0 : list.size();
            tvSoLuong.setText(n == 0
                    ? "Không tìm thấy biển báo nào."
                    : "Tìm thấy " + n + " biển báo");
        });
    }

    private void moChiTiet(TrafficSign sign) {
        Intent i = new Intent(this, ChiTietBienBaoActivity.class);
        i.putExtra(ChiTietBienBaoActivity.EXTRA_MA, sign.maBien);
        i.putExtra(ChiTietBienBaoActivity.EXTRA_TEN, sign.tenBien);
        i.putExtra(ChiTietBienBaoActivity.EXTRA_NHOM, sign.nhomBien);
        i.putExtra(ChiTietBienBaoActivity.EXTRA_MO_TA, sign.moTa);
        i.putExtra(ChiTietBienBaoActivity.EXTRA_ANH, sign.anhUrl);
        startActivity(i);
    }
}
