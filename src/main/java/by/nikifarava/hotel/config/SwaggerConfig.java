package by.nikifarava.hotel.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI hotelOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Property View Hotels API")
                        .description("RESTful API application for working with hotels")
                        .version("1.0.0"));
    }
}
