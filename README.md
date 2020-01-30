# spring-jtok

![JToK](jtok.png?raw=true "JToK")

Spring-jtok is Java Spring library to implement Event Driven Microservices. 

It propagates Spring Domain Events to remote services using Apache Kafka. 
Spring JPA Domain Entities can emit events using Spring Application Events 
support and @DomainEvents and @AfterDomainEventPublication annotations 
(@see also org.springframework.data.domain.AbstractAggregateRoot). 
JToK public these events to configured Apache Kafka topics.

## Getting Started
Spring-jtok can be used in spring boot jpa application.

Use spring-boot cli to create a new project:

`spring init --build=gradle --dependencies=data-jpa,webflux,postgresql,lombok,data-rest --groupId=it.plansoft depot` 

Add spring-jtok dependency to the new created project using jitpack virtual repository :

```groovy
    repositories {
        jcenter()
        maven { url "https://jitpack.io" }
    }
```

and add JToK jitpack dependency:

```groovy
    implementation 'com.github.ppanconi:spring-jtok:master-SNAPSHOT'
```

JToK uses Spring Kafka at version 2.4.1.RELEASE and kafka client 2.4.0 so make sure
your project use these versions

```groovy
    implementation 'org.springframework.kafka:spring-kafka:2.4.1.RELEASE'
    implementation 'org.apache.kafka:kafka-clients:2.4.0'
```

In main configuration enable JToK domain event publisher or/and
JTok external domain event subscriber:

```java
@SpringBootApplication
@EnableExternalDomainEventSubscriber
@EnableDomainEventPublisher
@EnableJpaRepositories
@EntityScan({"it.plansoft.depot"})
public class DepotApplication {

	public static void main(String[] args) {
		SpringApplication.run(DepotApplication.class, args);
	}

}

``` 

You have also do add EnableJpaRepositories and EntityScan("\<\<application package\>\>"") annotations.

Configure the application setting properties in application.properties or equivalent files

```properties
############################################
# JToK domain events publisher configs
# application domain name
jtok.domain.name=depot
# number of outbox transaction table partitions
jtok.domain.partitions=3 
# zookeeper connection string for leader publisher tasks election
jtok.pub.zookeeperQuorum=localhost:2183,localhost:2182,localhost:2181

############################################
# JToK external events subscriber configs
# comma separated topics name to subscribe
jtok.external.domain.topics=ecommerce.order_created

# spring kafka configurations
spring.kafka.consumer.bootstrap-servers=localhost:9092,localhost:9093,localhost:9094
spring.kafka.producer.bootstrap-servers=localhost:9092,localhost:9093,localhost:9094

```

