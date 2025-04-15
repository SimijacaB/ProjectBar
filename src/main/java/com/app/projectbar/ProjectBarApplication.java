package com.app.projectbar;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication(scanBasePackages = "com.app.projectbar")
public class ProjectBarApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProjectBarApplication.class, args);


		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

		String adminPassword = encoder.encode("admin123");
		String meseroPassword = encoder.encode("mesero123");

		System.out.println("meseroPassword = " + meseroPassword);
		System.out.println("adminPassword = " + adminPassword);
	}




}
