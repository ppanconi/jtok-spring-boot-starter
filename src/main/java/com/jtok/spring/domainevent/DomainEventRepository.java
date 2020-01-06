package com.jtok.spring.domainevent;

import org.springframework.data.repository.CrudRepository;

public interface DomainEventRepository extends CrudRepository<DomainEvent, String> {
}
