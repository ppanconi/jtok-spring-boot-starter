package com.jtok.spring.domainevent;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface DomainEventRepository extends CrudRepository<DomainEvent, String> {

    List<DomainEvent> findByDomainPartitionAndOffSetNullOrderByEventTsMils(
            int domainPartition
    );
}
