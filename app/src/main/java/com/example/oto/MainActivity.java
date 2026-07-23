package com.example.oto;

import android.app.DatePickerDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.oto.auth.AuthManager;
import com.example.oto.auth.DangNhapActivity;
import com.example.oto.auth.VaiTro;
import com.example.oto.databinding.ActivityMainBinding;
import com.example.oto.ui.viewmodel.MainViewModel;
import com.example.oto.ui.AdminActivity;
import com.example.oto.ui.AdminBienBaoActivity;
import com.example.oto.ui.AdminNguoiDungActivity;
import com.example.oto.ui.BienBaoActivity;
import com.example.oto.ui.CaNhanActivity;
import com.example.oto.util.AnhUtil;
import com.google.firebase.auth.FirebaseUser;
import com.example.oto.ui.OnTapActivity;
import com.example.oto.ui.ThiActivity;
import com.example.oto.ui.ThongKeActivity;

import java.util.Calendar;

/** Trang chủ — lưới chức năng + đếm ngược ngày thi dự kiến (DatePicker). */
public class MainActivity extends AppCompatActivity {

    /** Số tổng đài tra cứu sát hạch — nhóm thay bằng số trung tâm thật khi demo. */
    private static final String SO_DIEN_THOAI_TRUNG_TAM = "19001234";
    private static final String EMAIL_NHOM = "nhom5.onthilaixe@gmail.com";

    private MainViewModel viewModel;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        viewModel.getDemNguoc().observe(this, this::veDemNguoc);

        binding.btnOnTap.setOnClickListener(v ->
                startActivity(new Intent(this, OnTapActivity.class)));

        binding.btnDiemLiet.setOnClickListener(v -> {
            Intent i = new Intent(this, OnTapActivity.class);
            i.putExtra(OnTapActivity.EXTRA_DIEM_LIET, true);
            startActivity(i);
        });

        binding.btnThi.setOnClickListener(v ->
                startActivity(new Intent(this, ThiActivity.class)));

        binding.btnBienBao.setOnClickListener(v ->
                startActivity(new Intent(this, BienBaoActivity.class)));

        binding.btnThongKe.setOnClickListener(v ->
                startActivity(new Intent(this, ThongKeActivity.class)));

        binding.btnDatNgayThi.setOnClickListener(v -> chonNgayThi());
        binding.headerCaNhan.setOnClickListener(v ->
                startActivity(CaNhanActivity.taoIntent(this)));
    }

    /** Ảnh đại diện + tên người dùng trên đầu trang chủ. */
    private void hienHoSo() {
        Bitmap anh = AnhUtil.docAnhDaiDien(this);
        if (anh != null) {
            binding.imgAvatarHome.setImageBitmap(anh);
        } else {
            binding.imgAvatarHome.setImageResource(R.drawable.ic_avatar_mac_dinh);
        }

        FirebaseUser u = new AuthManager().getUser();
        if (u == null) {
            binding.tvTenNguoiDung.setText(R.string.hoso_offline);
        } else {
            binding.tvTenNguoiDung.setText(
                    u.getDisplayName() == null ? u.getEmail() : u.getDisplayName());
        }
    }

    // ---------- Menu: các chức năng dùng Intent ngầm ----------

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    /**
     * Người dùng thường không nhìn thấy các mục quản trị.
     * Đây mới chỉ là ẩn giao diện — việc chặn thật nằm trong từng màn quản trị.
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean laAdmin = VaiTro.laAdmin(this);
        menu.findItem(R.id.action_admin).setVisible(laAdmin);
        menu.findItem(R.id.action_admin_bien_bao).setVisible(laAdmin);
        menu.findItem(R.id.action_admin_nguoi_dung).setVisible(laAdmin);
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
        if (id == R.id.action_admin_nguoi_dung) {
            startActivity(new Intent(this, AdminNguoiDungActivity.class));
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
                .setMessage(R.string.hoi_dang_xuat)
                .setPositiveButton(R.string.dang_xuat, (d, w) -> {
                    new AuthManager().dangXuat();
                    VaiTro.xoa(this);
                    Intent i = new Intent(this, DangNhapActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                    finish();
                })
                .setNegativeButton(R.string.huy, null)
                .show();
    }

    /** Intent ngầm ACTION_VIEW với geo: — mở bản đồ tìm trung tâm sát hạch gần nhất. */
    private void timTrungTamSatHach() {
        Uri uri = Uri.parse("geo:0,0?q=" + Uri.encode("trung tâm sát hạch lái xe"));
        moIntent(new Intent(Intent.ACTION_VIEW, uri), getString(R.string.loi_khong_co_ban_do));
    }

    /** Intent ngầm ACTION_DIAL — mở sẵn màn hình gọi, người dùng tự bấm gọi. */
    private void goiTrungTam() {
        moIntent(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + SO_DIEN_THOAI_TRUNG_TAM)),
                getString(R.string.loi_khong_goi_duoc));
    }

    /** Intent ngầm ACTION_SENDTO — gửi email góp ý / báo lỗi câu hỏi. */
    private void gopY() {
        Intent email = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + EMAIL_NHOM));
        email.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.gop_y_tieu_de));
        email.putExtra(Intent.EXTRA_TEXT, getString(R.string.gop_y_noi_dung));
        moIntent(email, getString(R.string.loi_khong_co_email));
    }

    private void gioiThieu() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.menu_gioi_thieu)
                .setMessage(R.string.gioi_thieu_noi_dung)
                .setPositiveButton(R.string.dong, null)
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
        viewModel.capNhatDemNguoc();
        viewModel.dongBoCauHoi();
        hienHoSo();
        // Vai trò có thể vừa được cấp trên Firebase Console -> đọc lại và vẽ lại menu.
        VaiTro.dongBo(this, vaiTro -> invalidateOptionsMenu());
    }

    /** ViewModel tính ra trạng thái đếm ngược; Activity chỉ chọn chuỗi tương ứng. */
    private void veDemNguoc(MainViewModel.DemNguoc dn) {
        switch (dn.trangThai) {
            case CHUA_DAT:
                binding.tvCountdown.setText(R.string.dem_nguoc_chua_dat);
                break;
            case DA_QUA:
                binding.tvCountdown.setText(R.string.dem_nguoc_da_qua);
                break;
            case HOM_NAY:
                binding.tvCountdown.setText(R.string.dem_nguoc_hom_nay);
                break;
            case CON_LAI:
            default:
                binding.tvCountdown.setText(getString(R.string.dem_nguoc_con_lai, dn.soNgay));
                break;
        }
    }

    private void chonNgayThi() {
        Calendar c = Calendar.getInstance();
        DatePickerDialog dlg = new DatePickerDialog(this, (view, year, month, day) -> {
            Calendar chon = Calendar.getInstance();
            chon.set(year, month, day, 0, 0, 0);
            viewModel.datNgayThi(chon.getTimeInMillis());
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        dlg.getDatePicker().setMinDate(System.currentTimeMillis());
        dlg.show();
    }
}
