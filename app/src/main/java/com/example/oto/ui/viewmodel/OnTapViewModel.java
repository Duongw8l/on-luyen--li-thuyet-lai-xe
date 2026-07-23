package com.example.oto.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.oto.data.QuizRepository;
import com.example.oto.data.entity.Chapter;
import com.example.oto.data.relation.QuestionWithAnswers;
import com.example.oto.ui.Resource;

import java.util.List;

/**
 * ViewModel màn Ôn tập.
 *
 * Giữ danh sách câu hỏi đang ôn và vị trí câu hiện tại. Nhờ vậy khi xoay màn hình,
 * Activity bị tạo lại nhưng ViewModel thì không — người học quay lại đúng câu đang dở
 * thay vì bị đưa về câu 1.
 */
public class OnTapViewModel extends AndroidViewModel {

    private final QuizRepository repo;

    private final MutableLiveData<Resource<List<QuestionWithAnswers>>> danhSach =
            new MutableLiveData<>();

    /** Vị trí câu đang xem trong danh sách. */
    private int viTri = 0;

    public OnTapViewModel(@NonNull Application application) {
        super(application);
        repo = new QuizRepository(application);
    }

    public LiveData<List<Chapter>> getChuong() {
        return repo.getChapters();
    }

    public LiveData<Resource<List<QuestionWithAnswers>>> getDanhSach() {
        return danhSach;
    }

    public void napTheoChuong(int chapterId) {
        danhSach.setValue(Resource.dangTai());
        repo.getQuestionsByChapter(chapterId, list -> {
            viTri = 0;
            danhSach.setValue(Resource.thanhCong(list));
        });
    }

    public void napDiemLiet() {
        danhSach.setValue(Resource.dangTai());
        repo.getDiemLiet(list -> {
            viTri = 0;
            danhSach.setValue(Resource.thanhCong(list));
        });
    }

    // ---------- Điều hướng giữa các câu ----------

    public int getViTri() {
        return viTri;
    }

    /** Trả về true nếu chuyển được; false khi đã ở đầu/cuối danh sách. */
    public boolean chuyen(int buoc) {
        int moi = viTri + buoc;
        if (moi < 0 || moi >= soCau()) {
            return false;
        }
        viTri = moi;
        return true;
    }

    public int soCau() {
        Resource<List<QuestionWithAnswers>> res = danhSach.getValue();
        return res == null || res.duLieu == null ? 0 : res.duLieu.size();
    }

    /** Câu hỏi đang xem, hoặc null nếu danh sách rỗng. */
    public QuestionWithAnswers cauHienTai() {
        Resource<List<QuestionWithAnswers>> res = danhSach.getValue();
        if (res == null || res.duLieu == null || res.duLieu.isEmpty()) {
            return null;
        }
        return res.duLieu.get(viTri);
    }
}
