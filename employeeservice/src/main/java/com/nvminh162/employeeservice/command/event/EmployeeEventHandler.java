package com.nvminh162.employeeservice.command.event;

import com.nvminh162.employeeservice.command.data.Employee;
import com.nvminh162.employeeservice.command.data.EmployeeRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Component
@RequiredArgsConstructor
public class EmployeeEventHandler {
    EmployeeRepository employeeRepository;

    @EventHandler
    public void on(EmployeeCreatedEvent event) {
        Employee employee = new Employee();
        BeanUtils.copyProperties(event, employee);
        employeeRepository.save(employee);
    }
}
