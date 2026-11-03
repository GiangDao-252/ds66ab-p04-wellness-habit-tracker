# Kịch bản demo G4

- **Demo 1**. Tạo plan và habit DAILY có target 10, ngày bắt đầu và kết thúc là hôm nay.
  Mở dashboard cho ngày hôm nay bằng mục 11. Qua mục 8 thêm check-in có giá trị 10.
  Dashboard in lại 100% trong cùng lượt thao tác. Thử sửa số đo về 5 để thấy 50%, sau đó xóa để về 0%.
- **Demo 2**. Thêm lại check-in 10, chọn mục 12, chọn Streak và cửa sổ 7 ngày.
  Dashboard đổi từ phần trăm sang số ngày. Khởi động lại ứng dụng và mở dashboard để xác minh cấu hình đã lưu.
- **Demo 3**. Thử số đo âm, ngày sai định dạng và ngày tương lai. Kiểm tra CLI báo lỗi, quay về menu và không phát refresh cho thao tác thất bại.

Luồng ghi đi từ view qua ApplicationService, CheckInService, repository và PostgreSQL.
Sau commit, PlanSubject thông báo DashboardObserver, observer đọc lại báo cáo.
ReportEvaluator gọi công thức CompletionPercentageStrategy hoặc StreakStrategy của Hiệp.
Check-in bằng 0 vẫn được lưu nhưng không đóng góp vào streak theo cách tích hợp hiện tại.
Cửa sổ được giao với thời gian hoạt động của habit. Công thức tuần giữ theo lớp của Hiệp.
