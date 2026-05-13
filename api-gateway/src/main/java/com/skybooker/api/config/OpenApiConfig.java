package com.skybooker.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI gatewayOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("SkyBooker API Gateway")
                        .version("1.0")
                        .description("Single secure entry point for all SkyBooker microservices")
                        .contact(new Contact()
                                .name("SkyBooker Team")
                                .email("support@skybooker.com")));
    }
}
