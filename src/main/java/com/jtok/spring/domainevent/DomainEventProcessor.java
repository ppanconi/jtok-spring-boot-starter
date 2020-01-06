package com.jtok.spring.domainevent;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.UUID;

@Service
@AllArgsConstructor
public class DomainEventProcessor {

    DomainEventRepository repository;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleDomainEvent(DomainEvent event) {

        String eventId = UUID.randomUUID().toString();
        event.setId(eventId);

        repository.save(event);

    }
}
