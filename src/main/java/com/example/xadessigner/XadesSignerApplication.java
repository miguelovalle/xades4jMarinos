package com.example.xadessigner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import com.example.xadessigner.config.XadesConfigProperties;


@SpringBootApplication
@ComponentScan(basePackages = "com.example.xadessigner")
@EnableConfigurationProperties(XadesConfigProperties.class)
public class XadesSignerApplication {
    public static void main(String[] args) {
        SpringApplication.run(XadesSignerApplication.class, args);
    }
}

