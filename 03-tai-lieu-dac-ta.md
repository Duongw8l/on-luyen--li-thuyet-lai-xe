# TÀI LIỆU ĐẶC TẢ YÊU CẦU PHẦN MỀM (SRS)

# ỨNG DỤNG ÔN THI LÝ THUYẾT SÁT HẠCH LÁI XE HẠNG B

**Môn:** Phát triển phần mềm cho thiết bị di động
**Nhóm:** 5 thành viên — Trường (nhóm trưởng), Hậu, An, Dương, Long
**Phiên bản tài liệu:** 1.0
**Ngày cập nhật:** 14/07/2026

---

## Mục đích của tài liệu này

Tài liệu **thiết kế** (`02-tai-lieu-thiet-ke.md`) mô tả hệ thống **dự định xây dựng**. Tài liệu **đặc tả** này mô tả hệ thống **đã xây dựng thực tế**: từng chức năng nhận đầu vào gì, xử lý ra sao, cho ra kết quả gì, và bị ràng buộc bởi quy tắc nào.

Mọi chức năng liệt kê ở Mục III đều **đã cài đặt và đã chạy thử trên máy ảo Android**. Những gì chưa làm được đặt riêng ở Mục VIII — Giới hạn hiện tại, để tránh tình trạng "tài liệu hứa nhiều hơn sản phẩm".

Ký hiệu mã yêu cầu: `FR` = yêu cầu chức năng (Functional Requirement), `NFR` = yêu cầu phi chức năng.

---

# I. TỔNG QUAN HỆ THỐNG

## 1.1. Tác nhân

| Tác nhân | Mô tả | Cách hệ thống nhận biết |
|---|---|---|
| **Khách (chưa đăng nhập)** | Dùng app ở chế độ ngoại tuyến | Không có phiên Firebase Auth |
| **Học viên** | Người ôn thi, đã có tài khoản | Firestore `users/{uid}.vaiTro = "user"` |
| **Quản trị viên** | Quản lý ngân hàng câu hỏi, biển báo | Firestore `users/{uid}.vaiTro = "admin"` |

Vai trò **không** do ứng dụng tự quyết định. Ứng dụng chỉ đọc vai trò từ máy chủ Firestore (xem FR-05).

## 1.2. Kiến trúc thực tế

```
┌──────────────────────────────────────────────────────┐
│ TẦNG GIAO DIỆN — 16 Activity                         │
│ (Đăng nhập, Trang chủ, Ôn tập, Thi, Kết quả,         │
│  Biển báo, Thống kê, Lịch sử, Cá nhân, 4 màn Admin…) │
└───────────────────────┬──────────────────────────────┘
                        │ gọi
┌───────────────────────▼──────────────────────────────┐
│ TẦNG NGHIỆP VỤ                                       │
│ • ExamScorer  — chấm điểm theo quy chế (thuần Java,  │
│                 tách khỏi Android để unit test được) │
│ • VaiTro      — phân quyền user / admin              │
│ • AuthManager — bọc Firebase Authentication          │
│ • AnhUtil     — nén, xoay, lưu ảnh đại diện          │
└───────────────────────┬──────────────────────────────┘
                        │
┌───────────────────────▼──────────────────────────────┐
│ TẦNG DỮ LIỆU                                         │
│ QuizRepository — "một cửa" điều phối nguồn dữ liệu   │
│        ↙                              ↘              │
│  Room (SQLite) — 11 bảng        Firebase             │
│  • câu hỏi, đáp án, biển báo    • Authentication     │
│  • lượt thi, chi tiết trả lời   • Firestore (vai trò)│
│  • người dùng, ngày thi                              │
│  → TOÀN BỘ HOẠT ĐỘNG OFFLINE                         │
└──────────────────────────────────────────────────────┘
```

**Nguyên tắc:** dữ liệu học tập (câu hỏi, bài thi, thống kê) nằm hoàn toàn trong Room nên chạy được khi không có mạng. Firebase chỉ phụ trách **tài khoản và vai trò** — hai thứ bắt buộc phải có máy chủ mới đáng tin.

## 1.3. Công nghệ đã dùng

