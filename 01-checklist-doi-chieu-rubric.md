# CHECKLIST ĐỐI CHIẾU TIÊU CHÍ CHẤM ĐIỂM

**Đề tài:** Ứng dụng ôn thi lý thuyết sát hạch lái xe hạng B (600 câu hỏi)
**Môn:** Phát triển phần mềm cho thiết bị di động
**Nhóm:** 5 thành viên — Trường (nhóm trưởng), Hậu, An, Dương, Long
**Hình thức thi:** Vấn đáp — điểm phụ thuộc chất lượng làm việc của từng cá nhân

---

## Cách dùng bảng này

Đây là bảng "bảo hiểm điểm số". Mỗi dòng là một tiêu chí trong đáp án chấm của giảng viên, kèm chức năng cụ thể trong app dùng để chứng minh tiêu chí đó.

Quy tắc: **mỗi tiêu chí phải có ít nhất một chức năng chạy được để demo**. Không có chức năng nào chứng minh được tiêu chí nào thì coi như mất trọn phần trăm đó, dù app đẹp đến đâu.

Trước buổi bảo vệ, cả nhóm ngồi lại tick từng dòng. Dòng nào chưa tick là dòng đang mất điểm.

Ký hiệu: `[ ]` chưa làm · `[~]` đang làm · `[x]` xong và đã test trên máy thật

---

## TIÊU CHÍ 1 — Nền tảng ứng dụng (30%)

### 1.1. Ứng dụng cài đặt thành công từ file APK — 10%

| # | Việc cần làm | Người phụ trách | Trạng thái |
|---|---|---|---|
| 1 | Build được file APK bản release (không phải debug) | Cả nhóm | [ ] |
| 2 | Ký APK (signing key) để cài được trên máy thật | Trường | [ ] |
| 3 | Cài thử trên **ít nhất 2 điện thoại thật khác nhau** | Cả nhóm | [ ] |
| 4 | Kiểm tra app mở được, không crash ngay khi khởi động | Cả nhóm | [ ] |
| 5 | Copy file APK ra USB / gửi link tải, có sẵn hôm bảo vệ | Trường | [ ] |
| 6 | Đặt tên app + icon riêng (không để icon Android mặc định) | An | [ ] |

> **Cảnh báo:** đây là 10% dễ mất nhất vì nhóm hay để đến sát ngày mới build. App chạy trên emulator không đồng nghĩa cài được trên máy thật. **Build APK từ tuần thứ 2**, đừng đợi.

---

### 1.2. Giao diện đồ họa phù hợp, dùng điều khiển hợp lý (datetime, combobox…) — 10%

| # | Điều khiển cần thể hiện | Xuất hiện ở màn hình nào | Người phụ trách | Trạng thái |
|---|---|---|---|---|
| 1 | **Button** | Toàn bộ app (các nút chức năng trang chủ) | An | [ ] |
| 2 | **TextView / EditText** | Đăng nhập, đăng ký, ô tìm kiếm | Trường | [ ] |
| 3 | **Spinner (combobox)** | Chọn chương ôn tập; chọn nhóm biển báo; chọn bộ đề | An | [~] |
| 4 | **RecyclerView** | Danh sách câu hỏi, danh sách biển báo, lịch sử thi | An / Dương | [~] |
| 5 | **RadioButton / RadioGroup** | Chọn đáp án A/B/C/D khi làm bài | Dương | [~] |
| 6 | **ProgressBar** | Đồng hồ đếm ngược khi thi; tiến độ ôn tập | Dương | [~] |
| 7 | **DatePicker (datetime)** | Lọc lịch sử thi theo ngày; đặt ngày thi dự kiến để đếm ngược | Long | [~] |
| 8 | **Menu / BottomNavigation** | Điều hướng: Trang chủ – Ôn tập – Thi – Thống kê – Cá nhân | An | [~] |
| 9 | **Dialog** | Xác nhận nộp bài; cảnh báo hết giờ | Dương | [~] |
| 10 | **ImageView** | Ảnh biển báo, ảnh sa hình, ảnh đại diện | An | [~] |
| 11 | **Chart (biểu đồ)** | Thống kê tiến độ theo 6 chương | Long | [~] |
| 12 | Giao diện đồng nhất: 1 bảng màu, 1 bộ font, có Dark mode | An | [~] |

