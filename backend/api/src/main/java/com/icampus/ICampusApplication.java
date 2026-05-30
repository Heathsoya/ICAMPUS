package com.icampus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(
    scanBasePackages = "com.icampus",
    exclude = {DataSourceAutoConfiguration.class}
)
public class ICampusApplication {
    public static void main(String[] args) {
        SpringApplication.run(ICampusApplication.class, args);
    }
}//你好
//test
//淘汰赛