package com.app.reporte;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ReportesApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReportesApplication.class, args);
	}

}
