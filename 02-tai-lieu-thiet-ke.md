# TÀI LIỆU THIẾT KẾ

# ỨNG DỤNG ÔN THI LÝ THUYẾT SÁT HẠCH LÁI XE HẠNG B

**Trường Đại học Sư phạm Hà Nội — Khoa Công nghệ thông tin**
**Môn: Phát triển phần mềm cho thiết bị di động**

**Nhóm thực hiện — 5 thành viên:**

| Họ tên | Vai trò |
|---|---|
| Trường | Nhóm trưởng — Tài khoản, Firebase, Đồng bộ dữ liệu |
| Hậu | Ngân hàng câu hỏi, Cơ sở dữ liệu, Quản trị viên |
| An | Ôn tập, Tra cứu biển báo, Giao diện |
| Dương | Thi thử, Mô phỏng kỳ thi |
| Long | Chấm điểm, Thống kê, Ôn lại câu sai |

---

# I. GIỚI THIỆU

## 1.1. Lý do chọn đề tài

Kỳ thi sát hạch cấp Giấy phép lái xe là kỳ thi bắt buộc với hàng triệu người mỗi năm tại Việt Nam. Từ ngày 01/6/2025, Cục Cảnh sát giao thông (Bộ Công an) đã ban hành bộ **600 câu hỏi lý thuyết mới**, thay thế bộ đề cũ do Bộ Giao thông Vận tải quản lý trước đây. Đồng thời, theo Luật Trật tự an toàn giao thông đường bộ có hiệu lực từ 01/01/2025, hệ thống phân hạng giấy phép lái xe cũng thay đổi — hạng B1 và B2 cũ được hợp nhất thành **hạng B**.

Thực tế cho thấy người học đang gặp ba khó khăn chính:

**Thứ nhất, tài liệu phân tán.** Bộ câu hỏi nằm rải rác ở nhiều nguồn, nhiều bản không cập nhật theo bộ đề mới, dẫn đến học sai.

**Thứ hai, không theo dõi được tiến bộ.** Người học không biết mình yếu ở chương nào, đang sai lặp lại những câu nào.

**Thứ ba, đặc thù "câu điểm liệt" ít được chú trọng.** Trong bộ đề có 60 câu điểm liệt — chỉ cần trả lời sai một câu là bị đánh trượt ngay lập tức, bất kể các câu còn lại đúng hết. Nhiều người trượt oan vì không nắm rõ nhóm câu này.

Xuất phát từ thực tế đó, nhóm xây dựng một ứng dụng di động hỗ trợ ôn thi lý thuyết sát hạch lái xe hạng B, hoạt động được **hoàn toàn ngoại tuyến (offline)** — phù hợp với thói quen ôn tập mọi lúc mọi nơi của người học.

## 1.2. Mục tiêu

**Mục tiêu tổng quát:** xây dựng ứng dụng Android giúp người học ôn luyện hiệu quả và mô phỏng chính xác kỳ thi lý thuyết sát hạch lái xe hạng B.

**Mục tiêu cụ thể:**

1. Lưu trữ và hiển thị trọn vẹn bộ 600 câu hỏi chính thức, phân theo 6 chương.
2. Mô phỏng kỳ thi thật: giới hạn thời gian, chấm điểm **đúng quy chế bao gồm luật câu điểm liệt**.
3. Tra cứu hệ thống biển báo giao thông.
4. Tự động ghi nhận và gợi ý ôn lại các câu trả lời sai.
5. Thống kê, phân tích điểm yếu của người học theo từng chương.
6. Hoạt động ổn định khi không có kết nối mạng.
7. Cho phép quản trị viên cập nhật ngân hàng câu hỏi khi quy định thay đổi.

## 1.3. Đối tượng sử dụng

**Người học (Học viên):** người đang chuẩn bị thi sát hạch lấy giấy phép lái xe hạng B.

**Quản trị viên (Admin):** người quản lý ngân hàng câu hỏi, biển báo và tài khoản người dùng.

## 1.4. Phạm vi và giới hạn của đề tài

Việc xác định rõ phạm vi giúp tập trung nguồn lực vào các chức năng cốt lõi.

