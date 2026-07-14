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
import com.example.oto.data.entity.TrafficSign;

import java.util.ArrayList;
import java.util.List;

/**
 * Quản trị biển báo (tiêu chí 2.1, mục 4): danh sách, tìm kiếm theo tên/mã,
 * lọc theo nhóm, thêm, sửa, xoá.
 */
public class AdminBienBaoActivity extends AppCompatActivity implements BienBaoAdminAdapter.OnItem {

    private static final String TAT_CA = "Tất cả nhóm";

    private QuizRepository repo;
    private BienBaoAdminAdapter adapter;
    private Spinner spinnerNhom;
    private EditText edtTimKiem;
    private TextView tvSoLuong;

    @Nullable
    private LiveData<List<TrafficSign>> nguonHienTai;

    private String nhomDangChon = null; // null = tất cả nhóm
    private String tuKhoa = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (VaiTro.chanNeuKhongPhaiAdmin(this)) {
            return;
        }
        setContentView(R.layout.activity_admin_bien_bao);
        setTitle(getString(R.string.menu_admin_bien_bao));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        repo = new QuizRepository(this);

        edtTimKiem = findViewById(R.id.edtTimKiem);
        spinnerNhom = findViewById(R.id.spinnerNhom);
        tvSoLuong = findViewById(R.id.tvSoLuong);

        RecyclerView rv = findViewById(R.id.rvBienBao);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BienBaoAdminAdapter(this);
        rv.setAdapter(adapter);

        findViewById(R.id.fabThem).setOnClickListener(v ->
                startActivity(new Intent(this, SuaBienBaoActivity.class)));

        theoDoiTimKiem();
        napSpinnerNhom();
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

    private void napSpinnerNhom() {
        repo.getSignGroups().observe(this, nhomList -> {
            List<String> items = new ArrayList<>();
            items.add(TAT_CA);
            if (nhomList != null) {
                items.addAll(nhomList);
            }
            spinnerNhom.setAdapter(new ArrayAdapter<>(
                    this, android.R.layout.simple_spinner_dropdown_item, items));
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

    private void apDungBoLoc() {
        if (nguonHienTai != null) {
            nguonHienTai.removeObservers(this);
        }
        nguonHienTai = repo.filterSigns(nhomDangChon, tuKhoa);
        nguonHienTai.observe(this, list -> {
            adapter.setData(list);
            int n = list == null ? 0 : list.size();
            tvSoLuong.setText(n == 0
                    ? "Không có biển báo nào khớp bộ lọc."
                    : n + " biển báo");
        });
    }

    @Override
    public void onSua(TrafficSign sign) {
        Intent i = new Intent(this, SuaBienBaoActivity.class);
        i.putExtra(SuaBienBaoActivity.EXTRA_SIGN_ID, sign.id);
        startActivity(i);
    }

    @Override
    public void onXoa(TrafficSign sign) {
        new AlertDialog.Builder(this)
                .setTitle("Xoá biển báo")
                .setMessage("Xoá biển " + sign.maBien + " — " + sign.tenBien + "?")
                .setPositiveButton("Xoá", (d, w) -> repo.deleteSign(sign, ok ->
                        Toast.makeText(this, "Đã xoá biển báo.", Toast.LENGTH_SHORT).show()))
                .setNegativeButton("Huỷ", null)
                .show();
    }
}
