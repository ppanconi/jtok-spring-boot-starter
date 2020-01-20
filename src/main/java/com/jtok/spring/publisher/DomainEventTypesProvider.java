package com.jtok.spring.publisher;

import com.jtok.spring.domainevent.DomainEventType;

import java.util.List;

public interface DomainEventTypesProvider {

    List<DomainEventType> provideDomainEventTypes();

}
