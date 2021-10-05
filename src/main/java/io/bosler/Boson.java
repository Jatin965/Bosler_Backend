package io.bosler;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@OpenAPIDefinition(
//		tags = {
//				@Tag(name = "Create", description = "Create operations."),
//				@Tag(name = "Retrieve", description = "Get operations."),
//				@Tag(name = "Delete", description = "Delete operations."),
//		},
		info = @Info(
				title = "Bosler.io API",
				version = "1.0.1",
				contact = @Contact(
						name = "Bosler.io API Support",
						url = "https://bosler.io/contact",
						email = "techsupport@bosler.io")
		)
)

// temporarily disabled
@SecurityScheme(
		name = "bearerAuth",
		type = SecuritySchemeType.HTTP,
		bearerFormat = "JWT",
		scheme = "bearer"
)

@SpringBootApplication
public class Boson {

	public static void main(String[] args) {
		SpringApplication.run(Boson.class, args);
	}

	@Bean
	PasswordEncoder passwordEncoder(){
		return new BCryptPasswordEncoder();
	}

}

