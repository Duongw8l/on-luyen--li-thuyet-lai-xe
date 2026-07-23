package com.example.oto.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioButton;
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
import com.example.oto.data.entity.Answer;
import com.example.oto.data.entity.Chapter;
import com.example.oto.data.entity.Question;
import com.example.oto.data.relation.QuestionWithAnswers;
import com.example.oto.databinding.ActivitySuaCauHoiBinding;
import com.example.oto.ui.viewmodel.SuaCauHoiViewModel;
import com.example.oto.util.AnhUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Form thêm mới / sửa một câu hỏi (tiêu chí 2.1).
 *
 * Ràng buộc nghiệp vụ được kiểm tra trước khi lưu:
 * - nội dung câu hỏi không rỗng;
 * - có ít nhất 2 đáp án;
 * - có ĐÚNG MỘT đáp án đúng, và đáp án đúng đó phải có nội dung.
 * RadioGroup đảm bảo phần "đúng một" ngay ở giao diện.
 */
public class SuaCauHoiActivity extends AppCompatActivity {

    /** Không truyền extra này = thêm câu hỏi mới. */
    public static final String EXTRA_QUESTION_ID = "question_id";

    private SuaCauHoiViewModel viewModel;

    private ActivitySuaCauHoiBinding binding;

    /** Gom 4 ô đáp án / 4 nút chọn thành mảng để duyệt bằng vòng lặp. */
    private EditText[] oDapAn;
    private RadioButton[] nutDung;

    /** Vị trí (0..3) của đáp án đang được đánh dấu đúng; -1 = chưa chọn. */
    private int viTriDung = -1;

    private final List<Integer> idChuongTheoViTri = new ArrayList<>();

    private int questionId = 0;         // 0 = thêm mới
    private Question questionDangSua;   // null khi thêm mới
    private int chuongCanChon = 0;      // chương của câu đang sửa, chờ Spinner nạp xong

    /** Ảnh minh hoạ đang chọn: đường dẫn file cục bộ ('/...') hoặc tên asset. null = không ảnh. */
    private String anhUrl;
    /** Ảnh đã lưu trong Room khi mở form — để dọn file cũ khi thay/bỏ ảnh. */
    private String anhUrlGoc;
    /** True khi đã Lưu/Xoá xong — để finish() không dọn nhầm ảnh vừa ghi vào Room. */
    private boolean daHoanTat;
    /** Uri file tạm cho camera ghi ảnh vào. */
    private Uri uriAnhTam;

