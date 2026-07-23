package com.example.oto.ui;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.oto.R;
import com.example.oto.data.entity.TrafficSign;
import com.example.oto.databinding.ItemBienBaoAdminBinding;
import com.example.oto.util.AnhUtil;

/** Adapter danh sách biển báo ở màn Quản trị (ListAdapter + DiffUtil). */
public class BienBaoAdminAdapter
        extends ListAdapter<TrafficSign, BienBaoAdminAdapter.VH> {

    public interface OnItem {
        void onSua(TrafficSign sign);

        void onXoa(TrafficSign sign);
    }

    private final OnItem callback;

    public BienBaoAdminAdapter(OnItem callback) {
        super(DIFF);
        this.callback = callback;
    }

    private static final DiffUtil.ItemCallback<TrafficSign> DIFF =
            new DiffUtil.ItemCallback<TrafficSign>() {
                @Override
                public boolean areItemsTheSame(@NonNull TrafficSign a, @NonNull TrafficSign b) {
                    return a.id == b.id;
                }

                @Override
                public boolean areContentsTheSame(@NonNull TrafficSign a, @NonNull TrafficSign b) {
                    return a.maBien.equals(b.maBien)
                            && a.tenBien.equals(b.tenBien)
                            && bang(a.nhomBien, b.nhomBien)
                            && bang(a.anhUrl, b.anhUrl);
                }
            };

    private static boolean bang(String a, String b) {
        return a == null ? b == null : a.equals(b);
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemBienBaoAdminBinding b = ItemBienBaoAdminBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new VH(b);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        TrafficSign s = getItem(position);

        Bitmap anh = AnhUtil.docAnh(s.anhUrl);
        if (anh != null) {
            h.b.imgBien.setImageBitmap(anh);
        } else {
            h.b.imgBien.setImageResource(R.drawable.ic_bien_bao_placeholder);
        }
        h.b.tvMaBien.setText(s.maBien);
        h.b.tvTenBien.setText(s.tenBien);
        h.b.tvNhomBien.setText(s.nhomBien == null ? "" : s.nhomBien);

        h.b.btnSua.setOnClickListener(v -> callback.onSua(s));
        h.b.btnXoa.setOnClickListener(v -> callback.onXoa(s));
        h.itemView.setOnClickListener(v -> callback.onSua(s));
    }

    static class VH extends RecyclerView.ViewHolder {
        final ItemBienBaoAdminBinding b;

        VH(@NonNull ItemBienBaoAdminBinding b) {
            super(b.getRoot());
            this.b = b;
        }
    }
}
