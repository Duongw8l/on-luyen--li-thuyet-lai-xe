package com.example.oto.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.oto.R;
import com.example.oto.auth.VaiTro;
import com.example.oto.data.entity.Chapter;
import com.example.oto.data.relation.QuestionWithAnswers;
import com.example.oto.databinding.ActivityAdminBinding;
import com.example.oto.ui.viewmodel.AdminCauHoiViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Màn Quản trị ngân hàng câu hỏi (tiêu chí 2.1): xem danh sách, tìm kiếm theo từ khóa,
 * lọc theo chương / câu điểm liệt, thêm, sửa, xoá.
 */
public class AdminActivity extends AppCompatActivity implements CauHoiAdminAdapter.OnItem {

    private AdminCauHoiViewModel viewModel;
    private CauHoiAdminAdapter adapter;
    private ActivityAdminBinding binding;

    /** id chương tương ứng từng vị trí trong Spinner (vị trí 0 = tất cả). */
    private final List<Integer> idTheoViTri = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (VaiTro.chanNeuKhongPhaiAdmin(this)) {
            return;
        }
        binding = ActivityAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setTitle(getString(R.string.menu_admin));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        viewModel = new ViewModelProvider(this).get(AdminCauHoiViewModel.class);

        binding.rvCauHoi.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CauHoiAdminAdapter(this);
        binding.rvCauHoi.setAdapter(adapter);

        binding.fabThem.setOnClickListener(v ->
                startActivity(new Intent(this, SuaCauHoiActivity.class)));

        binding.cbDiemLiet.setOnCheckedChangeListener(
                (v, checked) -> viewModel.datChiDiemLiet(checked));

        theoDoiTimKiem();
        napSpinnerChuong();
        quanSatDanhSach();

        // Mở màn Quản trị là kéo câu hỏi mới từ máy chủ về (im lặng, không phiền admin).
        dongBo(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.admin_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_dong_bo) {
            dongBo(true); // bấm tay -> có thông báo kết quả
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Đồng bộ ngân hàng câu hỏi với Firestore.
     *
     * @param baoKetQua true khi admin bấm "Đồng bộ ngay" (hiện thông báo); false khi
     *                  chạy tự động lúc mở màn hình (chỉ báo khi có lỗi).
     */
    private void dongBo(boolean baoKetQua) {
        if (baoKetQua) {
            Toast.makeText(this, R.string.dong_bo_dang_chay, Toast.LENGTH_SHORT).show();
        }
        viewModel.dongBo(
                kq -> {
                    if (baoKetQua) {
                        Toast.makeText(this,
                                getString(R.string.dong_bo_xong, kq.soCauKeoVe, kq.soCauDayLen),
                                Toast.LENGTH_LONG).show();
                    }
                },
                loi -> {
                    if (baoKetQua) {
                        Toast.makeText(this, loi, Toast.LENGTH_LONG).show();
                    }
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

    private void napSpinnerChuong() {
        viewModel.getChuong().observe(this, chapters -> {
            // Mục đầu tiên là "Tất cả chương", ứng với id 0 trong truy vấn lọc.
            List<String> nhan = new ArrayList<>();
            idTheoViTri.clear();
            nhan.add(getString(R.string.tat_ca_chuong));
            idTheoViTri.add(0);
            if (chapters != null) {
                for (Chapter c : chapters) {
                    nhan.add(c.toString());
                    idTheoViTri.add(c.id);
                }
            }

            ArrayAdapter<String> ad = new ArrayAdapter<>(
                    this, android.R.layout.simple_spinner_dropdown_item, nhan);
            binding.spinnerChuong.setAdapter(ad);
            binding.spinnerChuong.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    viewModel.datChuong(idTheoViTri.get(position));
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
                    ? getString(R.string.khong_co_cau_hoi_khop)
                    : getString(R.string.n_cau_hoi, n));
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
                .setTitle(R.string.xoa_cau_hoi)
                .setMessage(getString(R.string.hoi_xoa_cau_hoi_so, qa.question.id))
                .setPositiveButton(R.string.xoa, (d, w) -> viewModel.xoa(qa.question, () ->
                        Toast.makeText(this, R.string.da_xoa_cau_hoi, Toast.LENGTH_SHORT).show()))
                .setNegativeButton(R.string.huy, null)
                .show();
    }
}
