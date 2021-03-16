package com.karrostech.services.infra;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
@Slf4j
public class DebeziumPlaybookApplication {

	public static void main(String[] args) {
		ApplicationContext applicationContext = SpringApplication.run(DebeziumPlaybookApplication.class, args);



	}

}
