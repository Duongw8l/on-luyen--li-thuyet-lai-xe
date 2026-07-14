package com.example.oto.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

import com.example.oto.data.entity.Attempt;
import com.example.oto.data.entity.UserAnswer;
import com.example.oto.data.relation.ChapterStat;

import java.util.List;

@Dao
public interface AttemptDao {

    @Insert
    long insertAttempt(Attempt attempt);

    @Insert
    void insertUserAnswers(List<UserAnswer> answers);

    /**
     * Lưu trọn một lượt thi (attempts + nhiều user_answers) trong MỘT giao dịch:
     * hoặc lưu hết, hoặc không lưu gì — đảm bảo toàn vẹn dữ liệu.
     */
    @Transaction
    default long saveAttempt(Attempt attempt, List<UserAnswer> answers) {
        long attemptId = insertAttempt(attempt);
        for (UserAnswer ua : answers) {
            ua.attemptId = (int) attemptId;
        }
        insertUserAnswers(answers);
        return attemptId;
    }

    @Query("SELECT * FROM attempts WHERE user_id = :userId ORDER BY ngay_thi DESC")
    LiveData<List<Attempt>> getByUser(String userId);

    /** Lọc lịch sử theo khoảng ngày (dùng DatePicker). */
    @Query("SELECT * FROM attempts WHERE user_id = :userId AND ngay_thi BETWEEN :from AND :to ORDER BY ngay_thi DESC")
    LiveData<List<Attempt>> getByUserInRange(String userId, long from, long to);

    @Delete
    void delete(Attempt attempt);

    @Query("DELETE FROM attempts WHERE user_id = :userId")
    void clearHistory(String userId);

    /** Các id câu hỏi mà người dùng từng trả lời sai (để ôn lại câu sai). */
    @Query("SELECT DISTINCT ua.question_id FROM user_answers ua " +
            "JOIN attempts a ON ua.attempt_id = a.id " +
            "WHERE a.user_id = :userId AND ua.dung_sai = 0")
    List<Integer> getWrongQuestionIds(String userId);

    /** Tỷ lệ đúng theo từng chương (phân tích điểm yếu). */
    @Query("SELECT c.id AS chapterId, c.so_thu_tu AS soThuTu, c.ten_chuong AS tenChuong, " +
            "COUNT(*) AS tong, SUM(CASE WHEN ua.dung_sai = 1 THEN 1 ELSE 0 END) AS dung " +
            "FROM user_answers ua " +
            "JOIN attempts a ON ua.attempt_id = a.id " +
            "JOIN questions q ON ua.question_id = q.id " +
            "JOIN chapters c ON q.chapter_id = c.id " +
            "WHERE a.user_id = :userId " +
            "GROUP BY c.id ORDER BY c.so_thu_tu ASC")
    List<ChapterStat> getChapterStats(String userId);
}