**Trong phạm vi đồ án:**

- Bộ 600 câu hỏi lý thuyết hạng B (dạng trắc nghiệm một đáp án đúng).
- Ôn tập theo chương, thi thử theo bộ đề và đề ngẫu nhiên.
- Tra cứu biển báo giao thông.
- Chấm điểm tự động, thống kê và phân tích kết quả.
- Quản trị ngân hàng câu hỏi.
- Nền tảng: Android.

**Ngoài phạm vi đồ án** (sẽ nêu ở phần Hướng phát triển):

- Phần thi mô phỏng 120 tình huống giao thông bằng video.
- Phần thi thực hành (sa hình trên xe, đường trường).
- Các hạng bằng lái khác (A1, A, C1, C, D…).
- Nền tảng iOS.
- Tính năng thi đấu trực tuyến giữa người dùng.

## 1.5. Nguồn dữ liệu

Bộ câu hỏi sử dụng trong ứng dụng là **bộ 600 câu hỏi lý thuyết dùng cho sát hạch lái xe cơ giới đường bộ do Cục Cảnh sát giao thông — Bộ Công an biên soạn và ban hành**, áp dụng chính thức từ 01/6/2025. Hình ảnh biển báo được lấy theo Quy chuẩn kỹ thuật quốc gia về báo hiệu đường bộ.

Ứng dụng được xây dựng cho mục đích học tập, phi thương mại. Nguồn dữ liệu được ghi rõ trong màn hình "Giới thiệu" của ứng dụng.

---

# II. PHÂN TÍCH HỆ THỐNG

## 2.1. Cấu trúc bộ đề thi

Bộ 600 câu hỏi được chia thành 6 chương:

| Chương | Nội dung | Số câu |
|---|---|---|
| 1 | Quy định chung và quy tắc giao thông đường bộ | 180 |
| 2 | Văn hóa giao thông, đạo đức người lái xe, kỹ năng phòng cháy chữa cháy và cứu hộ cứu nạn | 25 |
| 3 | Kỹ thuật lái xe | (theo bộ đề gốc) |
| 4 | Cấu tạo và sửa chữa | 37 (câu 264–300) |
| 5 | Báo hiệu đường bộ | 185 (câu 301–485) |
| 6 | Giải thế sa hình và kỹ năng xử lý tình huống giao thông | 115 (câu 486–600) |

Trong 600 câu có **60 câu điểm liệt** — là các câu liên quan tới tình huống mất an toàn giao thông nghiêm trọng.

> **Lưu ý khi triển khai:** số câu và thời gian làm bài của đề thi hạng B cần được **xác minh lại từ nguồn chính thức của Cục CSGT** trước khi cài đặt phần thi thử, vì các nguồn tham khảo trên mạng có thông tin không thống nhất. Thông số này được lưu trong tệp cấu hình để dễ điều chỉnh.

## 2.2. Quy tắc chấm điểm

Đây là điểm đặc thù quan trọng nhất của kỳ thi. Thuật toán chấm điểm gồm hai bước:

```
Bước 1 — Kiểm tra câu điểm liệt:
    NẾU thí sinh trả lời sai BẤT KỲ câu điểm liệt nào
        → KẾT QUẢ = TRƯỢT (dừng ngay, không cần xét tiếp)
        → Lý do trượt: "Trả lời sai câu điểm liệt số X"

Bước 2 — Xét ngưỡng điểm:
    NẾU số câu đúng >= ngưỡng đạt
        → KẾT QUẢ = ĐẠT
    NGƯỢC LẠI
        → KẾT QUẢ = TRƯỢT
        → Lý do trượt: "Chỉ đúng X/Y câu, chưa đạt ngưỡng"
```

Việc thí sinh có thể đúng gần hết các câu nhưng vẫn trượt vì sai một câu điểm liệt là điều ứng dụng phải thể hiện chính xác và giải thích rõ cho người dùng.

## 2.3. Sơ đồ Use Case

**Tác nhân 1 — Người học:**

