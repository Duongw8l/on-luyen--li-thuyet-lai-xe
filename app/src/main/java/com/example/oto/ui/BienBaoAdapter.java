package com.example.oto.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.oto.R;
import com.example.oto.data.entity.TrafficSign;

import java.util.ArrayList;
import java.util.List;

/** Adapter cho RecyclerView danh sách biển báo. */
public class BienBaoAdapter extends RecyclerView.Adapter<BienBaoAdapter.VH> {

    /** Bấm vào một biển báo -> mở màn hình chi tiết. */
    public interface OnClick {
        void onClick(TrafficSign sign);
    }

    private final List<TrafficSign> data = new ArrayList<>();
    private final OnClick onClick;

    public BienBaoAdapter(OnClick onClick) {
        this.onClick = onClick;
    }

    public void setData(List<TrafficSign> list) {
        data.clear();
        if (list != null) {
            data.addAll(list);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_bien_bao, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        TrafficSign s = data.get(position);
        h.tvMa.setText(s.maBien);
        h.tvTen.setText(s.tenBien);
        h.tvNhom.setText(s.nhomBien == null ? "" : s.nhomBien);
        // Bản offline chưa có file ảnh biển báo -> dùng ảnh giữ chỗ.
        h.img.setImageResource(R.drawable.ic_bien_bao_placeholder);
        h.itemView.setOnClickListener(v -> onClick.onClick(s));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final ImageView img;
        final TextView tvMa, tvTen, tvNhom;

        VH(@NonNull View v) {
            super(v);
            img = v.findViewById(R.id.imgBien);
            tvMa = v.findViewById(R.id.tvMaBien);
            tvTen = v.findViewById(R.id.tvTenBien);
            tvNhom = v.findViewById(R.id.tvNhomBien);
        }
    }
}