> **Mẹo ăn điểm:** rubric ghi rõ "datetime, combobox". Đây là hai điều khiển app ôn thi **không tự nhiên cần**. Phải cố tình nhét vào:
> - **DatePicker**: cho người dùng chọn "Ngày thi sát hạch dự kiến" → app hiển thị "Còn 12 ngày nữa thi". Vừa dùng DatePicker, vừa là tính năng có ý nghĩa thật.
> - **Spinner**: dùng để chọn chương / nhóm biển báo, thay vì để một danh sách nút bấm.

---

### 1.3. Sử dụng CSDL (SQLite, Firebase…), đảm bảo toàn vẹn dữ liệu — 10%

| # | Việc cần làm | Người phụ trách | Trạng thái |
|---|---|---|---|
| 1 | Thiết kế schema đầy đủ (11 bảng — xem tài liệu thiết kế) | Hậu | [ ] |
| 2 | Cài đặt **Room** (SQLite) — lưu 600 câu hỏi offline | Hậu | [ ] |
| 3 | Cài đặt **Firebase** (Auth + Firestore + Storage) | Trường | [~] |
| 4 | Script seed đổ 600 câu hỏi từ file JSON vào database | Trường | [ ] |
| 5 | Khóa ngoại (Foreign Key) giữa các bảng — đảm bảo toàn vẹn | Hậu | [ ] |
| 6 | Ràng buộc: xóa câu hỏi thì xóa luôn đáp án (CASCADE) | Hậu | [ ] |
| 7 | Ràng buộc: mỗi câu hỏi phải có đúng 1 đáp án đúng | Hậu | [ ] |
| 8 | Không lưu mật khẩu dạng thô (Firebase Auth tự lo) | Trường | [~] |
| 9 | Đồng bộ dữ liệu Room ↔ Firestore khi có mạng | Trường | [ ] |

> **Câu hỏi vấn đáp có thể gặp:** "Toàn vẹn dữ liệu được đảm bảo thế nào?" → Trả lời bằng: khóa ngoại, ràng buộc CASCADE, ràng buộc mỗi câu chỉ có một đáp án đúng, và giao dịch (transaction) khi lưu bài thi để không lưu nửa chừng.

---

## TIÊU CHÍ 2 — Chức năng ứng dụng (50%)

### 2.1. Chức năng quản lý: Thêm, sửa, xoá, tìm kiếm — 10%

App ôn thi bản chất chỉ **đọc** dữ liệu, nên phải chủ động xây phần CRUD. Có hai tầng:

**A. CRUD phía Quản trị viên (Admin)**

| # | Chức năng | Người phụ trách | Trạng thái |
|---|---|---|---|
| 1 | **Thêm** câu hỏi mới (nội dung, 4 đáp án, chọn đáp án đúng, chọn chương, đánh dấu điểm liệt) | Hậu | [~] |
| 2 | **Sửa** câu hỏi đã có | Hậu | [~] |
| 3 | **Xoá** câu hỏi (có hộp thoại xác nhận) | Hậu | [~] |
| 4 | **Thêm / sửa / xoá** biển báo (mã, tên, nhóm, mô tả, ảnh) | Hậu | [~] |
| 5 | Quản lý người dùng: xem danh sách, khoá tài khoản | Trường | [ ] |

**B. CRUD phía Người dùng**

| # | Chức năng | Người phụ trách | Trạng thái |
|---|---|---|---|
| 6 | **Ghi chú cá nhân** cho từng câu hỏi — thêm / sửa / xoá ghi chú | An | [ ] |
| 7 | **Đánh dấu sao** câu hỏi khó — thêm / bỏ đánh dấu | An | [ ] |
| 8 | Sửa thông tin cá nhân (tên, ảnh đại diện) | Trường | [ ] |
| 9 | Xoá lịch sử làm bài | Long | [~] |

