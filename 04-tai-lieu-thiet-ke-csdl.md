# TÀI LIỆU THIẾT KẾ CƠ SỞ DỮ LIỆU

# ỨNG DỤNG ÔN THI LÝ THUYẾT SÁT HẠCH LÁI XE HẠNG B

> Tài liệu này mô tả chi tiết phần cơ sở dữ liệu của ứng dụng: mô hình dữ liệu, đặc tả từng bảng, ràng buộc toàn vẹn, các truy vấn chính và cơ chế khởi tạo dữ liệu.
> Đây là bản chi tiết hoá của mục **3.3** trong `02-tai-lieu-thiet-ke.md`. Nội dung được viết bám sát code thực tế trong `app/src/main/java/com/example/oto/data/`.

---

# I. TỔNG QUAN

## 1.1. Hai nơi lưu dữ liệu

Ứng dụng dùng **hai** kho dữ liệu, với ranh giới rất rõ ràng:

| Kho | Công nghệ | Chứa gì | Vì sao |
|---|---|---|---|
| **Cục bộ** | Room (SQLite), file `oto.db` | 11 bảng: câu hỏi, đáp án, biển báo, bộ đề, lượt thi, câu trả lời, ghi chú, lịch ôn lại, người dùng | Toàn bộ việc học/thi phải chạy được **offline** |
| **Đám mây** | Firebase Auth + Firestore | Tài khoản (email/mật khẩu) và hồ sơ `users/{uid}` gồm `hoTen`, `email`, `vaiTro`, `ngayTao` | Xác thực và **phân quyền admin phải do máy chủ quyết định**, không để client tự nhận |

**Nguyên tắc bất di bất dịch:** khi người dùng đang ôn tập hoặc làm bài thi, ứng dụng **chỉ đọc Room**, không gọi mạng. Nếu mất mạng giữa lúc thi, bài thi vẫn hoàn thành và lưu được bình thường.

## 1.2. Vì sao ảnh không để trên Firebase Storage

Ảnh biển báo và sa hình được đóng gói trong APK (thư mục `assets/`), không tải từ mạng. Lý do: đây là dữ liệu **tĩnh, dùng chung cho mọi người, cần có mặt khi offline**. Trường `anh_url` trong bảng `questions` và `traffic_signs` vì thế lưu **đường dẫn tương đối tới asset hoặc tới file trong bộ nhớ riêng của app**, không phải URL Internet.

Riêng ảnh do admin tự chọn khi sửa biển báo thì được nén và chép vào bộ nhớ riêng của app (`AnhUtil`), rồi lưu đường dẫn file vào `anh_url`.

## 1.3. Khởi tạo Room — `AppDatabase`

```java
@Database(entities = { Chapter, Question, Answer, TrafficSign, ExamSet,
                       ExamSetQuestion, User, Attempt, UserAnswer,
                       ReviewSchedule, Note },
          version = 2, exportSchema = true)
public abstract class AppDatabase extends RoomDatabase { ... }
```

Bốn điểm cần nắm để trả lời khi vấn đáp:

1. **Singleton + double-checked locking.** `AppDatabase.get(context)` chỉ tạo đúng một thể hiện cho cả vòng đời ứng dụng. Biến `INSTANCE` khai báo `volatile`, khối khởi tạo nằm trong `synchronized` — tránh trường hợp hai luồng cùng tạo hai kết nối database.
2. **Executor dùng chung.** `AppDatabase.IO` là một `ExecutorService` 4 luồng. **Mọi** thao tác đọc/ghi Room đều chạy qua nó; kết quả trả về luồng chính bằng `Handler(Looper.getMainLooper())` trong `QuizRepository`, hoặc tự động qua `LiveData`. Không có truy vấn nào chạy trên UI thread.
3. **Seed lần đầu.** `SEED_CALLBACK.onCreate()` chỉ chạy **một lần duy nhất** — đúng lúc SQLite tạo file `oto.db`. Nó gọi `DatabaseSeeder.seed(INSTANCE, appContext)` trên luồng nền để đổ dữ liệu ban đầu (cần Context để đọc file trong `assets/`).
4. **Nâng cấp bằng migration tường minh.** `.addMigrations(MIGRATION_1_2)`, **không** dùng `fallbackToDestructiveMigration`. Chi tiết ở mục IX.

> **Về `appContext`:** `AppDatabase` giữ một tham chiếu tĩnh tới `context.getApplicationContext()`. Dùng application context (không phải Activity) nên không gây rò rỉ bộ nhớ — application sống bằng vòng đời tiến trình, đúng bằng vòng đời của singleton database.

## 1.4. Vị trí của CSDL trong kiến trúc MVVM

Tầng dữ liệu **không bao giờ được Activity gọi trực tiếp**. Luồng bắt buộc:

```
Activity/Fragment  →  ViewModel  →  Repository  →  DAO  →  SQLite
                                        ↘  NguoiDungRepository → Firestore
```

- **`QuizRepository`** — "một cửa" cho toàn bộ dữ liệu học tập trong Room.
- **`NguoiDungRepository`** — riêng cho collection `users` trên Firestore (màn Quản trị người dùng).
- **12 ViewModel** (thư mục `ui/viewmodel/`) đứng giữa, giữ trạng thái màn hình và phát dữ liệu qua `LiveData`.

Nghĩa là khi bảo vệ, câu "database nằm ở đâu trong kiến trúc" trả lời được bằng đúng một dòng: **Room là nguồn đọc chính ở đáy tầng dữ liệu, và chỉ Repository mới được chạm vào nó.**

> **Lưu ý kỹ thuật:** Room bật `PRAGMA foreign_keys = ON` mặc định, nên các ràng buộc khoá ngoại khai báo dưới đây có hiệu lực thật ở tầng SQLite, không chỉ là chú thích.

---

# II. MÔ HÌNH THỰC THỂ — LIÊN KẾT (ERD)

## 2.1. Sơ đồ quan hệ

```
                    ┌────────────┐
                    │  chapters  │  6 chương
                    └─────┬──────┘
                          │ 1
                          │            (RESTRICT: còn câu hỏi thì
                          │             không xoá được chương)
                          │ N
   ┌──────────────┐ N   ┌─┴──────────┐ 1     N ┌───────────┐
   │  exam_sets   ├─────┤ questions  ├─────────┤  answers  │
   └──────┬───────┘  ▲  └─────┬──────┘ CASCADE └───────────┘
          │          │        │
          │   exam_set_questions        (bảng nối N-N)
          │                    │
          │ 1                  │ 1
          │                    ├───────────────┬──────────────┐
          │                    │ N             │ N            │ N
   ┌──────┴──────┐      ┌──────┴──────┐  ┌─────┴─────┐  ┌─────┴──────────┐
   │  attempts   │      │ user_answers│  │   notes   │  │ review_schedule│
   └──────┬──────┘ 1  N └─────────────┘  └─────┬─────┘  └────────┬───────┘
          │ N                                  │ N               │ N
          │              ┌──────────┐          │                 │
          └──────────────┤  users   ├──────────┴─────────────────┘
                     N   └──────────┘   (CASCADE trên mọi nhánh)
                                 1

   ┌────────────────┐
   │ traffic_signs  │   bảng độc lập — tra cứu biển báo
   └────────────────┘
```