| Thành phần | Công nghệ thực tế |
|---|---|
| Ngôn ngữ | Java |
| minSdk / targetSdk | 24 / 34 |
| CSDL cục bộ | Room (SQLite), 11 bảng |
| Xác thực | Firebase Authentication (Email/Password) |
| CSDL đám mây | Cloud Firestore (chỉ lưu hồ sơ + vai trò) |
| Lưu ảnh đại diện | Bộ nhớ riêng của ứng dụng (`filesDir`), nén JPEG |
| Gửi email | Firebase Auth (đặt lại mật khẩu, xác minh) + Intent `ACTION_SEND` / `ACTION_SENDTO` |
| Biểu đồ | `BarChartView` — View tự vẽ bằng Canvas, **không dùng thư viện ngoài** |
| Giao diện | Material 3, DayNight (hỗ trợ Dark mode) |

> **Ghi chú:** tài liệu thiết kế ban đầu dự kiến dùng MPAndroidChart và Firebase Storage. Thực tế nhóm tự vẽ biểu đồ (giảm phụ thuộc, dễ giải thích thuật toán vẽ) và lưu ảnh trong bộ nhớ trong (rubric cho phép "Firebase Storage **hoặc** bộ nhớ trong").

---

# II. THÔNG SỐ KỲ THI

Toàn bộ thông số đề thi tập trung trong lớp `ExamConfig` để sửa một chỗ là đổi toàn hệ thống:

| Thông số | Giá trị hiện tại | Trạng thái |
|---|---|---|
| Số câu mỗi đề | 25 | ⚠️ **CHỜ XÁC MINH** từ nguồn Cục CSGT |
| Thời gian làm bài | 19 phút | ⚠️ **CHỜ XÁC MINH** |
| Ngưỡng đạt | 21 câu đúng | ⚠️ **CHỜ XÁC MINH** |
| Số câu điểm liệt tối thiểu trong đề | 1 | ⚠️ **CHỜ XÁC MINH** |

> **Đây là rủi ro nghiệp vụ lớn nhất còn lại.** Nếu chấm sai quy chế, giảng viên chỉ cần hỏi một câu là lộ. Cả nhóm phải xác minh 4 con số này từ nguồn chính thức trước ngày bảo vệ.

---

# III. ĐẶC TẢ YÊU CẦU CHỨC NĂNG

## Nhóm A — Tài khoản và phân quyền

### FR-01. Đăng ký tài khoản

| | |
|---|---|
| **Tác nhân** | Khách |
| **Đầu vào** | Họ tên, email, mật khẩu, nhập lại mật khẩu |
| **Xử lý** | Kiểm tra hợp lệ → tạo tài khoản trên Firebase Auth → lưu họ tên vào hồ sơ → **gửi email xác minh** → tạo document `users/{uid}` trên Firestore với `vaiTro = "user"` |
| **Đầu ra** | Hộp thoại "Đăng ký thành công. Email xác minh đã gửi tới …" |
| **Ràng buộc** | Họ tên không rỗng; email đúng định dạng; mật khẩu ≥ 6 ký tự; hai mật khẩu phải khớp; email chưa từng đăng ký |
| **Lỗi** | Thông báo tiếng Việt (VD: "Email này đã được đăng ký") |

### FR-02. Đăng nhập

| | |
|---|---|
| **Đầu vào** | Email, mật khẩu |
| **Xử lý** | Xác thực qua Firebase Auth → đồng bộ họ tên/email xuống Room → **đọc vai trò từ Firestore** → vào Trang chủ |
| **Ràng buộc** | Mật khẩu **không bao giờ** được ứng dụng lưu; Firebase giữ và băm phía máy chủ |
| **Ghi nhớ phiên** | Đã đăng nhập lần trước thì mở app vào thẳng Trang chủ |

### FR-03. Quên mật khẩu — gửi email đặt lại ⭐

| | |
|---|---|
| **Đầu vào** | Email đã đăng ký |
| **Xử lý** | Gọi `FirebaseAuth.sendPasswordResetEmail()` |
| **Đầu ra** | Khung xanh: "Đã gửi email đặt lại mật khẩu tới … Hãy mở hộp thư (kiểm tra cả mục Spam)…" |
| **Ghi chú** | Đây là chức năng gửi email chính, đáp ứng trực tiếp tiêu chí rubric 2.5 |

### FR-04. Dùng offline / Đăng xuất

