package com.jtok.spring.domainevent;

public enum DomainEventType {


    ORDER_CREATED("order_created", 5);

    String topic;
    int topicPartitions;

    public String getTopic() {
        return topic;
    }

    public int getTopicPartitions() {
        return topicPartitions;
    }

    DomainEventType(String topic, int topicPartitions) {
        this.topic = topic;
        this.topicPartitions = topicPartitions;
    }
}
