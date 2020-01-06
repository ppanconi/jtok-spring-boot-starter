package com.jtok.spring.domainevent;

import org.apache.curator.framework.CuratorFramework;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.support.SmartLifecycleRoleController;
import org.springframework.integration.zookeeper.config.CuratorFrameworkFactoryBean;

@Configuration
public class DomainEventExporterConfiguration {

    //TODO move in configuration file
    public static final String ZOOKEEPER_QUORUM = "localhost:2183,localhost:2182,localhost:2181";

    @Bean(name = "curatorClient")
    public CuratorFrameworkFactoryBean curatorFrameworkFactory() {
        return new CuratorFrameworkFactoryBean(DomainEventExporterConfiguration.ZOOKEEPER_QUORUM);
    }

    @Bean
    public DomainEventExporterLeaderInitiatorDefinitions leaderInitiatorDefinitions() {
        return new DomainEventExporterLeaderInitiatorDefinitions();
    }

}