Khách có thể bấm "Dùng offline, không đăng nhập" để vào thẳng Trang chủ — toàn bộ phần ôn tập, thi thử, tra cứu, thống kê vẫn hoạt động. Khi đăng xuất, ứng dụng **xoá vai trò đã nhớ trong máy**, tránh việc người dùng sau vẫn thấy chức năng quản trị.

### FR-05. Phân quyền Học viên / Quản trị viên ⭐

| | |
|---|---|
| **Nguồn vai trò** | Firestore, document `users/{uid}`, field `vaiTro` ∈ {`user`, `admin`} |
| **Bộ nhớ đệm** | Vai trò đọc được lưu vào SharedPreferences → biết quyền ngay cả khi offline |
| **Cấp quyền admin** | Chỉ làm được từ Firebase Console (sửa tay field `vaiTro`) |

**Bảo vệ ba tầng** (điểm cần nêu khi vấn đáp):

1. **Tầng giao diện** — người dùng thường không nhìn thấy hai mục "Quản trị câu hỏi" / "Quản trị biển báo" trong menu.
2. **Tầng chức năng** — cả 4 màn quản trị đều kiểm tra vai trò ngay đầu `onCreate`; không phải admin thì **đóng màn hình ngay** và báo "Chức năng này chỉ dành cho quản trị viên". Ẩn menu chỉ là mỹ quan; đây mới là chốt chặn thật.
3. **Tầng máy chủ** — Firestore Security Rules chặn client tự sửa vai trò của mình:

```
allow create: if request.auth.uid == uid && request.resource.data.vaiTro == 'user';
allow update: if request.auth.uid == uid
              && request.resource.data.vaiTro == resource.data.vaiTro;
```

Nghĩa là **kể cả người dùng dịch ngược ứng dụng và gọi thẳng Firestore, họ vẫn không thể tự phong mình làm admin.**

### FR-06. Ảnh đại diện ⭐

| | |
|---|---|
| **Nguồn ảnh** | (a) Thư viện ảnh — Intent ngầm `ACTION_GET_CONTENT`; (b) Camera — Intent ngầm `ACTION_IMAGE_CAPTURE` |
| **Quyền** | Camera cần **runtime permission** `CAMERA`. Thư viện **không** cần quyền vì hệ thống chỉ trả về đúng tấm ảnh người dùng chọn (cách Google khuyến nghị từ Android 13) |
| **Xử lý ảnh** | Đọc ảnh → thu nhỏ cạnh dài về ≤ 512px → xoay đúng hướng theo thẻ **EXIF** → nén **JPEG chất lượng 80** → ghi vào `filesDir/anh_dai_dien.jpg` → lưu đường dẫn vào bảng `users` |
| **Hiển thị** | Khung tròn ở màn Cá nhân và ở đầu Trang chủ |
| **Kết quả nén đo được** | Ảnh 75 KB → **9.985 bytes**; ảnh chụp camera → **6.367 bytes** |
| **Kỹ thuật** | Dùng `FileProvider` để đưa file cho app Camera ghi vào (Android cấm chia sẻ `file://` từ API 24) |

## Nhóm B — Ôn tập

### FR-07. Ôn tập theo chương

Người dùng chọn chương bằng **Spinner** → duyệt từng câu (Câu trước / Câu tiếp) → chọn đáp án bằng **RadioGroup** → bấm "Kiểm tra": đáp án đúng tô **xanh**, đáp án chọn sai tô **đỏ**, hiện **giải thích**. Câu điểm liệt có nhãn cảnh báo "⚠ ĐIỂM LIỆT".

### FR-08. Ôn tập nhóm câu điểm liệt

Lọc riêng các câu có `is_diem_liet = 1`, dùng chung màn ôn tập (ẩn Spinner chọn chương).

### FR-09. Ôn thẳng chương yếu nhất

Từ màn Thống kê, bấm "Ôn ngay chương yếu nhất" → Intent tường minh mở màn Ôn tập và **tự chọn sẵn đúng chương đang yếu**.

## Nhóm C — Thi thử

### FR-10. Sinh đề ngẫu nhiên

| | |
|---|---|
| **Xử lý** | Bốc trước đủ số câu điểm liệt yêu cầu → bốc tiếp các câu ngẫu nhiên khác cho đủ `SO_CAU`, **không trùng lặp** (dùng `LinkedHashMap` khử trùng theo id) |
| **Ràng buộc** | Đề luôn có ít nhất `SO_CAU_DIEM_LIET` câu điểm liệt — giống đề thi thật |

