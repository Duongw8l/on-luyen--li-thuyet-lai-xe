# CLAUDE.md — Ngữ cảnh dự án

> File này để Claude Code đọc mỗi khi làm việc trong dự án. Nó mô tả kiến trúc, quy ước và ràng buộc của dự án. Luôn tuân theo các quy tắc ở đây khi sinh code.

## 1. Tổng quan dự án

Ứng dụng Android **ôn thi lý thuyết sát hạch lái xe hạng B** (bộ 600 câu hỏi chính thức của Cục CSGT, áp dụng từ 01/6/2025).

Đây là đồ án môn **Phát triển phần mềm cho thiết bị di động**, nhóm 5 người, thi vấn đáp. Ưu tiên: code rõ ràng, dễ giải thích khi bảo vệ, đúng kiến trúc chuẩn — hơn là tối ưu hoá phức tạp.

Đối tượng người dùng: **Học viên** (ôn thi) và **Admin** (quản lý câu hỏi, biển báo).

## 2. Công nghệ và kiến trúc

- **Ngôn ngữ:** Java
- **Kiến trúc:** MVVM (View → ViewModel → Repository → Data sources)
- **CSDL cục bộ:** Room (SQLite) — nguồn đọc CHÍNH
- **Đám mây:** Firebase (Authentication + Cloud Firestore + Storage)
- **Bất đồng bộ:** ExecutorService / AsyncTask thay thế bằng Executor; kết quả trả về qua LiveData. Room dùng truy vấn trả `LiveData<>` hoặc chạy trên Executor riêng (KHÔNG truy vấn Room trên luồng chính).
- **Quan sát dữ liệu:** LiveData + ViewModel
- **Ảnh:** Glide
- **Biểu đồ:** MPAndroidChart
- **Tiêm phụ thuộc:** khởi tạo thủ công (Repository, DAO truyền vào ViewModel qua ViewModelProvider.Factory). KHÔNG ép dùng Hilt/Dagger trừ khi nhóm đã quen.

### Nguyên tắc kiến trúc BẮT BUỘC tuân theo

1. **Offline-first.** App LUÔN đọc câu hỏi, đáp án, biển báo từ Room. KHÔNG đọc trực tiếp từ Firestore khi người dùng đang ôn/làm bài. Firestore chỉ dùng để đồng bộ.
2. **Phân tầng nghiêm ngặt.** Activity/Fragment không được gọi thẳng Repository, DAO hay Firestore. Luồng bắt buộc: View → ViewModel → Repository → (Room DAO / Firebase).
3. **ViewModel không giữ tham chiếu tới Activity/Fragment/Context của View** (tránh rò rỉ bộ nhớ). Nếu cần Context, dùng AndroidViewModel với Application context.
4. **Mọi thao tác I/O (Room, Firebase, mạng) chạy trên background thread** (Executor), KHÔNG chặn luồng UI. Kết quả cập nhật lên UI qua LiveData.
5. **Không hardcode chuỗi hiển thị** — đưa vào `strings.xml` (hỗ trợ tiếng Việt).

## 3. Phân chia dữ liệu (RẤT QUAN TRỌNG)

| Loại dữ liệu | Lưu ở đâu | Ghi chú |
|---|---|---|
| 600 câu hỏi, đáp án, giải thích | Room + `assets/questions.json` | ĐÃ NẠP ĐỦ 600 câu. Sinh từ docx bằng `tools/convert_chuong.py` — KHÔNG sửa tay questions.json, sửa docx rồi chạy lại script |
| Ảnh biển báo, ảnh sa hình | `assets/` (trong APK) | KHÔNG để trên Firebase Storage |
| Tài khoản, xác thực | Firebase Auth | |
| Tiến độ học, lịch sử thi, câu sai, ghi chú | Firestore + Room cache | Dữ liệu cá nhân, cần đồng bộ đa thiết bị |
| Ảnh đại diện người dùng | Firebase Storage | Dung lượng nhỏ |
| Câu hỏi admin thêm/sửa sau này | Firestore → đồng bộ delta về Room | Xem mục Đồng bộ |

### Cơ chế đồng bộ câu hỏi (admin → máy khách)

- Bảng `questions` có cột `updated_at`.
- Máy khách đồng bộ bằng cách hỏi Firestore: lấy các câu có `updated_at` mới hơn lần đồng bộ cuối (`whereGreaterThan`), rồi `upsert` vào Room. Đây là **tải delta** — TUYỆT ĐỐI không tải lại toàn bộ 600 câu mỗi lần.
- Lưu mốc `lastSyncTime` trong SharedPreferences.

## 4. Lược đồ cơ sở dữ liệu (Room entities)

Mỗi entity là một class Java gắn `@Entity`, mỗi bảng có một interface DAO gắn `@Dao`.

