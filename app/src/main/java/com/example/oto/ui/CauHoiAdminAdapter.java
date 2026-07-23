package com.example.oto.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.oto.R;
import com.example.oto.data.entity.Answer;
import com.example.oto.data.relation.QuestionWithAnswers;
import com.example.oto.databinding.ItemCauHoiAdminBinding;

/** Adapter danh sách câu hỏi ở màn Quản trị (ListAdapter + DiffUtil). */
public class CauHoiAdminAdapter
        extends ListAdapter<QuestionWithAnswers, CauHoiAdminAdapter.VH> {

    public interface OnItem {
        void onSua(QuestionWithAnswers qa);

        void onXoa(QuestionWithAnswers qa);
    }

    private final OnItem callback;

    public CauHoiAdminAdapter(OnItem callback) {
        super(DIFF);
        this.callback = callback;
    }

    private static final DiffUtil.ItemCallback<QuestionWithAnswers> DIFF =
            new DiffUtil.ItemCallback<QuestionWithAnswers>() {
                @Override
                public boolean areItemsTheSame(@NonNull QuestionWithAnswers a,
                                               @NonNull QuestionWithAnswers b) {
                    return a.question.id == b.question.id;
                }

                @Override
                public boolean areContentsTheSame(@NonNull QuestionWithAnswers a,
                                                  @NonNull QuestionWithAnswers b) {
                    // So cả số đáp án vì dòng có hiển thị "N đáp án" và cảnh báo
                    // khi số đáp án đúng khác 1.
                    return a.question.noiDung.equals(b.question.noiDung)
                            && a.question.chapterId == b.question.chapterId
                            && a.question.isDiemLiet == b.question.isDiemLiet
                            && a.answers.size() == b.answers.size()
                            && demDapAnDung(a) == demDapAnDung(b);
                }
            };

    /** Mỗi câu hỏi phải có ĐÚNG một đáp án đúng — đếm để cảnh báo nếu dữ liệu hỏng. */
    private static int demDapAnDung(QuestionWithAnswers qa) {
        int n = 0;
        for (Answer a : qa.answers) {
            if (a.isCorrect) {
                n++;
            }
        }
        return n;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCauHoiAdminBinding b = ItemCauHoiAdminBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new VH(b);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        QuestionWithAnswers qa = getItem(position);

        h.b.tvNoiDung.setText(h.itemView.getContext().getString(
                R.string.cau_hoi_dong_tom_tat, qa.question.id, qa.question.noiDung));
        h.b.tvChuong.setText(h.itemView.getContext().getString(
                R.string.cau_hoi_dong_chuong, qa.question.chapterId, qa.answers.size()));

        h.b.tvDiemLiet.setVisibility(qa.question.isDiemLiet ? View.VISIBLE : View.GONE);

        // Cảnh báo dữ liệu hỏng: mỗi câu phải có đúng một đáp án đúng.
        h.b.tvCanhBao.setVisibility(demDapAnDung(qa) == 1 ? View.GONE : View.VISIBLE);

        h.b.btnSua.setOnClickListener(v -> callback.onSua(qa));
        h.b.btnXoa.setOnClickListener(v -> callback.onXoa(qa));
        h.itemView.setOnClickListener(v -> callback.onSua(qa));
    }

    static class VH extends RecyclerView.ViewHolder {
        final ItemCauHoiAdminBinding b;

        VH(@NonNull ItemCauHoiAdminBinding b) {
            super(b.getRoot());
            this.b = b;
        }
    }
}
