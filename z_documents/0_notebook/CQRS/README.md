## **Tổng Quan**
CQRS tách biệt việc **ghi dữ liệu (Command)** và **đọc dữ liệu (Query)** thành 2 luồng độc lập.

---

## **📝 LUỒNG COMMAND (Bên Trái - Ghi Dữ Liệu)**

### **1. User Interface → Commands**
- Người dùng thực hiện hành động (tạo, sửa, xóa sách)
- Ví dụ: Tạo sách mới → `CreateBookCommand`

### **2. Command Bus**
- Nhận command và định tuyến đến handler phù hợp
- Giống như một "bưu điện" phân phối công việc

### **3. Command Handler**
- Xử lý business logic
- Gọi Aggregate để thực hiện thay đổi
- Trong code của bạn: `BookCommandController` → `BookAggregate`

### **4. Aggregates (Domain Model)**
- Đại diện cho entity trong domain (BookAggregate)
- Validate business rules
- **Tạo ra Events** (BookCreatedEvent) thay vì trực tiếp lưu vào DB

### **5. Repository → Event Store**
- Lưu **Events** vào Event Store (không phải data thực)
- Event Store lưu lịch sử tất cả thay đổi
- Có thể rebuild lại trạng thái bất kỳ lúc nào

---

## **📊 LUỒNG QUERY (Bên Phải - Đọc Dữ Liệu)**

### **1. User Interface → Query**
- Người dùng yêu cầu xem dữ liệu
- Ví dụ: Lấy danh sách sách, tìm kiếm sách

### **2. Query Facade**
- Nhận query requests
- Gọi Thin Data Layer

### **3. Thin Data Layer**
- Truy vấn trực tiếp vào **Data** (Read Database)
- Tối ưu cho việc đọc (có thể denormalize, cache)

### **4. Data → DTO**
- Trả về data dạng DTO cho UI
- Database này được tối ưu cho READ

---

## **🔄 KẾT NỐI GIỮA 2 LUỒNG: EVENT BUS**

### **Event Handler (Quan trọng!)**
```
Event Store → Event Bus → Event Handler → Data (Read DB)
```

**Cách hoạt động:**
1. Sau khi lưu Event vào Event Store
2. Event được publish lên **Event Bus**
3. **Event Handler** (BookEventsHandler) lắng nghe events
4. Event Handler cập nhật vào **Data** (Read Database)

**Trong code của bạn:**
- `BookEventsHandler` nhận `BookCreatedEvent`
- Lưu vào `BookRepository` → cập nhật database query

---

## **💡 TẠI SAO LẠI TÁCH RA NHƯ VẬY?**

### **Ưu điểm:**
1. **Performance**: Database đọc/ghi tối ưu riêng
2. **Scalability**: Scale read/write độc lập
3. **Flexibility**: Có thể có nhiều read models khác nhau
4. **Event Sourcing**: Lưu lịch sử đầy đủ
5. **Audit Trail**: Biết chính xác ai làm gì, khi nào

### **Ví dụ thực tế trong code của bạn:**

**Command Flow:**
```java
BookRequestModel → CreateBookCommand 
→ BookAggregate.handle() 
→ BookCreatedEvent 
→ Event Store
```

**Event Handler:**
```java
BookCreatedEvent 
→ BookEventsHandler.on() 
→ BookRepository.save() 
→ Database
```

**Query Flow:**
```java
Query Request 
→ BookQueryController 
→ BookRepository.findAll() 
→ Trả về data
```

---

Bạn có muốn tôi giải thích chi tiết hơn phần nào không? Hoặc tôi có thể xem code hiện tại của bạn để chỉ cụ thể hơn?

User: Aggregate là gì