- `chapters(id, ten_chuong, so_thu_tu)` — 7 chương theo cấu trúc bộ 600 câu chính thức (166/26/21/56/35/182/114 câu)
- `questions(id, chapter_id FK, noi_dung, anh_url, is_diem_liet, giai_thich, updated_at)`
- `answers(id, question_id FK, noi_dung, is_correct)` — xoá câu hỏi thì CASCADE xoá đáp án
- `traffic_signs(id, ma_bien, ten_bien, nhom_bien, anh_url, mo_ta)`
- `exam_sets(id, ten_de, so_cau, thoi_gian, nguong_dat)`
- `exam_set_questions(exam_set_id FK, question_id FK)`
- `users(id, ho_ten, email, anh_dai_dien, vai_tro, ngay_thi_du_kien)` — vai_tro ∈ {user, admin}
- `attempts(id, user_id FK, exam_set_id, so_cau_dung, ket_qua, ly_do_truot, thoi_gian_lam, ngay_thi)` — ket_qua ∈ {DAT, TRUOT}
- `user_answers(id, attempt_id FK, question_id FK, answer_id, dung_sai)` — bảng QUAN TRỌNG NHẤT, dùng để xem lại bài + phân tích điểm yếu
- `review_schedule(id, user_id FK, question_id FK, so_lan_sai, lan_on_tiep, khoang_cach)` — spaced repetition
- `notes(id, user_id FK, question_id FK, noi_dung)` — ghi chú cá nhân

### Ràng buộc toàn vẹn
- Mỗi `questions` phải có ĐÚNG một `answers` với `is_correct = true`.
- Lưu một lượt thi (`attempts` + nhiều `user_answers`) phải nằm trong MỘT transaction (dùng `@Transaction` trong DAO).

## 5. Quy tắc nghiệp vụ QUAN TRỌNG

### Chấm điểm (không được làm sai)
Thuật toán chấm gồm 2 bước, ĐÚNG THỨ TỰ:
1. Nếu thí sinh trả lời sai BẤT KỲ câu điểm liệt nào (`is_diem_liet = true`) → **TRƯỢT ngay**, `ly_do_truot = "Sai câu điểm liệt số X"`. Không xét tiếp.
2. Nếu không sai câu điểm liệt: xét `so_cau_dung >= nguong_dat` → ĐẠT, ngược lại → TRƯỢT với lý do "không đủ số câu đúng".

KHÔNG được chấm điểm chỉ bằng cách đếm số câu đúng. Luật điểm liệt là bắt buộc.

### Cấu trúc đề thi
Số câu / thời gian / ngưỡng đạt của đề hạng B lấy từ tệp cấu hình (`exam_sets`), KHÔNG hardcode rải rác trong code. (Nhóm đang xác minh lại thông số chính thức từ Cục CSGT — để dễ chỉnh.)

## 6. Quy ước code

- Đặt tên package theo tính năng: `data/`, `ui/<feature>/` (VD: `ui/exam/`, `ui/review/`, `ui/admin/`).
- Đặt tên biến, hàm, class bằng tiếng Anh theo chuẩn Java (class PascalCase, biến/hàm camelCase); chuỗi hiển thị và comment giải thích bằng tiếng Việt được.
- Mỗi màn hình: một Fragment/Activity + một ViewModel riêng.
- Dùng `ViewBinding`, KHÔNG dùng `findViewById`.
- Danh sách dùng `RecyclerView` + `ListAdapter` + `DiffUtil`.
- Xử lý trạng thái UI: dùng một class trạng thái (VD Resource<T> có các trạng thái Loading/Success/Error) phát qua LiveData.
- Room: dùng truy vấn trả `LiveData<>` để tự cập nhật UI; thao tác ghi chạy qua Executor.
- Mật khẩu: nếu tự quản lý thì hash bằng BCrypt (thư viện jBCrypt), TUYỆT ĐỐI không lưu thô. (Ưu tiên dùng Firebase Auth.)
- Bảo mật Firestore: có Security Rules — mỗi người chỉ đọc/ghi dữ liệu của chính mình; chỉ admin ghi được `questions`.

## 7. Khi sinh code, luôn:

- Giải thích ngắn gọn (bằng tiếng Việt) phần code làm gì, vì thi vấn đáp cần hiểu code.
- Ưu tiên giải pháp đơn giản, dễ đọc hơn là "thông minh" khó hiểu.
- Viết Java thuần, tương thích API phù hợp (dùng tính năng Java 8 như lambda, Stream ở mức vừa phải — nhóm phải đọc hiểu được).
- Không thêm thư viện mới nếu chưa hỏi — nhóm cần kiểm soát dependency.
- Tôn trọng phân tầng MVVM và nguyên tắc offline-first ở mục 2.
- Nếu một yêu cầu vi phạm kiến trúc (VD: đọc Firestore lúc làm bài), hãy cảnh báo thay vì làm theo.

## 8. Điều KHÔNG làm

- KHÔNG đọc câu hỏi trực tiếp từ Firestore khi người dùng đang làm bài/ôn tập.
- KHÔNG tải lại toàn bộ 600 câu mỗi lần đồng bộ (chỉ tải delta).
- KHÔNG để ảnh biển báo trên Firebase Storage.
- KHÔNG lưu mật khẩu dạng thô.
- KHÔNG truy vấn Room hay gọi Firebase trên luồng chính (UI thread).
- KHÔNG đặt logic nghiệp vụ (chấm điểm, đồng bộ) trong Activity/Fragment.
- KHÔNG hardcode số câu/thời gian đề thi rải rác trong code.
- KHÔNG dùng findViewById (dùng ViewBinding).
