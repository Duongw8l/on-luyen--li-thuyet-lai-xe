package com.example.oto.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.oto.R;
import com.example.oto.auth.HoSoNguoiDung;
import com.example.oto.databinding.ItemNguoiDungAdminBinding;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/** Adapter danh sách tài khoản ở màn Quản trị người dùng (ListAdapter + DiffUtil). */
public class NguoiDungAdminAdapter
        extends ListAdapter<HoSoNguoiDung, NguoiDungAdminAdapter.VH> {

    public interface OnItem {
        void onDoiVaiTro(HoSoNguoiDung hoSo);
    }

    private final SimpleDateFormat dinhDangNgay =
            new SimpleDateFormat("dd/MM/yyyy", new Locale("vi", "VN"));

    private final OnItem callback;

    /** uid của admin đang đăng nhập — dòng của chính mình không cho đổi vai trò. */
    private final String uidHienTai;

    public NguoiDungAdminAdapter(OnItem callback, String uidHienTai) {
        super(DIFF);
        this.callback = callback;
        this.uidHienTai = uidHienTai;
    }

    private static final DiffUtil.ItemCallback<HoSoNguoiDung> DIFF =
            new DiffUtil.ItemCallback<HoSoNguoiDung>() {
                @Override
                public boolean areItemsTheSame(@NonNull HoSoNguoiDung a,
                                               @NonNull HoSoNguoiDung b) {
                    return a.uid.equals(b.uid);
                }

                @Override
                public boolean areContentsTheSame(@NonNull HoSoNguoiDung a,
                                                  @NonNull HoSoNguoiDung b) {
                    // vaiTro nằm ở đây để sau khi admin nâng/hạ quyền, đúng dòng đó
                    // được vẽ lại chứ không phải cả danh sách.
                    return bang(a.vaiTro, b.vaiTro)
                            && bang(a.hoTen, b.hoTen)
                            && bang(a.email, b.email)
                            && a.ngayTao == b.ngayTao;
                }
            };

    private static boolean bang(String a, String b) {
        return a == null ? b == null : a.equals(b);
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemNguoiDungAdminBinding b = ItemNguoiDungAdminBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new VH(b);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        HoSoNguoiDung u = getItem(position);
        boolean laChinhMinh = u.uid.equals(uidHienTai);

        h.b.tvTen.setText(u.tenHienThi());
        h.b.tvEmail.setText(u.email);

        h.b.tvVaiTro.setText(u.laAdmin()
                ? h.itemView.getContext().getString(R.string.nhan_admin)
                : h.itemView.getContext().getString(R.string.nhan_user));
        h.b.tvVaiTro.setTextColor(ContextCompat.getColor(h.itemView.getContext(),
                u.laAdmin() ? R.color.brand : R.color.muted));

        android.content.Context ctx = h.itemView.getContext();
        String ngay = u.ngayTao > 0
                ? ctx.getString(R.string.ngay_tao_tai_khoan,
                        dinhDangNgay.format(new Date(u.ngayTao)))
                : ctx.getString(R.string.khong_ro_ngay_tao);
        h.b.tvNgayTao.setText(laChinhMinh
                ? ctx.getString(R.string.tai_khoan_cua_ban, ngay)
                : ngay);

        // Rules không cho tự đổi vai trò của chính mình, nên ẩn nút luôn cho khỏi bấm nhầm.
        if (laChinhMinh) {
            h.b.btnDoiVaiTro.setVisibility(View.GONE);
            h.itemView.setOnClickListener(null);
        } else {
            h.b.btnDoiVaiTro.setVisibility(View.VISIBLE);
            h.b.btnDoiVaiTro.setText(u.laAdmin()
                    ? R.string.ha_xuong_user
                    : R.string.nang_len_admin);
            h.b.btnDoiVaiTro.setOnClickListener(v -> callback.onDoiVaiTro(u));
            h.itemView.setOnClickListener(v -> callback.onDoiVaiTro(u));
        }
    }

    static class VH extends RecyclerView.ViewHolder {
        final ItemNguoiDungAdminBinding b;

        VH(@NonNull ItemNguoiDungAdminBinding b) {
            super(b.getRoot());
            this.b = b;
        }
    }
}
