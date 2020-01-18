package com.jtok.spring.publisher;

import com.jtok.spring.domainevent.DomainEvent;
import com.jtok.spring.domainevent.DomainEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
public class DomainEventPublisherKafka implements DomainEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(DomainEventPublisherKafka.class);

    private DomainEventRepository repository;
    private KafkaTemplate<String, String> kafkaTemplate;

    public DomainEventPublisherKafka(DomainEventRepository repository, KafkaTemplate<String, String> kafkaTemplate) {
        this.repository = repository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Value("${jtok.domain.name}")
    String domainName;

    @Transactional
    public void export(int partition) {
        Iterable<DomainEvent> events = repository.findByDomainPartitionAndOffSetNullOrderByEventTsMils(partition);

        List<CompletableFuture<SendResult<String, String>>> completableFutureList = new ArrayList<>();

        events.forEach(event -> {

            ListenableFuture<SendResult<String, String>> listenableFuture = kafkaTemplate.send(
                    domainName + "." + event.getEventType().topic(),
                    event.getTopicPartition(),
                    event.getEventTsMils(),
                    event.getKey(),
                    event.getEventData());

            listenableFuture.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {
                @Override
                public void onFailure(Throwable ex) {
                    throw new RuntimeException("Error sending data to kafka: " + ex.getMessage(), ex);
                }

                @Override
                public void onSuccess(SendResult<String, String> result) {
                    log.info("delivered event to kafka to offset " + result.getRecordMetadata().offset());
                    event.setOffSet(result.getRecordMetadata().offset());
                }
            });

            completableFutureList.add(listenableFuture.completable());

        });

        kafkaTemplate.flush();

        try {
            CompletableFuture.allOf(completableFutureList.toArray(new CompletableFuture[completableFutureList.size()]))
                    .get(30, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("Error sending data to kafka " + e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

}
