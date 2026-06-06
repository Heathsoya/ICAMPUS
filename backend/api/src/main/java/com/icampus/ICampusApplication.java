package com.icampus;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.icampus.infra.persistence.mapper")
@SpringBootApplication(scanBasePackages = "com.icampus")
public class ICampusApplication {
    public static void main(String[] args) {
        SpringApplication.run(ICampusApplication.class, args);
    }
}//你好
//test
//淘汰赛