package com.example.user.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @io.swagger.v3.oas.annotations.info.Info(title = "API Documents", version = "1", description = "회원 API"),
        security = @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "Authorization")
)
@io.swagger.v3.oas.annotations.security.SecurityScheme(
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER,
        name = "Authorization",
        paramName = "Authorization"
)
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() { // swagger Failed to fetch 오류
        return new OpenAPI();
    }
}
