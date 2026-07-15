package com.example.oto.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.oto.R;
import com.example.oto.auth.VaiTro;
import com.example.oto.data.QuizRepository;
import com.example.oto.data.entity.TrafficSign;
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

    private QuizRepository repo;

    private EditText edtMaBien, edtTenBien, edtMoTa;
    private Spinner spinnerNhom;
    private ImageView imgBien;

    private int signId = 0;            // 0 = thêm mới
    private TrafficSign signDangSua;   // null khi thêm mới

    /** Đường dẫn ảnh biển đang chọn (null = chưa có ảnh). Lưu vào Room khi bấm Lưu. */
    private String anhUrl;

    /** Ảnh đã lưu trong Room khi mở form (null khi thêm mới) — dùng để dọn file cũ khi thay/xoá. */
    private String anhUrlGoc;

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
                            "Cần quyền Camera để chụp ảnh biển báo.", Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (VaiTro.chanNeuKhongPhaiAdmin(this)) {
            return;
        }
        setContentView(R.layout.activity_sua_bien_bao);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        repo = new QuizRepository(this);

        edtMaBien = findViewById(R.id.edtMaBien);
        edtTenBien = findViewById(R.id.edtTenBien);
        edtMoTa = findViewById(R.id.edtMoTa);
        imgBien = findViewById(R.id.imgBien);
        spinnerNhom = findViewById(R.id.spinnerNhom);
        spinnerNhom.setAdapter(new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, NHOM_BIEN));

        findViewById(R.id.btnDoiAnh).setOnClickListener(v -> chonNguonAnh());
        imgBien.setOnClickListener(v -> chonNguonAnh());

        signId = getIntent().getIntExtra(EXTRA_SIGN_ID, 0);
        setTitle(signId == 0
                ? getString(R.string.them_bien_bao)
                : getString(R.string.sua_bien_bao));

        View btnXoa = findViewById(R.id.btnXoa);
        btnXoa.setVisibility(signId == 0 ? View.GONE : View.VISIBLE);
        btnXoa.setOnClickListener(v -> xacNhanXoa());
        findViewById(R.id.btnLuu).setOnClickListener(v -> luu());

        if (signId != 0) {
            napBienDeSua();
        }
    }

    private void napBienDeSua() {
        repo.getSign(signId, sign -> {
            if (sign == null) {
                Toast.makeText(this, "Không tìm thấy biển báo.", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            signDangSua = sign;
            edtMaBien.setText(sign.maBien);
            edtTenBien.setText(sign.tenBien);
            edtMoTa.setText(sign.moTa);
            anhUrl = anhUrlGoc = sign.anhUrl;
            hienAnh();

            int viTri = NHOM_BIEN.indexOf(sign.nhomBien);
            if (viTri >= 0) {
                spinnerNhom.setSelection(viTri);
            }
        });
    }

    /** Hiển thị ảnh đã lưu của biển; chưa có thì dùng ảnh giữ chỗ. */
    private void hienAnh() {
        Bitmap anh = AnhUtil.docAnh(anhUrl);
        if (anh != null) {
            imgBien.setImageBitmap(anh);
        } else {
            imgBien.setImageResource(R.drawable.ic_bien_bao_placeholder);
        }
    }

    /** Hộp thoại cho chọn nguồn ảnh: thư viện hay chụp mới bằng camera. */
    private void chonNguonAnh() {
        String[] luaChon = {"Chọn ảnh từ thư viện", "Chụp ảnh bằng camera"};
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
            Toast.makeText(this, "Không đọc được ảnh này.", Toast.LENGTH_SHORT).show();
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
        String maBien = edtMaBien.getText().toString().trim();
        if (maBien.isEmpty()) {
            edtMaBien.setError("Chưa nhập mã biển");
            return;
        }
        String tenBien = edtTenBien.getText().toString().trim();
        if (tenBien.isEmpty()) {
            edtTenBien.setError("Chưa nhập tên biển");
            return;
        }

        TrafficSign s = signDangSua == null ? new TrafficSign() : signDangSua;
        s.id = signId; // 0 khi thêm mới
        s.maBien = maBien;
        s.tenBien = tenBien;
        s.nhomBien = NHOM_BIEN.get(spinnerNhom.getSelectedItemPosition());
        s.moTa = edtMoTa.getText().toString().trim();
        s.anhUrl = anhUrl;

        repo.saveSign(s, ok -> {
            if (!ok) {
                edtMaBien.setError("Mã biển này đã tồn tại");
                Toast.makeText(this, "Mã biển đã tồn tại — hãy dùng mã khác.",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            // Ảnh cũ đã bị thay bằng ảnh khác -> xoá file cũ để khỏi rác bộ nhớ.
            if (anhUrlGoc != null && !anhUrlGoc.equals(anhUrl)) {
                AnhUtil.xoaAnh(anhUrlGoc);
            }
            Toast.makeText(this,
                    signId == 0 ? "Đã thêm biển báo." : "Đã lưu thay đổi.",
                    Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void xacNhanXoa() {
        if (signDangSua == null) {
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("Xoá biển báo")
                .setMessage("Xoá biển " + signDangSua.maBien + "? Không khôi phục được.")
                .setPositiveButton("Xoá", (d, w) -> repo.deleteSign(signDangSua, ok -> {
                    // Xoá luôn file ảnh của biển: cả ảnh đã lưu lẫn ảnh tạm vừa chọn (nếu có).
                    AnhUtil.xoaAnh(anhUrlGoc);
                    if (anhUrl != null && !anhUrl.equals(anhUrlGoc)) {
                        AnhUtil.xoaAnh(anhUrl);
                    }
                    Toast.makeText(this, "Đã xoá biển báo.", Toast.LENGTH_SHORT).show();
                    finish();
                }))
                .setNegativeButton("Huỷ", null)
                .show();
    }
}