GitHub Copilot: Read [](file:///d%3A/Workspace/course/project/microservice-spring-boot-es/bookservice/src/main/java/com/nvminh162/bookservice/command/aggregate/BookAggregate.java)

# **AGGREGATE LÀ GÌ?**

## **📖 Định Nghĩa**

**Aggregate** là một khái niệm cốt lõi trong **Domain-Driven Design (DDD)** và **Event Sourcing**:

- Là một **nhóm các đối tượng domain** được coi như **1 đơn vị duy nhất** cho việc thay đổi dữ liệu
- Có một **Aggregate Root** (gốc) để kiểm soát tất cả truy cập vào các đối tượng bên trong
- Đảm bảo **tính nhất quán** của business rules

---

## **🎯 VAI TRÒ CỦA AGGREGATE**

### **1. Bảo vệ Business Logic**
```java
@Aggregate
public class BookAggregate {
    @AggregateIdentifier
    String id;  // ID duy nhất định danh Aggregate này
```

- Mỗi Aggregate có **1 ID duy nhất** (`@AggregateIdentifier`)
- ID này đại diện cho **1 thực thể cụ thể** (ví dụ: cuốn sách có ID "book-123")

### **2. Xử Lý Commands**
```java
@CommandHandler
public BookAggregate(CreateBookCommand command) {
    // Validate business rules ở đây
    // Không lưu trực tiếp vào DB
    // Mà phát ra Event
    BookCreatedEvent event = new BookCreatedEvent();
    BeanUtils.copyProperties(command, event);
    AggregateLifecycle.apply(event);  // Phát event
}
```

**Nhiệm vụ:**
- Nhận Command từ Controller
- **Validate business rules** (kiểm tra điều kiện nghiệp vụ)
- **Không lưu trực tiếp vào database**
- Phát ra **Event** nếu hợp lệ

### **3. Quản Lý Trạng Thái (State)**
```java
@EventSourcingHandler
public void on(BookCreatedEvent event) {
    // Cập nhật trạng thái của Aggregate
    this.id = event.getId();
    this.name = event.getName();
    this.author = event.getAuthor();
    this.isReady = event.getIsReady();
}
```

**Nhiệm vụ:**
- Lắng nghe Event vừa phát ra
- **Cập nhật trạng thái** của Aggregate
- Trạng thái này được **khôi phục từ Event Store**

---

## **🔄 CÁCH HOẠT ĐỘNG**

### **Ví dụ: Tạo sách "Java Programming"**

```
1. Command gửi đến:
   CreateBookCommand(id="book-1", name="Java Programming")
   
2. BookAggregate nhận command:
   ├─ Kiểm tra: Tên sách có rỗng không?
   ├─ Kiểm tra: Tác giả hợp lệ?
   └─ ✓ Hợp lệ → Phát BookCreatedEvent
   
3. Event được lưu vào Event Store:
   Event #1: BookCreatedEvent(id="book-1", name="Java Programming")
   
4. Aggregate cập nhật trạng thái:
   BookAggregate.on(event) → this.id = "book-1"
                          → this.name = "Java Programming"
```

---

## **💡 TẠI SAO CẦN AGGREGATE?**

### **1. Đảm Bảo Tính Nhất Quán**
```java
// Sai nếu không dùng Aggregate:
book.setName("New Name");
book.setAuthor("New Author");
database.save(book);  // ❌ Không kiểm tra business rules
```

```java
// Đúng với Aggregate:
UpdateBookCommand cmd = new UpdateBookCommand(...);
// → Aggregate kiểm tra business rules
// → Phát UpdateBookEvent nếu hợp lệ
// ✓ Đảm bảo tính nhất quán
```

### **2. Event Sourcing**
Aggregate không lưu **trạng thái hiện tại**, mà lưu **chuỗi các sự kiện**:

```
Event Store:
- Event #1: BookCreatedEvent (name="Java", author="John")
- Event #2: BookNameUpdatedEvent (name="Java Advanced")
- Event #3: BookAuthorUpdatedEvent (author="Jane")

→ Trạng thái hiện tại = Replay tất cả events
→ Có thể xem lại trạng thái bất kỳ thời điểm nào
```

### **3. Khôi Phục Trạng Thái**
```java
// Khi load lại BookAggregate với id="book-1":
// Axon sẽ:
1. Tìm tất cả events có id="book-1" trong Event Store
2. Replay từng event theo thứ tự
3. Gọi @EventSourcingHandler cho mỗi event
4. → Aggregate có đúng trạng thái hiện tại
```

---

## **🎯 TÓM TẮT**

| **Khía cạnh** | **Mô tả** |
|---------------|-----------|
| **Là gì** | Đối tượng domain quản lý business logic và trạng thái |
| **Nhận** | Commands |
| **Xử lý** | Validate business rules |
| **Phát ra** | Events |
| **Lưu trữ** | Trạng thái được khôi phục từ Events |
| **Mục đích** | Đảm bảo tính nhất quán và audit trail |

**Trong code của bạn:**
- `BookAggregate` = 1 cuốn sách cụ thể
- Mỗi cuốn sách có ID riêng
- Mọi thay đổi đều qua Commands → Events
- Không bao giờ sửa trực tiếp database

Bạn có muốn tôi giải thích thêm về Event Sourcing hoặc các pattern khác không?

User: giải thích rõ lại theo sơ đồ này và ánh xạ luồng theo code

GitHub Copilot: Read [](file:///d%3A/Workspace/course/project/microservice-spring-boot-es/bookservice/src/main/java/com/nvminh162/bookservice/command/controller/BookCommandController.java)

Read [](file:///d%3A/Workspace/course/project/microservice-spring-boot-es/bookservice/src/main/java/com/nvminh162/bookservice/command/event/BookCreatedEvent.java)

Read [](file:///d%3A/Workspace/course/project/microservice-spring-boot-es/bookservice/src/main/java/com/nvminh162/bookservice/command/event/BookEventsHandler.java)

Read [](file:///d%3A/Workspace/course/project/microservice-spring-boot-es/bookservice/src/main/java/com/nvminh162/bookservice/command/data/Book.java)

Read [](file:///d%3A/Workspace/course/project/microservice-spring-boot-es/bookservice/src/main/java/com/nvminh162/bookservice/command/data/BookRepository.java)

Read [](file:///d%3A/Workspace/course/project/microservice-spring-boot-es/bookservice/src/main/java/com/nvminh162/bookservice/command/model/BookRequestModel.java)

# **📊 GIẢI THÍCH LUỒNG CQRS THEO SƠ ĐỒ VÀ CODE**

## **🔴 LUỒNG COMMAND - TẠO SÁCH MỚI**

### **BƯỚC 1: USER INTERFACE** 
```
Sơ đồ: User Interface → Commands
```

**Request từ người dùng:**
```json
POST /api/v1/books
{
  "name": "Java Programming",
  "author": "John Doe"
}
```

**Code:** BookRequestModel.java
```java
public class BookRequestModel {
    String name;
    String author;
    Boolean isReady;
}
```
➡️ **DTO nhận request từ client**

---

### **BƯỚC 2: COMMAND BUS**
```
Sơ đồ: Commands → Command Bus → Command
```

**Code:** BookCommandController.java
```java
@PostMapping
public String createBook(@RequestBody BookRequestModel model) {
    // 1. Tạo Command với ID random
    CreateBookCommand command = new CreateBookCommand(
        UUID.randomUUID().toString(), 
        model.getName(), 
        model.getAuthor(), 
        true
    );
    
    // 2. CommandGateway = Command Bus
    // Gửi command và đợi kết quả
    return commandGateway.sendAndWait(command);
}
```

**Vai trò của `CommandGateway` (Command Bus):**
- Nhận command
- Tìm Aggregate phù hợp dựa vào `@TargetAggregateIdentifier`
- Định tuyến command đến handler

**Code:** CreateBookCommand.java
```java
public class CreateBookCommand {
    @TargetAggregateIdentifier  // ← Liên kết với BookAggregate
    String id;
    String name;
    String author;
    Boolean isReady;
}
```

---

### **BƯỚC 3: COMMAND HANDLER**
```
Sơ đồ: Command → Command Handler → Domain Model
```

**Code:** BookAggregate.java
```java
@Aggregate  // ← Domain Model
public class BookAggregate {
    @AggregateIdentifier
    String id;
    String name;
    String author;
    Boolean isReady;

    @CommandHandler  // ← Command Handler
    public BookAggregate(CreateBookCommand command) {
        // Validate business logic ở đây
        // VD: if (command.getName().isEmpty()) throw exception
        
        // Tạo event
        BookCreatedEvent event = new BookCreatedEvent();
        BeanUtils.copyProperties(command, event);
        
        // Phát event (KHÔNG lưu database)
        AggregateLifecycle.apply(event);
    }
}
```

**📌 Quan trọng:** Aggregate **KHÔNG lưu trực tiếp vào database**, chỉ phát **Event**!

---

### **BƯỚC 4: AGGREGATES → REPOSITORY → EVENT STORE**
```
Sơ đồ: Domain Model → Repository → Event Store → Events
```

**Code:** BookAggregate.java
```java
@EventSourcingHandler  // Lắng nghe event vừa phát
public void on(BookCreatedEvent event) {
    // Cập nhật trạng thái Aggregate
    this.id = event.getId();
    this.name = event.getName();
    this.author = event.getAuthor();
    this.isReady = event.getIsReady();
}
```

**Axon Framework tự động:**
1. Lưu `BookCreatedEvent` vào **Event Store** (database riêng)
2. Gọi `@EventSourcingHandler` để cập nhật trạng thái Aggregate
3. Publish event lên **Event Bus**

---

## **🟠 EVENT BUS - CẦU NỐI**

### **BƯỚC 5: EVENT BUS → EVENT HANDLER**
```
Sơ đồ: Event Store → Event Bus → Event Handler
```

**Code:** BookEventsHandler.java
```java
@Component  // Spring quét và đăng ký handler này
public class BookEventsHandler {
    private final BookRepository bookRepository;

    @EventHandler  // Lắng nghe event từ Event Bus
    public void on(BookCreatedEvent event) {
        // Chuyển Event → Entity
        Book book = new Book();
        BeanUtils.copyProperties(event, book);
        
        // Lưu vào database (Read Model)
        bookRepository.save(book);
    }
}
```

**Vai trò:**
- Lắng nghe events từ Event Bus
- Cập nhật **Read Database** (data cho Query)
- Tách biệt với Command processing

---

### **BƯỚC 6: EVENT HANDLER → DATA**
```
Sơ đồ: Event Handler → Data (Read Database)
```

**Code:** Book.java + BookRepository.java
```java
@Entity
@Table(name = "books")
public class Book {
    @Id
    String id;
    String name;
    String author;
    Boolean isReady;
}

public interface BookRepository extends JpaRepository<Book, String> {}
```

➡️ Data này dùng cho **Query side** (đọc dữ liệu)

---

## **📈 LUỒNG HOÀN CHỈNH - VÍ DỤ THỰC TẾ**

```
📱 CLIENT: POST /api/v1/books
         { "name": "Java Book", "author": "John" }
              ↓
              
1️⃣ BookCommandController.createBook()
   - Nhận BookRequestModel
   - Tạo CreateBookCommand(id=uuid, name="Java Book", author="John")
   - commandGateway.sendAndWait(command)  ← Command Bus
              ↓
              
2️⃣ Axon Command Bus
   - Đọc @TargetAggregateIdentifier từ CreateBookCommand
   - Tìm BookAggregate có @AggregateIdentifier tương ứng
   - Gọi @CommandHandler
              ↓
              
3️⃣ BookAggregate @CommandHandler
   - Validate business logic
   - Tạo BookCreatedEvent
   - AggregateLifecycle.apply(event)  ← Phát event
              ↓
              
4️⃣ Axon Framework (tự động)
   - Lưu BookCreatedEvent vào Event Store
   - Gọi BookAggregate.on(event) → Cập nhật state
   - Publish event lên Event Bus
              ↓
              
5️⃣ BookEventsHandler @EventHandler
   - Nhận BookCreatedEvent từ Event Bus
   - Chuyển Event → Book entity
   - bookRepository.save(book)  ← Lưu vào Database
              ↓
              
✅ DATABASE (Read Model)
   books table: id=uuid, name="Java Book", author="John"
```

---

## **🔵 SO SÁNH EVENT STORE vs DATABASE**

| **Event Store** | **Read Database** |
|-----------------|-------------------|
| Lưu **BookCreatedEvent** | Lưu **Book entity** |
| Lưu lịch sử thay đổi | Lưu trạng thái hiện tại |
| Không thể sửa/xóa | Có thể query nhanh |
| Dùng để rebuild state | Dùng cho Query side |

---

## **💡 TẠI SAO PHỨC TẠP NHƯ VẬY?**

### **✅ Ưu điểm:**

**1. Audit Trail:**
```
Event Store:
- 10:00 → BookCreatedEvent(id=1, name="Java")
- 10:30 → BookNameChangedEvent(id=1, oldName="Java", newName="Java 21")
- 11:00 → BookDeletedEvent(id=1)

→ Biết chính xác ai làm gì, khi nào
```

**2. Rebuild State:**
```java
// Có thể rebuild lại trạng thái bất kỳ
BookAggregate at 10:15 → name = "Java"
BookAggregate at 10:45 → name = "Java 21"
BookAggregate at 11:15 → deleted = true
```

**3. Tách biệt Read/Write:**
```
Write: Command → Event Store (tối ưu cho ghi)
Read:  Query → Database (tối ưu cho đọc, có index)
```

**4. Scalability:**
- Event Store có thể dùng Kafka, RabbitMQ
- Read Database có thể scale độc lập
- Có thể có nhiều Read Models khác nhau

---

## **📋 TÓM TẮT MAPPING**

| **Sơ đồ** | **Code** |
|-----------|----------|
| User Interface | `POST /api/v1/books` |
| Commands | `BookRequestModel` |
| Command Bus | `CommandGateway.sendAndWait()` |
| Command | `CreateBookCommand` với `@TargetAggregateIdentifier` |
| Command Handler | `BookAggregate` constructor với `@CommandHandler` |
| Domain Model | `BookAggregate` với `@Aggregate` |
| Repository (Event) | Axon Framework tự động |
| Event Store | Axon event database (tự động) |
| Event Bus | Axon Event Bus (tự động) |
| Event Handler | `BookEventsHandler.on()` với `@EventHandler` |
| Data | `Book` entity + `BookRepository.save()` |

---
