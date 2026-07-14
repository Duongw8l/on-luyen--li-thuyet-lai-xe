package com.example.oto.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.oto.data.entity.TrafficSign;

import java.util.List;

@Dao
public interface TrafficSignDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<TrafficSign> signs);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(TrafficSign sign);

    @Update
    void update(TrafficSign sign);

    @Delete
    void delete(TrafficSign sign);

    @Query("SELECT * FROM traffic_signs ORDER BY ma_bien ASC")
    LiveData<List<TrafficSign>> getAllLive();

    @Query("SELECT * FROM traffic_signs WHERE nhom_bien = :nhom ORDER BY ma_bien ASC")
    LiveData<List<TrafficSign>> getByGroup(String nhom);

    /** Tìm biển báo theo tên hoặc mã biển. */
    @Query("SELECT * FROM traffic_signs WHERE ten_bien LIKE '%' || :kw || '%' OR ma_bien LIKE '%' || :kw || '%' ORDER BY ma_bien ASC")
    LiveData<List<TrafficSign>> search(String kw);

    /**
     * Lọc theo nhóm biển (Spinner) kết hợp từ khóa (ô tìm kiếm).
     * nhom = null nghĩa là "Tất cả nhóm".
     */
    @Query("SELECT * FROM traffic_signs WHERE (:nhom IS NULL OR nhom_bien = :nhom) " +
            "AND (ten_bien LIKE '%' || :kw || '%' OR ma_bien LIKE '%' || :kw || '%') " +
            "ORDER BY ma_bien ASC")
    LiveData<List<TrafficSign>> filter(String nhom, String kw);

    /** Danh sách nhóm biển hiện có — đổ vào Spinner. */
    @Query("SELECT DISTINCT nhom_bien FROM traffic_signs WHERE nhom_bien IS NOT NULL ORDER BY nhom_bien ASC")
    LiveData<List<String>> getGroups();

    @Query("SELECT * FROM traffic_signs WHERE id = :id")
    TrafficSign getById(int id);

    /** Kiểm tra trùng mã biển (bỏ qua chính biển đang sửa). */
    @Query("SELECT COUNT(*) FROM traffic_signs WHERE ma_bien = :maBien AND id <> :boQuaId")
    int demTrungMa(String maBien, int boQuaId);

    @Query("SELECT COUNT(*) FROM traffic_signs")
    int count();
}
