# FINAL — TỔNG KẾT DỰ ÁN & PHÂN CÔNG CÔNG VIỆC

> Cập nhật: 23/07/2026. File này tổng hợp: (1) thông tin dự án, (2) tóm tắt tài liệu thiết kế, (3) trạng thái hoàn thành, (4) phân công lại công việc cho giai đoạn nước rút trước bảo vệ.

---

# I. THÔNG TIN DỰ ÁN

| Mục | Nội dung |
|---|---|
| Tên đề tài | Ứng dụng Android **Ôn thi lý thuyết sát hạch lái xe hạng B** |
| Môn học | Phát triển phần mềm cho thiết bị di động — Khoa CNTT, ĐH Sư phạm Hà Nội |
| Hình thức | Đồ án nhóm 5 người, **thi vấn đáp** |
| Bộ dữ liệu | **600 câu chính thức** của Cục CSGT (áp dụng từ 01/6/2025) — đã nạp đủ |
| Ngôn ngữ / Kiến trúc | Java · MVVM (View → ViewModel → Repository → Room/Firebase) |
| CSDL cục bộ | Room (SQLite) — 11 bảng, offline-first |
| Đám mây | Firebase Auth (tài khoản) + Cloud Firestore (hồ sơ, vai trò, delta câu hỏi) |
| SDK | compileSdk 36 · minSdk 24 · targetSdk 34 |
| Kích thước APK | ~17.6 MB (đã nén toàn bộ ảnh assets 27MB → 5MB) |

## Số liệu bộ câu hỏi

| Chương | Tên | Số câu | Có ảnh |
|---|---|---|---|
| 1 | Quy định chung và quy tắc giao thông đường bộ | 166 | 6 |
| 2 | Nghiệp vụ vận tải | 26 | 5 |
| 3 | Văn hóa giao thông và đạo đức người lái xe | 21 | 0 |
| 4 | Kỹ thuật lái xe | 56 | 5 |
| 5 | Cấu tạo và sửa chữa | 35 | 9 |
| 6 | Hệ thống biển báo | 182 | 181 |
| 7 | Giải thế sa hình và kỹ năng xử lý tình huống | 114 | 114 |
| **Tổng** | | **600** | **320** |

- **60 câu điểm liệt** đã đánh dấu theo danh sách chính thức: 45 câu chương 1, 4 câu chương 3, 11 câu chương 4.
- Cấu trúc đề thi hạng B (`ExamConfig`): **30 câu / 20 phút / đạt ≥ 27**, mỗi đề có câu điểm liệt; **sai 1 câu điểm liệt là TRƯỢT ngay** bất kể tổng điểm.
- `questions.json` đúng thứ tự 1→600 nên **id trong Room trùng số câu chính thức** — tra cứu, đối chiếu rất nhanh.

## Tài liệu liên quan trong repo

- `CLAUDE.md` — ngữ cảnh kiến trúc, quy ước code, nguyên tắc bắt buộc.
- `04-tai-lieu-thiet-ke-csdl.md` — đặc tả chi tiết 11 bảng Room, ràng buộc, truy vấn.
- `02-tai-lieu-thiet-ke.md`, `03-tai-lieu-dac-ta.md` — thiết kế & đặc tả tổng thể.
- `de_goc/` — 7 file docx nguồn của bộ 600 câu.
- `tools/convert_chuong.py` — công cụ sinh `questions.json` từ docx.

---

# II. TÓM TẮT THIẾT KẾ (những điểm sẽ bị hỏi khi vấn đáp)

## 2.1. Offline-first

Toàn bộ ôn tập / thi thử **chỉ đọc Room**, không gọi mạng. Mất mạng giữa lúc thi vẫn hoàn thành và lưu bài bình thường. Firebase chỉ dùng cho: đăng nhập, hồ sơ + vai trò, và **phát tán delta câu hỏi** do admin sửa.

## 2.2. Chấm điểm — 2 bước, KHÔNG được đảo thứ tự (`logic/ExamScorer`)

1. Sai **bất kỳ** câu điểm liệt nào → TRƯỢT ngay, ghi rõ lý do.
2. Không sai điểm liệt → xét `số câu đúng ≥ 27` → ĐẠT / TRƯỢT.

`ExamScorer` tách khỏi Android thuần Java → có **unit test** (`ExamScorerTest`, 3 test).

## 2.3. Đồng bộ delta câu hỏi (Room ↔ Firestore)

