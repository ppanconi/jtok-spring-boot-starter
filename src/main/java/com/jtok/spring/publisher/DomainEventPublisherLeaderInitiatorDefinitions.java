package com.jtok.spring.publisher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.Environment;
import org.springframework.integration.context.IntegrationContextUtils;
import org.springframework.integration.support.SmartLifecycleRoleController;
import org.springframework.integration.zookeeper.config.LeaderInitiatorFactoryBean;
import org.springframework.lang.Nullable;

import java.util.HashMap;
import java.util.Map;

public class DomainEventPublisherLeaderInitiatorDefinitions implements
//        BeanDefinitionRegistryPostProcessor
        BeanFactoryPostProcessor,
        BeanPostProcessor
{

    private static final Logger log = LoggerFactory.getLogger(DomainEventPublisherLeaderInitiatorDefinitions.class);

    private int domainEventsPartitionNumber;
    private String domainName;

    private Map<String, String> rolesToExporterNames = new HashMap<>();

    public DomainEventPublisherLeaderInitiatorDefinitions(Environment environment) {
        BindResult<DomainConfigs> result = Binder.get(environment)
                .bind("jtok.domain", DomainConfigs.class);
        DomainConfigs properties = result.get();

        this.domainEventsPartitionNumber = properties.getPartitions();
        this.domainName = properties.getName();
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException  {

        for (int i = 0; i < domainEventsPartitionNumber; i++) {

            String exporterName = "domainEventExporter_" + i;
            String role = exporterName;

            this.rolesToExporterNames.put(role, exporterName);

            BeanDefinitionBuilder initiatorBuilder =
                    BeanDefinitionBuilder.rootBeanDefinition(LeaderInitiatorFactoryBean.class)
                            .addPropertyReference("client", "curatorClient")
                            .addPropertyValue("path", "/domainEvents/" + domainName + "/" + i + "/")
                            .addPropertyValue("role", role);

            ((DefaultListableBeanFactory) beanFactory).
                    registerBeanDefinition("domainEventExporterLeaderInitiator_" + i,
                            initiatorBuilder.getBeanDefinition());

            BeanDefinitionBuilder taskBuilder =
                    BeanDefinitionBuilder.rootBeanDefinition(DomainEventPublisherTask.class)
                        .addPropertyValue("partition", i)
                        .addPropertyValue("role", role)
                        .addPropertyReference("groupMember", "groupMember")
                        .addPropertyValue("totalNumberOfPartitions", this.domainEventsPartitionNumber)
                        .addAutowiredProperty("exporter");

            ((DefaultListableBeanFactory) beanFactory).
                    registerBeanDefinition(exporterName, taskBuilder.getBeanDefinition());

        }
    }

    @Override
    @Nullable
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {

        if (beanName.equals(IntegrationContextUtils.INTEGRATION_LIFECYCLE_ROLE_CONTROLLER)) {
            SmartLifecycleRoleController roleController = (SmartLifecycleRoleController) bean;
            this.rolesToExporterNames.forEach((role, name) -> {
                roleController.addLifecycleToRole(role, name);
            });
        }

        return bean;
    }

//    @Override
//    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
//        for (int i = 0; i < DOMAIN_EVENTS_PARTITION_NUMBER; i++) {
//
//            BeanDefinitionBuilder b =
//                    BeanDefinitionBuilder.rootBeanDefinition(LeaderInitiatorFactoryBean.class)
//                            .addPropertyValue("client", registry.getBeanDefinition("curatorClient").)
//                            .addPropertyValue("path", "/domainEvents/" + DOMAIN_NAME + "/" + i + "/")
//                            .addPropertyValue("role", "domainEventExporter_" + i);
//
//
//            registry.registerBeanDefinition("domainEventExporterLeaderInitiator_" + i, b.getBeanDefinition());
//
//        }
//    }
//
//    @Override
//    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
//        beanFactory.addBeanPostProcessor();
//    }
}
