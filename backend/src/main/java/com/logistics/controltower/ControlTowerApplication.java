package com.logistics.controltower;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ControlTowerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ControlTowerApplication.class, args);
    }
}
