package com.jtok.spring.publisher;

public interface DomainEventPublisher {
    void export(int partition);
}
