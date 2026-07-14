package com.example.oto.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.oto.data.entity.User;

@Dao
public interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(User user);

    @Update
    void update(User user);

    @Query("SELECT * FROM users WHERE id = :id")
    User getById(String id);

    @Query("SELECT * FROM users WHERE id = :id")
    LiveData<User> getByIdLive(String id);

    @Query("UPDATE users SET ngay_thi_du_kien = :millis WHERE id = :id")
    void updateNgayThi(String id, long millis);

    /** Đồng bộ họ tên + email từ tài khoản Firebase xuống Room (không có mật khẩu). */
    @Query("UPDATE users SET ho_ten = :hoTen, email = :email WHERE id = :id")
    void updateHoSo(String id, String hoTen, String email);

    /** Đường dẫn ảnh đại diện đã nén trong bộ nhớ riêng của app. */
    @Query("UPDATE users SET anh_dai_dien = :duongDan WHERE id = :id")
    void updateAnhDaiDien(String id, String duongDan);

    @Query("SELECT COUNT(*) FROM users")
    int count();
}
