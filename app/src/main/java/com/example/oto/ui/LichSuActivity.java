package com.example.oto.ui;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.oto.R;
import com.example.oto.data.DatabaseSeeder;
import com.example.oto.data.ExamConfig;
import com.example.oto.data.QuizRepository;
import com.example.oto.data.entity.Attempt;

import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Lịch sử làm bài: RecyclerView các lượt thi, lọc theo khoảng ngày (DatePicker),
 * xoá một lượt hoặc xoá toàn bộ, bấm vào một lượt để xem lại kết quả.
 */
public class LichSuActivity extends AppCompatActivity implements LichSuAdapter.OnItem {

    private static final SimpleDateFormat FMT_NGAY =
            new SimpleDateFormat("dd/MM/yyyy", new Locale("vi", "VN"));

    private QuizRepository repo;
    private LichSuAdapter adapter;
    private TextView tvTomTat;
    private MaterialButton btnTuNgay, btnDenNgay;

    @Nullable
    private LiveData<List<Attempt>> nguonHienTai;

    /** Mốc lọc, 0 = chưa chọn. */
    private long tuNgay = 0;
    private long denNgay = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lich_su);
        setTitle(getString(R.string.menu_lich_su));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        repo = new QuizRepository(this);

        tvTomTat = findViewById(R.id.tvTomTat);
        btnTuNgay = findViewById(R.id.btnTuNgay);
        btnDenNgay = findViewById(R.id.btnDenNgay);

        RecyclerView rv = findViewById(R.id.rvLichSu);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LichSuAdapter(this);
        rv.setAdapter(adapter);

        btnTuNgay.setOnClickListener(v -> chonNgay(true));
        btnDenNgay.setOnClickListener(v -> chonNgay(false));
        findViewById(R.id.btnBoLoc).setOnClickListener(v -> boLoc());
        findViewById(R.id.btnXoaTatCa).setOnClickListener(v -> xacNhanXoaTatCa());

        napDanhSach();
    }

    // ---------- Bộ lọc ngày ----------

    private void chonNgay(boolean laTuNgay) {
        Calendar c = Calendar.getInstance();
        long hienTai = laTuNgay ? tuNgay : denNgay;
        if (hienTai > 0) {
            c.setTimeInMillis(hienTai);
        }
        new DatePickerDialog(this, (view, year, month, day) -> {
            Calendar chon = Calendar.getInstance();
            if (laTuNgay) {
                chon.set(year, month, day, 0, 0, 0); // đầu ngày
                chon.set(Calendar.MILLISECOND, 0);
                tuNgay = chon.getTimeInMillis();
                btnTuNgay.setText("Từ " + FMT_NGAY.format(chon.getTime()));
            } else {
                chon.set(year, month, day, 23, 59, 59); // cuối ngày
                chon.set(Calendar.MILLISECOND, 999);
                denNgay = chon.getTimeInMillis();
                btnDenNgay.setText("Đến " + FMT_NGAY.format(chon.getTime()));
            }
            napDanhSach();
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void boLoc() {
        tuNgay = 0;
        denNgay = 0;
        btnTuNgay.setText(getString(R.string.tu_ngay));
        btnDenNgay.setText(getString(R.string.den_ngay));
        napDanhSach();
    }

    /** Chỉ lọc khi đã chọn đủ hai mốc; ngược lại lấy toàn bộ lịch sử. */
    private void napDanhSach() {
        if (nguonHienTai != null) {
            nguonHienTai.removeObservers(this);
        }
        boolean coLoc = tuNgay > 0 && denNgay > 0;
        nguonHienTai = coLoc
                ? repo.getHistoryInRange(DatabaseSeeder.LOCAL_USER_ID, tuNgay, denNgay)
                : repo.getHistory(DatabaseSeeder.LOCAL_USER_ID);

        nguonHienTai.observe(this, list -> {
            adapter.setData(list);
            tomTat(list, coLoc);
        });
    }

    private void tomTat(List<Attempt> list, boolean coLoc) {
        if (list == null || list.isEmpty()) {
            tvTomTat.setText(coLoc
                    ? "Không có lượt thi nào trong khoảng ngày đã chọn."
                    : "Chưa có lượt thi nào. Hãy vào Thi thử để bắt đầu.");
            return;
        }
        int soDat = 0;
        for (Attempt a : list) {
            if (Attempt.KET_QUA_DAT.equals(a.ketQua)) {
                soDat++;
            }
        }
        tvTomTat.setText(list.size() + " lượt thi · Đạt " + soDat + " · Trượt "
                + (list.size() - soDat));
    }

    // ---------- Thao tác trên từng lượt ----------

    /** Xem lại: Intent tường minh mở lại màn hình Kết quả của lượt thi đã lưu. */
    @Override
    public void onXem(Attempt a) {
        Intent i = new Intent(this, KetQuaActivity.class);
        i.putExtra(KetQuaActivity.EXTRA_DAT, Attempt.KET_QUA_DAT.equals(a.ketQua));
        i.putExtra(KetQuaActivity.EXTRA_SO_DUNG, a.soCauDung);
        i.putExtra(KetQuaActivity.EXTRA_TONG, ExamConfig.SO_CAU);
        i.putExtra(KetQuaActivity.EXTRA_LY_DO, a.lyDoTruot);
        i.putExtra(KetQuaActivity.EXTRA_DIEM_LIET,
                a.lyDoTruot != null && a.lyDoTruot.toLowerCase(new Locale("vi")).contains("điểm liệt"));
        i.putExtra(KetQuaActivity.EXTRA_THOI_GIAN, a.thoiGianLam);
        startActivity(i);
    }

    @Override
    public void onXoa(Attempt a) {
        new AlertDialog.Builder(this)
                .setTitle("Xoá lượt thi")
                .setMessage("Xoá lượt thi ngày " + FMT_NGAY.format(a.ngayThi) + "?")
                .setPositiveButton("Xoá", (d, w) -> repo.deleteAttempt(a))
                .setNegativeButton("Huỷ", null)
                .show();
    }

    private void xacNhanXoaTatCa() {
        new AlertDialog.Builder(this)
                .setTitle("Xoá toàn bộ lịch sử")
                .setMessage("Toàn bộ lịch sử làm bài sẽ bị xoá và không khôi phục được. Tiếp tục?")
                .setPositiveButton("Xoá hết", (d, w) ->
                        repo.clearHistory(DatabaseSeeder.LOCAL_USER_ID))
                .setNegativeButton("Huỷ", null)
                .show();
    }
}
