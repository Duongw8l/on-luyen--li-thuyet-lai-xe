package com.example.oto.data;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;

import com.example.oto.data.entity.Answer;
import com.example.oto.data.entity.Attempt;
import com.example.oto.data.entity.Chapter;
import com.example.oto.data.entity.ExamSet;
import com.example.oto.data.entity.Question;
import com.example.oto.data.entity.TrafficSign;
import com.example.oto.data.entity.User;
import com.example.oto.data.entity.UserAnswer;
import com.example.oto.data.relation.ChapterStat;
import com.example.oto.data.relation.QuestionWithAnswers;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Repository — "một cửa" điều phối nguồn dữ liệu (mục 3.1 tài liệu: MVVM).
 * Bản offline chỉ đọc/ghi Room. Khi gắn Firebase, phần đồng bộ được thêm ở đây.
 */
public class QuizRepository {

    private final AppDatabase db;
    private final Handler main = new Handler(Looper.getMainLooper());

    public QuizRepository(Context context) {
        this.db = AppDatabase.get(context);
    }

    // ---------- Chương ----------
    public LiveData<List<Chapter>> getChapters() {
        return db.chapterDao().getAllLive();
    }

    // ---------- Ôn tập ----------
    public void getQuestionsByChapter(int chapterId, Callback<List<QuestionWithAnswers>> cb) {
        AppDatabase.IO.execute(() -> {
            List<QuestionWithAnswers> list = db.questionDao().getByChapter(chapterId);
            main.post(() -> cb.onResult(list));
        });
    }

    public void getDiemLiet(Callback<List<QuestionWithAnswers>> cb) {
        AppDatabase.IO.execute(() -> {
            List<QuestionWithAnswers> list = db.questionDao().getDiemLiet();
            main.post(() -> cb.onResult(list));
        });
    }

    public LiveData<List<QuestionWithAnswers>> searchQuestions(String keyword) {
        return db.questionDao().search(keyword);
    }

    // ---------- CRUD câu hỏi (màn Quản trị) ----------

    /** Danh sách câu hỏi cho Admin: lọc theo từ khóa + chương + câu điểm liệt. */
    public LiveData<List<QuestionWithAnswers>> filterQuestions(String kw, int chapterId,
                                                               boolean chiDiemLiet) {
        return db.questionDao().filterForAdmin(kw == null ? "" : kw, chapterId, chiDiemLiet);
    }

    public void getQuestion(int id, Callback<QuestionWithAnswers> cb) {
        AppDatabase.IO.execute(() -> {
            QuestionWithAnswers qa = db.questionDao().getOne(id);
            main.post(() -> cb.onResult(qa));
        });
    }

    /**
     * Thêm mới (question.id == 0) hoặc cập nhật câu hỏi kèm đáp án.
     * Cả hai đều chạy trong một giao dịch của DAO.
     *
     * Trả về id câu hỏi (mới sinh khi thêm) qua callback: nơi gọi cần id này để
     * đẩy câu hỏi lên Firestore đồng bộ cho các máy khác.
     */
    public void saveQuestion(Question question, List<Answer> answers, Callback<Long> cb) {
        AppDatabase.IO.execute(() -> {
            // Đóng dấu thời điểm sửa: đây là mốc mà đồng bộ delta dựa vào để biết
            // câu nào đã đổi kể từ lần đồng bộ trước.
            question.updatedAt = System.currentTimeMillis();
            if (question.id == 0) {
                db.questionDao().insertQuestionWithAnswers(question, answers);
            } else {
                db.questionDao().updateQuestionWithAnswers(question, answers);
            }
            long id = question.id;
            main.post(() -> cb.onResult(id));
        });
    }

    /** Xoá câu hỏi — đáp án của nó bị xoá theo nhờ ràng buộc CASCADE. */
    public void deleteQuestion(Question question, Callback<Boolean> cb) {
        AppDatabase.IO.execute(() -> {
            db.questionDao().delete(question);
            main.post(() -> cb.onResult(true));
        });
    }

    // ---------- Thi thử ----------
    public LiveData<List<ExamSet>> getExamSets() {
        return db.examSetDao().getAllLive();
    }

    /** Lấy câu hỏi của một bộ đề cố định. */
    public void getExamSetQuestions(int examSetId, Callback<List<QuestionWithAnswers>> cb) {
        AppDatabase.IO.execute(() -> {
            List<Integer> ids = db.examSetDao().getQuestionIds(examSetId);
            List<QuestionWithAnswers> list = db.questionDao().getByIds(ids);
            main.post(() -> cb.onResult(list));
        });
    }

