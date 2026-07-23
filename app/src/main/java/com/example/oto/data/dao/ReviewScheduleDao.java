package com.example.oto.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.oto.data.entity.ReviewSchedule;

import java.util.List;

/**
 * DAO cho lịch ôn lại giãn dần (spaced repetition).
 *
 * Ý tưởng: câu nào hay sai thì ôn lại sớm, câu nào đã nhớ thì giãn dần ra
 * (1 ngày → 3 → 7 → 14...). Nhờ vậy người học không phải ôn lại đều tất cả 600 câu.
 */
@Dao
public interface ReviewScheduleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ReviewSchedule item);

    @Update
    void update(ReviewSchedule item);

    /** Lịch ôn của một câu cụ thể; null nếu câu đó chưa từng vào lịch. */
    @Query("SELECT * FROM review_schedule WHERE user_id = :userId AND question_id = :questionId LIMIT 1")
    ReviewSchedule getOne(String userId, int questionId);

    /** Các câu tới hạn ôn lại tính đến thời điểm :moc (epoch millis). */
    @Query("SELECT * FROM review_schedule WHERE user_id = :userId AND lan_on_tiep <= :moc "
            + "ORDER BY lan_on_tiep ASC")
    List<ReviewSchedule> getToiHan(String userId, long moc);

    /** Đếm số câu tới hạn — dùng hiện huy hiệu trên trang chủ. */
    @Query("SELECT COUNT(*) FROM review_schedule WHERE user_id = :userId AND lan_on_tiep <= :moc")
    LiveData<Integer> demToiHanLive(String userId, long moc);

    /** Các câu sai nhiều nhất — dùng gợi ý "câu bạn hay sai". */
    @Query("SELECT * FROM review_schedule WHERE user_id = :userId ORDER BY so_lan_sai DESC LIMIT :n")
    List<ReviewSchedule> getHaySaiNhat(String userId, int n);

    @Query("DELETE FROM review_schedule WHERE user_id = :userId AND question_id = :questionId")
    void xoa(String userId, int questionId);

    @Query("DELETE FROM review_schedule WHERE user_id = :userId")
    void xoaTatCa(String userId);
}
