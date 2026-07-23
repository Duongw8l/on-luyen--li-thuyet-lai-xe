package com.example.oto.data;

import android.content.Context;
import android.util.Log;

import com.example.oto.data.entity.Answer;
import com.example.oto.data.entity.Chapter;
import com.example.oto.data.entity.ExamSet;
import com.example.oto.data.entity.ExamSetQuestion;
import com.example.oto.data.entity.Question;
import com.example.oto.data.entity.TrafficSign;
import com.example.oto.data.entity.User;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Đổ dữ liệu ban đầu vào database, chạy đúng MỘT lần lúc SQLite tạo file oto.db.
 *
 * Câu hỏi được đọc từ {@code assets/questions.json} chứ không viết thẳng trong code.
 * Nhờ vậy khi có bộ 600 câu chính thức của Cục CSGT, chỉ cần thay file JSON — không
 * phải sửa và biên dịch lại code Java.
 */
public final class DatabaseSeeder {

    private static final String TAG = "DatabaseSeeder";
    private static final String FILE_CAU_HOI = "questions.json";

    private DatabaseSeeder() {
    }

    /** Tài khoản người dùng cục bộ mặc định cho bản offline (chưa gắn Firebase). */
    public static final String LOCAL_USER_ID = "local";

    /** Thứ tự gọi tôn trọng khoá ngoại: chương trước câu hỏi, bộ đề sau cùng. */
    public static void seed(AppDatabase db, Context context) {
        seedChapters(db);
        seedUser(db);
        seedQuestions(db, context);
        seedTrafficSigns(db);
        seedExamSets(db);
    }

    private static void seedChapters(AppDatabase db) {
        // Tên chương theo đúng cấu trúc bộ 600 câu chính thức (khớp các file docx của nhóm).
        List<Chapter> chapters = Arrays.asList(
                new Chapter(1, "Quy định chung và quy tắc giao thông đường bộ", 1),
                new Chapter(2, "Nghiệp vụ vận tải", 2),
                new Chapter(3, "Văn hóa giao thông và đạo đức người lái xe", 3),
                new Chapter(4, "Kỹ thuật lái xe", 4),
                new Chapter(5, "Cấu tạo và sửa chữa", 5),
                new Chapter(6, "Báo hiệu đường bộ", 6),
                new Chapter(7, "Giải thế sa hình và kỹ năng xử lý tình huống", 7)
        );
        db.chapterDao().insertAll(chapters);
    }

    private static void seedUser(AppDatabase db) {
        User u = new User(LOCAL_USER_ID, "Học viên demo", "hocvien@demo.local", "user");
        db.userDao().insert(u);
    }

    /**
     * Đọc assets/questions.json rồi chèn từng câu hỏi kèm đáp án vào Room.
     *
     * Mỗi câu được ghi bằng một giao dịch của DAO (câu hỏi + đáp án cùng lúc),
     * nên không bao giờ tồn tại câu hỏi thiếu đáp án trong database.
     */
    private static void seedQuestions(AppDatabase db, Context context) {
        String json = docAsset(context, FILE_CAU_HOI);
        if (json == null) {
            Log.e(TAG, "Khong doc duoc " + FILE_CAU_HOI + " — bo qua seed cau hoi.");
            return;
        }
        try {
            JSONArray mang = new JSONObject(json).getJSONArray("cau_hoi");
            for (int i = 0; i < mang.length(); i++) {
                themCauHoi(db, mang.getJSONObject(i));
            }
            Log.i(TAG, "Da nap " + mang.length() + " cau hoi tu " + FILE_CAU_HOI);
        } catch (Exception e) {
            // JSON hỏng thì bỏ qua phần câu hỏi, các bảng khác vẫn seed bình thường —
            // app mở được và màn Quản trị vẫn cho thêm câu hỏi bằng tay.
            Log.e(TAG, "File " + FILE_CAU_HOI + " sai dinh dang: " + e.getMessage());
        }
    }

