package com.karrostech.services.infra;

import com.karrostech.services.infra.properties.DbzProperties;
import com.karrostech.services.infra.properties.TopicProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
@Slf4j
public class DebeziumPlaybookApplication {

	public static void main(String[] args) {
		ApplicationContext applicationContext = SpringApplication.run(DebeziumPlaybookApplication.class, args);

		DbzProperties debeziumProperties = applicationContext.getBean(DbzProperties.class);
		log.info("Debezium Configuration: {}", debeziumProperties.toString());

		TopicProperties topicProperties = applicationContext.getBean(TopicProperties.class);
		log.info("TopicProperties Configuration: {}", topicProperties.toString());


	}

}
