package com.hoatv.webflux;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@SpringBootApplication
@EnableR2dbcRepositories
public class WebfluxApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebfluxApplication.class, args);
    }

    @Autowired
    private EndpointSettingRepository endpointSettingRepository;

    @Bean
    RouterFunction<ServerResponse> composedRoutes() {
        return RouterFunctions.route(
                GET("/webflux/ext-rest/settings"),
                request -> ok().body(endpointSettingRepository.findAll(), EndpointSetting.class)
        );
    }
}
