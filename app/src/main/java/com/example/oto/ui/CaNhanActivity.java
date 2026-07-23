package com.example.oto.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;

import com.example.oto.R;
import com.example.oto.auth.AuthManager;
import com.example.oto.auth.VaiTro;
import com.example.oto.databinding.ActivityCaNhanBinding;
import com.example.oto.ui.viewmodel.CaNhanViewModel;
import com.example.oto.util.AnhUtil;
import com.google.firebase.auth.FirebaseUser;

import java.io.File;

/**
 * Màn hình cá nhân (tiêu chí 2.2 — xử lý hình ảnh).
 *
 * Ảnh đại diện có thể lấy từ hai nguồn:
 * - Thư viện ảnh của máy (Intent ngầm mở app Ảnh — không cần xin quyền vì hệ thống
 *   chỉ trả về đúng tấm ảnh người dùng chọn);
 * - Camera (Intent ngầm ACTION_IMAGE_CAPTURE — cần xin quyền CAMERA lúc chạy).
 *
 * Ảnh sau đó được nén và lưu vào bộ nhớ riêng của app, đường dẫn lưu trong Room.
 */
public class CaNhanActivity extends AppCompatActivity {

    private CaNhanViewModel viewModel;
    private AuthManager auth;

    private ActivityCaNhanBinding binding;

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
                            R.string.can_quyen_camera_dai_dien, Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCaNhanBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setTitle(getString(R.string.menu_ca_nhan));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        viewModel = new ViewModelProvider(this).get(CaNhanViewModel.class);
        auth = new AuthManager();

        binding.btnGuiLaiXacMinh.setOnClickListener(v -> guiLaiXacMinh());
        binding.btnDoiAnh.setOnClickListener(v -> chonNguonAnh());
        binding.imgAvatar.setOnClickListener(v -> chonNguonAnh());
        binding.btnXoaAnh.setOnClickListener(v -> xoaAnh());

        hienThongTinTaiKhoan();
        hienAnhDaiDien();
    }

    private void hienThongTinTaiKhoan() {
        FirebaseUser u = auth.getUser();
        if (u == null) {
            binding.tvTen.setText(R.string.ten_hoc_vien_offline);
            binding.tvEmail.setText(R.string.chua_dang_nhap);
            binding.tvTrangThaiEmail.setVisibility(View.GONE);
            binding.btnGuiLaiXacMinh.setVisibility(View.GONE);
            return;
        }
        veThongTin(u);

        // isEmailVerified() đọc từ token đã cache trong máy: người dùng bấm liên kết trong
        // hộp thư xong quay lại app vẫn thấy "chưa xác minh" cho tới khi token tự làm mới
        // (khoảng 1 giờ) hoặc đăng nhập lại. reload() hỏi thẳng máy chủ nên biết ngay.
        // Không có mạng thì reload hỏng — cứ giữ nguyên trạng thái cache đang hiện.
        if (!u.isEmailVerified()) {
            u.reload().addOnSuccessListener(x -> {
                FirebaseUser moi = auth.getUser();
                if (moi != null && !isFinishing()) {
                    veThongTin(moi);
                }
            });
        }
    }

    private void veThongTin(FirebaseUser u) {
        binding.tvTen.setText(u.getDisplayName() == null ? getString(R.string.ten_hoc_vien) : u.getDisplayName());
        binding.tvEmail.setText(u.getEmail());

        String vaiTro = getString(VaiTro.laAdmin(this)
                ? R.string.vai_tro_quan_tri : R.string.vai_tro_hoc_vien);
        boolean daXacMinh = u.isEmailVerified();
        String xacMinh = getString(daXacMinh
                ? R.string.email_da_xac_minh : R.string.email_chua_xac_minh);
        binding.tvTrangThaiEmail.setVisibility(View.VISIBLE);
        binding.tvTrangThaiEmail.setText(
                getString(R.string.vai_tro_va_xac_minh, vaiTro, xacMinh));
        binding.btnGuiLaiXacMinh.setVisibility(daXacMinh ? View.GONE : View.VISIBLE);
    }

    /** Gửi lại email xác minh — lối thoát khi email lúc đăng ký không tới nơi. */
    private void guiLaiXacMinh() {
        binding.btnGuiLaiXacMinh.setEnabled(false);
        auth.guiLaiEmailXacMinh((thanhCong, loi) -> {
            binding.btnGuiLaiXacMinh.setEnabled(true);
            if (thanhCong) {
                Toast.makeText(this, R.string.da_gui_email_xac_minh_huong_dan,
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this,
                        loi == null ? getString(R.string.loi_khong_gui_duoc_xac_minh) : loi,
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void hienAnhDaiDien() {
        Bitmap anh = AnhUtil.docAnhDaiDien(this);
        if (anh != null) {
            binding.imgAvatar.setImageBitmap(anh);
        } else {
            binding.imgAvatar.setImageResource(R.drawable.ic_avatar_mac_dinh);
        }
    }

    /** Hộp thoại cho chọn: lấy ảnh từ thư viện hay chụp mới. */
    private void chonNguonAnh() {
        String[] luaChon = {
                getString(R.string.chon_anh_thu_vien),
                getString(R.string.chup_anh_camera)};
        new AlertDialog.Builder(this)
                .setTitle(R.string.anh_dai_dien)
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

    /** Nén ảnh, lưu vào bộ nhớ trong, ghi đường dẫn vào Room rồi hiển thị. */
    private void luuAnh(Uri nguon) {
        String duongDan = AnhUtil.luuAnhDaiDien(this, nguon);
        if (duongDan == null) {
            Toast.makeText(this, R.string.loi_khong_doc_duoc_anh, Toast.LENGTH_SHORT).show();
            return;
        }
        viewModel.capNhatAnhDaiDien(duongDan);
        hienAnhDaiDien();
        Toast.makeText(this, R.string.da_cap_nhat_anh_dai_dien, Toast.LENGTH_SHORT).show();
    }

    private void xoaAnh() {
        File f = AnhUtil.fileAnhDaiDien(this);
        if (!f.exists()) {
            Toast.makeText(this, R.string.chua_co_anh_dai_dien, Toast.LENGTH_SHORT).show();
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.hoi_xoa_anh_dai_dien)
                .setMessage(R.string.hoi_ve_anh_mac_dinh)
                .setPositiveButton(R.string.xoa, (d, w) -> {
                    if (f.delete()) {
                        viewModel.capNhatAnhDaiDien(null);
                        hienAnhDaiDien();
                    }
                })
                .setNegativeButton(R.string.huy, null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        hienThongTinTaiKhoan();
    }

    /** Trang chủ mở màn này bằng Intent tường minh. */
    public static Intent taoIntent(android.content.Context context) {
        return new Intent(context, CaNhanActivity.class);
    }
}
