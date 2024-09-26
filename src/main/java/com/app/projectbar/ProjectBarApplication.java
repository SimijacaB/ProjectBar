package com.app.projectbar;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.app.projectbar")
public class ProjectBarApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProjectBarApplication.class, args);
	}


}
