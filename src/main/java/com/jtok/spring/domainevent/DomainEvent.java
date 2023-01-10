package com.jtok.spring.domainevent;

import lombok.*;

import org.springframework.lang.Nullable;

//import javax.persistence.*;
import java.io.Serializable;
import java.util.Map;

//@Entity
//@Table(name = "Domain_Events")
@Data
@NoArgsConstructor
public class DomainEvent implements Serializable {

//    @Id
    String id;

//    @Column(nullable = false)
    @NonNull
    String key;

//    @Column(nullable = false)
    String domainEvent;

//    @Column(nullable = true)
    @Nullable
    String refEvent;

//    @Column(nullable = false)
    int domainPartition;

//    @Column(nullable = false)
    String topic;

//    @Column(nullable = false)
    int topicPartition;

//    @Column(nullable = true, length = 1024)
    String eventData;

//    @Column(nullable = false)
    long eventTsMils;

//    @Column(nullable = true)
    @Nullable
    Long offSet;

    @NonNull
//    @Transient
    public DomainEventType getDomainEventType() {
        String[] parts = getDomainEvent().split("@");

        try {
            Class clz = Class.forName(parts[0]);
            Object e = Enum.valueOf(clz, parts[1]);
            return (DomainEventType) e;
        } catch (Exception e) {
            throw new RuntimeException("Error building DomainEventType " + getDomainEvent(), e);
        }
    }

    public void setDomainEventType(DomainEventType domainEventType) {
        setDomainEvent(domainEventType.getClass().getName() + "@" + domainEventType.name());
    }


//    @Transient
    @Nullable
    private Map<String, Object> applicationPayload;


    @Builder
    public static DomainEvent newDomainEvent(String key, DomainEventType domainEventType,
                                             Map<String, Object> applicationPayload) {
        DomainEvent domainEvent = new DomainEvent();
        domainEvent.setKey(key);
        domainEvent.setDomainEventType(domainEventType);
        domainEvent.setApplicationPayload(applicationPayload);

        return domainEvent;
    }

    @Builder(builderClassName = "BuilderWithRef", builderMethodName = "builderWithRef")
    public static DomainEvent newDomainEventWithRef(String key, DomainEventType domainEventType, String ref,
                                             Map<String, Object> applicationPayload) {
        DomainEvent domainEvent = new DomainEvent();
        domainEvent.setKey(key);
        domainEvent.setDomainEventType(domainEventType);
        domainEvent.setRefEvent(ref);
        domainEvent.setApplicationPayload(applicationPayload);

        return domainEvent;
    }

}
