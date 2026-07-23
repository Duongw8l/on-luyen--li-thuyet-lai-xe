package com.example.oto.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.example.oto.data.DatabaseSeeder;
import com.example.oto.data.QuizRepository;

/**
 * ViewModel màn Cá nhân.
 *
 * Chỉ phụ trách phần dữ liệu (đường dẫn ảnh đại diện lưu trong Room). Việc chọn/chụp
 * ảnh dùng ActivityResultLauncher nên bắt buộc phải ở Activity — launcher gắn với
 * vòng đời của Activity, không đưa xuống ViewModel được.
 */
public class CaNhanViewModel extends AndroidViewModel {

    private final QuizRepository repo;

    public CaNhanViewModel(@NonNull Application application) {
        super(application);
        repo = new QuizRepository(application);
    }

    /** @param duongDan đường dẫn file ảnh đã nén trong bộ nhớ riêng; null = xoá ảnh. */
    public void capNhatAnhDaiDien(String duongDan) {
        repo.capNhatAnhDaiDien(DatabaseSeeder.LOCAL_USER_ID, duongDan);
    }
}
