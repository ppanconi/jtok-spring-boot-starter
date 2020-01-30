# spring-jtok

![JToK](jtok.png?raw=true "JToK")

Spring-jtok is Java Spring library to implemet Event Driven Micorservices. 

It propagates Spring Domain Events to remote services using Apache Kafka. Spring JPA Doman Entities can emit events using Spring Application Events support and @DomainEvents and @AfterDomainEventPublication annotations (@see also org.springframework.data.domain.AbstractAggregateRoot). JToK publics these events to configured Apache Kafka topics. 

