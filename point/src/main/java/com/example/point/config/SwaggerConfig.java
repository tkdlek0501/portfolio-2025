package com.example.point.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "포인트 API 명세",
                version = "1.0.0",
                description = "포인트 API 명세"),
        security = @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "Authorization")
)
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization");

        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList("Authorization");

        return new OpenAPI()
                .components(new Components().addSecuritySchemes("Authorization", securityScheme))
                .security(Collections.singletonList(securityRequirement))
                .addServersItem(new Server().url("http://localhost:8080/api/point-server"));
    }
}
