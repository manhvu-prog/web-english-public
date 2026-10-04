package com.example.ankard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Lớp khởi chạy chính của ứng dụng Ankard Flashcard.
 * Sử dụng Spring Boot để tự động cấu hình và chạy ứng dụng.
 */
@SpringBootApplication
public class AnkardApplication {
    public static void main(String[] args) {
        SpringApplication.run(AnkardApplication.class, args);
    }
}