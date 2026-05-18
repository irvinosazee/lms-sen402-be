package com.lms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Application entry point. @SpringBootApplication bundles @Configuration + @ComponentScan + auto-config. */
@SpringBootApplication
public class LmsBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(LmsBackendApplication.class, args);
    }
}
