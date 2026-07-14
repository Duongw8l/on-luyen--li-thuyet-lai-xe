package com.example.oto.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;

import androidx.exifinterface.media.ExifInterface;

import java.io.File;
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
        try {
            Bitmap goc = docBitmap(context, nguon);
            if (goc == null) {
                return null;
            }
            Bitmap nhoLai = thuNho(goc);
            Bitmap dungHuong = xoayTheoExif(context, nguon, nhoLai);

            File dich = fileAnhDaiDien(context);
            try (FileOutputStream out = new FileOutputStream(dich)) {
                dungHuong.compress(Bitmap.CompressFormat.JPEG, CHAT_LUONG, out);
            }
            return dich.getAbsolutePath();
        } catch (IOException | SecurityException e) {
            return null;
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
