package com.example.oto.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.util.Base64;

import androidx.exifinterface.media.ExifInterface;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Xử lý ảnh đại diện (tiêu chí 2.2).
 *
 * Ảnh chụp từ camera thường nặng vài MB. Trước khi lưu, ảnh được thu nhỏ về
 * tối đa 512px và nén JPEG chất lượng 80 — vừa tiết kiệm dung lượng, vừa tránh
 * lỗi OutOfMemory khi hiển thị.
 */
public final class AnhUtil {

    /** Cạnh dài nhất của ảnh sau khi nén. */
    private static final int CANH_TOI_DA = 512;
    private static final int CHAT_LUONG = 80;

    /** Tên file ảnh đại diện trong bộ nhớ riêng của app. */
    private static final String TEN_FILE = "anh_dai_dien.jpg";

    /** Thư mục con chứa ảnh các biển báo trong bộ nhớ riêng của app. */
    private static final String THU_MUC_BIEN = "bien_bao";

    /** Thư mục con chứa ảnh minh hoạ câu hỏi (câu do admin thêm, kéo từ Firestore về). */
    private static final String THU_MUC_CAU = "cau_hoi";

    private AnhUtil() {
    }

    /** File ảnh đại diện (có thể chưa tồn tại). */
    public static File fileAnhDaiDien(Context context) {
        return new File(context.getFilesDir(), TEN_FILE);
    }

    /** File tạm để camera ghi ảnh gốc vào. */
    public static File fileAnhTam(Context context) {
        return new File(context.getCacheDir(), "anh_tam.jpg");
    }

    /**
     * Đọc ảnh từ Uri (thư viện hoặc file camera), thu nhỏ + nén, ghi đè lên file
     * ảnh đại diện. Trả về đường dẫn file đã lưu, hoặc null nếu lỗi.
     */
    public static String luuAnhDaiDien(Context context, Uri nguon) {
        File dich = fileAnhDaiDien(context);
        return luuNenVaoFile(context, nguon, dich) ? dich.getAbsolutePath() : null;
    }

    /**
     * Lưu ảnh cho một biển báo: thu nhỏ + nén rồi ghi vào một file mới trong thư mục
     * riêng của app. Mỗi lần đổi ảnh tạo một file tên riêng (theo mốc thời gian) nên
     * ảnh của biển này không đè lên ảnh của biển khác. Trả về đường dẫn tuyệt đối
     * của file đã lưu, hoặc null nếu lỗi.
     */
    public static String luuAnhBienBao(Context context, Uri nguon) {
        File thuMuc = new File(context.getFilesDir(), THU_MUC_BIEN);
        if (!thuMuc.exists() && !thuMuc.mkdirs()) {
            return null;
        }
        File dich = new File(thuMuc, "bien_" + System.currentTimeMillis() + ".jpg");
        return luuNenVaoFile(context, nguon, dich) ? dich.getAbsolutePath() : null;
    }

    /**
     * Lưu ảnh minh hoạ cho một câu hỏi (admin chọn từ thư viện/camera): thu nhỏ + nén
     * rồi ghi vào một file mới trong thư mục riêng của app. Trả về đường dẫn tuyệt đối
     * (bắt đầu bằng '/'), hoặc null nếu lỗi.
     */
    public static String luuAnhCauHoi(Context context, Uri nguon) {
        File thuMuc = new File(context.getFilesDir(), THU_MUC_CAU);
        if (!thuMuc.exists() && !thuMuc.mkdirs()) {
            return null;
        }
        File dich = new File(thuMuc, "cau_" + System.currentTimeMillis() + ".jpg");
        return luuNenVaoFile(context, nguon, dich) ? dich.getAbsolutePath() : null;
    }

    /**
     * Đọc một file ảnh cục bộ rồi mã hoá Base64 để NHÚNG vào document Firestore
     * (cơ chế đồng bộ ảnh câu hỏi mà không dùng Firebase Storage). Ảnh đã được nén
     * sẵn khi lưu nên chuỗi Base64 đủ nhỏ để nằm gọn trong giới hạn 1 MiB của Firestore.
     * Trả về null nếu đường dẫn rỗng, không phải file, hoặc đọc lỗi.
     */
    public static String docFileBase64(String duongDan) {
        if (duongDan == null || duongDan.isEmpty()) {
            return null;
        }
        File f = new File(duongDan);
        if (!f.exists()) {
            return null;
        }
        try (FileInputStream in = new FileInputStream(f)) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            int n;
            while ((n = in.read(buf)) != -1) {
                out.write(buf, 0, n);
            }
            return Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP);
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Giải mã chuỗi Base64 (ảnh câu hỏi kéo từ Firestore về) rồi ghi ra một file cục bộ
     * mới. Trả về đường dẫn tuyệt đối để gán vào {@code Question.anhUrl}, hoặc null nếu
     * chuỗi hỏng / ghi lỗi.
     */
    public static String ghiAnhCauHoiBase64(Context context, String base64) {
        if (base64 == null || base64.isEmpty()) {
            return null;
        }
        try {
            byte[] bytes = Base64.decode(base64, Base64.NO_WRAP);
            File thuMuc = new File(context.getFilesDir(), THU_MUC_CAU);
            if (!thuMuc.exists() && !thuMuc.mkdirs()) {
                return null;
            }
            File dich = new File(thuMuc, "cau_" + System.currentTimeMillis() + ".jpg");
            try (FileOutputStream out = new FileOutputStream(dich)) {
                out.write(bytes);
            }
            return dich.getAbsolutePath();
        } catch (IllegalArgumentException | IOException e) {
            return null;
        }
    }