- **Chỉ chứa delta**: 600 câu gốc đóng trong APK, KHÔNG bao giờ đẩy lên mây.
- **Đẩy lên**: admin lưu → ghi Room trước (đóng dấu `updatedAt`) → set document `questions/{id}`.
- **Kéo về**: mọi máy hỏi `whereGreaterThan("updatedAt", mocKeoVe)` → upsert vào Room. Hai mốc thời gian (`moc_day_len`, `moc_keo_ve`) lưu SharedPreferences.
- **Điểm tinh tế nhất**: upsert dùng **UPDATE chứ không INSERT REPLACE** — REPLACE = DELETE+INSERT sẽ CASCADE xoá `user_answers` (lịch sử làm bài) của người dùng.
- Chặn vòng lặp đẩy–kéo: kéo về xong nâng luôn `moc_day_len`.

## 2.4. Ảnh câu hỏi — 2 nguồn, phân biệt bằng dấu `/`

- `anhUrl` **không** bắt đầu bằng `/` → tên file trong `assets/images/` (ảnh 600 câu gốc, offline sẵn).
- `anhUrl` bắt đầu bằng `/` → file cục bộ (ảnh admin thêm, đồng bộ qua Firestore dạng **Base64 nhúng trong document** — đúng ràng buộc *không dùng Firebase Storage*).
- Hiển thị chung một đường: `AnhUtil.docAnhCauHoi()`.

## 2.5. Phân quyền THẬT bằng Firestore Security Rules

Vai trò lưu tại `users/{uid}.vaiTro`. Rules chặn tự nâng quyền (kể cả dịch ngược APK); chỉ admin ghi được collection `questions`. Client chỉ **ẩn/hiện menu** — chốt chặn thật nằm ở máy chủ. Admin đầu tiên phải cấp tay từ Firebase Console (cố ý — chicken-and-egg).

## 2.6. Pipeline dữ liệu từ docx

`tools/convert_chuong.py <file.docx> <chương> [SO=CHISO...]` — nhận 4 kiểu đánh dấu đáp án của file nhóm (☑/☐, bôi highlight, chữ đỏ, chữ xanh), dấu ★ = điểm liệt, tách ảnh nhúng, gộp idempotent, **từ chối ghi khi dữ liệu bất thường**. Muốn sửa câu hỏi: sửa docx trong `de_goc/` rồi chạy lại script — KHÔNG sửa tay `questions.json`.

Sau khi đổi `questions.json` phải xoá database (gỡ app / xoá dữ liệu) vì seed chỉ chạy một lần lúc tạo `oto.db`.

---

# III. TRẠNG THÁI HOÀN THÀNH

## Đã xong ✅

| Tính năng | Ghi chú |
|---|---|
| Đăng nhập / đăng ký / quên mật khẩu | Firebase Auth |
| Ôn tập theo chương + nhóm câu điểm liệt | 600 câu, ảnh hiển thị ở Ôn tập & Thi |
| Thi thử 30 câu / 20 phút, luật điểm liệt | Đề ngẫu nhiên đảm bảo có câu điểm liệt |
| Tra cứu biển báo + Admin CRUD biển báo | Kèm ảnh camera/thư viện |
| Admin CRUD câu hỏi + nút "Đồng bộ ngay" | Kèm ảnh minh hoạ |
| Đồng bộ delta câu hỏi đa thiết bị | Đã test round-trip trên máy thật |
| Quản trị người dùng (đổi vai trò) | Qua Firestore + Rules |
| Thống kê tỷ lệ đúng theo chương | BarChartView tự vẽ |
| Cá nhân: avatar, ngày thi dự kiến, đếm ngược | |
| Unit test | ExamScorer (3) + QuestionFirestoreMapper (10) |

## Việc còn lại trước bảo vệ ⏳

| Việc | Mô tả |
|---|---|
| Dọn Firestore | Xoá document test cũ trong collection `questions` trên Console |
| Rà soát nội dung 600 câu | Soát xác suất đáp án + ảnh, đặc biệt ảnh câu 72–73 chương 1 |
| Kiểm thử tổng thể | Đi qua toàn bộ luồng trên ≥ 2 máy trước bảo vệ |

## Ngoài phạm vi (nhóm chốt ngày 23/07/2026) 🚫

