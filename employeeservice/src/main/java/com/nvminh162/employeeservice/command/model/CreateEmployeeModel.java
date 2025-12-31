package com.nvminh162.employeeservice.command.model;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateEmployeeModel {

    @NotBlank(message = "First name is mandatory")
    String firstName;
    @NotBlank(message = "Last name is mandatory")
    String lastName;
    @NotBlank(message = "KIN is mandatory")
    String kin;
}
