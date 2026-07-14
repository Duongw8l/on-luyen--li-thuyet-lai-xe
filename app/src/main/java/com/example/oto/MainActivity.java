package com.example.oto;

import android.app.DatePickerDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.oto.auth.AuthManager;
import com.example.oto.auth.DangNhapActivity;
import com.example.oto.auth.VaiTro;
import com.example.oto.data.DatabaseSeeder;
import com.example.oto.data.QuizRepository;
import com.example.oto.ui.AdminActivity;
import com.example.oto.ui.AdminBienBaoActivity;
import com.example.oto.ui.BienBaoActivity;
import com.example.oto.ui.CaNhanActivity;
import com.example.oto.util.AnhUtil;
import com.google.firebase.auth.FirebaseUser;
import com.example.oto.ui.LichSuActivity;
import com.example.oto.ui.OnTapActivity;
import com.example.oto.ui.ThiActivity;
import com.example.oto.ui.ThongKeActivity;
import com.google.android.material.button.MaterialButton;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

/** Trang chủ — lưới chức năng + đếm ngược ngày thi dự kiến (DatePicker). */
public class MainActivity extends AppCompatActivity {

    /** Số tổng đài tra cứu sát hạch — nhóm thay bằng số trung tâm thật khi demo. */
    private static final String SO_DIEN_THOAI_TRUNG_TAM = "19001234";
    private static final String EMAIL_NHOM = "nhom5.onthilaixe@gmail.com";

    private QuizRepository repo;
    private android.widget.TextView tvCountdown;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        repo = new QuizRepository(this);
        tvCountdown = findViewById(R.id.tvCountdown);

        ((MaterialButton) findViewById(R.id.btnOnTap)).setOnClickListener(v ->
                startActivity(new Intent(this, OnTapActivity.class)));

        ((MaterialButton) findViewById(R.id.btnDiemLiet)).setOnClickListener(v -> {
            Intent i = new Intent(this, OnTapActivity.class);
            i.putExtra(OnTapActivity.EXTRA_DIEM_LIET, true);
            startActivity(i);
        });

        ((MaterialButton) findViewById(R.id.btnThi)).setOnClickListener(v ->
                startActivity(new Intent(this, ThiActivity.class)));

        ((MaterialButton) findViewById(R.id.btnBienBao)).setOnClickListener(v ->
                startActivity(new Intent(this, BienBaoActivity.class)));

        ((MaterialButton) findViewById(R.id.btnThongKe)).setOnClickListener(v ->
                startActivity(new Intent(this, ThongKeActivity.class)));

        ((MaterialButton) findViewById(R.id.btnLichSu)).setOnClickListener(v ->
                startActivity(new Intent(this, LichSuActivity.class)));

