package com.icampus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.icampus")
public class ICampusApplication {
    public static void main(String[] args) {
        SpringApplication.run(ICampusApplication.class, args);
    }
}