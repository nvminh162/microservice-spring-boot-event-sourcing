package com.nvminh162.employeeservice.command.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateEmployeeCommand {

    @TargetAggregateIdentifier
    String id;
    String firstName;
    String lastName;
    String kin;
    Boolean isDisciplined;
}
