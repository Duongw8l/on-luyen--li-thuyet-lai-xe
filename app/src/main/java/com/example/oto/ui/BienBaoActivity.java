package com.example.oto.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.oto.R;
import com.example.oto.data.entity.TrafficSign;
import com.example.oto.databinding.ActivityBienBaoBinding;
import com.example.oto.ui.viewmodel.BienBaoViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Tra cứu biển báo: RecyclerView + Spinner lọc nhóm + ô tìm kiếm theo tên/mã biển.
 * Bấm vào một biển -> Intent tường minh mở màn hình chi tiết.
 */
public class BienBaoActivity extends AppCompatActivity {

    // Nhãn "tất cả nhóm" lấy từ strings.xml lúc chạy (xem onCreate).
    private String tatCa;

    private BienBaoViewModel viewModel;
    private BienBaoAdapter adapter;
    private ActivityBienBaoBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBienBaoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setTitle(getString(R.string.menu_bien_bao));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        tatCa = getString(R.string.tat_ca_nhom);
        viewModel = new ViewModelProvider(this).get(BienBaoViewModel.class);

        binding.rvBienBao.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BienBaoAdapter(this::moChiTiet);
        binding.rvBienBao.setAdapter(adapter);

        napSpinnerNhom();
        theoDoiTimKiem();
        quanSatDanhSach();
    }

    private void napSpinnerNhom() {
        viewModel.getNhomBien().observe(this, nhomList -> {
            List<String> items = new ArrayList<>();
            items.add(tatCa);
            if (nhomList != null) {
                items.addAll(nhomList);
            }
            ArrayAdapter<String> ad = new ArrayAdapter<>(
                    this, android.R.layout.simple_spinner_dropdown_item, items);
            binding.spinnerNhom.setAdapter(ad);
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

    /**
     * Chỉ đăng ký quan sát MỘT lần. Mỗi khi bộ lọc đổi, ViewModel tự chuyển sang
     * truy vấn mới bằng switchMap — Activity không phải gỡ/đăng ký lại observer.
     */
    private void quanSatDanhSach() {
        viewModel.getDanhSach().observe(this, list -> {
            adapter.submitList(list);
            int n = list == null ? 0 : list.size();
            binding.tvSoLuong.setText(n == 0
                    ? getString(R.string.khong_tim_thay_bien)
                    : getString(R.string.tim_thay_n_bien, n));
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
