package com.nvminh162.bookservice.command.controller;

import com.nvminh162.bookservice.command.command.CreateBookCommand;
import com.nvminh162.bookservice.command.model.BookRequestModel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/books")
@RequiredArgsConstructor
public class BookCommandController {
    private final CommandGateway commandGateway; // Phát đi những Event

    @PostMapping
    public String createBook(@RequestBody BookRequestModel model) {
        CreateBookCommand command = new CreateBookCommand(UUID.randomUUID().toString(), model.getName(), model.getAuthor(), true);
        return commandGateway.sendAndWait(command);
    }


}