- Đăng ký tài khoản
- Đăng nhập / Đăng xuất
- Quên mật khẩu (nhận email đặt lại)
- Cập nhật thông tin cá nhân (bao gồm chọn ảnh đại diện)
- Ôn tập theo chương
- Ôn tập nhóm 60 câu điểm liệt
- Thi thử theo bộ đề cố định
- Thi thử với đề ngẫu nhiên
- Xem kết quả và xem lại bài làm
- Ôn lại các câu đã trả lời sai
- Tra cứu biển báo giao thông
- Tìm kiếm câu hỏi
- Thêm / sửa / xoá ghi chú cá nhân cho câu hỏi
- Xem thống kê tiến độ học tập
- Gửi kết quả thi qua email
- Chia sẻ kết quả
- Đặt ngày thi dự kiến và xem đếm ngược

**Tác nhân 2 — Quản trị viên:**

- Đăng nhập với quyền quản trị
- Thêm / sửa / xoá câu hỏi
- Thêm / sửa / xoá biển báo (kèm chọn ảnh từ thư viện)
- Đánh dấu câu điểm liệt
- Quản lý bộ đề thi
- Quản lý tài khoản người dùng
- Xem thống kê toàn hệ thống

## 2.4. Yêu cầu phi chức năng

**Hoạt động ngoại tuyến:** toàn bộ chức năng ôn tập, thi thử, tra cứu biển báo phải hoạt động khi không có mạng.

**Bảo mật:** mật khẩu không lưu dạng văn bản thô; sử dụng Firebase Authentication để quản lý xác thực.

**Hiệu năng:** màn hình danh sách 600 câu hỏi phải cuộn mượt (sử dụng RecyclerView có tái sử dụng khung nhìn).

**Toàn vẹn dữ liệu:** ràng buộc khóa ngoại giữa các bảng; mỗi câu hỏi bắt buộc có đúng một đáp án đúng; lưu bài thi bằng giao dịch (transaction) để tránh lưu nửa chừng.

**Khả năng cập nhật:** khi cơ quan chức năng thay đổi bộ câu hỏi, ứng dụng tải bản cập nhật từ máy chủ mà không cần cài đặt lại.

---

# III. THIẾT KẾ HỆ THỐNG

## 3.1. Kiến trúc tổng thể

Ứng dụng áp dụng mô hình kiến trúc **MVVM (Model – View – ViewModel)**, phân tách rõ ràng giữa giao diện và xử lý nghiệp vụ.

```
┌─────────────────────────────────────────┐
│  TẦNG GIAO DIỆN                         │
│  View (Activity / Fragment)             │
│         ↕                               │
│  ViewModel (LiveData / Flow)            │
└─────────────────────────────────────────┘
                  ↕
┌─────────────────────────────────────────┐
│  TẦNG DỮ LIỆU                           │
│  Repository (điều phối nguồn dữ liệu)   │
│         ↙            ↘                  │
│  Room (SQLite)    Firebase              │
│  - 600 câu hỏi    - Xác thực            │
│  - Ảnh biển báo   - Đồng bộ tiến độ     │
│  - Tiến độ học    - Lưu ảnh đại diện    │
│  → OFFLINE        - Cập nhật bộ đề      │
└─────────────────────────────────────────┘
```

**Nguyên tắc hoạt động:** Repository là "một cửa" quyết định lấy dữ liệu từ đâu. Mặc định đọc từ Room (chạy được offline). Khi có mạng, đồng bộ tiến độ lên Firebase và kiểm tra bản cập nhật bộ đề.

## 3.2. Công nghệ sử dụng

| Thành phần | Công nghệ |
|---|---|
| Nền tảng | Android (Java / Kotlin) |
| Kiến trúc | MVVM |
| Cơ sở dữ liệu cục bộ | Room (SQLite) |
| Xác thực & Cơ sở dữ liệu đám mây | Firebase Authentication, Cloud Firestore |
| Lưu trữ ảnh | Firebase Storage |
| Gửi email | Firebase Auth (đặt lại mật khẩu), Intent ACTION_SEND |
| Biểu đồ thống kê | MPAndroidChart |
| Quản lý mã nguồn | Git / GitHub |
| Thiết kế giao diện | Figma |

## 3.3. Thiết kế cơ sở dữ liệu

