package com.weekendwarriors.weekend_warriors_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@SpringBootApplication
@RestController
@OpenAPIDefinition(info = @Info(title = "API", version = "1.0", description = "APIs for Weekend Warriors"))
public class WeekendWarriorsBackendApplication {

	@GetMapping("/welcome")
	public String welcome(){
		return "Weekend Warriors Backend: Greetings from Spring Boot Dockerized!";
	}

	public static void main(String[] args)
	{
		SpringApplication.run(WeekendWarriorsBackendApplication.class, args);
	}
}
