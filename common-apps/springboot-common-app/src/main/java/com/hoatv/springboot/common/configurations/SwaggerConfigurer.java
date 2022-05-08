package com.hoatv.springboot.common.configurations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

@Configuration
@Profile({"dev", "test"})
public class SwaggerConfigurer {

    private static final Logger LOGGER = LoggerFactory.getLogger(SwaggerConfigurer.class);

    @Bean
    public Docket api() {
        LOGGER.info("Initialize Swagger");
        return new Docket(DocumentationType.SWAGGER_2)
            .select()
            .apis(RequestHandlerSelectors.basePackage("com.hoatv"))
            .paths(PathSelectors.any())
            .build();
    }
}
