package com.nvminh162.employeeservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan({"com.nvminh162.employeeservice", "com.nvminh162.commonservice"})
public class EmployeeserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(EmployeeserviceApplication.class, args);
	}

}
