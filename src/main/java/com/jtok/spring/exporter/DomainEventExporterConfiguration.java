package com.jtok.spring.exporter;

import org.apache.curator.framework.CuratorFramework;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.integration.zookeeper.config.CuratorFrameworkFactoryBean;

import java.util.UUID;

@Configuration
@EnableConfigurationProperties(DomainConfigs.class)
public class DomainEventExporterConfiguration {

    //TODO move in configuration file
    public static final String ZOOKEEPER_QUORUM = "localhost:2183,localhost:2182,localhost:2181";

    @Bean(name = "curatorClient")
    public CuratorFrameworkFactoryBean curatorFrameworkFactory() {
        return new CuratorFrameworkFactoryBean(DomainEventExporterConfiguration.ZOOKEEPER_QUORUM);
    }

    @Bean
    public DomainEventExporterLeaderInitiatorDefinitions leaderInitiatorDefinitions(Environment environment) {
        return new DomainEventExporterLeaderInitiatorDefinitions(environment);
    }

    @Bean(name = "groupMember")
    public GroupMemberFactoryBean groupMember(CuratorFramework client, DomainConfigs domainConfigs) {
        return new GroupMemberFactoryBean(client, "/domains/" + domainConfigs.getName(), UUID.randomUUID().toString());
    }

}
