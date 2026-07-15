package com.example.oto.ui;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.oto.R;
import com.example.oto.data.entity.TrafficSign;
import com.example.oto.util.AnhUtil;

import java.util.ArrayList;
import java.util.List;

/** Adapter danh sách biển báo ở màn Quản trị: mỗi dòng có nút Sửa và Xoá. */
public class BienBaoAdminAdapter extends RecyclerView.Adapter<BienBaoAdminAdapter.VH> {

    public interface OnItem {
        void onSua(TrafficSign sign);

        void onXoa(TrafficSign sign);
    }

    private final List<TrafficSign> data = new ArrayList<>();
    private final OnItem callback;

    public BienBaoAdminAdapter(OnItem callback) {
        this.callback = callback;
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
                .inflate(R.layout.item_bien_bao_admin, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        TrafficSign s = data.get(position);
        Bitmap anh = AnhUtil.docAnh(s.anhUrl);
        if (anh != null) {
            h.img.setImageBitmap(anh);
        } else {
            h.img.setImageResource(R.drawable.ic_bien_bao_placeholder);
        }
        h.tvMa.setText(s.maBien);
        h.tvTen.setText(s.tenBien);
        h.tvNhom.setText(s.nhomBien == null ? "" : s.nhomBien);

        h.btnSua.setOnClickListener(v -> callback.onSua(s));
        h.btnXoa.setOnClickListener(v -> callback.onXoa(s));
        h.itemView.setOnClickListener(v -> callback.onSua(s));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final ImageView img;
        final TextView tvMa, tvTen, tvNhom, btnSua, btnXoa;

        VH(@NonNull View v) {
            super(v);
            img = v.findViewById(R.id.imgBien);
            tvMa = v.findViewById(R.id.tvMaBien);
            tvTen = v.findViewById(R.id.tvTenBien);
            tvNhom = v.findViewById(R.id.tvNhomBien);
            btnSua = v.findViewById(R.id.btnSua);
            btnXoa = v.findViewById(R.id.btnXoa);
        }
    }
}
