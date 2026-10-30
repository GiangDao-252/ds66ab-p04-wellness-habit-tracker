# Tài liệu Thiết kế Lớp & Kiến trúc Hệ thống (Class Design Document)

**Dự án:** Project G4: Personal Wellness Habit Tracker  
**Tác giả:** Hiệp

---

## 1. Tổng quan Kiến trúc & Áp dụng Nguyên lý OOP

Hệ thống được thiết kế tuân thủ nghiêm ngặt các nguyên lý Lập trình hướng đối tượng (**OOP**) và các nguyên lý thiết kế phần mềm hướng đối tượng **SOLID**, nhằm đạt được độ gắn kết cao (**High Cohesion**) và mức độ phụ thuộc thấp (**Low Coupling**):

- **Single Responsibility Principle (SRP):** Mỗi lớp chỉ giữ một trách nhiệm duy nhất (Ví dụ: `CheckIn` chỉ quản lý giá trị/ngày đo lường; `StreakStrategy` chỉ chuyên trách tính toán chuỗi ngày liên tiếp).
- **Open/Closed Principle (OCP):** Dễ dàng mở rộng thêm các thuật toán đánh giá mới (như Frequency, Completion, Streak) thông qua Interface `GoalEvaluationStrategy` mà hoàn toàn không phải sửa đổi code của lớp `Habit` hay `GoalEvaluator`.
- **Dependency Inversion Principle (DIP):** Lớp ngữ cảnh `GoalEvaluator` phụ thuộc vào Abstraction (`GoalEvaluationStrategy`) thay vì phụ thuộc vào các cài đặt cụ thể.

---

## 2. Chi tiết Trách nhiệm & Mối quan hệ giữa các Lớp (Responsibilities & Relations)

### A. Gói Mô hình Nghiệp vụ (`com.oop.project.model`)

- **`CheckIn` (Value Object):**
  - **Trách nhiệm:** Đại diện cho một lần ghi nhận kết quả thói quen vào một ngày cụ thể (`LocalDate`) với một giá trị đo lường (`double value`). Áp dụng lập trình phòng thủ (`defensive programming`) chặn ngày tương lai và giá trị âm.
  - **Mối quan hệ:** Được quản lý bên trong danh sách (`List<CheckIn>`) của lớp `Habit`.
- **`Habit` (Entity):**
  - **Trách nhiệm:** Quản lý thông tin chi tiết về một thói quen (tên, tần suất, giá trị mục tiêu, trạng thái kích hoạt, khoảng thời gian thực hiện). Lưu trữ tập hợp các `CheckIn`.
  - **Mối quan hệ:** Quan hệ tổng hợp (**Composition**) chứa nhiều đối tượng `CheckIn`. Thuộc sở hữu của `WellnessPlan`.
- **`WellnessPlan` (Aggregate Root):**
  - **Trách nhiệm:** Đóng vai trò là gốc kết hợp (Aggregate Root) quản lý toàn bộ danh sách các thói quen (`Habit`) trong một kế hoạch chăm sóc sức khỏe cá nhân.

### B. Gói Chiến lược & Đánh giá (`com.oop.project.strategy`)

- **`GoalEvaluationStrategy` (Interface):**
  - **Trách nhiệm:** Định nghĩa hợp đồng chuẩn (`evaluate(Habit habit)`) cho mọi chiến lược đánh giá mục tiêu. Thể hiện tính **Polymorphism** (Đa hình).
- **`TargetCompletionStrategy`, `FrequencyCountStrategy`, `StreakStrategy`:**
  - **Trách nhiệm:** Các lớp cụ thể (Concrete Strategies) thực hiện các thuật toán đánh giá khác nhau (đánh giá theo định mức, theo số lần, hoặc theo chuỗi ngày liên tiếp `consecutive-day streak` có xử lý ngày thiếu và trùng lặp check-in).
- **`GoalEvaluator` (Context):**
  - **Trách nhiệm:** Đóng vai trò là Context trong **Strategy Pattern**. Lưu giữ chiến lược hiện tại và cung cấp cơ chế chuyển đổi chiến lược linh hoạt ngay khi chương trình đang chạy (**Runtime Polymorphism**) thông qua phương thức `setStrategy()`.

### C. Gói Ngoại lệ Tùy chỉnh (`com.oop.project.exception`)

- **`WellnessDomainException` & Các lớp con (`InvalidCheckInException`, `InvalidHabitException`, `InvalidPlanException`):**
  - **Trách nhiệm:** Xây dựng cấu trúc phân cấp ngoại lệ rõ ràng (`exception hierarchy`) giúp bắt và xử lý các lỗi nghiệp vụ một cách chính xác, minh bạch.

---

## 3. Biểu đồ Lớp UML (UML Class Diagram Overview)

Sơ đồ cấu trúc lớp và mối quan hệ liên kết trong dự án:

```text
+-------------------+       1        0..*       +-------------------+
|   WellnessPlan    |-------------------------->|       Habit       |
+-------------------+                           +-------------------+
                                                          |
                                                          | 1        0..* (Composition)
                                                          v
                                                +-------------------+
                                                |      CheckIn      |
                                                +-------------------+

+-----------------------+                      +-----------------------+
|    GoalEvaluator      |                      | GoalEvaluationStrategy|
|     (Context)         |                      |      (Interface)      |
+-----------------------+                      +-----------------------+
| - strategy            |                              ^
+-----------------------+                              |
| + setStrategy()       |         +--------------------+--------------------+
| + evaluateHabit()     |         |                    |                    |
+-----------------------+   +-------------------+  +-------------------+  +-------------------+
                            | TargetCompletion  |  | FrequencyCount    |  |  StreakStrategy   |
                            +-------------------+  +-------------------+  +-------------------+
```
