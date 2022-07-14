package com.hoatv.ext.endpoint;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableAutoConfiguration
@ComponentScan({"com.hoatv.ext.endpoint", "com.hoatv.springboot.common"})
public class ExtEndpointApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExtEndpointApplication.class);
    }
}
