package com.example.oto.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.oto.data.Callback;
import com.example.oto.data.QuestionSyncRepository;
import com.example.oto.data.QuizRepository;
import com.example.oto.data.entity.Answer;
import com.example.oto.data.entity.Chapter;
import com.example.oto.data.entity.Question;
import com.example.oto.data.relation.QuestionWithAnswers;
import com.example.oto.ui.Resource;

import java.util.List;

/** ViewModel form thêm/sửa câu hỏi. */
public class SuaCauHoiViewModel extends AndroidViewModel {

    private final QuizRepository repo;
    private final QuestionSyncRepository syncRepo;
    private final MutableLiveData<Resource<QuestionWithAnswers>> cauHoi = new MutableLiveData<>();

    public SuaCauHoiViewModel(@NonNull Application application) {
        super(application);
        repo = new QuizRepository(application);
        syncRepo = new QuestionSyncRepository(application);
    }

    public LiveData<List<Chapter>> getChuong() {
        return repo.getChapters();
    }

    public LiveData<Resource<QuestionWithAnswers>> getCauHoi() {
        return cauHoi;
    }

    /** Nạp câu hỏi cần sửa. Gọi lại sau khi xoay màn hình sẽ không nạp lại. */
    public void napNeuChua(int questionId) {
        if (cauHoi.getValue() != null) {
            return;
        }
        cauHoi.setValue(Resource.dangTai());
        repo.getQuestion(questionId, qa -> {
            if (qa == null || qa.question == null) {
                cauHoi.setValue(Resource.loi("Không tìm thấy câu hỏi."));
                return;
            }
            cauHoi.setValue(Resource.thanhCong(qa));
        });
    }

    /** Câu hỏi đang sửa, null khi đang thêm mới. */
    public Question cauDangSua() {
        Resource<QuestionWithAnswers> res = cauHoi.getValue();
        return res == null || res.duLieu == null ? null : res.duLieu.question;
    }

    /**
     * Lưu câu hỏi kèm đáp án vào Room (một giao dịch), rồi ĐẨY LÊN Firestore để các
     * máy khác đồng bộ được. Room là nguồn chính nên dù đẩy lên thất bại (mất mạng),
     * câu hỏi vẫn đã lưu an toàn dưới máy.
     *
     * @param dongBoOk nhận true nếu đẩy lên máy chủ thành công, false nếu chưa đẩy được.
     */
    public void luu(Question question, List<Answer> dapAn, Callback<Boolean> dongBoOk) {
        repo.saveQuestion(question, dapAn, id -> {
            question.id = id.intValue();
            syncRepo.dayCauHoiLen(question, dapAn, dongBoOk);
        });
    }

    /** Xoá câu hỏi dưới máy rồi xoá luôn bản trên máy chủ (nếu từng được đẩy lên). */
    public void xoa(Question question, Runnable khiXong) {
        repo.deleteQuestion(question, ok -> {
            syncRepo.xoaCauHoiTrenMayChu(question.id, daXoa -> {
            });
            khiXong.run();
        });
    }
}