## 2.2. Bảng liên kết

| Quan hệ | Kiểu | Thể hiện bằng |
|---|---|---|
| chapters → questions | 1 – N | `questions.chapter_id` |
| questions → answers | 1 – N | `answers.question_id` |
| exam_sets ↔ questions | N – N | Bảng nối `exam_set_questions` |
| users → attempts | 1 – N | `attempts.user_id` |
| attempts → user_answers | 1 – N | `user_answers.attempt_id` |
| questions → user_answers | 1 – N | `user_answers.question_id` |
| users + questions → notes | 1 – 1 theo cặp | `notes(user_id, question_id)` |
| users + questions → review_schedule | 1 – 1 theo cặp | `review_schedule(user_id, question_id)` |

---

# III. ĐẶC TẢ CHI TIẾT TỪNG BẢNG

Ký hiệu: **PK** = khoá chính, **FK** = khoá ngoại, **IDX** = có chỉ mục.

## 3.1. `chapters` — Chương

| Cột | Kiểu SQLite | Ràng buộc | Ý nghĩa |
|---|---|---|---|
| `id` | INTEGER | PK (gán tay 1–6) | Mã chương |
| `ten_chuong` | TEXT | NOT NULL | Tên chương |
| `so_thu_tu` | INTEGER | | Thứ tự hiển thị 1–6 |

Khoá chính **không** dùng `autoGenerate` — 6 chương là dữ liệu cố định theo bộ đề của Cục CSGT, đánh số tay để id ổn định giữa các lần cài lại app.

Class `Chapter` override `toString()` trả về `"Chương 1 — Quy định chung..."` để dùng thẳng trong `Spinner` chọn chương, khỏi cần adapter riêng.

## 3.2. `questions` — Câu hỏi

| Cột | Kiểu | Ràng buộc | Ý nghĩa |
|---|---|---|---|
| `id` | INTEGER | PK, autoGenerate | |
| `chapter_id` | INTEGER | FK → `chapters.id`, **ON DELETE RESTRICT**, IDX | Thuộc chương nào |
| `noi_dung` | TEXT | NOT NULL | Nội dung câu hỏi |
| `anh_url` | TEXT | nullable | Ảnh biển báo / sa hình |
| `is_diem_liet` | INTEGER (0/1) | | **Câu điểm liệt** |
| `giai_thich` | TEXT | nullable | Giải thích đáp án |
| `updated_at` | INTEGER | NOT NULL DEFAULT 0 | Thời điểm sửa gần nhất (epoch millis) |

**Cột `updated_at` phục vụ đồng bộ delta.** Mỗi lần `QuizRepository.saveQuestion()` chạy, cột này được đóng dấu `System.currentTimeMillis()`. Nhờ đó máy khách chỉ cần hỏi những câu có `updated_at` mới hơn lần đồng bộ cuối, thay vì tải lại toàn bộ 600 câu (xem mục 7.7 và 10.1). Giá trị `0` nghĩa là "chưa từng đồng bộ".

**Vì sao `RESTRICT` chứ không `CASCADE`?** Xoá một chương mà cuốn theo hàng trăm câu hỏi là thảm hoạ dữ liệu. `RESTRICT` khiến SQLite từ chối thao tác xoá chương khi vẫn còn câu hỏi tham chiếu — đây là hàng rào an toàn cố ý, ngược hẳn với quan hệ `questions → answers` bên dưới.

**Cột `is_diem_liet` là cột quan trọng nhất của bảng.** Toàn bộ luật chấm điểm đặc thù của kỳ thi sát hạch xoay quanh cột này (xem mục V).

## 3.3. `answers` — Đáp án

| Cột | Kiểu | Ràng buộc | Ý nghĩa |
|---|---|---|---|
| `id` | INTEGER | PK, autoGenerate | |
| `question_id` | INTEGER | FK → `questions.id`, **ON DELETE CASCADE**, IDX | Thuộc câu hỏi nào |
| `noi_dung` | TEXT | NOT NULL | Nội dung đáp án |
| `is_correct` | INTEGER (0/1) | | Có phải đáp án đúng |

Ở đây `CASCADE` là đúng: đáp án **không có ý nghĩa độc lập** khi câu hỏi cha bị xoá. Xoá câu hỏi thì đáp án phải biến mất theo, nếu không sẽ để lại rác mồ côi trong database.

**Ràng buộc nghiệp vụ:** mỗi câu hỏi phải có **đúng một** đáp án `is_correct = 1`. SQLite không diễn đạt được ràng buộc "đúng một" bằng CHECK constraint thông thường, nên nó được bảo đảm ở hai chỗ khác:
- Khi ghi: màn hình `SuaCauHoiActivity` bắt buộc admin chọn một đáp án đúng trước khi lưu.
- Khi đọc: `QuestionWithAnswers.correctAnswerId()` trả về `0` nếu không tìm thấy đáp án đúng — giá trị canh gác giúp phát hiện dữ liệu hỏng thay vì âm thầm chấm sai.

## 3.4. `traffic_signs` — Biển báo

| Cột | Kiểu | Ràng buộc | Ý nghĩa |
|---|---|---|---|
| `id` | INTEGER | PK, autoGenerate | |
| `ma_bien` | TEXT | NOT NULL | Mã biển, VD `P.101` |
| `ten_bien` | TEXT | NOT NULL | Tên biển |
| `nhom_bien` | TEXT | nullable | Cấm / Nguy hiểm / Hiệu lệnh / Chỉ dẫn / Phụ |
| `anh_url` | TEXT | nullable | Đường dẫn ảnh |
| `mo_ta` | TEXT | nullable | Mô tả ý nghĩa |

Bảng **độc lập**, không khoá ngoại tới bảng nào — chức năng tra cứu biển báo tách rời khỏi luồng thi.

Tính duy nhất của `ma_bien` không đặt bằng UNIQUE index mà kiểm tra ở tầng ứng dụng qua truy vấn `demTrungMa(maBien, boQuaId)`. Lý do: cần **báo lỗi thân thiện bằng tiếng Việt** trên form ("Mã biển đã tồn tại") thay vì để `SQLiteConstraintException` ném ra; và tham số `boQuaId` cho phép admin lưu lại chính biển đang sửa mà không bị báo trùng với chính nó.

## 3.5. `exam_sets` — Bộ đề