### Danh sách bảng

**`chapters` — Chương**

| Trường | Kiểu | Ghi chú |
|---|---|---|
| id | INTEGER | Khóa chính |
| ten_chuong | TEXT | Tên chương |
| so_thu_tu | INTEGER | 1–6 |

**`questions` — Câu hỏi**

| Trường | Kiểu | Ghi chú |
|---|---|---|
| id | INTEGER | Khóa chính |
| chapter_id | INTEGER | Khóa ngoại → chapters |
| noi_dung | TEXT | Nội dung câu hỏi |
| anh_url | TEXT | Ảnh biển báo / sa hình (có thể rỗng) |
| **is_diem_liet** | BOOLEAN | **Đánh dấu câu điểm liệt** |
| giai_thich | TEXT | Lời giải thích đáp án |

**`answers` — Đáp án**

| Trường | Kiểu | Ghi chú |
|---|---|---|
| id | INTEGER | Khóa chính |
| question_id | INTEGER | Khóa ngoại → questions (CASCADE) |
| noi_dung | TEXT | Nội dung đáp án |
| is_correct | BOOLEAN | Đáp án đúng hay không |

**`traffic_signs` — Biển báo**

| Trường | Kiểu | Ghi chú |
|---|---|---|
| id | INTEGER | Khóa chính |
| ma_bien | TEXT | Mã biển (VD: P.101) |
| ten_bien | TEXT | Tên biển báo |
| nhom_bien | TEXT | Cấm / Nguy hiểm / Hiệu lệnh / Chỉ dẫn / Phụ |
| anh_url | TEXT | Ảnh biển báo |
| mo_ta | TEXT | Mô tả ý nghĩa |

**`exam_sets` — Bộ đề cố định**

| Trường | Kiểu | Ghi chú |
|---|---|---|
| id | INTEGER | Khóa chính |
| ten_de | TEXT | VD: "Đề số 1" |
| so_cau | INTEGER | Số câu trong đề |
| thoi_gian | INTEGER | Thời gian làm bài (phút) |
| nguong_dat | INTEGER | Số câu đúng tối thiểu để đạt |

**`exam_set_questions` — Bảng nối bộ đề ↔ câu hỏi**

| Trường | Kiểu |
|---|---|
| exam_set_id | INTEGER (FK) |
| question_id | INTEGER (FK) |

**`users` — Người dùng**

| Trường | Kiểu | Ghi chú |
|---|---|---|
| id | TEXT | Khóa chính (Firebase UID) |
| ho_ten | TEXT | |
| email | TEXT | |
| anh_dai_dien | TEXT | Đường dẫn ảnh |
| vai_tro | TEXT | "user" hoặc "admin" |
| ngay_thi_du_kien | DATE | Dùng cho đếm ngược |

**`attempts` — Lượt thi**

| Trường | Kiểu | Ghi chú |
|---|---|---|
| id | INTEGER | Khóa chính |
| user_id | TEXT | Khóa ngoại → users |
| exam_set_id | INTEGER | Khóa ngoại (rỗng nếu đề ngẫu nhiên) |
| so_cau_dung | INTEGER | |
| **ket_qua** | TEXT | "DAT" / "TRUOT" |
| **ly_do_truot** | TEXT | "Sai câu điểm liệt" / "Không đủ điểm" |
| thoi_gian_lam | INTEGER | Số giây đã dùng |
| ngay_thi | DATETIME | |

**`user_answers` — Chi tiết từng câu trả lời** ⭐

| Trường | Kiểu | Ghi chú |
|---|---|---|
| id | INTEGER | Khóa chính |
| attempt_id | INTEGER | Khóa ngoại → attempts |
| question_id | INTEGER | Khóa ngoại → questions |
| answer_id | INTEGER | Đáp án đã chọn |
| dung_sai | BOOLEAN | |

> Đây là bảng **quan trọng nhất**. Không có bảng này thì không thể xem lại bài làm, không thể gom câu sai, và không thể phân tích điểm yếu theo chương.

**`review_schedule` — Lịch ôn lại câu sai**

