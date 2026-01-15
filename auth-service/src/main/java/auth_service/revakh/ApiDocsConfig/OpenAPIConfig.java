package auth_service.revakh.ApiDocsConfig;


import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {
    @Bean
    public OpenAPI customOpenApi(){
        return new OpenAPI()
                //information about the porject
                .info(new Info()
                        .title("Revakh financial application")
                        .description("Comprehensive financial management microservices with AI chatbot integration.\n" +
                                " \n" +
                                " Features:\n" +
                                " - User authentication & authorization\n" +
                                " - Wallet management\n" +
                                " - Transaction tracking\n" +
                                " - Budget planning\n" +
                                " - Category management\n" +
                                " - AI chatbot")
                        .version("version 1.0")
                        .contact(new Contact()
                                .email("tauroshanjeeth@gmail.com")
                                .name("Jeethan Tauro")
                        )
                )
                //security and its schemes
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components().addSecuritySchemes("Bearer Authentication",new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("Enter the jwt token")
                ))
                .addServersItem(new Server().
                        url("http://localhost:8080")
                        .description("Development and testing server")
                );
    }
}