| Cột | Kiểu | Ý nghĩa |
|---|---|---|
| `id` | INTEGER PK autoGenerate | |
| `ten_de` | TEXT NOT NULL | Tên đề |
| `so_cau` | INTEGER | Số câu trong đề |
| `thoi_gian` | INTEGER | Thời gian làm bài (**phút**) |
| `nguong_dat` | INTEGER | Số câu đúng tối thiểu để ĐẠT |

Thông số đề thi nằm **trong dữ liệu**, không rải rác trong code. Khi Cục CSGT đổi cấu trúc đề, chỉ cần sửa bản ghi trong bảng này. Đề ngẫu nhiên (không thuộc bộ đề cố định nào) lấy thông số từ hằng số trong `ExamConfig.java` — cũng tập trung một chỗ.

## 3.6. `exam_set_questions` — Bảng nối bộ đề ↔ câu hỏi

| Cột | Kiểu | Ràng buộc |
|---|---|---|
| `exam_set_id` | INTEGER | PK ghép, FK → `exam_sets.id` CASCADE |
| `question_id` | INTEGER | PK ghép, FK → `questions.id` CASCADE, IDX |

**Khoá chính ghép** `(exam_set_id, question_id)` khiến một câu hỏi không thể xuất hiện hai lần trong cùng một đề — ràng buộc này miễn phí, do chính khoá chính bảo đảm.

SQLite tự tạo index cho cột **đầu tiên** của khoá chính ghép (`exam_set_id`), nên chỉ cần khai báo thêm `@Index("question_id")` cho chiều tra ngược.

## 3.7. `users` — Người dùng

| Cột | Kiểu | Ràng buộc | Ý nghĩa |
|---|---|---|---|
| `id` | **TEXT** | PK, NOT NULL | **Firebase UID**; bản offline dùng `"local"` |
| `ho_ten` | TEXT | | Họ tên |
| `email` | TEXT | | Email |
| `anh_dai_dien` | TEXT | | Đường dẫn ảnh trong bộ nhớ riêng của app |
| `vai_tro` | TEXT | | `"user"` hoặc `"admin"` |
| `ngay_thi_du_kien` | INTEGER | | Epoch millis; `0` = chưa đặt |

**Khoá chính là TEXT, không phải INTEGER** — đây là quyết định thiết kế đáng nói khi bảo vệ. Dùng thẳng Firebase UID làm khoá chính giúp bản ghi Room và tài khoản Firebase khớp nhau tự nhiên, không cần bảng ánh xạ trung gian. Cái giá phải trả là mọi `user_id` trong các bảng con đều là TEXT.

**Cột `vai_tro` trong Room chỉ là bản sao để hiển thị giao diện.** Nguồn sự thật về vai trò nằm ở Firestore `users/{uid}.vaiTro`, được bảo vệ bằng Security Rules. Chi tiết ở mục VI — đây là điểm quan trọng để chứng minh phân quyền của ứng dụng là thật, không phải chỉ ẩn nút bấm.

**Không có cột mật khẩu.** Mật khẩu do Firebase Auth quản lý hoàn toàn, ứng dụng không bao giờ chạm vào — không lưu thô, cũng không cần tự hash.

## 3.8. `attempts` — Lượt thi

| Cột | Kiểu | Ràng buộc | Ý nghĩa |
|---|---|---|---|
| `id` | INTEGER | PK autoGenerate | |
| `user_id` | TEXT | FK → `users.id` CASCADE, IDX | Ai thi |
| `exam_set_id` | INTEGER | *không FK* | Bộ đề; **`0` = đề ngẫu nhiên** |
| `so_cau_dung` | INTEGER | | Số câu đúng |
| `ket_qua` | TEXT | | `"DAT"` / `"TRUOT"` |
| `ly_do_truot` | TEXT | | Rỗng nếu đạt |
| `thoi_gian_lam` | INTEGER | | Số **giây** đã dùng |
| `ngay_thi` | INTEGER | | Epoch millis |

Hai giá trị `"DAT"` / `"TRUOT"` được khai báo thành hằng số `Attempt.KET_QUA_DAT` / `KET_QUA_TRUOT` để không gõ nhầm chuỗi rải rác trong code.

**Vì sao `exam_set_id` cố ý KHÔNG khai báo khoá ngoại?** Vì nó nhận giá trị quy ước `0` cho đề ngẫu nhiên — mà `0` thì không tồn tại trong `exam_sets`. Nếu khai báo FK, mọi lượt thi đề ngẫu nhiên sẽ bị SQLite từ chối. Đây là đánh đổi có chủ đích: chấp nhận một cột không được ràng buộc để biểu diễn được cả hai loại đề trong cùng một bảng lịch sử.

> Cách làm chặt chẽ hơn là để cột nullable với `NULL` nghĩa là đề ngẫu nhiên, khi đó vẫn khai báo được FK (SQLite bỏ qua kiểm tra FK khi giá trị là NULL). Nếu có thời gian cải tiến, đây là chỗ nên sửa.

**Lưu ý về đơn vị thời gian:** `attempts.thoi_gian_lam` tính bằng **giây**, còn `exam_sets.thoi_gian` tính bằng **phút**. Khác đơn vị vì mục đích khác nhau (một bên đo chính xác thời gian thực làm, một bên là cấu hình cho người đọc), nhưng dễ nhầm — cần nhớ khi so sánh hai giá trị.

## 3.9. `user_answers` — Chi tiết câu trả lời

| Cột | Kiểu | Ràng buộc | Ý nghĩa |
|---|---|---|---|
| `id` | INTEGER | PK autoGenerate | |
| `attempt_id` | INTEGER | FK → `attempts.id` CASCADE, IDX | Thuộc lượt thi nào |
| `question_id` | INTEGER | FK → `questions.id` CASCADE, IDX | Câu hỏi nào |
| `answer_id` | INTEGER | | Đáp án đã chọn; **`0` = bỏ trống** |
| `dung_sai` | INTEGER (0/1) | | Đúng hay sai |

**Đây là bảng quan trọng nhất của toàn bộ CSDL.** Nó là bảng duy nhất lưu vết ở mức từng câu, và là nguồn dữ liệu cho ba chức năng:

1. **Xem lại bài thi** — hiện lại đúng đáp án người dùng đã chọn ở từng câu.
2. **Ôn lại câu sai** — gom `question_id` có `dung_sai = 0`.
3. **Phân tích điểm yếu theo chương** — nối `user_answers → questions → chapters` rồi gom nhóm.

Nếu chỉ lưu `so_cau_dung` trong `attempts` mà bỏ bảng này, ứng dụng sẽ mất cả ba chức năng trên — đó là lý do nó tồn tại dù nhìn qua có vẻ trùng lặp dữ liệu.