**C. Tìm kiếm**

| # | Chức năng | Người phụ trách | Trạng thái |
|---|---|---|---|
| 10 | Tìm kiếm câu hỏi theo **từ khóa** trong nội dung | An | [~] |
| 11 | Tìm kiếm biển báo theo **tên hoặc mã biển** | An | [~] |
| 12 | Lọc câu hỏi theo **chương** và theo **câu điểm liệt** | An | [~] |

---

### 2.2. Chức năng xử lý hình ảnh (chọn ảnh từ thư viện, lưu, hiển thị) — 10%

**Đây là tiêu chí app ôn thi KHÔNG tự nhiên có. Bắt buộc phải chủ động làm, nếu không mất trọn 10%.**

| # | Việc cần làm | Người phụ trách | Trạng thái |
|---|---|---|---|
| 1 | **Chọn ảnh đại diện** từ thư viện ảnh của máy | Trường | [~] |
| 2 | **Chụp ảnh** bằng camera làm ảnh đại diện | Trường | [~] |
| 3 | **Lưu** ảnh đại diện (Firebase Storage hoặc bộ nhớ trong) | Trường | [~] |
| 4 | **Hiển thị** ảnh đại diện ở màn hình cá nhân + thanh điều hướng | Trường | [~] |
| 5 | Xin quyền truy cập thư viện / camera đúng cách (runtime permission) | Trường | [~] |
| 6 | **Admin chọn ảnh biển báo / sa hình từ thư viện** khi thêm câu hỏi | Hậu | [ ] |
| 7 | Hiển thị ảnh câu hỏi (biển báo, sa hình) khi làm bài | Dương | [ ] |
| 8 | Cho phép **phóng to ảnh** sa hình (zoom) khi làm bài | Dương | [ ] |
| 9 | Nén ảnh trước khi lưu để tiết kiệm dung lượng | Trường | [~] |

> **Ưu tiên cao nhất:** mục 1–4 (ảnh đại diện) là cách nhanh nhất và chắc chắn nhất để ăn 10% này. Làm sớm.

---

### 2.3. Áp dụng Intent để xây dựng chức năng chính — 10%

Rubric muốn thấy Intent được dùng **có ý thức**. Liệt kê sẵn để lúc vấn đáp trả lời được ngay.

**A. Intent tường minh (Explicit Intent) — chuyển giữa các màn hình**

| # | Luồng chuyển màn hình | Người phụ trách | Trạng thái |
|---|---|---|---|
| 1 | Đăng nhập → Trang chủ (kèm truyền dữ liệu người dùng) | Trường | [~] |
| 2 | Trang chủ → Màn hình Thi (truyền loại đề: ngẫu nhiên / bộ đề số N) | Dương | [ ] |
| 3 | Màn hình Thi → Màn hình Kết quả (truyền điểm, danh sách câu sai) | Dương | [ ] |
| 4 | Kết quả → Xem lại bài làm (truyền mã lượt thi) | Dương | [ ] |
| 5 | Trang chủ → Ôn tập → Chi tiết câu hỏi (truyền mã câu hỏi) | An | [~] |
| 6 | Trang chủ → Tra cứu biển báo → Chi tiết biển báo | An | [~] |

**B. Intent ngầm (Implicit Intent) — gọi ứng dụng khác của hệ thống**

