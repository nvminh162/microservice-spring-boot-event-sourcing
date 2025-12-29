package com.nvminh162.bookservice.command.aggregate;

import com.nvminh162.bookservice.command.command.CreateBookCommand;
import com.nvminh162.bookservice.command.event.BookCreatedEvent;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;
import org.springframework.beans.BeanUtils;

@Aggregate // định nghĩa đây là Aggregate
@NoArgsConstructor // yêu cầu bắt buộc khởi tạo này của Axon
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookAggregate {
    /*
    Dòng này liên kết @TargetAggregateIdentifier (BookAggregate) (CreateBookCommand) giúp khi phát ra command sẽ tới (Boook), đây cũng là khóa duy nhất cho thực thể Book giúp quản lý trạng thái thay đổi
    VD: Tạo 1 quyển sách `Java Book` => Phát ra 10 event để thay đổi quyển sách `Java Book` => 10 event này phải có cùng @AggregateIdentifier để biết 10 event này thuộc thực thể nào? => để khôi phục data của thực thể đó!
    */
    @AggregateIdentifier
    String id;
    String name;
    String author;
    Boolean isReady;

    // Tạo command handler => public 1 event
    @CommandHandler
    public BookAggregate(CreateBookCommand command) { // => mục đích giúp cho app nhận biết Book Aggreate được init => nếu k có hàm này sẽ Exception
        BookCreatedEvent event = new BookCreatedEvent();
        BeanUtils.copyProperties(command, event); // Cách này thay vì gắn từng field => Copy tất cả dữ liệu thuộc tính của Object truyền vào tới Object đích
        AggregateLifecycle.apply(event); // đối tượng vòng đồi của Aggreate với method apply() để public event
    }

    // Lắng nghe event vừa public
    @EventSourcingHandler
    // (Annotation Axon Framework): Chỉ định method này sẽ xử lý sự kiện được phát ra, sử dụng nó để cập nhật trạng thái của Aggregate
    public void on(BookCreatedEvent event) { // event được phát CreateBookCommand => lắng nghe => thay đổi Aggregate
        // cập nhật lại => do tạo mới 1 quyền sách cần thay đổi tất cả thuộc tính của quyền sách này => xác định trạng thái của quyển sách
        this.id = event.getId();
        this.name = event.getName();
        this.author = event.getAuthor();
        this.isReady = event.getIsReady();
    }
}