    /**
     * Chuyển một phần tử JSON thành Question + danh sách Answer rồi ghi xuống Room.
     * Ném JSONException lên hàm gọi để một câu sai định dạng làm dừng cả mẻ seed,
     * thay vì âm thầm chèn nửa vời.
     */
    private static void themCauHoi(AppDatabase db, JSONObject o) throws org.json.JSONException {
        Question q = new Question(
                o.getInt("chuong"),
                o.getString("noi_dung"),
                o.optBoolean("diem_liet", false),
                o.optString("giai_thich", ""));
        // Ảnh minh hoạ (tuỳ chọn): tên file trong assets/, VD "images/p101.png".
        // Rỗng thì để null — câu không có ảnh.
        String anh = o.optString("anh", "");
        q.anhUrl = anh.isEmpty() ? null : anh;

        JSONArray ds = o.getJSONArray("dap_an");
        int viTriDung = o.getInt("dap_an_dung");

        List<Answer> answers = new ArrayList<>();
        for (int i = 0; i < ds.length(); i++) {
            answers.add(new Answer(0, ds.getString(i), i == viTriDung));
        }
        db.questionDao().insertQuestionWithAnswers(q, answers);
    }

    /** Đọc trọn một file trong assets/ thành chuỗi UTF-8. */
    private static String docAsset(Context context, String tenFile) {
        try (InputStream in = context.getAssets().open(tenFile)) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            int n;
            while ((n = in.read(buf)) != -1) {
                out.write(buf, 0, n);
            }
            return new String(out.toByteArray(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            Log.e(TAG, "Loi doc asset " + tenFile + ": " + e.getMessage());
            return null;
        }
    }

    private static void seedTrafficSigns(AppDatabase db) {
        List<TrafficSign> signs = Arrays.asList(
                new TrafficSign("P.101", "Đường cấm", "Cấm",
                        "Cấm tất cả các loại phương tiện đi lại cả hai hướng."),
                new TrafficSign("P.102", "Cấm đi ngược chiều", "Cấm",
                        "Cấm các loại xe đi vào theo chiều đặt biển."),
                new TrafficSign("W.201a", "Chỗ ngoặt nguy hiểm vòng bên trái", "Nguy hiểm",
                        "Báo trước sắp đến chỗ đường ngoặt nguy hiểm."),
                new TrafficSign("R.301a", "Hướng đi phải theo — đi thẳng", "Hiệu lệnh",
                        "Các xe chỉ được đi thẳng."),
                new TrafficSign("I.401", "Bắt đầu đường ưu tiên", "Chỉ dẫn",
                        "Báo hiệu bắt đầu đoạn đường ưu tiên.")
        );
        db.trafficSignDao().insertAll(signs);
    }

    private static void seedExamSets(AppDatabase db) {
        // Một bộ đề cố định mẫu. Thông số (số câu, thời gian, ngưỡng đạt) cần
        // xác minh lại từ nguồn chính thức của Cục CSGT trước khi chốt.
        List<Integer> allQ = new ArrayList<>();
        // Lấy toàn bộ id câu hỏi hiện có làm đề mẫu
        for (Question q : selectAllQuestionIds(db)) {
            allQ.add(q.id);
        }
        if (allQ.isEmpty()) {
            return;
        }
        ExamSet de1 = new ExamSet("Đề số 1", allQ.size(), ExamConfig.THOI_GIAN_PHUT, ExamConfig.NGUONG_DAT);
        long id = db.examSetDao().insert(de1);
        List<ExamSetQuestion> links = new ArrayList<>();
        for (Integer qid : allQ) {
            links.add(new ExamSetQuestion((int) id, qid));
        }
        db.examSetDao().insertLinks(links);
    }

    private static List<Question> selectAllQuestionIds(AppDatabase db) {
        // Dùng truy vấn ngẫu nhiên lấy toàn bộ (số lượng mẫu nhỏ)
        List<Question> result = new ArrayList<>();
        for (com.example.oto.data.relation.QuestionWithAnswers qa : db.questionDao().getRandom(1000)) {
            result.add(qa.question);
        }
        return result;
    }
}