    private final ActivityResultLauncher<String> chonAnhThuVien =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    luuAnh(uri);
                }
            });

    private final ActivityResultLauncher<Uri> chupAnh =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), thanhCong -> {
                if (Boolean.TRUE.equals(thanhCong) && uriAnhTam != null) {
                    luuAnh(uriAnhTam);
                }
            });

    private final ActivityResultLauncher<String> xinQuyenCamera =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), duocPhep -> {
                if (Boolean.TRUE.equals(duocPhep)) {
                    moCamera();
                } else {
                    Toast.makeText(this, R.string.can_quyen_camera_cau_hoi, Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (VaiTro.chanNeuKhongPhaiAdmin(this)) {
            return;
        }
        binding = ActivitySuaCauHoiBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        viewModel = new ViewModelProvider(this).get(SuaCauHoiViewModel.class);

        oDapAn = new EditText[]{
                binding.edtA, binding.edtB, binding.edtC, binding.edtD};
        nutDung = new RadioButton[]{
                binding.rbA, binding.rbB, binding.rbC, binding.rbD};
        for (int i = 0; i < nutDung.length; i++) {
            final int viTri = i;
            nutDung[i].setOnClickListener(v -> chonDapAnDung(viTri));
        }

        questionId = getIntent().getIntExtra(EXTRA_QUESTION_ID, 0);
        setTitle(questionId == 0
                ? getString(R.string.them_cau_hoi)
                : getString(R.string.sua_cau_hoi));

        binding.btnXoa.setVisibility(questionId == 0 ? View.GONE : View.VISIBLE);
        binding.btnXoa.setOnClickListener(v -> xacNhanXoa());
        binding.btnLuu.setOnClickListener(v -> luu());

        binding.btnThemAnh.setOnClickListener(v -> chonNguonAnh());
        binding.imgCauHoi.setOnClickListener(v -> chonNguonAnh());
        binding.btnBoAnh.setOnClickListener(v -> boAnh());

        napSpinnerChuong();
        if (questionId != 0) {
            napCauHoiDeSua();
        }
    }

    private void napSpinnerChuong() {
        viewModel.getChuong().observe(this, chapters -> {
            List<String> nhan = new ArrayList<>();
            idChuongTheoViTri.clear();
            if (chapters != null) {
                for (Chapter c : chapters) {
                    nhan.add(c.toString());
                    idChuongTheoViTri.add(c.id);
                }
            }
            binding.spinnerChuong.setAdapter(new ArrayAdapter<>(
                    this, android.R.layout.simple_spinner_dropdown_item, nhan));

            if (chuongCanChon > 0) {
                chonChuong(chuongCanChon);
                chuongCanChon = 0;
            }
        });
    }

    private void chonChuong(int chapterId) {
        int viTri = idChuongTheoViTri.indexOf(chapterId);
        if (viTri >= 0) {
            binding.spinnerChuong.setSelection(viTri);
        }
    }

    /** Đổ dữ liệu câu hỏi cũ lên form. */
    private void napCauHoiDeSua() {
        viewModel.getCauHoi().observe(this, res -> {
            if (res.laLoi()) {
                Toast.makeText(this, res.thongBaoLoi, Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            if (!res.laThanhCong() || res.duLieu == null) {
                return;
            }
            QuestionWithAnswers qa = res.duLieu;
            questionDangSua = qa.question;

            binding.edtNoiDung.setText(qa.question.noiDung);
            binding.edtGiaiThich.setText(qa.question.giaiThich);
            binding.cbDiemLiet.setChecked(qa.question.isDiemLiet);
            anhUrl = anhUrlGoc = qa.question.anhUrl;
            hienAnh();

            // Spinner có thể chưa nạp xong -> nhớ lại để chọn sau.
            if (idChuongTheoViTri.isEmpty()) {
                chuongCanChon = qa.question.chapterId;
            } else {
                chonChuong(qa.question.chapterId);
            }

            for (int i = 0; i < oDapAn.length && i < qa.answers.size(); i++) {
                Answer a = qa.answers.get(i);
                oDapAn[i].setText(a.noiDung);
                if (a.isCorrect) {
                    chonDapAnDung(i);
                }
            }
        });
        viewModel.napNeuChua(questionId);
    }

    private void luu() {
        String noiDung = binding.edtNoiDung.getText().toString().trim();
        if (noiDung.isEmpty()) {
            binding.edtNoiDung.setError(getString(R.string.loi_chua_nhap_noi_dung));
            return;
        }
        if (idChuongTheoViTri.isEmpty()) {
            Toast.makeText(this, R.string.loi_chua_nap_chuong, Toast.LENGTH_SHORT).show();
            return;
        }

        if (viTriDung < 0) {
            Toast.makeText(this, R.string.loi_chua_danh_dau_dap_an, Toast.LENGTH_SHORT).show();
            return;
        }
        if (oDapAn[viTriDung].getText().toString().trim().isEmpty()) {
            oDapAn[viTriDung].setError(getString(R.string.loi_dap_an_dung_trong));
            return;
        }

        // Chỉ lưu các đáp án có nội dung; đáp án đúng luôn nằm trong số đó.
        List<Answer> dapAn = new ArrayList<>();
        int chiSoDung = -1;
        for (int i = 0; i < oDapAn.length; i++) {
            String noiDungDapAn = oDapAn[i].getText().toString().trim();
            if (noiDungDapAn.isEmpty()) {
                continue;
            }
            if (i == viTriDung) {
                chiSoDung = dapAn.size();
            }
            dapAn.add(new Answer(0, noiDungDapAn, i == viTriDung));
        }
        if (dapAn.size() < 2 || chiSoDung < 0) {
            Toast.makeText(this, R.string.loi_it_nhat_2_dap_an, Toast.LENGTH_SHORT).show();
            return;
        }

        Question q = questionDangSua == null ? new Question() : questionDangSua;
        q.id = questionId; // 0 khi thêm mới
        q.noiDung = noiDung;
        q.chapterId = idChuongTheoViTri.get(binding.spinnerChuong.getSelectedItemPosition());
        q.isDiemLiet = binding.cbDiemLiet.isChecked();
        q.giaiThich = binding.edtGiaiThich.getText().toString().trim();
        q.anhUrl = anhUrl;

        boolean laThemMoi = questionId == 0;
        viewModel.luu(q, dapAn, dongBoOk -> {
            // Ảnh cũ đã bị thay -> xoá file cục bộ cũ cho khỏi rác (không đụng ảnh assets).
            if (laAnhCucBo(anhUrlGoc) && !anhUrlGoc.equals(anhUrl)) {
                AnhUtil.xoaAnh(anhUrlGoc);
            }
            daHoanTat = true;
            int thongBao;
            if (!dongBoOk) {
                // Đã lưu vào Room nhưng chưa đẩy lên máy chủ được (mất mạng/quyền).
                thongBao = R.string.da_luu_chua_dong_bo;
            } else {
                thongBao = laThemMoi ? R.string.da_them_cau_hoi : R.string.da_luu_thay_doi;
            }
            Toast.makeText(this, thongBao, Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    /** Đánh dấu một đáp án là đúng và bỏ chọn các đáp án còn lại. */
    private void chonDapAnDung(int viTri) {
        viTriDung = viTri;
        for (int i = 0; i < nutDung.length; i++) {
            nutDung[i].setChecked(i == viTri);
        }
    }

    // ---------- Ảnh minh hoạ ----------

    /** Hiển thị ảnh hiện tại (assets hoặc file cục bộ); ẩn ImageView nếu không có ảnh. */
    private void hienAnh() {
        Bitmap anh = AnhUtil.docAnhCauHoi(this, anhUrl);
        if (anh != null) {
            binding.imgCauHoi.setImageBitmap(anh);
            binding.imgCauHoi.setVisibility(View.VISIBLE);
            binding.btnBoAnh.setVisibility(View.VISIBLE);
            binding.btnThemAnh.setText(R.string.doi_anh_cau_hoi);
        } else {
            binding.imgCauHoi.setVisibility(View.GONE);
            binding.btnBoAnh.setVisibility(View.GONE);
            binding.btnThemAnh.setText(R.string.them_anh_cau_hoi);
        }
    }

    /** Hộp thoại chọn nguồn ảnh: thư viện hay chụp mới. */
    private void chonNguonAnh() {
        String[] luaChon = {
                getString(R.string.chon_anh_thu_vien),
                getString(R.string.chup_anh_camera)};
        new AlertDialog.Builder(this)
                .setTitle(R.string.them_anh_cau_hoi)
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
        uriAnhTam = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", tam);
        chupAnh.launch(uriAnhTam);
    }

    /** Nén + lưu ảnh vào bộ nhớ trong rồi hiển thị. Đường dẫn ghi vào Room khi bấm Lưu. */
    private void luuAnh(Uri nguon) {
        String duongDan = AnhUtil.luuAnhCauHoi(this, nguon);
        if (duongDan == null) {
            Toast.makeText(this, R.string.loi_khong_doc_duoc_anh, Toast.LENGTH_SHORT).show();
            return;
        }
        // Dọn file tạm của lần chọn trước trong phiên (không đụng ảnh đã lưu trong Room).
        if (laAnhCucBo(anhUrl) && !anhUrl.equals(anhUrlGoc)) {
            AnhUtil.xoaAnh(anhUrl);
        }
        anhUrl = duongDan;
        hienAnh();
    }

    /** Bỏ ảnh khỏi câu hỏi (chỉ đánh dấu; file cục bộ được dọn khi Lưu hoặc rời form). */
    private void boAnh() {
        if (laAnhCucBo(anhUrl) && !anhUrl.equals(anhUrlGoc)) {
            AnhUtil.xoaAnh(anhUrl);
        }
        anhUrl = null;
        hienAnh();
    }

    /** True nếu chuỗi là đường dẫn file cục bộ (được phép xoá), không phải tên asset. */
    private boolean laAnhCucBo(String s) {
        return s != null && s.startsWith("/");
    }

    private void xacNhanXoa() {
        if (questionDangSua == null) {
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.xoa_cau_hoi)
                .setMessage(R.string.hoi_xoa_cau_hoi_cascade)
                .setPositiveButton(R.string.xoa, (d, w) -> viewModel.xoa(questionDangSua, () -> {
                    // Xoá luôn file ảnh cục bộ của câu (cả ảnh đã lưu lẫn ảnh tạm vừa chọn).
                    if (laAnhCucBo(anhUrlGoc)) {
                        AnhUtil.xoaAnh(anhUrlGoc);
                    }
                    if (laAnhCucBo(anhUrl) && !anhUrl.equals(anhUrlGoc)) {
                        AnhUtil.xoaAnh(anhUrl);
                    }
                    daHoanTat = true;
                    Toast.makeText(this, R.string.da_xoa_cau_hoi, Toast.LENGTH_SHORT).show();
                    finish();
                }))
                .setNegativeButton(R.string.huy, null)
                .show();
    }

    /**
     * Rời form mà chưa Lưu/Xoá: dọn file ảnh tạm đã chọn trong phiên (ảnh đang lưu trong
     * Room giữ nguyên). Bắt cả Back phần cứng lẫn nút Up trên ActionBar.
     */
    @Override
    public void finish() {
        if (!daHoanTat && laAnhCucBo(anhUrl) && !anhUrl.equals(anhUrlGoc)) {
            AnhUtil.xoaAnh(anhUrl);
        }
        super.finish();
    }
}