| Trường | Kiểu | Ghi chú |
|---|---|---|
| id | INTEGER | Khóa chính |
| user_id | TEXT | Khóa ngoại |
| question_id | INTEGER | Khóa ngoại |
| so_lan_sai | INTEGER | |
| lan_on_tiep | DATE | Ngày cần ôn lại |
| khoang_cach | INTEGER | Số ngày giãn cách (tăng dần) |

**`notes` — Ghi chú cá nhân**

| Trường | Kiểu | Ghi chú |
|---|---|---|
| id | INTEGER | Khóa chính |
| user_id | TEXT | Khóa ngoại |
| question_id | INTEGER | Khóa ngoại |
| noi_dung | TEXT | Ghi chú người dùng tự viết |

### Ràng buộc toàn vẹn dữ liệu

- Xoá câu hỏi → tự động xoá các đáp án của nó (ON DELETE CASCADE).
- Mỗi câu hỏi bắt buộc có **đúng một** đáp án `is_correct = true`.
- Lưu một lượt thi (`attempts` + nhiều `user_answers`) được thực hiện trong **một giao dịch (transaction)** — hoặc lưu hết, hoặc không lưu gì.

---

# IV. MÔ TẢ CHỨC NĂNG

## 4.1. Nhóm chức năng Tài khoản

**Đăng ký tài khoản.** Người dùng nhập họ tên, email, mật khẩu. Hệ thống kiểm tra tính hợp lệ (email đúng định dạng, mật khẩu tối thiểu 6 ký tự) và gửi **email xác minh**.

**Đăng nhập.** Xác thực qua Firebase Authentication.

**Quên mật khẩu.** Người dùng nhập email, hệ thống **gửi email chứa liên kết đặt lại mật khẩu**.

**Quản lý thông tin cá nhân.** Cho phép sửa họ tên và **chọn ảnh đại diện từ thư viện ảnh hoặc chụp ảnh mới bằng camera**. Ảnh được nén, lưu lên Firebase Storage và hiển thị trong ứng dụng.

**Đặt ngày thi dự kiến.** Người dùng chọn ngày thi bằng **DatePicker**; trang chủ hiển thị đếm ngược số ngày còn lại.

## 4.2. Nhóm chức năng Ôn tập

**Ôn tập theo chương.** Người dùng chọn chương (qua **Spinner**), duyệt lần lượt từng câu hỏi. Sau khi chọn đáp án, ứng dụng hiển thị ngay đáp án đúng kèm **giải thích**.

**Ôn tập 60 câu điểm liệt.** Nhóm câu hỏi riêng, được nhấn mạnh vì sai một câu là trượt.

**Ôn lại các câu bị sai.** Ứng dụng tự động gom các câu người dùng từng trả lời sai. Áp dụng cơ chế **ôn lại theo lịch giãn dần**: câu trả lời sai nhiều lần sẽ xuất hiện lại thường xuyên hơn; câu đã trả lời đúng nhiều lần sẽ giãn dần khoảng cách ôn.

**Ghi chú cá nhân.** Người dùng có thể **thêm, sửa, xoá** ghi chú riêng cho từng câu hỏi.

**Đánh dấu câu khó.** Người dùng đánh dấu sao câu hỏi để ôn lại sau.

**Đọc câu hỏi bằng giọng nói.** Sử dụng Text-to-Speech, hỗ trợ ôn tập khi không tiện nhìn màn hình.

## 4.3. Nhóm chức năng Tra cứu biển báo

**Danh sách biển báo** phân theo 5 nhóm: biển cấm, biển nguy hiểm, biển hiệu lệnh, biển chỉ dẫn, biển phụ. Chọn nhóm qua **Spinner**.

**Tìm kiếm biển báo** theo tên hoặc mã biển.

**Chi tiết biển báo:** hiển thị ảnh biển, mã, tên, mô tả ý nghĩa.

## 4.4. Nhóm chức năng Thi thử

**Thi theo bộ đề cố định.** Người dùng chọn một bộ đề có sẵn.

**Thi với đề ngẫu nhiên.** Hệ thống sinh đề ngẫu nhiên theo đúng cấu trúc đề thi thật (đảm bảo đủ số câu mỗi chương và có câu điểm liệt).

