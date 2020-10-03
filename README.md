# spring-jtok

![JToK](jtok.png?raw=true "JToK")

Spring-jtok is a Java Spring library to implement Event Driven Microservices. 

It propagates Spring Domain Events to remote services using Apache Kafka. 
Spring JPA Domain Entities can emit events using Spring Application Events 
support, @DomainEvents and @AfterDomainEventPublication annotations 
([@see also org.springframework.data.domain.AbstractAggregateRoot](https://docs.spring.io/spring-data/commons/docs/current/api/org/springframework/data/domain/AbstractAggregateRoot.html)). 
JToK issues these events to auto configured Apache Kafka topics.

## Features 
 - Total transparency to Spring Event Application model 
 - Automatic kafka stuffs creation and configuration, including topics, consumers and producers
 - Automatic application event handlers failures management with automatic retries and dead letters configuration
 - Embedded partitioned Transactional Outbox Service with automatic distributed consensual balancing
 
 ![Partitioned Transactional Outbox](top.gif?raw=true "JToK")

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
@EntityScan({"your.application.entity.package"})
public class DepotApplication {

	public static void main(String[] args) {
		SpringApplication.run(DepotApplication.class, args);
	}

}

``` 

You have also to add EnableJpaRepositories and EntityScan("\<\<application package\>\>"") annotations.

Configure application settings properties (application.properties or equivalent file)

```properties
############################################
# JToK domain events publisher configs
# application domain name
jtok.domain.name=depot
# number of outbox transaction table partitions
jtok.domain.partitions=3 
# zookeeper connection string, for leader publisher tasks election
jtok.pub.zookeeperQuorum=localhost:2183,localhost:2182,localhost:2181

############################################
# JToK external events subscriber configs
# comma separated name for topics  to subscribe
jtok.external.domain.topics=ecommerce.order_created

# spring kafka configurations
spring.kafka.consumer.bootstrap-servers=localhost:9092,localhost:9093,localhost:9094
spring.kafka.producer.bootstrap-servers=localhost:9092,localhost:9093,localhost:9094

```

#### Publish Events

To publish events from your Agregate, make your Aggragate root classes extend 
`org.springframework.data.domain.AbstractAggregateRoot` and your business transactional 
methods emit events using `registerEvent(event)`

```java

@Entity
@Table(name = "Accounts")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentAccount extends AbstractAggregateRoot {

    ...
	
    public int place(List<OperationArticleItem> items) {

	//do your business
	...
	registerEvent(DomainEvent.builderWithRef()
                    .key(refKey)
                    .domainEventType(PaymentsEvent.PAYMENTS_OPERATION_ADDED)
                    .ref(refType)
                    .applicationPayload(new HashMap<String, Object>(){{
                        put("refKey", refKey);
                        put("operation", "charge");
                        put("amount", amount);
                    }})
                    .build()
        );    
	
	...
    }
    
    ...
}
```

to receive and handle events from external domains use 
`@org.springframework.context.event.EventListener` 
annotation on your service transactional business methods

```java
@Service
public class AccountService {

    @Transactional
    @EventListener(condition = "#event.name == 'ecommerce.ORDER_TO_BE_PAYED'")
    public void handleECommerceOrderCreated(ExternalDomainEvent event) {

        JSONObject payload = event.getPayload();
        String userId = payload.getAsString("customer");
        String orderId = payload.getAsString("globalId");
        Number granTotal = payload.getAsNumber("granTotal");

        BigDecimal charge = new BigDecimal(granTotal.doubleValue()).multiply(new BigDecimal("-1.00"));

	//do charge business
        int status = addCharge(userId, charge, event.getName(), event.getId(), orderId);
	
	...
   }

}

```
