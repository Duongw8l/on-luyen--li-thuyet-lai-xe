package com.example.oto.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.oto.data.entity.ExamSet;
import com.example.oto.data.entity.ExamSetQuestion;

import java.util.List;

@Dao
public interface ExamSetDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(ExamSet examSet);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertLinks(List<ExamSetQuestion> links);

    @Query("SELECT * FROM exam_sets ORDER BY id ASC")
    LiveData<List<ExamSet>> getAllLive();

    @Query("SELECT * FROM exam_sets ORDER BY id ASC")
    List<ExamSet> getAllSync();

    @Query("SELECT * FROM exam_sets WHERE id = :id")
    ExamSet getById(int id);

    /** Danh sách id câu hỏi thuộc một bộ đề. */
    @Query("SELECT question_id FROM exam_set_questions WHERE exam_set_id = :examSetId")
    List<Integer> getQuestionIds(int examSetId);

    @Query("SELECT COUNT(*) FROM exam_sets")
    int count();
}