**Đồng hồ đếm ngược.** Hiển thị thời gian còn lại bằng **ProgressBar** kết hợp đồng hồ số. Hết giờ, hệ thống **tự động nộp bài**.

**Làm bài.** Chọn đáp án bằng **RadioGroup**; có thanh điều hướng nhanh giữa các câu; đánh dấu câu chưa làm.

**Nộp bài.** Có hộp thoại xác nhận, cảnh báo nếu còn câu chưa trả lời.

**Xem lại bài làm.** Hiển thị từng câu: đáp án đã chọn, đáp án đúng, giải thích.

## 4.5. Nhóm chức năng Chấm điểm và Thống kê

**Chấm điểm tự động theo đúng quy chế** (xem mục 2.2): ưu tiên kiểm tra câu điểm liệt trước, sau đó mới xét ngưỡng điểm.

**Hiển thị kết quả** rõ ràng: Đạt / Trượt, số câu đúng, thời gian làm bài, và **nêu rõ lý do nếu trượt**.

**Lịch sử làm bài.** Danh sách các lượt thi đã thực hiện, có thể **lọc theo khoảng ngày** (dùng DatePicker) và **xoá** lượt thi cũ.

**Thống kê tiến độ.** Biểu đồ thể hiện tỷ lệ đúng theo từng chương trong 6 chương, giúp người học nhận ra mình yếu ở đâu. Ứng dụng đưa ra gợi ý: "Bạn đang yếu nhất ở Chương 5 — Báo hiệu đường bộ (đúng 52%). Nên ôn lại chương này."

**Gửi kết quả thi qua email.** Nút trên màn hình kết quả, mở ứng dụng email với nội dung kết quả được điền sẵn (Intent `ACTION_SEND`).

**Chia sẻ kết quả** lên mạng xã hội (Intent `ACTION_SEND`).

**Tìm trung tâm sát hạch gần nhất.** Mở Google Maps với từ khóa tìm kiếm (Intent `ACTION_VIEW`).

## 4.6. Nhóm chức năng Quản trị viên

**Quản lý câu hỏi:** thêm, sửa, xoá, tìm kiếm câu hỏi. Khi thêm câu hỏi có ảnh (biển báo, sa hình), quản trị viên **chọn ảnh từ thư viện ảnh của thiết bị**.

**Đánh dấu câu điểm liệt.**

**Quản lý biển báo:** thêm, sửa, xoá biển báo kèm ảnh.

**Quản lý bộ đề thi:** tạo bộ đề, chọn câu hỏi đưa vào đề.

**Quản lý người dùng:** xem danh sách, khoá tài khoản.

**Cập nhật bộ đề từ xa:** khi cơ quan chức năng sửa đổi bộ câu hỏi, quản trị viên cập nhật trên máy chủ; ứng dụng của người dùng tự tải bản mới mà không cần cài đặt lại.

---

# V. THIẾT KẾ GIAO DIỆN

## 5.1. Danh sách màn hình

