package com.example.oto.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.oto.R;
import com.example.oto.auth.VaiTro;
import com.example.oto.data.QuizRepository;
import com.example.oto.data.entity.TrafficSign;

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

    private int signId = 0;            // 0 = thêm mới
    private TrafficSign signDangSua;   // null khi thêm mới

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
        spinnerNhom = findViewById(R.id.spinnerNhom);
        spinnerNhom.setAdapter(new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, NHOM_BIEN));

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

            int viTri = NHOM_BIEN.indexOf(sign.nhomBien);
            if (viTri >= 0) {
                spinnerNhom.setSelection(viTri);
            }
        });
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

        repo.saveSign(s, ok -> {
            if (!ok) {
                edtMaBien.setError("Mã biển này đã tồn tại");
                Toast.makeText(this, "Mã biển đã tồn tại — hãy dùng mã khác.",
                        Toast.LENGTH_SHORT).show();
                return;
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
                    Toast.makeText(this, "Đã xoá biển báo.", Toast.LENGTH_SHORT).show();
                    finish();
                }))
                .setNegativeButton("Huỷ", null)
                .show();
    }
}
