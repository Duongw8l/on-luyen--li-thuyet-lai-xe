package com.example.oto.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.oto.R;
import com.example.oto.data.entity.Answer;
import com.example.oto.data.relation.QuestionWithAnswers;

import java.util.ArrayList;
import java.util.List;

/** Adapter danh sách câu hỏi ở màn Quản trị: mỗi dòng có nút Sửa và Xoá. */
public class CauHoiAdminAdapter extends RecyclerView.Adapter<CauHoiAdminAdapter.VH> {

    public interface OnItem {
        void onSua(QuestionWithAnswers qa);

        void onXoa(QuestionWithAnswers qa);
    }

    private final List<QuestionWithAnswers> data = new ArrayList<>();
    private final OnItem callback;

    public CauHoiAdminAdapter(OnItem callback) {
        this.callback = callback;
    }

    public void setData(List<QuestionWithAnswers> list) {
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
                .inflate(R.layout.item_cau_hoi_admin, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        QuestionWithAnswers qa = data.get(position);

        h.tvNoiDung.setText("#" + qa.question.id + "  " + qa.question.noiDung);
        h.tvChuong.setText("Chương " + qa.question.chapterId
                + " · " + qa.answers.size() + " đáp án");

        h.tvDiemLiet.setVisibility(qa.question.isDiemLiet ? View.VISIBLE : View.GONE);

        // Cảnh báo dữ liệu hỏng: mỗi câu phải có đúng một đáp án đúng.
        h.tvCanhBao.setVisibility(demDapAnDung(qa) == 1 ? View.GONE : View.VISIBLE);

        h.btnSua.setOnClickListener(v -> callback.onSua(qa));
        h.btnXoa.setOnClickListener(v -> callback.onXoa(qa));
        h.itemView.setOnClickListener(v -> callback.onSua(qa));
    }

    private int demDapAnDung(QuestionWithAnswers qa) {
        int n = 0;
        for (Answer a : qa.answers) {
            if (a.isCorrect) {
                n++;
            }
        }
        return n;
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView tvNoiDung, tvChuong, tvDiemLiet, tvCanhBao, btnSua, btnXoa;

        VH(@NonNull View v) {
            super(v);
            tvNoiDung = v.findViewById(R.id.tvNoiDung);
            tvChuong = v.findViewById(R.id.tvChuong);
            tvDiemLiet = v.findViewById(R.id.tvDiemLiet);
            tvCanhBao = v.findViewById(R.id.tvCanhBao);
            btnSua = v.findViewById(R.id.btnSua);
            btnXoa = v.findViewById(R.id.btnXoa);
        }
    }
}