| # | Chức năng | Intent dùng | Người phụ trách | Trạng thái |
|---|---|---|---|---|
| 7 | Mở thư viện ảnh chọn ảnh đại diện | `ACTION_GET_CONTENT` | Trường | [~] |
| 8 | Mở camera chụp ảnh | `ACTION_IMAGE_CAPTURE` | Trường | [~] |
| 9 | **Gửi kết quả thi qua email** | `ACTION_SEND` | Long | [~] |
| 10 | **Chia sẻ kết quả** lên Zalo / Facebook / Messenger | `ACTION_SEND` | Long | [~] |
| 11 | **Mở Google Maps tìm trung tâm sát hạch gần nhất** | `ACTION_VIEW` (geo:) | Long | [~] |
| 12 | Mở trình duyệt xem văn bản luật giao thông gốc | `ACTION_VIEW` | An | [~] |
| 13 | Gọi điện tới trung tâm sát hạch | `ACTION_DIAL` | Long | [~] |

> **Điểm sáng tạo:** mục 11 (mở Google Maps tìm trung tâm sát hạch) là chức năng vừa dùng Intent ngầm, vừa rất có ích thật — đây là loại chi tiết giúp ăn điểm phụ "tính sáng tạo".

---

### 2.4. Ứng dụng hoạt động đúng mục đích — 10%

Nghĩa là: app phải thực sự giúp người dùng ôn thi được, và **mô phỏng đúng quy chế thi thật**.

| # | Việc cần làm | Người phụ trách | Trạng thái |
|---|---|---|---|
| 1 | **Xác minh cấu trúc đề thi hạng B từ nguồn chính thức Cục CSGT** (số câu, thời gian, số câu cần đúng để đạt) | Cả nhóm | [ ] |
| 2 | Nhập đủ **600 câu hỏi** có đáp án đúng | Cả nhóm | [ ] |
| 3 | Đánh dấu chính xác **60 câu điểm liệt** | Hậu | [ ] |
| 4 | Phân loại đúng **6 chương** theo bộ đề gốc | Hậu | [ ] |
| 5 | Đủ ảnh biển báo và ảnh sa hình cho các câu cần ảnh | An | [ ] |
| 6 | **Logic chấm điểm đúng luật:** sai 1 câu điểm liệt → TRƯỢT ngay, dù các câu khác đúng hết | Long | [~] |
| 7 | Đồng hồ đếm ngược đúng thời gian quy định, hết giờ tự nộp bài | Dương | [~] |
| 8 | Hiển thị rõ lý do trượt ("Sai câu điểm liệt số X" hoặc "Không đủ số câu đúng") | Long | [~] |
| 9 | Ghi rõ **nguồn bộ câu hỏi** (Cục CSGT ban hành) trong màn hình Giới thiệu | Trường | [ ] |
| 10 | App **chạy được offline hoàn toàn** (không mạng vẫn ôn và thi được) | Hậu | [ ] |

> **Đây là tiêu chí quan trọng nhất về mặt nghiệp vụ.** Nếu chấm điểm sai luật (không xử lý điểm liệt), giảng viên chỉ cần hỏi một câu là lộ ngay app làm ẩu.

---

### 2.5. Chức năng gửi email (thông báo, đổi mật khẩu…) — 10%

**Tiêu chí thứ hai mà app ôn thi KHÔNG tự nhiên có. Bắt buộc làm.**

| # | Việc cần làm | Cách làm | Người phụ trách | Trạng thái |
|---|---|---|---|---|
| 1 | **Quên mật khẩu** → gửi email đặt lại mật khẩu | Firebase Auth `sendPasswordResetEmail()` | Trường | [~] |
| 2 | **Xác minh email** khi đăng ký tài khoản mới | Firebase Auth `sendEmailVerification()` | Trường | [~] |
| 3 | **Gửi kết quả thi qua email** (nút trên màn hình Kết quả) | Intent `ACTION_SEND` | Long | [~] |
| 4 | **Gửi báo cáo tiến độ học tập** qua email theo yêu cầu | Intent `ACTION_SEND` | Long | [~] |
| 5 | Gửi góp ý / báo lỗi câu hỏi sai tới nhóm phát triển | Intent `ACTION_SENDTO` | An | [~] |

