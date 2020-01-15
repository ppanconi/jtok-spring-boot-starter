package it.plansoft.ecommerce;

import com.jtok.spring.exporter.EnableDomainEventExporter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableDomainEventExporter
@EntityScan({"com.jtok.spring.domainevent","it.plansoft.ecommerce"})
@EnableJpaRepositories({"com.jtok.spring.domainevent","it.plansoft.ecommerce"})
//@EnableTransactionManagement
//@EnableScheduling
//@EnableIntegration
public class SpringJtokApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringJtokApplication.class, args);
	}

}
