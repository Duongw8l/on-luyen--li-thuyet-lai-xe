package com.example.oto.ui;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.oto.R;
import com.example.oto.data.entity.Attempt;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/** Adapter cho RecyclerView lịch sử các lượt thi. */
public class LichSuAdapter extends RecyclerView.Adapter<LichSuAdapter.VH> {

    public interface OnItem {
        void onXem(Attempt attempt);

        void onXoa(Attempt attempt);
    }

    private static final SimpleDateFormat FMT =
            new SimpleDateFormat("HH:mm dd/MM/yyyy", new Locale("vi", "VN"));

    private final List<Attempt> data = new ArrayList<>();
    private final OnItem callback;

    public LichSuAdapter(OnItem callback) {
        this.callback = callback;
    }

    public void setData(List<Attempt> list) {
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
                .inflate(R.layout.item_lich_su, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Attempt a = data.get(position);
        boolean dat = Attempt.KET_QUA_DAT.equals(a.ketQua);

        h.tvKetQua.setText(dat ? "ĐẠT" : "TRƯỢT");
        h.tvKetQua.setTextColor(Color.parseColor(dat ? "#2E7D32" : "#C62828"));

        h.tvNgay.setText(FMT.format(new Date(a.ngayThi)));
        h.tvChiTiet.setText("Đúng " + a.soCauDung + " câu · Thời gian "
                + String.format(Locale.getDefault(), "%02d:%02d",
                a.thoiGianLam / 60, a.thoiGianLam % 60));

        if (!dat && a.lyDoTruot != null && !a.lyDoTruot.isEmpty()) {
            h.tvLyDo.setVisibility(View.VISIBLE);
            h.tvLyDo.setText(a.lyDoTruot);
        } else {
            h.tvLyDo.setVisibility(View.GONE);
        }

        h.itemView.setOnClickListener(v -> callback.onXem(a));
        h.btnXoa.setOnClickListener(v -> callback.onXoa(a));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView tvKetQua, tvNgay, tvChiTiet, tvLyDo, btnXoa;

        VH(@NonNull View v) {
            super(v);
            tvKetQua = v.findViewById(R.id.tvKetQuaItem);
            tvNgay = v.findViewById(R.id.tvNgayThi);
            tvChiTiet = v.findViewById(R.id.tvChiTiet);
            tvLyDo = v.findViewById(R.id.tvLyDoItem);
            btnXoa = v.findViewById(R.id.btnXoaItem);
        }
    }
}