| # | Màn hình | Mô tả | Điều khiển chính |
|---|---|---|---|
| 1 | **Khởi động (Splash)** | Logo ứng dụng, kiểm tra trạng thái đăng nhập | ImageView |
| 2 | **Đăng nhập** | Nhập email, mật khẩu; liên kết "Quên mật khẩu" | EditText, Button, TextView |
| 3 | **Đăng ký** | Nhập họ tên, email, mật khẩu, xác nhận mật khẩu | EditText, Button |
| 4 | **Quên mật khẩu** | Nhập email để nhận liên kết đặt lại | EditText, Button |
| 5 | **Trang chủ** | Lưới các nút chức năng; đếm ngược ngày thi | Button, CardView, TextView |
| 6 | **Ôn tập — Chọn chương** | Danh sách 6 chương kèm tiến độ từng chương | Spinner, RecyclerView, ProgressBar |
| 7 | **Ôn tập — Câu hỏi** | Nội dung câu, ảnh (nếu có), 4 đáp án; hiện đáp án đúng + giải thích sau khi chọn | RadioGroup, ImageView, Button |
| 8 | **60 câu điểm liệt** | Danh sách riêng nhóm câu điểm liệt | RecyclerView |
| 9 | **Các câu bị sai** | Danh sách câu đã trả lời sai; nút "Ôn lại ngay" | RecyclerView, Button |
| 10 | **Tra cứu biển báo** | Lưới ảnh biển báo; ô tìm kiếm; lọc theo nhóm biển | Spinner, SearchView, GridView |
| 11 | **Chi tiết biển báo** | Ảnh lớn, mã biển, tên, mô tả | ImageView, TextView |
| 12 | **Chọn hình thức thi** | Đề ngẫu nhiên / Thi theo bộ đề | Button, Spinner |
| 13 | **Làm bài thi** | Câu hỏi, đáp án, đồng hồ đếm ngược, thanh điều hướng câu | RadioGroup, ProgressBar, RecyclerView |
| 14 | **Kết quả thi** | ĐẠT/TRƯỢT, số câu đúng, lý do trượt; nút gửi email, chia sẻ, xem lại bài | TextView, Button |
| 15 | **Xem lại bài làm** | Từng câu: đáp án đã chọn, đáp án đúng, giải thích | RecyclerView |
| 16 | **Thống kê** | Biểu đồ tỷ lệ đúng theo 6 chương; gợi ý chương cần ôn | Chart, TextView |
| 17 | **Lịch sử làm bài** | Danh sách lượt thi; lọc theo ngày; xoá | RecyclerView, DatePicker |
| 18 | **Cá nhân** | Ảnh đại diện (chọn từ thư viện/chụp), họ tên, ngày thi dự kiến | ImageView, EditText, DatePicker |
| 19 | **Giới thiệu** | Thông tin nhóm, nguồn dữ liệu bộ 600 câu | TextView |
| 20 | **[Admin] Quản lý câu hỏi** | Danh sách, tìm kiếm, thêm/sửa/xoá | RecyclerView, SearchView, Button |
| 21 | **[Admin] Thêm/Sửa câu hỏi** | Nhập nội dung, 4 đáp án, chọn chương, chọn ảnh từ thư viện, đánh dấu điểm liệt | EditText, Spinner, CheckBox, ImageView |
| 22 | **[Admin] Quản lý biển báo** | Danh sách, thêm/sửa/xoá biển báo kèm ảnh | RecyclerView, Button |
| 23 | **[Admin] Quản lý người dùng** | Danh sách người dùng, khoá tài khoản | RecyclerView |

## 5.2. Sơ đồ điều hướng

```
Splash
  ├── (chưa đăng nhập) → Đăng nhập ⇄ Đăng ký
  │                          └→ Quên mật khẩu
  └── (đã đăng nhập) → TRANG CHỦ
                          │
       ┌──────────────────┼──────────────────┬─────────────┐
       │                  │                  │             │
   Ôn tập             Thi thử           Thống kê      Cá nhân
       │                  │                  │             │
   Chọn chương      Chọn hình thức     Lịch sử      Chọn ảnh
       │                  │                  │        đại diện
   Câu hỏi          Làm bài thi        Xem lại bài
       │                  │
   Ghi chú          Kết quả thi
                          ├→ Gửi email
                          ├→ Chia sẻ
                          └→ Xem lại bài làm

   Tra cứu biển báo → Chi tiết biển báo
   60 câu điểm liệt
   Các câu bị sai

   [Admin] → Quản lý câu hỏi / biển báo / người dùng
```

## 5.3. Nguyên tắc thiết kế giao diện

**Bảng màu:** sử dụng màu sắc phân biệt rõ các nhóm chức năng trên trang chủ (mỗi nút một màu), giúp người dùng nhận diện nhanh. Đáp án đúng hiển thị màu xanh, đáp án sai màu đỏ.

**Chế độ tối (Dark mode):** hỗ trợ cả giao diện sáng và tối.

**Cỡ chữ:** đủ lớn để đọc thoải mái, vì đối tượng người dùng đa dạng độ tuổi.

**Ảnh sa hình:** cho phép phóng to (zoom), vì chi tiết trong ảnh sa hình thường nhỏ.

---

