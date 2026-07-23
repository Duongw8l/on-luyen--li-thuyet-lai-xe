package com.example.oto.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.example.oto.data.entity.Answer;
import com.example.oto.data.entity.Question;
import com.example.oto.data.relation.QuestionWithAnswers;

import java.util.List;

@Dao
public interface QuestionDao {

    // ----- CRUD Admin -----
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Question question);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAnswers(List<Answer> answers);

    @Update
    void update(Question question);

    @Delete
    void delete(Question question); // CASCADE tự xoá đáp án

    @Query("DELETE FROM answers WHERE question_id = :questionId")
    void deleteAnswersOf(int questionId);

    /**
     * Thêm một câu hỏi kèm 4 đáp án trong cùng một giao dịch (toàn vẹn dữ liệu).
     * Trả về id vừa sinh để nơi gọi biết định danh câu hỏi (dùng khi đẩy lên Firestore).
     */
    @Transaction
    default long insertQuestionWithAnswers(Question question, List<Answer> answers) {
        long qid = insert(question);
        question.id = (int) qid;
        for (Answer a : answers) {
            a.questionId = (int) qid;
        }
        insertAnswers(answers);
        return qid;
    }

    /**
     * Sửa câu hỏi kèm đáp án: xoá hết đáp án cũ rồi ghi đáp án mới, tất cả trong
     * MỘT giao dịch — không bao giờ tồn tại câu hỏi mất đáp án giữa chừng.
     */
    @Transaction
    default void updateQuestionWithAnswers(Question question, List<Answer> answers) {
        update(question);
        deleteAnswersOf(question.id);
        for (Answer a : answers) {
            a.id = 0; // để Room tự sinh id mới
            a.questionId = question.id;
        }
        insertAnswers(answers);
    }

    // ----- Truy vấn / lọc / tìm kiếm -----
    @Transaction
    @Query("SELECT * FROM questions WHERE chapter_id = :chapterId ORDER BY id ASC")
    List<QuestionWithAnswers> getByChapter(int chapterId);

    @Transaction
    @Query("SELECT * FROM questions WHERE is_diem_liet = 1 ORDER BY id ASC")
    List<QuestionWithAnswers> getDiemLiet();

    @Transaction
    @Query("SELECT * FROM questions WHERE id IN (:ids)")
    List<QuestionWithAnswers> getByIds(List<Integer> ids);

    /** Tìm kiếm câu hỏi theo từ khóa trong nội dung. */
    @Transaction
    @Query("SELECT * FROM questions WHERE noi_dung LIKE '%' || :keyword || '%' ORDER BY id ASC")
    LiveData<List<QuestionWithAnswers>> search(String keyword);

    @Query("SELECT * FROM questions ORDER BY id ASC")
    LiveData<List<Question>> getAllLive();

    @Transaction
    @Query("SELECT * FROM questions WHERE id = :id")
    QuestionWithAnswers getOne(int id);

    /**
     * Bộ lọc màn Quản trị: từ khóa + chương + chỉ câu điểm liệt.
     * chapterId = 0 nghĩa là tất cả các chương.
     */
    @Transaction
    @Query("SELECT * FROM questions WHERE noi_dung LIKE '%' || :kw || '%' " +
            "AND (:chapterId = 0 OR chapter_id = :chapterId) " +
            "AND (:chiDiemLiet = 0 OR is_diem_liet = 1) " +
            "ORDER BY id ASC")
    LiveData<List<QuestionWithAnswers>> filterForAdmin(String kw, int chapterId, boolean chiDiemLiet);

    /** Lấy ngẫu nhiên n câu hỏi (dùng sinh đề ngẫu nhiên). */
    @Transaction
    @Query("SELECT * FROM questions ORDER BY RANDOM() LIMIT :n")
    List<QuestionWithAnswers> getRandom(int n);

    /** Lấy ngẫu nhiên n câu điểm liệt (đảm bảo đề có câu điểm liệt). */
    @Transaction
    @Query("SELECT * FROM questions WHERE is_diem_liet = 1 ORDER BY RANDOM() LIMIT :n")
    List<QuestionWithAnswers> getRandomDiemLiet(int n);

    /**
     * Các câu đã sửa kể từ mốc :moc — dùng cho đồng bộ delta LÊN Firestore.
     * TUYỆT ĐỐI không tải/đẩy lại toàn bộ 600 câu mỗi lần đồng bộ.
     */
    @Query("SELECT * FROM questions WHERE updated_at > :moc ORDER BY updated_at ASC")
    List<Question> getSuaSau(long moc);

    /** Đáp án của một câu (dùng khi đẩy câu hỏi lên Firestore). */
    @Query("SELECT * FROM answers WHERE question_id = :questionId ORDER BY id ASC")
    List<Answer> getAnswersOf(int questionId);

    @Query("SELECT COUNT(*) FROM questions WHERE id = :id")
    int countById(int id);

    @Query("SELECT COUNT(*) FROM questions")
    int count();

    /**
     * Ghi một câu hỏi tải VỀ từ Firestore vào Room (upsert theo id Firestore).
     *
     * KHÔNG dùng INSERT REPLACE cho câu hỏi: REPLACE = XOÁ dòng cũ rồi chèn lại,
     * mà xoá câu hỏi sẽ CASCADE xoá luôn user_answers/notes/review_schedule của
     * người dùng — tức là đồng bộ một câu đã sửa sẽ làm MẤT lịch sử làm bài.
     * Vì vậy: câu đã có thì UPDATE (không đụng dòng), câu mới thì INSERT.
     * Chỉ có đáp án là được thay mới hoàn toàn (đáp án không có bảng con tham chiếu).
     */
    @Transaction
    default void upsertTuFirestore(Question question, List<Answer> answers) {
        if (countById(question.id) > 0) {
            update(question);
        } else {
            insert(question); // dòng chưa tồn tại nên không xảy ra CASCADE
        }
        deleteAnswersOf(question.id);
        for (Answer a : answers) {
            a.id = 0; // để Room tự sinh id mới
            a.questionId = question.id;
        }
        insertAnswers(answers);
    }
}
