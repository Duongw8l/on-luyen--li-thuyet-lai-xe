package com.example.oto.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.oto.data.DatabaseSeeder;
import com.example.oto.data.ExamConfig;
import com.example.oto.data.QuizRepository;
import com.example.oto.data.entity.Attempt;
import com.example.oto.data.entity.UserAnswer;
import com.example.oto.data.relation.QuestionWithAnswers;
import com.example.oto.logic.ExamResult;
import com.example.oto.logic.ExamScorer;
import com.example.oto.ui.Resource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ViewModel màn Thi thử.
 *
 * Chứa toàn bộ nghiệp vụ của một lượt thi: sinh đề, nhớ đáp án đã chọn, chấm điểm
 * và lưu kết quả. Activity chỉ còn việc vẽ giao diện và chạy đồng hồ đếm ngược.
 *
 * Đây là chỗ luật điểm liệt được áp dụng (qua ExamScorer): sai một câu điểm liệt là
 * trượt ngay, không xét tiếp số câu đúng.
 */
public class ThiViewModel extends AndroidViewModel {

    private final QuizRepository repo;

    private final MutableLiveData<Resource<List<QuestionWithAnswers>>> deThi =
            new MutableLiveData<>();

    /** questionId -> answerId người dùng đã chọn. */
    private final Map<Integer, Integer> dapAnDaChon = new HashMap<>();

    private int viTri = 0;
    private boolean daNop = false;

    public ThiViewModel(@NonNull Application application) {
        super(application);
        repo = new QuizRepository(application);
    }

    public LiveData<Resource<List<QuestionWithAnswers>>> getDeThi() {
        return deThi;
    }

    /** Sinh đề một lần duy nhất — gọi lại (VD sau khi xoay màn hình) sẽ không sinh đề mới. */
    public void sinhDeNeuChua() {
        if (deThi.getValue() != null) {
            return;
        }
        deThi.setValue(Resource.dangTai());
        repo.generateRandomExam(list -> {
            if (list == null || list.isEmpty()) {
                deThi.setValue(Resource.loi("Chưa có câu hỏi trong ngân hàng."));
                return;
            }
            deThi.setValue(Resource.thanhCong(list));
        });
    }

    // ---------- Trạng thái làm bài ----------

    public List<QuestionWithAnswers> cacCau() {
        Resource<List<QuestionWithAnswers>> res = deThi.getValue();
        return res == null || res.duLieu == null ? new ArrayList<>() : res.duLieu;
    }

    public int getViTri() {
        return viTri;
    }

    public boolean chuyen(int buoc) {
        int moi = viTri + buoc;
        if (moi < 0 || moi >= cacCau().size()) {
            return false;
        }
        viTri = moi;
        return true;
    }

    public QuestionWithAnswers cauHienTai() {
        List<QuestionWithAnswers> ds = cacCau();
        return ds.isEmpty() ? null : ds.get(viTri);
    }

    public void chonDapAn(int questionId, int answerId) {
        dapAnDaChon.put(questionId, answerId);
    }

    /** Đáp án đã chọn cho một câu, null nếu chưa chọn. */
    public Integer dapAnCua(int questionId) {
        return dapAnDaChon.get(questionId);
    }

    public int soCauChuaLam() {
        return cacCau().size() - dapAnDaChon.size();
    }

    public boolean daNop() {
        return daNop;
    }

    // ---------- Chấm điểm và lưu ----------

    /**
     * Chấm bài và lưu lượt thi xuống Room. Trả kết quả cho View qua callback để
     * View mở màn hình Kết quả.
     *
     * @param giayDaDung số giây đã làm bài, do View đo bằng đồng hồ đếm ngược
     * @return kết quả chấm, hoặc null nếu đã nộp rồi (chặn nộp hai lần)
     */
    public ExamResult chamVaLuu(int giayDaDung) {
        if (daNop) {
            return null;
        }
        daNop = true;

        List<QuestionWithAnswers> ds = cacCau();
        ExamResult kq = ExamScorer.cham(ds, dapAnDaChon, ExamConfig.NGUONG_DAT);

        Attempt attempt = new Attempt();
        attempt.userId = DatabaseSeeder.LOCAL_USER_ID;
        attempt.examSetId = 0; // đề ngẫu nhiên
        attempt.soCauDung = kq.soCauDung;
        attempt.ketQua = kq.dat ? Attempt.KET_QUA_DAT : Attempt.KET_QUA_TRUOT;
        attempt.lyDoTruot = kq.lyDoTruot;
        attempt.thoiGianLam = giayDaDung;
        attempt.ngayThi = System.currentTimeMillis();

        List<UserAnswer> chiTiet = new ArrayList<>();
        for (QuestionWithAnswers qa : ds) {
            Integer chon = dapAnDaChon.get(qa.question.id);
            boolean dung = chon != null && chon == qa.correctAnswerId();
            chiTiet.add(new UserAnswer(0, qa.question.id, chon == null ? 0 : chon, dung));
        }

        // attempts + user_answers được ghi trong MỘT giao dịch ở tầng DAO.
        repo.saveAttempt(attempt, chiTiet, id -> {
        });

        return kq;
    }
}