Cột `dung_sai` là **dữ liệu suy dẫn được** (có thể tính lại từ `answer_id` và `answers.is_correct`). Chấp nhận phi chuẩn hoá ở đây một cách có ý thức: nó giúp các truy vấn thống kê không phải JOIN thêm bảng `answers`, và giữ nguyên kết quả chấm cũ ngay cả khi admin sửa đáp án về sau.

## 3.10. `notes` — Ghi chú cá nhân

| Cột | Kiểu | Ràng buộc |
|---|---|---|
| `id` | INTEGER | PK autoGenerate |
| `user_id` | TEXT | FK → `users.id` CASCADE, IDX |
| `question_id` | INTEGER | FK → `questions.id` CASCADE, IDX |
| `noi_dung` | TEXT | NOT NULL |

Mỗi cặp (người dùng, câu hỏi) có tối đa một ghi chú — bảo đảm bằng logic ứng dụng và truy vấn `LIMIT 1`.

## 3.11. `review_schedule` — Lịch ôn lại giãn dần

| Cột | Kiểu | Ràng buộc | Ý nghĩa |
|---|---|---|---|
| `id` | INTEGER | PK autoGenerate | |
| `user_id` | TEXT | FK → `users.id` CASCADE, IDX | |
| `question_id` | INTEGER | FK → `questions.id` CASCADE, IDX | |
| `so_lan_sai` | INTEGER | | Đã sai bao nhiêu lần |
| `lan_on_tiep` | INTEGER | | Ngày cần ôn lại (epoch millis) |
| `khoang_cach` | INTEGER | | Số ngày giãn cách hiện tại: 1, 3, 7, 14… |

Thiết kế cho thuật toán **lặp lại giãn cách (spaced repetition)**: câu trả lời đúng thì `khoang_cach` tăng lên, đẩy `lan_on_tiep` ra xa; trả lời sai thì `so_lan_sai` tăng và `khoang_cach` đặt lại về mức thấp.

`ReviewScheduleDao` cung cấp sẵn các truy vấn cần cho thuật toán:

| Truy vấn | Dùng để |
|---|---|
| `getOne(userId, questionId)` | Lấy lịch của một câu; `null` = câu chưa vào lịch |
| `getToiHan(userId, moc)` | Các câu tới hạn ôn tính đến thời điểm `moc` |
| `demToiHanLive(userId, moc)` | Đếm số câu tới hạn — hiện huy hiệu trên trang chủ |
| `getHaySaiNhat(userId, n)` | Top câu sai nhiều nhất |
| `xoa` / `xoaTatCa` | Dọn lịch khi người dùng đã thuộc |

> **Tình trạng hiện tại — cần nói rõ khi bảo vệ:** bảng và DAO đã sẵn sàng, nhưng **chưa có màn hình nào gọi tới**. Chức năng "ôn lại câu sai" hiện chạy bằng `AttemptDao.getWrongQuestionIds()` — gom mọi câu từng sai trong lịch sử, chưa áp dụng giãn cách. Phần còn thiếu là thuật toán cập nhật `khoang_cach`/`lan_on_tiep` sau mỗi lần trả lời, xem mục 10.3.

---

# IV. CHỈ MỤC VÀ HIỆU NĂNG

## 4.1. Danh sách chỉ mục

| Bảng | Cột đánh chỉ mục | Phục vụ truy vấn |
|---|---|---|
| `questions` | `chapter_id` | Lọc câu hỏi theo chương |
| `answers` | `question_id` | Nạp đáp án của câu hỏi |
| `exam_set_questions` | `question_id` | Tra ngược câu hỏi → đề |
| `attempts` | `user_id` | Lịch sử thi của một người |
| `user_answers` | `attempt_id`, `question_id` | Xem lại bài, thống kê theo chương |
| `notes` | `user_id`, `question_id` | Lấy ghi chú theo cặp |
| `review_schedule` | `user_id`, `question_id` | Lấy lịch ôn theo cặp |

**Quy tắc chung:** mọi cột khoá ngoại đều được đánh chỉ mục. Room còn cảnh báo lúc biên dịch nếu quên — vì thiếu chỉ mục trên FK khiến SQLite phải quét toàn bảng mỗi lần kiểm tra ràng buộc khi xoá bản ghi cha.

## 4.2. Truy vấn không có chỉ mục hỗ trợ

Các truy vấn tìm kiếm dùng `LIKE '%từ khoá%'` (bảng `questions` và `traffic_signs`) **không thể** dùng chỉ mục — dấu `%` ở đầu chuỗi khiến B-tree vô dụng, SQLite buộc phải quét toàn bảng.

Đây là chấp nhận được ở quy mô này: 600 câu hỏi và vài trăm biển báo là tập dữ liệu rất nhỏ, quét toàn bảng vẫn dưới vài mili giây, và truy vấn chạy trên luồng nền nên không đơ giao diện. Nếu dữ liệu lớn hơn nhiều lần, giải pháp là bảng ảo **FTS4/FTS5** của SQLite (Room hỗ trợ qua `@Fts4`).

---

# V. RÀNG BUỘC TOÀN VẸN VÀ GIAO DỊCH

## 5.1. Ba tầng bảo vệ toàn vẹn

| Tầng | Cơ chế | Ví dụ |
|---|---|---|
| **SQLite** | FK, CASCADE, RESTRICT, PK ghép, NOT NULL | Xoá câu hỏi → đáp án tự xoá |
| **Room** | `@Transaction` gói nhiều thao tác | Lưu lượt thi trọn vẹn |
| **Ứng dụng** | Kiểm tra trên form trước khi ghi | Bắt buộc chọn một đáp án đúng |

## 5.2. Giao dịch 1 — Lưu một lượt thi

```java
@Transaction
default long saveAttempt(Attempt attempt, List<UserAnswer> answers) {
    long attemptId = insertAttempt(attempt);
    for (UserAnswer ua : answers) {
        ua.attemptId = (int) attemptId;   // gán FK sau khi có id thật
    }
    insertUserAnswers(answers);
    return attemptId;
}
```

Một lượt thi là **1 bản ghi `attempts` + N bản ghi `user_answers`**. Nếu ghi nửa chừng rồi lỗi (hết pin, hệ điều hành huỷ tiến trình), sẽ xuất hiện một lượt thi ghi 25 câu đúng nhưng chỉ có 12 câu trả lời chi tiết — dữ liệu thống kê sai vĩnh viễn.

`@Transaction` bảo đảm nguyên tắc **"hoặc tất cả, hoặc không gì cả"**: Room bọc cả khối trong `BEGIN TRANSACTION` … `COMMIT`, có lỗi thì `ROLLBACK` sạch sẽ.

Chi tiết đáng chú ý: `attemptId` chỉ biết được **sau** khi chèn bản ghi cha, nên vòng lặp gán FK phải nằm giữa hai lệnh chèn — bên trong cùng một giao dịch.

