package com.jtok.spring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.config.EnablePublisher;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableJpaRepositories
@EnableTransactionManagement
@EnableScheduling
@EnableIntegration
@EnablePublisher
public class SpringJtokApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringJtokApplication.class, args);
	}

}
