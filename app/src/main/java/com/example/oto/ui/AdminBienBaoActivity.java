package com.example.oto.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.oto.R;
import com.example.oto.auth.VaiTro;
import com.example.oto.data.entity.TrafficSign;
import com.example.oto.databinding.ActivityAdminBienBaoBinding;
import com.example.oto.ui.viewmodel.AdminBienBaoViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Quản trị biển báo (tiêu chí 2.1, mục 4): danh sách, tìm kiếm theo tên/mã,
 * lọc theo nhóm, thêm, sửa, xoá.
 */
public class AdminBienBaoActivity extends AppCompatActivity implements BienBaoAdminAdapter.OnItem {

    // Nhãn "tất cả nhóm" lấy từ strings.xml lúc chạy (xem onCreate).
    private String tatCa;

    private AdminBienBaoViewModel viewModel;
    private BienBaoAdminAdapter adapter;
    private ActivityAdminBienBaoBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (VaiTro.chanNeuKhongPhaiAdmin(this)) {
            return;
        }
        binding = ActivityAdminBienBaoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setTitle(getString(R.string.menu_admin_bien_bao));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        tatCa = getString(R.string.tat_ca_nhom);
        viewModel = new ViewModelProvider(this).get(AdminBienBaoViewModel.class);

        binding.rvBienBao.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BienBaoAdminAdapter(this);
        binding.rvBienBao.setAdapter(adapter);

        binding.fabThem.setOnClickListener(v ->
                startActivity(new Intent(this, SuaBienBaoActivity.class)));

        theoDoiTimKiem();
        napSpinnerNhom();
        quanSatDanhSach();
    }

    private void theoDoiTimKiem() {
        binding.edtTimKiem.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                viewModel.datTuKhoa(s.toString().trim());
            }
        });
    }

    private void napSpinnerNhom() {
        viewModel.getNhomBien().observe(this, nhomList -> {
            List<String> items = new ArrayList<>();
            items.add(tatCa);
            if (nhomList != null) {
                items.addAll(nhomList);
            }
            binding.spinnerNhom.setAdapter(new ArrayAdapter<>(
                    this, android.R.layout.simple_spinner_dropdown_item, items));
            binding.spinnerNhom.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String chon = (String) parent.getItemAtPosition(position);
                    viewModel.datNhom(tatCa.equals(chon) ? null : chon);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        });
    }

    /** Đăng ký một lần; ViewModel tự đổi truy vấn khi bộ lọc thay đổi. */
    private void quanSatDanhSach() {
        viewModel.getDanhSach().observe(this, list -> {
            adapter.submitList(list);
            int n = list == null ? 0 : list.size();
            binding.tvSoLuong.setText(n == 0
                    ? getString(R.string.khong_co_bien_khop)
                    : getString(R.string.n_bien_bao, n));
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
                .setTitle(R.string.xoa_bien_bao)
                .setMessage(getString(R.string.hoi_xoa_bien, sign.maBien, sign.tenBien))
                .setPositiveButton(R.string.xoa, (d, w) -> viewModel.xoa(sign, () ->
                        Toast.makeText(this, R.string.da_xoa_bien_bao, Toast.LENGTH_SHORT).show()))
                .setNegativeButton(R.string.huy, null)
                .show();
    }
}
