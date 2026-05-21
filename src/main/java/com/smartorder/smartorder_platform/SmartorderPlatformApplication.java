package com.smartorder.smartorder_platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.smartorder")
public class SmartorderPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartorderPlatformApplication.class, args);
    }
}