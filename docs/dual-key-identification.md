# TECHNICAL SPECIFICATION: DUAL-KEY IDENTIFICATION & SMART USER TAGS

**Project:** ThreadIt Forum Backend  
**Author:** LongDx  
**Version:** 2.2 (Hybrid: Latinized Prefix + NanoID Suffix)  
**Status:** Approved

---

## 1. Bối cảnh & Vấn đề (Context & Problem)

Trong quá trình thiết kế hệ thống định danh người dùng (User Identity), chúng ta đối mặt với các thách thức:

-   **Quyền tự do đặt tên:** User muốn đặt tên hiển thị tùy ý (Emoji, Tiếng Trung/Nhật...).
-   **Khả năng tìm kiếm toàn cầu:** User quốc tế cần một cách dễ dàng để gõ và tìm kiếm những cái tên đặc biệt này.
-   **Độc nhất & Bảo mật:** Cần một mã định danh duy nhất, không đoán được (Unpredictable) và có thể thay đổi (Re-rollable) khi cần.

> [!NOTE]
> Chi tiết triển khai code và thiết kế DB được mô tả tại [user-identity-spec.md](file:///Users/techmax/Documents/GitHub/forum-backend/docs/user-identity-spec.md).

---

## 2. Giải pháp Kiến trúc (Architecture Solution)

Chúng tôi áp dụng chiến lược **Hybrid User Tag**: Kết hợp giữa **Gợi nhớ (Latinized Name)** và **Bảo mật (Random NanoID)**.

### 2.1. Cấu trúc Public Tag

Mỗi User sẽ có một "Public Tag" dùng để định danh trên giao diện và URL:

**Format:** `Latinized_Initials` + `#` + `NanoID`

**Ví dụ:**
-   User: "李小龙" (Lý Tiểu Long)
-   Latinized: "LL"
-   NanoID: "Xy9z"
-   **FINAL TAG:** `LL#Xy9z`

### 2.2. Phân tích thành phần

| Thành phần | Nguồn gốc | Công nghệ | Mục đích |
| :--- | :--- | :--- | :--- |
| **Prefix (Tiền tố)** | Display Name | **IBM ICU4J** | Giúp ID trở nên thân thiện, dễ đọc, dễ nhớ. Giải quyết bài toán "Chữ tượng hình". |
| **Suffix (Hậu tố)** | Random Generated | **NanoID** | Đảm bảo tính duy nhất (Unique), bảo mật (không lộ ID thật), và cho phép đổi mới (Re-roll). |

---

## 3. Thuật toán xử lý (Algorithm Detail)

### 3.1. Xử lý phần Prefix (Tiền tố) - "The Latinizer"

Sử dụng thư viện **IBM ICU4J** để chuyển đổi mọi ngôn ngữ về ký tự Latin (A-Z).

1.  **Transliteration:** Dùng bộ dịch Any-Latin; Latin-ASCII.
    *   "李小龙" -> "Li Xiao Long"
    *   "Nguyễn Văn A" -> "Nguyen Van A"
2.  **Abbreviation (Viết tắt):** Lấy chữ cái đầu của các từ (hoặc giữ nguyên nếu ngắn).
    *   "Li Xiao Long" -> "LXL" (hoặc "LL" tùy config).
3.  **Sanitization:** Loại bỏ ký tự đặc biệt.

### 3.2. Xử lý phần Suffix (Hậu tố) - "The Randomizer"

Thay vì Hash ID (cũ), ta sinh một chuỗi ngẫu nhiên bằng **NanoID**.

*   **Độ dài:** 8-12 ký tự (Tùy chỉnh độ khó).
*   **Charset:** A-Z, a-z, 0-9.
*   **Collision Check:** Kiểm tra trùng trong DB trước khi gán.

---

## 4. Luồng xử lý (Implementation Flow)

### 4.1. Khi lưu vào Database (Write)
-   **Internal ID:** Lưu TSID (Primary Key).
-   **Public ID:** Lưu phần Suffix NanoID (`Xy9z`) vào cột `public_id`.
-   **Prefix:** Có thể không cần lưu (tính toán động) HOẶC lưu vào cột riêng `tag_prefix` để search nhanh hơn.

### 4.2. Khi truy vấn (Search)
User gõ tìm kiếm: `LL#Xy9z`
1.  Hệ thống tách chuỗi lấy phần sau dấu `#` -> `Xy9z`.
2.  Query: `SELECT * FROM users WHERE public_id = 'Xy9z'`.
3.  (Optional) Kiểm tra xem Prefix có khớp với DisplayName hiện tại không (để redirect nếu user đổi tên hiển thị).

---

## 5. Dependencies Required

Hệ thống cần cả 2 thư viện:

```xml
<!-- 1. NanoID for Unique Suffix -->
<dependency>
    <groupId>com.aventrix.jnanoid</groupId>
    <artifactId>jnanoid</artifactId>
    <version>2.0.0</version>
</dependency>

<!-- 2. ICU4J for Prefix Latinization -->
<dependency>
    <groupId>com.ibm.icu</groupId>
    <artifactId>icu4j</artifactId>
    <version>74.2</version>
</dependency>

<!-- 3. TSID Creator for Internal ID -->
<dependency>
    <groupId>com.github.f4b6a3</groupId>
    <artifactId>tsid-creator</artifactId>
    <version>5.2.6</version>
</dependency>
```