    /**
     * Đọc ảnh minh hoạ của một câu hỏi để hiển thị.
     *
     * {@code anhUrl} có thể là hai dạng:
     *  - Đường dẫn file cục bộ (bắt đầu bằng '/'): ảnh của câu do admin thêm/đồng bộ.
     *  - Tên file trong assets/ (không bắt đầu bằng '/'): ảnh của bộ câu hỏi gốc đóng
     *    sẵn trong APK, VD "images/p101.png".
     * Trả về null nếu rỗng hoặc không đọc được (khi đó màn hình ẩn ImageView đi).
     */
    public static Bitmap docAnhCauHoi(Context context, String anhUrl) {
        if (anhUrl == null || anhUrl.isEmpty()) {
            return null;
        }
        if (anhUrl.startsWith("/")) {
            return docAnh(anhUrl); // file cục bộ
        }
        // Ảnh gói sẵn trong assets/
        try (InputStream in = context.getAssets().open(anhUrl)) {
            return BitmapFactory.decodeStream(in);
        } catch (IOException e) {
            return null;
        }
    }

    /** Xoá file ảnh đã lưu theo đường dẫn; bỏ qua nếu rỗng hoặc file không tồn tại. */
    public static void xoaAnh(String duongDan) {
        if (duongDan == null || duongDan.isEmpty()) {
            return;
        }
        File f = new File(duongDan);
        if (f.exists()) {
            f.delete();
        }
    }

    /** Đọc ảnh đã lưu theo đường dẫn tuyệt đối; null nếu đường dẫn rỗng hoặc file không còn. */
    public static Bitmap docAnh(String duongDan) {
        if (duongDan == null || duongDan.isEmpty()) {
            return null;
        }
        File f = new File(duongDan);
        if (!f.exists()) {
            return null;
        }
        return BitmapFactory.decodeFile(f.getAbsolutePath());
    }

    /**
     * Đọc ảnh từ Uri (thư viện hoặc file camera), thu nhỏ + nén rồi ghi đè vào file
     * đích. Trả về true nếu lưu thành công.
     */
    private static boolean luuNenVaoFile(Context context, Uri nguon, File dich) {
        try {
            Bitmap goc = docBitmap(context, nguon);
            if (goc == null) {
                return false;
            }
            Bitmap nhoLai = thuNho(goc);
            Bitmap dungHuong = xoayTheoExif(context, nguon, nhoLai);
            try (FileOutputStream out = new FileOutputStream(dich)) {
                dungHuong.compress(Bitmap.CompressFormat.JPEG, CHAT_LUONG, out);
            }
            return true;
        } catch (IOException | SecurityException e) {
            return false;
        }
    }

    private static Bitmap docBitmap(Context context, Uri uri) throws IOException {
        try (InputStream in = context.getContentResolver().openInputStream(uri)) {
            return BitmapFactory.decodeStream(in);
        }
    }

    private static Bitmap thuNho(Bitmap goc) {
        int w = goc.getWidth();
        int h = goc.getHeight();
        int canhDai = Math.max(w, h);
        if (canhDai <= CANH_TOI_DA) {
            return goc;
        }
        float tyLe = (float) CANH_TOI_DA / canhDai;
        return Bitmap.createScaledBitmap(goc, Math.round(w * tyLe), Math.round(h * tyLe), true);
    }

    /** Ảnh chụp dọc hay bị nằm ngang — đọc thẻ EXIF để xoay lại cho đúng. */
    private static Bitmap xoayTheoExif(Context context, Uri uri, Bitmap bitmap) throws IOException {
        int goc;
        try (InputStream in = context.getContentResolver().openInputStream(uri)) {
            if (in == null) {
                return bitmap;
            }
            ExifInterface exif = new ExifInterface(in);
            int huong = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (huong) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    goc = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    goc = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    goc = 270;
                    break;
                default:
                    return bitmap;
            }
        }
        Matrix m = new Matrix();
        m.postRotate(goc);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
    }

    /** Đọc ảnh đại diện đã lưu để hiển thị; null nếu người dùng chưa đặt ảnh. */
    public static Bitmap docAnhDaiDien(Context context) {
        File f = fileAnhDaiDien(context);
        if (!f.exists()) {
            return null;
        }
        return BitmapFactory.decodeFile(f.getAbsolutePath());
    }
}