| Hạng mục | Lý do & cách trả lời khi vấn đáp |
|---|---|
| **Lịch sử thi theo từng tài khoản** | Màn cũ đã gỡ vì lỗi mọi user dùng chung `LOCAL_USER_ID`. Trình bày là **hướng phát triển**: schema đã sẵn sàng (`attempts`, `user_answers` vẫn được ghi đúng trong transaction), chỉ cần gắn uid Firebase và dựng UI |
| **Ôn lại câu sai** | `AttemptDao.getWrongQuestionIds()` đã viết sẵn — hướng phát triển, thiếu mỗi UI |
| **Ghi chú / lịch ôn lại** (`notes`, `review_schedule`) | Schema dự phòng cho hướng phát triển (spaced repetition). Giữ entity + DAO trong schema, nói rõ khi vấn đáp là "thiết kế sẵn, chưa làm UI" |

---

# IV. PHÂN CÔNG CÔNG VIỆC — THEO CHỨC NĂNG

Mỗi chức năng có **một người chịu trách nhiệm chính** (code + trả lời vấn đáp về phần đó). Chức năng *Lịch sử thi & Ôn lại câu sai* đã **cắt khỏi phạm vi** (xem mục III). Bảng tổng quan:

| # | Chức năng | Phụ trách | Trạng thái | Hạn chót |
|---|---|---|---|---|
| 1 | Xác thực & tài khoản cá nhân | **Trường** | ✅ còn kiểm thử | **25/07** |
| 2 | Đồng bộ câu hỏi Firestore | **Trường** | ✅ còn dọn data + test 2 máy | **26/07** |
| 3 | Ngân hàng câu hỏi & CSDL | **Hậu** | ✅ còn rà soát nội dung | **27/07** |
| 4 | Quản trị (câu hỏi, biển báo, người dùng) | **Hậu** | ✅ còn kiểm thử | **26/07** |
| 5 | Ôn tập & Tra cứu biển báo | **An** | ✅ còn soát UI | **26/07** |
| 6 | Thi thử & mô phỏng kỳ thi | **Dương** | ✅ còn kiểm thử | **26/07** |
| 7 | Chấm điểm & Thống kê | **Long** | ✅ còn kiểm thử | **26/07** |

> Lịch chung: **28/07** gom & sửa lỗi tìm được · **29/07** slide + đóng băng code · **30/07** tổng duyệt vấn đáp. Nếu ngày bảo vệ khác dự kiến, cả nhóm dời lịch tương ứng (giữ nguyên khoảng cách giữa các mốc).

## 1. Xác thực & tài khoản cá nhân — Trường

- **Gồm:** đăng nhập / đăng ký / quên mật khẩu (`auth/`), vai trò (`VaiTro`), hồ sơ + avatar + ngày thi dự kiến (`CaNhanActivity`, `AnhUtil`).
- **Việc còn lại:**
  - Xác nhận Firestore Rules bản mới nhất đã Publish — **hạn 24/07**.
  - Test đăng nhập/đăng xuất chéo 2 tài khoản trên 1 máy (vai trò không dính nhau) — **hạn 25/07**.
- **Vấn đáp:** luồng Firebase Auth; vì sao vai trò cache trong SharedPreferences nhưng chốt chặn thật ở Rules; admin đầu tiên cấp từ Console.

## 2. Đồng bộ câu hỏi Firestore — Trường

- **Gồm:** `QuestionSyncRepository`, `QuestionFirestoreMapper` (+10 unit test), nút "Đồng bộ ngay", pull im lặng ở trang chủ.
- **Việc còn lại:**
  - Xoá document test cũ trong collection `questions` trên Console — **hạn 24/07** (làm sớm kẻo máy khác cài mới kéo nhầm).
  - Test đồng bộ **2 máy thật**: thêm câu kèm ảnh ở máy 1 → máy 2 nhận — **hạn 26/07**.
- **Vấn đáp:** delta bằng `whereGreaterThan(updatedAt)` + 2 mốc; vì sao upsert dùng UPDATE chứ không REPLACE (CASCADE mất `user_answers`); ảnh nhúng Base64 thay vì Storage.

## 3. Ngân hàng câu hỏi & CSDL — Hậu

- **Gồm:** 11 bảng Room (`data/entity`, `data/dao`), `DatabaseSeeder` 7 chương, `questions.json` 600 câu, pipeline `tools/convert_chuong.py`, `de_goc/`.
- **Việc còn lại:**
  - Rà soát xác suất ~10 câu/chương (đáp án đúng, ảnh khớp câu) — **hạn 27/07**.
  - Xác nhận/thay ảnh câu 72–73 chương 1 (docx gốc dùng chung 1 ảnh) — **hạn 25/07**.
  - Soát 60 câu điểm liệt trên app khớp danh sách chính thức — **hạn 26/07**.
