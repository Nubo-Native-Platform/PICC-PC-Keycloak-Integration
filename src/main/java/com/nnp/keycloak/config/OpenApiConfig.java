package com.nnp.keycloak.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

/**
 * OpenAPI / Swagger documentation configuration for Keycloak Integration Service.
 */
@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "BearerAuth";

    @org.springframework.beans.factory.annotation.Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI keycloakOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("PICC-PC-Keycloak-Integration REST API")
                        .description("Enterprise REST API integration microservice for Keycloak IAM. "
                                + "Provides automated realm, client, user, group, and role mapping governance "
                                + "with in-memory caching for the Nubo Native Platform (NNP).")
                        .version("0.0.1-SNAPSHOT")
                        .contact(new Contact()
                                .name("Nubo Native Platform Team")
                                .email("contribution@nubons.com")
                                .url("https://github.com/Nubo-Native-Platform/PICC-PC-Keycloak-Integration"))
                        .license(new License()
                                .name("Apache License 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .servers(List.of(
                        new Server().url("/").description("Current Server Context"),
                        new Server().url("http://localhost:" + serverPort).description("Local Development Server")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components().addSecuritySchemes(
                        SECURITY_SCHEME_NAME,
                        new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Enter JWT Bearer token for authorized requests")));
    }
}

