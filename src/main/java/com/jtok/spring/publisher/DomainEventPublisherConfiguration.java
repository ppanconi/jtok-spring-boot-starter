package com.jtok.spring.publisher;

import com.jtok.spring.domainevent.DomainEventProcessor;
import com.jtok.spring.domainevent.DomainEventRepository;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.integration.zookeeper.config.CuratorFrameworkFactoryBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.UUID;

@Configuration
@EnableConfigurationProperties(DomainConfigs.class)
@EnableScheduling
@EntityScan({"com.jtok.spring.domainevent"})
@EnableJpaRepositories({"com.jtok.spring.domainevent"})
public class DomainEventPublisherConfiguration {

    //TODO move in configuration file
    public static final String ZOOKEEPER_QUORUM = "localhost:2183,localhost:2182,localhost:2181";

    @Bean(name = "curatorClient")
    public CuratorFrameworkFactoryBean curatorFrameworkFactory() {
        return new CuratorFrameworkFactoryBean(DomainEventPublisherConfiguration.ZOOKEEPER_QUORUM);
    }

    @Bean
    public DomainEventPublisherLeaderInitiatorDefinitions leaderInitiatorDefinitions(Environment environment) {
        return new DomainEventPublisherLeaderInitiatorDefinitions(environment);
    }

    @Bean(name = "groupMember")
    public GroupMemberFactoryBean groupMember(CuratorFramework client, DomainConfigs domainConfigs) {
        return new GroupMemberFactoryBean(client, "/domains/" + domainConfigs.getName(), UUID.randomUUID().toString());
    }

    @Bean
    @Autowired
    public DomainEventPublisher domainEventExporter(DomainEventRepository repository, KafkaTemplate<String, String> kafkaTemplate) {
        return new DomainEventPublisherKafka(repository, kafkaTemplate);
    }

    @Bean
    @Autowired
    DomainEventProcessor domainEventProcessor(DomainEventRepository repository) {
        return new DomainEventProcessor(repository);
    }
}
