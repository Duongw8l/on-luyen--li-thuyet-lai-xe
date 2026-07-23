package com.example.oto.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import android.util.Log;

import com.example.oto.data.DatabaseSeeder;
import com.example.oto.data.QuestionSyncRepository;
import com.example.oto.data.QuizRepository;
import com.example.oto.data.entity.User;

import java.util.concurrent.TimeUnit;

/** ViewModel trang chủ: đếm ngược tới ngày thi dự kiến. */
public class MainViewModel extends AndroidViewModel {

    /** Trạng thái đếm ngược, để View chỉ việc chọn chuỗi hiển thị tương ứng. */
    public enum TrangThaiDemNguoc {
        CHUA_DAT,
        DA_QUA,
        HOM_NAY,
        CON_LAI
    }

    public static class DemNguoc {
        public final TrangThaiDemNguoc trangThai;
        public final long soNgay;

        DemNguoc(TrangThaiDemNguoc trangThai, long soNgay) {
            this.trangThai = trangThai;
            this.soNgay = soNgay;
        }
    }

    private final QuizRepository repo;
    private final QuestionSyncRepository syncRepo;
    private final MutableLiveData<DemNguoc> demNguoc = new MutableLiveData<>();

    public MainViewModel(@NonNull Application application) {
        super(application);
        repo = new QuizRepository(application);
        syncRepo = new QuestionSyncRepository(application);
    }

    public LiveData<DemNguoc> getDemNguoc() {
        return demNguoc;
    }

    public void capNhatDemNguoc() {
        repo.getUser(DatabaseSeeder.LOCAL_USER_ID, this::tinhDemNguoc);
    }

    /**
     * Kéo câu hỏi mới do admin thêm/sửa từ Firestore về Room (im lặng, chạy nền).
     * Chạy khi mở trang chủ để mọi người dùng — kể cả không phải admin — luôn có
     * ngân hàng câu hỏi mới nhất. Lỗi mạng chỉ ghi log, KHÔNG làm phiền người dùng vì
     * ôn/thi vẫn chạy tốt với dữ liệu offline sẵn có.
     */
    public void dongBoCauHoi() {
        syncRepo.keoVe(
                kq -> {
                    if (kq.soCauKeoVe > 0) {
                        Log.i("MainViewModel", "Da dong bo " + kq.soCauKeoVe + " cau hoi moi.");
                    }
                },
                loi -> Log.w("MainViewModel", "Dong bo cau hoi that bai: " + loi));
    }

    private void tinhDemNguoc(User user) {
        if (user == null || user.ngayThiDuKien <= 0) {
            demNguoc.setValue(new DemNguoc(TrangThaiDemNguoc.CHUA_DAT, 0));
            return;
        }
        long conLai = user.ngayThiDuKien - System.currentTimeMillis();
        long ngay = TimeUnit.MILLISECONDS.toDays(conLai);
        if (ngay < 0) {
            demNguoc.setValue(new DemNguoc(TrangThaiDemNguoc.DA_QUA, 0));
        } else if (ngay == 0) {
            demNguoc.setValue(new DemNguoc(TrangThaiDemNguoc.HOM_NAY, 0));
        } else {
            demNguoc.setValue(new DemNguoc(TrangThaiDemNguoc.CON_LAI, ngay));
        }
    }

    public void datNgayThi(long millis) {
        repo.setNgayThiDuKien(DatabaseSeeder.LOCAL_USER_ID, millis);
        // Ghi chạy trên luồng nền; đọc lại sau một nhịp ngắn để lấy giá trị vừa ghi.
        new android.os.Handler(android.os.Looper.getMainLooper())
                .postDelayed(this::capNhatDemNguoc, 200);
    }
}
