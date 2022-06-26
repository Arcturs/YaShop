package ru.vrn.vsu.csf.asashina.yandexproject.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfiguration {

    @Bean
    public GroupedOpenApi cinemaGroupApi() {
        return GroupedOpenApi.builder()
                .group("all")
                .pathsToMatch("/**")
                .build();
    }

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Mega Market Open API")
                        .version("1.0")
                        .description("Вступительное задание в Летнюю Школу Бэкенд Разработки Яндекса 2022")
                        .contact(new Contact()
                                .name("Anastasiya Sashina")
                                .email("yaninastya2010@yandex.ru")
                        )
                );
    }
}
