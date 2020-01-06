package com.jtok.spring.domainevent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.NonNull;

import org.springframework.lang.Nullable;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "Domain_Events")
@Data
@AllArgsConstructor
@RequiredArgsConstructor
@NoArgsConstructor
public class DomainEvent implements Serializable {

    @Id
    String id;

    @Column(nullable = false)
    @NonNull
    String key;

    @Column(nullable = false)
    @NonNull
    DomainEventType eventType;

    @Column(nullable = false)
    @Nullable
    int domainPartition;

    @Column(nullable = false)
    @Nullable
    int topicPartition;

    @Column(nullable = true)
    @Nullable
    String eventData;

    @Column(nullable = false)
    @NonNull
    long eventTsMils;
}
