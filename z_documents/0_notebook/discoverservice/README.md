Giải thích Service Discovery Pattern theo sơ đồ và liên hệ với code hiện tại:

## Service Discovery Pattern - Service Registry Pattern

### Các thành phần trong sơ đồ

#### 1. Client (Người dùng/Ứng dụng)
- Gửi request đến hệ thống
- Không biết địa chỉ cụ thể của các microservice

#### 2. Gateway (API Gateway)
- Điểm vào duy nhất
- Nhận request từ Client
- Tra cứu Service Registry để lấy địa chỉ của các service
- Định tuyến request đến service phù hợp

#### 3. Service Registry (Eureka Server trong code của bạn)
- Lưu trữ danh sách các service đang chạy
- Cung cấp địa chỉ (IP/Port) khi Gateway hỏi
- Nhận đăng ký từ các service khi chúng khởi động

#### 4. Microservices (Product, ShoppingCart, Pricing, Order)
- Các service độc lập
- Tự đăng ký với Service Registry khi khởi động
- Cung cấp business logic

### Luồng hoạt động (theo sơ đồ)

```
Bước 1: Đăng ký (Register)
   ┌─────────────────┐
   │  Microservices  │ ──Register──> ┌──────────────────┐
   │  (Product,      │               │ Service Registry │
   │   ShoppingCart, │               │  (Eureka Server) │
   │   Pricing,      │               └──────────────────┘
   │   Order)        │
   └─────────────────┘
   
   → Khi service khởi động, nó tự đăng ký với Registry
   → Cung cấp: tên service, IP, port, health status

Bước 2: Client gửi request
   Client ──Request──> Gateway

Bước 3: Gateway tra cứu (Get IPs)
   Gateway ──Get IPs──> Service Registry
   Service Registry ──Return IPs──> Gateway
   
   → Gateway hỏi: "Service Product ở đâu?"
   → Registry trả về: "http://192.168.1.10:8080"

Bước 4: Gateway gọi service (Call IPs)
   Gateway ──Call IPs──> Microservices (Product, ShoppingCart, Pricing)
   
   → Gateway dùng địa chỉ vừa nhận để gọi service
   → Service xử lý và trả kết quả
```

### So sánh với code hiện tại

Trong code của bạn:

```
┌─────────────────────┐
│  discoverserver     │  ← Đây là Service Registry (Eureka Server)
│  Port: 8761         │
│  @EnableEurekaServer│
└─────────────────────┘
         ▲
         │ Register
         │
┌─────────────────────┐
│  bookservice        │  ← Đây là Microservice (Eureka Client)
│  Port: 9001         │
│  @EnableDiscovery   │
└─────────────────────┘
```

Hiện tại bạn có:
- Service Registry: `discoverserver` (Eureka Server)
- Microservice: `bookservice` (Eureka Client)

Thiếu:
- Gateway: chưa có API Gateway (có thể dùng Spring Cloud Gateway)
- Client: có thể là web app, mobile app, hoặc service khác

### Ví dụ mở rộng với Gateway

Nếu thêm Gateway, luồng sẽ như sau:

```java
// 1. Book Service đăng ký với Eureka
@EnableDiscoveryClient
public class BookserviceApplication { ... }

// 2. Gateway tra cứu Eureka để tìm bookservice
@EnableDiscoveryClient
public class GatewayApplication {
    // Gateway tự động tra cứu service qua tên "bookservice"
    // Không cần hardcode IP/port
}

// 3. Client gọi qua Gateway
// Client → http://gateway:8080/api/books
// Gateway → tìm "bookservice" trong Eureka
// Gateway → http://bookservice:9001/books
```

### Lợi ích của pattern này

1. Tự động hóa
   - Service tự đăng ký, không cần cấu hình thủ công
   - Gateway tự động tra cứu địa chỉ

2. Linh hoạt
   - Service có thể thay đổi IP/port
   - Có thể scale (nhiều instance cùng tên)

3. Tập trung
   - Gateway là điểm vào duy nhất
   - Dễ quản lý routing, authentication, rate limiting

4. Tách biệt
   - Client không cần biết địa chỉ service
   - Service không cần biết client

### Các pattern liên quan

1. Client-Side Discovery
   - Client tự tra cứu Registry và gọi trực tiếp service
   - Không có Gateway

2. Server-Side Discovery (như sơ đồ)
   - Gateway tra cứu và định tuyến
   - Client chỉ gọi Gateway

### Tóm tắt

- Service Registry (Eureka): nơi các service đăng ký và được tra cứu
- Gateway: điểm vào, tra cứu Registry và định tuyến
- Microservices: tự đăng ký và cung cấp chức năng
- Client: gọi qua Gateway

Code hiện tại đã có Service Registry và một microservice. Có thể bổ sung Gateway để hoàn thiện kiến trúc như sơ đồ.