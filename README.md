# Ankard Flashcard - English Learning Platform

Ankard là một nền tảng học tiếng Anh qua Flashcard sử dụng thuật toán Lặp lại ngắt quãng (Spaced Repetition). Dự án hỗ trợ người dùng tự quản lý bộ từ vựng cá nhân, nhập bộ thẻ từ cộng đồng (Công cộng, VIP), và thống kê quá trình học tập chi tiết.

## 🚀 Công nghệ sử dụng (Tech Stack)
* **Backend:** Java 17, Spring Boot 3.2.0, Spring Data JPA, Spring Security (Crypto)
* **Frontend:** HTML, CSS, JavaScript (Vanilla), Thymeleaf (Template Engine)
* **Database:** MySQL 8.x
* **Build Tool:** Maven

---

## 💻 Yêu cầu hệ thống (Prerequisites)
Để chạy được dự án này trên máy, bạn cần cài đặt:
1. **Java Development Kit (JDK) 17**: [Tải tại đây](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
2. **MySQL Server** (Khuyên dùng v8.x): [Tải tại đây](https://dev.mysql.com/downloads/installer/)
3. **Maven** (Không bắt buộc nếu dùng wrapper `mvnw` đi kèm proj): [Tải tại đây](https://maven.apache.org/download.cgi)
4. (Tùy chọn) Một IDE hỗ trợ Java như **IntelliJ IDEA**, **Eclipse**, hoặc **VS Code**.

---

## 🛠️ Hướng dẫn cài đặt và thiết lập (Setup & Run)

### Bước 1: Tải mã nguồn (Clone)
Mở Terminal/Command Prompt và chạy lệnh sau để tải project về máy:
```bash
git clone https://github.com/Tên_Tài_Khoản_Của_Bạn/web-english-public.git
cd web-english-public
```

### Bước 2: Thiết lập Cơ sở dữ liệu (Database Setup)
Dự án sử dụng cơ sở dữ liệu MySQL có tên là `english_learning_flashcard`.

1. Khởi động **MySQL Server** (và mở công cụ quản lý như MySQL Workbench, Navicat hoặc DBeaver).
2. Tạo một database mới (Schema) có tên là `english_learning_flashcard` với Character Set là `utf8mb4`:
   ```sql
   CREATE DATABASE english_learning_flashcard CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```
3. Khôi phục cấu trúc và dữ liệu ban đầu: Ở thư mục gốc của dự án, mở và chạy kịch bản (script) từ file **`database_setup.sql`** vào database vừa tạo.

### Bước 3: Cấu hình kết nối DB trong mã nguồn
Mở file cấu hình tại đường dẫn `src/main/resources/application.properties`. Chỉnh sửa lại `username` và `password` cho trùng khớp với tài khoản MySQL trên máy tính của bạn:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/english_learning_flashcard?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=Mật_khẩu_mysql_của_bạn
```
*(Lưu ý: Mặc định cổng chạy ứng dụng đang được set ở cấu hình `server.port=8081`)*

### Bước 4: Chạy dự án (Run the Application)

**Cách 1: Sử dụng Maven Wrapper (Trực tiếp bằng Terminal)**
Mở terminal ở thư mục gốc của dự án và chạy câu lệnh:
* Với Windows:
  ```cmd
  mvnw.cmd spring-boot:run
  ```
* Với Mac/Linux:
  ```bash
  ./mvnw spring-boot:run
  ```

**Cách 2: Chạy bằng IntelliJ IDEA / Eclipse**
1. Mở IDE và chọn **Open Project** -> Trỏ tới thư mục chứa mã nguồn.
2. Đợi IDE tải xuống các thư viện (`dependencies`) trong file `pom.xml`.
3. Tìm đến file `src/main/java/com/example/ankard/AnkardApplication.java`.
4. Click chuột phải, chọn **Run 'AnkardApplication'**.

### Bước 5: Truy cập Ứng dụng
Sau khi terminal báo `Started AnkardApplication in x.xxx seconds`, mở trình duyệt web và truy cập vào:
👉 **http://localhost:8081**

---

## 🔑 Tài khoản mặc định để kiểm thử (Test Accounts)
Sau khi load thành công file `database_setup.sql`, hệ thống sẽ có các đặc quyền và tài khoản có sẵn như sau (Ví dụ mặc định):
* **Super Admin**: (Bạn có thể tra cứu thông tin đăng nhập trong file `database_setup.sql`)
* **Admin**: (Tra cứu trong SQL)
* **Người dùng VIP / Thường**: (Tra cứu trong SQL hoặc tự tạo account mới trên giao diện web).

*(Gợi ý: Tìm các dòng INSERT INTO users trong script DB để lấy username)*

---

## 📦 Cấu trúc Thư mục chính (Folder Structure)
```
ankard/
├── src/main/java/com/example/ankard/
│   ├── controller/   # Chứa các Controller xử lý API & Routing HTTP
│   ├── service/      # Chứa Business Logic (Import, Duyệt bộ thẻ, Học thẻ...)
│   ├── repository/   # Chứa truy vấn thao tác CSDL thông qua Spring Data JPA
│   ├── model/        # Chứa Entity định nghĩa các Bảng trong CSDL
│   └── dto/          # Data Transfer Object (Đối tượng vận chuyển Data)
├── src/main/resources/
│   ├── static/       # Chứa CSS, JavaScript, Images, Asset tĩnh
│   ├── templates/    # Chứa giao diện HTML Thymeleaf (Views)
│   └── application.properties # File cấu hình hệ thống, database
└── pom.xml           # File quản lý thư viện Maven
```
