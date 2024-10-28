package com.weekendwarriors.weekend_warriors_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class WeekendWarriorsBackendApplication {

	@GetMapping("/welcome")
	public String welcome(){
		return "Weekend Warriors Backend: Greetings from Spring Boot Dockerized!";
	}

	public static void main(String[] args) {
		SpringApplication.run(WeekendWarriorsBackendApplication.class, args);
	}

}
