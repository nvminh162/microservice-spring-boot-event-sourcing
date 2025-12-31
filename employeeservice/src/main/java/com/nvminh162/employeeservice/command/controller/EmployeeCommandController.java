package com.nvminh162.employeeservice.command.controller;

import com.nvminh162.employeeservice.command.command.CreateEmployeeCommand;
import com.nvminh162.employeeservice.command.model.CreateEmployeeModel;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EmployeeCommandController {
    CommandGateway commandGateway;

    @PostMapping
    public String addEmployee(@Valid @RequestBody CreateEmployeeModel model) {
        CreateEmployeeCommand command = new CreateEmployeeCommand(UUID.randomUUID().toString(), model.getFirstName(), model.getLastName(), model.getKin(), false);
        return commandGateway.sendAndWait(command);
    }
}
