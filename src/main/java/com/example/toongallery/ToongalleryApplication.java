package com.example.toongallery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaAuditing
public class ToongalleryApplication {

	public static void main(String[] args) {
		SpringApplication.run(ToongalleryApplication.class, args);
	}

}
