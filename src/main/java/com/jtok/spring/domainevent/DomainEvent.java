package com.jtok.spring.domainevent;

import lombok.*;

import org.springframework.lang.Nullable;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Map;

@Entity
@Table(name = "Domain_Events")
@Data
@AllArgsConstructor
@RequiredArgsConstructor
@NoArgsConstructor
@Builder
public class DomainEvent implements Serializable {

    @Id
    String id;

    @Column(nullable = false)
    @NonNull
    String key;

    @NonNull
    @Transient
    DomainEventType eventType;

    @Column(nullable = false)
    @Access(AccessType.PROPERTY)
    public String getDomainEvent() {
        if (getEventType() == null) {
            return null;
        }
        return "" + getEventType().getClass().getName() + "@" + getEventType().name();
    }

    public void setDomainEvent(String str) {
        String[] parts = str.split("@");

        try {
            Class clz = Class.forName(parts[0]);
            Object e = Enum.valueOf(clz, parts[1]);
            setEventType( (DomainEventType)e);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Column(nullable = false)
    @Nullable
    int domainPartition;

    @Column(nullable = false)
    @Nullable
    int topicPartition;

    @Column(nullable = true, length = 1024)
    @Nullable
    String eventData;

    @Column(nullable = false)
    @Nullable
    long eventTsMils;

    @Column(nullable = true)
    @Nullable
    Long offSet;

    @Transient
    @Nullable
    Map<String, Object> applicationPayload;

}
