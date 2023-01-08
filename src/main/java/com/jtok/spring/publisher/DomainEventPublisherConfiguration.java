package com.jtok.spring.publisher;

import org.apache.curator.framework.CuratorFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.integration.zookeeper.config.CuratorFrameworkFactoryBean;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.UUID;

@Configuration
@EnableConfigurationProperties(DomainConfigs.class)
@EnableScheduling
//@EntityScan({"com.jtok.spring.domainevent"})
//@EnableJpaRepositories({"com.jtok.spring.domainevent"})
public class DomainEventPublisherConfiguration {

    public static final String ZOOKEEPER_QUORUM = "jtok.pub.zookeeperQuorum";

    private static final Logger log = LoggerFactory.getLogger(DomainEventPublisherConfiguration.class);
    
    @Bean(name = "curatorClient")
    public CuratorFrameworkFactoryBean curatorFrameworkFactory(Environment environment) {
        String zookeeperQuorum = environment.getProperty(ZOOKEEPER_QUORUM);
        return new CuratorFrameworkFactoryBean(zookeeperQuorum);
    }

    @Bean
    public DomainEventPublisherLeaderInitiatorDefinitions leaderInitiatorDefinitions(Environment environment) {
        return new DomainEventPublisherLeaderInitiatorDefinitions(environment);
    }

    @Bean(name = "groupMember")
    public GroupMemberFactoryBean groupMember(CuratorFramework client, DomainConfigs domainConfigs) {
        return new GroupMemberFactoryBean(client, "/domains/" + domainConfigs.getName(), UUID.randomUUID().toString());
    }

//    @Bean
//    @Autowired
//    public DomainEventPublisher domainEventExporter(DomainEventRepository repository, KafkaTemplate<String, String> kafkaTemplate, GenericApplicationContext context) {
//        return new DomainEventPublisherKafkaSupport(repository, kafkaTemplate, context);
//    }

//    @Bean
//    @Autowired
//    DomainEventProcessor domainEventProcessor(DomainEventRepository repository) {
//        return new DomainEventProcessor(repository);
//    }
}
