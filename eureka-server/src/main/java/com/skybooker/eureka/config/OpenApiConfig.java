package com.skybooker.eureka.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI eurekaOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("SkyBooker Eureka Server API")
                        .version("1.0")
                        .description("Discovery server helper APIs and service registry access for SkyBooker.")
                        .contact(new Contact().name("SkyBooker Team").email("support@skybooker.com")));
    }
}