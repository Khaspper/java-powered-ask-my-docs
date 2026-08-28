package com.khaspper.askmydocs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AskmydocsApplication {

	public static void main(String[] args) {
		SpringApplication.run(AskmydocsApplication.class, args);
	}

}
