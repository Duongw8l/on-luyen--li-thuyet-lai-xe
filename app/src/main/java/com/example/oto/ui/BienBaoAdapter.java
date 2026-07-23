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
import com.example.oto.databinding.ItemBienBaoBinding;
import com.example.oto.util.AnhUtil;

/**
 * Adapter danh sách biển báo.
 *
 * Dùng ListAdapter + DiffUtil: khi danh sách đổi (lọc theo nhóm, gõ từ khoá tìm kiếm),
 * DiffUtil tự so sánh danh sách cũ và mới rồi chỉ vẽ lại đúng những dòng thay đổi,
 * thay vì notifyDataSetChanged() vẽ lại toàn bộ. Nhờ vậy danh sách không bị nháy
 * và cuộn mượt hơn.
 */
public class BienBaoAdapter extends ListAdapter<TrafficSign, BienBaoAdapter.VH> {

    /** Bấm vào một biển báo -> mở màn hình chi tiết. */
    public interface OnClick {
        void onClick(TrafficSign sign);
    }

    private final OnClick onClick;

    public BienBaoAdapter(OnClick onClick) {
        super(DIFF);
        this.onClick = onClick;
    }

    /**
     * Quy tắc so sánh của DiffUtil:
     * - areItemsTheSame: có phải CÙNG một biển báo không (so theo khoá chính id);
     * - areContentsTheSame: nội dung hiển thị có đổi không (quyết định có vẽ lại dòng đó).
     */
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

    /** So sánh hai chuỗi có thể null (nhomBien và anhUrl đều cho phép null). */
    private static boolean bang(String a, String b) {
        return a == null ? b == null : a.equals(b);
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemBienBaoBinding b = ItemBienBaoBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new VH(b);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        TrafficSign s = getItem(position);
        h.b.tvMaBien.setText(s.maBien);
        h.b.tvTenBien.setText(s.tenBien);
        h.b.tvNhomBien.setText(s.nhomBien == null ? "" : s.nhomBien);

        // Ảnh do quản trị viên đặt (nếu có), ngược lại dùng ảnh giữ chỗ.
        Bitmap anh = AnhUtil.docAnh(s.anhUrl);
        if (anh != null) {
            h.b.imgBien.setImageBitmap(anh);
        } else {
            h.b.imgBien.setImageResource(R.drawable.ic_bien_bao_placeholder);
        }

        h.itemView.setOnClickListener(v -> onClick.onClick(s));
    }

    static class VH extends RecyclerView.ViewHolder {
        final ItemBienBaoBinding b;

        VH(@NonNull ItemBienBaoBinding b) {
            super(b.getRoot());
            this.b = b;
        }
    }
}