### FR-11. Làm bài và đồng hồ đếm ngược

Đồng hồ số + **ProgressBar** giảm dần theo thời gian còn lại. **Hết giờ → tự động nộp bài** (không cần người dùng bấm gì). Lựa chọn đáp án được nhớ khi chuyển qua lại giữa các câu.

### FR-12. Nộp bài

Có **hộp thoại xác nhận**; nếu còn câu chưa trả lời thì cảnh báo rõ số câu ("Bạn còn 7 câu chưa trả lời. Vẫn nộp bài?").

### FR-13. Chấm điểm theo đúng quy chế ⭐⭐

Đây là thuật toán nghiệp vụ cốt lõi (`ExamScorer`), gồm **hai bước, thứ tự không được đảo**:

```
Bước 1 — Kiểm tra câu điểm liệt:
    NẾU sai BẤT KỲ câu điểm liệt nào
        → TRƯỢT NGAY, dừng, không xét tiếp
        → Lý do: "Trả lời sai câu điểm liệt số X"

Bước 2 — Xét ngưỡng điểm:
    NẾU số câu đúng >= NGUONG_DAT → ĐẠT
    NGƯỢC LẠI → TRƯỢT
        → Lý do: "Chỉ đúng X/Y câu, chưa đạt ngưỡng Z câu"
```

**Hệ quả nghiệp vụ:** thí sinh đúng gần hết vẫn có thể trượt nếu sai một câu điểm liệt. Ứng dụng phải nói rõ điều đó, không được chỉ đếm số câu đúng.

`ExamScorer` được tách khỏi Android (thuần Java) nên **kiểm thử được bằng unit test** — xem Mục IX.

### FR-14. Màn hình Kết quả

Hiện **ĐẠT** (xanh) / **TRƯỢT** (đỏ), số câu đúng, thời gian làm bài, và **lý do trượt nổi bật** ("❌ TRƯỢT DO CÂU ĐIỂM LIỆT"). Có nút **Chia sẻ kết quả** qua Intent ngầm `ACTION_SEND` (Zalo, Messenger, email…).

### FR-15. Lưu lượt thi — giao dịch

Một lượt thi gồm 1 bản ghi `attempts` + N bản ghi `user_answers`, được lưu trong **MỘT transaction**: hoặc lưu hết, hoặc không lưu gì. Không bao giờ tồn tại lượt thi thiếu chi tiết câu trả lời.

## Nhóm D — Tra cứu biển báo

### FR-16. Danh sách, lọc, tìm kiếm

**RecyclerView** danh sách biển báo, kết hợp đồng thời hai bộ lọc trong **một truy vấn SQL**: **Spinner** chọn nhóm biển (Cấm / Nguy hiểm / Hiệu lệnh / Chỉ dẫn / Phụ) và ô **tìm kiếm theo tên hoặc mã biển** (VD gõ "P.10" → còn P.101, P.102).

### FR-17. Chi tiết biển báo

Intent tường minh mở màn chi tiết: ảnh, mã, tên, nhóm, mô tả. Có nút **"Xem văn bản luật gốc"** → Intent ngầm `ACTION_VIEW` mở trình duyệt, và nút chia sẻ.

## Nhóm E — Thống kê và Lịch sử

### FR-18. Biểu đồ tỷ lệ đúng theo chương ⭐

| | |
|---|---|
| **Dữ liệu** | Truy vấn SQL JOIN `user_answers` × `attempts` × `questions` × `chapters`, gom nhóm theo chương |
| **Hiển thị** | `BarChartView` **tự vẽ bằng Canvas**: cột ngang, màu theo mức độ — **đỏ < 50%**, **cam 50–79%**, **xanh ≥ 80%** |
| **Phân tích** | Tự tìm chương có tỷ lệ đúng thấp nhất và gợi ý: "Chương yếu nhất: Chương 2 … (chỉ đúng 0%). Nên ôn lại chương này trước." |

### FR-19. Gửi báo cáo tiến độ qua email

Nút trên màn Thống kê → Intent ngầm `ACTION_SENDTO` (`mailto:`) mở app email với nội dung điền sẵn: tỷ lệ đúng từng chương + chương cần ôn thêm.

