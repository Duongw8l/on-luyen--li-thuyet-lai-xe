package com.example.oto.data;

import com.example.oto.data.entity.Answer;
import com.example.oto.data.entity.Chapter;
import com.example.oto.data.entity.ExamSet;
import com.example.oto.data.entity.ExamSetQuestion;
import com.example.oto.data.entity.Question;
import com.example.oto.data.entity.TrafficSign;
import com.example.oto.data.entity.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Đổ dữ liệu mẫu vào database lần đầu tạo.
 *
 * LƯU Ý cho cả nhóm: đây mới là DỮ LIỆU MẪU để app chạy được và demo kiến trúc.
 * Bộ 600 câu hỏi chính thức của Cục CSGT sẽ được nạp qua script seed từ file JSON
 * (việc của Trường/Hậu). Khi có JSON đầy đủ, thay phần seed câu hỏi bên dưới.
 */
public final class DatabaseSeeder {

    private DatabaseSeeder() {
    }

    /** Tài khoản người dùng cục bộ mặc định cho bản offline (chưa gắn Firebase). */
    public static final String LOCAL_USER_ID = "local";

    public static void seed(AppDatabase db) {
        seedChapters(db);
        seedUser(db);
        seedQuestions(db);
        seedTrafficSigns(db);
        seedExamSets(db);
    }

    private static void seedChapters(AppDatabase db) {
        List<Chapter> chapters = Arrays.asList(
                new Chapter(1, "Quy định chung và quy tắc giao thông đường bộ", 1),
                new Chapter(2, "Văn hóa, đạo đức người lái xe, PCCC và cứu hộ", 2),
                new Chapter(3, "Kỹ thuật lái xe", 3),
                new Chapter(4, "Cấu tạo và sửa chữa", 4),
                new Chapter(5, "Báo hiệu đường bộ", 5),
                new Chapter(6, "Giải thế sa hình và xử lý tình huống", 6)
        );
        db.chapterDao().insertAll(chapters);
    }

    private static void seedUser(AppDatabase db) {
        User u = new User(LOCAL_USER_ID, "Học viên demo", "hocvien@demo.local", "user");
        db.userDao().insert(u);
    }

    private static void seedQuestions(AppDatabase db) {
        // Chương 1 — quy tắc chung
        add(db, 1, false,
                "Khái niệm \"đường bộ\" được hiểu như thế nào là đúng?",
                new String[]{
                        "Đường bộ gồm đường, cầu đường bộ, hầm đường bộ, bến phà đường bộ.",
                        "Đường bộ chỉ gồm mặt đường và lề đường.",
                        "Đường bộ chỉ gồm phần đường xe chạy."
                }, 0,
                "Theo Luật, đường bộ gồm đường, cầu, hầm và bến phà đường bộ.");

        add(db, 1, true,
                "Khi điều khiển xe trên đường mà trong máu có nồng độ cồn thì bị xử lý thế nào?",
                new String[]{
                        "Không được phép điều khiển xe — đây là hành vi bị nghiêm cấm.",
                        "Được phép nếu nồng độ cồn thấp.",
                        "Được phép vào ban ngày."
                }, 0,
                "CÂU ĐIỂM LIỆT: điều khiển xe khi trong máu/hơi thở có cồn là hành vi bị nghiêm cấm.");

        add(db, 1, false,
                "Trên đường một chiều có vạch kẻ phân làn, xe thô sơ phải đi ở làn nào?",
                new String[]{
                        "Làn sát bên phải ngoài cùng.",
                        "Làn sát bên trái ngoài cùng.",
                        "Bất kỳ làn nào."
                }, 0,
                "Xe thô sơ đi làn bên phải trong cùng, xe cơ giới đi làn bên trái.");

        // Chương 2 — văn hóa, đạo đức
        add(db, 2, false,
                "Người lái xe cần có đạo đức nghề nghiệp như thế nào?",
                new String[]{
                        "Có ý thức tổ chức kỷ luật, tận tâm, thượng tôn pháp luật.",
                        "Chỉ cần lái nhanh để tiết kiệm thời gian.",
                        "Chỉ cần thuộc luật là đủ."
                }, 0,
                "Đạo đức người lái xe gắn với ý thức kỷ luật và thượng tôn pháp luật.");

        // Chương 5 — báo hiệu đường bộ
        add(db, 5, false,
                "Biển báo hình tròn, viền đỏ, nền trắng thuộc nhóm biển nào?",
                new String[]{
                        "Biển báo cấm.",
                        "Biển báo nguy hiểm.",
                        "Biển chỉ dẫn."
                }, 0,
                "Biển tròn viền đỏ nền trắng là nhóm biển báo cấm.");

        add(db, 5, true,
                "Gặp biển \"Cấm đi ngược chiều\" (nền đỏ, gạch trắng ngang) người lái phải làm gì?",
                new String[]{
                        "Không được đi vào theo chiều đặt biển.",
                        "Được phép đi nếu đường vắng.",
                        "Được phép đi chậm."
                }, 0,
                "CÂU ĐIỂM LIỆT: đi ngược chiều là tình huống mất an toàn nghiêm trọng.");

        // Chương 6 — sa hình
        add(db, 6, true,
                "Tại nơi giao nhau, khi có xe ưu tiên (cứu hỏa, cứu thương đang làm nhiệm vụ) thì phải?",
                new String[]{
                        "Nhường đường cho xe ưu tiên đi trước.",
                        "Đi trước nếu mình tới ngã tư trước.",
                        "Bấm còi yêu cầu xe ưu tiên nhường."
                }, 0,
                "CÂU ĐIỂM LIỆT: phải nhường đường cho xe ưu tiên đang làm nhiệm vụ.");

        add(db, 6, false,
                "Thứ tự các xe qua ngã tư không có tín hiệu đèn được xác định theo?",
                new String[]{
                        "Xe ưu tiên → đường ưu tiên → bên phải trống → rẽ phải, đi thẳng, rẽ trái.",
                        "Xe nào to hơn đi trước.",
                        "Xe nào bấm còi trước đi trước."
                }, 0,
                "Nguyên tắc: xe ưu tiên, rồi đường ưu tiên, rồi nhường bên phải.");
    }

    /** Thêm một câu hỏi + các đáp án; đáp án đúng nằm ở vị trí correctIndex. */
    private static void add(AppDatabase db, int chapterId, boolean diemLiet,
                            String noiDung, String[] dapAn, int correctIndex, String giaiThich) {
        Question q = new Question(chapterId, noiDung, diemLiet, giaiThich);
        List<Answer> answers = new ArrayList<>();
        for (int i = 0; i < dapAn.length; i++) {
            answers.add(new Answer(0, dapAn[i], i == correctIndex));
        }
        db.questionDao().insertQuestionWithAnswers(q, answers);
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
