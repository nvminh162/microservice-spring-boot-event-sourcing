package com.nvminh162.employeeservice.command.data;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table( name = "employees")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Employee {

    @Id
    String id;
    String firstName;
    String lastName;
    String kin;
    Boolean isDisciplined;
}