### FR-20. Lịch sử làm bài

**RecyclerView** các lượt thi (kết quả, ngày giờ, số câu đúng, thời gian, lý do trượt). Có:
- **Lọc theo khoảng ngày** bằng **DatePicker** (Từ ngày / Đến ngày; chỉ lọc khi chọn đủ hai mốc);
- **Xoá một lượt** hoặc **xoá toàn bộ** (đều có hộp thoại xác nhận);
- Bấm một lượt → Intent tường minh mở lại màn Kết quả của lượt đó.

### FR-21. Đếm ngược ngày thi

Trang chủ có thẻ đếm ngược. Bấm "Đặt ngày thi dự kiến" → **DatePicker** (không cho chọn ngày quá khứ) → hiển thị "Còn N ngày nữa tới ngày thi".

## Nhóm F — Quản trị (chỉ Admin)

### FR-22. CRUD câu hỏi ⭐

| Thao tác | Chi tiết |
|---|---|
| **Xem / lọc** | Danh sách toàn bộ câu hỏi; lọc đồng thời theo **từ khoá**, **chương** (Spinner) và **chỉ câu điểm liệt** (CheckBox) trong một truy vấn SQL |
| **Thêm** | Nội dung, chương, đánh dấu điểm liệt, 4 ô đáp án A–D, chọn đáp án đúng, giải thích |
| **Sửa** | Form tự nạp lại dữ liệu cũ |
| **Xoá** | Có hộp thoại xác nhận; đáp án bị xoá theo nhờ **CASCADE** |

**Ràng buộc "đúng một đáp án đúng"** được bảo vệ ở ba chỗ:
- Không đánh dấu đáp án đúng → không lưu được;
- Đáp án đúng để trống → không lưu được; phải có ít nhất 2 đáp án;
- Danh sách quản trị **tự cảnh báo đỏ** nếu phát hiện câu hỏi trong CSDL không có đúng một đáp án đúng.

**Sửa câu hỏi chạy trong một transaction**: xoá đáp án cũ + ghi đáp án mới là một khối — không bao giờ có trạng thái câu hỏi mất đáp án giữa chừng.

### FR-23. CRUD biển báo

Danh sách + tìm kiếm + lọc nhóm; thêm / sửa / xoá (mã, tên, nhóm, mô tả).

**Ràng buộc chống trùng mã biển:** mã biển là định danh nghiệp vụ (người dùng tra cứu theo mã), nên hệ thống đếm trước khi ghi và **từ chối lưu nếu mã đã tồn tại ở biển khác** (có loại trừ chính biển đang sửa, để sửa biển mà không đổi mã vẫn lưu được).

## Nhóm G — Tiện ích (Intent ngầm)

| Chức năng | Intent | Vị trí |
|---|---|---|
| Tìm trung tâm sát hạch gần nhất | `ACTION_VIEW` với `geo:` | Menu Trang chủ |
| Gọi trung tâm sát hạch | `ACTION_DIAL` | Menu Trang chủ |
| Góp ý / báo lỗi câu hỏi | `ACTION_SENDTO` (`mailto:`) | Menu Trang chủ |
| Chia sẻ kết quả thi | `ACTION_SEND` | Màn Kết quả |
| Gửi báo cáo tiến độ | `ACTION_SENDTO` | Màn Thống kê |
| Xem văn bản luật gốc | `ACTION_VIEW` (https) | Chi tiết biển báo |
| Giới thiệu — ghi rõ nguồn bộ 600 câu | Dialog | Menu Trang chủ |

---

# IV. YÊU CẦU PHI CHỨC NĂNG

