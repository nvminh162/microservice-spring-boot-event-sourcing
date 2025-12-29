package com.nvminh162.bookservice.command.event;

import com.nvminh162.bookservice.command.data.Book;
import com.nvminh162.bookservice.command.data.BookRepository;
import lombok.RequiredArgsConstructor;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component // ứng dụng quét tới class này để lắng nghe (Scan class được đánh dấu)
@RequiredArgsConstructor
public class BookEventsHandler {
    private final BookRepository bookRepository;

    @EventHandler // tại đây mới thực sự xử lý event đó
    public void on(BookCreatedEvent event) {
        Book book = new Book();
        BeanUtils.copyProperties(event, book);
        bookRepository.save(book);
    }
}
