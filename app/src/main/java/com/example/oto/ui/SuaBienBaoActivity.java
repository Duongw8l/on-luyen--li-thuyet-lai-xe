package com.example.oto.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;

import com.example.oto.R;
import com.example.oto.auth.VaiTro;
import com.example.oto.data.entity.TrafficSign;
import com.example.oto.databinding.ActivitySuaBienBaoBinding;
import com.example.oto.ui.viewmodel.SuaBienBaoViewModel;
import com.example.oto.util.AnhUtil;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * Form thêm mới / sửa một biển báo.
 *
 * Ràng buộc: mã biển và tên biển bắt buộc; mã biển không được trùng với biển khác
 * (mã biển là định danh nghiệp vụ, người dùng tra cứu theo mã này).
 */
public class SuaBienBaoActivity extends AppCompatActivity {

    /** Không truyền extra này = thêm biển báo mới. */
    public static final String EXTRA_SIGN_ID = "sign_id";

    /** Các nhóm biển theo quy chuẩn báo hiệu đường bộ. */
    private static final List<String> NHOM_BIEN = Arrays.asList(
            "Cấm", "Nguy hiểm", "Hiệu lệnh", "Chỉ dẫn", "Phụ");

    private SuaBienBaoViewModel viewModel;

    private ActivitySuaBienBaoBinding binding;

    private int signId = 0;            // 0 = thêm mới
    private TrafficSign signDangSua;   // null khi thêm mới

    /** Đường dẫn ảnh biển đang chọn (null = chưa có ảnh). Lưu vào Room khi bấm Lưu. */
    private String anhUrl;

    /** Ảnh đã lưu trong Room khi mở form (null khi thêm mới) — dùng để dọn file cũ khi thay/xoá. */
    private String anhUrlGoc;

    /** True khi đã Lưu hoặc Xoá xong — để finish() không dọn nhầm ảnh vừa được ghi vào Room. */
    private boolean daHoanTat;

    /** Uri của file tạm mà camera sẽ ghi ảnh vào. */
    private Uri uriAnhTam;

