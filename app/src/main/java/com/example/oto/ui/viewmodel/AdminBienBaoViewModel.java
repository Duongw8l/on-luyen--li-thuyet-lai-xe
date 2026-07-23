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

/** ViewModel màn Quản trị biển báo: lọc theo nhóm + từ khoá, xoá biển. */
public class AdminBienBaoViewModel extends AndroidViewModel {

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

    public AdminBienBaoViewModel(@NonNull Application application) {
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
        BoLoc b = boLoc.getValue();
        boLoc.setValue(new BoLoc(nhom, b == null ? "" : b.tuKhoa));
    }

    public void datTuKhoa(String tuKhoa) {
        BoLoc b = boLoc.getValue();
        boLoc.setValue(new BoLoc(b == null ? null : b.nhom, tuKhoa));
    }

    public void xoa(TrafficSign sign, Runnable khiXong) {
        repo.deleteSign(sign, ok -> khiXong.run());
    }
}
