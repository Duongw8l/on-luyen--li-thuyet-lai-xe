package com.example.oto.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.oto.data.entity.Chapter;

import java.util.List;

@Dao
public interface ChapterDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Chapter> chapters);

    @Query("SELECT * FROM chapters ORDER BY so_thu_tu ASC")
    LiveData<List<Chapter>> getAllLive();

    @Query("SELECT * FROM chapters ORDER BY so_thu_tu ASC")
    List<Chapter> getAllSync();

    @Query("SELECT COUNT(*) FROM chapters")
    int count();
}
