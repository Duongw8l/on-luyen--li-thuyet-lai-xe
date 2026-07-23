# Ứng dụng Ôn thi Lý thuyết Sát hạch Lái xe hạng B

Ứng dụng Android hỗ trợ ôn luyện và mô phỏng kỳ thi lý thuyết sát hạch giấy phép lái xe **hạng B** theo bộ **600 câu hỏi** của Cục Cảnh sát giao thông (áp dụng từ 01/6/2025). Ứng dụng hoạt động **hoàn toàn ngoại tuyến** cho phần ôn tập và thi thử; chỉ cần mạng cho đăng nhập/đăng ký tài khoản.

> Đồ án môn **Phát triển phần mềm cho thiết bị di động** — Khoa Công nghệ thông tin, Trường Đại học Sư phạm Hà Nội.

## Tính năng

- **Ôn tập theo chương** — đủ **600 câu chính thức** phân theo 7 chương (320 câu kèm ảnh biển báo/sa hình), riêng nhóm **60 câu điểm liệt**.
- **Thi thử** — mô phỏng kỳ thi thật hạng B: 30 câu / 20 phút / đạt ≥ 27, chấm đúng quy chế **bao gồm luật câu điểm liệt** (sai 1 câu điểm liệt là trượt ngay).
- **Tra cứu biển báo** — tra cứu hệ thống biển báo giao thông kèm ảnh và mô tả.
- **Thống kê** — phân tích điểm yếu theo từng chương, biểu đồ cột, nhảy thẳng tới ôn chương yếu nhất.
- **Tài khoản cá nhân** — hồ sơ người dùng, ảnh đại diện (chụp ảnh hoặc chọn từ thư viện), đếm ngược ngày thi.
- **Quản trị viên (Admin)** — cập nhật ngân hàng câu hỏi (kèm ảnh) và biển báo; quản lý vai trò người dùng.
- **Đồng bộ đa thiết bị** — câu hỏi admin thêm/sửa được đồng bộ delta qua Firestore về mọi máy; ôn tập/thi vẫn hoàn toàn offline.

### Hướng phát triển

- **Lịch sử làm bài** và **Ôn lại câu sai** — lược đồ CSDL (`attempts`, `user_answers`) và truy vấn đã sẵn sàng, sẽ bổ sung giao diện ở phiên bản sau.
- **Ghi chú cá nhân & lịch ôn lại ngắt quãng** (`notes`, `review_schedule`) — đã thiết kế sẵn trong schema.

## Công nghệ sử dụng

| Thành phần | Lựa chọn |
|---|---|
| Ngôn ngữ | Java |
| Kiến trúc | MVVM (ViewModel + LiveData) |
| CSDL cục bộ | Room (SQLite) — dữ liệu offline |
| Xác thực | Firebase Authentication |
| Hồ sơ / vai trò | Cloud Firestore (user / admin) |
| Giao diện | AppCompat, Material Components, ConstraintLayout, RecyclerView |
| Build | Gradle (Kotlin DSL) + Version Catalog |

- `compileSdk` 36 · `minSdk` 24 · `targetSdk` 34
- `applicationId`: `com.example.oto`

## Cấu trúc mã nguồn

```
app/src/main/java/com/example/oto/
├── auth/          Đăng nhập, đăng ký, quên mật khẩu, quản lý vai trò (Firebase)
├── data/          Room database, DAO, entity, repository, seed, đồng bộ Firestore
│   ├── dao/
│   ├── entity/
│   └── relation/
├── logic/         Chấm điểm kỳ thi (ExamScorer, ExamResult)
├── ui/            Các Activity & Adapter: ôn tập, thi, kết quả, biển báo,
│   └── viewmodel/ thống kê, cá nhân, quản trị — mỗi màn một ViewModel
├── util/          Tiện ích (xử lý ảnh)
└── MainActivity.java

de_goc/            7 file docx nguồn của bộ 600 câu
tools/             convert_chuong.py — sinh assets/questions.json từ docx
```

## Yêu cầu môi trường

- Android Studio (bản mới) + Android SDK (API 34+)
- JDK 11
- Một dự án Firebase và file `app/google-services.json` tương ứng (cần cho đăng nhập)

## Cài đặt và chạy

1. Clone dự án:
   ```bash
   git clone <URL-repo>
   ```
2. Mở thư mục dự án bằng Android Studio và để Gradle sync tự động.
3. Đảm bảo có file `app/google-services.json` (tải từ Firebase Console).
4. Chạy trên máy ảo hoặc thiết bị thật:
   ```bash
   ./gradlew installDebug
   ```
   hoặc bấm **Run** trong Android Studio.

> Lần chạy đầu, ứng dụng tự đổ **đủ 600 câu hỏi chính thức** (7 chương, 60 câu điểm liệt, 320 câu kèm ảnh) từ `assets/questions.json` vào Room. Muốn sửa nội dung câu hỏi: sửa file docx trong `de_goc/` rồi chạy `python tools/convert_chuong.py <file.docx> <số chương>` — KHÔNG sửa tay questions.json. Sau khi đổi dữ liệu phải gỡ app cài lại (seed chỉ chạy lúc tạo database).

## Phân quyền Firestore

Quy tắc phân quyền được lưu tại [`firestore.rules`](firestore.rules) — bản sao của rules cấu hình trên Firebase Console, phân tách quyền giữa **user** và **admin**.

## Tài liệu

- [`02-tai-lieu-thiet-ke.md`](02-tai-lieu-thiet-ke.md) — Tài liệu thiết kế
- [`03-tai-lieu-dac-ta.md`](03-tai-lieu-dac-ta.md) — Tài liệu đặc tả
- [`04-tai-lieu-thiet-ke-csdl.md`](04-tai-lieu-thiet-ke-csdl.md) — Tài liệu thiết kế cơ sở dữ liệu
- [`01-checklist-doi-chieu-rubric.md`](01-checklist-doi-chieu-rubric.md) — Checklist đối chiếu rubric
- [`final.md`](final.md) — Tổng kết dự án, trạng thái và phân công công việc

## Nhóm thực hiện

| Thành viên | Vai trò |
|---|---|
| Trường | Nhóm trưởng — Tài khoản, Firebase, Đồng bộ dữ liệu |
| Hậu | Ngân hàng câu hỏi, Cơ sở dữ liệu, Quản trị viên |
| An | Ôn tập, Tra cứu biển báo, Giao diện |
| Dương | Thi thử, Mô phỏng kỳ thi |
| Long | Chấm điểm, Thống kê |