> **Cách nhanh nhất:** Firebase Auth có sẵn hàm gửi email đặt lại mật khẩu — bật lên là chạy, mất khoảng 10 phút. Đây là 10% dễ lấy nhất trong toàn bộ rubric. **Làm ngay tuần đầu.**

---

## TIÊU CHÍ 3 — Tài liệu và hoàn thiện (20%)

### 3.1. Có tài liệu thiết kế: giới thiệu, mô tả chức năng, màn hình giao diện — 5%

| # | Việc cần làm | Người phụ trách | Trạng thái |
|---|---|---|---|
| 1 | Phần **Giới thiệu**: lý do chọn đề tài, mục tiêu, đối tượng dùng | Trường | [ ] |
| 2 | Phần **Mô tả chức năng**: liệt kê đầy đủ từng chức năng | Cả nhóm | [ ] |
| 3 | Phần **Màn hình giao diện**: ảnh chụp + mô tả từng màn hình | An | [ ] |
| 4 | Sơ đồ **Use Case** (2 tác nhân: Người dùng, Admin) | Hậu | [ ] |
| 5 | Sơ đồ **ERD** (quan hệ giữa các bảng) | Hậu | [ ] |
| 6 | Sơ đồ **kiến trúc hệ thống** (MVVM: View – ViewModel – Repository – Room/Firebase) | Trường | [ ] |
| 7 | Phần **Phạm vi và giới hạn** (nêu rõ cái gì KHÔNG làm) | Trường | [ ] |
| 8 | Phần **Phân công công việc** | Trường | [ ] |
| 9 | **Tài liệu tham khảo** (ghi rõ nguồn bộ 600 câu) | Cả nhóm | [ ] |

---

### 3.2. Hoàn thiện đủ các chức năng đã nêu trong tài liệu thiết kế — 10%

> **ĐÂY LÀ CÁI BẪY LỚN NHẤT CỦA RUBRIC.**
>
> Tài liệu hứa bao nhiêu thì phải làm đủ bấy nhiêu. Hứa nhiều mà làm không hết → **mất 10%**.
>
> **Nguyên tắc vàng:** chỉ ghi vào phần "Chức năng" những gì **chắc chắn làm xong**. Mọi ý tưởng hay ho khác (thi đối kháng, AI gợi ý lộ trình, 120 tình huống mô phỏng…) đưa hết xuống mục **"Hướng phát triển trong tương lai"** — nêu ở đó thì KHÔNG bị trừ điểm vì chưa làm.

| # | Việc cần làm | Người phụ trách | Trạng thái |
|---|---|---|---|
| 1 | Rà soát: mọi chức năng ghi trong tài liệu đều đã code xong | Cả nhóm | [ ] |
| 2 | Rà soát ngược: mọi chức năng đã code đều có trong tài liệu | Cả nhóm | [ ] |
| 3 | Không chức năng nào bấm vào bị crash hoặc "đang phát triển" | Cả nhóm | [ ] |
| 4 | Chốt danh sách chức năng **1 tuần trước hạn** — sau đó không thêm gì mới | Trường | [ ] |

---

### 3.3. Điểm phụ: tính sáng tạo, độ phức tạp, trả lời câu hỏi phụ — 5%

| # | Điểm nhấn để gây ấn tượng | Người phụ trách | Trạng thái |
|---|---|---|---|
| 1 | **Thuật toán chấm điểm có câu điểm liệt** (không chỉ đếm câu đúng) | Long | [~] |
| 2 | **Ôn lại câu sai theo lịch giãn dần (spaced repetition)** — câu sai xuất hiện lại theo chu kỳ tăng dần | Long | [ ] |
| 3 | **Phân tích điểm yếu theo 6 chương** — biểu đồ chỉ ra chương yếu nhất, gợi ý ôn | Long | [~] |
| 4 | **Hoạt động offline hoàn toàn** + tự đồng bộ khi có mạng | Hậu / Trường | [ ] |
| 5 | **Đọc câu hỏi bằng giọng nói (Text-to-Speech)** — ôn khi đang di chuyển | An | [ ] |
| 6 | **Đếm ngược tới ngày thi** (dùng DatePicker) | Long | [~] |
| 7 | **Mở Google Maps tìm trung tâm sát hạch gần nhất** | Long | [~] |
| 8 | **Chế độ tối (Dark mode)** | An | [~] |
| 9 | **Cập nhật bộ đề từ xa** — khi Cục CSGT sửa bộ câu hỏi, app tải bản mới về mà không cần cài lại | Trường | [ ] |

