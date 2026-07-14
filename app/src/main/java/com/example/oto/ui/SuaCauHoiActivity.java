package com.example.oto.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.oto.R;
import com.example.oto.auth.VaiTro;
import com.example.oto.data.QuizRepository;
import com.example.oto.data.entity.Answer;
import com.example.oto.data.entity.Chapter;
import com.example.oto.data.entity.Question;
import com.example.oto.data.relation.QuestionWithAnswers;

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

    private QuizRepository repo;

    private EditText edtNoiDung, edtGiaiThich;
    private EditText[] oDapAn;
    private RadioButton[] nutDung;
    private Spinner spinnerChuong;
    private CheckBox cbDiemLiet;

    /** Vị trí (0..3) của đáp án đang được đánh dấu đúng; -1 = chưa chọn. */
    private int viTriDung = -1;

    private final List<Integer> idChuongTheoViTri = new ArrayList<>();

    private int questionId = 0;         // 0 = thêm mới
    private Question questionDangSua;   // null khi thêm mới
    private int chuongCanChon = 0;      // chương của câu đang sửa, chờ Spinner nạp xong

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (VaiTro.chanNeuKhongPhaiAdmin(this)) {
            return;
        }
        setContentView(R.layout.activity_sua_cau_hoi);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        repo = new QuizRepository(this);

        edtNoiDung = findViewById(R.id.edtNoiDung);
        edtGiaiThich = findViewById(R.id.edtGiaiThich);
        spinnerChuong = findViewById(R.id.spinnerChuong);
        cbDiemLiet = findViewById(R.id.cbDiemLiet);

        oDapAn = new EditText[]{
                findViewById(R.id.edtA), findViewById(R.id.edtB),
                findViewById(R.id.edtC), findViewById(R.id.edtD)};
        nutDung = new RadioButton[]{
                findViewById(R.id.rbA), findViewById(R.id.rbB),
                findViewById(R.id.rbC), findViewById(R.id.rbD)};
        for (int i = 0; i < nutDung.length; i++) {
            final int viTri = i;
            nutDung[i].setOnClickListener(v -> chonDapAnDung(viTri));
        }

        questionId = getIntent().getIntExtra(EXTRA_QUESTION_ID, 0);
        setTitle(questionId == 0
                ? getString(R.string.them_cau_hoi)
                : getString(R.string.sua_cau_hoi));

        View btnXoa = findViewById(R.id.btnXoa);
        btnXoa.setVisibility(questionId == 0 ? View.GONE : View.VISIBLE);
        btnXoa.setOnClickListener(v -> xacNhanXoa());
        findViewById(R.id.btnLuu).setOnClickListener(v -> luu());

        napSpinnerChuong();
        if (questionId != 0) {
            napCauHoiDeSua();
        }
    }

    private void napSpinnerChuong() {
        repo.getChapters().observe(this, chapters -> {
            List<String> nhan = new ArrayList<>();
            idChuongTheoViTri.clear();
            if (chapters != null) {
                for (Chapter c : chapters) {
                    nhan.add(c.toString());
                    idChuongTheoViTri.add(c.id);
                }
            }
            spinnerChuong.setAdapter(new ArrayAdapter<>(
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
            spinnerChuong.setSelection(viTri);
        }
    }

    /** Đổ dữ liệu câu hỏi cũ lên form. */
    private void napCauHoiDeSua() {
        repo.getQuestion(questionId, qa -> {
            if (qa == null || qa.question == null) {
                Toast.makeText(this, "Không tìm thấy câu hỏi.", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            questionDangSua = qa.question;

            edtNoiDung.setText(qa.question.noiDung);
            edtGiaiThich.setText(qa.question.giaiThich);
            cbDiemLiet.setChecked(qa.question.isDiemLiet);

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
    }

    private void luu() {
        String noiDung = edtNoiDung.getText().toString().trim();
        if (noiDung.isEmpty()) {
            edtNoiDung.setError("Chưa nhập nội dung câu hỏi");
            return;
        }
        if (idChuongTheoViTri.isEmpty()) {
            Toast.makeText(this, "Chưa nạp xong danh sách chương.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (viTriDung < 0) {
            Toast.makeText(this, "Hãy đánh dấu đáp án đúng.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (oDapAn[viTriDung].getText().toString().trim().isEmpty()) {
            oDapAn[viTriDung].setError("Đáp án đúng không được để trống");
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
            Toast.makeText(this, "Câu hỏi phải có ít nhất 2 đáp án.", Toast.LENGTH_SHORT).show();
            return;
        }

        Question q = questionDangSua == null ? new Question() : questionDangSua;
        q.id = questionId; // 0 khi thêm mới
        q.noiDung = noiDung;
        q.chapterId = idChuongTheoViTri.get(spinnerChuong.getSelectedItemPosition());
        q.isDiemLiet = cbDiemLiet.isChecked();
        q.giaiThich = edtGiaiThich.getText().toString().trim();

        repo.saveQuestion(q, dapAn, ok -> {
            Toast.makeText(this,
                    questionId == 0 ? "Đã thêm câu hỏi." : "Đã lưu thay đổi.",
                    Toast.LENGTH_SHORT).show();
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

    private void xacNhanXoa() {
        if (questionDangSua == null) {
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("Xoá câu hỏi")
                .setMessage("Xoá câu hỏi này? Đáp án của nó cũng bị xoá theo (CASCADE).")
                .setPositiveButton("Xoá", (d, w) -> repo.deleteQuestion(questionDangSua, ok -> {
                    Toast.makeText(this, "Đã xoá câu hỏi.", Toast.LENGTH_SHORT).show();
                    finish();
                }))
                .setNegativeButton("Huỷ", null)
                .show();
    }
}