    /**
     * Sinh đề ngẫu nhiên theo cấu trúc: đảm bảo có đủ số câu điểm liệt yêu cầu,
     * phần còn lại lấy ngẫu nhiên, không trùng.
     */
    public void generateRandomExam(Callback<List<QuestionWithAnswers>> cb) {
        AppDatabase.IO.execute(() -> {
            Map<Integer, QuestionWithAnswers> picked = new LinkedHashMap<>();
            for (QuestionWithAnswers qa : db.questionDao().getRandomDiemLiet(ExamConfig.SO_CAU_DIEM_LIET)) {
                picked.put(qa.question.id, qa);
            }
            for (QuestionWithAnswers qa : db.questionDao().getRandom(ExamConfig.SO_CAU * 3)) {
                if (picked.size() >= ExamConfig.SO_CAU) {
                    break;
                }
                picked.put(qa.question.id, qa);
            }
            List<QuestionWithAnswers> list = new ArrayList<>(picked.values());
            main.post(() -> cb.onResult(list));
        });
    }

    // ---------- Lưu bài thi (transaction) ----------
    public void saveAttempt(Attempt attempt, List<UserAnswer> answers, Callback<Long> cb) {
        AppDatabase.IO.execute(() -> {
            long id = db.attemptDao().saveAttempt(attempt, answers);
            main.post(() -> cb.onResult(id));
        });
    }

    // ---------- Thống kê ----------
    public void getChapterStats(String userId, Callback<List<ChapterStat>> cb) {
        AppDatabase.IO.execute(() -> {
            List<ChapterStat> stats = db.attemptDao().getChapterStats(userId);
            main.post(() -> cb.onResult(stats));
        });
    }

    // ---------- Biển báo ----------
    public LiveData<List<TrafficSign>> getSigns() {
        return db.trafficSignDao().getAllLive();
    }

    public LiveData<List<TrafficSign>> searchSigns(String kw) {
        return db.trafficSignDao().search(kw);
    }

    /** Lọc biển báo theo nhóm (Spinner) + từ khóa. nhom = null: tất cả nhóm. */
    public LiveData<List<TrafficSign>> filterSigns(String nhom, String kw) {
        return db.trafficSignDao().filter(nhom, kw == null ? "" : kw);
    }

    public LiveData<List<String>> getSignGroups() {
        return db.trafficSignDao().getGroups();
    }

    // ---------- CRUD biển báo (màn Quản trị) ----------

    public void getSign(int id, Callback<TrafficSign> cb) {
        AppDatabase.IO.execute(() -> {
            TrafficSign s = db.trafficSignDao().getById(id);
            main.post(() -> cb.onResult(s));
        });
    }

    /**
     * Lưu biển báo. Trả về false nếu mã biển đã tồn tại ở một biển khác —
     * mã biển là định danh nghiệp vụ nên không được trùng.
     */
    public void saveSign(TrafficSign sign, Callback<Boolean> cb) {
        AppDatabase.IO.execute(() -> {
            if (db.trafficSignDao().demTrungMa(sign.maBien, sign.id) > 0) {
                main.post(() -> cb.onResult(false));
                return;
            }
            if (sign.id == 0) {
                db.trafficSignDao().insert(sign);
            } else {
                db.trafficSignDao().update(sign);
            }
            main.post(() -> cb.onResult(true));
        });
    }

    public void deleteSign(TrafficSign sign, Callback<Boolean> cb) {
        AppDatabase.IO.execute(() -> {
            db.trafficSignDao().delete(sign);
            main.post(() -> cb.onResult(true));
        });
    }

    // ---------- Người dùng / ngày thi dự kiến ----------
    public void getUser(String id, Callback<User> cb) {
        AppDatabase.IO.execute(() -> {
            User u = db.userDao().getById(id);
            main.post(() -> cb.onResult(u));
        });
    }

    public void setNgayThiDuKien(String userId, long millis) {
        AppDatabase.IO.execute(() -> db.userDao().updateNgayThi(userId, millis));
    }

    /** Lưu họ tên + email lấy từ tài khoản Firebase (mật khẩu không bao giờ lưu ở đây). */
    public void capNhatHoSo(String userId, String hoTen, String email) {
        AppDatabase.IO.execute(() -> db.userDao().updateHoSo(userId, hoTen, email));
    }

    public void capNhatAnhDaiDien(String userId, String duongDan) {
        AppDatabase.IO.execute(() -> db.userDao().updateAnhDaiDien(userId, duongDan));
    }
}