# VI. PHÂN CÔNG CÔNG VIỆC

| Thành viên | Module phụ trách | Nội dung công việc |
|---|---|---|
| **Trường** (Nhóm trưởng) | Tài khoản, Firebase, Đồng bộ | Khởi tạo dự án; cấu hình Firebase; đăng ký/đăng nhập; **gửi email đặt lại mật khẩu**; **chọn ảnh đại diện từ thư viện/camera**; đồng bộ dữ liệu; cập nhật bộ đề từ xa; build file APK |
| **Hậu** | Cơ sở dữ liệu, Ngân hàng câu hỏi, Quản trị | Thiết kế schema; cài đặt Room; script nhập 600 câu hỏi; **màn hình Admin thêm/sửa/xoá/tìm kiếm câu hỏi và biển báo**; đánh dấu câu điểm liệt; ràng buộc toàn vẹn dữ liệu |
| **An** | Ôn tập, Tra cứu biển báo, Giao diện | Màn hình ôn tập theo chương; hiển thị câu hỏi và giải thích; **tra cứu và tìm kiếm biển báo**; ghi chú cá nhân; Text-to-Speech; thiết kế giao diện tổng thể, Dark mode; tài liệu mô tả màn hình |
| **Dương** | Thi thử | Màn hình chọn hình thức thi; sinh đề ngẫu nhiên đúng cấu trúc; màn hình làm bài; **đồng hồ đếm ngược, tự nộp bài khi hết giờ**; xem lại bài làm; điều hướng giữa các Activity bằng Intent |
| **Long** | Chấm điểm, Thống kê | **Thuật toán chấm điểm có xử lý câu điểm liệt**; màn hình kết quả; **ôn lại câu sai theo lịch giãn dần**; biểu đồ thống kê theo chương; lịch sử làm bài; **gửi kết quả qua email, chia sẻ, mở Google Maps** |

**Công việc chung của cả nhóm:**

- Chuẩn hoá dữ liệu 600 câu hỏi và ảnh biển báo (mỗi người kiểm tra 120 câu).
- Xác minh cấu trúc đề thi từ nguồn chính thức.
- Kiểm thử chéo giữa các module.
- Build và kiểm tra cài đặt file APK trên máy thật.
- Hoàn thiện tài liệu và chuẩn bị bảo vệ.

**Quản lý mã nguồn:** cả nhóm sử dụng chung một kho lưu trữ trên GitHub. Mỗi thành viên làm việc trên một nhánh riêng và gộp mã qua Pull Request, đảm bảo mã nguồn không đè lên nhau và ghi nhận rõ đóng góp của từng cá nhân.

---

# VII. HƯỚNG PHÁT TRIỂN TRONG TƯƠNG LAI

Các nội dung dưới đây **nằm ngoài phạm vi đồ án lần này**, được nêu ra như định hướng mở rộng:

- Bổ sung phần thi mô phỏng 120 tình huống giao thông bằng video.
- Mở rộng sang các hạng bằng khác: A1, A, C1, C, D…
- Ứng dụng trí tuệ nhân tạo để gợi ý lộ trình ôn tập cá nhân hoá.
- Tính năng thi đấu trực tuyến giữa hai người dùng.
- Bảng xếp hạng cộng đồng.
- Phát triển phiên bản iOS.
- Tích hợp đăng ký lịch thi sát hạch trực tuyến.

---

# VIII. TÀI LIỆU THAM KHẢO

1. Bộ 600 câu hỏi dùng cho sát hạch lái xe cơ giới đường bộ — Cục Cảnh sát giao thông, Bộ Công an (áp dụng từ 01/6/2025).
2. Luật Trật tự, an toàn giao thông đường bộ (có hiệu lực từ 01/01/2025).
3. Quy chuẩn kỹ thuật quốc gia về báo hiệu đường bộ.
4. Tài liệu chính thức Android Developers — https://developer.android.com
5. Tài liệu Firebase — https://firebase.google.com/docs

> *Ghi chú: nhóm cần bổ sung số hiệu văn bản chính xác của các nguồn (1), (2), (3) từ cổng thông tin chính thức trước khi nộp báo cáo.*