| Mã | Yêu cầu | Cách đáp ứng |
|---|---|---|
| **NFR-01** | **Hoạt động offline hoàn toàn** | Toàn bộ câu hỏi, biển báo, lượt thi, thống kê nằm trong Room. Không có mạng vẫn ôn, thi, tra cứu, xem thống kê được. Chỉ đăng nhập cần mạng, và có lối "dùng offline" |
| **NFR-02** | **Không lưu mật khẩu thô** | Ứng dụng không bao giờ thấy mật khẩu; Firebase giữ và băm phía máy chủ |
| **NFR-03** | **Toàn vẹn dữ liệu** | Khoá ngoại; CASCADE; ràng buộc một đáp án đúng; transaction khi lưu bài thi và khi sửa câu hỏi |
| **NFR-04** | **Phân quyền không thể vượt mặt từ phía client** | Firestore Security Rules (xem FR-05) |
| **NFR-05** | **Hiệu năng danh sách** | RecyclerView tái sử dụng khung nhìn cho mọi danh sách dài |
| **NFR-06** | **Tiết kiệm bộ nhớ ảnh** | Ảnh đại diện nén còn ~6–10 KB thay vì vài MB |
| **NFR-07** | **Giao diện nhất quán** | Material 3, một bảng màu, hỗ trợ **Dark mode** (DayNight) |
| **NFR-08** | **Cài được trên máy thật** | minSdk 24; targetSdk 34 để hệ thống tự chừa chỗ cho thanh trạng thái |

---

# V. ĐẶC TẢ DỮ LIỆU

## 5.1. Danh sách bảng (Room / SQLite)

| # | Bảng | Vai trò | Đã dùng? |
|---|---|---|---|
| 1 | `chapters` | 6 chương | ✔ |
| 2 | `questions` | Câu hỏi, cờ `is_diem_liet`, giải thích | ✔ |
| 3 | `answers` | Đáp án, cờ `is_correct` | ✔ |
| 4 | `traffic_signs` | Biển báo | ✔ |
| 5 | `exam_sets` | Bộ đề cố định | Bảng có, **UI chưa dùng** |
| 6 | `exam_set_questions` | Nối bộ đề ↔ câu hỏi | Bảng có, **UI chưa dùng** |
| 7 | `users` | Người dùng, vai trò, ảnh đại diện, ngày thi dự kiến | ✔ |
| 8 | `attempts` | Một lượt thi | ✔ |
| 9 | `user_answers` | Chi tiết từng câu trả lời | ✔ |
| 10 | `notes` | Ghi chú cá nhân | Bảng có, **chưa có chức năng** |
| 11 | `review_schedule` | Lịch ôn lại câu sai | Bảng có, **chưa có chức năng** |

## 5.2. Ràng buộc toàn vẹn

| Ràng buộc | Cài đặt |
|---|---|
| Xoá câu hỏi → xoá đáp án của nó | `ForeignKey.CASCADE` trên `answers.question_id` |
| Xoá người dùng → xoá lượt thi | `CASCADE` trên `attempts.user_id` |
| Không xoá được chương còn câu hỏi | `ForeignKey.RESTRICT` trên `questions.chapter_id` |
| Mỗi câu hỏi có **đúng một** đáp án đúng | Chặn ở form Admin + cảnh báo trong danh sách |
| Mã biển không trùng | Kiểm tra đếm trước khi ghi |
| Lưu lượt thi trọn vẹn | `@Transaction saveAttempt()` |
| Sửa câu hỏi trọn vẹn | `@Transaction updateQuestionWithAnswers()` |

## 5.3. Dữ liệu trên Firestore

Chỉ một collection duy nhất:

```
users/{uid} = {
    hoTen:  string,
    email:  string,
    vaiTro: "user" | "admin",   ← client KHÔNG được sửa (Rules chặn)
    ngayTao: number
}
```

---

# VI. DANH SÁCH MÀN HÌNH (16 Activity)

| # | Màn hình | Điều khiển chính | Quyền |
|---|---|---|---|
| 1 | Đăng nhập | EditText, Button, ProgressBar | Khách |
| 2 | Đăng ký | EditText, Button | Khách |
| 3 | Quên mật khẩu | EditText, Button | Khách |
| 4 | Trang chủ | GridLayout nút, CardView, DatePicker, **Menu** | Mọi tác nhân |
| 5 | Ôn tập theo chương | **Spinner**, RadioGroup, ImageView | Mọi tác nhân |
| 6 | Thi thử | RadioGroup, **ProgressBar**, CountDownTimer, **Dialog** | Mọi tác nhân |
| 7 | Kết quả thi | TextView, Button (chia sẻ) | Mọi tác nhân |
| 8 | Tra cứu biển báo | **RecyclerView**, **Spinner**, EditText tìm kiếm | Mọi tác nhân |
| 9 | Chi tiết biển báo | ImageView, Button | Mọi tác nhân |
| 10 | Thống kê | **Chart tự vẽ**, Button | Mọi tác nhân |
| 11 | Lịch sử làm bài | RecyclerView, **DatePicker**, Dialog | Mọi tác nhân |
| 12 | Cá nhân | **ImageView (ảnh đại diện)**, Button | Mọi tác nhân |
| 13 | [Admin] Quản trị câu hỏi | RecyclerView, Spinner, CheckBox, FAB | **Admin** |
| 14 | [Admin] Thêm / Sửa câu hỏi | EditText, Spinner, CheckBox, RadioButton | **Admin** |
| 15 | [Admin] Quản trị biển báo | RecyclerView, Spinner, FAB | **Admin** |
| 16 | [Admin] Thêm / Sửa biển báo | EditText, Spinner | **Admin** |