- **Vấn đáp:** ràng buộc CASCADE + `@Transaction` khi lưu lượt thi; migration tường minh vs destructive; quy trình sửa câu hỏi (sửa docx → chạy script → xoá DB seed lại).

## 4. Quản trị — Hậu

- **Gồm:** CRUD câu hỏi (`AdminActivity`, `SuaCauHoiActivity` — kèm ảnh), CRUD biển báo (`AdminBienBaoActivity`, `SuaBienBaoActivity`), quản trị người dùng (`AdminNguoiDungActivity` — đổi vai trò).
- **Việc còn lại:**
  - Test thêm/sửa/xoá có ảnh khi có mạng và **mất mạng** (Toast "đã lưu máy, chưa đồng bộ") — **hạn 26/07**.
  - Test user thường không vào được màn admin (cả 3 màn) — **hạn 26/07**.
- **Vấn đáp:** vì sao chặn admin cả ở client lẫn Rules; luồng ảnh camera qua FileProvider; dọn file ảnh rác khi thay/hủy.

## 5. Ôn tập & Tra cứu biển báo — An

- **Gồm:** `OnTapActivity` (7 chương + nhóm điểm liệt, hiện ảnh), `BienBaoActivity` + `ChiTietBienBaoActivity` (lọc nhóm, tìm kiếm), toàn bộ polish giao diện.
- **Việc còn lại:**
  - Đi UI hết 7 chương: ảnh đúng, layout không vỡ với câu dài / ảnh to, dark mode — **hạn 26/07**.
  - Màn "Các câu điểm liệt" hiển thị đủ 60 câu — **hạn 25/07**.
  - Trạng thái loading/empty các danh sách — **hạn 26/07**.
- **Vấn đáp:** MVVM tầng View (ViewBinding, LiveData, vì sao ViewModel không giữ Context); RecyclerView + ListAdapter + DiffUtil.

## 6. Thi thử & mô phỏng kỳ thi — Dương

- **Gồm:** `ThiActivity` + `ThiViewModel` (sinh đề ngẫu nhiên có điểm liệt, đếm ngược, tự nộp), `ExamConfig` (30/20/27), `KetQuaActivity`.
- **Việc còn lại:**
  - Test hết giờ tự nộp / thoát giữa chừng / xoay màn hình — **hạn 26/07**.
  - Cố tình sai câu điểm liệt → phải TRƯỢT ngay kèm lý do — **hạn 25/07**.
  - (Tuỳ chọn, nếu kịp trước 28/07) đọc ngưỡng đạt từ `exam_sets` thay hằng số.
- **Vấn đáp:** cách sinh đề không trùng câu và đảm bảo điểm liệt; CountDownTimer; lưu lượt thi 1 transaction.

## 7. Chấm điểm & Thống kê — Long

- **Gồm:** `logic/ExamScorer` (+3 unit test), `ThongKeActivity` + truy vấn `getChapterStats` (JOIN 4 bảng), `BarChartView`.
- **Việc còn lại:**
  - Kiểm thử thống kê sau ≥ 3 lượt thi thật — **hạn 26/07**.
  - Nút "ôn chương yếu nhất" nhảy đúng chương — **hạn 26/07**.
  - Hỗ trợ kiểm thử chéo các chức năng khác (Long rảnh nhất sau khi cắt mục lịch sử) — **25–27/07**.
- **Vấn đáp:** thuật toán 2 bước KHÔNG đảo thứ tự; vì sao tách thuần Java để unit test; đọc truy vấn thống kê; trả lời được vì sao lịch sử thi / ôn câu sai để lại làm **hướng phát triển** (schema + DAO đã sẵn, xem mục III).

## Việc chung cả nhóm

| Hạn | Việc |
|---|---|
| **27/07** | Mỗi người đi hết luồng phần mình phụ trách trên máy thật, ghi lỗi vào nhóm chat |
| **28/07** | Gom & sửa toàn bộ lỗi tìm được; đọc mục II — **ai cũng phải trả lời được 2.1 và 2.2** |
| **29/07** | Hoàn thành slide (kiến trúc mục II, demo trực tiếp, số liệu mục I); **đóng băng code** |
| **30/07** | Tổng duyệt vấn đáp chéo: mỗi người hỏi người khác 3 câu về phần không phải của mình |

---

*File này do nhóm cập nhật khi phân công thay đổi. Trạng thái chi tiết xem lịch sử commit.*