## 5.3. Giao dịch 2 — Sửa câu hỏi kèm đáp án

```java
@Transaction
default void updateQuestionWithAnswers(Question question, List<Answer> answers) {
    update(question);
    deleteAnswersOf(question.id);   // xoá sạch đáp án cũ
    for (Answer a : answers) {
        a.id = 0;                   // để Room tự sinh id mới
        a.questionId = question.id;
    }
    insertAnswers(answers);
}
```

Chiến lược **xoá hết rồi ghi lại** đơn giản hơn nhiều so với việc đối chiếu từng đáp án xem cái nào sửa, cái nào thêm, cái nào bỏ. Nhưng nó có một khoảnh khắc nguy hiểm: giữa `deleteAnswersOf()` và `insertAnswers()`, câu hỏi **không có đáp án nào**. `@Transaction` khiến trạng thái trung gian đó không bao giờ lộ ra ngoài — không luồng nào khác đọc được, và nếu lỗi thì quay lui về đáp án cũ nguyên vẹn.

Dòng `a.id = 0` là bắt buộc: với `autoGenerate = true`, Room hiểu `id = 0` là "hãy sinh id mới cho tôi". Nếu giữ nguyên id cũ, các id đó vừa bị xoá nên sẽ tạo ra bản ghi với id cũ — không sai về mặt dữ liệu nhưng dễ gây nhầm lẫn khi truy vết.

## 5.4. Ràng buộc nghiệp vụ không diễn đạt được bằng SQL

| Ràng buộc | Bảo đảm ở đâu |
|---|---|
| Mỗi câu hỏi có đúng một đáp án đúng | Form nhập của admin + hàm canh gác `correctAnswerId()` |
| `ma_bien` không trùng | Truy vấn `demTrungMa()` trước khi lưu |
| `ket_qua` chỉ nhận `"DAT"`/`"TRUOT"` | Hằng số trong `Attempt` |
| `vai_tro` chỉ nhận `"user"`/`"admin"` | **Firestore Security Rules** (mục VI) |

---

# VI. PHẦN DỮ LIỆU TRÊN FIRESTORE

## 6.1. Cấu trúc

Chỉ đúng **một** collection được mở:

```
users/{uid}
  ├── hoTen   : string
  ├── email   : string
  ├── vaiTro  : "user" | "admin"
  └── ngayTao : number (epoch millis)
```

Mọi collection khác bị chặn toàn bộ bằng rule `match /{document=**} { allow read, write: if false; }`.

Toàn bộ việc đọc/ghi collection này nằm trong **`NguoiDungRepository`** — Activity không gọi `FirebaseFirestore` trực tiếp:

| Hàm | Việc |
|---|---|
| `uidHienTai()` | uid người đang đăng nhập |
| `taiDanhSach(khiXong, khiLoi)` | tải toàn bộ tài khoản, đã sắp xếp admin lên đầu |
| `doiVaiTro(hoSo, khiXong, khiLoi)` | nâng/hạ quyền một tài khoản khác |

Đây là ngoại lệ có chủ đích với nguyên tắc offline-first ở mục 1.1: màn Quản trị người dùng **cần mạng**, vì danh sách toàn bộ tài khoản là dữ liệu của máy chủ, Room dưới máy chỉ giữ đúng hồ sơ của người đang dùng máy đó. Ngoại lệ này chấp nhận được vì nó không nằm trong luồng ôn tập/thi.

## 6.2. Vì sao vai trò phải nằm trên máy chủ

Đây là phần bảo mật đáng nói nhất của thiết kế CSDL.

Nếu vai trò admin chỉ lưu trong Room dưới máy, bất kỳ ai cũng có thể dịch ngược APK, sửa giá trị `vai_tro` thành `"admin"` và mở khoá toàn bộ chức năng quản trị. Phân quyền kiểu đó chỉ là **ẩn nút bấm**, không phải bảo mật.

Vì thế vai trò được lưu ở Firestore và bảo vệ bằng ba luật:

```javascript
// TẠO: tài khoản mới bắt buộc là "user"
allow create: if request.auth.uid == uid
              && request.resource.data.vaiTro == 'user';

// SỬA: chính chủ sửa hồ sơ mình — vai trò PHẢI giữ nguyên
(request.auth.uid == uid
 && request.resource.data.vaiTro == resource.data.vaiTro)
||
// hoặc: admin đổi vai trò của NGƯỜI KHÁC, và CHỈ được đụng field vaiTro
(request.auth.uid != uid
 && laAdmin()
 && request.resource.data.diff(resource.data).affectedKeys().hasOnly(['vaiTro']))
```

Hệ quả: **không ai tự phong mình làm admin được**, kể cả gọi thẳng API Firestore bỏ qua ứng dụng Android. Điều kiện `uid != mình` còn khiến admin không tự sửa vai trò của chính mình, nên nhánh thứ nhất luôn là luật duy nhất áp cho hồ sơ của chính bạn.

Hàm `laAdmin()` đọc vai trò **từ chính Firestore**, không tin bất cứ dữ liệu nào client gửi lên:

```javascript
function laAdmin() {
  return request.auth != null
         && get(/databases/$(database)/documents/users/$(request.auth.uid))
              .data.vaiTro == 'admin';
}
```

Bản sao `users.vai_tro` trong Room chỉ dùng để quyết định **hiện hay ẩn** menu quản trị — thuần tuý cho trải nghiệm, không phải hàng rào bảo mật.

## 6.3. Tài khoản `"local"`

`DatabaseSeeder` tạo sẵn một người dùng `id = "local"` để ứng dụng chạy được ngay cả khi chưa đăng nhập Firebase (tiện cho phát triển và demo offline). Khi người dùng đăng nhập thật, dữ liệu được gắn theo Firebase UID.

---

# VII. TRUY VẤN TIÊU BIỂU

## 7.1. Nạp câu hỏi kèm đáp án — `@Relation`

```java
public class QuestionWithAnswers {
    @Embedded
    public Question question;

    @Relation(parentColumn = "id", entityColumn = "question_id")
    public List<Answer> answers;
}
```

```java
@Transaction
@Query("SELECT * FROM questions WHERE chapter_id = :chapterId ORDER BY id ASC")
List<QuestionWithAnswers> getByChapter(int chapterId);
```

Room tự chạy truy vấn thứ hai để nạp đáp án và ghép vào từng câu hỏi — không phải viết JOIN rồi tự gom nhóm bằng tay.

`@Transaction` ở đây **không phải để ghi**, mà để **đọc nhất quán**: nó bảo đảm hai truy vấn (lấy câu hỏi, lấy đáp án) nhìn thấy cùng một ảnh chụp database. Không có nó, admin sửa đáp án đúng lúc đang nạp có thể tạo ra một câu hỏi ghép với đáp án của phiên bản khác.

