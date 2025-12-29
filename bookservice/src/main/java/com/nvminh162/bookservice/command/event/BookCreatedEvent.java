package com.nvminh162.bookservice.command.event;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookCreatedEvent {
    String id;
    String name;
    String author;
    Boolean isReady;
}