    /** Mở thư viện ảnh (bên dưới là Intent ngầm ACTION_GET_CONTENT). */
    private final ActivityResultLauncher<String> chonAnhThuVien =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    luuAnh(uri);
                }
            });

    /** Mở camera (Intent ngầm ACTION_IMAGE_CAPTURE). */
    private final ActivityResultLauncher<Uri> chupAnh =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), thanhCong -> {
                if (Boolean.TRUE.equals(thanhCong) && uriAnhTam != null) {
                    luuAnh(uriAnhTam);
                }
            });

    /** Xin quyền camera lúc chạy (runtime permission). */
    private final ActivityResultLauncher<String> xinQuyenCamera =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), duocPhep -> {
                if (Boolean.TRUE.equals(duocPhep)) {
                    moCamera();
                } else {
                    Toast.makeText(this,
                            R.string.can_quyen_camera_bien_bao, Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (VaiTro.chanNeuKhongPhaiAdmin(this)) {
            return;
        }
        binding = ActivitySuaBienBaoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        viewModel = new ViewModelProvider(this).get(SuaBienBaoViewModel.class);

        binding.spinnerNhom.setAdapter(new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, NHOM_BIEN));

        binding.btnDoiAnh.setOnClickListener(v -> chonNguonAnh());
        binding.imgBien.setOnClickListener(v -> chonNguonAnh());

        signId = getIntent().getIntExtra(EXTRA_SIGN_ID, 0);
        setTitle(signId == 0
                ? getString(R.string.them_bien_bao)
                : getString(R.string.sua_bien_bao));

        binding.btnXoa.setVisibility(signId == 0 ? View.GONE : View.VISIBLE);
        binding.btnXoa.setOnClickListener(v -> xacNhanXoa());
        binding.btnLuu.setOnClickListener(v -> luu());

        if (signId != 0) {
            napBienDeSua();
        }
    }

    private void napBienDeSua() {
        viewModel.getBienBao().observe(this, res -> {
            if (res.laLoi()) {
                Toast.makeText(this, res.thongBaoLoi, Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            if (!res.laThanhCong() || res.duLieu == null) {
                return;
            }
            TrafficSign sign = res.duLieu;
            signDangSua = sign;
            binding.edtMaBien.setText(sign.maBien);
            binding.edtTenBien.setText(sign.tenBien);
            binding.edtMoTa.setText(sign.moTa);
            anhUrl = anhUrlGoc = sign.anhUrl;
            hienAnh();

            int viTri = NHOM_BIEN.indexOf(sign.nhomBien);
            if (viTri >= 0) {
                binding.spinnerNhom.setSelection(viTri);
            }
        });
        viewModel.napNeuChua(signId);
    }

    /** Hiển thị ảnh đã lưu của biển; chưa có thì dùng ảnh giữ chỗ. */
    private void hienAnh() {
        Bitmap anh = AnhUtil.docAnh(anhUrl);
        if (anh != null) {
            binding.imgBien.setImageBitmap(anh);
        } else {
            binding.imgBien.setImageResource(R.drawable.ic_bien_bao_placeholder);
        }
    }

    /** Hộp thoại cho chọn nguồn ảnh: thư viện hay chụp mới bằng camera. */
    private void chonNguonAnh() {
        String[] luaChon = {
                getString(R.string.chon_anh_thu_vien),
                getString(R.string.chup_anh_camera)};
        new AlertDialog.Builder(this)
                .setTitle(R.string.doi_anh_bien)
                .setItems(luaChon, (d, viTri) -> {
                    if (viTri == 0) {
                        chonAnhThuVien.launch("image/*");
                    } else {
                        kiemTraQuyenRoiChup();
                    }
                })
                .show();
    }

    private void kiemTraQuyenRoiChup() {
        boolean daCoQuyen = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
        if (daCoQuyen) {
            moCamera();
        } else {
            xinQuyenCamera.launch(Manifest.permission.CAMERA);
        }
    }

    private void moCamera() {
        File tam = AnhUtil.fileAnhTam(this);
        // FileProvider: chia sẻ file của app cho app Camera một cách an toàn.
        uriAnhTam = FileProvider.getUriForFile(
                this, getPackageName() + ".fileprovider", tam);
        chupAnh.launch(uriAnhTam);
    }

    /** Nén ảnh, lưu vào bộ nhớ trong rồi hiển thị. Đường dẫn ghi vào Room khi bấm Lưu. */
    private void luuAnh(Uri nguon) {
        String duongDan = AnhUtil.luuAnhBienBao(this, nguon);
        if (duongDan == null) {
            Toast.makeText(this, R.string.loi_khong_doc_duoc_anh, Toast.LENGTH_SHORT).show();
            return;
        }
        // Dọn file tạm của lần chọn trước trong phiên này (ảnh đã lưu trong Room thì giữ lại
        // phòng khi người dùng huỷ, chỉ xoá khi bấm Lưu).
        if (anhUrl != null && !anhUrl.equals(anhUrlGoc)) {
            AnhUtil.xoaAnh(anhUrl);
        }
        anhUrl = duongDan;
        hienAnh();
    }

    private void luu() {
        String maBien = binding.edtMaBien.getText().toString().trim();
        if (maBien.isEmpty()) {
            binding.edtMaBien.setError(getString(R.string.loi_chua_nhap_ma_bien));
            return;
        }
        String tenBien = binding.edtTenBien.getText().toString().trim();
        if (tenBien.isEmpty()) {
            binding.edtTenBien.setError(getString(R.string.loi_chua_nhap_ten_bien));
            return;
        }

        TrafficSign s = signDangSua == null ? new TrafficSign() : signDangSua;
        s.id = signId; // 0 khi thêm mới
        s.maBien = maBien;
        s.tenBien = tenBien;
        s.nhomBien = NHOM_BIEN.get(binding.spinnerNhom.getSelectedItemPosition());
        s.moTa = binding.edtMoTa.getText().toString().trim();
        s.anhUrl = anhUrl;

        viewModel.luu(s, ok -> {
            if (!ok) {
                binding.edtMaBien.setError(getString(R.string.loi_ma_bien_trung));
                Toast.makeText(this, R.string.loi_ma_bien_trung_toast,
                        Toast.LENGTH_SHORT).show();
                return;
            }
            // Ảnh cũ đã bị thay bằng ảnh khác -> xoá file cũ để khỏi rác bộ nhớ.
            if (anhUrlGoc != null && !anhUrlGoc.equals(anhUrl)) {
                AnhUtil.xoaAnh(anhUrlGoc);
            }
            daHoanTat = true;
            Toast.makeText(this,
                    signId == 0 ? R.string.da_them_bien_bao : R.string.da_luu_thay_doi,
                    Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void xacNhanXoa() {
        if (signDangSua == null) {
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.xoa_bien_bao)
                .setMessage(getString(R.string.hoi_xoa_bien_ngan, signDangSua.maBien))
                .setPositiveButton(R.string.xoa, (d, w) -> viewModel.xoa(signDangSua, () -> {
                    // Xoá luôn file ảnh của biển: cả ảnh đã lưu lẫn ảnh tạm vừa chọn (nếu có).
                    AnhUtil.xoaAnh(anhUrlGoc);
                    if (anhUrl != null && !anhUrl.equals(anhUrlGoc)) {
                        AnhUtil.xoaAnh(anhUrl);
                    }
                    daHoanTat = true;
                    Toast.makeText(this, R.string.da_xoa_bien_bao, Toast.LENGTH_SHORT).show();
                    finish();
                }))
                .setNegativeButton(R.string.huy, null)
                .show();
    }

    /**
     * Chốt chung cho mọi cách rời form (Back phần cứng lẫn nút Up trên ActionBar đều
     * đi qua đây). Nếu rời mà chưa Lưu/Xoá, dọn file ảnh tạm đã chọn trong phiên;
     * ảnh đang lưu trong Room được giữ nguyên.
     */
    @Override
    public void finish() {
        if (!daHoanTat && anhUrl != null && !anhUrl.equals(anhUrlGoc)) {
            AnhUtil.xoaAnh(anhUrl);
        }
        super.finish();
    }
}
