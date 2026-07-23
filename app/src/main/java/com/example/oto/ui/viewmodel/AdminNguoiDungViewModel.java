package com.example.oto.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.oto.auth.HoSoNguoiDung;
import com.example.oto.auth.VaiTro;
import com.example.oto.data.NguoiDungRepository;
import com.example.oto.ui.Resource;

import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel màn Quản trị người dùng.
 *
 * Không cần Application context (Firestore tự khởi tạo) nên kế thừa ViewModel thường
 * thay vì AndroidViewModel.
 *
 * Toàn bộ danh sách được tải một lần vào {@code tatCa}; bộ lọc theo vai trò và từ khoá
 * chạy dưới máy trên danh sách đó, không gọi lại mạng mỗi lần gõ phím.
 */
public class AdminNguoiDungViewModel extends ViewModel {

    private final NguoiDungRepository repo = new NguoiDungRepository();

    /** Danh sách gốc tải từ Firestore. */
    private final List<HoSoNguoiDung> tatCa = new ArrayList<>();

    private final MutableLiveData<Resource<List<HoSoNguoiDung>>> hienThi = new MutableLiveData<>();

    private String tuKhoa = "";
    private String vaiTroLoc = null; // null = tất cả vai trò

    public LiveData<Resource<List<HoSoNguoiDung>>> getHienThi() {
        return hienThi;
    }

    public String uidHienTai() {
        return repo.uidHienTai();
    }

    public void tai() {
        hienThi.setValue(Resource.dangTai());
        repo.taiDanhSach(
                ds -> {
                    tatCa.clear();
                    tatCa.addAll(ds);
                    apDungBoLoc();
                },
                loi -> {
                    tatCa.clear();
                    hienThi.setValue(Resource.loi(loi));
                });
    }

    public void datTuKhoa(String tuKhoa) {
        this.tuKhoa = tuKhoa;
        apDungBoLoc();
    }

    /** @param vaiTro {@link VaiTro#ADMIN}, {@link VaiTro#USER}, hoặc null cho tất cả. */
    public void datVaiTroLoc(String vaiTro) {
        this.vaiTroLoc = vaiTro;
        apDungBoLoc();
    }

    private void apDungBoLoc() {
        List<HoSoNguoiDung> ketQua = new ArrayList<>();
        for (HoSoNguoiDung u : tatCa) {
            if (vaiTroLoc != null && !vaiTroLoc.equals(u.vaiTro)) {
                continue;
            }
            if (!tuKhoa.isEmpty() && !u.khop(tuKhoa)) {
                continue;
            }
            ketQua.add(u);
        }
        // Luôn tạo danh sách MỚI: ListAdapter giữ tham chiếu danh sách đã submit,
        // nên không được submit lại chính đối tượng cũ đã bị sửa nội dung.
        hienThi.setValue(Resource.thanhCong(ketQua));
    }

    /**
     * Đổi vai trò rồi tải lại danh sách, để thứ tự sắp xếp khớp với dữ liệu máy chủ.
     *
     * @param khiXong nhận vai trò mới để View báo cho người dùng
     */
    public void doiVaiTro(HoSoNguoiDung hoSo,
                          com.example.oto.data.Callback<String> khiXong,
                          com.example.oto.data.Callback<String> khiLoi) {
        repo.doiVaiTro(hoSo,
                vaiTroMoi -> {
                    khiXong.onResult(vaiTroMoi);
                    tai();
                },
                khiLoi);
    }
}
