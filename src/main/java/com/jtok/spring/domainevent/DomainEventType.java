package com.jtok.spring.domainevent;

public interface DomainEventType {

    String name();

    int topicPartitions();

    String topic();

}
