package com.example.oto.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.oto.data.Callback;
import com.example.oto.data.QuizRepository;
import com.example.oto.data.entity.TrafficSign;
import com.example.oto.ui.Resource;

/** ViewModel form thêm/sửa biển báo. */
public class SuaBienBaoViewModel extends AndroidViewModel {

    private final QuizRepository repo;
    private final MutableLiveData<Resource<TrafficSign>> bienBao = new MutableLiveData<>();

    public SuaBienBaoViewModel(@NonNull Application application) {
        super(application);
        repo = new QuizRepository(application);
    }

    public LiveData<Resource<TrafficSign>> getBienBao() {
        return bienBao;
    }

    public void napNeuChua(int signId) {
        if (bienBao.getValue() != null) {
            return;
        }
        bienBao.setValue(Resource.dangTai());
        repo.getSign(signId, sign -> {
            if (sign == null) {
                bienBao.setValue(Resource.loi("Không tìm thấy biển báo."));
                return;
            }
            bienBao.setValue(Resource.thanhCong(sign));
        });
    }

    /** Biển đang sửa, null khi đang thêm mới. */
    public TrafficSign bienDangSua() {
        Resource<TrafficSign> res = bienBao.getValue();
        return res == null ? null : res.duLieu;
    }

    /**
     * Lưu biển báo.
     *
     * @param ketQua nhận false khi mã biển đã tồn tại ở một biển khác — mã biển là
     *               định danh nghiệp vụ nên không được trùng.
     */
    public void luu(TrafficSign sign, Callback<Boolean> ketQua) {
        repo.saveSign(sign, ketQua);
    }

    public void xoa(TrafficSign sign, Runnable khiXong) {
        repo.deleteSign(sign, ok -> khiXong.run());
    }
}
