package com.hoatv.spring.cloud.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Cloud Gateway Application (Reactive WebFlux)
 * 
 * Note: Does not use springboot-common-app dependency because it contains
 * servlet-based components incompatible with reactive WebFlux architecture.
 * Logging configuration is provided by local logback-spring.xml.
 */
@SpringBootApplication
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class);
    }
}
