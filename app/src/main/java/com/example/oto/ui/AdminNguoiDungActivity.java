package com.example.oto.ui;

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
import com.example.oto.auth.HoSoNguoiDung;
import com.example.oto.auth.VaiTro;
import com.example.oto.databinding.ActivityAdminNguoiDungBinding;
import com.example.oto.ui.viewmodel.AdminNguoiDungViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Quản trị người dùng: xem danh sách tài khoản, tìm theo tên/email, lọc theo vai trò,
 * nâng quyền admin hoặc hạ về người dùng thường.
 *
 * Danh sách tài khoản nằm trên Firestore (collection "users") chứ không phải trong Room —
 * Room dưới máy chỉ giữ đúng một hồ sơ của người đang dùng máy đó. Vì vậy màn này cần mạng.
 *
 * Việc đổi vai trò được Firestore Rules kiểm tra lại phía máy chủ: chỉ admin mới ghi được
 * field vaiTro của người khác, và không ai tự đổi vai trò của chính mình. Xem firestore.rules.
 */
public class AdminNguoiDungActivity extends AppCompatActivity
        implements NguoiDungAdminAdapter.OnItem {

    // Nhãn bộ lọc vai trò lấy từ strings.xml lúc chạy (xem napSpinnerVaiTro).
    private String tatCa;
    private String chiAdmin;
    private String chiUser;

    private AdminNguoiDungViewModel viewModel;
    private NguoiDungAdminAdapter adapter;
    private ActivityAdminNguoiDungBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (VaiTro.chanNeuKhongPhaiAdmin(this)) {
            return;
        }
        binding = ActivityAdminNguoiDungBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setTitle(getString(R.string.menu_admin_nguoi_dung));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        viewModel = new ViewModelProvider(this).get(AdminNguoiDungViewModel.class);

        binding.rvNguoiDung.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NguoiDungAdminAdapter(this, viewModel.uidHienTai());
        binding.rvNguoiDung.setAdapter(adapter);

        viewModel.getHienThi().observe(this, res -> {
            if (res.laDangTai()) {
                binding.tvSoLuong.setText(R.string.dang_tai_danh_sach);
                return;
            }
            if (res.laLoi()) {
                adapter.submitList(new ArrayList<>());
                binding.tvSoLuong.setText(R.string.loi_tai_danh_sach_nguoi_dung);
                return;
            }
            List<HoSoNguoiDung> ds = res.duLieu;
            adapter.submitList(ds);
            binding.tvSoLuong.setText(ds == null || ds.isEmpty()
                    ? getString(R.string.khong_co_tai_khoan_khop)
                    : getString(R.string.n_tai_khoan, ds.size()));
        });

        theoDoiTimKiem();
        napSpinnerVaiTro();
        viewModel.tai();
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

    private void napSpinnerVaiTro() {
        tatCa = getString(R.string.tat_ca_vai_tro);
        chiAdmin = getString(R.string.chi_quan_tri);
        chiUser = getString(R.string.chi_nguoi_dung_thuong);

        List<String> items = new ArrayList<>();
        items.add(tatCa);
        items.add(chiAdmin);
        items.add(chiUser);
        binding.spinnerVaiTro.setAdapter(new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, items));
        binding.spinnerVaiTro.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String chon = (String) parent.getItemAtPosition(position);
                if (chiAdmin.equals(chon)) {
                    viewModel.datVaiTroLoc(VaiTro.ADMIN);
                } else if (chiUser.equals(chon)) {
                    viewModel.datVaiTroLoc(VaiTro.USER);
                } else {
                    viewModel.datVaiTroLoc(null);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    @Override
    public void onDoiVaiTro(HoSoNguoiDung hoSo) {
        boolean nangLen = !hoSo.laAdmin();
        String thongDiep = getString(
                nangLen ? R.string.hoi_nang_quyen : R.string.hoi_thu_hoi_quyen,
                hoSo.tenHienThi());

        new AlertDialog.Builder(this)
                .setTitle(nangLen ? R.string.nang_len_admin : R.string.ha_xuong_user)
                .setMessage(thongDiep)
                .setPositiveButton(nangLen ? R.string.nang_len_admin : R.string.ha_xuong_user,
                        (d, w) -> ghiVaiTro(hoSo))
                .setNegativeButton(R.string.huy, null)
                .show();
    }

    /**
     * Việc ghi lên Firestore nằm ở Repository; Activity chỉ hiển thị kết quả.
     * Firestore Rules vẫn kiểm tra lại phía máy chủ nên nếu người gọi không phải admin,
     * thao tác bị từ chối và nhánh lỗi được gọi.
     */
    private void ghiVaiTro(HoSoNguoiDung hoSo) {
        viewModel.doiVaiTro(hoSo,
                vaiTroMoi -> Toast.makeText(this,
                        getString(R.string.da_doi_vai_tro, hoSo.tenHienThi(), vaiTroMoi),
                        Toast.LENGTH_SHORT).show(),
                loi -> Toast.makeText(this, loi, Toast.LENGTH_LONG).show());
    }
}
