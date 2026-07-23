package com.example.oto.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.oto.data.entity.Answer;
import com.example.oto.data.entity.Question;
import com.example.oto.util.AnhUtil;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;

/**
 * Đồng bộ ngân hàng câu hỏi giữa Room (dưới máy) và Firestore (đám mây).
 *
 * VÌ SAO CÓ LỚP NÀY (mục 3 CLAUDE.md — cơ chế đồng bộ câu hỏi admin → máy khách):
 * 600 câu gốc được đóng sẵn trong APK và seed thẳng vào Room, KHÔNG bao giờ đẩy lên
 * Firestore. Firestore chỉ chứa phần "delta" — những câu admin THÊM hoặc SỬA sau này.
 * Máy khách tải delta về bằng cách hỏi các câu có {@code updatedAt} mới hơn lần đồng bộ
 * cuối ({@code whereGreaterThan}) rồi upsert vào Room. TUYỆT ĐỐI không tải/đẩy lại cả
 * 600 câu mỗi lần.
 *
 * Đây là lớp Firestore (giống {@link NguoiDungRepository}), tách khỏi {@link QuizRepository}
 * để {@code QuizRepository} vẫn thuần Room. Việc admin sửa ngân hàng câu hỏi là thao tác
 * CÓ MẠNG — ngoại lệ có chủ đích với offline-first, vì nó không thuộc luồng ôn/thi.
 *
 * GIỚI HẠN đã biết (chấp nhận trong phạm vi đồ án, giả định MỘT admin thao tác khi online):
 *  - id câu mới do Room tự tăng; nếu hai máy admin cùng thêm câu mới có thể trùng id.
 *  - Xoá một câu GỐC (id ≤ 600) không lan sang máy khác (không dùng tombstone).
 */
public class QuestionSyncRepository {

    private static final String TAG = "QuestionSync";

    // Firestore: collection "questions", mỗi document đặt tên theo id câu hỏi trong Room.
    // Việc chuyển đổi qua lại giữa entity và document nằm ở QuestionFirestoreMapper
    // (tách riêng để unit test được).
    private static final String COLLECTION = "questions";

    // Hai mốc thời gian lưu trong SharedPreferences (đây là "lastSyncTime" mục 3 CLAUDE.md).
    private static final String PREF = "dong_bo_cau_hoi";
    /** Mốc ĐẨY LÊN: câu có updated_at > mốc này là thay đổi cục bộ chưa gửi lên. */
    private static final String KEY_MOC_DAY_LEN = "moc_day_len";
    /** Mốc KÉO VỀ: chỉ hỏi Firestore những câu có updatedAt > mốc này. */
    private static final String KEY_MOC_KEO_VE = "moc_keo_ve";

    /** Kết quả một lần đồng bộ, để màn hình hiển thị cho người dùng. */
    public static class KetQua {
        public int soCauKeoVe;   // số câu tải từ máy chủ về
        public int soCauDayLen;  // số câu đẩy từ máy lên
    }

    private final AppDatabase db;
    private final SharedPreferences prefs;
    /** Application context — dùng để đọc/ghi file ảnh câu hỏi. Không gây rò rỉ bộ nhớ. */
    private final Context appContext;
    private final Handler main = new Handler(Looper.getMainLooper());

    public QuestionSyncRepository(Context context) {
        this.appContext = context.getApplicationContext();
        this.db = AppDatabase.get(context);
        this.prefs = appContext.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        chotMocGocNeuChua();
    }

    /**
     * Lần đầu bật đồng bộ: coi TOÀN BỘ câu đang có trong Room là dữ liệu gốc đóng sẵn
     * trong APK, nên đặt mốc đẩy-lên = thời điểm hiện tại để KHÔNG đẩy 600 câu gốc lên
     * Firestore. Chỉ những câu admin sửa/thêm SAU mốc này mới được đẩy.
     */
    private void chotMocGocNeuChua() {
        if (!prefs.contains(KEY_MOC_DAY_LEN)) {
            prefs.edit().putLong(KEY_MOC_DAY_LEN, System.currentTimeMillis()).apply();
        }
    }

    // ===================== ĐẨY LÊN (admin sửa xong gọi ngay) =====================

