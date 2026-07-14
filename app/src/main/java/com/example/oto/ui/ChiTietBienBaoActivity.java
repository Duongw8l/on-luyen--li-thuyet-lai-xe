package com.example.oto.ui;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.oto.R;

/** Chi tiết một biển báo + nút mở văn bản luật gốc bằng Intent ngầm ACTION_VIEW. */
public class ChiTietBienBaoActivity extends AppCompatActivity {

    public static final String EXTRA_MA = "ma_bien";
    public static final String EXTRA_TEN = "ten_bien";
    public static final String EXTRA_NHOM = "nhom_bien";
    public static final String EXTRA_MO_TA = "mo_ta";

    /** QCVN 41:2024/BGTVT — quy chuẩn báo hiệu đường bộ. */
    private static final String URL_VAN_BAN_LUAT =
            "https://vanban.chinhphu.vn/?pageid=27160&docid=211316";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chi_tiet_bien_bao);

        String ma = getIntent().getStringExtra(EXTRA_MA);
        String ten = getIntent().getStringExtra(EXTRA_TEN);
        String nhom = getIntent().getStringExtra(EXTRA_NHOM);
        String moTa = getIntent().getStringExtra(EXTRA_MO_TA);

        setTitle(ma == null ? getString(R.string.menu_bien_bao) : ma);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        ((TextView) findViewById(R.id.tvMaBien)).setText(ma);
        ((TextView) findViewById(R.id.tvTenBien)).setText(ten);
        ((TextView) findViewById(R.id.tvNhomBien)).setText("Nhóm: " + (nhom == null ? "—" : nhom));
        ((TextView) findViewById(R.id.tvMoTa)).setText(moTa == null ? "" : moTa);

        findViewById(R.id.btnXemLuat).setOnClickListener(v -> moVanBanLuat());
        findViewById(R.id.btnChiaSeBien).setOnClickListener(v -> chiaSe(ma, ten, moTa));
    }

    private void moVanBanLuat() {
        Intent view = new Intent(Intent.ACTION_VIEW, Uri.parse(URL_VAN_BAN_LUAT));
        try {
            startActivity(view);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Máy chưa có trình duyệt web.", Toast.LENGTH_SHORT).show();
        }
    }

    private void chiaSe(String ma, String ten, String moTa) {
        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType("text/plain");
        send.putExtra(Intent.EXTRA_SUBJECT, "Biển báo " + ma);
        send.putExtra(Intent.EXTRA_TEXT,
                "Biển " + ma + " — " + ten + "\n" + (moTa == null ? "" : moTa));
        startActivity(Intent.createChooser(send, "Chia sẻ biển báo qua"));
    }
}
