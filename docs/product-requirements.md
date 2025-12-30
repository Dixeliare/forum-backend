# PRODUCT REQUIREMENTS & SYSTEM OVERVIEW

**Tên dự án:** (Tạm gọi) Hybrid Social Platform  
**Phiên bản:** 1.0  
**Ngày lập:** 26/12/2025

---

## 1. VẤN ĐỀ & GIẢI PHÁP (PROBLEM & SOLUTION)

### Vấn đề (Problem)
Người dùng hiện tại phải dùng nhiều ứng dụng rời rạc:
*   Một nơi để **hỏi đáp chuyên sâu** (như Reddit/Voz/StackOverflow).
*   Một nơi để **giải trí nhanh** (như TikTok/Threads/Twitter).
*   Một nơi để **sinh hoạt nhóm** (như Facebook Groups).

### Giải pháp (Solution)
Xây dựng một **Super App** kết hợp 2 thế giới:
1.  **Chiều sâu:** Hệ thống Forum 3 cấp lưu trữ kiến thức.
2.  **Tốc độ:** Newsfeed dạng "Threads" tập trung vào khám phá nội dung viral từ người lạ và cộng đồng.

---

## 2. CÁC PHÂN HỆ CHÍNH (CORE MODULES)

### A. Phân hệ Forum (Knowledge Base)
Hoạt động theo mô hình diễn đàn truyền thống để thảo luận sâu, lưu trữ kiến thức lâu dài.

**Cấu trúc 3 lớp (3 Layers):**
1.  **Categories:** Danh mục lớn (VD: Công nghệ, Đời sống).
2.  **Sub-forums:** Chủ đề cụ thể (VD: Java Backend, Chuyện trò linh tinh).
3.  **Threads:** Bài thảo luận (Bắt buộc có **Tiêu đề** + **Nội dung**).

**Tính chất:** Nội dung tồn tại lâu dài (Evergreen), tìm kiếm dễ dàng, đề cao chất lượng tranh luận.

### B. Phân hệ Community & Personal (Social Network)
Hoạt động theo mô hình Mạng xã hội (Facebook Group/Threads/Twitter).

**Thành phần:**
*   **Community:** Nhóm sinh hoạt chung, sở thích.
*   **Personal Wall:** Trang cá nhân của user.
*   **Posts:** Bài đăng nhanh (Không tiêu đề, chú trọng Ảnh/Video/Text ngắn).

**Tính chất:** Real-time, giải trí, trôi nhanh theo thời gian (Ephemeral).

### C. Smart Newsfeed (The "Mixer")
Trái tim của ứng dụng, nơi giữ chân người dùng.

*   **Trải nghiệm:** Cuộn vô tận (Infinite Scroll), tự động phát video/ảnh.
*   **Cơ chế:** Ưu tiên hiển thị nội dung hay của người lạ (Discovery) thay vì chỉ hiển thị bạn bè.

---

## 3. LOGIC HIỂN THỊ FEED (THE 70-20-10 RULE)

Newsfeed không hiển thị theo thời gian thuần túy mà được trộn theo tỷ lệ cố định để tối ưu hóa việc khám phá nội dung mới nhưng vẫn giữ được kết nối cá nhân.

| Nguồn (Source) | Tỷ lệ | Mô tả |
| :--- | :--- | :--- |
| **Discovery (Viral)** | **70%** | Các bài Post cá nhân hoặc Community Post của người lạ đang có điểm Viral cao. |
| **Following (High Quality)** | **20%** | Bài của bạn bè/Community đã tham gia, NHƯNG chỉ hiện những bài có tương tác tốt (lọc bỏ bài rác). |
| **Forum Highlights** | **10%** | Các Thread đang tranh luận "nóng" trong Forum để user biết đến sự tồn tại của Forum. |

---
*End of Requirement.*
