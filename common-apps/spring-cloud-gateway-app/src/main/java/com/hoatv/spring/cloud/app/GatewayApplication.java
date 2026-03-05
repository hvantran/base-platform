package com.hoatv.spring.cloud.app;

import com.hoatv.springboot.common.configurations.InitializeConfigurations;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

/**
 * Spring Cloud Gateway Application (Reactive WebFlux)
 * 
 * Note: Excludes InitializeConfigurations from springboot-common-app because it contains
 * servlet-based components (ServletListenerRegistrationBean) incompatible with reactive WebFlux.
 */
@SpringBootApplication
@ComponentScan(
    basePackages = {"com.hoatv"},
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = {InitializeConfigurations.class}
    )
)
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class);
    }
}
