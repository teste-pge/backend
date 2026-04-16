package com.rideflow.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI rideFlowOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("RideFlow API")
                        .description("API REST para sistema de corridas em tempo real. "
                                + "Gerencia corridas, motoristas e notificações via SSE.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("RideFlow Team")
                                .email("contato@rideflow.com")))
                .externalDocs(new ExternalDocumentation()
                        .description("Documentação completa do projeto")
                        .url("https://github.com/teste-pge/backend"))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Desenvolvimento local"),
                        new Server().url("http://backend:8080").description("Docker Compose")))
                .tags(List.of(
                        new Tag().name("Corridas").description("Criação, consulta, aceitação e rejeição de corridas"),
                        new Tag().name("Motoristas").description("Consulta de motoristas e disponibilidade"),
                        new Tag().name("Notificações").description("Stream SSE de notificações em tempo real")));
    }
}
