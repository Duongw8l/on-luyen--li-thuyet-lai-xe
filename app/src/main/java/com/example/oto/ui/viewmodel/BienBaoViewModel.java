package com.example.oto.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.oto.data.QuizRepository;
import com.example.oto.data.entity.TrafficSign;

import java.util.List;

/**
 * ViewModel màn Tra cứu biển báo.
 *
 * Điểm đáng nói khi bảo vệ: bộ lọc (nhóm biển + từ khoá) được giữ trong một LiveData
 * riêng ({@code boLoc}). Mỗi lần bộ lọc đổi, {@code switchMap} tự huỷ đăng ký truy vấn
 * cũ và đăng ký truy vấn mới. Trước đây Activity phải tự gọi removeObservers() trước
 * mỗi lần đổi bộ lọc — quên một lần là danh sách bị nhiều observer ghi đè lẫn nhau.
 * switchMap làm việc đó tự động và đúng theo vòng đời.
 */
public class BienBaoViewModel extends AndroidViewModel {

    /** Cặp giá trị bộ lọc hiện tại. */
    public static class BoLoc {
        public final String nhom; // null = tất cả nhóm
        public final String tuKhoa;

        BoLoc(String nhom, String tuKhoa) {
            this.nhom = nhom;
            this.tuKhoa = tuKhoa;
        }
    }

    private final QuizRepository repo;
    private final MutableLiveData<BoLoc> boLoc = new MutableLiveData<>(new BoLoc(null, ""));

    private final LiveData<List<TrafficSign>> danhSach;

    public BienBaoViewModel(@NonNull Application application) {
        super(application);
        repo = new QuizRepository(application);
        danhSach = Transformations.switchMap(boLoc,
                bl -> repo.filterSigns(bl.nhom, bl.tuKhoa));
    }

    public LiveData<List<TrafficSign>> getDanhSach() {
        return danhSach;
    }

    public LiveData<List<String>> getNhomBien() {
        return repo.getSignGroups();
    }

    public void datNhom(String nhom) {
        BoLoc ht = boLoc.getValue();
        boLoc.setValue(new BoLoc(nhom, ht == null ? "" : ht.tuKhoa));
    }

    public void datTuKhoa(String tuKhoa) {
        BoLoc ht = boLoc.getValue();
        boLoc.setValue(new BoLoc(ht == null ? null : ht.nhom, tuKhoa));
    }
}
