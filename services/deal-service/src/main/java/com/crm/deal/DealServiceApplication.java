package com.crm.deal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class DealServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(DealServiceApplication.class, args);
    }
}
