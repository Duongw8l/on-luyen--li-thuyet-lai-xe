package com.example.oto.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.oto.R;
import com.example.oto.auth.AuthManager;
import com.example.oto.auth.VaiTro;
import com.example.oto.data.DatabaseSeeder;
import com.example.oto.data.QuizRepository;
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

    private QuizRepository repo;
    private AuthManager auth;

    private ImageView imgAvatar;
    private TextView tvTen, tvEmail, tvTrangThaiEmail;

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
                            "Cần quyền Camera để chụp ảnh đại diện.", Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ca_nhan);
        setTitle(getString(R.string.menu_ca_nhan));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        repo = new QuizRepository(this);
        auth = new AuthManager();

        imgAvatar = findViewById(R.id.imgAvatar);
        tvTen = findViewById(R.id.tvTen);
        tvEmail = findViewById(R.id.tvEmail);
        tvTrangThaiEmail = findViewById(R.id.tvTrangThaiEmail);

        findViewById(R.id.btnDoiAnh).setOnClickListener(v -> chonNguonAnh());
        imgAvatar.setOnClickListener(v -> chonNguonAnh());
        findViewById(R.id.btnXoaAnh).setOnClickListener(v -> xoaAnh());

        hienThongTinTaiKhoan();
        hienAnhDaiDien();
    }

    private void hienThongTinTaiKhoan() {
        FirebaseUser u = auth.getUser();
        if (u == null) {
            tvTen.setText("Học viên (dùng offline)");
            tvEmail.setText("Chưa đăng nhập");
            tvTrangThaiEmail.setVisibility(android.view.View.GONE);
            return;
        }
        tvTen.setText(u.getDisplayName() == null ? "Học viên" : u.getDisplayName());
        tvEmail.setText(u.getEmail());

        String vaiTro = VaiTro.laAdmin(this) ? "Quản trị viên" : "Học viên";
        String xacMinh = u.isEmailVerified()
                ? "✓ Email đã xác minh"
                : "⚠ Email chưa xác minh — kiểm tra hộp thư";
        tvTrangThaiEmail.setText("Vai trò: " + vaiTro + "\n" + xacMinh);
    }

    private void hienAnhDaiDien() {
        Bitmap anh = AnhUtil.docAnhDaiDien(this);
        if (anh != null) {
            imgAvatar.setImageBitmap(anh);
        } else {
            imgAvatar.setImageResource(R.drawable.ic_avatar_mac_dinh);
        }
    }

    /** Hộp thoại cho chọn: lấy ảnh từ thư viện hay chụp mới. */
    private void chonNguonAnh() {
        String[] luaChon = {"Chọn ảnh từ thư viện", "Chụp ảnh bằng camera"};
        new AlertDialog.Builder(this)
                .setTitle("Ảnh đại diện")
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
            Toast.makeText(this, "Không đọc được ảnh này.", Toast.LENGTH_SHORT).show();
            return;
        }
        repo.capNhatAnhDaiDien(DatabaseSeeder.LOCAL_USER_ID, duongDan);
        hienAnhDaiDien();
        Toast.makeText(this, "Đã cập nhật ảnh đại diện.", Toast.LENGTH_SHORT).show();
    }

    private void xoaAnh() {
        File f = AnhUtil.fileAnhDaiDien(this);
        if (!f.exists()) {
            Toast.makeText(this, "Chưa có ảnh đại diện.", Toast.LENGTH_SHORT).show();
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("Xoá ảnh đại diện")
                .setMessage("Trở về ảnh mặc định?")
                .setPositiveButton("Xoá", (d, w) -> {
                    if (f.delete()) {
                        repo.capNhatAnhDaiDien(DatabaseSeeder.LOCAL_USER_ID, null);
                        hienAnhDaiDien();
                    }
                })
                .setNegativeButton("Huỷ", null)
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