## 7.2. Thống kê tỷ lệ đúng theo chương — JOIN 4 bảng

```sql
SELECT c.id AS chapterId, c.so_thu_tu AS soThuTu, c.ten_chuong AS tenChuong,
       COUNT(*) AS tong,
       SUM(CASE WHEN ua.dung_sai = 1 THEN 1 ELSE 0 END) AS dung
FROM user_answers ua
JOIN attempts  a ON ua.attempt_id  = a.id
JOIN questions q ON ua.question_id = q.id
JOIN chapters  c ON q.chapter_id   = c.id
WHERE a.user_id = :userId
GROUP BY c.id
ORDER BY c.so_thu_tu ASC
```

Đây là truy vấn phức tạp nhất trong dự án, và là câu trả lời cho câu hỏi "vì sao cần bảng `user_answers`". Chuỗi JOIN đi từ **câu trả lời → lượt thi → câu hỏi → chương**, rồi gom nhóm để tính tỷ lệ đúng từng chương.

Mẹo `SUM(CASE WHEN … THEN 1 ELSE 0 END)` là cách đếm có điều kiện của SQL: đếm số câu đúng trong cùng một lần quét đã đếm tổng số câu, khỏi cần hai truy vấn riêng.

Kết quả ánh xạ vào POJO `ChapterStat` (không phải entity) — Room khớp tên cột với tên trường nhờ các bí danh `AS`. Class này có sẵn hàm `phanTram()` để đổ vào biểu đồ cột.

## 7.3. Lấy các câu từng trả lời sai

```sql
SELECT DISTINCT ua.question_id
FROM user_answers ua
JOIN attempts a ON ua.attempt_id = a.id
WHERE a.user_id = :userId AND ua.dung_sai = 0
```

`DISTINCT` vì một câu có thể sai ở nhiều lượt thi khác nhau, chỉ cần ôn lại một lần.

## 7.4. Bộ lọc động màn quản trị — một truy vấn cho mọi tổ hợp

```sql
SELECT * FROM questions
WHERE noi_dung LIKE '%' || :kw || '%'
  AND (:chapterId = 0 OR chapter_id = :chapterId)
  AND (:chiDiemLiet = 0 OR is_diem_liet = 1)
ORDER BY id ASC
```

Kỹ thuật `(:thamSo = giáTrịRỗng OR điềuKiện)` cho phép **một truy vấn duy nhất** phục vụ mọi tổ hợp bộ lọc: khi `chapterId = 0` (nghĩa là "tất cả chương"), vế trái đúng nên toàn bộ điều kiện luôn đúng và bộ lọc coi như bị vô hiệu.

Cách này tránh phải viết 8 hàm DAO cho 3 bộ lọc bật/tắt độc lập. Tương tự với `TrafficSignDao.filter()`, ở đó dùng `(:nhom IS NULL OR nhom_bien = :nhom)`.

Truy vấn trả `LiveData` nên khi admin thêm/sửa/xoá câu hỏi, danh sách trên màn hình tự cập nhật, không cần gọi làm mới thủ công.

## 7.5. Sinh đề ngẫu nhiên

```sql
SELECT * FROM questions ORDER BY RANDOM() LIMIT :n
SELECT * FROM questions WHERE is_diem_liet = 1 ORDER BY RANDOM() LIMIT :n
```

Hai truy vấn tách riêng để bảo đảm đề luôn có đúng số câu điểm liệt quy định (`ExamConfig.SO_CAU_DIEM_LIET`), giống đề thi thật — nếu bốc ngẫu nhiên từ toàn bộ 600 câu thì có đề không có câu điểm liệt nào, không mô phỏng đúng kỳ thi.

## 7.6. Đọc phản ứng — `LiveData` vs. đồng bộ

DAO cung cấp hai kiểu hàm, dùng cho hai mục đích khác nhau:

| Kiểu | Ví dụ | Dùng khi nào |
|---|---|---|
| `LiveData<List<T>>` | `getAllLive()` | Dữ liệu hiển thị và **cần tự cập nhật** khi database đổi. Room tự chạy trên luồng nền. |
| `List<T>` đồng bộ | `getAllSync()`, `getByChapter()` | Đọc một lần trong luồng nghiệp vụ (sinh đề, chấm điểm). **Bắt buộc** gọi trong `AppDatabase.IO`. |

Ở tầng trên, các truy vấn `LiveData` của DAO được ViewModel bọc thêm một lớp `Transformations.switchMap`, để bộ lọc đổi thì tự chuyển sang truy vấn tương ứng. Ví dụ trong `BienBaoViewModel`:

```java
danhSach = Transformations.switchMap(boLoc,
        bl -> repo.filterSigns(bl.nhom, bl.tuKhoa));
```

Nhờ vậy Activity chỉ `observe()` **một lần duy nhất**. Trước đây Activity phải tự gọi `removeObservers()` trước mỗi lần đổi bộ lọc — quên một lần là nhiều observer cùng ghi vào một danh sách và kết quả nhảy loạn.

## 7.7. Truy vấn phục vụ đồng bộ delta

```sql
SELECT * FROM questions WHERE updated_at > :moc ORDER BY updated_at ASC
```

Trả về đúng những câu đã sửa kể từ mốc `:moc`. Đi kèm là `upsertAll()` (`@Insert(onConflict = REPLACE)`) để ghi đè các câu tải về từ Firestore theo khoá chính.

Hai hàm này là **hạ tầng cho cơ chế đồng bộ ở mục 10.1** — bảng và truy vấn đã sẵn sàng, phần gọi Firestore chưa viết. Điểm cần nhấn mạnh khi bảo vệ: đồng bộ **bắt buộc là delta**, vì Firestore tính tiền theo số document đọc, tải lại 600 câu mỗi lần vừa tốn tiền vừa tốn băng thông của người học.

---

# VIII. KHỞI TẠO DỮ LIỆU

## 8.1. Cơ chế hiện tại

`DatabaseSeeder.seed(db, context)` chạy **một lần duy nhất** khi SQLite tạo file `oto.db`, theo thứ tự tôn trọng khoá ngoại:

```
seedChapters()  →  seedUser()  →  seedQuestions(context)  →  seedTrafficSigns()  →  seedExamSets()
```

Thứ tự này không tuỳ tiện: `questions` tham chiếu `chapters` nên chương phải có trước; `exam_set_questions` tham chiếu cả `exam_sets` lẫn `questions` nên phải chạy cuối.

## 8.2. Câu hỏi nạp từ `assets/questions.json`

Câu hỏi **không viết thẳng trong code Java** mà đọc từ file JSON đóng gói trong APK:

```json
{
  "cau_hoi": [
    {
      "chuong": 1,
      "noi_dung": "Khái niệm \"đường bộ\" được hiểu như thế nào là đúng?",
      "diem_liet": false,
      "giai_thich": "Theo Luật, đường bộ gồm đường, cầu, hầm và bến phà đường bộ.",
      "dap_an": ["…", "…", "…"],
      "dap_an_dung": 0
    }
  ]
}
```