---

## BẢNG TỔNG HỢP — MỖI NGƯỜI GÁNH TIÊU CHÍ NÀO

Vì đây là **thi vấn đáp**, mỗi người phải tự tay làm ít nhất một tiêu chí rubric để có cái mà trình bày.

| Thành viên | Module phụ trách | Tiêu chí rubric trực tiếp gánh |
|---|---|---|
| **Trường** (nhóm trưởng) | Tài khoản, Firebase, đồng bộ | Gửi email (2.5) · Xử lý ảnh đại diện (2.2) · CSDL (1.3) · Build APK (1.1) |
| **Hậu** | Ngân hàng câu hỏi, Admin | Thêm/sửa/xoá/tìm kiếm (2.1) · CSDL & toàn vẹn dữ liệu (1.3) · Chọn ảnh câu hỏi (2.2) |
| **An** | Ôn tập, Tra cứu biển báo, UI | Giao diện & điều khiển (1.2) · Tìm kiếm (2.1) · Tài liệu màn hình (3.1) |
| **Dương** | Thi thử | Intent tường minh (2.3) · Nhiều Activity · Hoạt động đúng mục đích (2.4) |
| **Long** | Chấm điểm, Thống kê | Intent ngầm: email/chia sẻ/Maps (2.3, 2.5) · Độ phức tạp thuật toán (3.3) |

---

## VIỆC LÀM NGAY TRONG TUẦN ĐẦU (ưu tiên theo điểm/công sức)

Sắp theo thứ tự "dễ lấy điểm nhất trước":

1. **[10%]** Bật Firebase Auth + chức năng gửi email quên mật khẩu → mất ~1 buổi, ăn trọn 10%.
2. **[10%]** Chọn ảnh đại diện từ thư viện → mất ~1 buổi, ăn trọn 10%.
3. **[10%]** Build thử file APK và cài lên máy thật → làm sớm để biết lỗi sớm.
4. **[10%]** Chuẩn bị file JSON 600 câu hỏi + ảnh → việc nặng nhất, phải bắt đầu ngay.
5. **[10%]** Màn hình Admin thêm/sửa/xoá/tìm kiếm câu hỏi.
6. Các phần còn lại.

---

## RÀ SOÁT CUỐI CÙNG — LÀM TRƯỚC NGÀY BẢO VỆ

| # | Việc | Trạng thái |
|---|---|---|
| 1 | File APK đã cài thử thành công trên máy thật, có sẵn để nộp | [ ] |
| 2 | Mọi tiêu chí trong tài liệu này đều có ít nhất 1 chức năng demo được | [ ] |
| 3 | Không chức năng nào crash | [ ] |
| 4 | Mọi chức năng trong tài liệu thiết kế đều đã hoàn thành | [ ] |
| 5 | Mỗi thành viên đọc hiểu được code phần mình, giải thích được khi bị hỏi | [ ] |
| 6 | Lịch sử Git thể hiện rõ đóng góp của từng người | [ ] |
| 7 | Chuẩn bị sẵn tài khoản demo (1 tài khoản người dùng, 1 tài khoản admin) | [ ] |
| 8 | Chuẩn bị sẵn kịch bản demo 5 phút: đăng nhập → thi thử → sai câu điểm liệt → xem kết quả trượt → xem thống kê điểm yếu | [ ] |
| 9 | Sạc đầy pin điện thoại demo, chuẩn bị cáp kết nối máy chiếu | [ ] |
