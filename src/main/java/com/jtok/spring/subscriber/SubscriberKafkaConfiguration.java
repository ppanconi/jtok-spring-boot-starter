package com.jtok.spring.subscriber;

import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.TopicPartition;
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
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.SeekToCurrentErrorHandler;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.util.backoff.ExponentialBackOff;

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
        kafkaListenerContainerFactory(ConsumerFactory<String, String> consumerFactory, KafkaTemplate<String, String> kafkaTemplate) {

        log.info("Default Kafka Consumer configs " + consumerFactory.getConfigurationProperties());

        Map<String, Object> props = new HashMap<>(consumerFactory.getConfigurationProperties());
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, domainName);
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 60_000); //1 minutes

        log.info("JTOK Kafka Consumer configs " + props);

        ConsumerFactory<String, String> myConsumerFactory = new DefaultKafkaConsumerFactory<>(props,
                new StringDeserializer(), new StringDeserializer());

        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(myConsumerFactory);
        factory.setConcurrency(3);
        factory.getContainerProperties().setPollTimeout(3000);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        factory.getContainerProperties().setGroupId(domainName);
        factory.getContainerProperties().setAckOnError(false);
        ExponentialBackOff backOff = new ExponentialBackOff();

        backOff.setMaxInterval(30000);
        backOff.setMaxElapsedTime(45000);

        SeekToCurrentErrorHandler errorHandler = new SeekToCurrentErrorHandler(
                new DeadLetterPublishingRecoverer(kafkaTemplate,
                        (r, e) -> new TopicPartition(domainName + "." + r.topic() + ".DLT", -1)),
                backOff
        );

        errorHandler.setCommitRecovered(true);

        factory.setErrorHandler(errorHandler);

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
