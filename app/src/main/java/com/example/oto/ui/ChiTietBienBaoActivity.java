package com.example.oto.ui;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.oto.R;
import com.example.oto.databinding.ActivityChiTietBienBaoBinding;
import com.example.oto.util.AnhUtil;

/** Chi tiết một biển báo + nút mở văn bản luật gốc bằng Intent ngầm ACTION_VIEW. */
public class ChiTietBienBaoActivity extends AppCompatActivity {

    public static final String EXTRA_MA = "ma_bien";
    public static final String EXTRA_TEN = "ten_bien";
    public static final String EXTRA_NHOM = "nhom_bien";
    public static final String EXTRA_MO_TA = "mo_ta";
    public static final String EXTRA_ANH = "anh_url";

    /** QCVN 41:2024/BGTVT — quy chuẩn báo hiệu đường bộ. */
    private static final String URL_VAN_BAN_LUAT =
            "https://vanban.chinhphu.vn/?pageid=27160&docid=211316";

    private ActivityChiTietBienBaoBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChiTietBienBaoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String ma = getIntent().getStringExtra(EXTRA_MA);
        String ten = getIntent().getStringExtra(EXTRA_TEN);
        String nhom = getIntent().getStringExtra(EXTRA_NHOM);
        String moTa = getIntent().getStringExtra(EXTRA_MO_TA);

        setTitle(ma == null ? getString(R.string.menu_bien_bao) : ma);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        binding.tvMaBien.setText(ma);
        binding.tvTenBien.setText(ten);
        binding.tvNhomBien.setText(getString(R.string.nhom_bien_hien_thi,
                nhom == null ? getString(R.string.khong_ro) : nhom));
        binding.tvMoTa.setText(moTa == null ? "" : moTa);

        Bitmap anh = AnhUtil.docAnh(getIntent().getStringExtra(EXTRA_ANH));
        if (anh != null) {
            binding.imgBien.setImageBitmap(anh);
        }

        binding.btnXemLuat.setOnClickListener(v -> moVanBanLuat());
        binding.btnChiaSeBien.setOnClickListener(v -> chiaSe(ma, ten, moTa));
    }

    private void moVanBanLuat() {
        Intent view = new Intent(Intent.ACTION_VIEW, Uri.parse(URL_VAN_BAN_LUAT));
        try {
            startActivity(view);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.loi_khong_co_trinh_duyet, Toast.LENGTH_SHORT).show();
        }
    }

    private void chiaSe(String ma, String ten, String moTa) {
        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType("text/plain");
        send.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.chia_se_bien_tieu_de, ma));
        send.putExtra(Intent.EXTRA_TEXT, getString(R.string.chia_se_bien_noi_dung,
                ma, ten, moTa == null ? "" : moTa));
        startActivity(Intent.createChooser(send, getString(R.string.chia_se_bien_qua)));
    }
}