    /**
     * Đẩy MỘT câu hỏi (kèm đáp án) lên Firestore, gọi ngay sau khi admin lưu vào Room.
     * Ghi đè trọn document theo id — set() không merge nên đáp án cũ không sót lại.
     *
     * @param onDone nhận true nếu đẩy thành công; false nếu lỗi (mất mạng/không đủ quyền).
     *               Màn hình dùng để báo "đã lưu máy nhưng chưa đồng bộ, thử lại sau".
     */
    public void dayCauHoiLen(Question q, List<Answer> answers, Callback<Boolean> onDone) {
        FirebaseFirestore.getInstance()
                .collection(COLLECTION).document(String.valueOf(q.id))
                .set(docChoCauHoi(q, answers))
                .addOnSuccessListener(v -> {
                    // Nâng mốc đẩy-lên để lần đồng bộ hàng loạt sau không gửi lại câu này.
                    napMocDayLen(q.updatedAt);
                    onDone.onResult(true);
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Day cau hoi " + q.id + " that bai: " + e.getMessage());
                    onDone.onResult(false);
                });
    }

    /** Xoá câu hỏi trên Firestore khi admin xoá dưới máy (no-op nếu câu chưa từng lên mây). */
    public void xoaCauHoiTrenMayChu(int questionId, Callback<Boolean> onDone) {
        FirebaseFirestore.getInstance()
                .collection(COLLECTION).document(String.valueOf(questionId))
                .delete()
                .addOnSuccessListener(v -> onDone.onResult(true))
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Xoa cau hoi " + questionId + " tren may chu that bai: " + e.getMessage());
                    onDone.onResult(false);
                });
    }

    // ===================== ĐỒNG BỘ HAI CHIỀU (nút "Đồng bộ ngay") =====================

    /**
     * Đẩy các thay đổi cục bộ còn tồn lên, rồi kéo delta từ máy chủ về.
     * Dùng cho nút "Đồng bộ ngay" của màn Quản trị.
     */
    public void dongBoTatCa(Callback<KetQua> onDone, Callback<String> onError) {
        // Bước đọc Room phải ở luồng nền.
        AppDatabase.IO.execute(() -> {
            long mocDayLen = prefs.getLong(KEY_MOC_DAY_LEN, 0);
            List<Question> canDay = db.questionDao().getSuaSau(mocDayLen);
            // Gom sẵn đáp án của từng câu (vẫn ở luồng nền).
            List<List<Answer>> dapAnMoiCau = new ArrayList<>();
            for (Question q : canDay) {
                dapAnMoiCau.add(db.questionDao().getAnswersOf(q.id));
            }
            main.post(() -> dayHangLoat(canDay, dapAnMoiCau,
                    soDay -> keoVe(
                            kq -> {
                                kq.soCauDayLen = soDay;
                                onDone.onResult(kq);
                            },
                            onError),
                    onError));
        });
    }

    /** Đẩy một loạt câu bằng WriteBatch (một lần ghi nguyên tử). */
    private void dayHangLoat(List<Question> cauHoi, List<List<Answer>> dapAn,
                             Callback<Integer> onDone, Callback<String> onError) {
        if (cauHoi.isEmpty()) {
            onDone.onResult(0);
            return;
        }
        FirebaseFirestore fs = FirebaseFirestore.getInstance();
        WriteBatch batch = fs.batch();
        long mocMax = prefs.getLong(KEY_MOC_DAY_LEN, 0);
        for (int i = 0; i < cauHoi.size(); i++) {
            Question q = cauHoi.get(i);
            batch.set(fs.collection(COLLECTION).document(String.valueOf(q.id)),
                    docChoCauHoi(q, dapAn.get(i)));
            mocMax = Math.max(mocMax, q.updatedAt);
        }
        long mocMoi = mocMax;
        batch.commit()
                .addOnSuccessListener(v -> {
                    napMocDayLen(mocMoi);
                    onDone.onResult(cauHoi.size());
                })
                .addOnFailureListener(e -> onError.onResult(
                        "Không đẩy được thay đổi lên máy chủ: " + e.getMessage()));
    }

    // ===================== KÉO VỀ (Firestore → Room) =====================

    /**
     * Tải các câu có {@code updatedAt} mới hơn lần đồng bộ cuối rồi upsert vào Room.
     * Đây là phần "tải delta" mô tả ở mục 3 CLAUDE.md — nền tảng để mọi máy khách nhận
     * được câu hỏi do admin thêm/sửa mà không phải tải lại cả bộ đề.
     */
    public void keoVe(Callback<KetQua> onDone, Callback<String> onError) {
        long mocKeoVe = prefs.getLong(KEY_MOC_KEO_VE, 0);
        FirebaseFirestore.getInstance()
                .collection(COLLECTION)
                .whereGreaterThan(QuestionFirestoreMapper.F_UPDATED_AT, mocKeoVe)
                .orderBy(QuestionFirestoreMapper.F_UPDATED_AT, Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(snap -> {
                    List<Question> cauHoi = new ArrayList<>();
                    List<List<Answer>> dapAn = new ArrayList<>();
                    List<String> anhData = new ArrayList<>();
                    long mocMoi = mocKeoVe;
                    for (DocumentSnapshot doc : snap.getDocuments()) {
                        Question q = QuestionFirestoreMapper.toQuestion(doc.getId(), doc.getData());
                        if (q == null) {
                            continue;
                        }
                        cauHoi.add(q);
                        dapAn.add(QuestionFirestoreMapper.toAnswers(doc.getData(), q.id));
                        // Ảnh (nếu có) đến dạng Base64 — sẽ giải mã ra file ở luồng nền.
                        anhData.add(doc.getString(QuestionFirestoreMapper.F_ANH_DATA));
                        mocMoi = Math.max(mocMoi, q.updatedAt);
                    }
                    ghiVaoRoom(cauHoi, dapAn, anhData, mocMoi, onDone);
                })
                .addOnFailureListener(e -> onError.onResult(
                        "Không tải được câu hỏi mới từ máy chủ: " + e.getMessage()));
    }

    /** Ghi các câu tải về vào Room ở luồng nền, rồi báo kết quả về luồng UI. */
    private void ghiVaoRoom(List<Question> cauHoi, List<List<Answer>> dapAn,
                            List<String> anhData, long mocMoi, Callback<KetQua> onDone) {
        AppDatabase.IO.execute(() -> {
            for (int i = 0; i < cauHoi.size(); i++) {
                Question q = cauHoi.get(i);
                // Câu có ảnh nhúng Base64 -> giải mã ra file cục bộ, gán đường dẫn vào anhUrl.
                String data = anhData.get(i);
                if (data != null && !data.isEmpty()) {
                    String duongDan = AnhUtil.ghiAnhCauHoiBase64(appContext, data);
                    if (duongDan != null) {
                        q.anhUrl = duongDan;
                    }
                }
                db.questionDao().upsertTuFirestore(q, dapAn.get(i));
            }
            // Nâng mốc kéo-về để lần sau không tải lại các câu này.
            prefs.edit().putLong(KEY_MOC_KEO_VE, mocMoi).apply();
            // Đồng thời nâng mốc đẩy-lên qua các câu vừa kéo về, để chúng KHÔNG bị coi là
            // thay đổi cục bộ rồi đẩy ngược lên (tránh vòng lặp đẩy–kéo giữa các máy).
            napMocDayLen(mocMoi);

            KetQua kq = new KetQua();
            kq.soCauKeoVe = cauHoi.size();
            main.post(() -> onDone.onResult(kq));
        });
    }

    /**
     * Dựng document Firestore cho một câu hỏi, xử lý ảnh:
     *  - anhUrl là đường dẫn file cục bộ ('/...') = ảnh admin tự thêm → NHÚNG bytes dạng
     *    Base64 vào field anhData, và xoá anhUrl (đường dẫn cục bộ vô nghĩa với máy khác).
     *  - anhUrl là tên asset (không bắt đầu bằng '/') = ảnh của bộ câu gốc → giữ nguyên
     *    anhUrl (mọi máy đều có asset đó trong APK), không nhúng gì.
     *  - không có ảnh → không thêm gì.
     */
    private java.util.Map<String, Object> docChoCauHoi(Question q, List<Answer> answers) {
        java.util.Map<String, Object> doc = QuestionFirestoreMapper.toDocument(q, answers);
        if (q.anhUrl != null && q.anhUrl.startsWith("/")) {
            String b64 = AnhUtil.docFileBase64(q.anhUrl);
            if (b64 != null) {
                doc.put(QuestionFirestoreMapper.F_ANH_DATA, b64);
                doc.put(QuestionFirestoreMapper.F_ANH_URL, null);
            }
        }
        return doc;
    }

    /** Nâng mốc đẩy-lên (chỉ tăng, không giảm) một cách an toàn cho đa luồng. */
    private synchronized void napMocDayLen(long moc) {
        long hienTai = prefs.getLong(KEY_MOC_DAY_LEN, 0);
        if (moc > hienTai) {
            prefs.edit().putLong(KEY_MOC_DAY_LEN, moc).apply();
        }
    }
}