**Sơ đồ điều hướng:**

```
Đăng nhập ⇄ Đăng ký
    ├→ Quên mật khẩu
    └→ TRANG CHỦ ──────────────────────────────────┐
         │                                         │
         ├─ Ôn tập theo chương                     │ Menu:
         ├─ 60 câu điểm liệt                       │  • [Admin] Quản trị câu hỏi
         ├─ Thi thử → Kết quả → Chia sẻ            │      → Thêm/Sửa câu hỏi
         ├─ Tra cứu biển báo → Chi tiết → Xem luật │  • [Admin] Quản trị biển báo
         ├─ Thống kê → Ôn chương yếu / Gửi email   │      → Thêm/Sửa biển báo
         ├─ Lịch sử → Xem lại kết quả              │  • Tìm trung tâm (Maps)
         └─ Cá nhân → Chọn/Chụp ảnh đại diện       │  • Gọi trung tâm
                                                   │  • Góp ý / Giới thiệu / Đăng xuất
```

---

# VII. MA TRẬN TRUY VẾT — RUBRIC ↔ CHỨC NĂNG

| Tiêu chí rubric | % | Chức năng chứng minh |
|---|---|---|
| 1.2 Giao diện, điều khiển (datetime, combobox…) | 10 | Spinner (FR-07, FR-16, FR-22), DatePicker (FR-20, FR-21), RecyclerView, RadioGroup, ProgressBar, Dialog, Menu, ImageView, Chart (FR-18), Dark mode |
| 1.3 CSDL, toàn vẹn dữ liệu | 10 | Room 11 bảng; CASCADE/RESTRICT; transaction (FR-15, FR-22); Firebase Auth + Firestore |
| 2.1 Thêm/sửa/xoá/tìm kiếm | 10 | FR-22 (CRUD câu hỏi), FR-23 (CRUD biển báo), FR-16, FR-20 |
| 2.2 Xử lý hình ảnh | 10 | FR-06 (thư viện + camera + nén + EXIF + hiển thị + runtime permission) |
| 2.3 Intent | 10 | Intent tường minh (16 màn hình); Intent ngầm (Nhóm G) |
| 2.4 Hoạt động đúng mục đích | 10 | FR-10, FR-11, FR-13 (chấm điểm liệt), FR-14; NFR-01 offline |
| 2.5 Gửi email | 10 | FR-03 (đặt lại mật khẩu), FR-01 (xác minh email), FR-19, chia sẻ/góp ý |
| 3.3 Sáng tạo, độ phức tạp | 5 | Thuật toán điểm liệt; biểu đồ tự vẽ; phân tích chương yếu; phân quyền 3 tầng; đếm ngược ngày thi; mở Maps |

---

# VIII. GIỚI HẠN HIỆN TẠI

Phần này liệt kê **trung thực** những gì tài liệu thiết kế ban đầu có nêu nhưng **chưa cài đặt**. Nhóm chủ động chuyển các mục này sang "Hướng phát triển" để không vi phạm tiêu chí 3.2 (tài liệu hứa gì phải làm đủ nấy).

