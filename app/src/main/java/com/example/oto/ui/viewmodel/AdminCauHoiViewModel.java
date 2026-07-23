package com.example.oto.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.oto.data.Callback;
import com.example.oto.data.QuestionSyncRepository;
import com.example.oto.data.QuizRepository;
import com.example.oto.data.entity.Chapter;
import com.example.oto.data.entity.Question;
import com.example.oto.data.relation.QuestionWithAnswers;

import java.util.List;

/** ViewModel màn Quản trị ngân hàng câu hỏi: lọc theo từ khoá + chương + câu điểm liệt. */
public class AdminCauHoiViewModel extends AndroidViewModel {

    /** Ba tiêu chí lọc gộp làm một, để switchMap chỉ chạy lại một truy vấn duy nhất. */
    public static class BoLoc {
        public final String tuKhoa;
        public final int chuongId;       // 0 = tất cả chương
        public final boolean chiDiemLiet;

        BoLoc(String tuKhoa, int chuongId, boolean chiDiemLiet) {
            this.tuKhoa = tuKhoa;
            this.chuongId = chuongId;
            this.chiDiemLiet = chiDiemLiet;
        }
    }

    private final QuizRepository repo;
    private final QuestionSyncRepository syncRepo;
    private final MutableLiveData<BoLoc> boLoc = new MutableLiveData<>(new BoLoc("", 0, false));
    private final LiveData<List<QuestionWithAnswers>> danhSach;

    public AdminCauHoiViewModel(@NonNull Application application) {
        super(application);
        repo = new QuizRepository(application);
        syncRepo = new QuestionSyncRepository(application);
        danhSach = Transformations.switchMap(boLoc,
                bl -> repo.filterQuestions(bl.tuKhoa, bl.chuongId, bl.chiDiemLiet));
    }

    public LiveData<List<QuestionWithAnswers>> getDanhSach() {
        return danhSach;
    }

    public LiveData<List<Chapter>> getChuong() {
        return repo.getChapters();
    }

    public void datTuKhoa(String tuKhoa) {
        BoLoc b = boLoc.getValue();
        boLoc.setValue(new BoLoc(tuKhoa, b == null ? 0 : b.chuongId,
                b != null && b.chiDiemLiet));
    }

    public void datChuong(int chuongId) {
        BoLoc b = boLoc.getValue();
        boLoc.setValue(new BoLoc(b == null ? "" : b.tuKhoa, chuongId,
                b != null && b.chiDiemLiet));
    }

    public void datChiDiemLiet(boolean chi) {
        BoLoc b = boLoc.getValue();
        boLoc.setValue(new BoLoc(b == null ? "" : b.tuKhoa, b == null ? 0 : b.chuongId, chi));
    }

    /** Xoá câu hỏi — đáp án bị xoá theo nhờ ràng buộc CASCADE ở tầng SQLite. */
    public void xoa(Question question, Runnable khiXong) {
        repo.deleteQuestion(question, ok -> {
            syncRepo.xoaCauHoiTrenMayChu(question.id, daXoa -> {
            });
            khiXong.run();
        });
    }

    /**
     * Đồng bộ hai chiều với Firestore: đẩy các thay đổi cục bộ lên rồi kéo delta về.
     * Kết quả trả về luồng UI để hiển thị số câu đã tải/đẩy.
     */
    public void dongBo(Callback<QuestionSyncRepository.KetQua> onDone, Callback<String> onError) {
        syncRepo.dongBoTatCa(onDone, onError);
    }
}