        findViewById(R.id.btnDatNgayThi).setOnClickListener(v -> chonNgayThi());
        findViewById(R.id.headerCaNhan).setOnClickListener(v ->
                startActivity(CaNhanActivity.taoIntent(this)));
    }

    /** Ảnh đại diện + tên người dùng trên đầu trang chủ. */
    private void hienHoSo() {
        ImageView avatar = findViewById(R.id.imgAvatarHome);
        Bitmap anh = AnhUtil.docAnhDaiDien(this);
        if (anh != null) {
            avatar.setImageBitmap(anh);
        } else {
            avatar.setImageResource(R.drawable.ic_avatar_mac_dinh);
        }

        TextView tvTen = findViewById(R.id.tvTenNguoiDung);
        FirebaseUser u = new AuthManager().getUser();
        if (u == null) {
            tvTen.setText("Đang dùng offline — bấm để xem hồ sơ");
        } else {
            tvTen.setText(u.getDisplayName() == null ? u.getEmail() : u.getDisplayName());
        }
    }

    // ---------- Menu: các chức năng dùng Intent ngầm ----------

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    /**
     * Người dùng thường không nhìn thấy hai mục quản trị.
     * Đây mới chỉ là ẩn giao diện — việc chặn thật nằm trong từng màn quản trị.
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean laAdmin = VaiTro.laAdmin(this);
        menu.findItem(R.id.action_admin).setVisible(laAdmin);
        menu.findItem(R.id.action_admin_bien_bao).setVisible(laAdmin);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_admin) {
            startActivity(new Intent(this, AdminActivity.class));
            return true;
        }
        if (id == R.id.action_admin_bien_bao) {
            startActivity(new Intent(this, AdminBienBaoActivity.class));
            return true;
        }
        if (id == R.id.action_tim_trung_tam) {
            timTrungTamSatHach();
            return true;
        }
        if (id == R.id.action_goi_trung_tam) {
            goiTrungTam();
            return true;
        }
        if (id == R.id.action_gop_y) {
            gopY();
            return true;
        }
        if (id == R.id.action_gioi_thieu) {
            gioiThieu();
            return true;
        }
        if (id == R.id.action_dang_xuat) {
            dangXuat();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void dangXuat() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.dang_xuat)
                .setMessage("Đăng xuất khỏi tài khoản? Dữ liệu ôn tập đã lưu trong máy vẫn còn.")
                .setPositiveButton(R.string.dang_xuat, (d, w) -> {
                    new AuthManager().dangXuat();
                    VaiTro.xoa(this);
                    Intent i = new Intent(this, DangNhapActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                    finish();
                })
                .setNegativeButton("Huỷ", null)
                .show();
    }

    /** Intent ngầm ACTION_VIEW với geo: — mở bản đồ tìm trung tâm sát hạch gần nhất. */
    private void timTrungTamSatHach() {
        Uri uri = Uri.parse("geo:0,0?q=" + Uri.encode("trung tâm sát hạch lái xe"));
        moIntent(new Intent(Intent.ACTION_VIEW, uri), "Máy chưa cài ứng dụng bản đồ.");
    }

    /** Intent ngầm ACTION_DIAL — mở sẵn màn hình gọi, người dùng tự bấm gọi. */
    private void goiTrungTam() {
        moIntent(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + SO_DIEN_THOAI_TRUNG_TAM)),
                "Máy không hỗ trợ gọi điện.");
    }

    /** Intent ngầm ACTION_SENDTO — gửi email góp ý / báo lỗi câu hỏi. */
    private void gopY() {
        Intent email = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + EMAIL_NHOM));
        email.putExtra(Intent.EXTRA_SUBJECT, "Góp ý / báo lỗi — ứng dụng Ôn thi lái xe B");
        email.putExtra(Intent.EXTRA_TEXT, "Mô tả lỗi hoặc góp ý của bạn:\n\n");
        moIntent(email, "Máy chưa cài ứng dụng email.");
    }

    private void gioiThieu() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.menu_gioi_thieu)
                .setMessage("Ứng dụng ôn thi lý thuyết sát hạch lái xe hạng B.\n\n"
                        + "Nguồn bộ câu hỏi: bộ 600 câu do Cục CSGT — Bộ Công an ban hành.\n\n"
                        + "Ứng dụng hoạt động offline hoàn toàn: toàn bộ câu hỏi, biển báo và "
                        + "lịch sử làm bài được lưu trong máy bằng SQLite (Room).")
                .setPositiveButton("Đóng", null)
                .show();
    }

    private void moIntent(Intent intent, String thongBaoLoi) {
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, thongBaoLoi, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        capNhatDemNguoc();
        hienHoSo();
        // Vai trò có thể vừa được cấp trên Firebase Console -> đọc lại và vẽ lại menu.
        VaiTro.dongBo(this, vaiTro -> invalidateOptionsMenu());
    }

    private void capNhatDemNguoc() {
        repo.getUser(DatabaseSeeder.LOCAL_USER_ID, user -> {
            if (user == null || user.ngayThiDuKien <= 0) {
                tvCountdown.setText("Chưa đặt ngày thi dự kiến");
                return;
            }
            long conLai = user.ngayThiDuKien - System.currentTimeMillis();
            long ngay = TimeUnit.MILLISECONDS.toDays(conLai);
            if (ngay < 0) {
                tvCountdown.setText("Ngày thi dự kiến đã qua");
            } else if (ngay == 0) {
                tvCountdown.setText("Hôm nay là ngày thi dự kiến!");
            } else {
                tvCountdown.setText("Còn " + ngay + " ngày nữa tới ngày thi");
            }
        });
    }

    private void chonNgayThi() {
        Calendar c = Calendar.getInstance();
        DatePickerDialog dlg = new DatePickerDialog(this, (view, year, month, day) -> {
            Calendar chon = Calendar.getInstance();
            chon.set(year, month, day, 0, 0, 0);
            repo.setNgayThiDuKien(DatabaseSeeder.LOCAL_USER_ID, chon.getTimeInMillis());
            tvCountdown.postDelayed(this::capNhatDemNguoc, 200);
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        dlg.getDatePicker().setMinDate(System.currentTimeMillis());
        dlg.show();
    }
}
