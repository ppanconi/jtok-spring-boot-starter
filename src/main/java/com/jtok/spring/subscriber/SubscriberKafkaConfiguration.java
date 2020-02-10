package com.jtok.spring.subscriber;

import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.Acknowledgment;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class SubscriberKafkaConfiguration implements ApplicationEventPublisherAware {

    private static final Logger log = LoggerFactory.getLogger(SubscriberKafkaConfiguration.class);

    private ApplicationEventPublisher applicationEventPublisher;

    @Value("${jtok.domain.name}")
    String domainName;

    @Bean
    KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, String>>
        kafkaListenerContainerFactory(ConsumerFactory<String, String> consumerFactory) {

        log.info("Kafka Consumer configs " + consumerFactory.getConfigurationProperties());

        Map<String, Object> props = new HashMap<>(consumerFactory.getConfigurationProperties());
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, domainName);

        ConsumerFactory<String, String> myConsumerFactory = new DefaultKafkaConsumerFactory<>(props,
                new StringDeserializer(), new StringDeserializer());

        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(myConsumerFactory);
        factory.setConcurrency(3);
        factory.getContainerProperties().setPollTimeout(3000);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        factory.getContainerProperties().setGroupId(domainName);

        return factory;
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }


    @KafkaListener(topics = "#{'${jtok.external.domain.topics}'.split(',')}")
    public void listen(String message, Acknowledgment acknowledgment) {

        try {

            JSONObject data = new JSONParser(JSONParser.DEFAULT_PERMISSIVE_MODE)
                    .parse(message, JSONObject.class);

            String eventName = "" + data.get("domain") + "." + data.get("event");
            ExternalDomainEvent externalDomainEvent = ExternalDomainEvent.builder()
                    .id(data.getAsString("eventId"))
                    .key(data.getAsString("key"))
                    .name(eventName)
                    .refName(data.getAsString("refEvent"))
                    .payload((JSONObject)data.get("payload"))
                    .build();

            applicationEventPublisher.publishEvent(externalDomainEvent);

            acknowledgment.acknowledge();

        } catch (Exception e) {
            log.error("Error during event elaboration " + message + ": " + e.getMessage(), e);
            throw new RuntimeException(e);
        }


    }

}
