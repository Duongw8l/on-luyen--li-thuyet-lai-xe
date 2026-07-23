package com.example.oto.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.oto.data.DatabaseSeeder;
import com.example.oto.data.QuizRepository;
import com.example.oto.data.relation.ChapterStat;
import com.example.oto.ui.Resource;

import java.util.List;

/** ViewModel màn Thống kê: tỷ lệ đúng theo chương và chương yếu nhất. */
public class ThongKeViewModel extends AndroidViewModel {

    private final QuizRepository repo;
    private final MutableLiveData<Resource<List<ChapterStat>>> thongKe = new MutableLiveData<>();

    public ThongKeViewModel(@NonNull Application application) {
        super(application);
        repo = new QuizRepository(application);
    }

    public LiveData<Resource<List<ChapterStat>>> getThongKe() {
        return thongKe;
    }

    public void nap() {
        thongKe.setValue(Resource.dangTai());
        repo.getChapterStats(DatabaseSeeder.LOCAL_USER_ID,
                list -> thongKe.setValue(Resource.thanhCong(list)));
    }

    /**
     * Chương có tỷ lệ đúng thấp nhất — dùng để gợi ý "nên ôn chương này trước".
     * Trả về null khi chưa có dữ liệu thống kê.
     */
    public ChapterStat chuongYeuNhat() {
        Resource<List<ChapterStat>> res = thongKe.getValue();
        if (res == null || res.duLieu == null || res.duLieu.isEmpty()) {
            return null;
        }
        ChapterStat yeuNhat = res.duLieu.get(0);
        for (ChapterStat s : res.duLieu) {
            if (s.phanTram() < yeuNhat.phanTram()) {
                yeuNhat = s;
            }
        }
        return yeuNhat;
    }
}