| # | Nội dung | Trạng thái |
|---|---|---|
| 1 | **Bộ 600 câu hỏi chính thức** | ⚠️ Hiện mới có **8 câu mẫu** trong `DatabaseSeeder`. **Đây là việc quan trọng nhất còn lại.** |
| 2 | Ảnh biển báo / sa hình thật | Đang dùng ảnh giữ chỗ |
| 3 | Thi theo **bộ đề cố định** | Bảng + DAO đã có, UI chưa cho chọn (hiện chỉ thi đề ngẫu nhiên) |
| 4 | **Xem lại chi tiết bài làm** từng câu | Chưa làm (hiện chỉ xem lại màn Kết quả tổng) |
| 5 | Ghi chú cá nhân, đánh dấu sao câu khó | Bảng `notes` đã có, chưa có UI |
| 6 | Ôn lại câu sai theo **lịch giãn dần** | Bảng `review_schedule` + DAO `getWrongQuestionIds()` đã có, chưa có UI |
| 7 | Đọc câu hỏi bằng **Text-to-Speech** | Chưa làm |
| 8 | Quản lý người dùng (khoá tài khoản) | Chưa làm |
| 9 | **Cập nhật bộ đề từ xa** | Chưa làm |
| 10 | Đồng bộ tiến độ Room ↔ Firestore | Chưa làm (Firestore hiện chỉ lưu vai trò) |
| 11 | Phóng to (zoom) ảnh sa hình | Chưa làm |
| 12 | Xác minh 4 thông số kỳ thi | ⚠️ Chưa xác minh (xem Mục II) |

---

# IX. KIỂM THỬ

## 9.1. Unit test

`ExamScorerTest` kiểm thử thuật toán chấm điểm — phần nghiệp vụ dễ sai nhất:
- Sai câu điểm liệt → TRƯỢT dù các câu khác đúng hết;
- Đúng ≥ ngưỡng và không sai điểm liệt → ĐẠT;
- Đúng < ngưỡng → TRƯỢT kèm lý do đúng.

## 9.2. Kiểm thử thủ công trên máy ảo (đã thực hiện)

| Luồng | Kết quả |
|---|---|
| Đăng ký → gửi email xác minh | ✔ Firebase trả thành công |
| Quên mật khẩu → gửi email đặt lại | ✔ Firebase trả thành công |
| Đăng nhập → Trang chủ | ✔ |
| Thi thử → sai câu điểm liệt → TRƯỢT + nêu lý do | ✔ |
| Thống kê → biểu đồ + gợi ý chương yếu | ✔ |
| Lịch sử → lọc theo ngày, xoá lượt thi | ✔ |
| Tra cứu biển báo → tìm kiếm, chi tiết | ✔ |
| Admin → thêm/sửa/xoá câu hỏi (8 → 9 → 8 câu) | ✔ |
| Admin → thêm/sửa/xoá biển báo; chặn trùng mã | ✔ |
| Ảnh đại diện: chọn từ thư viện + chụp camera + nén | ✔ |
| Người dùng thường **không thấy** menu quản trị | ✔ |
| Intent ngầm: gọi điện, bản đồ, email, chia sẻ | ✔ |

## 9.3. Việc kiểm thử còn phải làm

- Cài **file APK release đã ký** lên **ít nhất 2 điện thoại thật** (rubric 1.1 — 10%, dễ mất điểm nhất).
- Kiểm thử với đủ 600 câu hỏi (hiện mới thử với 8 câu).
- Kiểm thử ở chế độ máy bay để chứng minh NFR-01.

---

# X. HƯỚNG PHÁT TRIỂN

Ngoài các mục ở Mục VIII, nhóm định hướng mở rộng:

- Thi mô phỏng 120 tình huống giao thông bằng video;
- Mở rộng các hạng bằng khác (A1, C1, D…);
- Gợi ý lộ trình ôn tập cá nhân hoá;
- Thi đấu trực tuyến, bảng xếp hạng;
- Phiên bản iOS.

---

# XI. TÀI LIỆU THAM KHẢO

1. Bộ 600 câu hỏi dùng cho sát hạch lái xe cơ giới đường bộ — Cục Cảnh sát giao thông, Bộ Công an (áp dụng từ 01/6/2025).
2. Luật Trật tự, an toàn giao thông đường bộ (hiệu lực 01/01/2025).
3. Quy chuẩn kỹ thuật quốc gia về báo hiệu đường bộ (QCVN 41).
4. Android Developers — https://developer.android.com
5. Firebase Documentation — https://firebase.google.com/docs

> *Nhóm cần bổ sung số hiệu văn bản chính xác của các nguồn (1), (2), (3) trước khi nộp báo cáo.*
