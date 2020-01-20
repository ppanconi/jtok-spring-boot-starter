package com.jtok.spring.domainevent;

public interface DomainEventTopicInfo {

    String topicName();

    int topicPartitions();

    int topicReplications();

}
