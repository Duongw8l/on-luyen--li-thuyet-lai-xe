# Ứng dụng Ôn thi Lý thuyết Sát hạch Lái xe hạng B

Ứng dụng Android hỗ trợ ôn luyện và mô phỏng kỳ thi lý thuyết sát hạch giấy phép lái xe **hạng B** theo bộ **600 câu hỏi** của Cục Cảnh sát giao thông (áp dụng từ 01/6/2025). Ứng dụng hoạt động **hoàn toàn ngoại tuyến** cho phần ôn tập và thi thử; chỉ cần mạng cho đăng nhập/đăng ký tài khoản.

> Đồ án môn **Phát triển phần mềm cho thiết bị di động** — Khoa Công nghệ thông tin, Trường Đại học Sư phạm Hà Nội.

## Tính năng

- **Ôn tập theo chương** — duyệt bộ câu hỏi phân theo 6 chương.
- **Thi thử** — mô phỏng kỳ thi thật: giới hạn thời gian, chấm điểm đúng quy chế, **bao gồm luật câu điểm liệt** (sai 1 câu điểm liệt là trượt).
- **Tra cứu biển báo** — tra cứu hệ thống biển báo giao thông kèm mô tả.
- **Ôn lại câu sai** — tự động ghi nhận và gợi ý ôn lại các câu trả lời sai.
- **Thống kê** — phân tích điểm yếu theo từng chương, biểu đồ cột.
- **Lịch sử làm bài** — lưu lại các lần thi và kết quả.
- **Tài khoản cá nhân** — hồ sơ người dùng, ảnh đại diện (chụp ảnh hoặc chọn từ thư viện).
- **Quản trị viên (Admin)** — cập nhật ngân hàng câu hỏi và biển báo khi quy định thay đổi.

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
├── data/          Room database, DAO, entity, repository, seed dữ liệu mẫu
│   ├── dao/
│   ├── entity/
│   └── relation/
├── logic/         Chấm điểm kỳ thi (ExamScorer, ExamResult)
├── ui/            Các Activity & Adapter: ôn tập, thi, kết quả, biển báo,
│                  thống kê, lịch sử, cá nhân, quản trị
├── util/          Tiện ích (xử lý ảnh)
└── MainActivity.java
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

> Lần chạy đầu, ứng dụng tự đổ **dữ liệu mẫu** (chương, câu hỏi, biển báo, bộ đề) để demo. Bộ 600 câu hỏi chính thức sẽ được nạp qua script seed từ file JSON.

## Phân quyền Firestore

Quy tắc phân quyền được lưu tại [`firestore.rules`](firestore.rules) — bản sao của rules cấu hình trên Firebase Console, phân tách quyền giữa **user** và **admin**.

## Tài liệu

- [`02-tai-lieu-thiet-ke.md`](02-tai-lieu-thiet-ke.md) — Tài liệu thiết kế
- [`03-tai-lieu-dac-ta.md`](03-tai-lieu-dac-ta.md) — Tài liệu đặc tả
- [`01-checklist-doi-chieu-rubric.md`](01-checklist-doi-chieu-rubric.md) — Checklist đối chiếu rubric

## Nhóm thực hiện

| Thành viên | Vai trò |
|---|---|
| Trường | Nhóm trưởng — Tài khoản, Firebase, Đồng bộ dữ liệu |
| Hậu | Ngân hàng câu hỏi, Cơ sở dữ liệu, Quản trị viên |
| An | Ôn tập, Tra cứu biển báo, Giao diện |
| Dương | Thi thử, Mô phỏng kỳ thi |
| Long | Chấm điểm, Thống kê, Ôn lại câu sai |
