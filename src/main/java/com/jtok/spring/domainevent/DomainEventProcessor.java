package com.jtok.spring.domainevent;

import net.minidev.json.JSONObject;
import org.apache.kafka.common.utils.Utils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.HashMap;
import java.util.UUID;

@Service
public class DomainEventProcessor {

    DomainEventRepository repository;

    public DomainEventProcessor(DomainEventRepository repository) {
        this.repository = repository;
    }

    @Value("${jtok.domain.partitions}")
    int domainPartitions;

    @Value("${jtok.domain.name}")
    String domainName;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleDomainEvent(DomainEvent event) {

        int topicPartition = Utils.toPositive(Utils.murmur2(event.getKey().getBytes())) % event.getDomainEventType().topic().topicPartitions();
        int domainPartition = topicPartition % domainPartitions;
        String eventId = UUID.randomUUID().toString();

        event.setId(eventId);
        event.setDomainPartition(domainPartition);
        event.setTopic(domainName + "." + event.getDomainEventType().topic().topicName());
        event.setTopicPartition(topicPartition);
        event.setEventTsMils(System.currentTimeMillis());
        event.setEventData(JSONObject.toJSONString(
                new HashMap<String, Object>() {{
                    put("domain", domainName);
                    put("event", event.getDomainEventType().name());
                    put("eventId", event.getId());
                    put("key", event.getKey());
                    put("refEvent", event.getRefEvent());
                    put("payload", event.getApplicationPayload());
                }}
        ));

        repository.save(event);

    }
}