| Trường | Ý nghĩa |
|---|---|
| `chuong` | id chương 1..6, khớp bảng `chapters` |
| `diem_liet` | `true` = câu điểm liệt |
| `dap_an` | mảng các đáp án |
| `dap_an_dung` | **chỉ số (từ 0)** của đáp án đúng trong mảng trên |

**Vì sao tách ra file JSON:**
- Thay bộ đề không cần biên dịch lại code;
- File dễ sinh tự động từ nguồn dữ liệu của Cục CSGT;
- Code seeder ngắn, không phải sửa khi dữ liệu đổi.

**Vì sao dùng `org.json` chứ không phải Gson:** `org.json` (`JSONObject`, `JSONArray`) có sẵn trong Android SDK, nên **không phải thêm thư viện mới** — đúng quy tắc kiểm soát dependency của nhóm.

Mỗi câu được ghi bằng `insertQuestionWithAnswers()` — một giao dịch cho cả câu hỏi lẫn đáp án, nên không bao giờ tồn tại câu hỏi thiếu đáp án. Nếu JSON sai định dạng, seeder ghi log lỗi và bỏ qua phần câu hỏi; các bảng khác vẫn seed bình thường nên app vẫn mở được và admin vẫn thêm câu hỏi tay được.

## 8.3. Việc còn lại

> **Cần nêu rõ khi bảo vệ:**
> - `assets/questions.json` hiện chỉ có **8 câu mẫu**, chưa phải bộ 600 câu chính thức. Cơ chế nạp đã xong, chỉ còn thay nội dung file.
> - Biển báo vẫn seed bằng code Java (5 biển mẫu) — chưa tách ra JSON như câu hỏi.
> - Thông số đề thi trong `ExamConfig.java` (25 câu / 19 phút / ngưỡng 21) **vẫn là giá trị mẫu chờ xác minh** từ nguồn chính thức. Nhóm đã chủ động ghi chú cảnh báo này ngay trong code.
>
> Lưu ý vận hành: seed chỉ chạy lúc **tạo mới** database. Sửa `questions.json` rồi cài đè sẽ **không** thấy dữ liệu mới — phải gỡ cài đặt app rồi cài lại (hoặc xoá dữ liệu ứng dụng).

---

# IX. QUẢN LÝ PHIÊN BẢN VÀ NÂNG CẤP LƯỢC ĐỒ

## 9.1. Cấu hình hiện tại

```java
version = 2,
exportSchema = true
...
.addMigrations(MIGRATION_1_2)
```

Ba thành phần phối hợp với nhau:

**1. `exportSchema = true` + khai báo nơi ghi trong Gradle**

```kotlin
javaCompileOptions {
    annotationProcessorOptions {
        arguments += mapOf("room.schemaLocation" to "$projectDir/schemas")
    }
}
```

Mỗi lần biên dịch, Room ghi ra `app/schemas/com.example.oto.data.AppDatabase/<version>.json` — mô tả đầy đủ lược đồ của phiên bản đó. **Commit thư mục này vào Git**: nó là căn cứ để viết migration cho bản sau, và cho phép cả nhóm xem lược đồ đã đổi những gì qua từng phiên bản.

**2. Migration tường minh 1 → 2**

```java
static final Migration MIGRATION_1_2 = new Migration(1, 2) {
    @Override
    public void migrate(@NonNull SupportSQLiteDatabase db) {
        db.execSQL("ALTER TABLE questions ADD COLUMN updated_at INTEGER NOT NULL DEFAULT 0");
    }
};
```

`NOT NULL DEFAULT 0` là **bắt buộc**: bảng đang có sẵn dữ liệu, SQLite cần biết điền giá trị nào cho các dòng cũ. Giá trị `0` mang nghĩa "chưa từng đồng bộ", nên lần đồng bộ delta đầu tiên sẽ coi mọi câu cũ là cần gửi lên.

**3. KHÔNG dùng `fallbackToDestructiveMigration`**

Tuỳ chọn đó khiến Room **xoá sạch database và seed lại** mỗi khi tăng version mà thiếu migration. Với người dùng thật, một bản cập nhật sẽ cuốn theo toàn bộ lịch sử thi, ghi chú và ngày thi dự kiến của họ — không chấp nhận được. Bản trước của dự án dùng tuỳ chọn này; **nay đã gỡ bỏ**.

## 9.2. Quy trình khi cần đổi lược đồ lần sau

1. Sửa entity (thêm/bớt cột).
2. Tăng `version` lên 3.
3. Viết `MIGRATION_2_3` với câu lệnh `ALTER TABLE` tương ứng, đối chiếu `schemas/2.json` để biết chính xác lược đồ cũ.
4. Thêm vào `.addMigrations(MIGRATION_1_2, MIGRATION_2_3)`.
5. Biên dịch — Room sinh `schemas/3.json`; commit file này.

Nếu quên bước 3–4, ứng dụng sẽ **ném `IllegalStateException` ngay khi mở database** thay vì âm thầm xoá dữ liệu. Đây chính là điều mong muốn: hỏng thì phải hỏng ồn ào, ngay lúc phát triển.

> **Hạn chế còn lại:** thư mục `schemas/` hiện chỉ có `2.json`, vì phiên bản 1 được tạo lúc `exportSchema` còn tắt. Room do đó không tự đối chiếu được migration 1→2; câu lệnh `ALTER TABLE` ở trên là do nhóm tự viết và tự kiểm. Từ phiên bản 2 trở đi thì cơ chế đối chiếu hoạt động đầy đủ.

---

# X. HƯỚNG PHÁT TRIỂN

## 10.1. Đồng bộ câu hỏi từ admin (tải delta)

Hiện admin sửa câu hỏi chỉ có hiệu lực **trên máy của chính admin** — chưa có kênh đẩy thay đổi tới máy học viên.

**Phần đã làm xong (hạ tầng phía Room):**
- Cột `questions.updated_at`, tự đóng dấu mỗi lần lưu câu hỏi (mục 3.2);
- `QuestionDao.getSuaSau(moc)` — lấy các câu đã sửa sau một mốc;
- `QuestionDao.upsertAll(list)` — ghi đè câu tải về theo khoá chính.

**Phần còn thiếu (phía Firestore):**
1. Admin ghi câu hỏi lên collection `questions` của Firestore, kèm rule chỉ admin ghi được. Rules hiện chặn mọi collection ngoài `users`, nên phải mở thêm một `match` cho `questions`.
2. Máy học viên lưu mốc `lastSyncTime` trong SharedPreferences, mỗi lần đồng bộ chỉ hỏi `whereGreaterThan("updated_at", lastSyncTime)` rồi gọi `upsertAll()` vào Room.

