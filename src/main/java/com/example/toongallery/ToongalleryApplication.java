package com.example.toongallery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaAuditing
@EnableCaching//캐시 활성화
public class ToongalleryApplication {

	public static void main(String[] args) {
		SpringApplication.run(ToongalleryApplication.class, args);
	}

}
