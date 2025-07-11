package com.example.xadessigner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;


@SpringBootApplication
@ComponentScan(basePackages = "com.example.xadessigner")
public class XadesSignerApplication {
    public static void main(String[] args) {
        SpringApplication.run(XadesSignerApplication.class, args);
    }
}