**Bắt buộc là tải delta, tuyệt đối không tải lại toàn bộ 600 câu mỗi lần** — vừa tốn băng thông của người dùng, vừa tốn hạn mức đọc Firestore (tính tiền theo số document đọc).

## 10.2. Đồng bộ tiến độ học đa thiết bị

Đẩy `attempts` + `user_answers` lên Firestore dưới `users/{uid}/attempts/{id}`, kèm rule chỉ chính chủ đọc/ghi. Room vẫn là nguồn đọc chính, Firestore chỉ là bản sao lưu và kênh đồng bộ giữa các thiết bị.

## 10.3. Hoàn thiện `review_schedule`

`ReviewScheduleDao` **đã viết xong** (mục 3.11). Phần còn thiếu là thuật toán cập nhật sau mỗi lần người học trả lời:

- Trả lời **đúng** → `khoang_cach` tăng theo dãy 1 → 3 → 7 → 14 ngày, `lan_on_tiep = hôm nay + khoang_cach`;
- Trả lời **sai** → `so_lan_sai++`, `khoang_cach` đặt lại về 1 ngày.

Sau đó thay `AttemptDao.getWrongQuestionIds()` bằng `getToiHan()` ở màn ôn câu sai, và gắn `demToiHanLive()` vào huy hiệu trên trang chủ.

## 10.4. Ràng buộc chặt hơn

- Đưa `attempts.exam_set_id` về nullable kèm khoá ngoại (mục 3.8).
- Thêm UNIQUE index cho `traffic_signs.ma_bien`, giữ kiểm tra ở tầng ứng dụng để báo lỗi thân thiện.
- Thêm UNIQUE index cho `notes(user_id, question_id)` và `review_schedule(user_id, question_id)`.

---

# XI. CÂU HỎI THƯỜNG GẶP KHI VẤN ĐÁP

**Vì sao dùng Room mà không dùng SQLite thuần?**
Room sinh code truy cập ở thời điểm **biên dịch** và kiểm tra cú pháp SQL ngay lúc đó — gõ sai tên cột là lỗi biên dịch chứ không phải lỗi lúc chạy. Ngoài ra Room tích hợp sẵn với LiveData (giao diện tự cập nhật) và bắt buộc tách DAO, hợp với kiến trúc MVVM.

**Vì sao khoá chính bảng `users` là TEXT?**
Vì nó chính là Firebase UID. Dùng thẳng UID giúp bản ghi Room và tài khoản Firebase khớp nhau mà không cần bảng ánh xạ trung gian.

**Vì sao `questions → chapters` là RESTRICT nhưng `answers → questions` là CASCADE?**
Đáp án không có ý nghĩa khi câu hỏi cha mất, nên xoá theo là đúng. Ngược lại, chương là dữ liệu gốc — xoá một chương mà cuốn theo hàng trăm câu hỏi là tai nạn dữ liệu, nên chặn lại.

**Bảng nào quan trọng nhất?**
`user_answers`. Nó là bảng duy nhất lưu vết ở mức từng câu, và là nguồn cho cả ba chức năng xem lại bài, ôn câu sai và phân tích điểm yếu theo chương.

**Vì sao phải dùng `@Transaction` khi lưu lượt thi?**
Vì một lượt thi gồm nhiều lệnh chèn ở hai bảng. Nếu ghi nửa chừng rồi lỗi, sẽ có lượt thi ghi 25 câu đúng nhưng chỉ lưu 12 câu chi tiết — thống kê sai vĩnh viễn và không tự phát hiện được.

**Vai trò admin lưu ở đâu, vì sao?**
Ở Firestore `users/{uid}.vaiTro`, bảo vệ bằng Security Rules. Bản trong Room chỉ để hiện/ẩn menu. Nếu để vai trò dưới máy, ai cũng có thể sửa APK để tự phong admin — khi đó phân quyền chỉ là ẩn nút bấm chứ không phải bảo mật.

**Nếu mất mạng lúc đang thi thì sao?**
Không ảnh hưởng gì. Toàn bộ câu hỏi, đáp án và việc lưu kết quả đều nằm ở Room dưới máy. Mạng chỉ cần cho đăng nhập lần đầu.

**Cột `dung_sai` có bị thừa không, khi đã có `answer_id`?**
Về lý thuyết là dữ liệu suy dẫn được. Nhưng giữ lại có hai lợi ích: các truy vấn thống kê khỏi phải JOIN thêm bảng `answers`, và kết quả chấm cũ được bảo toàn ngay cả khi admin sửa đáp án về sau. Đây là phi chuẩn hoá có chủ đích.

**Nếu cập nhật app thì dữ liệu người dùng có mất không?**
Không. Database nâng cấp bằng `Migration` tường minh — chỉ thêm cột, giữ nguyên dữ liệu. Bản trước của dự án dùng `fallbackToDestructiveMigration` (xoá sạch mỗi lần đổi version), nay đã gỡ bỏ vì với người dùng thật thì đó là mất toàn bộ lịch sử thi.

**Vì sao câu hỏi để trong `assets/questions.json` mà không viết trong code?**
Để thay bộ 600 câu chính thức không cần biên dịch lại code, và để file JSON có thể sinh tự động từ nguồn dữ liệu của Cục CSGT. Đọc bằng `org.json` có sẵn trong Android SDK nên không phải thêm thư viện.

**Activity có được gọi thẳng DAO không?**
Không. Luồng bắt buộc là Activity → ViewModel → Repository → DAO. Hiện không còn Activity nào tham chiếu `QuizRepository`, `AppDatabase` hay `FirebaseFirestore` — có thể kiểm chứng bằng cách tìm kiếm ba tên đó trong thư mục `ui/`.

**Bảng `review_schedule` có DAO rồi sao chưa dùng?**
Bảng và DAO đã sẵn sàng, nhưng thuật toán giãn cách chưa cài. Chức năng ôn câu sai hiện gom mọi câu từng sai từ lịch sử (`getWrongQuestionIds`). Đây là việc còn lại đã ghi rõ ở mục 10.3, không phải thiếu sót do quên.

---

## Lịch sử thay đổi tài liệu

| Phiên bản DB | Thay đổi |
|---|---|
| v1 | Lược đồ 11 bảng ban đầu |
| **v2** | Thêm `questions.updated_at` (`MIGRATION_1_2`); bật `exportSchema`; bỏ `fallbackToDestructiveMigration`; thêm `ReviewScheduleDao`; câu hỏi chuyển sang nạp từ `assets/questions.json`; tách `NguoiDungRepository` cho phần Firestore |

*Tài liệu này bám sát mã nguồn tại thời điểm viết. Khi lược đồ thay đổi, cập nhật cả tài liệu này và mục 3.3 của `02-tai-lieu-thiet-ke.md`.*
