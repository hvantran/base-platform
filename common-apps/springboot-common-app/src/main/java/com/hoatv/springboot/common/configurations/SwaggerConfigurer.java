package com.hoatv.springboot.common.configurations;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile({"dev", "test"})
public class SwaggerConfigurer {

    private static final Logger LOGGER = LoggerFactory.getLogger(SwaggerConfigurer.class);

    @Bean
    public OpenAPI springShopOpenAPI() {
        LOGGER.info("Configuring Swagger");
        return new OpenAPI()
                .info(new Info().title("My application API")
                        .description("API documentation application")
                        .version("v0.0.1")
                        .license(new License().name("Apache 2.0").url("http://springdoc.org")))
                .externalDocs(new ExternalDocumentation()
                        .description("API Documentation")
                        .url("https://github.com/hvantran/project-management"));
    }
}
