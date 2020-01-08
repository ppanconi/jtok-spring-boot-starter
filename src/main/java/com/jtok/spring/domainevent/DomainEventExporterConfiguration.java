package com.jtok.spring.domainevent;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.nodes.GroupMember;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.support.SmartLifecycleRoleController;
import org.springframework.integration.zookeeper.config.CuratorFrameworkFactoryBean;
import org.springframework.integration.zookeeper.config.LeaderInitiatorFactoryBean;

import java.util.UUID;

@Configuration
public class DomainEventExporterConfiguration {

    //TODO move in configuration file
    public static final String ZOOKEEPER_QUORUM = "localhost:2183,localhost:2182,localhost:2181";

    public static final int DOMAIN_EVENTS_PARTITION_NUMBER = 3;
    public static final String DOMAIN_NAME = "ecommerce";

    @Bean(name = "curatorClient")
    public CuratorFrameworkFactoryBean curatorFrameworkFactory() {
        return new CuratorFrameworkFactoryBean(DomainEventExporterConfiguration.ZOOKEEPER_QUORUM);
    }

    @Bean
    public DomainEventExporterLeaderInitiatorDefinitions leaderInitiatorDefinitions() {
        return new DomainEventExporterLeaderInitiatorDefinitions(DOMAIN_EVENTS_PARTITION_NUMBER, DOMAIN_NAME);
    }

    @Bean(name = "groupMember")
    public GroupMemberFactoryBean groupMember(CuratorFramework client) {
        return new GroupMemberFactoryBean(client, "/domains/" + DOMAIN_NAME, UUID.randomUUID().toString());
    }



}
